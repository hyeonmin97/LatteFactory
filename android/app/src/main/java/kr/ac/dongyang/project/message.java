package kr.ac.dongyang.project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import kr.ac.dongyang.project.service.BluetoothService;

public class message extends AppCompatActivity {
    private static final String TAG = "message";
    int flag=0;//사용자가 버튼을 눌렀는지 확인하는 플래그변수
    TextView count_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message);
        Button btnY = (Button)findViewById(R.id.btnY);
        Button btnN = (Button)findViewById(R.id.btnN);
        SharedPreferences timer = getSharedPreferences("timer",0);

        String conversiontime2 = timer.getString("time","000020");
        int timeToInt = Integer.parseInt(conversiontime2)*1000;
        Log.d(TAG, conversiontime2);
        count_view = findViewById(R.id.timer);
        String conversionTime = "000020"; //타이머 돌릴 시간
        countDown(conversiontime2); //카운트 다운 시작

        Log.d(TAG, "string" + conversiontime2);
        Log.d(TAG, "int"+ Integer.parseInt(conversiontime2));
        btnY.setOnClickListener(new View.OnClickListener(){//넘어졌을 경우
            @Override
            public void onClick(View v) {
                flag=1;//버튼 누름
                Intent intent = new Intent(getApplicationContext(), BluetoothService.class);
                intent.putExtra("button", "btnY");

                startService(intent);//tcp 서비스에 전달
                finish();
            }
        });
        btnN.setOnClickListener(new View.OnClickListener(){//넘어지지 않았을경우
            @Override
            public void onClick(View v) {
                flag=1;//버튼 누름
                //Toast.makeText(getApplicationContext(),"안넘어짐",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), BluetoothService.class);
                intent.putExtra("button", "btnN");
                startService(intent);
                finish();
            }
        });
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (flag == 0) {//눌린 버튼이 없을때(사용자가 버튼을 안눌렀을때)
                    Intent intent = new Intent(getApplicationContext(), BluetoothService.class);
                    intent.putExtra("button", "btnY");
                    startService(intent);
                    finish();
                }
            }
        }, timeToInt);//60초 기다렸다가 꺼짐
        flag=0;
        Log.d("flag", String.valueOf(flag));
    }

    @Override
    public void onBackPressed() {//메시지 화면에서 뒤로가기 눌렀을때
        flag=1;//버튼 누름으로 판단
        Intent intent = new Intent(getApplicationContext(), BluetoothService.class);
        intent.putExtra("button", "btnN");
        startService(intent);
        super.onBackPressed();
    }

    public void countDown(String time){
        long conversionTime = 0;

        // 1000 단위가 1초
        // 60000 단위가 1분
        // 60000 * 3600 = 1시간

        String getHour = time.substring(0, 2);
        String getMin = time.substring(2, 4);
        String getSecond = time.substring(4, 6);

        // "00"이 아니고, 첫번째 자리가 0 이면 제거
        if (getHour.substring(0, 1) == "0") {
            getHour = getHour.substring(1, 2);
        }

        if (getMin.substring(0, 1) == "0") {
            getMin = getMin.substring(1, 2);
        }

        if (getSecond.substring(0, 1) == "0") {
            getSecond = getSecond.substring(1, 2);
        }

        // 변환시간
        conversionTime = Long.valueOf(getHour) * 1000 * 3600 + Long.valueOf(getMin) * 60 * 1000 + Long.valueOf(getSecond) * 1000;

        // 첫번쨰 인자 : 원하는 시간 (예를들어 30초면 30 x 1000(주기))
        // 두번쨰 인자 : 주기( 1000 = 1초)
        new CountDownTimer(conversionTime, 1000) {

            // 특정 시간마다 뷰 변경
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {

                // 시간단위
                String hour = String.valueOf(millisUntilFinished / (60 * 60 * 1000));

                // 분단위
                long getMin = millisUntilFinished - (millisUntilFinished / (60 * 60 * 1000)) ;
                String min = String.valueOf(getMin / (60 * 1000)); // 몫

                // 초단위
                String second = String.valueOf((getMin % (60 * 1000)) / 1000); // 나머지

                // 밀리세컨드 단위
                String millis = String.valueOf((getMin % (60 * 1000)) % 1000); // 몫

                // 시간이 한자리면 0을 붙인다
                if (hour.length() == 1) {
                    hour = "0" + hour;
                }

                // 분이 한자리면 0을 붙인다
                if (min.length() == 1) {
                    min = "0" + min;
                }

                // 초가 한자리면 0을 붙인다
                if (second.length() == 1) {
                    second = "0" + second;
                }

                count_view.setText(hour + ":" + min + ":" + second); //텍스트 보이는 내용
            }

            // 제한시간 종료시 메세지 전달로 넘어간다.
            public void onFinish() {
//                flag=1;
//                Intent intent = new Intent(getApplicationContext(), gyroService.class);
//                intent.putExtra("button", "btnY");
//
//                startService(intent);//tcp 서비스에 전달
//                finish();
            }
        }.start();
    }
}