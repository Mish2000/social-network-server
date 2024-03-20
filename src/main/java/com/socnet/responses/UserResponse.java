package com.socnet.responses;

import com.socnet.entities.User;

import java.util.List;

public class UserResponse extends BasicResponse{
    private List<User> allUsers;

    public UserResponse(List<User> allUsers) {
        this.allUsers = allUsers;
    }

    public UserResponse(boolean success, Integer errorCode, List<User> allUsers) {
        super(success, errorCode);
        this.allUsers = allUsers;
    }

    public List<User> getAllUsers() {
        return allUsers;
    }

    public void setAllUsers(List<User> allUsers) {
        this.allUsers = allUsers;
    }
}
