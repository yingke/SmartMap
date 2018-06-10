package com.smartmap.bean;

import cn.bmob.v3.BmobUser;

/**
 * Created by  yingke on 2018-06-10.
 * yingke.github.io
 */
public class MyUser extends BmobUser {
    private int isAdmin;

    public int getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(int isAdmin) {
        this.isAdmin = isAdmin;
    }
}
