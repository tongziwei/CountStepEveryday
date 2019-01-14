package com.goertek.countstepeveryday.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.goertek.countstepeveryday.R;
import com.goertek.countstepeveryday.UpdateUiCallback;
import com.goertek.countstepeveryday.activity.MainActivity;
import com.goertek.countstepeveryday.bean.StepData;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

public class StepService extends Service implements SensorEventListener{

    private int currentStep;//当前所走步数
    /*** 计步传感器类型  Sensor.TYPE_STEP_COUNTER或者Sensor.TYPE_STEP_DETECTOR*/
    private static int stepSensorType = -1;
    /**
     * 每次第一次启动记步服务时是否从系统中获取了已有的步数记录
     */
    private boolean isHasRecorder = false;
    /**
     * 系统中获取到的已有的步数
     */
    private int hasStepCount = 0;
    /**
     * 上一次的步数
     */
    private int previousStepCount = 0;

    private  StepBinder mBinder = new StepBinder();
    private static final int NOTIFY_STEP_ID = 100; //步数通知ID
    private static final int NOTIFY_REMIND_ID = 200; //提醒锻炼notificationID
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder builder;
    /*** 默认为30秒进行一次存储*/
    private static int duration = 30 * 1000;

    private static String currentDate = ""; //当日日期：格式yyyy-mm-dd

    private InfoReceiver mInfoReceiver;

    private SensorManager mSensorManager;

