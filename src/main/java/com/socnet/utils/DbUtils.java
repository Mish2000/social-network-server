package com.socnet.utils;

import com.socnet.entities.Note;
import com.socnet.entities.User;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Component
public class DbUtils {

    private Connection connection = null;

    @PostConstruct
    public void createConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/social_network",
                    Constants.DB_USERNAME, Constants.DB_PASSWORD);
            System.out.println("Connection success");
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean registerUser(User user) {
        String query = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());
            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public User signIn(String username, String password) {
        String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
        User user = null;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                user = new User();
                user.setId(resultSet.getInt("id"));
                user.setToken(String.valueOf(new Random().nextInt()));

                sql = "UPDATE users SET token = ? WHERE id = ?";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, user.getToken());
                preparedStatement.setInt(2, user.getId());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }


    public boolean usernameAvailable(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<User> getAllUsers() {
        List<User> allUsers = new ArrayList<>();
        String sql = "SELECT id, username, token FROM users";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                User user = new User();
                user.setId(resultSet.getInt("id"));
                user.setUsername(resultSet.getString("username"));
                user.setToken(resultSet.getString("token"));
                allUsers.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return allUsers;
    }

    public List<Note> getNotes(String token) {
        List<Note> notes = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT users.id, users.username FROM users WHERE token = ?");
            preparedStatement.setString(1, token);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int userId = resultSet.getInt("id");
                String username = resultSet.getString("username");
                preparedStatement = connection.prepareStatement(
                        "SELECT id, content FROM notes WHERE user_id = ?");
                preparedStatement.setInt(1, userId);
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    int noteId = resultSet.getInt("id");
                    String content = resultSet.getString("content");
                    Note note = new Note(noteId, content, username);
                    notes.add(note);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }


    public void saveNewNote(String content, String token) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT id FROM users WHERE token =? ");
            preparedStatement.setString(1, token);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int id = resultSet.getInt(1);
                preparedStatement = connection.prepareStatement("INSERT INTO notes (content, date, user_id)\n" +
                        "VALUES (?, CURRENT_TIMESTAMP, ?)");
                preparedStatement.setString(1, content);
                preparedStatement.setInt(2, id);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void removeNote(String token, int noteId) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT id FROM users WHERE token =? ");
            preparedStatement.setString(1, token);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int userId = resultSet.getInt(1);
                preparedStatement = connection.prepareStatement("DELETE FROM notes WHERE id = ? AND user_id = ?");
                preparedStatement.setInt(1, noteId);
                preparedStatement.setInt(2, userId);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public List<User> searchUsers(String searchQuery) {
        List<User> foundUsers = new ArrayList<>();
        String sql = "SELECT id, username, token FROM users WHERE username LIKE ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, "%" + searchQuery + "%");
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    User user = new User();
                    user.setId(resultSet.getInt("id"));
                    user.setUsername(resultSet.getString("username"));
                    user.setToken(resultSet.getString("token"));
                    foundUsers.add(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return foundUsers;
    }

    public boolean followUser(int followerId, int followedId) {
        String sql = "INSERT INTO follows (follower_id, followed_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, followerId);
            ps.setInt(2, followedId);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<User> getFollowing(int userId) {
        List<User> followedUsers = new ArrayList<>();
        String sql = "SELECT u.id, u.username FROM follows f JOIN users u ON f.followed_id = u.id WHERE f.follower_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    followedUsers.add(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return followedUsers;
    }

    public boolean unfollowUser(int followerId, int followedId) {
        String sql = "DELETE FROM follows WHERE follower_id = ? AND followed_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, followerId);
            ps.setInt(2, followedId);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Note> getFeedNotes(int userId) {
        List<Note> feedNotes = new ArrayList<>();
        String sql = "SELECT n.*, u.username FROM notes n " +
                "JOIN follows uf ON n.user_id = uf.followed_id " +
                "JOIN users u ON n.user_id = u.id " +
                "WHERE uf.follower_id = ? " +
                "ORDER BY n.date DESC LIMIT 20";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String content = rs.getString("content");
                String username = rs.getString("username");
                Note note = new Note(id, content, username);
                feedNotes.add(note);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return feedNotes;
    }

}
