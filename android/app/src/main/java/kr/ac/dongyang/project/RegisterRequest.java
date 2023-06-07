package kr.ac.dongyang.project;

import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class RegisterRequest extends StringRequest {

    // 서버 url 설정 (php 파일 연동)
    final static private String URL = "http://122.32.165.55/regist.php"; // 여기 채워야함. "http://퍼블릭 DNS 주소/Register.php"
    private Map<String, String> map;

    public RegisterRequest(String id, String password, String name, String phone, String email, String emCall1 , Response.Listener<String> listener ) {
        super(Method.POST, URL, listener, null);
        map = new HashMap<>();
        map.put("id", id);
        map.put("password", password);
        map.put("name", name);
        map.put("phone", phone);
        map.put("email", email);
        map.put("emCall1", emCall1);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError{
        return map;
    }
}
