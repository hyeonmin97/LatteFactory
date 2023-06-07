package kr.ac.dongyang.project;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.content.Intent;

import androidx.annotation.Nullable;

public class header extends Activity {

    private TextView home_user_name;
    String name;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navi_header);

        home_user_name = findViewById(R.id.home_user_name);

        Intent intent=getIntent();
        if(intent!=null) {
            String name = intent.getStringExtra("name");
            home_user_name.setText(name);
        }

    }
}
