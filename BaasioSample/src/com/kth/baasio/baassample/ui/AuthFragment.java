
package com.kth.baasio.baassample.ui;

import static com.kth.common.utils.LogUtils.LOGE;
import static com.kth.common.utils.LogUtils.makeLogTag;
import static org.usergrid.java.client.utils.ObjectUtils.isEmpty;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.kth.baasio.auth.AuthUtils;
import com.kth.baasio.baassample.R;
import com.kth.baasio.baassample.preferences.SamplePreferences;
import com.kth.baasio.baassample.ui.BaasMainActivity.OptionsItemSelectedListener;
import com.kth.common.sns.tools.facebook.FacebookAuthActivity;
import com.kth.common.sns.tools.facebook.FacebookSessionStore;

import org.usergrid.android.client.callbacks.ApiResponseCallback;
import org.usergrid.java.client.response.ApiResponse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A {@link ListFragment} showing a list of sessions. This fragment supports
 * multiple-selection using the contextual action bar (on API 11+ devices), and
 * also supports a separate 'activated' state for indicating the
 * currently-opened detail view on tablet devices.
 */
public class AuthFragment extends SherlockFragment implements OptionsItemSelectedListener {

    private static final String TAG = makeLogTag(AuthFragment.class);

    private static final int REQUEST_ACCESS_TOKEN_FOR_LOGIN = 1;

    private ViewGroup mRootView;

    private EditText mEmailId;

    private EditText mPassword;

    private TextView mResult;

    public AuthFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mRootView = (ViewGroup)inflater.inflate(R.layout.fragment_auth, null);

        mEmailId = (EditText)mRootView.findViewById(R.id.id);
        mPassword = (EditText)mRootView.findViewById(R.id.password);

        if (!isEmpty(SamplePreferences.getLoginId(getActivity()))) {
            mEmailId.setText(SamplePreferences.getLoginId(getActivity()));
        }

        mResult = (TextView)mRootView.findViewById(R.id.result);

        Button signup = (Button)mRootView.findViewById(R.id.signup);
        signup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String email = mEmailId.getText().toString();
                String password = mPassword.getText().toString();

                if (!isEmpty(email)) {
                    SamplePreferences.setLoginId(getActivity(), email);
                }

                AuthUtils.signup(getActivity(), email, email, email, password,
                        new ApiResponseCallback() {

                            @Override
                            public void onException(Exception e) {
                                // TODO Auto-generated method stub

                            }

                            @Override
                            public void onResponse(ApiResponse response) {
                                if (response != null) {
                                    mResult.setText(response.toString());
                                    LOGE(TAG, "ErrorCode >>> " + response.getError());
                                }
                            }
                        });
            }
        });

        Button unsubscribe = (Button)mRootView.findViewById(R.id.unsubscribe);
        unsubscribe.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String email = mEmailId.getText().toString();
                AuthUtils.unsubscribe(getActivity(), email, new ApiResponseCallback() {

                    @Override
                    public void onException(Exception e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onResponse(ApiResponse response) {
                        if (response != null) {
                            mResult.setText(response.toString());

                            if (!isEmpty(response.getError())) {
                                Toast.makeText(getActivity(), response.getErrorDescription(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            }
        });

        Button login = (Button)mRootView.findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String email = mEmailId.getText().toString();
                String password = mPassword.getText().toString();

                if (!isEmpty(email)) {
                    SamplePreferences.setLoginId(getActivity(), email);
                }

                AuthUtils.login(getActivity(), email, password, new ApiResponseCallback() {

                    @Override
                    public void onException(Exception e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onResponse(ApiResponse response) {
                        if (response != null) {
                            mResult.setText(response.toString());

                            if (!isEmpty(response.getError())) {
                                Toast.makeText(getActivity(), response.getErrorDescription(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            }
        });

        Button logout = (Button)mRootView.findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AuthUtils.logout(getActivity());

                FacebookSessionStore.clear(getActivity());
            }
        });

        Button signupFB = (Button)mRootView.findViewById(R.id.signupFB);
        signupFB.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FacebookAuthActivity.class);
                intent.putExtra(FacebookAuthActivity.INTENT_REQUEST_CODE,
                        FacebookAuthActivity.REQUEST_ACCESS_TOKEN_FOR_LOGIN);
                startActivityForResult(intent, REQUEST_ACCESS_TOKEN_FOR_LOGIN);
            }
        });

        Button loginFB = (Button)mRootView.findViewById(R.id.loginFB);
        loginFB.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FacebookAuthActivity.class);
                intent.putExtra(FacebookAuthActivity.INTENT_REQUEST_CODE,
                        FacebookAuthActivity.REQUEST_ACCESS_TOKEN_FOR_LOGIN);
                startActivityForResult(intent, REQUEST_ACCESS_TOKEN_FOR_LOGIN);
            }
        });

        return mRootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_CANCELED) {
            if (requestCode == REQUEST_ACCESS_TOKEN_FOR_LOGIN) {
                if (data != null) {
                    String error = data.getStringExtra(FacebookAuthActivity.INTENT_RESULT_ERRORMSG);

                    Toast.makeText(getActivity(), "Facebook Login Fail: " + error,
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "Facebook Login Fail: Unknown", Toast.LENGTH_LONG)
                            .show();
                }

            }
        } else {
            Toast.makeText(getActivity(), "Facebook Login Success", Toast.LENGTH_LONG).show();
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

}
