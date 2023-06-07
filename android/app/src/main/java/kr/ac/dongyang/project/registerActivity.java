package kr.ac.dongyang.project;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


public class registerActivity extends AppCompatActivity {

    private static final String TAG = "registerActivity";

    private Button idR;
    private EditText id1, pass1, pass2, name1, phone1, email1, Econ1;
    private static String id, password, passwordCk, name, phone, email, EmCon;
    private AlertDialog dialog;
    private boolean validate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 값 가져오기
        id1 = findViewById(R.id.id1);
        pass1 = findViewById(R.id.pass1);
        pass2 = findViewById(R.id.pass2);
        name1 = findViewById(R.id.name1);
        phone1 = findViewById(R.id.phone1);
        email1 = findViewById(R.id.email1);
        Econ1 = findViewById(R.id.emCall1);
        Button btnR = findViewById(R.id.btnR);


        // 아이디 중복 체크
        idR = findViewById(R.id.idR);
        idR.setOnClickListener(view -> {
            id = id1.getText().toString();
            if (validate) {
                return; // 검증 완료
            }
            if (id.equals("")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(registerActivity.this);
                dialog = builder.setMessage("아이디를 입력하세요.").setPositiveButton("확인", null).create();
                dialog.show();
                return;
            }
            Response.Listener<String> responseListener = response -> {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");

                    if (success) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(registerActivity.this);
                        dialog = builder.setMessage("사용 가능한 아이디입니다.").setPositiveButton("확인", null).create();
                        dialog.show();
                        id1.setEnabled(false); // 아이디값 고정
                        validate = true; // 검증 완료
                        idR.setBackgroundColor(Color.LTGRAY);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(registerActivity.this);
                        dialog = builder.setMessage("이미 존재하는 아이디입니다.").setNegativeButton("확인", null).create();
                        dialog.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            };
            ValidateRequest validateRequest = new ValidateRequest(id, responseListener);
            RequestQueue queue = Volley.newRequestQueue(registerActivity.this);
            queue.add(validateRequest);
        });


        // 회원가입 버튼이 눌렸을 때
        btnR.setOnClickListener(view -> {
            // 현재 입력된 정보를 string으로 가져오기
            id = id1.getText().toString();
            password = pass1.getText().toString();
            passwordCk = pass2.getText().toString();
            name = name1.getText().toString();
            phone = phone1.getText().toString();
            email = email1.getText().toString();
            EmCon = Econ1.getText().toString();


            // 아이디 중복체크 했는지 확인
            if (!validate) {
                AlertDialog.Builder builder = new AlertDialog.Builder(registerActivity.this);
                dialog = builder.setMessage("중복된 아이디가 있는지 확인하세요.").setNegativeButton("확인", null).create();
                dialog.show();
                return;
            }


            //한 칸이라도 입력 안했을 경우
            if (id.equals("") || password.equals("") || passwordCk.equals("") || name.equals("") || phone.equals("") || email.equals("") || EmCon.equals("")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(registerActivity.this);
                dialog = builder.setMessage("모두 입력해주세요.").setNegativeButton("확인", null).create();
                dialog.show();
                return;
            }

            // 회원가입 절차 시작
            Response.Listener<String> responseListener = response -> {
                try {
                    // String으로 그냥 못 보냄으로 JSON Object 형태로 변형하여 전송
                    // 서버 통신하여 회원가입 성공 여부를 jsonResponse로 받음
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");

                    // 회원가입 성공 시
                    if (password.equals(passwordCk)) {
                        if (success) { //회원가입 성공
                            Toast.makeText(getApplicationContext(), "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(registerActivity.this, loginActivity.class);
                            startActivity(intent);
                            finish(); // 액티비티를 종료시킴(회원등록 창을 닫음)
                        } else { // 회원가입이 안된다면
                            Toast.makeText(getApplicationContext(), "회원가입에 실패했습니다. 다시 한 번 확인해 주세요.", Toast.LENGTH_SHORT).show();
                        }
                    } else { // 비밀번호가 동일하지 않다면
                        AlertDialog.Builder builder = new AlertDialog.Builder(registerActivity.this);
                        dialog = builder.setMessage("비밀번호가 동일하지 않습니다.").setNegativeButton("확인", null).create();
                        dialog.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            };
            // Volley 라이브러리를 이용해 실제 서버와 통신을 구현하는 부분
            RegisterRequest registerRequest = new RegisterRequest(id, password, name, phone, email, EmCon, responseListener);
            RequestQueue queue = Volley.newRequestQueue(registerActivity.this);
            queue.add(registerRequest);

        });
    }
}
