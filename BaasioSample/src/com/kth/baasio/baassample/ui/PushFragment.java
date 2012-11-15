
package com.kth.baasio.baassample.ui;

import static com.kth.common.utils.LogUtils.makeLogTag;
import static org.usergrid.java.client.utils.ObjectUtils.isEmpty;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gcm.GCMRegistrar;
import com.kth.baasio.Baasio;
import com.kth.baasio.baassample.R;
import com.kth.baasio.baassample.ui.BaasMainActivity.OptionsItemSelectedListener;
import com.kth.baasio.gcm.GcmUtils;
import com.kth.baasio.gcm.callback.GcmTaskCallback;
import com.kth.baasio.preferences.BaasPreferences;

import org.usergrid.android.client.callbacks.ApiResponseCallback;
import org.usergrid.java.client.response.ApiResponse;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PushFragment extends SherlockFragment implements OptionsItemSelectedListener,
        OnClickListener {

    private static final String TAG = makeLogTag(PushFragment.class);

    private ViewGroup mRootView;

    private TextView mLoginStatus;

    private TextView mRegId;

    private TextView mDeviceUuid;

    private EditText mTag;

    private TextView mResult;

    private AsyncTask mGCMRegisterTask;

    public PushFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mRootView = (ViewGroup)inflater.inflate(R.layout.fragment_push, null);

        mRegId = (TextView)mRootView.findViewById(R.id.regId);
        mDeviceUuid = (TextView)mRootView.findViewById(R.id.deviceUuid);
        mLoginStatus = (TextView)mRootView.findViewById(R.id.loginStatus);
        mTag = (EditText)mRootView.findViewById(R.id.tag);

        Button regDevice = (Button)mRootView.findViewById(R.id.buttonRegDevice);
        regDevice.setOnClickListener(this);

        Button unregDevice = (Button)mRootView.findViewById(R.id.buttonUnregDevice);
        unregDevice.setOnClickListener(this);

        Button getDevice = (Button)mRootView.findViewById(R.id.buttonGetDevice);
        getDevice.setOnClickListener(this);

        Button updateDevice = (Button)mRootView.findViewById(R.id.buttonUpdateDevice);
        updateDevice.setOnClickListener(this);

        mResult = (TextView)mRootView.findViewById(R.id.result);

        String regId = GCMRegistrar.getRegistrationId(getActivity());
        if (regId != null) {
            mRegId.setText(regId);
        }

        String deviceUuid = BaasPreferences.getDeviceUuidForPush(getActivity());
        if (deviceUuid != null) {
            mDeviceUuid.setText(deviceUuid);
        }

        String tags = BaasPreferences.getRegisteredTags(getActivity());
        if (tags != null) {
            mTag.setText(tags);
        }

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isEmpty(Baasio.getInstance().getAccessToken())) {
            mLoginStatus.setText("logout");
        } else {
            mLoginStatus.setText("login");
        }

        String deviceUuid = BaasPreferences.getDeviceUuidForPush(getActivity());
        if (deviceUuid != null) {
            mDeviceUuid.setText(deviceUuid);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /*
     * (non-Javadoc)
     * @see com.kth.kanu.baassample.ui.BaaSActivity.OptionsItemSelectedListener#
     * onParentOptionsItemSelected(com.actionbarsherlock.view.MenuItem)
     */
    @Override
    public boolean onParentOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonRegDevice: {
                String tagString = mTag.getText().toString().trim();
                mGCMRegisterTask = GcmUtils.registerGCMClientWithTags(getActivity(), tagString,
                        new GcmTaskCallback() {

                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(getActivity(), "register:" + response,
                                        Toast.LENGTH_LONG).show();

                                String deviceUuid = BaasPreferences
                                        .getDeviceUuidForPush(getActivity());
                                if (deviceUuid != null) {
                                    mDeviceUuid.setText(deviceUuid);
                                }
                            }
                        });
                break;
            }
            case R.id.buttonUnregDevice: {
                GcmUtils.unregisterGCMClient(getActivity(), new GcmTaskCallback() {

                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getActivity(), response, Toast.LENGTH_LONG).show();

                        String deviceUuid = BaasPreferences.getDeviceUuidForPush(getActivity());
                        if (deviceUuid != null) {
                            mDeviceUuid.setText(deviceUuid);
                        }
                    }
                });
                break;
            }
            case R.id.buttonGetDevice: {
                if (!GCMRegistrar.isRegisteredOnServer(getActivity())) {
                    Toast.makeText(getActivity(), "Already unregistered on the GCM server.",
                            Toast.LENGTH_LONG).show();
                } else {
                    String deviceUuid = BaasPreferences.getDeviceUuidForPush(getActivity());
                    if (isEmpty(deviceUuid)) {
                        Toast.makeText(getActivity(), "Device Uuid is empty.", Toast.LENGTH_LONG)
                                .show();
                        break;
                    } else {
                        Baasio.getInstance().getDeviceForPushAsync(deviceUuid,
                                new ApiResponseCallback() {

                                    @Override
                                    public void onException(Exception e) {
                                        if (e != null) {
                                            mResult.setText("getDeviceForPushAsync Exception: "
                                                    + e.toString());
                                        }
                                    }

                                    @Override
                                    public void onResponse(ApiResponse response) {
                                        if (response != null) {
                                            mResult.setText(response.toString());
                                        }
                                    }
                                });
                    }
                }
                break;
            }
            case R.id.buttonUpdateDevice: {
                String tagString = mTag.getText().toString().trim();
                mGCMRegisterTask = GcmUtils.registerGCMClientWithTags(getActivity(), tagString,
                        new GcmTaskCallback() {

                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(getActivity(), "update:" + response,
                                        Toast.LENGTH_LONG).show();

                                String deviceUuid = BaasPreferences
                                        .getDeviceUuidForPush(getActivity());
                                if (deviceUuid != null) {
                                    mDeviceUuid.setText(deviceUuid);
                                }
                            }
                        });
                break;
            }
        }
    }

}
