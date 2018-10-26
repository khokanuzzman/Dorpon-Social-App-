package com.example.khokan.dorpon;

/**
 * Created by USER on 10/15/2018.
 */

public class FindFriends {
    public String profileImages,userFullName,status;

    public FindFriends()
    {

    }

    public FindFriends(String profileImages, String userFullName, String status) {
        this.profileImages = profileImages;
        this.userFullName = userFullName;
        this.status = status;
    }

    public String getProfileImages() {
        return profileImages;
    }

    public void setProfileImages(String profileImages) {
        this.profileImages = profileImages;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
