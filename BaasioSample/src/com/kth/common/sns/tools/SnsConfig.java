
package com.kth.common.sns.tools;

import com.kth.baasio.baassample.BuildConfig;

public class SnsConfig {
    // Facebook App ID
    // 510355658975579 : BaasSample_KTH(kth-store), 475255092514703 :
    // BaasSample(DEBUG)
    public static final String FACEBOOK_APPID = BuildConfig.DEBUG ? "510355658975579"
            : "475255092514703"; // BaasSample

    // public static final String FACEBOOK_APPID = "475255092514703";
    // //BaasSample_KTH

    // Facebook Login 시 request Permissions, Permissions의 속성에 대해서는 Facebook
    // developer site를 참조바란다.
    public static final String[] FACEBOOK_PERMISSIONS = new String[] {
            "publish_stream", "read_stream", "offline_access", "email"
    };

    // Twitter Consumer, ConsumerSecret Key
    public static final String TWITTER_CONSUMER_KEY = "S4Z3E7kzojfk2fBiyM1IMA";

    public static final String TWITTER_CONSUMER_SECRET = "wm1HDjYUjpyI3HKjJ88AbKi0oElDjTBX1aESn9I4";

    public static final String TWITTER_CALLBACK_URL = "http://testbrad.com";
}
