package com.ponyvillelive.pvlmobile.net;


import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by tinker on 6/02/16.
 */
public class SoundCloudAPI {

    public static final String API_KEY= "7f96863fb9f47c0f89e145bcac582088";
    public static final String CLIENT_ID_REQ = "&client_id=" + API_KEY;
    public static final String CLIENT_ID = "?client_id=" + API_KEY;
    private static OkHttpClient client;

    public static String resolve(String url){
        if (null == client){
            client = new OkHttpClient.Builder()
                    .followRedirects(false)
                    .build();
        }
        Request request = new Request.Builder()
                .url("http://api.soundcloud.com/resolve?url=" + url + CLIENT_ID_REQ)
                .build();


        ResultCallback cb = new ResultCallback();

        synchronized (cb){
            client.newCall(request).enqueue(cb);
            while (!cb.gotResult){
                try{
                    cb.wait();
                }
                catch (Exception e){
                    Log.d("PVL", "SoundcloudAPI call failed: " + e);
                }
            }
        }

        return cb.result;
    }

    static class ResultCallback implements Callback{
        String result = "empty";
        Boolean gotResult = false;

        @Override
        public synchronized void onFailure(Call call, IOException e) {
            result = "failed";
            gotResult = true;
            Log.d("PVL", "SoundcloudAPI resolve failed: " + e);
            notify();
        }

        @Override
        public synchronized void onResponse(Call call, Response response) throws IOException {
            Log.d("PVL SC", "headers: " + response.headers());
            result = response.headers().get("Location");

            if (result != null && result.endsWith(CLIENT_ID))
            {
                result = result.substring(0, result.length() - CLIENT_ID.length());
            }
            gotResult = true;
            notify();

            response.body().close();
        }
    }

}
