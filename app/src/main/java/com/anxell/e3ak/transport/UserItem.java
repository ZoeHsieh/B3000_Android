package com.anxell.e3ak.transport;

/**
 * Created by Sean on 7/27/2017.
 */

public class UserItem {

    private String name;
    private String password;


    public UserItem(String name, String password) {
        super();
        this.name = name;
        this.password = password;

    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

}
