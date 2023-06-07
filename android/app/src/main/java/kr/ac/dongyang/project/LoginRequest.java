package kr.ac.dongyang.project;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class LoginRequest extends StringRequest {

    // URL 서버 설정 (PhP연결)
    final static private String URL = "http://122.32.165.55/Login.php";
    private Map<String, String> map;

    public LoginRequest(String id,
                        String password,
                        Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null );

        map = new HashMap<>();
        map.put("id",id);
        map.put("password",password);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}
