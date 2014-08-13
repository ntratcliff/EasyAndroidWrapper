package com.test.testwrapper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.GeolocationPermissions;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

    static String BASE_URL = "http://google.com"; //URL of your webapp

    static boolean ALLOW_JS = true; //Leave true unless your webapp does not use JavaScript
    static boolean ALLOW_JS_RESOURCE_LOADING = true; //Allows services like AJAX to be called from JS. Leave true unless you have security qualms.

    static boolean ALLOW_GEOLOCATION = true; //Allows geolocation services
    static boolean REQUEST_GEOLOCATION_PERMISSION = true; //Ask the user for use of geolocation.
    static boolean USER_ALLOW_GEOLOCATION = !REQUEST_GEOLOCATION_PERMISSION; //Will be set true if the user allows geolocation, or if REQUEST_GEOLOCATION_PERMISSION is false
    static boolean RETAIN_GEOLOCATION_PERMISSION = false; //Retains permission of use of location.

    static boolean SAVE_FORM_DATA = true; //Allows the webview to save form data

    WebView mWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = (WebView) findViewById(R.id.browser);

        WebSettings settings = mWebView.getSettings();

        settings.setJavaScriptEnabled(ALLOW_JS);
        settings.setGeolocationEnabled(ALLOW_GEOLOCATION);
        settings.setSaveFormData(SAVE_FORM_DATA);

        if (Build.VERSION.SDK_INT >= 16) {
            settings.setAllowUniversalAccessFromFileURLs(ALLOW_JS_RESOURCE_LOADING);
            settings.setAllowFileAccessFromFileURLs(ALLOW_JS_RESOURCE_LOADING);
        }

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                requestGeolocationPermission(origin, callback);
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (!isNetworkAvailable()) {
                    view.stopLoading();
                    alertNoNetwork();
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                alertLoadFailed(description);
            }
        });
        mWebView.loadUrl(BASE_URL);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) { //Allows using the back button to navigate back
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (mWebView.canGoBack()) {
                        mWebView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean isNetworkAvailable() { //Checks if the device has a network connection
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void alertNoNetwork() { //Alerts the user that there is no network connection
        new AlertDialog.Builder(this)
                .setTitle("No Network Connection")
                .setMessage("This application requires a connection to the internet, please connect to the internet and try again.")
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish(); //Exit the app
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void alertLoadFailed(String description) { //Alerts the user that there was an error loading the page
        new AlertDialog.Builder(this)
                .setTitle("Page Load Failed")
                .setMessage(description)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish(); //Exit the app
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void requestGeolocationPermission(final String origin, final GeolocationPermissions.Callback callback) { //Prompts the user to allow/deny geolocation
        if (REQUEST_GEOLOCATION_PERMISSION && !USER_ALLOW_GEOLOCATION) {
            new AlertDialog.Builder(this)
                    .setTitle("Location Requested")
                    .setMessage("This app has requested to use your location.")
                    .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            MainActivity.USER_ALLOW_GEOLOCATION = true;
                            callback.invoke(origin, USER_ALLOW_GEOLOCATION, RETAIN_GEOLOCATION_PERMISSION);
                        }
                    })
                    .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            MainActivity.USER_ALLOW_GEOLOCATION = false;
                            callback.invoke(origin, USER_ALLOW_GEOLOCATION, RETAIN_GEOLOCATION_PERMISSION);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_map)
                    .show();
        } else
            callback.invoke(origin, USER_ALLOW_GEOLOCATION, RETAIN_GEOLOCATION_PERMISSION);
    }
}