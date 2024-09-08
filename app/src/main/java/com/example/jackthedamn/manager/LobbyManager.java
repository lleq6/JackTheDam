package com.example.jackthedamn.manager;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.jackthedamn.model.Player;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.*;

public class LobbyManager
{
    public static List<String> Rooms = new ArrayList<>();

    public static List<String> GetRooms()
    {
        return Rooms;
    }

    public static void AddRoom(String Code)
    {
        if (!Rooms.contains(Code))
        {
            Rooms.add(Code);
        }
    }

    public static void ClearRooms()
    {
        Rooms.clear();
    }

    public static String[] GetRoomsCode()
    {
        ArrayList<String> Codes = new ArrayList<>();
        for (String Code : Rooms)
        {
            Codes.add(Code);
        }
        Object[] objectArray = Codes.toArray();
        return Arrays.copyOf(objectArray, objectArray.length, String[].class);
    }

    public static void LoadRoomsOnDatabase()
    {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child("Lobby").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task)
            {
                if (!task.isSuccessful())
                {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else
                {
                    if (task.getResult().getValue() != null)
                    {
                        try
                        {
                            for (DataSnapshot rSnapshot : task.getResult().getChildren())
                            {
                                AddRoom(rSnapshot.getKey());
                            }
                        }
                        catch (Exception ex)
                        {
                            Log.d("Exception", ex.toString());
                        }
                    }
                }
            }
        });
    }
}
