package com.example.manager.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.manager.Adapter.FileAdapter;
import com.example.manager.Application.App;
import com.example.manager.Model.MediaFiles;
import com.example.manager.R;
import com.example.manager.Thread.SendFile;
import com.example.manager.Utils.ActionBarUtil;
import com.example.manager.Utils.LoadFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dangelo on 2016/4/3.
 */
public class MusicActivity extends Activity {

    private App app;
    private ListView listView;
    private LinearLayout back;
    private LinearLayout search;
    private RelativeLayout relative_search;
    private EditText editText;
    private LinearLayout cancel;
    private LoadFile loadFile;
    private LinearLayout edit;
    private LinearLayout copy;
    private LinearLayout move;
    private LinearLayout share;
    private LinearLayout delete;
    private LinearLayout chooseAll;
    private RelativeLayout no_files_image;
    private RelativeLayout no_files_text;
    private List<MediaFiles> searchList;
    private boolean hasChoseAll = false;
    private boolean isSearching = false;
    private FileAdapter musicAdapter;
    private MusicHandler musicHandler;
    private ProgressDialog progressDialog;
    public  static List<MediaFiles> choseFiles;
    private int count = 0;
    public static String TAG = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBarUtil.initActionBar(getActionBar(), getResources().getString(R.string.music), 0x222);
        setContentView(R.layout.filelist);
        initView();
        setListener();
        Cursor cursor = loadFile.loadMusic(getContentResolver());
        if(cursor != null && cursor.getCount() != 0) {
            cursor.close();
            musicAdapter = new FileAdapter(getApplicationContext(), loadFile, loadFile.getMusicList(), choseFiles, edit);
            listView.setAdapter(musicAdapter);
        } else {
            loadFile.addView(no_files_image, no_files_text, R.drawable.no_music);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(edit.getVisibility() == View.VISIBLE){
            if(hasChoseAll){
                for (int i = 0; i < loadFile.getMusicList().size(); i++) {
                    loadFile.getMusicList().get(i).count = 0;
                    loadFile.getMusicList().get(i).setFilePath(TAG);
                }
            } else {
                for (int i = 0; i < choseFiles.size(); i++) {
                    choseFiles.get(i).count = 0;
                    choseFiles.get(i).setFilePath(TAG);
                }
            }
            musicAdapter.notifyDataSetChanged();
            edit.setVisibility(View.GONE);
        }
    }

