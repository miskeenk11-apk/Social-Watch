package com.socialwatch.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Locale;

public class MainActivity extends Activity {
    private WebView webView;

    private boolean isAllowed(String url) {
        if (url == null) return false;
        String u = url.toLowerCase(Locale.US);
        return u.startsWith("https://www.youtube.com/")
                || u.startsWith("https://m.youtube.com/")
                || u.startsWith("https://youtube.com/")
                || u.startsWith("https://www.facebook.com/")
                || u.startsWith("https://m.facebook.com/")
                || u.startsWith("https://facebook.com/")
                || u.startsWith("https://www.tiktok.com/")
                || u.startsWith("https://m.tiktok.com/")
                || u.startsWith("https://tiktok.com/")
                || u.startsWith("file:///");
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webview);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setSupportMultipleWindows(false);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return !isAllowed(request.getUrl().toString());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return !isAllowed(url);
            }
            @Override public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (!isAllowed(url)) view.loadUrl("file:///android_asset/index.html");
            }
        });

        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override public void onBackPressed() {
        // SOCIAL WATCH is intentionally not a general browser: Back returns home.
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override protected void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
