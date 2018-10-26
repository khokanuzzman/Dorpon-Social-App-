package com.example.khokan.dorpon.Model;

/**
 * Created by USER on 10/13/2018.
 */

public class Posts {

    public String uid, time,profileImage,postImage,postDescription,fullName,date;

    public Posts()
    {

    }

    public Posts(String uid, String time, String profileImage, String postImage, String postDescription, String fullName, String date) {
        this.uid = uid;
        this.time = time;
        this.profileImage = profileImage;
        this.postImage = postImage;
        this.postDescription = postDescription;
        this.fullName = fullName;
        this.date = date;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getPostDescription() {
        return postDescription;
    }

    public void setPostDescription(String postDescription) {
        this.postDescription = postDescription;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
