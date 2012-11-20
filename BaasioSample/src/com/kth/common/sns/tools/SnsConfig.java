package com.kth.common.sns.tools;

import com.kth.baasio.baassample.BuildConfig;

public class SnsConfig {
    // Facebook App ID
    public static final String FACEBOOK_APPID = BuildConfig.DEBUG ? "FACEBOOK_APPID_DEBUG"
            : "FACEBOOK_APPID"; // BaasSample

    // public static final String FACEBOOK_APPID = "FACEBOOK_APPID";
    // //BaasSample_KTH

    // Facebook Login 시 request Permissions, Permissions의 속성에 대해서는 Facebook
    // developer site를 참조바란다.
    public static final String[] FACEBOOK_PERMISSIONS = new String[] {
            "publish_stream", "read_stream", "offline_access", "email"
    };

    // Twitter Consumer, ConsumerSecret Key
    public static final String TWITTER_CONSUMER_KEY = "TWITTER_CONSUMER_KEY";

    public static final String TWITTER_CONSUMER_SECRET = "TWITTER_CONSUMER_SECRET";

    public static final String TWITTER_CALLBACK_URL = "http://callback.url";
}
