package com.music.music_simple;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.music.music_simple.databinding.ActivityMainBinding;
import com.music.music_simple.databinding.ItemMusicBinding;

import java.io.File;
import java.io.Serializable;

//主界面
public class MainActivity extends AppCompatActivity {


    private MusicAdapter adapter = new MusicAdapter();
    ActivityMainBinding binding;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("音乐");
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("注意");
        progressDialog.setMessage("音乐下载中...");
        progressDialog.setCancelable(false);


        adapter.getData().add(new Music("笼（电影《消失的她》片尾曲）", "http://music.163.com/song/media/outer/url?id=2057534370.mp3", "张碧晨", "04:40"));
        adapter.getData().add(new Music("负重一万斤长大", "http://music.163.com/song/media/outer/url?id=1406686876.mp3", "太一", "04:22"));
        adapter.getData().add(new Music("凄美地", "http://music.163.com/song/media/outer/url?id=436346833.mp3", "郭顶", "04:10"));
        adapter.getData().add(new Music("起风了", "http://music.163.com/song/media/outer/url?id=1330348068.mp3", "买辣椒也用券", "05:25"));
        adapter.getData().add(new Music("Try", "http://music.163.com/song/media/outer/url?id=1917022378.mp3", "Paul Eckert", "04:33"));
        adapter.getData().add(new Music("想去海边", "http://music.163.com/song/media/outer/url?id=1413863166.mp3", "夏日入侵企画", "04:27"));
        adapter.getData().add(new Music("这是我一生中最勇敢的瞬间", "http://music.163.com/song/media/outer/url?id=1366216050.mp3", "棱镜乐队", "04:34"));

        binding.rvMusic.setAdapter(adapter);
        binding.rvMusic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Music music = adapter.getItem(position);
                if (new File(getExternalCacheDir(), music.name + ".mp3").exists()) {
                    Intent intent = new Intent(MainActivity.this, PlayActivity.class);
                    intent.putExtra("musicList", (Serializable) adapter.getData());
                    intent.putExtra("current", position);
                    startActivity(intent);
                } else {
                    progressDialog.show();
                    Intent service = new Intent(MainActivity.this, DownloadService.class);
                    service.putExtra("name", music.name);
                    service.putExtra("url", music.path);
                    startService(service);
                }
            }
        });
        // 注册广播接收器
        downloadCompleteReceiver = new DownloadCompleteReceiver();
        IntentFilter intentFilter = new IntentFilter(DownloadService.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadCompleteReceiver, intentFilter);
    }

    DownloadCompleteReceiver downloadCompleteReceiver;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 取消注册广播接收器
        unregisterReceiver(downloadCompleteReceiver);
    }

    public class DownloadCompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadService.ACTION_DOWNLOAD_COMPLETE)) {
                boolean success = intent.getBooleanExtra("success", false);
                progressDialog.dismiss();
                if (success) {
                    Toast.makeText(context, "下载成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


}