package com.hyperkode.friendshare.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;

import com.hyperkode.friendshare.R;
import com.hyperkode.friendshare.fragment.LoginFragment;

public class StartActivity2 extends FragmentActivity {
    private static final String TAG = "FragmentActivity";
    private ProgressBar titleProgressBar;
    private Activity mThis;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mThis = this;
        logDeviceInfo();
        setupScreen();
        initialize(new Handler() {
            @Override
            public void handleMessage(Message message) {
                FragmentManager fm = StartActivity2.this.getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.add(R.id.main_view_containter, LoginFragment.newInstance());
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.addToBackStack(null);
                ft.commit();
            }
        });
    }
    
    private void setupScreen() {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
//      setContentView(R.layout.splash);
        setContentView(R.layout.main_framelayout);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
        titleProgressBar = (ProgressBar) findViewById(R.id.title_progressbar);
    }
    
    private void logDeviceInfo() {
        Log.i(TAG, "Device Info:");        
        Log.i(TAG, "  Build.USER:           " + Build.USER);
        Log.i(TAG, "  Build.BRAND:          " + Build.BRAND);
        Log.i(TAG, "  Build.DEVICE:         " + Build.DEVICE);
        Log.i(TAG, "  Build.FINGERPRINT:    " + Build.FINGERPRINT);
        Log.i(TAG, "  Build.TAGS:           " + Build.TAGS);
        Log.i(TAG, "  Build.VERSION.SDK_INT:" + String.valueOf(Build.VERSION.SDK_INT));       
    }
  
    private void initialize(final Handler completionHandler) {
        Thread splashThread =  new Thread(){
            @Override
            public void run() {
                if (titleProgressBar != null)
                    titleProgressBar.setVisibility(View.VISIBLE);
                splashScreenDelayStub();
                //Message m = Message.obtain();
                completionHandler.sendEmptyMessage(1);
            }
        };
        splashThread.start();        
    }
    
    public boolean splashScreenDelayStub() {        
        try {
            synchronized(this){
                wait(1000);
            }
        } catch(InterruptedException ex){                    
        }
        return false;
    }    

}
