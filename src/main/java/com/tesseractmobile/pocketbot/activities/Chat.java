package com.tesseractmobile.pocketbot.activities;

public class Chat {
    public String text;
    public String user;
    
    public Chat(){};
    
    public Chat(final String user, final String text){
        this.user = user;
        this.text = text;
    }
}