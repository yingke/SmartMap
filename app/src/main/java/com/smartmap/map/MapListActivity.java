package com.smartmap.map;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.smartmap.MainActivity;
import com.smartmap.R;
import com.smartmap.bean.MapProject;
import com.smartmap.bean.MyUser;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;


public class MapListActivity extends AppCompatActivity implements  MyItemClickListener{

    RecyclerView map_rc;
    List<MapProject> prolist;
    MapRVAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_list);
        ActionBar actionBar =getSupportActionBar();
        actionBar.setTitle("项目列表");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addproject();
            }
        });

        map_rc = (RecyclerView)findViewById(R.id.map_rc);

        GridLayoutManager layoutManager = new GridLayoutManager(this,2);
       layoutManager.setOrientation(GridLayoutManager.VERTICAL);
        map_rc.setLayoutManager(layoutManager);
        prolist =new ArrayList<MapProject>();
        adapter = new MapRVAdapter(MapListActivity.this,prolist);
        adapter.setOnItemClickListener(MapListActivity.this);
        map_rc.setAdapter(adapter);
        getdate();


    }

    private  void addproject(){
        View v = LayoutInflater.from(MapListActivity.this).inflate(R.layout.dialog_view,null);
        final EditText et_projectname = (EditText)v.findViewById(R.id.et_projectname);
        final EditText et_projectmiaoshu = (EditText)v.findViewById(R.id.et_projectmiashu);
        Button btn_ok = (Button)v.findViewById(R.id.btn_OK);
        Button btn_quxiao = (Button)v.findViewById(R.id.btn_quxiao);

        final AlertDialog dialog = new AlertDialog.Builder(MapListActivity.this) .create();
        //创建
        dialog.setTitle("添加项目");
        dialog.setCancelable(false);
        dialog.setView(v);//设置自定义view
        dialog.show();

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pro_name = et_projectname.getText().toString();
                String pro_miaoshu = et_projectmiaoshu.getText().toString();
                if (TextUtils.isEmpty(pro_name)){
                    et_projectname.setError("项目名称不能为空");

                }else if(TextUtils.isEmpty(pro_miaoshu)){
                    et_projectmiaoshu.setError("项目描述不能为空");

                }else {
                    MapProject mapProject = new MapProject(pro_name,pro_miaoshu);
                    mapProject.add("user", BmobUser.getCurrentUser(MyUser.class));
                    mapProject.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            if(e==null){
                                Intent intent = new Intent();
                                intent.putExtra("id",s);
                                intent.setClass(MapListActivity.this, MainActivity.class);

                                startActivity(intent);

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
        Toast.makeText(MapListActivity.this,s,Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onItemClick(View view, int postion) {
        MapProject mapProject = prolist.get(postion);
        Intent intent = new Intent();
        intent.putExtra("id",mapProject.getObjectId());
        intent.setClass(MapListActivity.this, MainActivity.class);

        startActivity(intent);


    }

    private void getdate(){

        BmobQuery<MapProject> query = new BmobQuery<MapProject>();
        query.findObjects(new FindListener<MapProject>() {
            @Override
            public void done(List<MapProject> list, BmobException e) {
                Log.i("list","LLLL"+list.size());
                prolist.addAll(list);
                adapter.notifyDataSetChanged();


            }
        });

    }
}
