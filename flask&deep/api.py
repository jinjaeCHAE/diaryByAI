from flask import Flask
from flask_restful import Resource, Api 

import math

import gluonnlp as nlp
import mxnet as mx
import pandas as pd
from gluonnlp.data import SentencepieceTokenizer
from kogpt2.mxnet_kogpt2 import get_mxnet_kogpt2_model
from kogpt2.utils import get_tokenizer
from mxnet import gluon, nd
from mxnet.gluon import nn
import papago
from quickdraw import QuickDrawData
import base64
from io import BytesIO
from PIL import Image

tr = papago.Translator(client_id='c0g2APLkKGr6vd1AlCmh',client_secret='VwFjRq2R7X')
qd = QuickDrawData()

app = Flask(__name__) 
api = Api(app)

U_TKN = '<usr>'
S_TKN = '<sys>'
BOS = '<s>'
EOS = '</s>'
MASK = '<unused0>'
SENT = '<unused1>'

class KoGPT2Chat(nn.HybridBlock):
    def __init__(self, kogpt2, prefix=None, params=None):
        super(KoGPT2Chat, self).__init__(prefix=prefix, params=params)
        self.kogpt2 = kogpt2

    def hybrid_forward(self, F, inputs):
        # (batch, seq_len, hiddens)
        output, _ = self.kogpt2(inputs)
        return output


if mx.context.num_gpus() > 0:
    ctx = mx.gpu()
else:
    ctx = mx.cpu()
    
tok_path = get_tokenizer()
model, vocab = get_mxnet_kogpt2_model(ctx=ctx)
tok = SentencepieceTokenizer(tok_path, num_best=0, alpha=0)
kogptqa = KoGPT2Chat(model)
# kogptqa.load_parameters("Kogpt2_chat.params", ctx=ctx)
def chat(text="",sent='0'):
    sent_tokens = tok(sent)
    cnt=0
    q = text.strip()
    q_tok = tok(q)
    a = ''
    a_tok = []
    while cnt>25:
        cnt+=1
        input_ids = mx.nd.array([vocab[U_TKN]] + vocab[q_tok] +
                                vocab[EOS, SENT] + vocab[sent_tokens] +
                                vocab[EOS, S_TKN] +
                                vocab[a_tok]).expand_dims(axis=0)
        pred = kogptqa(input_ids.as_in_context(ctx))
        gen = vocab.to_tokens(
            mx.nd.argmax(
                pred,
                axis=-1).squeeze().astype('int').asnumpy().tolist())[-1]
        if gen == EOS:
            break
        if gent.replace('_', ' ') == '<pad>':
            break
        a += gen.replace('▁', ' ')
        a_tok = tok(a)
    return a.strip()

def draw_person():
    face = qd.get_drawing('face').image
    shirt = qd.get_drawing('t-shirt').image
    pants = qd.get_drawing('pants').image
    result = Image.new("L",(255, 255*3))
    result.paste(im=face, box=(0, 0))
    result.paste(im=shirt, box=(0, 255))
    result.paste(im=pants, box=(0, 255*2))
    result = result.resize((int(result.width/3),int(result.height/3)))
    return result

@app.route('/diary/<text>') 
def get_diary(text): 
    try:
        diary = chat(text = text)
        return {'status': 'success','request':diary} 
    except Exception as e: 
        return {'error': str(e)}
    
@app.route('/draw/<text>')
def draw(text):
    try:
        if text == '사람':
            image = draw_person()
        else :
            word = tr.translate(text).text
            word = word.lower()
            word = word.replace("a ", "")
            word = word.replace("an ", "")
            image = qd.get_drawing(word).get_image()
        img = image.convert("RGBA")
        datas = img.getdata()

        newData = []
        for item in datas:
            if item[0] == 255 and item[1] == 255 and item[2] == 255:
                newData.append((255, 255, 255, 0))
            else:
                newData.append(item)

        img.putdata(newData)
        image = img
        
        buffered = BytesIO()
        image.save(buffered, format="PNG")
        img_str = base64.b64encode(buffered.getvalue())
        return {'status': 'success','request':img_str.decode("utf-8")}
    except Exception as e: 
        return {'error': str(e)}

    

if __name__ == '__main__': 
    app.run(host="0.0.0.0",port=5000,debug=True)