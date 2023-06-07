package kr.ac.dongyang.project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;



public class loginActivity extends Activity {
    EditText id, password;
    Button btn_Login, register;
    CheckBox chx1;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mContext = this;
        id = findViewById(R.id.id);
        password = findViewById(R.id.password);
        btn_Login = findViewById(R.id.btnLogin);
        register = findViewById(R.id.register);
        chx1 = (CheckBox) findViewById(R.id.chx1);


        SharedPreferences setting;
        SharedPreferences.Editor editor;
        setting = getSharedPreferences("setting", 0);
        editor= setting.edit();

        if(setting.getBoolean("chx1", false)){
            id.setText(setting.getString("id", ""));
            password.setText(setting.getString("password", ""));
            chx1.setChecked(true);
        }


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),registerActivity.class);
                startActivity(intent);
            }
        });

        btn_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //edit에 현재 입력되어있는 값을 가져온다.
                String userID = id.getText().toString();
                String userPass = password.getText().toString();

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if (success) { //로그인에 성공한 경우
                                String id = jsonObject.getString("id");
                                String password = jsonObject.getString("password");
                                Toast.makeText(getApplicationContext(), "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                                editor.putString("id", id);
                                editor.putString("password", password);
                                editor.commit();
                                if (chx1.isChecked()) { // 로그인 상태 유지가 체크되어 있다면
                                    editor.putBoolean("chx1",true); // 자동 로그인 여부를 true
                                    editor.commit();
                                    Intent intent = new Intent(loginActivity.this, MainActivity2.class);
                                    startActivity(intent);
                                    finish();

                                } else { // 로그인 상태 유지 체크가 안되어 있다면
                                    editor.putBoolean("chx1",false); // 자동 로그인 여부를 false
                                    editor.commit();
                                    Intent intent = new Intent(loginActivity.this, MainActivity2.class);
                                    startActivity(intent);
                                    finish();
                                }
//                                Intent intent = new Intent(getApplicationContext(), MainActivity2.class);
//                                Intent tcp = new Intent(getApplicationContext(), gyroService.class);
//                                tcp.putExtra("id",id);//tcp 클래스에도 id정보 넘기
                                Intent main = new Intent(getApplicationContext(), MainActivity2.class);
                                main.putExtra("id",id);//tcp 클래스에도 id정보 넘기
//                                startService(tcp);//서비스 시작은 startService
                            } else { //로그인에 실패한 경우
                                Toast.makeText(getApplicationContext(), "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                editor.clear();
                                editor.commit();
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                // Volley 라이브러리를 이용해 실제 서버와 통신을 구현하는 부분
                LoginRequest loginRequest = new LoginRequest(userID, userPass, responseListener);
                RequestQueue queue = Volley.newRequestQueue(loginActivity.this);
                queue.add(loginRequest);
            }
        });
    }
}