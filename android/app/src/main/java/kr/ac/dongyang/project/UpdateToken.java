package kr.ac.dongyang.project;

import android.util.Log;

import kr.ac.dongyang.project.dto.TokenDTO;
import kr.ac.dongyang.project.service.MysqlInterface;
import retrofit2.Call;
import retrofit2.Callback;

public class UpdateToken {
    public static void update(String id, String newToken) {
        final String TAG = "updateToken";
        MysqlInterface api = RetrofitInit.getRetrofit().create(MysqlInterface.class);
        Call<TokenDTO> gyro = api.updateToken(id, newToken);
        gyro.enqueue(new Callback<TokenDTO>(){
            @Override
            public void onResponse (Call <TokenDTO> call, retrofit2.Response <TokenDTO> response){
                Log.d(TAG, newToken);
                Log.d(TAG, "update token : " + response.body().isSuccess());
            }

            @Override
            public void onFailure (Call <TokenDTO> call, Throwable t){
            Log.d(TAG, "fail");
            }
        });
    }
}
