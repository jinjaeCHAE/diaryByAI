/**
 * Copyright 2015 IBM Corp. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

var express = require('express'); // app server
var bodyParser = require('body-parser'); // parser for post requests
var AssistantV2 = require('ibm-watson/assistant/v2'); // watson sdk
var IamAuthenticator = require('ibm-watson/auth').IamAuthenticator;

var Actions = require('./functions/actions');
var actions = new Actions();

var SearchDocs = require('./functions/searchDocs');
var searchDocs = new SearchDocs();

var BankFunctions = require('./functions/bankFunctions');
var bankFunctions = new BankFunctions();

var mysql = require('mysql');
var db_config = require('./config/db-config.json')

const axios = require('axios');
var urlencode = require('urlencode');

const { response } = require('express');
const { connected } = require('process');
const { type, freemem } = require('os');
const { connect } = require('http2');
var app = express();
app.use(bodyParser.json({ limit: '200mb' }));
app.use(bodyParser.urlencoded({ limit: '200mb', extended: true }));
app.use(bodyParser.text({ limit: '200mb' }));
//DB 연결
var connection = mysql.createConnection({
  host: db_config.host,
  user: db_config.user,
  database: db_config.database,
  password: db_config.password,
  port: db_config.port
});

var connection;
function handleDisconnect() {
  connection.connect(function onConnect(err) {   // The server is either down
    if (err) {                                  // or restarting (takes a while sometimes).
      console.log('error when connecting to db:', err);
      setTimeout(handleDisconnect, 10000);    // We introduce a delay before attempting to reconnect,
    }                                           // to avoid a hot loop, and to allow our node script to
  });                                             // process asynchronous requests in the meantime.
  // If you're also serving http, display a 503 error.
  connection.on('error', function onError(err) {
    console.log('db error', err);
    if (err.code == 'PROTOCOL_CONNECTION_LOST') {   // Connection to the MySQL server is usually
      handleDisconnect();                         // lost due to either server restart, or a
    } else {                                        // connnection idle timeout (the wait_timeout
      throw err;                                  // server variable configures this)
    }
  });
}
handleDisconnect();

function chatBubbleInsert(sessionId, chat_text) {
  var bubble_sql_0 = 'INSERT INTO Bubbles (is_user,session_id,bubble) VALUE (0,?,?)';
  connection.query(bubble_sql_0, [sessionId, chat_text], function (err, result) {
    if (err) throw "chat_text error..." + err;
    console.info('chat_text bubble inserted!');
  });
};
function userBubbleInsert(sessionId, user_text) {
  var bubble_sql_1 = 'INSERT INTO Bubbles (is_user,session_id,bubble) VALUE (1,?,?)';
  connection.query(bubble_sql_1, [sessionId, user_text], function (err, result) {
    if (err) throw "user_text error..." + err;
    console.info('user_text bubble inserted!');
  });
};
function entityInsert(params) {
  var sql = 'UPDATE Entity SET intent=?, what_col=?, who_col=?, when_col=?, why_col=?,where_col=?, how_col=? WHERE session_id=?';
  connection.query(sql, params, function (err, result) {
    if (err) throw "column insert error!" + err;
    console.info('column inserted!');
  });
}

function Josa(txt, josa) {
  var code = txt.charCodeAt(txt.length - 1) - 44032;
  var cho = 19, jung = 21, jong = 28;
  var i1, i2, code1, code2;
  // 원본 문구가 없을때는 빈 문자열 반환
  if (txt.length == 0) return '';
  // 한글이 아닐때
  if (code < 0 || code > 11171) return txt;
  if (code % 28 == 0) return txt + Josa.get(josa, false);
  else return txt + Josa.get(josa, true);
}
Josa.get = function (josa, jong) {
  // jong : true면 받침있음, false면 받침없음
  if (josa == '을' || josa == '를') return (jong ? '을' : '를');
  if (josa == '이' || josa == '가') return (jong ? '이' : '가');
  if (josa == '은' || josa == '는') return (jong ? '은' : '는');
  if (josa == '와' || josa == '과') return (jong ? '과' : '와');
  // 알 수 없는 조사
  return '**';
}

// Bootstrap application settings
app.use(express.static('./public')); // load UI from public folder
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// Create the service wrapper
var assistant = new AssistantV2({
  version: '2019-02-28',
  authenticator: new IamAuthenticator({
    apikey: process.env.ASSISTANT_IAM_APIKEY
  }),
  url: process.env.ASSISTANT_URL,
});

var date = new Date();
date.setMonth(date.getMonth() + 1);
var initContext = {
  skills: {
    'main skill': {
      user_defined: {
        acc_minamt: 50,
        acc_currbal: 430,
        acc_paydue: date.getFullYear() + '-' + (date.getMonth() + 1) + '-26 12:00:00',
        accnames: [
          5624,
          5893,
          9225,
        ]
      }
    }
  }
};

/*
 * Endpoint to be call from the client side.
 * Required.body.firstcall is set when initialising chat and sends initial context (initContext)
 * Context is then set when required for actions.
 */

