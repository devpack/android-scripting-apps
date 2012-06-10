package com.asap;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;

import com.asap.config.Config;
import com.asap.process.MyScriptProcess;
import com.googlecode.android_scripting.AndroidProxy;
import com.googlecode.android_scripting.jsonrpc.RpcReceiverManager;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class BackgroundScriptService extends Service {
	Config config = Config.getConfigSingleton();

	private final CountDownLatch mLatch = new CountDownLatch(1);
	private IBinder mBinder;
	private MyScriptProcess myScriptProcess;
	
	private static BackgroundScriptService instance;
	private boolean killMe;
	  
	private InterpreterConfiguration mInterpreterConfiguration = null;
	private RpcReceiverManager mFacadeManager;
    private AndroidProxy mProxy;
    
    private static Context context = null;
    static {
      instance = null;
    }
    
    // ------------------------------------------------------------------------------------------------------

	public class LocalBinder extends Binder {
		public BackgroundScriptService getService() {
			return BackgroundScriptService.this;
		}
	}

    // ------------------------------------------------------------------------------------------------------

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

    // ------------------------------------------------------------------------------------------------------

//	public BackgroundScriptService() {
//		mBinder = new LocalBinder();
//	}

    // ------------------------------------------------------------------------------------------------------

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

    // ------------------------------------------------------------------------------------------------------

    public static Context getAppContext() {
        return BackgroundScriptService.context;
    }
    
    // ------------------------------------------------------------------------------------------------------

	@Override
	public void onCreate() {
		super.onCreate();
		BackgroundScriptService.context = getApplicationContext();
		mBinder = new LocalBinder();
	}

    // ------------------------------------------------------------------------------------------------------

	private void killProcess() {
		this.killMe = true;
	    instance = null;
	    if (myScriptProcess != null) {
	    	myScriptProcess.kill();
	    }
	}

    // ------------------------------------------------------------------------------------------------------
	
	@Override
	public void onStart(Intent intent, final int startId) {
		super.onStart(intent, startId);
		
		killProcess();
		
		instance = this;
	    this.killMe = false;

		new startMyAsyncTask().execute(startId);
	}

    // ------------------------------------------------------------------------------------------------------

	  public class startMyAsyncTask extends AsyncTask<Integer, Integer, Boolean> {
		   @Override
		   protected void onPreExecute() {
		   }
	
		   @Override
		   protected Boolean doInBackground(Integer... params) {	    
			   startMyMain(params[0]);
			   
			   // TODO
			   return true;
		   }
	
		   @Override
		   protected void onProgressUpdate(Integer... values) {
		   }
	
		   @Override
		   protected void onPostExecute(Boolean installStatus) {
		   }
	   
	  }

	// ------------------------------------------------------------------------------------------------------

	private void startMyMain(final int startId) {

		String scriptName = config.getMAIN_SCRIPT_NAME();
		scriptName = this.getFilesDir().getAbsolutePath() + "/" + scriptName;
		File script = new File(scriptName);
		
		// arguments
		ArrayList<String> args = new ArrayList<String>();
		args.add(scriptName);
		for(String sarg : config.getSCRIPT_ARGS()) {
			args.add(sarg);
		}

		File interpreterBinary = new File(config.getINTERPRETER_BIN_PATH());
		
		// launch script
		mProxy = new AndroidProxy(this, null, true);
		mProxy.startLocal();
		mLatch.countDown();
	      
		myScriptProcess = MyScriptProcess.launchScript(script, mInterpreterConfiguration, mProxy, new Runnable() {
					@Override
					public void run() {
						//mProxy.shutdown();
						//stopSelf(startId);
						
						// hard force restart
//				        if (!ScriptService.this.killMe) {
//				        	startMyMain();				        	
//				        }

					}
				}, script.getParent(),  Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + this.getPackageName(), args, config.getENV_VARS(), interpreterBinary);		
	}
	
    // ------------------------------------------------------------------------------------------------------

	public RpcReceiverManager getRpcReceiverManager() throws InterruptedException {
		mLatch.await();
		
		if (mFacadeManager==null) { // Facade manage may not be available on startup.
		mFacadeManager = mProxy.getRpcReceiverManagerFactory()
		.getRpcReceiverManagers().get(0);
		}
		return mFacadeManager;
	}

    // ------------------------------------------------------------------------------------------------------
	
}