
package com.kth.common.sns.tools.twitter;

import com.actionbarsherlock.app.SherlockActivity;
import com.kth.baasio.baassample.R;
import com.kth.common.sns.tools.SnsConfig;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SNSWebView extends Activity {

    // 내부 webview를 사용하고 싶을 때 쓴다.
    private WebView mWebview = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sns_webview);

        String URL = null;
        Intent intent = getIntent();
        URL = intent.getStringExtra("URL");

        mWebview = (WebView)findViewById(R.id.webView);

        mWebview.setWebViewClient(new SnsWebViewClient());
        mWebview.clearCache(true);
        mWebview.loadUrl(URL);
    }

    /**
     * sns용 웹뷰 처리
     * 
     * @author maro
     */
    private class SnsWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (view == null || url == null) {
                return false;
            }

            // 트위터 연동 성공
            if (url.startsWith(SnsConfig.TWITTER_CALLBACK_URL)) {
                Intent intent = new Intent();
                intent.putExtra("URL", url);
                setResult(RESULT_OK, intent);

                finish();
            } else {
                view.loadUrl(url);
            }
            return true;
        }

        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            int keyCode = event.getKeyCode();
            if ((keyCode == KeyEvent.KEYCODE_DPAD_LEFT) && view.canGoBack()) {
                view.goBack();
                return true;
            } else if ((keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) && view.canGoForward()) {
                view.goForward();
                return true;
            }
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }
    }
}
