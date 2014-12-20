package com.kekwanu.androidserviceexample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by onwuneme on 12/10/14.
 */
public class BootCompletedIntentReceiver extends BroadcastReceiver {
    private static final String TAG = BootCompletedIntentReceiver.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent backgroundServiceIntent = new Intent(context, MyService.class);
        context.startService(backgroundServiceIntent);
    }
}
