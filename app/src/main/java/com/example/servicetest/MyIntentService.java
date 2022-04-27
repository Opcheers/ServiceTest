package com.example.servicetest;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

public class MyIntentService extends IntentService {



    public MyIntentService() {
        super("MyIntentService");
    }

    /**
     * 这个方法是在子线程中运行的
     * 这里可用于处理一些具体逻辑，不会发生ANR(Application not responding)的问题
     * @param intent
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d("MyIntentService", "onHandleIntent: Thread id is " + Thread.currentThread().getId());
    }

    /**
     * onHandleIntent调用结束即具体逻辑处理完后，会自动调用onDestroy, 停止该服务的
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("MyIntentService", "onDestroy...");
    }
}
