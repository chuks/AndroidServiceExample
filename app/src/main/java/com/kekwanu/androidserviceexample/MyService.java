package com.kekwanu.androidserviceexample;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;

import java.io.IOException;

public class MyService extends Service {
    private final static String TAG = MyService.class.getCanonicalName();
    private boolean bound = false;
    private IBinder mBinder = new LocalBinder();
    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying;
    private int count;
    private boolean initialized;

    public MyService() {

    }

    @Override
    public void onCreate(){

        super.onCreate();

        count = 0;
        initialize();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String intent1 = intent.getStringExtra("data");

        return Service.START_STICKY;
    }

    public class LocalBinder extends Binder {
        MyService getService() {

            return MyService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        bound = true;

        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        bound = false;
        return true; // ensures onRebind is called
    }

    @Override
    public void onRebind(Intent intent) {

        bound = true;
    }

    /*
    HACK --swiping the app from task list will KILL this service, even though the Settings will show 0 process, 1 service running. This
    is a 4.4 bug. See more here - http://bit.ly/1fTJzdm and fix here http://bit.ly/1dzp1pT
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {

        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(
                getApplicationContext(), 1, restartServiceIntent,
                PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext()
                .getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);

        super.onTaskRemoved(rootIntent);
    }

    public boolean initialize() {

        if (vibrator == null) {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }

        initialized = true;
        return initialized;
    }

    public void setInitializedFlag(boolean flag){
        Log.i(TAG, "setInitializedFlag");

        initialized = flag;
    }

    public boolean getInitializedFlag(){
        Log.i(TAG, "getInitializedFlag");

        return initialized;
    }

    public void playNotificationSound() {
        try{
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) );

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                public boolean onError(MediaPlayer mp, int what, int extra) {

                    return true;
                }
            });

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
                public void onPrepared(MediaPlayer mp){

                    mp.setLooping(true);
                    mp.start();

                    isPlaying = true;
                }
            });

            mediaPlayer.prepare();
            count++;

        }
        catch (IllegalArgumentException e) {
            Log.i(TAG, "playNotificationSound - You might not set the URI correctly!");
        }
        catch (SecurityException e) {
            Log.i(TAG, "playNotificationSound -  URI cannot be accessed, permission needed");
        }
        catch (IllegalStateException e) {
            Log.i(TAG, "playNotificationSound -  Media Player is not in correct state");
        }
        catch (IOException e) {
            Log.i(TAG, "playNotificationSound -  IO Error occurred");
        }

        if (vibrator.hasVibrator()) {
            long[] pattern = {0, 100, 1000};
            vibrator.vibrate(pattern, 0);
        }
        else {
            Log.i(TAG, "playNotificationSound - sorry, your device cannot vibrate...");

            vibrator.cancel();
        }
    }

    public void stopNotificationSound(){
        Log.i(TAG, "stopNotificationSound");

        if (mediaPlayer != null){
            try{
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;

                isPlaying = false;
            }
            catch(IllegalStateException e){
                Log.i(TAG, "stopNotificationSound - "+e.getMessage());

                isPlaying = false;
                mediaPlayer = null;

                e.printStackTrace();
            }

            if (vibrator != null){
                Log.i(TAG, "stopNotificationSound --canceling vibration...");

                vibrator.cancel();
            }
        }
        else{
            Log.i(TAG, "stopNotificationSound --player does not exist, ignoring...");
        }
    }

    public int getPlayCount(){
        return count;
    }

    @Override
    public void onDestroy(){
        Log.i(TAG,"onDestroy");

        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        if (vibrator != null){
            vibrator.cancel();
            vibrator = null;
        }

        isPlaying = false;
    }
}

