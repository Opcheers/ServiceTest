# 服务



### 服务简介

服务是Android中实现程序后台运行的解决方案，适合执行那些不需要和用户交互而且还要求长时间运行的任务。

服务并不是在一个独立进程中的，而是依赖于创建服务时所在的应用程序进程。当某个应用程序进程被杀掉，所有依赖于该应用程序的服务也会停止服务。

我们需要在服务内部手动创建子进程，并在这里执行具体的服务，否则有可能出现主进程被阻塞的情况。

### 异步消息处理机制

```Java
package com.example.androidthreadtest;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int UPDATE_TEXT = 1;
    private TextView mText;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            //主线程中运行
            switch (msg.what){
                case UPDATE_TEXT:
                    mText.setText("Nice to meet you!");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mText = this.findViewById(R.id.text);
        Button button = this.findViewById(R.id.change_text);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.change_text:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message message = new Message();
                        message.what = UPDATE_TEXT;
                        mHandler.sendMessage(message);
                    }
                }).start();
                break;
        }
    }
}
```

1.在主线程中创建一个Handler对象，并重写handlerMessage()方法

2.当子线程需要进行UI操作，就创建一个Message对象，并通过Handler将这条消息发出去

3.之后这条消息会被加入MessageQueue中等待被处理

4.而Looper会一直试图从MessageQueue中取出待处理消息，最后会分发回到Handler中的handlerMessage()中。



### 服务的基本用法

#### 定义一个服务

```Java
public class MyService extends Service {
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * 服务创建时调用
     */
    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * 服务启动时调用
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);

    }

    /**
     * 服务销毁时调用
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

    }
}
```

#### 启动和停止服务

**1.活动决定何时开启或停止服务**

```java 
Intent startIntent = new Intent(this, MyService.class);
startService(startIntent);//启动服务——活动决定服务的开启

Intent stopIntent = new Intent(this, MyService.class);
stopService(stopIntent);//停止服务——活动决定服务停止
```

**2.服务自己停止：**在服务中任意位置调用stopSelf()

#### 活动和服务进行通信

```java
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
         * 活动与服务解绑时调用
         * @param componentName
         */
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected...");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ...
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
            default:
                break;
        }
    }
}
```

### 服务的生命周期

**1.一旦在项目的任何地方调用了Context的startService()方法，相应的服务就会被启动起来**

- 如果这个服务没有创建过，就会先调用onCreate()，然后调用onStartCommand()；如果这个服务被创建过，就直接回调onStartCommand()
- 即，每调用一次startService()就会调用一次onStartCommand()

**2.服务启动会就会一直保持运行状态，直到Context的stopService()或者Service的stopSelf()被调用**

- **一个服务只有一个实例**，所以可以startService()很多次，但关闭只需要一次stopService()或者stopSelf()
- 调用stopService()之后，服务中的onDestroy就会执行

**3.调用Context的bindService()方法，就会获得服务的持久连接**

- 如果bindService()第三个参数是BIND_AUTO_CREATE
  - 如果Service没创建，就会自动创建，会调用onCreate()和onBind()，但不会调用onStartCommand()，即onCreate() -> onBind() -> ServiceConnection.onServiceConnected()
  - 如果Service创建了，就直接调用onBind()，即onBind() -> ServiceConnection.onServiceConnected()

**4.调用Context的unbindService()方法，就会获得解绑**

- 调用unbindService()之后，服务中的onDestroy就会执行
- 同时调用StartService和bindService()关闭时需要unbindService()和stopService()

![image-20220427144917607](C:\Users\83771\AppData\Roaming\Typora\typora-user-images\image-20220427144917607.png)



#### 前台服务

**前台服务和普通服务的区别在于：**

- 前台服务会有一个正在运行的图标在系统的状态栏，下拉可查看详细信息，效果类似于通知；‍
- 普通服务在系统内存不足时可能会被回收，而前台服务不会；‍

```Java
public class MyService extends Service {
    ...
    
    /**
     * 服务创建时调用
     */
    @Override
    public void onCreate() {
        super.onCreate();
        
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("my_service", "前台service通知", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(this, "my_service")
                .setContentTitle("This is content titele")
                .setContentText("This is content text")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pi)
                .build();
        startForeground(1, notification);//调用该方法后会让MyService变成一个前台服务，并在系统状态栏显示出来
    }
}
```

#### IntentService：集开启线程和自动停止线程于一身

```Java
//记得要注册一下
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
```

```Java
//IntentService启动，与一般的Service启动无疑
Intent intentService = new Intent(this, MyIntentService.class);
startService(intentService);
```
