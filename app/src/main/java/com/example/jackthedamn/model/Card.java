package com.example.jackthedamn.model;

public class Card {
    String card;
    int point;
    String pic;

    public Card(String c , int p){
        this.card = c;
        this.point = p;
    }

    public int getPoint() {
        return point;
    }

    public String getCard() {
        return card;
    }


}
