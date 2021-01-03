package com.example.icandoit.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Random;

public class Diary implements Serializable {
    private static final long serialVersionUID = 1L;
    private static Diary diary = new Diary();
    private String date;
    private String text;
    private String image;

    private String intent="";
    private String what="";
    private String where="";
    private String when="";
    private String who="";
    private String how="";
    private String why="";

    private Diary() {
        text = "테스트를 해보자.";
        Date d = new Date();
        date = String.format("%04d-%02d-%02d", d.getYear()+1900, d.getMonth()+1, d.getDate()-1);
    }

    public static Diary getinstance() {
        return diary;
    }

    public void clear() {
        date = null;
        text = null;
        image = null;
    }

    public String getDate() {
        return date;
    }

    public void setDate(int year, int month, int date) {
        this.date = String.format("%04d-%02d-%02d", year, month, date-1);
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getText() {
        int length = text.length();
        StringBuilder sb = new StringBuilder();
        sb.append(text);
        while(length++ < 60) {
            sb.append(" ");
        }

        return sb.toString();
    }

    public void setText(String text) {
        this.text = text;
    }

    public Bitmap getBitmap() {
        // convert base64
        byte[] bImage = Base64.decode(image, 0);

        // convert stream
        ByteArrayInputStream bais = new ByteArrayInputStream(bImage);

        // convert image
        return BitmapFactory.decodeStream(bais);
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        Matrix rotate = new Matrix();
//        rotate.postRotate(180);
//        image = Bitmap.createBitmap(image, 0, 0,
//                image.getWidth(), image.getHeight(), rotate, false);
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);

        // convert byte array
        byte[] bImage = baos.toByteArray();

        // convert base64
        this.image = Base64.encodeToString(bImage, 0);
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        if (intent == null || intent.equals("null")) {
            intent = "";
        }
        this.intent = intent;
    }

    public String getWhat() {
        return what;
    }

    public void setWhat(String what) {
        if (what == null || what.equals("null")) {
            what = "";
        }
        this.what = what;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        if (where == null || where.equals("null")) {
            where = "";
        }
        this.where = where;
    }

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        if (when == null || when.equals("null")) {
            when = "";
        }
        this.when = when;
    }

    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        if (who == null || who.equals("null")) {
            who = "";
        }
        this.who = who;
    }

    public String getHow() {
        return how;
    }

    public void setHow(String how) {
        if (how == null || how.equals("null")) {
            how = "";
        }
        this.how = how;
    }

    public String getWhy() {
        return why;
    }

    public void setWhy(String why) {
        if (why == null || why.equals("null")) {
            why = "";
        }
        this.why = why;
    }

    public String[] getQuery() {
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());
        String[] list = null;
        int i;
        String answer1="";
        String answer2="";
        String answer3="";

        if ((i=random.nextInt(2)) == 0) {
            answer1 += "나는 오늘";
        } else if (i==1) {
            answer1 += "나는";
        }

        int num = random.nextInt(3);

        if (num == 0) {
            list = new String[2];
            answer1+=","+when;

            if (random.nextInt(2) > 0) {
                answer1 += ","+who;
                answer1 += ","+where;
            } else {
                answer1 += ","+where;
                answer1 += ","+who;
            }

            if (intent.equals(what)) {
                answer1+=","+what;
            } else {
                answer1 += "," + what;
                answer1 += "," + intent;
            }

            answer1+=','+how;

            if (random.nextInt(2) > 0) {
                answer2+=","+why;
                answer2+=",때문";

            } else {
                answer2+=","+why;
            }
            list[0] = answer1;
            list[1] = answer2;
            return list;
        } else if (num == 1) {
            list = new String[2];

            answer1+=","+when+"에";

            if (random.nextInt(2) > 0) {
                answer1 += ","+who;
                answer1 += ","+where+"에";
            } else {
                answer1 += ","+where+"에";
                answer1 += ","+who;
            }

            if (intent.equals(what)) {
                answer1+=","+what;
            } else {
                answer1 += "," + what;
                answer1 += "," + intent;
            }

            if (random.nextInt(2) > 0) {
                answer1+=",했다";
            } else {
                answer1+=",함";
            }

            if (random.nextInt(2) > 0) {
                answer2+=how;
                answer2+=","+why;
                if (random.nextInt(2) > 0) {
                    answer2+=",때문";
                }
            } else {
                answer2+=why;
                answer2+=","+how;
            }
            list[0] = answer1;
            list[1] = answer2;
            return list;
        } else if (num == 2) {
            list = new String[3];
            answer1+=","+when+"에";
            answer1+=","+where+"에";
            answer1+=",감";
            answer2+="나";
            answer2+=","+who;
            if (random.nextInt(2) > 0) {
                answer2+="와";
            }
            if (intent.equals(what)) {
                answer2+=","+what;
            } else {
                answer2 += "," + what;
                answer2 += "," + intent;
            }

            if (random.nextInt(2) > 0) {
                answer2+=",했다";
            } else {
                answer2+=",함";
            }

            if (random.nextInt(2) > 0) {
                answer3+=how;
                answer3+=","+why;
                if (random.nextInt(2) > 0) {
                    answer3+=",때문";
                }
            } else {
                answer3+=why;
                answer3+=","+how;
            }
            list[0] = answer1;
            list[1] = answer2;
            list[2] = answer3;
            return list;
        }
        return list;
    }
}
