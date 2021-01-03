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
    

def chat(model_params,text="",sent='0'):
    tok_path = get_tokenizer()
    model, vocab = get_mxnet_kogpt2_model(ctx=ctx)
    tok = SentencepieceTokenizer(tok_path, num_best=0, alpha=0)
    kogptqa = KoGPT2Chat(model)
    kogptqa.load_parameters(model_params, ctx=ctx)
    sent_tokens = tok(sent)
    cnt=0
    while 1:
        cnt+=1
        if cnt>50:
            break
        q = text.strip()
        if q == 'quit':
            break
        q_tok = tok(q)
        a = ''
        a_tok = []
        while 1:
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
            a += gen.replace('▁', ' ')
            a_tok = tok(a)
        return a.strip()
        
@app.route('/diary/<text>') 
def get_diary(text): 
    try:
        diary = chat('kogpt2_chat.params',text = text)
        return {'status': 'success','request':diary} 
    except Exception as e: 
        return {'error': str(e)}
    
@app.route('/draw/<text>')
def draw(text):
    try:
        word = tr.translate(text).text
        image = qd.get_drawing(word).get_image()
        buffered = BytesIO()
        image.save(buffered, format="JPEG")
        img_str = base64.b64encode(buffered.getvalue())
        return {'status': 'success','request':str(img_str)}
    except Exception as e: 
        return {'error': str(e)}
    
if __name__ == '__main__': 
    app.run(debug=True)