package com.avinash.syncopyproject.Model;

public class User {

    private String user_id;
    private String short_id;
    private String username;
    private int profile_photo;
    private Boolean isPc;

    public Boolean getPc() {
        return isPc;
    }

    public void setPc(Boolean pc) {
        isPc = pc;
    }

    public User() {
    }

    public String getShort_id() {
        return short_id;
    }

    public void setShort_id(String short_id) {
        this.short_id = short_id;
    }

    public User(String user_id, String short_id, String username, int profile_photo, Boolean isPc) {
        this.user_id = user_id;
        this.username = username;
        this.profile_photo = profile_photo;
        this.short_id = short_id;
        this.isPc = isPc;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getProfile_photo() {
        return profile_photo;
    }

    public void setProfile_photo(int profile_photo) {
        this.profile_photo = profile_photo;
    }
}
