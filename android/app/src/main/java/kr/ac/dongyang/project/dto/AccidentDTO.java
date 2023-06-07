package kr.ac.dongyang.project.dto;

import com.google.gson.annotations.SerializedName;

public class AccidentDTO {
    @SerializedName("success")
    private boolean success;

    public boolean isSuccess() {
        return success;
    }
}