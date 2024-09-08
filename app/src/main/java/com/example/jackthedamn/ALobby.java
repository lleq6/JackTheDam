package com.example.jackthedamn;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.jackthedamn.manager.LobbyManager;
import com.example.jackthedamn.manager.UtilsManager;
import com.example.jackthedamn.model.Player;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class ALobby extends AppCompatActivity {
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lobby);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        OnLobbyEventListener();

        intent = new Intent(ALobby.this, AGame.class);
        Bundle bundle = getIntent().getExtras();
        Player Player = new Player(bundle.get("name").toString());

        TextView name = (TextView) findViewById(R.id.lobbyName);
        name.setText("Welcome : " + Player.getPlayerName() + "'s");

        ListView m_ListView = (ListView) findViewById(R.id.RoomsView);
        m_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String strItem = parent.getItemAtPosition(position).toString();
                String[] Code = strItem.split(" ");
                intent.putExtra("status",true);
                OnJoinRoom(Code[0], Player);
            }
        });

        Button hostBnt = (Button)findViewById(R.id.hostButton);

        hostBnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String roomCode = UtilsManager.GetRandomString(5);
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                //define key room
                DatabaseReference lobbyRef = dbRef.child("Lobby");
                DatabaseReference roomRef = lobbyRef.child(roomCode);
                //create host
                DatabaseReference hostRef = roomRef.child("Host");
                hostRef.child("UID").setValue(Player.getUID());
                hostRef.child("name").setValue(Player.getPlayerName());
                DatabaseReference cardRef = hostRef.child("card");
                hostRef.child("point").setValue(0);
                cardRef.child("0").setValue(false);

                //create player
                DatabaseReference playerRef = roomRef.child("Player");
                playerRef.child("name").setValue(0);
                playerRef.child("uid").setValue(0);
                playerRef.child("point").setValue(0);
                DatabaseReference cardPlayerRef = playerRef.child("card");
                cardPlayerRef.child("0").setValue(false);

                //player status
                DatabaseReference statusRef = roomRef.child("Status");
                statusRef.child("ready").setValue(false);
                statusRef.child("playerDrawing").setValue(false);
                statusRef.child("playerStay").setValue(false);
                statusRef.child("closeRoom").setValue(false);
                statusRef.child("gameStart").setValue(false);
                intent.putExtra("roomCode", roomCode);
                intent.putExtra("status", false);
                startActivity(intent);
                finish();
            }
        });
        Button joinBnt = (Button)findViewById(R.id.joinButton);
        EditText codeRoom = (EditText) findViewById(R.id.codeInput);

        joinBnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                intent.putExtra("status",true);
                OnJoinRoom(codeRoom.getText().toString(), Player);
            }
        });

        Button btRefresh = (Button)findViewById(R.id.btRefresh);
        btRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RefreshRooms();
            }
        });

        Button btBack = (Button)findViewById(R.id.btBack);
        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ALobby.this, AMainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void OnJoinRoom(String Code, Player Player)
    {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference lobbyRef = dbRef.child("Lobby");

        lobbyRef.child(Code).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    if (task.getResult().getValue() == null){
                        AlertDialog.Builder builder = new AlertDialog.Builder(ALobby.this);
                        builder.setTitle("Alert");
                        builder.setMessage("Room not found!");
                        // Add the buttons.
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User taps OK button.
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                    else {
                        DatabaseReference roomRef = lobbyRef.child(Code);
                        DatabaseReference playerRef = roomRef.child("Player");
                        playerRef.child("uid").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                Log.d("player call",task.getResult().getValue().toString());
                                if (task.getResult().getValue().toString().equals("0")){
                                    Log.d("join",String.valueOf(task.getResult().getValue()));
                                    intent.putExtra("roomCode", Code);
                                    playerRef.child("name").setValue(Player.getPlayerName());
                                    playerRef.child("uid").setValue(Player.getUID());
                                    startActivity(intent);
                                    finish();
                                }
                                else{
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ALobby.this);
                                    builder.setTitle("Alert");
                                    builder.setMessage("Room has full player!");
                                    // Add the buttons.
                                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // User taps OK button.
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
        });
    }

    private void OnLobbyEventListener()
    {
        DatabaseReference DbRef = FirebaseDatabase.getInstance().getReference();
        DbRef.child("Lobby").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                RefreshRooms();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                RefreshRooms();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                RefreshRooms();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                RefreshRooms();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                RefreshRooms();
            }
        });
        DbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                RefreshRooms();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                RefreshRooms();
            }
        });
        DbRef.removeEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                RefreshRooms();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                RefreshRooms();
            }
        });
        DbRef.removeEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                RefreshRooms();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                RefreshRooms();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                RefreshRooms();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                RefreshRooms();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                RefreshRooms();
            }
        });
    }

    private void RefreshRooms()
    {
        DatabaseReference DbRef = FirebaseDatabase.getInstance().getReference();
        DbRef.child("Lobby").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    if (LobbyManager.GetRooms().size() != task.getResult().getChildrenCount())
                    {
                        LobbyManager.ClearRooms();
                    }
                }
            }
        });
        LobbyManager.LoadRoomsOnDatabase();
        ListView m_ListView = (ListView) findViewById(R.id.RoomsView);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, LobbyManager.GetRoomsCode());
        m_ListView.setAdapter(arrayAdapter);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent intent = new Intent(this, AMainActivity.class);
        startActivity(intent);
        finish();
    }
}