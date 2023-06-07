package kr.ac.dongyang.project;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

public class timer extends Activity {
    RadioGroup rg;
    RadioButton ten,twenty,thirty,fourty,fifty,sixty;
    ImageButton back;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timer);
        back = findViewById(R.id.backPress);
        rg = findViewById(R.id.rg);
        ten = findViewById(R.id.ten);
        twenty = findViewById(R.id.twenty);
        thirty = findViewById(R.id.thirty);
        fourty = findViewById(R.id.fourty);
        fifty = findViewById(R.id.fifty);
        sixty = findViewById(R.id.sixty);
        SharedPreferences timer;
        SharedPreferences.Editor edit;
        timer = getSharedPreferences("timer",0);
        edit = timer.edit();

        String getTime = timer.getString("time","000020");
        int time = Integer.parseInt(getTime);

        Log.d("test", "switch");
        Log.d("test", String.valueOf(time));
        switch (time) {
            case 10:
                ten.setChecked(true);
                break;
            case 20:
                twenty.setChecked(true);
                break;
            case 30:
                thirty.setChecked(true);
                break;
            case 40:
                fourty.setChecked(true);
                break;
            case 50:
                fifty.setChecked(true);
                break;
            case 60:
                sixty.setChecked(true);
                break;
        }


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent back = new Intent(getApplicationContext(), message.class);
                //startService(back);
                finish();
            }
        });
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if(radioGroup.getId() == R.id.rg){
                    switch(checkedId){
                        case R.id.ten:
                            ten.setChecked(true);
                            edit.putString("time","000010");
                            edit.apply();
                            Toast.makeText(getApplicationContext(), "10초 선택.", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.twenty:
                            twenty.setChecked(true);
                            edit.putString("time","000020");
                            edit.apply();
                            Toast.makeText(getApplicationContext(), "20초 선택.", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.thirty:
                            thirty.setChecked(true);
                            edit.putString("time","000030");
                            edit.apply();
                            Toast.makeText(getApplicationContext(), "30초 선택.", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.fourty:
                            fourty.setChecked(true);
                            edit.putString("time","000040");
                            edit.apply();
                            Toast.makeText(getApplicationContext(), "40초 선택.", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.fifty:
                            fifty.setChecked(true);
                            edit.putString("time","000050");
                            edit.apply();
                            Toast.makeText(getApplicationContext(), "50초 선택.", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.sixty:
                            sixty.setChecked(true);
                            edit.putString("time","000060");
                            edit.apply();
                            Toast.makeText(getApplicationContext(), "60초 선택.", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        });
    }
}
