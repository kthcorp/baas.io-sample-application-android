
package com.kth.common.sns.tools.twitter;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.kth.baasio.baassample.R;
import com.kth.common.sns.tools.SnsConfig;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class TwitterAuthActivity extends SherlockFragmentActivity {
    private static final String _TAG = "TwitterAuthActivity";

    private Context mContext;

    // private SnsAuthManager mSnsAuthManager = null;

    private static final int TWITTER_LOGIN_REQUEST = 1;

    public static final int REQUEST_ACCESS_TOKEN_FOR_LOGIN = 1;

    public static final String INTENT_RESULT_TOKEN = "token";

    public static final String INTENT_RESULT_TOKENSECRET = "tokensecret";

    public static final String INTENT_RESULT_ID = "id";

    public static final String INTENT_RESULT_UNIQID = "uniqid";

    public static final String INTENT_RESULT_ERRORMSG = "error_msg";

    // Defined SNS Login Status
    // private static final int TWITTERLOGIN = 200;
    // private static final int TWITTERLOGOUT = -200;

    // private static final int SNS_LOGIN_SUCCESS = 0;
    // private static final int SNS_LOGIN_FAIL_ETC = 3;

    public static final String INTENT_REQUEST_CODE = "request_code";

    private int mRequestMode = -1;

    private Twitter mTwitter;

    private static RequestToken mRequestToken;

    private AccessToken mAccessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sns_twitter);

        mContext = this;

        mTwitter = new TwitterFactory().getInstance();
        mTwitter.setOAuthConsumer(SnsConfig.TWITTER_CONSUMER_KEY, SnsConfig.TWITTER_CONSUMER_SECRET);

        mRequestMode = getIntent().getIntExtra(INTENT_REQUEST_CODE, -1);

        GetAuthRequestToken task = new GetAuthRequestToken();
        task.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TWITTER_LOGIN_REQUEST) {
            if (resultCode == RESULT_OK) {
                String url = data.getStringExtra("URL");
                Uri uri = Uri.parse(url);

                GetAccessToken task = new GetAccessToken(uri);
                task.execute();
                return;
            } else if (resultCode == RESULT_CANCELED) {
                setResult(RESULT_CANCELED);
                finish();
            }

            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    class GetAuthRequestToken extends AsyncTask<Void, TwitterException, RequestToken> {

        private Intent mIntent;

        public GetAuthRequestToken() {
            mIntent = new Intent();
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected RequestToken doInBackground(Void... arg0) {
            RequestToken result = null;
            try {
                result = mTwitter.getOAuthRequestToken(SnsConfig.TWITTER_CALLBACK_URL);
            } catch (TwitterException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                publishProgress(e);
            }
            return result;
        }

        @Override
        protected void onProgressUpdate(TwitterException... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);

            mIntent.putExtra(INTENT_RESULT_ERRORMSG, values[0].getErrorMessage());
        }

        @Override
        protected void onPostExecute(RequestToken result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            if (result != null) {
                mRequestToken = result;

                Intent intent = new Intent(mContext, SNSWebView.class);
                intent.putExtra("LOGIN_TYPE", "TW");
                intent.putExtra("URL", mRequestToken.getAuthorizationURL());

                startActivityForResult(intent, TWITTER_LOGIN_REQUEST);

            } else {
                setResult(RESULT_CANCELED, mIntent);

                finish();
            }
        }
    }

    class GetAccessToken extends AsyncTask<Void, TwitterException, AccessToken> {

        private Uri mUri;

        private Intent mIntent;

        public GetAccessToken(Uri uri) {
            mUri = uri;
            mIntent = new Intent();
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected AccessToken doInBackground(Void... arg0) {
            AccessToken result = null;
            if (mUri != null) {
                try {
                    result = mTwitter.getOAuthAccessToken(mRequestToken,
                            mUri.getQueryParameter("oauth_verifier"));
                } catch (TwitterException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    publishProgress(e);
                }
            }
            return result;
        }

        @Override
        protected void onProgressUpdate(TwitterException... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);

            mIntent.putExtra(INTENT_RESULT_ERRORMSG, values[0].getErrorMessage());
        }

        @Override
        protected void onPostExecute(AccessToken result) {
            super.onPostExecute(result);

            if (result != null) {
                mAccessToken = result;

                TwitterSessionStore.save(mAccessToken, mContext);

                if (mRequestMode == REQUEST_ACCESS_TOKEN_FOR_LOGIN) {
                    mIntent.putExtra(INTENT_RESULT_TOKEN, mAccessToken.getToken());
                    mIntent.putExtra(INTENT_RESULT_TOKENSECRET, mAccessToken.getTokenSecret());
                    mIntent.putExtra(INTENT_RESULT_ID, mAccessToken.getScreenName());
                    mIntent.putExtra(INTENT_RESULT_UNIQID, mAccessToken.getUserId());

                    setResult(RESULT_OK, mIntent);
                } else {
                    setResult(RESULT_OK);
                }

                finish();
            } else {
                setResult(RESULT_CANCELED, mIntent);

                finish();
            }
        }
    }
}
