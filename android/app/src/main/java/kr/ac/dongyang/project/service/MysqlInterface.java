package kr.ac.dongyang.project.service;

import kr.ac.dongyang.project.dto.AccidentDTO;
import kr.ac.dongyang.project.dto.CustomerDTO;
import kr.ac.dongyang.project.dto.TokenDTO;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface MysqlInterface {
    String URL = "http://122.32.165.55";
//            데이터베이스로 넘어짐 감지할때 썼던 코드
//    @FormUrlEncoded
//    @POST("/gyroUpdate.php")
//    Call<GyroDTO> updateGyro(@Field("id") String id, @Field("accident") int accident);

    @GET("/customer.php")
    Call<CustomerDTO> getPhoneNumber(@Query("id") String id);

    @FormUrlEncoded
    @POST("/updateToken.php")
    Call<TokenDTO> updateToken(@Field("id") String id, @Field("token") String Token);

    @FormUrlEncoded
    @POST("/accident.php")
    Call<AccidentDTO> accident(@Field("customerId") String id, @Field("accidentLocation") String accidentLocation);

}
