package com.example.jackthedamn.manager;

import com.example.jackthedamn.model.Card;

import java.util.ArrayList;
import java.util.Collections;

public class DeckManager {
    String[] suits = {"c", "d", "h", "s"};
    String[] rank = {"a", "2", "3", "4", "5", "6","7", "8", "9", "10", "j", "q", "k"};
    Card test;
    ArrayList<Card> deck = new ArrayList<>();
    public DeckManager(){
        CreateDeck();
        ShuffleDeck();
//        printDeck();
    }

    private void CreateDeck() {
        for (String s : suits){

            for (String r : rank){
                int point;
                if (r.equals("a")){
                    point = 1;
                } else if (r.equals("j") || r.equals("q") || r.equals("k")) {
                    point = 10;
                } else{
                    point = Integer.parseInt(r);
                }

                String card = s+r;

                Card newCard = new Card(card, point);

                deck.add(newCard);
            }
        }
    }

    private void ShuffleDeck(){
        Collections.shuffle(deck);
    }

    public void printDeck(){
        for (Card c : deck){
            System.out.println(c.getCard()+c.getPoint());
        }
    }

    public Card drawCard(){
        Card cur = deck.get(0);
        deck.remove(0);
        return cur;
    }

}
