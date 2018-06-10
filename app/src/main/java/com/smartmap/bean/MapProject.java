package com.smartmap.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by  yingke on 2018-06-10.
 * yingke.github.io
 */
public class MapProject extends BmobObject {
    private String Projectname;
    private String Projectmiaoshu;

    public MapProject(String projectname, String projectmiaoshu) {
        Projectname = projectname;
        Projectmiaoshu = projectmiaoshu;
    }

    public MapProject() {
    }

    public String getProjectname() {
        return Projectname;
    }

    public void setProjectname(String projectname) {
        Projectname = projectname;
    }

    public String getProjectmiaoshu() {
        return Projectmiaoshu;
    }

    public void setProjectmiaoshu(String projectmiaoshu) {
        Projectmiaoshu = projectmiaoshu;
    }
}
