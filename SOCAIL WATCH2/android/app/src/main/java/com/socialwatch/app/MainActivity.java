package com.socialwatch.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;

import java.util.Locale;

public class MainActivity extends Activity {

    private WebView webView;
    private Button backButton;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    private String defaultUserAgent;
    private boolean defaultWideViewPort;
    private boolean defaultLoadWithOverviewMode;

    private static final String HOME_URL = "file:///android_asset/index.html";

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

    private boolean isTikTok(String url) {
        if (url == null) return false;

        String u = url.toLowerCase(Locale.US);

        return u.startsWith("https://www.tiktok.com/")
                || u.startsWith("https://m.tiktok.com/")
                || u.startsWith("https://tiktok.com/");
    }

    private boolean isHome(String url) {
        return url != null && url.startsWith("file:///");
    }

    private void applySettingsForUrl(String url) {
        if (webView == null || defaultUserAgent == null) return;

        WebSettings settings = webView.getSettings();

        if (isTikTok(url)) {
            // TikTok ONLY keeps the newer desktop-browser method.
            settings.setUserAgentString(DESKTOP_USER_AGENT);
            settings.setUseWideViewPort(false);
            settings.setLoadWithOverviewMode(false);
        } else {
            // YouTube + Facebook + SOCIAL WATCH home use original/default WebView settings.
            settings.setUserAgentString(defaultUserAgent);
            settings.setUseWideViewPort(defaultWideViewPort);
            settings.setLoadWithOverviewMode(defaultLoadWithOverviewMode);
        }
    }

    private void updateBackButtonVisibility(String url) {
        if (backButton == null) return;
        backButton.setVisibility(isHome(url) ? View.GONE : View.VISIBLE);
    }

    private void goBackStepByStep() {
        if (webView == null) return;

        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            webView.loadUrl(HOME_URL);
        }
    }

    private boolean hasInternetConnection() {
        if (connectivityManager == null) {
            connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        }

        if (connectivityManager == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(network);
            if (caps == null) return false;

            boolean transport = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);

            boolean validated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            return transport && validated;
        }

        android.net.NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    private void sendNetworkStatusToPage() {
        if (webView == null) return;

        final boolean available = hasInternetConnection();
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.evaluateJavascript(
                        "if(window.SOCIAL_WATCH&&window.SOCIAL_WATCH.setNetworkStatus){window.SOCIAL_WATCH.setNetworkStatus(" + available + ");}",
                        null
                );
            }
        });
    }

    private void registerNetworkCallback() {
        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return;

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                sendNetworkStatusToPage();
            }

            @Override
            public void onLost(Network network) {
                sendNetworkStatusToPage();
            }

            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                sendNetworkStatusToPage();
            }
        };

        connectivityManager.registerDefaultNetworkCallback(networkCallback);
    }

    private void unregisterNetworkCallback() {
        if (connectivityManager == null || networkCallback == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return;

        try {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        } catch (Exception ignored) {
        }

        networkCallback = null;
    }

    private void addBackButtonOverlay() {
        backButton = new Button(this);
        backButton.setText("‹");
        backButton.setTextSize(32);
        backButton.setTextColor(Color.WHITE);
        backButton.setBackgroundColor(Color.rgb(91, 185, 238));
        backButton.setGravity(Gravity.CENTER);
        backButton.setAllCaps(false);
        backButton.setContentDescription("Back");
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBackStepByStep();
            }
        });

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dp(44), dp(44));
        params.gravity = Gravity.TOP | Gravity.START;
        params.leftMargin = dp(8);
        params.topMargin = dp(8);

        addContentView(backButton, params);
        backButton.setVisibility(View.GONE);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);

        WebSettings s = webView.getSettings();

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

        addBackButtonOverlay();
        registerNetworkCallback();

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();

                if (!isAllowed(url)) return true;

                applySettingsForUrl(url);
                updateBackButtonVisibility(url);
                view.loadUrl(url);
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!isAllowed(url)) return true;

                applySettingsForUrl(url);
                updateBackButtonVisibility(url);
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (!isAllowed(url)) {
                    applySettingsForUrl(HOME_URL);
                    updateBackButtonVisibility(HOME_URL);
                    view.loadUrl(HOME_URL);
                    return;
                }

                applySettingsForUrl(url);
                updateBackButtonVisibility(url);
                sendNetworkStatusToPage();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                updateBackButtonVisibility(url);
                sendNetworkStatusToPage();
            }
        });

        applySettingsForUrl(HOME_URL);
        updateBackButtonVisibility(HOME_URL);
        webView.loadUrl(HOME_URL);
    }

    @Override
    public void onBackPressed() {
        goBackStepByStep();
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkCallback();

        if (webView != null) {
            webView.destroy();
        }

        super.onDestroy();
    }
}
