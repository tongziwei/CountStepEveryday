package com.goertek.countstepeveryday.activity;

import android.app.TimePickerDialog;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.goertek.countstepeveryday.R;
import com.goertek.countstepeveryday.util.SharedPreferenceUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SetPlanActivity extends AppCompatActivity implements View.OnClickListener {
    private Toolbar toolbar;
    private EditText tv_step_number;
    private CheckBox cb_remind;
    private TextView tv_remind_time;
    private Button btn_save;

    private SharedPreferenceUtil sp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_plan);
        initView();
    }

    private void initView(){
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle("锻炼计划");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        tv_step_number = (EditText)findViewById(R.id.tv_step_number);
        cb_remind = (CheckBox)findViewById(R.id.cb_remind);
        tv_remind_time = (TextView)findViewById(R.id.tv_remind_time);
        btn_save = (Button)findViewById(R.id.btn_save);
        btn_save.setOnClickListener(this);
        tv_remind_time.setOnClickListener(this);

        sp = new SharedPreferenceUtil(this);
        String planStepNumber = (String) sp.getParams("planWalk_QTY","7000");
        tv_step_number.setText(planStepNumber);
        boolean isRemind = (boolean) sp.getParams("isRemind",true);
        if(isRemind){
            cb_remind.setChecked(true);
        }else{
            cb_remind.setChecked(false);
        }
        String reminderTime = (String) sp.getParams("remindTime","20:00");
        tv_remind_time.setText(reminderTime);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_save:
                sp.setParams("planWalk_QTY",tv_step_number.getText().toString());
                if(cb_remind.isChecked()){
                    sp.setParams("isRemind",true);
                }else {
                    sp.setParams("isRemind",false);
                }
                sp.setParams("remindTime",tv_remind_time.getText().toString());
                break;
            case R.id.tv_remind_time:
                showTimePickerDialog();
                break;
            default:
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            default:
        }
        return true;
    }

    /**
     * 显示时间选择对话框
     */
    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(SetPlanActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String time = hourOfDay + ":" + minute;
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                try {
                    Date timeDate = sdf.parse(time);
                    tv_remind_time.setText(sdf.format(timeDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }
}
