package kr.ac.dongyang.project.dto;

import com.google.gson.annotations.SerializedName;

public class CustomerDTO {
    @SerializedName("success")
    private boolean success;

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("phone")
    private String phone;

    @SerializedName("emCall1")
    private String emCall1;

    @SerializedName("emCall2")
    private String emCall2;

    @SerializedName("emCall3")
    private String emCall3;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmCall1() {
        return emCall1;
    }

    public void setEmCall1(String emCall1) {
        this.emCall1 = emCall1;
    }

    public String getEmCall2() {
        return emCall2;
    }

    public void setEmCall2(String emCall2) {
        this.emCall2 = emCall2;
    }

    public String getEmCall3() {
        return emCall3;
    }

    public void setEmCall3(String emCall3) {
        this.emCall3 = emCall3;
    }


    public CustomerDTO(boolean success, String id, String name, String phone, String emCall1) {
        this.success = success;
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.emCall1 = emCall1;
    }

    @Override
    public String toString() {
        return "CustomerDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", emCall1='" + emCall1 + '\'' +
                '}';
    }

    public void clear(){
        success = false;
        id = name = phone = emCall1 = emCall2 = emCall3 = null;
    }

}
