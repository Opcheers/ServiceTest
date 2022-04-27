package com.example.servicetest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private MyService.DownloadBinder mDownloadBinder;

    private ServiceConnection mConnection = new ServiceConnection() {

        /**
         * 活动与服务绑定成功时调用
         * @param componentName
         * @param iBinder 在这里可以向下转型得到mDownloadBinder实例
         *                然后调用DownloadBinder中的方法实现活动指挥服务
         */
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected...");
            mDownloadBinder = (MyService.DownloadBinder) iBinder;
            mDownloadBinder.startDownload();
            mDownloadBinder.getProcess();
        }

        /**
         * 当调用者主动通过unbindService()断开与Service的连接时，该方法不会被调用
         * 在连接正常关闭的情况下是不会被调用的, 该方法只在Service被破坏了或者被杀死的时候调用.
         * 例如, 系统资源不足, 要关闭一些Services, 刚好连接绑定的Service是被关闭者之一,这个时候onServiceDisconnected()就会被调用。
         * @param componentName
         */
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startBtn = this.findViewById(R.id.start_service);
        Button stopBtn = this.findViewById(R.id.stop_service);
        Button bindBtn = this.findViewById(R.id.bind_service);
        Button unbindBtn = this.findViewById(R.id.unbind_service);
        Button intentServiceBtn = this.findViewById(R.id.start_intent_service);

        startBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);
        bindBtn.setOnClickListener(this);
        unbindBtn.setOnClickListener(this);
        intentServiceBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.start_service:
                Intent startIntent = new Intent(this, MyService.class);
                startService(startIntent);//启动服务——活动决定服务的开启
                break;
            case R.id.stop_service:
                Intent stopIntent = new Intent(this, MyService.class);
                stopService(stopIntent);//停止服务——活动决定服务停止
                break;
            case R.id.bind_service:
                Intent bindIntent = new Intent(this, MyService.class);
                bindService(bindIntent, mConnection, BIND_AUTO_CREATE);
                //BIND_AUTO_CREATE表示在活动和服务进行绑定后自动创建服务(如果没床架)
                //但是绑定成功时候会先调用 ServiceConnection.onServiceConnected()
                break;
            case R.id.unbind_service:
                unbindService(mConnection);
                break;
            case R.id.start_intent_service:
                Log.d(TAG, "Thread id is" + Thread.currentThread().getId());
                Intent intentService = new Intent(this, MyIntentService.class);
                startService(intentService);
                break;
            default:
                break;
        }
    }


}