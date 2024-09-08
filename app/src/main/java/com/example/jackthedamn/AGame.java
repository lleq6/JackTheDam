package com.example.jackthedamn;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.jackthedamn.manager.DeckManager;
import com.example.jackthedamn.manager.LobbyManager;
import com.example.jackthedamn.manager.UtilsManager;
import com.example.jackthedamn.model.Card;
import com.example.jackthedamn.model.Player;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AGame extends AppCompatActivity {
    //deck
    DeckManager deck;
    int cardIndex, playerCardIndex, playerPoint, hostPoint;
    int[] cardList = {1, 2, 3, 4, 5, 6, 7, 8, 9};

    Boolean joinStatus = false,
            readyStatus = false,
            stayStatus = false;

    //db ref
    DatabaseReference dbRef, roomRef, hostRef, cardRef, playerCardRef, playerRef, statusRef, lobbyRef;
    //host
    Button startButton, drawButton, closeRoomButton;
    //player
    Button readyButton, playerDrawButton, quitButton, stayButton;

    //host card
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //roomcode
        Bundle bundle = getIntent().getExtras();
        String roomCode = bundle.get("roomCode").toString();

        joinStatus = bundle.getBoolean("status");
        //db ref
        dbRef = FirebaseDatabase.getInstance().getReference();

//        DatabaseReference a = dbRef.child(("Room"));
        lobbyRef = dbRef.child("Lobby");
        roomRef = lobbyRef.child(roomCode);


        hostRef = roomRef.child("Host");
        cardRef = hostRef.child("card");
        playerRef = roomRef.child("Player");
        playerCardRef = playerRef.child("card");


        //get widget ref
            //host
        startButton = (Button) findViewById(R.id.startButton);
        drawButton = (Button) findViewById(R.id.drawButton);
        closeRoomButton = (Button) findViewById(R.id.closeButton);
        TextView codeRoomText = (TextView) findViewById(R.id.codeRoom);
        TextView hostScore = (TextView) findViewById(R.id.hostScore);
            //player

        readyButton = (Button) findViewById(R.id.readyButton);
        playerDrawButton = (Button) findViewById(R.id.playerDrawButton);
        quitButton = (Button) findViewById(R.id.quitButton);
        stayButton = (Button) findViewById(R.id.stayButton);
        TextView playerScore = (TextView)findViewById(R.id.playerScore);

        join(joinStatus);
        //define key room
        codeRoomText.setText(roomCode);
//        DatabaseReference roomRef = dbRef.child(roomCode);

        if (joinStatus){
            hostRef.child("name").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    TextView htitle = (TextView) findViewById(R.id.hostTitle);
                    String text = task.getResult().getValue(String.class);
                    htitle.setText(text);
                }
            });
            playerRef.child("name").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    TextView ptitle = (TextView) findViewById(R.id.playerTitle);
                    ptitle.setText(task.getResult().getValue(String.class));
                }
            });
        }else{
            hostRef.child("name").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    TextView htitle = (TextView) findViewById(R.id.hostTitle);
                    String text = task.getResult().getValue(String.class);
                    htitle.setText(text);
                }
            });

        }





        //ready
        statusRef = roomRef.child("Status");
        statusRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                statusRef.child("ready").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        boolean readyStatus = task.getResult().getValue(boolean.class);
//                        Log.d("")
                        if (readyStatus) {
                            startButton.setEnabled(true);
                        } else {
                            startButton.setEnabled(false);
                        }
                    }
                });
                statusRef.child("playerStay").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        boolean stayStatus = task.getResult().getValue(boolean.class);
