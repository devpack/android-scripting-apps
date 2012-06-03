package com.android.scripting.apps;

import com.android.scripting.apps.config.Config;

import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.util.Log;

public class Webview extends Activity {

	private Activity that;
    private WebView mWebView;
	private RelativeLayout  container;
	private PowerManager.WakeLock mWakeLock;
    private boolean mActivityInPause = true;
    private static final int RELEASE_WAKELOCK = 108;
    private final static int WAKELOCK_TIMEOUT = 5 * 60 * 1000;
    
    Config config = Config.getConfigSingleton();

    @Override
    public void onCreate(Bundle icicle) {
    	super.onCreate(icicle);
    	that = this;
    	
    	requestWindowFeature(Window.FEATURE_NO_TITLE); 
    	this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                
		container = new RelativeLayout (this);
		RelativeLayout.LayoutParams topLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		container.setLayoutParams( topLayoutParams );
		setContentView(container);
		
        mWebView = new WebView(this);
        RelativeLayout.LayoutParams webviewLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		mWebView.setLayoutParams( webviewLayoutParams );

		container.addView(mWebView);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState ){
        mWebView.saveState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration  newConfig) {
      super.onConfigurationChanged(newConfig);
      
	  Log.d(config.getLOG_TAG(), "Launch Webview: " + "file://" + this.getFilesDir().getAbsolutePath() + "/" + config.getMAIN_SCRIPT_NAME());
      mWebView.loadUrl("file://" + this.getFilesDir().getAbsolutePath() + "/" + config.getMAIN_SCRIPT_NAME());
    }
    
    // ------------------------------------------------------------------------------------------------------

    @Override
    protected void onStart() {
    	super.onStart();
    	
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, config.getLOG_TAG());	
        mWebView.setKeepScreenOn(true);
        
        mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        mWebView.setScrollbarFadingEnabled(false); 
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setScrollContainer(false);
        
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setPluginsEnabled (true);
        
        webSettings.setSupportZoom(false);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        
        if (Build.VERSION.SDK_INT >= 5 ) {
            try {
              Method m1 = WebSettings.class.getMethod("setDomStorageEnabled", new Class[]{Boolean.TYPE});
              m1.invoke(webSettings, Boolean.TRUE);
              
              Method m2 = WebSettings.class.getMethod("setDatabaseEnabled", new Class[]{Boolean.TYPE});
              m2.invoke(webSettings, Boolean.TRUE);
              
              Method m3 = WebSettings.class.getMethod("setDatabasePath", new Class[]{String.class});
              m3.invoke(webSettings, this.getFilesDir().getParent() + "/databases/");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
          }	        
        
        mWebView.setWebChromeClient(new WebChromeClient() {
        });
        
        mWebView.setWebViewClient(new WebViewClient() {
//        	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//        		Toast.makeText(that, "Error: " + description, Toast.LENGTH_SHORT).show();
//        	}
        });
        
        webSettings.setCacheMode (WebSettings.LOAD_NORMAL);
        
        if (Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT < 14) {
            mWebView.setSystemUiVisibility(WebView.STATUS_BAR_HIDDEN);
        }
        else if (Build.VERSION.SDK_INT >= 14) {
        	mWebView.setSystemUiVisibility(WebView.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
        
		Log.d(config.getLOG_TAG(), "Launch Webview: " + "file://" + this.getFilesDir().getAbsolutePath() + "/" + config.getMAIN_SCRIPT_NAME());
        mWebView.loadUrl("file://" + this.getFilesDir().getAbsolutePath() + "/" +  config.getMAIN_SCRIPT_NAME());
    }
 
    // ------------------------------------------------------------------------------------------------------

	private Handler wakelockHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RELEASE_WAKELOCK:
                    if (mWakeLock.isHeld()) {
                        mWakeLock.release();
                    }
                    break;
            }
        }
    };

    // ------------------------------------------------------------------------------------------------------

    @Override
    protected void onStop() {
        super.onStop();
        mWebView.destroy();
        finish();
    }

    // ------------------------------------------------------------------------------------------------------
    
    @Override 
    protected void onPause() {
        super.onPause();
                
        if (mActivityInPause) {
            return;
        }
        mActivityInPause = true;
        
        if (!pauseWebView()) {
            mWakeLock.acquire();
            wakelockHandler.sendMessageDelayed(wakelockHandler.obtainMessage(RELEASE_WAKELOCK), WAKELOCK_TIMEOUT);
        }
	}
    
    // ------------------------------------------------------------------------------------------------------

    private boolean pauseWebView() {
        if (mActivityInPause) {
            if (mWebView != null) {
                try {
                    Method method = WebView.class.getMethod("onPause");
                    method.invoke(mWebView);
                } catch (final Exception e) {
                }
        		mWebView.pauseTimers();
            }
            return true;
        } else {
            return false;
        }
    }

    // ------------------------------------------------------------------------------------------------------

    private boolean resumeWebView() {
        if (!mActivityInPause) {
            if (mWebView != null) {
                try {
                    Method method = WebView.class.getMethod("onResume");
                    method.invoke(mWebView);
                } catch (final Exception e) {
                }
            	mWebView.resumeTimers();
            }
            return true;
        } else {
            return false;
        }
    }

    // ------------------------------------------------------------------------------------------------------

    @Override 
    protected void onResume() {
        super.onResume();

        if (!mActivityInPause) {
            return;
        }

        mActivityInPause = false;
        resumeWebView();

        if (mWakeLock.isHeld()) {
        	wakelockHandler.removeMessages(RELEASE_WAKELOCK);
            mWakeLock.release();
        }
    }


}