app.get('/api/date', function (req, res) {
  var userId = req.query.user_id;
  var year = req.query.year;
  var month = req.query.month;
  console.log(req.query);
  var sql = 'SELECT DATE(dateTime) as d FROM Diary WHERE user_id=? AND DATE(dateTime) like ?'
  console.log(userId, year, month);
  if (!year || !month || !userId) {
    return res.status(err.code || 500).send();
  }

  var params = [userId, year + "-" + month + "%"];
  connection.query(sql, params, function (err, result) {
    if (err) {
      return res.status(err.code || 500).json(err);
    }
    let response = {};
    response.dates = new Set();
    for (let i = 0; i < result.length; i++) {
      response.dates.add(result[i].d.getDate());
    }
    response.dates = Array.from(response.dates);
    return res.json(response);
  });
});

// 챗봇 대화
app.post('/api/message', function (req, res) {
  var assistantId = process.env.ASSISTANT_ID || '<assistant-id>';
  var sessionId = req.body.session_id;
  var userId = req.body.user_id;
  if (!assistantId || assistantId === '<assistant-id>') {
    return res.json({
      'output': {
        'text': 'The app has not been configured with a <b>ASSISTANT_ID</b> environment variable. Please refer to the ' + '<a href="https://github.com/watson-developer-cloud/assistant-intermediate">README</a> documentation on how to set this variable. <br>' + 'Once a workspace has been defined the intents may be imported from ' + '<a href="https://github.com/watson-developer-cloud/assistant-intermediate/blob/master/training/banking_workspace.json">here</a> in order to get a working application.'
      }
    });
  }

  var textIn = '';

  if (req.body.input) {
    textIn = req.body.input.text;
  }
  var payload = {
    assistantId: assistantId,
    sessionId: req.body.session_id,
    input: {
      message_type: 'text',
      text: textIn,
    }
  };

  if (req.body.firstCall || req.body.context) {
    payload.context = req.body.context || initContext;
  }

  // Send the input to the assistant service
  assistant.message(payload, function (err, data) {
    if (err) {
      return res.status(err.code || 500).json(err);
    }
    searchDocs.addDocs(data.result, function () {
      actions.testForAction(data.result, req.body.session_id).then(function (d) {
        let chat_text = d.output.generic[0].text;
        let user_text = textIn;

        // entity init
        if (d.output.entities.length == 0) {
          if (textIn != '') {
            userBubbleInsert(sessionId, user_text);
          } else {
            connection.query('INSERT INTO Entity (session_id) VALUE (?)', sessionId, function (err, result) {
              if (err) throw "session_id error..." + err;
              console.info('session_id inserted!');
            })
          }
          chatBubbleInsert(sessionId, chat_text);
        }

        //get entity, update entity 
        let sql = 'SELECT * FROM Entity where session_id=?';
        var intent = '', what = '', who = '', when = '', why = '', where = '', how = '';
        var is_full = false;
        connection.query(sql, sessionId, function (err, result) {
          if (err) throw 'err;;' + err;
          if (result.length == 0) {
            return;
          }
          intent = result[0].intent;
          what = result[0].what_col;
          who = result[0].who_col;
          when = result[0].when_col;
          why = result[0].why_col;
          where = result[0].where_col;
          how = result[0].how_col;

          let getEntity = d.output.entities;
          let getIntent = d.output.intents;

          for (let i = 0; i < getIntent.length; i++) {
            let int = getIntent[i].intent;
            intent = int;
          }
          for (let i = 0; i < getEntity.length; i++) {
            let en = getEntity[i].entity;
            let val = getEntity[i].value;
            if (en == 'what') what = val;
            if (en == 'where') where = val;
            if (en == 'when') when = val;
            if (en == 'who') who = val;
            if (en == 'why') why = val;
            if (en == 'how') how = val;
          }

          let params = [intent, what, who, when, why, where, how, sessionId];
          console.log(params);
          entityInsert(params);



          if (intent, what, who, when, why, where, how != null) {
            is_full = true;
          }

          console.log("1234" + res);
          userBubbleInsert(sessionId, user_text);
          chatBubbleInsert(sessionId, chat_text);

          d.is_full = is_full;

          return res.json(d);

        });
      }).catch(function (error) {
        return res.json(error);
      });
    });
  });
});

