package com.socialwatch.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Locale;

public class MainActivity extends Activity {

    private WebView webView;

    private String defaultUserAgent;
    private boolean defaultWideViewPort;
    private boolean defaultLoadWithOverviewMode;

    private static final String DESKTOP_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/140.0.0.0 Safari/537.36";

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

    private boolean isFacebookOrTikTok(String url) {
        if (url == null) return false;

        String u = url.toLowerCase(Locale.US);

        return u.startsWith("https://www.facebook.com/")
                || u.startsWith("https://m.facebook.com/")
                || u.startsWith("https://facebook.com/")
                || u.startsWith("https://www.tiktok.com/")
                || u.startsWith("https://m.tiktok.com/")
                || u.startsWith("https://tiktok.com/");
    }

    private void applySettingsForUrl(String url) {
        if (webView == null || defaultUserAgent == null) return;

        WebSettings settings = webView.getSettings();

        if (isFacebookOrTikTok(url)) {

            // Facebook + TikTok:
            // Desktop browser User-Agent.
            settings.setUserAgentString(DESKTOP_USER_AGENT);

            // Keep desktop layout from being automatically
            // reduced to a very small frame.
            settings.setUseWideViewPort(false);
            settings.setLoadWithOverviewMode(false);

        } else {

            // Original/default Android WebView behavior.
            // YouTube remains unchanged.
            settings.setUserAgentString(defaultUserAgent);
            settings.setUseWideViewPort(defaultWideViewPort);
            settings.setLoadWithOverviewMode(defaultLoadWithOverviewMode);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);

        WebSettings s = webView.getSettings();

        // Save original WebView settings.
        defaultUserAgent = s.getUserAgentString();
        defaultWideViewPort = s.getUseWideViewPort();
        defaultLoadWithOverviewMode = s.getLoadWithOverviewMode();

        // Existing SOCIAL WATCH settings — unchanged.
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setSupportMultipleWindows(false);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request) {

                String url = request.getUrl().toString();

                if (!isAllowed(url)) {
                    return true;
                }

                applySettingsForUrl(url);
                view.loadUrl(url);
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    String url) {

                if (!isAllowed(url)) {
                    return true;
                }

                applySettingsForUrl(url);
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(
                    WebView view,
                    String url,
                    Bitmap favicon) {

                if (!isAllowed(url)) {

                    applySettingsForUrl(
                            "file:///android_asset/index.html"
                    );

                    view.loadUrl(
                            "file:///android_asset/index.html"
                    );

                    return;
                }

                applySettingsForUrl(url);
            }
        });

        // Start with original/default settings.
        applySettingsForUrl(
                "file:///android_asset/index.html"
        );

        webView.loadUrl(
                "file:///android_asset/index.html"
        );
    }

    @Override
    public void onBackPressed() {

        // Return to SOCIAL WATCH home.
        applySettingsForUrl(
                "file:///android_asset/index.html"
        );

        webView.loadUrl(
                "file:///android_asset/index.html"
        );
    }

    @Override
    protected void onDestroy() {

        if (webView != null) {
            webView.destroy();
        }

        super.onDestroy();
    }
}
