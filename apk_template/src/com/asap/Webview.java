package com.asap;

import com.asap.config.Config;
import com.googlecode.android_scripting.AndroidProxy;
import com.googlecode.android_scripting.FileUtils;
import com.googlecode.android_scripting.rpc.MethodDescriptor;
import com.googlecode.android_scripting.rpc.RpcError;
import com.googlecode.android_scripting.event.Event;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.net.Uri;
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
import com.googlecode.android_scripting.AndroidProxy;

import com.googlecode.android_scripting.jsonrpc.JsonBuilder;
import com.googlecode.android_scripting.jsonrpc.JsonRpcResult;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.jsonrpc.RpcReceiverManager;
import com.googlecode.android_scripting.facade.EventFacade;
import android.os.IBinder;

public class Webview extends Activity {

	private static final String HTTP = "http";
	private String jsonSrc;
    private String androidJsSrc;
    private String mAPIWrapperSource;
    private static final String ANDROID_PROTOTYPE_JS =
    	      "Android.prototype.%1$s = function(var_args) { "
    	          + "return this._call(\"%1$s\", Array.prototype.slice.call(arguments)); };";
    private RpcReceiverManager mFacadeManager;
    private HtmlEventObserver mObserver;
    private JavaScriptWrapper mWrapper;
	private final CountDownLatch mLatch = new CountDownLatch(1);
    private AndroidProxy mProxy;

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
    
    private String generateAPIWrapper() {
        StringBuilder wrapper = new StringBuilder();
        for (Class<? extends RpcReceiver> clazz : mFacadeManager.getRpcReceiverClasses()) {
          for (MethodDescriptor rpc : MethodDescriptor.collectFrom(clazz)) {
            wrapper.append(String.format(ANDROID_PROTOTYPE_JS, rpc.getName()));
          }
        }
        return wrapper.toString();
      }
    
    private class JavaScriptWrapper {
        @SuppressWarnings("unused")
        public String call(String data) throws JSONException {
          Log.v(config.getLOG_TAG(), "Received: " + data);
          JSONObject request = new JSONObject(data);
          int id = request.getInt("id");
          String method = request.getString("method");
          JSONArray params = request.getJSONArray("params");
          MethodDescriptor rpc = mFacadeManager.getMethodDescriptor(method);
          if (rpc == null) {
            return JsonRpcResult.error(id, new RpcError("Unknown RPC.")).toString();
          }
          try {
            return JsonRpcResult.result(id, rpc.invoke(mFacadeManager, params)).toString();
          } catch (Throwable t) {
            Log.e(config.getLOG_TAG(), "Invocation error.", t);
            return JsonRpcResult.error(id, t).toString();
          }
        }

        @SuppressWarnings("unused")
        public void dismiss() {
          Activity parent = that;
          parent.finish();
        }
      }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	
        try {
			jsonSrc = FileUtils.readFromAssetsFile(that, "json2.js");
	        androidJsSrc = FileUtils.readFromAssetsFile(that, "android.js");
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
        
        mObserver = new HtmlEventObserver();

        // TODO
        //RpcReceiverManager mFacadeManager = getRpcReceiverManager();
        //mFacadeManager.getReceiver(EventFacade.class).addGlobalEventObserver(mObserver);
        //mAPIWrapperSource = generateAPIWrapper();
        
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
        
        mWebView.addJavascriptInterface(mWrapper, "_rpc_wrapper");
        mWebView.addJavascriptInterface(new Object() {

          @SuppressWarnings("unused")
          public void register(String event, int id) {
            mObserver.register(event, id);
          }
        }, "_callback_wrapper");
        
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
        
        mWebView.loadUrl("javascript:" + jsonSrc);
        mWebView.loadUrl("javascript:" + androidJsSrc);
        mWebView.loadUrl("javascript:" + mAPIWrapperSource);
		Log.d(config.getLOG_TAG(), "Launch Webview: " + "file://" + this.getFilesDir().getAbsolutePath() + "/" + config.getMAIN_SCRIPT_NAME());
        mWebView.loadUrl("file://" + this.getFilesDir().getAbsolutePath() + "/" +  config.getMAIN_SCRIPT_NAME());
    }
 
    // ------------------------------------------------------------------------------------------------------

    private class HtmlEventObserver implements EventFacade.EventObserver {
        private Map<String, Set<Integer>> mEventMap = new HashMap<String, Set<Integer>>();

        public void register(String eventName, Integer id) {
          if (mEventMap.containsKey(eventName)) {
            mEventMap.get(eventName).add(id);
          } else {
            Set<Integer> idSet = new HashSet<Integer>();
            idSet.add(id);
            mEventMap.put(eventName, idSet);
          }
        }

        @Override
        public void onEventReceived(Event event) {
          final JSONObject json = new JSONObject();
          try {
            json.put("data", JsonBuilder.build(event.getData()));
          } catch (JSONException e) {
            e.printStackTrace();
          }
          if (mEventMap.containsKey(event.getName())) {
            for (final Integer id : mEventMap.get(event.getName())) {
             that.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  mWebView.loadUrl(String.format("javascript:droid._callback(%d, %s);", id, json));
                }
              });
            }
          }
        }

        @SuppressWarnings("unused")
        public void dismiss() {
          Activity parent = that;
          parent.finish();
        }
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
        mFacadeManager.getReceiver(EventFacade.class).removeEventObserver(mObserver);
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