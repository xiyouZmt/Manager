package com.example.manager.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.manager.Activity.HomeActivity;
import com.example.manager.Application.App;
import com.example.manager.CheckBox.SmoothCheckBox;
import com.example.manager.R;
import com.example.manager.ResideMenu.ResideMenu;
import com.example.manager.Thread.Connect;
import com.xys.libzxing.zxing.activity.CaptureActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectFragment extends Fragment {

    private App app;
    private View view;
    private LinearLayout menu;
    private EditText count;
    private ImageView clear;
    private Button connect;
    private Button scan;
    private SmoothCheckBox smoothCheckBox;
    public  static ConnectHandler connectHandler;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_connect,null);
        initViews();
        setListener();
        /**
         * 自动填写IP地址
         */
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("IP", Context.MODE_PRIVATE);
        String IPAddress = sharedPreferences.getString("IPAddress", "");
        count.setText(IPAddress);
        return view;
    }

    public class ConnectListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.menu :
                    HomeActivity.resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
                    break;
                case R.id.connect :
                    if(count.getText().toString().equals("")){
                        Toast.makeText(getActivity(), "IP地址不能为空", Toast.LENGTH_SHORT).show();
                    } else {
                        if(smoothCheckBox.isChecked()){
                            /**
                             * 记住密码
                             */
                            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("IP", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("IPAddress", count.getText().toString());
                            editor.apply();
                        }
                        if(!app.getUser().connected){
                            progressDialog.show();
                            app.getUser().IP = count.getText().toString();
                            Connect st = new Connect(app.getUser().socket, count.getText().toString(), app.getUser().port);
                            Thread t = new Thread(st, "Connect");
                            t.start();
                        } else {
                            Toast.makeText(getActivity(), "已连接", Toast.LENGTH_SHORT).show();
                        }

                    }
                    break;
                case R.id.scan :
                    /**
                     * 打开扫码界面扫描二维码
                     */
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), CaptureActivity.class);
                    startActivityForResult(intent, 0);
                    break;
                case R.id.clear_Count :
                    count.setText("");
                    break;
            }
        }
    }

    public class ConnectHandler extends Handler{
        public void handleMessage(Message msg){
            App app = (App) getActivity().getApplication();
            switch (msg.what){
                case 0x000 :
                    progressDialog.dismiss();
                    app.getUser().connected = true;
                    Toast.makeText(getActivity(), "连接成功!", Toast.LENGTH_SHORT).show();
                    break;
                case 0x111 :
                    progressDialog.dismiss();
                    app.getUser().connected = false;
                    Toast.makeText(getActivity(), "连接失败，请重新连接", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == getActivity().RESULT_OK){
            String result = data.getStringExtra("result");
            count.setText(result);
            progressDialog.show();
            app.getUser().IP = count.getText().toString();
            Connect st = new Connect(app.getUser().socket, count.getText().toString(), app.getUser().port);
            Thread t = new Thread(st, "Connect");
            t.start();
        }
    }

    public boolean checkIP(String IP){

        String str =  "([1-9]|[1-9]//d|1//d{2}|2[0-4]//d|25[0-5])(//.(//d|[1-9]//d|1//d{2}|2[0-4]//d|25[0-5])){3}";
        Pattern pattern = Pattern.compile(str);
        Matcher matcher = pattern.matcher(IP);
        return matcher.matches();
    }

    public void setListener(){
        menu.setOnClickListener(new ConnectListener());
        connect.setOnClickListener(new ConnectListener());
        clear.setOnClickListener(new ConnectListener());
        scan.setOnClickListener(new ConnectListener());
    }

    public void initViews(){
        menu = (LinearLayout) view.findViewById(R.id.menu);
        LinearLayout back = (LinearLayout) view.findViewById(R.id.back);
        TextView title = (TextView) view.findViewById(R.id.fileName);
        LinearLayout search = (LinearLayout) view.findViewById(R.id.search);
        search.setVisibility(View.GONE);
        back.setVisibility(View.GONE);
        menu.setVisibility(View.VISIBLE);
        title.setText("连接设备");
        count = (EditText) view.findViewById(R.id.count);
        smoothCheckBox = (SmoothCheckBox) view.findViewById(R.id.SmoothCheckBox);
        connect = (Button) view.findViewById(R.id.connect);
        scan = (Button) view.findViewById(R.id.scan);
        connectHandler = new ConnectHandler();
        app = (App)getActivity().getApplication();
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("正在连接...");
        progressDialog.setCancelable(false);
        clear = (ImageView) view.findViewById(R.id.clear_Count);
    }

}
