package com.example.ilostifind.Objects;

import com.google.firebase.database.ServerValue;

import java.util.Date;

public class Post {
    //Declarations
    private String postid;  //unique post id
    private String catpost; //category of the post i.e. iLost or iFound
    private String tdesc; //Description
    private String picture; // picture
    private String pStatus; // status of the post
    private String userID;

    private String username;

    private String clickerID;
    private Object timestamp; //time of post


    public Post(String catpost, String tdesc, String picture, String pStatus, String userID, String username) {
        this.catpost = catpost;
        this.tdesc = tdesc;
        this.picture = picture;
        this.pStatus = pStatus;
        this.userID = userID;
        this.username = username;
        this.timestamp = ServerValue.TIMESTAMP;

    }

    public Post() {
    }


    public String getClickerID() {
        return clickerID;
    }

    public void setClickerID(String clickerID) {
        this.clickerID = clickerID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPostid() {
        return postid;
    }

    public String getCatpost() {
        return catpost;
    }

    public String getTdesc() {
        return tdesc;
    }

    public String getPicture() {
        return picture;
    }

    public String getpStatus() {
        return pStatus;
    }

    public String getUserID() {
        return userID;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public void setCatpost(String catpost) {
        this.catpost = catpost;
    }

    public void setTdesc(String tdesc) {
        this.tdesc = tdesc;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public void setpStatus(String pStatus) {
        this.pStatus = pStatus;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
