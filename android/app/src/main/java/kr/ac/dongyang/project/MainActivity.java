package kr.ac.dongyang.project;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBar;

import android.annotation.SuppressLint;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    ImageButton blbx;
    ImageButton back;
    Boolean autoLogin = false;
    private PermissionSupport permission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //메세지 관련 권한 물어봄
        permissionCheck();


        //subActivity로 이동하는 버튼
        blbx = findViewById(R.id.blbx);
        back = findViewById(R.id.back);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        SharedPreferences setting;
        SharedPreferences.Editor editor;
        setting = getSharedPreferences("setting", 0);
        editor= setting.edit();

        autoLogin = setting.getBoolean("chx1", false);
        Log.i("test", String.valueOf(autoLogin));

        // 자동로그인 구현
        if(autoLogin){
            Log.i("test",  "자동로그인");
            String loginId = setting.getString("id", "");
            String loginPassword = setting.getString("password", "");
            Response.Listener<String> responseListener = new Response.Listener<String>() {
                @SuppressLint("LongLogTag")
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");
                        if (success) { //로그인에 성공한 경우
                            editor.putString("id", loginId);
                            editor.putString("password", loginPassword);
                            editor.putBoolean("chx1",true);
                            editor.apply();
                            Log.d("login - mainactivity 자동로그인", loginId);
                            Toast.makeText(getApplicationContext(), "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), MainActivity2.class);
                            startActivity(intent);
                        } else { //로그인에 실패한 경우
                            Toast.makeText(getApplicationContext(), "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };
            LoginRequest loginRequest = new LoginRequest(loginId, loginPassword, responseListener);
            RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
            queue.add(loginRequest);

        }
    }
    //로그인 액티비티는 안되서 다른 방법으로 진행
    public void onClick(View view) {
        Intent intentLogin = new Intent(this, loginActivity.class);
        startActivity(intentLogin);
    }
    // 마지막으로 뒤로 가기 버튼을 눌렀던 시간 저장
    private long backKeyPressedTime = 0;
    // 첫 번째 뒤로 가기 버튼을 누를 때 표시
    private Toast toast;
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        // 기존 뒤로 가기 버튼의 기능을 막기 위해 주석 처리 또는 삭제

        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간에 1.5초를 더해 현재 시간과 비교 후
        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간이 1.5초가 지났으면 Toast 출력
        // 2000 milliseconds = 2.0 seconds
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "뒤로 가기 버튼을 한 번 더 누르시면 종료됩니다.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }
        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간에 2.0초를 더해 현재 시간과 비교 후
        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간이 2.0초가 지나지 않았으면 종료
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
            finishAffinity();
            toast.cancel();
        }
    }
    // 권한 체크
    private void permissionCheck(){
        // SDK 23버전 이하 버전에서는 Permission이 필요하지 않다.
        if(Build.VERSION.SDK_INT >= 23){
            // 방금 전 만들었던 클래스 객체 생성
            permission = new PermissionSupport(this, this);

            // 권한 체크한 후에 리턴이 false로 들어온다면
            if (!permission.checkPermission()){
                // 권한 요청을 해줍니다.
                permission.requestPermission();
            }
        }
    }
    // Request Permission에 대한 결과 값을 받아올 수 있습니다.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 여기서도 리턴이 false로 들어온다면 (사용자가 권한 허용을 거부하였다면)

        if (!permission.permissionResult(requestCode, permissions, grantResults)) {
            // 여기서 다시 Permission 요청을 걸었습니다.
            permission.requestPermission();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

