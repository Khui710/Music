package com.music.music_simple;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.music.music_simple.databinding.ActivityPlayBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
//音乐播放界面
public class PlayActivity extends AppCompatActivity {
    public MusicService.MusicBinder musicBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicBinder = (MusicService.MusicBinder) service;
            Uri url = Uri.parse(musicList.get(current).path);
            musicBinder.setDataSource(url, false);
            binding.tvName.setText(musicList.get(current).name);
            binding.tvNameX.setText(musicList.get(current).name);
            binding.tvSinger.setText(musicList.get(current).singer);
            musicBinder.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (current == musicList.size() - 1) {
                        current = 0;
                    }
                    current++;
                    loadSong();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private int current = 0;
    private List<Music> musicList = new ArrayList<>();
    ActivityPlayBinding binding;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        musicList = (List<Music>) getIntent().getSerializableExtra("musicList");
        current = getIntent().getIntExtra("current", 0);
        setTitle("正在播放...");
        binding = ActivityPlayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("注意");
        progressDialog.setMessage("音乐下载中...");
        progressDialog.setCancelable(false);

        binding.tvName.setText(musicList.get(0).name);
        binding.tvNameX.setText(musicList.get(0).name);
        binding.tvSinger.setText(musicList.get(0).singer);
        binding.ivPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicBinder == null) return;
                playing = !playing;
                if (playing) {
                    musicBinder.play();
                } else {
                    musicBinder.pause();
                }
                binding.ivPlay.setImageResource(playing ? R.drawable.ic_music_play : R.drawable.ic_music_pause);
            }
        });
        binding.ivLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (current == 0) {
                    Toast.makeText(PlayActivity.this, "没有上一首了", Toast.LENGTH_SHORT).show();
                    return;
                }

                current--;
                loadSong();
            }
        });
        binding.ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (current == musicList.size() - 1) {
                    Toast.makeText(PlayActivity.this, "没有下一首了", Toast.LENGTH_SHORT).show();
                    return;
                }
                current++;
                loadSong();
            }
        });
        bindService(new Intent(this, MusicService.class), connection, BIND_AUTO_CREATE);
        MusicService.setOnProgressChange(new MusicService.OnProgressChange() {
            @Override
            public void progress(int current, int max) {
                if (destroyed) return;
                binding.seekBar.setMax(max);
                binding.seekBar.setProgress(current);
            }
        });
        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MusicService.seekTo(seekBar.getProgress());
            }
        });

        // 注册广播接收器
        downloadCompleteReceiver = new PlayActivity.DownloadCompleteReceiver();
        IntentFilter intentFilter = new IntentFilter(DownloadService.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadCompleteReceiver, intentFilter);
    }

    PlayActivity.DownloadCompleteReceiver downloadCompleteReceiver;

    private boolean checkFileExists(String path) {
        return new File(getExternalCacheDir(), path + ".mp3").exists();
    }

    boolean playing = false;

    private void loadSong() {
        Music music = musicList.get(current);
        if (!checkFileExists(music.name)) {
            Intent intent = new Intent(PlayActivity.this, DownloadService.class);
            intent.putExtra("name", music.name);
            intent.putExtra("url", music.path);
            startService(intent);
            progressDialog.show();
            return;
        }

        Uri url = Uri.parse(new File(getExternalCacheDir(), music.name + ".mp3").getAbsolutePath());
        musicBinder.setDataSource(url, true);
        playing = true;
        binding.ivPlay.setImageResource(playing ? R.drawable.ic_music_play : R.drawable.ic_music_pause);
        binding.tvNameX.setText(music.name);
        binding.tvName.setText(music.name);
        binding.tvSinger.setText(music.singer);
    }

    public class DownloadCompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadService.ACTION_DOWNLOAD_COMPLETE)) {
                boolean success = intent.getBooleanExtra("success", false);
                progressDialog.dismiss();
                if (success) {
                    loadSong();
                    Toast.makeText(context, "下载成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private boolean destroyed = false;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyed = true;
        musicBinder.pause();
        unregisterReceiver(downloadCompleteReceiver);
    }
}