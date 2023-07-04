package com.music.music_simple;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
//音乐播放服务
public class MusicService extends Service {
    private static MediaPlayer player;
    public static OnProgressChange onProgressChange;

    private static Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
                int currentPosition = 0;
                int duration = 0;
                if (player.isPlaying()) {
                     currentPosition = player.getCurrentPosition();
                     duration = player.getDuration();
                }
                onProgressChange.progress(currentPosition, duration);

            handler.sendEmptyMessageDelayed(1, 100);
        }
    };

    public static void seekTo(int value) {
        player.seekTo(value);
    }

    public static void setOnProgressChange(OnProgressChange onProgressChange) {
        MusicService.onProgressChange = onProgressChange;
        handler.sendEmptyMessage(1);
    }

    public interface OnProgressChange {
        void progress(int current, int max);
    }

    public static MusicBinder binder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        binder = new MusicBinder();
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player = new MediaPlayer();
        player.setLooping(false);

    }

    public interface OnPreparedListener {
        void onPrepared();
    }

    private Executor executor = Executors.newSingleThreadExecutor();

    public class MusicBinder extends Binder {

        private boolean prepared = false;

        private OnPreparedListener preparedListener;

        public void setPreparedListener(OnPreparedListener preparedListener) {
            this.preparedListener = preparedListener;
        }

        public void setDataSource(String path) {
            prepared = false;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        player.reset();
                        player.setDataSource(MusicService.this,Uri.parse(path));
                        player.prepare();
                        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                prepared = true;
                                if (preparedListener != null) {
                                    preparedListener.onPrepared();
                                }
                                mp.start();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener){
            player.setOnCompletionListener(listener);
        }
        public void setDataSource(Uri path, boolean autoPlay) {
            prepared = false;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        player.reset();
                        player.setDataSource(MusicService.this,path);
                        player.prepare();
                        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                prepared = true;
                                if (preparedListener != null) {
                                    preparedListener.onPrepared();
                                }
                                if (autoPlay) {
                                    mp.start();
                                }

                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        public void seekToProgress(int progress) {
            if (!prepared) return;
            if (!player.isPlaying()) {
                player.start();
            }
            player.seekTo(progress);

        }

        public int getDuration() {
            return player.getDuration();
        }

        public int getCurrentProgress() {
            return player.getCurrentPosition();
        }

        public void play() {
            if (!prepared) return;
            if (!player.isPlaying()) {
                player.start();
            }
        }

        public void pause() {
            if (!prepared) return;
            if (player.isPlaying()) {
                player.pause();
            }
        }

        public void loop(boolean loop) {
            player.setLooping(loop);
        }

    }

    @Override
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);
        if (player.isPlaying()) {
            player.pause();
            player.release();
            player = null;
        }
    }


}
