package com.asap;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.googlecode.android_scripting.AndroidProxy;
import com.googlecode.android_scripting.jsonrpc.RpcReceiverManager;

import java.util.concurrent.CountDownLatch;

public class RpcService extends Service {
	private final CountDownLatch mLatch = new CountDownLatch(1);
	private IBinder mBinder;
	
	private RpcReceiverManager mFacadeManager;
    public static AndroidProxy mProxy;
    
    // ------------------------------------------------------------------------------------------------------

	public class LocalBinder extends Binder {
		public RpcService getService() {
			return RpcService.this;
		}
	}

    // ------------------------------------------------------------------------------------------------------

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

    // ------------------------------------------------------------------------------------------------------

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

    // ------------------------------------------------------------------------------------------------------

	@Override
	public void onCreate() {
		super.onCreate();
		mBinder = new LocalBinder();
	}

    // ------------------------------------------------------------------------------------------------------

	@Override
	public void onStart(Intent intent, final int startId) {
		super.onStart(intent, startId);
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
		mProxy = new AndroidProxy(this, null, true);
		mProxy.startLocal();
		mLatch.countDown();
		
		Intent intent = new Intent(getBaseContext(), Webview.class);
		intent.setAction(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplication().startActivity(intent);
	}
	
    // ------------------------------------------------------------------------------------------------------

	RpcReceiverManager getRpcReceiverManager() throws InterruptedException {
		mLatch.await();

		if (mFacadeManager==null) { // Facade manage may not be available on startup.
		mFacadeManager = mProxy.getRpcReceiverManagerFactory()
		.getRpcReceiverManagers().get(0);
		}
		return mFacadeManager;
	}

}