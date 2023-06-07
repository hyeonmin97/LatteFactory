package kr.ac.dongyang.project.dto;

import com.google.gson.annotations.SerializedName;

public class GyroDTO {
    @SerializedName("success")
    private boolean success;

    public GyroDTO(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
