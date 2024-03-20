package com.socnet.entities;

public class Note {
    private final int id;
    private final String content;
    private String username;

    public Note(int id, String content, String username) {
        this.id = id;
        this.content = content;
        this.username = username;
    }

    public int getId() {
        return this.id;
    }

    public String getContent() {
        return this.content;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", username='" + username + '\'' +
                '}';
    }

}


