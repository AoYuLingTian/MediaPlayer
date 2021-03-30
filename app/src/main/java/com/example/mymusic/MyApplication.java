package com.example.mymusic;

import android.app.Application;

/**
 * @Author: longyu
 * @CreateDate: 2021/3/29 9:42
 * @Description:
 */
public class MyApplication extends Application {

    private static MyApplication myApplication = null;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
    }

    public static MyApplication getInstance(){
        return myApplication;
    }
}
