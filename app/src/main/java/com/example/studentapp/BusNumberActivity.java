package com.example.studentapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class BusNumberActivity extends AppCompatActivity {
    private EditText nextBusNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_number);

        nextBusNumber = findViewById(R.id.next_editTx_id);

    }
    public void nextBtn(View view) {
        String busNumber = nextBusNumber.getText().toString().trim();
        Intent intent = new Intent(this,MapsActivityS.class);
        intent.putExtra("busNumber",busNumber);
        startActivity(intent);
    }
}