app.post('/api/delete', function (req, res) {
  let userId = req.body.user_id;
  let year = req.body.year;
  let month = req.body.month;
  let date = req.body.date;
  console.log(req.body);
  if (!year || !month || !date || !userId) {
    return res.status(err.code || 500).send();
  }

  let dateString = year + "-" + month + "-" + date;
  let sql = "DELETE FROM Diary WHERE DATE(dateTime)=? AND user_id=?;";
  let params = [dateString, userId];
  connection.query(sql, params, function (err, result) {
    if (err) {
      return res.status(err.code || 500).json(err);
    }
    console.log("delete diary");
    return res.status(200).send();
  });


});

app.post('/api/update', function (req, res) {
  let userId = req.body.user_id;
  let year = req.body.year;
  let month = req.body.month;
  let date = req.body.date;
  let image = req.body.image;
  let content = req.body.content;

  if (!year || !month || !date || !userId) {
    return res.status(err.code || 500).send();
  }

  let dateString = year + "-" + month + "-" + date;

  let sql = "UPDATE Diary SET image=?, content=? WHERE DATE(dateTime)=? AND user_id=?";
  let params = [image, content, dateString, userId];
  connection.query(sql, params, function (err, result) {
    if (err) {
      return res.status(err.code || 500).json(err);
    }
    console.log("update diary");
    return res.status(200).send();
  });
});

