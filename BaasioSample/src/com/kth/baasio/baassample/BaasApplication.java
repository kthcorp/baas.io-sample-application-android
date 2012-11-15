
package com.kth.baasio.baassample;

import static com.kth.common.utils.LogUtils.makeLogTag;

import com.kth.baasio.Baasio;
import com.kth.baasio.gcm.GcmUtils;
import com.kth.baasio.gcm.callback.GcmTaskCallback;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

public class BaasApplication extends Application {
    private static final String TAG = makeLogTag(BaasApplication.class);

    private AsyncTask mGCMRegisterTask;

    @Override
    public void onCreate() {
        super.onCreate();

        Baasio.getInstance().init(this, BaasioConfig.BAASIO_URL, BaasioConfig.ORGANIZATION_ID,
                BaasioConfig.APPLICATION_ID, BaasioConfig.GCM_SENDER_ID);

        // Baasio.getInstance().init(this, BaasioConfig.BAASIO_URL,
        // BaasioConfig.ORGANIZATION_ID,
        // BaasioConfig.APPLICATION_ID);

        if (Baasio.getInstance().isGcmEnabled()) {
            mGCMRegisterTask = GcmUtils.registerGCMClient(this, new GcmTaskCallback() {

                @Override
                public void onResponse(String response) {
                    Log.i(TAG, response);
                }
            });
        }
    }

    @Override
    public void onTerminate() {
        if (mGCMRegisterTask != null) {
            mGCMRegisterTask.cancel(true);
        }

        GcmUtils.onDestroy(this);

        super.onTerminate();
    }
}
