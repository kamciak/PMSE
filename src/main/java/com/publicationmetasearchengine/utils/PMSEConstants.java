package com.publicationmetasearchengine.utils;

public enum PMSEConstants {
    INSTANCE;

    public static final String ARXIV_SHORT_NAME="Arxiv";
    public static final String BWN_SHORT_NAME="BWN";
    public static final String WOK_SHORT_NAME="WOK";

    public static final int AUTHOR_MAX_NAME_LENGHT = 64; //from installDb script Author.name field

    public static final String EMAIL_VALIDATION_STRING = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@"+
                   "[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
}
