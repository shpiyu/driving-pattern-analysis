package com.gmail.piyushranjan95.car;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RegisterActivity extends AppCompatActivity {

    private String username;
    private EditText nameInput;
    private Button nextButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //check if the app is executed for the first time
        SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        if(pref.getBoolean("activity_executed", false)){
            //changed to devicelist activity from locking activity
            Intent intent = new Intent(this, DeviceListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            SharedPreferences.Editor ed = pref.edit();
            ed.putBoolean("activity_executed", true);
            ed.commit();
        }



        nameInput = (EditText) findViewById(R.id.usernameTextBox);
        nextButton = (Button) findViewById(R.id.nextButton);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = nameInput.getText().toString();
                // TODO: 27/5/17 store name in shared prefs
                SharedPreferences pref = getSharedPreferences("namePref",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("username",username);
                editor.commit();
                Log.d("username",username);
                Log.d("username","saved name "+pref.getString("username","fg"));
                startActivity(new Intent(getApplicationContext(),DeviceListActivity.class));
            }
        });
    }
}
