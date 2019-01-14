package com.goertek.countstepeveryday.bean;

import org.litepal.crud.DataSupport;

/**
 * Created by clara.tong on 2018/7/26.
 */

public class StepData extends DataSupport{
    private int id;
    private String date;
    private int step;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    @Override
    public String toString() {
        return "StepData{" +
                "id=" + id +
                ", date='" + date + '\'' +
                ", step='" + step + '\'' +
                '}';
    }
}
