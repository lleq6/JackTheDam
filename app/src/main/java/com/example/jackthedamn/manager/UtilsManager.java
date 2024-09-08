package com.example.jackthedamn.manager;

import android.content.Context;
import android.widget.Toast;

import java.util.Random;

public class UtilsManager {
    public static String GetRandomString(int Length) {
        String Salts = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder Builder = new StringBuilder();
        Random m_Random = new Random();
        while (Builder.length() < Length) {
            int Index = (int) (m_Random.nextFloat() * Salts.length());
            Builder.append(Salts.charAt(Index));
        }
        return Builder.toString();
    }

    public static void Alert(Context Context, String Text) {
        Toast m_Toast = Toast.makeText(Context, Text, Toast.LENGTH_SHORT);
        m_Toast.show();
    }
}