package com.smartmap;
import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMapOptions;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.smartmap.bean.MapProject;
import com.smartmap.bean.PointItem;
import com.smartmap.map.MapListActivity;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;

public class MainActivity extends AppCompatActivity implements LocationSource,
        AMapLocationListener  ,AMap.OnCameraChangeListener ,AMap.OnMarkerClickListener {
    private AMap aMap;
    private MapView mapView;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private boolean mPermissionEnabled = false;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private UiSettings uiSettings;//设置地图自带按钮的位置
    private LatLng latlng;
    private MarkerOptions markerOption;
    private boolean isadd = false;
    private String name = "";
    private MapProject project;
    private PointItem pointItem;
    ActionBar actionBar;
    private List<PointItem> listpoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         actionBar =getSupportActionBar();

        Intent intent = getIntent();
        String id =  intent.getStringExtra("id");
       project = new MapProject();
        BmobQuery<MapProject> mapProjectBmobQuery = new BmobQuery<MapProject>();
        mapProjectBmobQuery.getObject(id, new QueryListener<MapProject>() {
            @Override
            public void done(MapProject mapProject, BmobException e) {
                if(e==null){
                    actionBar.setTitle(mapProject.getProjectname());
                    project = mapProject;
                    getmarks(mapProject.getObjectId());

                }else{
                    toast("查询失败：" + e.getMessage());
                }
            }
        });




        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.mapfab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Toast.makeText(MainActivity.this,"5566",Toast.LENGTH_SHORT).show();
                isadd = true;
                addmarker();
            }
        });
        isPermissionOK();
        initmap();
        mapView.onCreate(savedInstanceState);

    }

    private void initmap(){
        mapView = findViewById(R.id.map);

        if (aMap == null) {
            // 显示地图
            aMap = mapView.getMap();
            uiSettings=aMap.getUiSettings();
          //  aMap.setOnMapClickListener(this);// 对amap添加单击地图事件监听器
            aMap.setOnCameraChangeListener(this);
            aMap.setOnMarkerClickListener(this);
            CameraUpdate mCameraUpdate = CameraUpdateFactory.zoomTo(12);
            aMap.moveCamera(mCameraUpdate);
            markerOption = new MarkerOptions();
        }
        setUpMap();
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        uiSettings.setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);//设置缩放按钮去位置
        // 自定义系统定位小蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromResource(R.drawable.gps_point));// 设置小蓝点的图标
        myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // aMap.setMyLocationType()
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        deactivate();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {

            if (amapLocation != null
                    && amapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode()+ ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr",errText);
            }
        }
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }


    @TargetApi(Build.VERSION_CODES.M)
    private boolean checkPermission() {
        boolean ret = true;

        List<String> permissionsNeeded = new ArrayList<String>();
        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            permissionsNeeded.add("位置权限");
        }
        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            permissionsNeeded.add("文件管理权限");
        }


        if (permissionsNeeded.size() > 0) {
            // Need Rationale
            String message = "本程序需要获取 " + permissionsNeeded.get(0);
            for (int i = 1; i < permissionsNeeded.size(); i++) {
                message = message + ", " + permissionsNeeded.get(i);
            }
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permissionsList.get(0))) {
                showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        });
            }
            else {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }
            ret = false;
        }

        return ret;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {
        boolean ret = true;
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            ret = false;
        }

        return ret;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


    private boolean isPermissionOK() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mPermissionEnabled = true;
            return true;
        }
        else {
            return checkPermission();
        }
    }

   /* @Override
    public void onMapClick(LatLng latLng) {
        Toast.makeText(MainActivity.this,"p"+latLng,Toast.LENGTH_SHORT).show();
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        LatLng target = cameraPosition.target;

       latlng = cameraPosition.target;
    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        latlng = cameraPosition.target;
        if (isadd){
            aMap.clear();
            addmarker();
        }


    }

    private void addmarker(){
        markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                .decodeResource(getResources(),R.mipmap.add_center_marker)));
        markerOption.position(latlng);
       // markerOption.title("添加");
        aMap.addMarker(markerOption);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        //Toast.makeText(MainActivity.this,  "ddd" +marker.getTitle(), Toast.LENGTH_LONG).show();
       // addproject(marker);
        return true;
    }


    private  void addproject(final Marker marker){
        View v = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_view,null);
        final EditText et_projectname = (EditText)v.findViewById(R.id.et_projectname);
        final EditText et_projectmiaoshu = (EditText)v.findViewById(R.id.et_projectmiashu);
        Button btn_ok = (Button)v.findViewById(R.id.btn_OK);
        Button btn_quxiao = (Button)v.findViewById(R.id.btn_quxiao);

        final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this) .create();
        //创建
        dialog.setTitle("添加标记点");
        dialog.setCancelable(false);
        dialog.setView(v);//设置自定义view
        dialog.show();

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pro_name = et_projectname.getText().toString();
                String pro_miaoshu = et_projectmiaoshu.getText().toString();
                if (TextUtils.isEmpty(pro_name)){
                    et_projectname.setError("标记点名称不能为空");

                }else if(TextUtils.isEmpty(pro_miaoshu)){
                    et_projectmiaoshu.setError("标记点描述名称不能为空");

                }else {
                    PointItem pointItem= new PointItem();
                    pointItem.setPointname(pro_name);
                    pointItem.setMiaoshu(pro_miaoshu);
                    pointItem.setLatitude(marker.getPosition().latitude);
                    pointItem.setLongitude(marker.getPosition().longitude);
                   pointItem.setMapProject(project);
                    pointItem.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            if(e==null){
                               aMap.clear();
                               //addAllMarker();
                                 dialog.dismiss();
                                toast("添加数据成功，返回objectId为："+s);
                            }else{
                                toast("创建数据失败：" + e.getMessage());
                            }
                        }
                    });
                }




            }
        });

        btn_quxiao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });




    }

    private void toast(String s){
        Toast.makeText(MainActivity.this,s,Toast.LENGTH_SHORT).show();

    }

    private void addMarkers(List<PointItem> object){
ArrayList<MarkerOptions> markerOptionlst = new ArrayList<MarkerOptions>();
        for (int i = 0; i < object.size(); i++) {
            LatLng latLng =new LatLng(object.get(i).getLatitude(),object.get(i).getLongitude());
            MarkerOptions markerOption = new MarkerOptions();
            markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                    .decodeResource(getResources(),R.mipmap.marker_default_icon)));
            markerOption.position(latLng);
            markerOptionlst.add(markerOption);
            aMap.addMarker(markerOption);
        }



    }

    private void getmarks(String s){
        BmobQuery<PointItem> pointItemBmobQuery = new BmobQuery<PointItem>();
        pointItemBmobQuery.addWhereEqualTo("mapProject",s);
        pointItemBmobQuery.findObjects(new FindListener<PointItem>() {
            @Override
            public void done(List<PointItem> list, BmobException e) {
                if(e==null){
                    Log.i("bmob","数据："+list.size());
                    addMarkers(list);

                }else{
                    Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });

    }
}