    private TimeCount timer;




/**************************************************************************************************/
    public StepService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initNotification();
        initTodayData();
        initBroadcastReceiver();
        new Thread(new Runnable() {
            public void run() {
                startStepDetector();
            }
        }).start();
        startTimeCount();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        unregisterReceiver(mInfoReceiver);
    }

    public class StepBinder extends Binder{
        public StepService getService(){
            return StepService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public int getCurrentStep(){
        return currentStep;
    }

    /***********************************************************************************************/

    private void initNotification(){
         mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new Notification();
        builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("每日计步")
               .setContentText("今日步数"+currentStep+"步")
               .setWhen(System.currentTimeMillis())
               .setSmallIcon(R.mipmap.logo)
               .setContentIntent(getDefalutIntent(Notification.FLAG_ONGOING_EVENT))
               .setPriority(Notification.PRIORITY_DEFAULT)
                .setAutoCancel(false)//设置这个标志当用户单击面板就可以让通知将自动取消
                .setOngoing(true);//设置他为一个正在进行的通知
        notification = builder.build();
        startForeground(NOTIFY_STEP_ID,notification);
    }

    private void initTodayData(){
        currentDate = getTodayDate(); //获得当天日期
        Connector.getDatabase();//创建数据库，这里的数据库采用litepal开源库
        List<StepData> stepDatas = DataSupport.where("date=?",currentDate).find(StepData.class); //查询当日步数数据
        if (stepDatas.size()== 0 || stepDatas.isEmpty()){
            currentStep = 0;
        }else if(stepDatas.size()== 1){
            currentStep = stepDatas.get(0).getStep();
        }else {
            Log.e(TAG, "somethingWrong" );
        }


    }

    private void initBroadcastReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        //屏幕亮屏、灭屏广播
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        //屏幕解锁广播
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        //关机广播
        intentFilter.addAction(Intent.ACTION_SHUTDOWN);
        //时间变化广播
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Intent.ACTION_DATE_CHANGED);
        // 当长按电源键弹出“关机”对话或者锁屏时系统会发出这个广播
        // example：有时候会用到系统对话框，权限可能很高，会覆盖在锁屏界面或者“关机”对话框之上，
        // 所以监听这个广播，当收到时就隐藏自己的对话，如点击pad右下角部分弹出的对话框
        intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        mInfoReceiver = new InfoReceiver();
        registerReceiver(mInfoReceiver,intentFilter);

    }

    class InfoReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
           String action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                Log.d(TAG, "screen on");
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                Log.d(TAG, "screen off");
                //改为60秒一存储
                duration = 60000;
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                Log.d(TAG, "screen unlock");
//                    save();
                //改为30秒一存储
                duration = 30000;
            } else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
                Log.i(TAG, " receive Intent.ACTION_CLOSE_SYSTEM_DIALOGS");
                //保存一次
                save();
            } else if (Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
                Log.i(TAG, " receive ACTION_SHUTDOWN");
                save();
            } else if (Intent.ACTION_DATE_CHANGED.equals(action)) {//日期变化步数重置为0
                save();
                isNewDay();
            } else if (Intent.ACTION_TIME_CHANGED.equals(action)) {
                //时间变化步数重置为0
                isCall(); //监听时间变化，提醒用户锻炼
                save();
                isNewDay();
            } else if (Intent.ACTION_TIME_TICK.equals(action)) {//日期变化步数重置为0
                isCall();
                save();
                isNewDay();
            }
            updateNotification();

        }
    }

    /*获取计步传感器实例*/
    private void startStepDetector(){
        if(mSensorManager != null){
            mSensorManager = null;
        }
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        //Android4.4以后可以用系统带的计步传感器计步，4.4以前用加速度传感器计步
        int VERSION_CODE = Build.VERSION.SDK_INT;
        if (VERSION_CODE > 19){
            addCountStepListener();
        }else{
            addBasePedometerListener();
        }

    }

    private void startTimeCount(){
        if(timer == null){
            timer = new TimeCount(duration,1000);
        }
        timer.start();
    }

    class TimeCount extends CountDownTimer{
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }
        //计时器正常结束则开始计步
        @Override
        public void onFinish() {
            timer.cancel();
            save();
            startTimeCount();
        }
    }
    /***********************************************************************************************/
    /** 添加传感器监听
     * 1. TYPE_STEP_COUNTER API的解释说返回从开机被激活后统计的步数，当重启手机后该数据归零，
     * 该传感器是一个硬件传感器所以它是低功耗的。
     * 为了能持续的计步，请不要反注册事件，就算手机处于休眠状态它依然会计步。
     * 当激活的时候依然会上报步数。该sensor适合在长时间的计步需求。
     * <p>
     * 2.TYPE_STEP_DETECTOR翻译过来就是走路检测，
     * API文档也确实是这样说的，该sensor只用来监监测走步，每次返回数字1.0。
     * 如果需要长事件的计步请使用TYPE_STEP_COUNTER。*/
    private void addCountStepListener(){
        Sensor countSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        Sensor detectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (countSensor != null){
            stepSensorType = Sensor.TYPE_STEP_COUNTER;
            mSensorManager.registerListener(StepService.this,countSensor,SensorManager.SENSOR_DELAY_NORMAL);
        }else if(detectorSensor != null){
            stepSensorType = Sensor.TYPE_STEP_DETECTOR;
            mSensorManager.registerListener(StepService.this,detectorSensor,SensorManager.SENSOR_DELAY_NORMAL);
        }else{
            addBasePedometerListener();
        }
    }

    private void addBasePedometerListener(){

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(stepSensorType == Sensor.TYPE_STEP_COUNTER){
            int tempStep = (int) event.values[0];//获得传感器返回的临时步数
            //首次如果没有获取手机系统中已有的步数则获取一次系统中APP还未开始记步的步数
            if(!isHasRecorder){
                isHasRecorder = true;
                hasStepCount = tempStep;
            }else{
                //获取APP打开到现在的总步数= 本次系统回调的总步数- APP打开之前记录的步数
                int thisStepCount = tempStep - hasStepCount;
               //本次有效步数= 本次打开APP的步数- 上一次打开APP的步数
                int thisStep = thisStepCount - previousStepCount;
                //总步数=现有步数+当前有效步数
                currentStep += thisStep;
                //最后一次APP打开到现在的总步数
                previousStepCount = thisStepCount;
            }
        }else if (stepSensorType == Sensor.TYPE_STEP_DETECTOR){
            if(event.values[0]==1.0){
                currentStep++;
            }
        }
        updateNotification();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**************************************************************************************************/
    public PendingIntent getDefalutIntent(int flags) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, new Intent(), flags);
        return pendingIntent;
    }

    private String getTodayDate(){
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }


    /**
     * 监听时间变化，提醒用户锻炼
     */
    private void isCall(){
        String time = this.getSharedPreferences("share_date", Context.MODE_PRIVATE).getString("remindTime","20:00");
        String plan = this.getSharedPreferences("share_date", Context.MODE_PRIVATE).getString("planWalk_QTY","7000");
        boolean isRemind = this.getSharedPreferences("share_date", Context.MODE_PRIVATE).getBoolean("isRemind",true);
     /*   Logger.d("time=" + time + "\n" +
                "new SimpleDateFormat(\"HH: mm\").format(new Date()))=" + new SimpleDateFormat("HH:mm").format(new Date()));*/
        if (isRemind && (currentStep < Integer.parseInt(plan)) &&
                (time.equals(new SimpleDateFormat("HH:mm").format(new Date())))
                ) {
            remindNotify();
        }
    }

    /**
     * 提醒锻炼通知栏
     */
    private void remindNotify(){
         Intent hangIntent = new Intent(this,MainActivity.class); //点击通知栏取消当前通知，进入主页
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,hangIntent,PendingIntent.FLAG_CANCEL_CURRENT);

        String plan = this.getSharedPreferences("share_date", Context.MODE_PRIVATE).getString("planWalk_QTY","7000");
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentIntent(pendingIntent)
                .setContentTitle("今日步数" + currentStep + " 步")
                .setContentText("距离目标还差" + (Integer.valueOf(plan) - currentStep) + "步，加油！")
                .setWhen(System.currentTimeMillis())
                .setOngoing(false)
                .setAutoCancel(true)
                .setTicker(getResources().getString(R.string.app_name) + "提醒您开始锻炼了")//通知首次出现在通知栏，带上升动画效果的
                .setPriority(Notification.PRIORITY_DEFAULT)//设置该通知优先级
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合：
                //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
                .setSmallIcon(R.mipmap.logo);
        notificationManager.notify(NOTIFY_REMIND_ID,mBuilder.build());
    }

    /*保存数据*/
    private void save(){
        int tempStep = currentStep;

        List<StepData> stepDataList = DataSupport.where("date=?",currentDate).find(StepData.class); //查询当日步数数据
        if (stepDataList.size()== 0 || stepDataList.isEmpty()){
            StepData stepData = new StepData();
            stepData.setDate(currentDate);
            stepData.setStep(tempStep);
            stepData.save();
        }else if(stepDataList.size()== 1) {
            StepData stepData = stepDataList.get(0);
            stepData.setStep(tempStep);
            stepData.save();
        }else {

        }
    }

    private void isNewDay(){
        String time = "00:00";
        if(time.equals(new SimpleDateFormat("HH:mm").format(new Date()))||!currentDate.equals(getTodayDate())){
            initTodayData();
        }
    }

    private void updateNotification(){
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = builder.setContentTitle("每日计步")
                .setContentText("今日步数"+currentStep+"步")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.logo)
                .setContentIntent(pendingIntent).build();

        mNotificationManager.notify(NOTIFY_STEP_ID,notification);

        if(mCallback != null){
            mCallback.updateUi(currentStep);
        }


    }

    /*更新UI回调*/
    private UpdateUiCallback mCallback;

    public void registerUiCallback(UpdateUiCallback paramUpdateUiCallback){
        this.mCallback = paramUpdateUiCallback;

    }


}
