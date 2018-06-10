package com.smartmap.bean;

import java.util.Date;

import cn.bmob.v3.BmobObject;

/**
 * Created by  yingke on 2018-06-10.
 * yingke.github.io
 */
public class PointItem extends BmobObject {
    private String Pointname; //名称
    private String miaoshu; //描述
    private double  latitude; //纬度
    private double  longitude; //经度
    private Date sigindata;
    private MapProject mapProject;

    public MapProject getMapProject() {
        return mapProject;
    }

    public void setMapProject(MapProject mapProject) {
        this.mapProject = mapProject;
    }

    public String getPointname() {
        return Pointname;
    }

    public void setPointname(String pointname) {
        Pointname = pointname;
    }

    public String getMiaoshu() {
        return miaoshu;
    }

    public void setMiaoshu(String miaoshu) {
        this.miaoshu = miaoshu;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Date getSigindata() {
        return sigindata;
    }

    public void setSigindata(Date sigindata) {
        this.sigindata = sigindata;
    }
}
