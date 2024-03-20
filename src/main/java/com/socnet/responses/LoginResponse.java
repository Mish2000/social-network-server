package com.socnet.responses;

public class LoginResponse extends BasicResponse {
    private String token;
    private Integer userId;

    public LoginResponse() {
        super();
    }

    public LoginResponse(boolean success, Integer errorCode, String token, Integer userId) {
        super(success, errorCode);
        this.token = token;
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}

