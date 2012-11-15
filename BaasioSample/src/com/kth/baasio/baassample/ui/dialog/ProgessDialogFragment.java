
package com.kth.baasio.baassample.ui.dialog;

import static com.kth.common.utils.LogUtils.makeLogTag;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;

public class ProgessDialogFragment extends DialogFragment {
    private static final String TAG = makeLogTag(ProgessDialogFragment.class);

    private String mBody;

    private int mStyle = ProgressDialog.STYLE_SPINNER;

    private int mProgressMax = 100;

    private ProgressDialog mDialog;

    public static ProgessDialogFragment newInstance() {
        ProgessDialogFragment frag = new ProgessDialogFragment();
        return frag;
    }

    public void setBody(String body) {
        mBody = body;
    }

    public void setStyle(int style) {
        mStyle = style;
    }

    public void setMax(int progressMax) {
        mProgressMax = progressMax;
    }

    public void setProgress(int progress) {
        if (mDialog != null) {
            mDialog.setProgress(progress);
        }
    }

    public int getProgress() {
        if (mDialog != null) {
            return mDialog.getProgress();
        }

        return -1;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDialog = new ProgressDialog(getActivity());
        if (mBody != null && mBody.length() > 0) {
            mDialog.setMessage(mBody);
        }

        mDialog.setCancelable(false);

        mDialog.setProgressStyle(mStyle);
        if (mStyle != ProgressDialog.STYLE_SPINNER) {
            mDialog.setMax(mProgressMax);
            mDialog.setIndeterminate(false);
        } else {
            mDialog.setIndeterminate(true);
        }
        mDialog.setProgress(50);
        // Disable the back button
        OnKeyListener keyListener = new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                }
                return false;
            }
        };
        mDialog.setOnKeyListener(keyListener);
        return mDialog;
    }
}
