package com.goertek.countstepeveryday.activity;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ListView;

import com.goertek.countstepeveryday.R;
import com.goertek.countstepeveryday.adapter.HistoryAdapter;
import com.goertek.countstepeveryday.bean.StepData;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private Toolbar titleToolbar;
    private ListView historyListView;
    private List<StepData> historyLists = new ArrayList<>();
    private HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        initView();
        initData();
    }

    private void initView(){
        titleToolbar = (Toolbar)findViewById(R.id.toolbar);
        historyListView = (ListView)findViewById(R.id.lv_history);

        setSupportActionBar(titleToolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar !=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle("历史记录");
        }
    }

    private void initData(){
        historyLists = DataSupport.where("date != ?","").find(StepData.class);
        historyAdapter = new HistoryAdapter(HistoryActivity.this,R.layout.history_item,historyLists);
        historyListView.setAdapter(historyAdapter);
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
}
