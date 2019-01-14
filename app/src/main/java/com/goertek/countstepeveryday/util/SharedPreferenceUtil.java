package com.goertek.countstepeveryday.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by clara.tong on 2019/1/7.
 */

public class SharedPreferenceUtil {
    /*SharePreference存储工具类*/
    private Context context;

    private String FILE_NAME;

    public SharedPreferenceUtil(Context context) {
        this.context = context;
    }

    public SharedPreferenceUtil(String FILE_NAME) {
        this.FILE_NAME = FILE_NAME;
    }

    public void setParams(String key,Object object){
        String type = object.getClass().getSimpleName();
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if("String".equals(type)){
            editor.putString(key,object.toString());
        }else if ("Integer".equals(type)){
            editor.putInt(key,(Integer) object);
        }else if ("Boolean".equals(type)){
            editor.putBoolean(key,(Boolean)object);
        }else if ("Float".equals(type)) {
            editor.putFloat(key, (Float) object);
        } else if ("Long".equals(type)) {
            editor.putLong(key, (Long) object);
        }
        editor.commit();
    }

    public Object getParams(String key,Object defaultObject){
        String type = defaultObject.getClass().getSimpleName();
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE);
        if("String".equals(type)){
            return sp.getString(key,defaultObject.toString());
        }else if ("Integer".equals(type)){
            return sp.getInt(key,(Integer) defaultObject);
        }else if ("Boolean".equals(type)){
            return sp.getBoolean(key,(Boolean)defaultObject);
        }else if ("Float".equals(type)) {
            return sp.getFloat(key,(Float)defaultObject);
        } else if ("Long".equals(type)) {
            return sp.getLong(key,(Long)defaultObject);
        }
        return null;
    }

    public void remove(String key){
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        editor.commit();
    }

    public void clear(){
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();
    }
}