//                        Log.d("")
                        if (stayStatus) {
                            drawButton.setText("Draw!");
                            drawButton.setEnabled(true);

                        } else {
                            drawButton.setText("Waiting for player stay!");
                            drawButton.setEnabled(false);
                        }
                    }
                });
                statusRef.child("playerDrawing").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!joinStatus){
                            if (task.getResult().getValue(boolean.class)){
                                if (!joinStatus){
                                    playerDraw();
                                }
                                statusRef.child("playerDrawing").setValue(false);
                            };

                        }
                    }
                });
                Log.d("status change", snapshot.getKey().toString());
                if (snapshot.getKey().toString().equals("gameStart")){
                if (joinStatus) {
                    statusRef.child("gameStart").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            boolean gameStart = task.getResult().getValue(boolean.class);
//                          Log.d("")
                            if (gameStart) {
                                readyButton.setVisibility(View.GONE);
                                playerDrawButton.setVisibility(View.VISIBLE);
                                stayButton.setVisibility(View.VISIBLE);
                                playerDrawButton.setEnabled(true);
                                stayButton.setEnabled(true);
                            } else {
                                drawButton.setText("Waiting for player stay!");
                                playerDrawButton.setEnabled(false);
                                stayButton.setEnabled(false);
                                readyButton.setVisibility(View.VISIBLE);
                                playerDrawButton.setVisibility(View.GONE);
                                stayButton.setVisibility(View.GONE);
                                statusRef.child("ready").setValue(false);
                                readyStatus = false;
                                readyButton.setText("Ready?");
                                readyButton.setBackgroundColor(0xFF664FA2);
                            }
                        }
                    });
                }
                }
                if (snapshot.getKey().toString().equals("closeRoom")){
                    if (snapshot.getValue(boolean.class) == true) {
                        OnQuit(roomCode);
                    }
                }
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        //listen host
        hostRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getKey().equals("point")){
                    long score = (long)snapshot.getValue();
                    hostScore.setText(snapshot.getValue().toString());

                    if (!joinStatus){
                        if (score > 21){
                            AlertDialog.Builder builder = new AlertDialog.Builder(AGame.this);
                            builder.setTitle("Alert");
                            builder.setMessage("Host Lose point over 21");
                            // Add the buttons.
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if (!joinStatus) {
                                        gameReset();
                                    }
                                    // User taps OK button.
                                }
                            });
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        }else if (score > playerPoint){
                            AlertDialog.Builder builder = new AlertDialog.Builder(AGame.this);
                            builder.setTitle("Alert");
                            builder.setMessage("Host win point over player");
                            // Add the buttons.
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if (!joinStatus) {
                                        gameReset();
                                    }
                                    // User taps OK button.
                                }
                            });
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        }
                    }else{
                        playerRef.child("point").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                long scoreP = (long)task.getResult().getValue();
                                if (score > 21){
                                    AlertDialog.Builder builder = new AlertDialog.Builder(AGame.this);
                                    builder.setTitle("Alert");
                                    builder.setMessage("Host Lose point over 21");
                                    playerDrawButton.setEnabled(false);
                                    stayButton.setEnabled(false);
                                    // Add the buttons.
                                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            stayStatus = false;
                                            UtilsManager.Alert(getApplicationContext(),"Waiting for host restart game");
                                        }
                                    });
                                    AlertDialog alertDialog = builder.create();
                                    alertDialog.show();
                                }else if (score > scoreP){
                                    AlertDialog.Builder builder = new AlertDialog.Builder(AGame.this);
                                    builder.setTitle("Alert");
                                    builder.setMessage("Host win point over player");
                                    playerDrawButton.setEnabled(false);
                                    stayButton.setEnabled(false);
                                    // Add the buttons.
                                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            stayStatus = false;
                                            UtilsManager.Alert(getApplicationContext(),"Waiting for host restart game");
                                        }
                                    });
                                    AlertDialog alertDialog = builder.create();
                                    alertDialog.show();
                                }
                            }
                        });
                    }

                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        cardRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                Log.d("card", snapshot.child("1").getValue(String.class));
                cardRef.child("0").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.getResult().getValue(boolean.class)){
                            Log.d("card clear","clearing card");
                            if (joinStatus){
                                clearHostCard();
                            }
                            cardRef.child("0").setValue(false);
                        }
                    }
                });
                if (!snapshot.getKey().equals("0") && !snapshot.getValue(String.class).equals("ID")){
                    Log.d("key", snapshot.getKey());
                    Log.d("value", String.valueOf(snapshot.getValue()));
                    String card = snapshot.getValue(String.class);
                    if (snapshot.getValue(String.class) != "ID"){
                        int key = Integer.parseInt(snapshot.getKey());
                        hostShowCard(card, key);

                    }
                }
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//                cardRef.child("1").setValue("id");
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //player
        playerRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (!joinStatus){
                    if (snapshot.getKey().equals("name")){
                        TextView pTitle = (TextView) findViewById(R.id.playerTitle);
                        pTitle.setText(snapshot.getValue(String.class));
//                        snapshot.getValue();
                    }

                }

                if (snapshot.getKey().equals("point")){
                    long score = (long)snapshot.getValue();
                    playerScore.setText(String.valueOf(score));
                    if (score > 21){
                        AlertDialog.Builder builder = new AlertDialog.Builder(AGame.this);
                        builder.setTitle("Alert");
                        builder.setMessage("Player Lose point over 21");
                        playerDrawButton.setEnabled(false);
                        stayButton.setEnabled(false);
                        // Add the buttons.
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (!joinStatus) {
                                    gameReset();
                                }else{
                                    stayStatus = false;
                                    UtilsManager.Alert(getApplicationContext(),"Waiting for host restart game");
                                }
                                // User taps OK button.
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                    }else if (score == 21){
                        AlertDialog.Builder builder = new AlertDialog.Builder(AGame.this);
                        builder.setTitle("Alert");
                        builder.setMessage("Player win got 21");
                        playerDrawButton.setEnabled(false);
                        stayButton.setEnabled(false);
                        // Add the buttons.
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (!joinStatus) {
                                    gameReset();
                                }else{
                                    stayStatus = false;
                                    UtilsManager.Alert(getApplicationContext(),"Waiting for host restart game");
                                }

                                // User taps OK button.
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                    }


                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        playerCardRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                playerCardRef.child("0").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.getResult().getValue(boolean.class)){
                            Log.d("card clear","clearing card");
                            if (joinStatus){
                                clearPlayerCard();
                            }
                            playerCardRef.child("0").setValue(false);
                        }
                    }
                });
                if (!snapshot.getKey().equals("0") && !snapshot.getValue(String.class).equals("ID")){
                    String card = snapshot.getValue(String.class);

                    int key = Integer.parseInt(snapshot.getKey());
                    if (snapshot.getValue(String.class) != "ID"){
                        playerShowCard(card, key);
                    }
                }
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //player draw
        //player Point

        //button Host
        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hostDraw();
            }
        });

        closeRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                statusRef.child("closeRoom").setValue(true);

            }
        });
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameStart();
                startButton.setVisibility(View.GONE);
                drawButton.setVisibility(View.VISIBLE);
            }
        });

        //button player

        readyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (readyStatus){
                    statusRef.child("ready").setValue(false);
                    readyStatus = false;
                    readyButton.setText("Ready?");
                    readyButton.setBackgroundColor(0xFF664FA2);
                }else{
                    statusRef.child("ready").setValue(true);
                    readyStatus = true;
                    readyButton.setText("Unready");
                    readyButton.setBackgroundColor(Color.MAGENTA);
                }

            }
        });

        playerDrawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerDrawOnClick();
            }
        });

        stayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("stay","stay clicked");
                Log.d("stay",stayStatus.toString());
                if(!stayStatus){
                    stayStatus = true;
                    statusRef.child("playerStay").setValue(stayStatus);
                    stayButton.setEnabled(false);
                    playerDrawButton.setEnabled(false);


                }
            }
        });

        quitButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent r = new Intent(AGame.this, ALobby.class);
                playerRef.child("name").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        TextView htitle = (TextView) findViewById(R.id.hostTitle);
                        String text = task.getResult().getValue(String.class);
                        r.putExtra("name",text);
                        playerRef.child("uid").setValue(0);
                        startActivity(r);

                    }
                });
            }
        });

    }
    public void gameStart () {
        gameReset();
        deck = new DeckManager();
        playerPoint = 0;
        hostPoint = 0;
        stayButton.setEnabled(true);
        playerDrawButton.setEnabled(true);
        playerRef.child("point").setValue(playerPoint);
        hostRef.child("point").setValue(hostPoint);
        statusRef.child("gameStart").setValue(true);
    }

    public void gameReset(){
        statusRef.child("gameStart").setValue(false);
        stayStatus = false;
        statusRef.child("playerStay").setValue(stayStatus);
        deck = new DeckManager();
        clearHostCard();
        clearPlayerCard();
        join(joinStatus);

    }
    public void clearHostCard(){
        for (int i : cardList) {
            cardRef.child(String.valueOf(i)).setValue("ID");
            Resources res = getResources();
            int imgID = res.getIdentifier(String.valueOf("hostCard" + i), "id", getPackageName());
            ImageView curImage = (ImageView) findViewById(imgID);
            String mDrawableName = "red_back";
            int resID = res.getIdentifier(mDrawableName, "drawable", getPackageName());
            curImage.setImageResource(resID);
            curImage.setVisibility(View.GONE);
        }
        cardRef.child("0").setValue(true);
        cardIndex = 1;
    }
    public void clearPlayerCard(){
        for (int i : cardList) {
            playerCardRef.child(String.valueOf(i)).setValue("ID");
            Resources res = getResources();
            int imgID = res.getIdentifier(String.valueOf("playerCard" + i), "id", getPackageName());
            ImageView curImage = (ImageView) findViewById(imgID);
            String mDrawableName = "red_back";
            int resID = res.getIdentifier(mDrawableName, "drawable", getPackageName());
            curImage.setImageResource(resID);
            curImage.setVisibility(View.GONE);
        }
        playerCardRef.child("0").setValue(true);
        playerCardIndex = 1;
    }

    public void hostDraw(){
        Resources res = getResources();
        Card curCard = deck.drawCard();
        cardRef.child(String.valueOf(cardIndex)).setValue(curCard.getCard());
//        String imgSID = String.format("hostCard%d", cardIndex);
        cardIndex += 1;
        hostPoint += curCard.getPoint();
        hostRef.child("point").setValue(hostPoint);
    }

    public void playerDrawOnClick(){
        statusRef.child("playerDrawing").setValue(true);

    }
    public void playerDraw(){
        Resources res = getResources();
        Card curCard = deck.drawCard();
        playerCardRef.child(String.valueOf(playerCardIndex)).setValue(curCard.getCard());
        String imgSID = String.format("playerCard%d", playerCardIndex);
        playerCardIndex += 1;
        playerPoint += curCard.getPoint();
        playerRef.child("point").setValue(playerPoint);


    }

    public void hostShowCard(String curCard, int key){
        Resources res = getResources();
        int imgID = res.getIdentifier(String.valueOf("hostCard" + key), "id", getPackageName());
        ImageView curImage = (ImageView) findViewById(imgID);
        String mDrawableName = String.valueOf(curCard);
        int resID = res.getIdentifier(mDrawableName, "drawable", getPackageName());
        curImage.setImageResource(resID);
        curImage.setVisibility(View.VISIBLE);

    }

    public void playerShowCard(String curCard, int key){
        Resources res = getResources();
        int imgID = res.getIdentifier(String.valueOf("playerCard" + key), "id", getPackageName());
        ImageView curImage = (ImageView) findViewById(imgID);
        String mDrawableName = String.valueOf(curCard);
        int resID = res.getIdentifier(mDrawableName, "drawable", getPackageName());
        curImage.setImageResource(resID);
        curImage.setVisibility(View.VISIBLE);

    }

    public void join(boolean status){
        if (!status){
            closeRoomButton.setVisibility(View.VISIBLE);
            startButton.setVisibility(View.VISIBLE);
            drawButton.setVisibility(View.GONE);
        }else{
            quitButton.setVisibility(View.VISIBLE);
            readyButton.setVisibility(View.VISIBLE);
            playerDrawButton.setVisibility(View.GONE);
            stayButton.setVisibility(View.GONE);
        }
    }

    public void OnQuit(String roomCode)
    {
        Intent r = new Intent(AGame.this, ALobby.class);
        if (!joinStatus) {
            hostRef.child("name").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    String text = task.getResult().getValue(String.class);
                    r.putExtra("name", text);
                    lobbyRef.child(roomCode).removeValue();
                    startActivity(r);
                    finish();
                }
            });
        } else {
            playerRef.child("name").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    TextView htitle = (TextView) findViewById(R.id.hostTitle);
                    String text = task.getResult().getValue(String.class);
                    r.putExtra("name",text);
                    playerRef.child("uid").setValue(0);
                    startActivity(r);

                }
            });
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Bundle bundle = getIntent().getExtras();
        OnQuit(bundle.get("roomCode").toString());
    }
}