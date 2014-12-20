package com.kekwanu.androidserviceexample;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.kekwanu.androidserviceexample.MyService;


public class MainActivity extends Activity {
    private final static String TAG = MainActivity.class.getCanonicalName();
    private MyService myService;
    private boolean mBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, MyService.class);
        intent.putExtra("data","datra");
        //intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
        startService(intent);
        bindService(intent, mServiceConnection, 0);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "onServiceDisconnected");

            myService = null;
            mBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            myService = ((MyService.LocalBinder) service).getService();

            if (!myService.getInitializedFlag()){
                Log.i(TAG, "onServiceConnected - service is already running and initialized.");

                if (!myService.initialize()) {
                    Log.i(TAG, "Unable to initialize Bluetooth");
                    finish();
                }
            }
            else{
                Log.i(TAG, "onServiceConnected - service is already running and initialized, this is just a reconnect...");
            }

            Log.i(TAG, "onServiceConnected - we are connected...continue");

            mBound = true;

            //begin only when we have a connection to the service
            doStuff();
        }
    };

    private void doStuff(){
        Log.i(TAG, "doStuff");

        myService.playNotificationSound();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
