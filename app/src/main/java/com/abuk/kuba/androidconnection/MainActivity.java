package com.abuk.kuba.androidconnection;


import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void getBluetoothActivity(View view){
        Intent intent = new Intent(getApplicationContext(), BluetoothActivity.class);
        startActivity(intent);
    }

    public void getWiFiActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), WiFiActivity.class);
        startActivity(intent);
    }
}
