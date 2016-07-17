package com.example.manager.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.manager.Adapter.FileAdapter;
import com.example.manager.Application.App;
import com.example.manager.Class.MediaFiles;
import com.example.manager.Thread.FileThread;
import com.example.manager.Utils.ActionBarUtil;
import com.example.manager.Utils.LoadFile;
import com.example.manager.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dangelo on 2016/4/4.
 */
public class VideoList extends Activity {

    private App app;
    private ListView listView;
    private LinearLayout back;
    private LinearLayout search;
    private LoadFile loadFile;
    private LinearLayout edit;
    private LinearLayout copy;
    private LinearLayout move;
    private LinearLayout share;
    private LinearLayout delete;
    private LinearLayout chooseAll;
    private RelativeLayout no_files_image;
    private RelativeLayout no_files_text;
    private boolean hasChoseAll = false;
    private VideoHandler videoHandler;
    private ProgressDialog progressdialog;
    public  static List<MediaFiles> choseFiles;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBarUtil.initActionBar(getActionBar(), getResources().getString(R.string.video), 0x222);
        setContentView(R.layout.filelist);
        initView();
        setListener();
        Cursor cursor = loadFile.loadVideo(getContentResolver());
        if(cursor != null && cursor.getCount() != 0) {
            cursor.close();
            FileAdapter videoAdapter = new FileAdapter(getApplicationContext(), loadFile.getVideoList(), R.drawable.video_image);
            listView.setAdapter(videoAdapter);
        } else {
            loadFile.addView(no_files_image,no_files_text,R.drawable.no_video);
        }
    }

    public class VideoListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            switch (v.getId()){
                case R.id.back :
                    finish();
                    break;
                case R.id.search :

                    break;
                case R.id.copy :
                    intent.setClass(VideoList.this, OperateFile.class);
                    intent.putExtra("style", R.string.video);
                    intent.putExtra("operation", "copy");
                    startActivity(intent);
                    break;
                case R.id.move :
                    intent.setClass(VideoList.this, OperateFile.class);
                    intent.putExtra("style", R.string.video);
                    intent.putExtra("operation", "move");
                    startActivity(intent);
                    break;
                case R.id.share :
                    if(app.getUser().connected) {
                        progressdialog.show();
                        File file = new File(choseFiles.get(0).getFilePath());
                        FileThread ft = new FileThread(app.getUser().socket, app.getUser().IP, app.getUser().port, file, videoHandler);
                        Thread t = new Thread(ft, "FileThread");
                        t.start();
                    } else {
                        Toast.makeText(VideoList.this, "设备未连接，请先连接设备", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.delete :
                    AlertDialog.Builder dialog = new AlertDialog.Builder(VideoList.this);
                    dialog.setTitle("提示").setMessage("确认删除").setPositiveButton("确认", new DialogListener())
                            .setNegativeButton("取消", new DialogListener()).create().show();
                    break;
                case R.id.chooseAll :
                    MediaFiles file;
                    if(!hasChoseAll) {
                        for (int i = 0; i < loadFile.getVideoList().size(); i++) {
                            file = loadFile.getVideoList().get(i);
                            if (file.count == 0) {
                                file.count = 1;
                                if (file.check != null) {
                                    file.check.setBackgroundResource(R.drawable.side_checked);
                                }
                            }
                            if (i == loadFile.getVideoList().size() - 1) {
                                hasChoseAll = true;
                            }
                        }
                    } else {
                        for (int i = 0; i < loadFile.getVideoList().size(); i++) {
                            file = loadFile.getVideoList().get(i);
                            file.count = 0;
                            if (file.check != null) {
                                file.check.setBackgroundResource(R.drawable.side);
                            }
                        }
                        edit.setVisibility(View.GONE);
                        hasChoseAll = false;
                    }
                    break;
            }
        }
    }

    public class VideoListListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MediaFiles mediaFiles = loadFile.getVideoList().get(position);
            if(mediaFiles.count == 1){
                mediaFiles.count = 0;
                mediaFiles.check.setBackgroundResource(R.drawable.side);
                choseFiles.remove(mediaFiles);
                int aChoose;
                for (aChoose = 0; aChoose < loadFile.getVideoList().size(); aChoose ++) {
                    if (loadFile.getVideoList().get(aChoose).count == 1) {
                        break;
                    }
                }
                if(aChoose == loadFile.getVideoList().size()){
                    edit.setVisibility(View.GONE);
                }
            } else {
                mediaFiles.count = 1;
                mediaFiles.check.setBackgroundResource(R.drawable.side_checked);
                choseFiles.add(mediaFiles);
                edit.setVisibility(View.VISIBLE);
            }
        }
    }

    public class DialogListener implements DialogInterface.OnClickListener{

        /**
         * setPositiveButton:一个积极的按钮，一般用于“OK”或者“继续”等操作。
         * setNegativeButton:一个负面的按钮，一般用于“取消”操作。
         * setNeutralButton:一个比较中性的按钮，一般用于“忽略”、“以后提醒我”等操作。
         * which为点击按钮的标识符，是一个 整形的数据，对于这三个按钮而言，每个按钮使用不同的int类型数据进行标识：
         * Positive（-1）、Negative(-2)、 Neutral(-3)。
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case -1 :
                    dialog.dismiss();
                    if(hasChoseAll){
                        choseFiles = loadFile.getVideoList();
                    }
                    for (int i = 0; i < choseFiles.size(); i++) {
                        File file = new File(choseFiles.get(i).getFilePath());
                        loadFile.deleteFile(file);
                        loadFile.getVideoList().remove(choseFiles.get(i));
                    }
                    choseFiles.clear();
                    FileAdapter musicAdapter = new FileAdapter(getApplicationContext(), loadFile.getVideoList(), R.drawable.video_image);
                    listView.setAdapter(musicAdapter);
                    Toast.makeText(VideoList.this, "删除完成", Toast.LENGTH_SHORT).show();
                    break;
                case -2 :
                    dialog.dismiss();
                    break;
            }
        }
    }

    public class VideoHandler extends Handler{
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0x001 :
                    count ++;
                    if(count < choseFiles.size()) {
                        File file = new File(choseFiles.get(count).getFilePath());
                        FileThread ft = new FileThread(app.getUser().socket, app.getUser().IP, app.getUser().port, file, videoHandler);
                        Thread t = new Thread(ft, "FileThread");
                        t.start();
                    } else {
                        progressdialog.dismiss();
                        Toast.makeText(VideoList.this, "传输完成!", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 0x333 :
                    progressdialog.dismiss();
                    app.getUser().connected = false;
                    Toast.makeText(VideoList.this, "连接失败，请重新连接", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    public void setListener(){
        back.setOnClickListener(new VideoListener());
        search.setOnClickListener(new VideoListener());
        copy.setOnClickListener(new VideoListener());
        move.setOnClickListener(new VideoListener());
        share.setOnClickListener(new VideoListener());
        delete.setOnClickListener(new VideoListener());
        chooseAll.setOnClickListener(new VideoListener());
        listView.setOnItemClickListener(new VideoListListener());
    }

    public void initView(){
        listView = (ListView) findViewById(R.id.fileList);
        back = (LinearLayout) findViewById(R.id.back);
        search = (LinearLayout) findViewById(R.id.search);
        edit = (LinearLayout) findViewById(R.id.edit);
        copy = (LinearLayout) findViewById(R.id.copy);
        move = (LinearLayout) findViewById(R.id.move);
        share = (LinearLayout) findViewById(R.id.share);
        delete = (LinearLayout) findViewById(R.id.delete);
        chooseAll = (LinearLayout) findViewById(R.id.chooseAll);
        no_files_image = (RelativeLayout) findViewById(R.id.no_files_image);
        no_files_text = (RelativeLayout) findViewById(R.id.no_files_text);
        loadFile = new LoadFile(VideoList.this);
        choseFiles = new ArrayList<>();
        videoHandler = new VideoHandler();
        progressdialog = new ProgressDialog(this);
        progressdialog.setMessage("传输中...");
        progressdialog.setCanceledOnTouchOutside(false);
        app = (App) getApplication();
    }

}