// 채팅 종료 요청 -> 일기 테이블 데이터 추가
app.post('/api/finish', function (req, res) {
  let sessionId = req.body.session_id;
  let userId = req.body.user_id;
  console.log(sessionId, userId);
  if (!sessionId || !userId) {
    return res.status(400).send();
  }
  connection.query('SELECT * FROM Entity WHERE session_id = ?', sessionId, function (err, rows) {
    // var url = 'http://3.35.104.72:5000/diary/'
    let diary = {}
    diary.intent = rows[0].intent;
    diary.what = rows[0].what_col;
    diary.where = rows[0].where_col;
    diary.who = rows[0].who_col;
    diary.when = rows[0].when_col;
    diary.how = rows[0].how_col;
    diary.why = rows[0].why_col;

    if (err) throw 'error...';
    console.log(rows[0]);
    let concat = '';
    if (rows[0].intent == '여행') {
      if (rows[0].who_col == '혼자') concat += '오늘은 ' + rows[0].who_col;
      else concat += '오늘은 ' + Josa(rows[0].who_col, '와');
      concat += ' ' + rows[0].where_col + '에';
      concat += ' ' + Josa(rows[0].what_col, '을') + ' 왔다.';
      concat += ' ' + rows[0].when_col + '에 갔던';
      concat += ' ' + rows[0].why_col;
      concat += ' ' + rows[0].how_col + '다';
    }
    else if (rows[0].intent == '공부') {
      if (rows[0].who_col == '혼자') concat += '오늘은 ' + rows[0].who_col;
      else concat += '오늘은 ' + Josa(rows[0].who_col, '와');
      concat += ' ' + rows[0].where_col + '에서';
      if (rows[0].when_col == '하루종일') concat += ' ' + rows[0].when_col;
      else concat += ' ' + rows[0].when_col + '에';
      concat += ' ' + rows[0].what_col + ' 공부를 했다. ';
      concat += '오늘 ' + rows[0].why_col + '서';
      concat += ' ' + rows[0].how_col + '다';
    }
    else if (rows[0].intent == '운동') {
      concat += '오늘 ' + rows[0].when_col + '에';
      if (rows[0].who_col == '혼자') concat += ' ' + rows[0].who_col;
      else concat += ' ' + Josa(rows[0].who_col, '와');
      concat += ' ' + rows[0].where_col + '에서';
      concat += ' ' + Josa(rows[0].what_col, '을') + ' 했다.';
      concat += ' ' + rows[0].why_col + '서';
      concat += ' 너무 ' + rows[0].how_col + '다';
    }
    else if (concat.length == 0) {
      if (rows[0].why_col) concat += rows[0].why_col + '해서';
      if (rows[0].what_col) concat += ' ' + Josa(rows[0].what_col, '을');
      if (rows[0].where_col) concat += ' ' + rows[0].where_col + '에서';
      if (rows[0].who_col) concat += ' ' + Josa(rows[0].who_col, '와');
      if (rows[0].when_col) concat += ' ' + rows[0].when_col + '에';
      if (rows[0].how_col) concat += ' ' + rows[0].how_col;
      concat += '다.';
    }

    console.log(concat);
    diary.text = concat;

    let diary_sql = 'INSERT INTO Diary (session_id, user_id, content)VALUE (?,?,?)';
    let diary_params = [sessionId, userId, concat];

    connection.query(diary_sql, diary_params, function (err, result) {
      if (err) throw 'diary insert err' + err;
      console.info('diary 테이블 insert 성공');
      return res.json(diary);
    });



    // console.log(concat);

    // let diary_sql = 'INSERT INTO Diary (session_id, user_id, content)VALUE (?,?,?)';
    // let diary_params = [sessionId,userId,concat];
    // connection.query(diary_sql,diary_params,function(err,result){
    // if (err) throw 'diary insert err'+err;
    // console.info('diary 테이블 insert 성공');
    // return res.json(concat);
  });
});


// 특정 날짜의 일기 받기 
app.get('/api/diary', (req, res) => {
  let userId = req.query.user_id;
  let dateTime = req.query.dateTime; //'2020-12-08' 같은 형식
  let sql = 'SELECT * FROM Diary WHERE user_id=? AND Date(dateTime)=? order by dateTime desc';
  let params = [userId, dateTime]
  let ret = {};
  console.log(params);
  if (!userId || !dateTime) {
    return res.status(400).send();
  }
  connection.query(sql, params, function (err, result) {
    if (err) throw 'err;;' + err;
    if (result.length != 0) {
      ret.dateTime = result[0].dateTime;
      ret.content = result[0].content;
      ret.image = result[0].image;
      connection.query('SELECT * FROM Entity WHERE session_id = ?', result[0].session_id, function (err, rows) {
        ret.intent = rows[0].intent;
        ret.what = rows[0].what_col;
        ret.where = rows[0].where_col;
        ret.who = rows[0].who_col;
        ret.when = rows[0].when_col;
        ret.how = rows[0].how_col;
        ret.why = rows[0].why_col;
        return res.json(ret);
      });
    } else {
      return res.status(400).send();
    }
  });
});

app.get('/bank/validate', function (req, res) {
  var value = req.query.value;
  var isAccValid = bankFunctions.validateAccountNumber(Number(value));
  // if accountNum is in list of valid accounts
  if (isAccValid === true) {
    res.send({ result: 'acc123valid' });
  } else {
    // return invalid by default
    res.send({ result: 'acc123invalid' });
  }
});

app.get('/bank/locate', function (req, res) {
  res.send({ result: 'zip123retrieved' });
});

app.get('/api/session', function (req, res) {
  assistant.createSession({
    assistantId: process.env.ASSISTANT_ID || '{assistant_id}',
  }, function (error, response) {
    if (error) {
      return res.send(error);
    } else {
      return res.send(response);
    }
  });
});

app.post('')
module.exports = app;