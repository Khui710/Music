package com.music.music_simple;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

//下载组件
public class DownloadService extends IntentService {
    public static final String ACTION_DOWNLOAD_COMPLETE = "com.music.music_simple.DOWNLOAD_COMPLETE";


    public DownloadService() {
        super("DownloadService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 在创建服务时初始化OkHttpClient
        Log.d("TAG", "onCreate: ");

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String url = intent.getStringExtra("url");
        String name = intent.getStringExtra("name");
        Log.d("TAG", "onHandleIntent: " + url + "--" + name);
        downloadFile(url, name);
    }

    private void downloadFile(String url, String name) {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("user-agent","PostmanRuntime/7.29.2")
                .build();


        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("TAG", "onFailure: "+e.getLocalizedMessage());
                sendBroadcast(new Intent(ACTION_DOWNLOAD_COMPLETE).putExtra("success", false));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d("TAG", "onResponse: "+response.code());
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        Sink sink = null;
                        BufferedSink bufferedSink = null;
                        //这是里的mContext是我提前获取了android的context
                        sink = Okio.sink(new File(getExternalCacheDir(), name + ".mp3"));
                        bufferedSink = Okio.buffer(sink);
                        bufferedSink.writeAll(response.body().source());
                        bufferedSink.close();

                        sendBroadcast(new Intent(ACTION_DOWNLOAD_COMPLETE).putExtra("success", true));
                    }
                } else {
                    sendBroadcast(new Intent(ACTION_DOWNLOAD_COMPLETE).putExtra("success", false));
                }
            }
        });

    }
}
