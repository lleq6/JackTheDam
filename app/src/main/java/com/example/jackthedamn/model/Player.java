package com.example.jackthedamn.model;
import java.util.UUID;
public class Player {
    String playerName;
    String UID;

    public Player(String name){
        this.playerName = name;
        this.UID = UUID.randomUUID().toString();
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getUID() {
        return UID;
    }
}
