package com.cabatuan.dismathoid2014;

/**
 * Created by cobalt on 8/27/14.
 */
public class Question {

    private int ID;
    private String QUESTION;
    private String CORRECT_ANSWER;
    private String OPTIONA;
    private String OPTIONB;
    private String OPTIONC;

    public Question() {
        ID = 0;
        QUESTION = "";
        CORRECT_ANSWER = "";
        OPTIONA = "";
        OPTIONB = "";
        OPTIONC = "";
    }

    public Question(String qUESTION, String cORRECT_ANSWER, String oPTIONA, String oPTIONB, String oPTIONC) {
// no ID???
        QUESTION = qUESTION;
        CORRECT_ANSWER = cORRECT_ANSWER;
        OPTIONA = oPTIONA;
        OPTIONB = oPTIONB;
        OPTIONC = oPTIONC;

    }

    public int getID() {
        return ID;
    }

    public String getQUESTION() {
        return QUESTION;
    }

    public String getOPTA() {
        return OPTIONA;
    }

    public String getOPTB() {
        return OPTIONB;
    }

    public String getOPTC() {
        return OPTIONC;
    }

    public String getCorrectAnswer() {
        return CORRECT_ANSWER;
    }

    public void setID(int id) {
        ID = id;
    }

    public void setQUESTION(String qUESTION) {
        QUESTION = qUESTION;
    }

    public void setOPTA(String oPTA) {
        OPTIONA = oPTA;
    }

    public void setOPTB(String oPTB) {
        OPTIONB = oPTB;
    }

    public void setOPTC(String oPTC) {
        OPTIONC = oPTC;
    }

    public void setCorrectAnswer(String aNSWER) {
        CORRECT_ANSWER = aNSWER;
    }


}
