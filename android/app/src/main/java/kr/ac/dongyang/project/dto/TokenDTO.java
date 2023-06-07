package kr.ac.dongyang.project.dto;

import com.google.gson.annotations.SerializedName;

public class TokenDTO {
    @SerializedName("success")
    private boolean success;

    public TokenDTO(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
