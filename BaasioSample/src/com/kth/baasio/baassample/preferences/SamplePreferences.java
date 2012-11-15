
package com.kth.baasio.baassample.preferences;

import static com.kth.common.utils.LogUtils.makeLogTag;

import com.kth.common.PlatformSpecificImplementationFactory;
import com.kth.common.preference.SharedPreferenceSaver;

import android.content.Context;
import android.content.SharedPreferences;

public class SamplePreferences {
    private static final String TAG = makeLogTag(SamplePreferences.class);

    private static final String DEFAULT_PREFERENCES_NAME = "SamplePreferences";

    private static final String SHARED_PREFERENCE_NAME_LOGIN_ID = "baas_login_id";

    private static SharedPreferences mPreferences;

    private static SharedPreferences getPreference(Context context) {
        if (mPreferences == null)
            mPreferences = context.getSharedPreferences(DEFAULT_PREFERENCES_NAME,
                    Context.MODE_PRIVATE);

        return mPreferences;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Clone is not allowed.");
    }

    public static void clear(Context context) {
        SharedPreferences.Editor editor = getPreference(context).edit();
        editor.clear();

        SharedPreferenceSaver saver = PlatformSpecificImplementationFactory
                .getSharedPreferenceSaver(context);
        saver.savePreferences(editor, false);
    }

    public static void setLoginId(Context context, String string) {
        SharedPreferences.Editor editor = getPreference(context).edit();
        editor.putString(SHARED_PREFERENCE_NAME_LOGIN_ID, string);

        SharedPreferenceSaver saver = PlatformSpecificImplementationFactory
                .getSharedPreferenceSaver(context);
        saver.savePreferences(editor, false);
    }

    public static String getLoginId(Context context) {
        SharedPreferences prefs = getPreference(context);
        String result = prefs.getString(SHARED_PREFERENCE_NAME_LOGIN_ID, "");

        return result;
    }
}
