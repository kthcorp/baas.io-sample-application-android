
package com.kth.common.sns.tools.facebook;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.kth.baasio.auth.AuthUtils;
import com.kth.common.sns.tools.SnsConfig;
import com.kth.common.sns.tools.facebook.SessionEvents.AuthListener;

import org.usergrid.android.client.callbacks.ApiResponseCallback;
import org.usergrid.java.client.response.ApiResponse;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

public class FacebookAuthActivity extends SherlockFragmentActivity {
    public static final String _TAG = "FacebookAuthActivity";

    private Context mContext;

    public static final String INTENT_REQUEST_CODE = "request_code";

    public static final int REQUEST_ACCESS_TOKEN_FOR_LOGIN = 1;

    public static final String INTENT_RESULT_ERRORMSG = "error_msg";

    public static final String INTENT_RESULT_TOKEN = "token";

    public static final String INTENT_RESULT_USER = "user";

    private int mRequestMode = -1;

    private Facebook mFacebook;

    private SessionListener mSessionListener = new SessionListener();

    private String[] mPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        mRequestMode = getIntent().getIntExtra(INTENT_REQUEST_CODE, -1);

        init();

        mFacebook.authorize(this, mPermissions, new LoginDialogListener());
    }

    public void init() {
        mFacebook = new Facebook(SnsConfig.FACEBOOK_APPID);

        mPermissions = SnsConfig.FACEBOOK_PERMISSIONS;

        FacebookSessionStore.restore(mFacebook, this);
        SessionEvents.addAuthListener(mSessionListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mFacebook.authorizeCallback(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SessionEvents.removeAuthListener(mSessionListener);
    }

    private final class LoginDialogListener implements DialogListener {
        public void onComplete(Bundle values) {
            SessionEvents.onLoginSuccess();
        }

        public void onFacebookError(FacebookError error) {
            SessionEvents.onLoginError(error.getMessage());
        }

        public void onError(DialogError error) {
            SessionEvents.onLoginError(error.getMessage());
        }

        public void onCancel() {
            SessionEvents.onLoginError("Action Canceled");
        }
    }

    Runnable task = new Runnable() {

        @Override
        public void run() {
            showProgressDialog("로그인 중입니다.");

            FacebookSessionStore.save(mFacebook, mContext);

            String token = mFacebook.getAccessToken();
            if (mRequestMode == REQUEST_ACCESS_TOKEN_FOR_LOGIN) {
                AuthUtils.signupViaFacebook(mContext, token, new ApiResponseCallback() {

                    @Override
                    public void onException(Exception e) {
                        dissmissProgressDialog();

                        Intent result = new Intent();
                        result.putExtra(INTENT_RESULT_ERRORMSG, e.toString());

                        setResult(RESULT_CANCELED, result);
                        finish();
                    }

                    @Override
                    public void onResponse(ApiResponse response) {
                        dissmissProgressDialog();

                        if (response != null) {
                            String error = response.getError();
                            if (error == null || error.length() <= 0) {
                                setResult(RESULT_OK);

                                finish();
                            } else {
                                Intent result = new Intent();
                                result.putExtra(INTENT_RESULT_ERRORMSG,
                                        response.getErrorDescription());

                                setResult(RESULT_CANCELED, result);
                                finish();
                            }
                        }
                    }
                });
            } else {
                setResult(RESULT_OK);
                finish();
            }
        }
    };

    private class SessionListener implements AuthListener {

        public void onAuthSucceed() {
            new Handler().postDelayed(task, 100);
            // showProgressDialog("로그인 중입니다.");
            //
            // FacebookSessionStore.save(mFacebook, mContext);
            //
            // String token = mFacebook.getAccessToken();
            // if(mRequestMode == REQUEST_ACCESS_TOKEN_FOR_LOGIN) {
            // Kanu.getInstance().authorizeAppUserViaFacebookAsync(token, new
            // ApiResponseCallback() {
            //
            // @Override
            // public void onException(Exception e) {
            // dissmissProgressDialog();
            //
            // Intent result = new Intent();
            // result.putExtra(INTENT_RESULT_ERRORMSG, e.toString());
            //
            // setResult(RESULT_CANCELED, result);
            // finish();
            // }
            //
            // @Override
            // public void onResponse(ApiResponse response) {
            // dissmissProgressDialog();
            //
            // if(response != null) {
            // String error = response.getError();
            // if(error == null || error.length() <= 0) {
            // Intent result = new Intent();
            // result.putExtra(INTENT_RESULT_TOKEN, response.getAccessToken());
            // result.putExtra(INTENT_RESULT_USER,
            // response.getUser().toString());
            // setResult(RESULT_OK, result);
            //
            // finish();
            // } else {
            // Toast.makeText(mContext, error, Toast.LENGTH_LONG).show();
            //
            // Intent result = new Intent();
            // result.putExtra(INTENT_RESULT_ERRORMSG, error);
            //
            // setResult(RESULT_CANCELED, result);
            // finish();
            // }
            // } else {
            // // Toast.makeText(mContext, "Unknown", Toast.LENGTH_LONG).show();
            //
            // Intent result = new Intent();
            // result.putExtra(INTENT_RESULT_ERRORMSG, "Unknown Error");
            //
            // setResult(RESULT_CANCELED, result);
            // finish();
            // }
            // }
            // });
            // } else {
            // setResult(RESULT_OK);
            // finish();
            // }
        }

        public void onAuthFail(String error) {
            Intent result = new Intent();
            result.putExtra(INTENT_RESULT_ERRORMSG, error);

            setResult(RESULT_CANCELED, result);
            finish();
        }
    }

    private void showProgressDialog(String body) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction. We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        ProgessDialogFragment progress = ProgessDialogFragment.newInstance();
        progress.setBody(body);
        progress.show(ft, "dialog");
    }

    private void dissmissProgressDialog() {
        ProgessDialogFragment progress = (ProgessDialogFragment)getSupportFragmentManager()
                .findFragmentByTag("dialog");

        if (progress != null) {
            progress.dismiss();
        }
    }
}
