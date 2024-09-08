package com.example.jackthedamn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.jackthedamn.manager.UtilsManager;

public class AMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button bnt = (Button)findViewById(R.id.button);

        bnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText name = (EditText) findViewById(R.id.playerName);
                if (name.length() > 0 && !name.toString().isEmpty())
                {
                    Intent intent = new Intent(AMainActivity.this, ALobby.class);
                    intent.putExtra("name",name.getText().toString());
                    startActivity(intent);
                    finish();
                }
                else
                {
                    UtilsManager.Alert(getApplicationContext(), "Please enter you name!");
                }

            }
        });


    }
}