    public class MusicListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            switch (v.getId()){
                case R.id.back :
                    finish();
                    break;
                case R.id.search :
                    search.setVisibility(View.GONE);
                    relative_search.setVisibility(View.VISIBLE);
                    isSearching = true;
                    break;
                case R.id.cancel :
                    relative_search.setVisibility(View.GONE);
                    search.setVisibility(View.VISIBLE);
                    isSearching = false;
                    musicAdapter = new FileAdapter(getApplicationContext(), loadFile, loadFile.getMusicList(), choseFiles, edit);
                    listView.setAdapter(musicAdapter);
                    break;
                case R.id.copy :
                    intent.setClass(MusicActivity.this, OperateActivity.class);
                    intent.putExtra("style", R.string.music);
                    intent.putExtra("operation", "copy");
                    startActivity(intent);
                    break;
                case R.id.move :
                    intent.setClass(MusicActivity.this, OperateActivity.class);
                    intent.putExtra("style", R.string.music);
                    intent.putExtra("operation", "move");
                    startActivity(intent);
                    break;
                case R.id.share :
                    if(app.getUser().connected) {
                        progressDialog.show();
                        File file = new File(choseFiles.get(0).getFilePath());
                        SendFile ft = new SendFile(app.getUser().socket, app.getUser().IP, app.getUser().port, file, musicHandler);
                        Thread t = new Thread(ft, "SendFile");
                        t.start();
                    } else {
                        Toast.makeText(MusicActivity.this, "设备未连接，请先连接设备", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.delete :
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MusicActivity.this);
                    dialog.setTitle("提示").setMessage("确认删除").setPositiveButton("确认", new DialogListener())
                            .setNegativeButton("取消", new DialogListener()).create().show();
                    break;
                case R.id.chooseAll :
                    MediaFiles file;
                    if(!hasChoseAll) {
                        for (int i = 0; i < loadFile.getMusicList().size(); i++) {
                            file = loadFile.getMusicList().get(i);
                            if (file.count == 0) {
                                file.count = 1;
                                if (file.checkBox != null) {
                                    file.checkBox.setChecked(true, true);
                                }
                            }
                            if (i == loadFile.getMusicList().size() - 1) {
                                hasChoseAll = true;
                            }
                        }
                        choseFiles = loadFile.getMusicList();
                    } else {
                        for (int i = 0; i < loadFile.getMusicList().size(); i++) {
                            file = loadFile.getMusicList().get(i);
                            file.count = 0;
                            if (file.checkBox != null) {
                                file.checkBox.setChecked(false, true);
                            }
                        }
                        edit.setVisibility(View.GONE);
                        hasChoseAll = false;
                    }
                    break;
            }
        }
    }

    public class MusicListListener implements AdapterView.OnItemClickListener{

        List<MediaFiles> list;

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(isSearching){
                list = searchList;
            } else {
                list = loadFile.getMusicList();
            }
//            MediaFiles mediaFiles = list.get(position);
//            if(mediaFiles.count == 1){
//                mediaFiles.count = 0;
//                mediaFiles.checkBox.setChecked(false);
//                choseFiles.remove(mediaFiles);
//                int aChoose;
//                for (aChoose = 0; aChoose < list.size(); aChoose ++) {
//                    if (list.get(aChoose).count == 1) {
//                        break;
//                    }
//                }
//                if(aChoose == list.size()){
//                    edit.setVisibility(View.GONE);
//                }
//            } else {
//                mediaFiles.count = 1;
//                mediaFiles.checkBox.setChecked(true);
//                choseFiles.add(mediaFiles);
//                edit.setVisibility(View.VISIBLE);
//            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse("file://" + loadFile.getMusicList().get(position).getFilePath());
            intent.setDataAndType(uri, "audio/*");
            startActivity(intent);
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
                    for (int i = 0; i < choseFiles.size(); i++) {
                        File file = new File(choseFiles.get(i).getFilePath());
                        loadFile.deleteFile(file);
                    }
                    if(hasChoseAll){
                        loadFile.getMusicList().clear();
                    } else {
                        loadFile.getMusicList().removeAll(choseFiles);
                        choseFiles.clear();
                    }
                    musicAdapter.notifyDataSetChanged();
                    Toast.makeText(MusicActivity.this, "删除完成", Toast.LENGTH_SHORT).show();
                    edit.setVisibility(View.GONE);
                    break;
                case -2 :
                    dialog.dismiss();
                    break;
            }
        }
    }

    public class EditTextListener implements TextWatcher{

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            searchList.clear();
            for (int i = 0; i < loadFile.getMusicList().size(); i++) {
                MediaFiles file = loadFile.getMusicList().get(i);
                if(file.getFileName().contains(s.toString())){
                    searchList.add(file);
                } else if(file.getArtist().contains(s.toString())){
                    searchList.add(file);
                }
            }
            FileAdapter fileAdapter = new FileAdapter(getApplicationContext(), loadFile, loadFile.getMusicList(), choseFiles, edit);
            listView.setAdapter(fileAdapter);
        }
    }

    public class MusicHandler extends Handler{
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0x001 :
                    count ++;
                    if(count < choseFiles.size()) {
                        File file = new File(choseFiles.get(count).getFilePath());
                        SendFile ft = new SendFile(app.getUser().socket, app.getUser().IP, app.getUser().port, file, musicHandler);
                        Thread t = new Thread(ft, "SendFile");
                        t.start();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(MusicActivity.this, "传输完成!", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 0x333 :
                    progressDialog.dismiss();
                    app.getUser().connected = false;
                    Toast.makeText(MusicActivity.this, "连接失败，请重新连接", Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(edit.getVisibility() == View.VISIBLE){
            if(hasChoseAll){
                for (int i = 0; i < loadFile.getMusicList().size(); i++) {
                    loadFile.getMusicList().get(i).count = 0;
                }
            } else {
                for (int i = 0; i < choseFiles.size(); i++) {
                    choseFiles.get(i).count = 0;
                }
            }
            musicAdapter.notifyDataSetChanged();
            edit.setVisibility(View.GONE);
        } else {
            finish();
        }
        return true;
    }

    public void setListener(){
        back.setOnClickListener(new MusicListener());
        search.setOnClickListener(new MusicListener());
        cancel.setOnClickListener(new MusicListener());
        copy.setOnClickListener(new MusicListener());
        move.setOnClickListener(new MusicListener());
        share.setOnClickListener(new MusicListener());
        delete.setOnClickListener(new MusicListener());
        chooseAll.setOnClickListener(new MusicListener());
        editText.addTextChangedListener(new EditTextListener());
        listView.setOnItemClickListener(new MusicListListener());
    }

    public void initView(){
        listView = (ListView) findViewById(R.id.fileList);
        back = (LinearLayout) findViewById(R.id.back);
        search = (LinearLayout) findViewById(R.id.search);
        relative_search = (RelativeLayout) findViewById(R.id.relative_search);
        editText = (EditText) findViewById(R.id.editText);
        cancel = (LinearLayout) findViewById(R.id.cancel);
        edit = (LinearLayout) findViewById(R.id.edit);
        copy = (LinearLayout) findViewById(R.id.copy);
        move = (LinearLayout) findViewById(R.id.move);
        share = (LinearLayout) findViewById(R.id.share);
        delete = (LinearLayout) findViewById(R.id.delete);
        chooseAll = (LinearLayout) findViewById(R.id.chooseAll);
        no_files_image = (RelativeLayout) findViewById(R.id.no_files);
        no_files_text = (RelativeLayout) findViewById(R.id.no_files_text);
        loadFile = new LoadFile(MusicActivity.this);
        choseFiles = new ArrayList<>();
        searchList = new ArrayList<>();
        musicHandler = new MusicHandler();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("传输中...");
        progressDialog.setCanceledOnTouchOutside(false);
        app = (App)getApplication();
    }

}
