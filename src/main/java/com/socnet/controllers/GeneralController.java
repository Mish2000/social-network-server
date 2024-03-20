package com.socnet.controllers;

import com.socnet.entities.Note;
import com.socnet.entities.User;
import com.socnet.responses.*;
import com.socnet.utils.DbUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.socnet.utils.Errors.*;

@RestController
public class GeneralController {

    @Autowired
    private DbUtils dbUtils;

    @RequestMapping("/sign-in")
    public LoginResponse checkUser(String username, String password) {
        User user = dbUtils.signIn(username, password);
        if (user != null && user.getToken() != null) {
            return new LoginResponse(true, null, user.getToken(), user.getId());
        } else {
            return new LoginResponse(false, ERROR_SIGN_IN_FAILED, null, null);
        }
    }


    @RequestMapping("/register")
    public RegisterResponse register(String username, String password, String repeat) {
        boolean success = false;
        Integer errorCode = null;
        Integer id = null;
        if (username != null) {
            if (password != null) {
                if (password.equals(repeat)) {
                    if (usernameAvailable(username).isAvailable()) {
                        User user = new User();
                        user.setUsername(username);
                        user.setPassword(password);
                        success = dbUtils.registerUser(user);
                        id = user.getId();
                    } else {
                        errorCode = ERROR_USERNAME_NOT_AVAILABLE;
                    }
                } else {
                    errorCode = ERROR_PASSWORDS_DONT_MATCH;
                }
            } else {
                errorCode = ERROR_MISSING_PASSWORD;
            }
        } else {
            errorCode = ERROR_MISSING_USERNAME;
        }
        return new RegisterResponse(success, errorCode, id);
    }

    @RequestMapping("/username-available")
    public UsernameAvailableResponse usernameAvailable(String username) {
        boolean success = false;
        Integer errorCode = null;
        boolean available = false;
        if (username != null) {
            available = dbUtils.usernameAvailable(username);
            success = true;
        } else {
            errorCode = ERROR_MISSING_USERNAME;
        }
        return new UsernameAvailableResponse(success, errorCode, available);

    }

    @RequestMapping("/get-all-users")
    public UserResponse getAllUsers() {
        List<User> allUsers = dbUtils.getAllUsers();
        return new UserResponse(allUsers);
    }

    @RequestMapping("/get-notes")
    public List<Note> getNotes(String token) {
        List<Note> userNotes = dbUtils.getNotes(token);
        return userNotes;
    }

    @RequestMapping("/save-new-note")
    public BasicResponse saveNewNote(String content, String token) {
        dbUtils.saveNewNote(content, token);
        return new BasicResponse(true, null);

    }

    @RequestMapping("/remove-note")
    public BasicResponse removeNote(String token, int noteId) {
        dbUtils.removeNote(token, noteId);
        return new BasicResponse(true, null);

    }

    @RequestMapping("/search-users")
    public List<User> searchUsers(String query) {
        return dbUtils.searchUsers(query);
    }

    @RequestMapping(value = "/follow")
    public String followUser(int followerId, int followedId) {
        boolean success = dbUtils.followUser(followerId, followedId);
        return success ? "Follow successful" : "Follow failed";
    }

    @RequestMapping("/get-following")
    public List<User> getFollowing(int userId) {
        return dbUtils.getFollowing(userId);
    }

    @RequestMapping("/unfollow")
    public BasicResponse unfollowUser(int followerId, int followedId) {
        boolean success = dbUtils.unfollowUser(followerId, followedId);
        if (success) {
            return new BasicResponse(true, null);
        } else {
            return new BasicResponse(false, ERROR_USER_UNFOLLOWING_FAILED);
        }
    }

    @RequestMapping("/get-feed-notes")
    public List<Note> getFeedNotes(int userId) {
        List<Note> feedNotes = dbUtils.getFeedNotes(userId);
        return feedNotes;
    }

}