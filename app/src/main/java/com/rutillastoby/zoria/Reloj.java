package com.rutillastoby.zoria;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.instacart.library.truetime.TrueTime;

import java.io.IOException;

public class Reloj extends MultiDexApplication {
    private static final String TAG = Reloj.class.getSimpleName();

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initTrueTime();
    }

    /**
     * init the TrueTime using a AsyncTask.
     */
    private void initTrueTime() {
        Log.d("HOLE", "HOLE");
        new InitTrueTimeAsyncTask().execute();

    }

    // a little part of me died, having to use this
    private class InitTrueTimeAsyncTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... params) {
            try {
                TrueTime.build()
                        //.withSharedPreferences(SampleActivity.this)
                        .withNtpHost("time.google.com")
                        .withLoggingEnabled(false)
                        .withSharedPreferencesCache(Reloj.this)
                        .withConnectionTimeout(3_1428)
                        .initialize();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "something went wrong when trying to initialize TrueTime", e);
            }
            return null;
        }
    }


}
