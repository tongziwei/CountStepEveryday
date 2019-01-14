package com.goertek.countstepeveryday.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.goertek.countstepeveryday.R;
import com.goertek.countstepeveryday.UpdateUiCallback;
import com.goertek.countstepeveryday.service.StepService;
import com.goertek.countstepeveryday.util.SharedPreferenceUtil;
import com.goertek.countstepeveryday.view.StepArcView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView searchDataTextView;
    private TextView setTrainingPlanTextView;
    private StepArcView stepArcView;
    private TextView isSupportTextView;

    private SharedPreferenceUtil spUtil;
    private StepService.StepBinder stepBinder;
    private StepService stepService;
    private boolean isBind = false;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            stepBinder = (StepService.StepBinder) service;
            stepService = stepBinder.getService();
            String planWalk_QTY = (String) spUtil.getParams("planWalk_QTY","7000");
            stepArcView.setCurrentCount(Integer.parseInt(planWalk_QTY),stepService.getCurrentStep());

            stepService.registerUiCallback(new UpdateUiCallback() {
                @Override
                public void updateUi(int stepCount) {
                    String planWalk_QTY = (String) spUtil.getParams("planWalk_QTY","7000");
                    stepArcView.setCurrentCount(Integer.parseInt(planWalk_QTY),stepCount);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initView(){
        searchDataTextView = (TextView)findViewById(R.id.tv_data);
        setTrainingPlanTextView = (TextView)findViewById(R.id.tv_set);
        stepArcView = (StepArcView)findViewById(R.id.cc);
        isSupportTextView = (TextView)findViewById(R.id.tv_isSupport);
        searchDataTextView.setOnClickListener(this);
        setTrainingPlanTextView.setOnClickListener(this);
    }

    private void initData(){
        spUtil = new SharedPreferenceUtil(this);
        String planWalk_QTY = (String) spUtil.getParams("planWalk_QTY","7000");
        stepArcView.setCurrentCount(Integer.parseInt(planWalk_QTY),0);
        isSupportTextView.setText("计步中...");
        startUpService();
    }

    private void startUpService(){
        Intent intent = new Intent(this,StepService.class);
        isBind = bindService(intent,connection,BIND_AUTO_CREATE);
        startService(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_data:
                Intent historyIntent = new Intent(this,HistoryActivity.class);
                startActivity(historyIntent);
                break;
            case R.id.tv_set:
                Intent setPlanIntent = new Intent(this,SetPlanActivity.class);
                startActivity(setPlanIntent);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isBind){
            unbindService(connection);
        }
    }
}
