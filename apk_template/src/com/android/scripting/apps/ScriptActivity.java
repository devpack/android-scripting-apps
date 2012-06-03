package com.android.scripting.apps;

import com.android.scripting.apps.config.Config;
import com.android.scripting.apps.support.Utils;
import com.googlecode.android_scripting.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import android.util.Log;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ScriptActivity extends Activity {
	Config config = Config.getConfigSingleton();

	Button buttonInstall;
	ProgressDialog myProgressDialog; 
	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// mounted sdcard ?
		if (!Environment.getExternalStorageState().equals("mounted")) {
		  Log.e(config.getLOG_TAG(), "External storage is not mounted");
		  
		  Toast toast = Toast.makeText( getApplicationContext(), "External storage not mounted", Toast.LENGTH_LONG);
		  toast.show();
		  return;
		}
	  
		ProjectDescriptionIniFile();
		
		// install needed ?
    	boolean installNeeded = isInstallNeeded();
		
    	if(installNeeded) {
    		setContentView(R.layout.install);
    	     
    		buttonInstall = (Button)findViewById(R.id.startinstall);
    		buttonInstall.setOnClickListener(new Button.OnClickListener(){

    		@Override
    		public void onClick(View v) {
    		  new InstallAsyncTask().execute();
    		  buttonInstall.setClickable(false);
    		}});
    	}
    	else {
    		openView();
    	    finish();
    	}

		//onStart();
  }

    // ------------------------------------------------------------------------------------------------------

	  private void ProjectDescriptionIniFile() {
		    try {
		    		InputStream projectDescriptionIni = getResources().openRawResource(R.raw.project_description);
			    
		            Properties pro = new Properties();
		            pro.load(projectDescriptionIni);
		   
		            System.out.println("LOG_TAG=" + pro.getProperty("LOG_TAG"));
		            System.out.println("MAIN_SCRIPT_NAME=" + pro.getProperty("MAIN_SCRIPT_NAME"));
		            System.out.println("INTERPRETER_ZIP_NAME=" + pro.getProperty("INTERPRETER_ZIP_NAME"));
		            System.out.println("INTERPRETER_EXTRAS_ZIP_NAME=" + pro.getProperty("INTERPRETER_EXTRAS_ZIP_NAME"));
		            System.out.println("INTERPRETER_BIN_PATH=" + pro.getProperty("INTERPRETER_BIN_PATH"));
		            System.out.println("INTERPRETER_NAME=" + pro.getProperty("INTERPRETER_NAME"));
		            System.out.println("INTERPRETER_NICE_NAME=" + pro.getProperty("INTERPRETER_NICE_NAME"));
		            System.out.println("ENV_VARS=" + pro.getProperty("ENV_VARS"));
		            System.out.println("SCRIPT_ARGS=" + pro.getProperty("SCRIPT_ARGS"));

			        // LOG_TAG
			        if(pro.getProperty("LOG_TAG") != null) {
			         try {
			           String s = pro.getProperty("LOG_TAG");
			           config.setLOG_TAG(s);
					 } catch (Exception e) {
					   System.err.println("Fail to set LOG_TAG, error: " + e);
					 }
			        }
			        
			        // MAIN_SCRIPT_NAME
			        if(pro.getProperty("MAIN_SCRIPT_NAME") != null) {
			         try {
			           String s = pro.getProperty("MAIN_SCRIPT_NAME");
			           config.setMAIN_SCRIPT_NAME(s);
					 } catch (Exception e) {
					   System.err.println("Fail to set MAIN_SCRIPT_NAME, error: " + e);
					 }
			        }
			        
			        // INTERPRETER_ZIP_NAME
			        if(pro.getProperty("INTERPRETER_ZIP_NAME") != null) {
			         try {
			           String s = pro.getProperty("INTERPRETER_ZIP_NAME");
			           config.setINTERPRETER_ZIP_NAME(s);
					 } catch (Exception e) {
					   System.err.println("Fail to set INTERPRETER_ZIP_NAME, error: " + e);
					 }
			        }
			        
			        // INTERPRETER_EXTRAS_ZIP_NAME
			        if(pro.getProperty("INTERPRETER_EXTRAS_ZIP_NAME") != null) {
			         try {
			           String s = pro.getProperty("INTERPRETER_EXTRAS_ZIP_NAME");
			           config.setINTERPRETER_EXTRAS_ZIP_NAME(s);
					 } catch (Exception e) {
					   System.err.println("Fail to set INTERPRETER_EXTRAS_ZIP_NAME, error: " + e);
					 }
			        }
			        
			        // INTERPRETER_BIN_PATH
			        if(pro.getProperty("INTERPRETER_BIN_PATH") != null) {
			         try {
			        	   String extStorage = Environment.getExternalStorageDirectory().getAbsolutePath();
			        	   String fileDir = this.getFilesDir().getAbsolutePath();
			        	   String packageName = this.getPackageName();
			        	   
			        	   String s = pro.getProperty("INTERPRETER_BIN_PATH");
			        	   config.setINTERPRETER_BIN_PATH(s.replaceAll("getExternalStorageDirectory", extStorage).replaceAll("getFilesDir", fileDir).replaceAll("getPackageName", packageName));
					 } catch (Exception e) {
					   System.err.println("Fail to set INTERPRETER_BIN_PATH, error: " + e);
					 }
			        }
			        
			        // INTERPRETER_NAME
			        if(pro.getProperty("INTERPRETER_NAME") != null) {
			         try {
			           String s = pro.getProperty("INTERPRETER_NAME");
			           config.setINTERPRETER_NAME(s);
					 } catch (Exception e) {
					   System.err.println("Fail to set INTERPRETER_NAME, error: " + e);
					 }
			        }
			        
			        // INTERPRETER_NICE_NAME
			        if(pro.getProperty("INTERPRETER_NICE_NAME") != null) {
			         try {
			           String s = pro.getProperty("INTERPRETER_NICE_NAME");
			           config.setINTERPRETER_NICE_NAME(s);
					 } catch (Exception e) {
					   System.err.println("Fail to set INTERPRETER_NICE_NAME, error: " + e);
					 }
			        }   

			        // ENV_VARS
			        if(pro.getProperty("ENV_VARS") != null) {
			         try {
			           String s = pro.getProperty("ENV_VARS");
			           HashMap<String, String> env = new HashMap<String, String>();
			           
			           String[] sub = s.split(",");
			           for(String ss : sub) {
			        	   String[] ssub = ss.split("=");
			        	   String var = ssub[0];
			        	   String extStorage = Environment.getExternalStorageDirectory().getAbsolutePath();
			        	   String fileDir = this.getFilesDir().getAbsolutePath();
			        	   String packageName = this.getPackageName();
			        	   String val = ssub[1].replaceAll("getExternalStorageDirectory", extStorage).replaceAll("getFilesDir", fileDir).replaceAll("getPackageName", packageName);
			        	   env.put(var, val);
			           }
			           
			           config.setENV_VARS(env);
					 } catch (Exception e) {
					   System.err.println("Fail to set ENV_VARS, error: " + e);
					 }
			        }   
			        
			        // SCRIPT_ARGS
			        if(pro.getProperty("SCRIPT_ARGS") != null) {
			         try {
			           String s = pro.getProperty("SCRIPT_ARGS");
			           
			           String[] sub = s.split(",");
			           String[] args = new String[sub.length];
			           
		        	   String extStorage = Environment.getExternalStorageDirectory().getAbsolutePath();
		        	   String packageName = this.getPackageName();
		        	   String fileDir = this.getFilesDir().getAbsolutePath();

			           for(int i=0; i<sub.length; i++) {
			        	   args[i]=sub[i].replaceAll("getExternalStorageDirectory", extStorage).replaceAll("getFilesDir", fileDir).replaceAll("getPackageName", packageName);
			           }
			           
			           config.setSCRIPT_ARGS(args);
			           
					 } catch (Exception e) {
					   System.err.println("Fail to set SCRIPT_ARGS, error: " + e);
					 }
			        } 
			        
			      }
		      catch(Exception e) {
		    	  e.printStackTrace();
		      }	
		  	}
	  
	private void sendmsg(String key, String value) {
	      Message message = installerHandler.obtainMessage();
	      Bundle bundle = new Bundle();
	      bundle.putString(key, value);
	      message.setData(bundle);
	      installerHandler.sendMessage(message);
	   }
	    
	   final Handler installerHandler = new Handler() {
	   @Override
	   public void handleMessage(Message message) {
		        Bundle bundle = message.getData();
		        
		        if (bundle.containsKey("showProgressDialog")) {
		 	       myProgressDialog = ProgressDialog.show(ScriptActivity.this, "Installing", "Loading", true); 
		        }
		        else if (bundle.containsKey("setMessageProgressDialog")) {
		        	if (myProgressDialog.isShowing()) {
			        	myProgressDialog.setMessage(bundle.getString("setMessageProgressDialog"));
		        	}
		        }
		        else if (bundle.containsKey("dismissProgressDialog")) {
		        	if (myProgressDialog.isShowing()) {
			        	myProgressDialog.dismiss();
		        	}
		        }
		        else if (bundle.containsKey("installSucceed")) {
		  		  Toast toast = Toast.makeText( getApplicationContext(), "Install Succeed", Toast.LENGTH_LONG);
				  toast.show();
		        }
		        else if (bundle.containsKey("installFailed")) {
			  		  Toast toast = Toast.makeText( getApplicationContext(), "Install Failed. Please check logs.", Toast.LENGTH_LONG);
					  toast.show();
			    }
	       }
	   };
	   
	  public class InstallAsyncTask extends AsyncTask<Void, Integer, Boolean> {
		   @Override
		   protected void onPreExecute() {
		   }
	
		   @Override
		   protected Boolean doInBackground(Void... params) {	    
	    	Log.i(config.getLOG_TAG(), "Installing...");

	    	// show progress dialog
	    	sendmsg("showProgressDialog", "");

	    	sendmsg("setMessageProgressDialog", "Please wait...");
	    	createOurExternalStorageRootDir();
	
			// Copy all resources
			copyResourcesToLocal();
	
			// TODO
		    return true;
		   }
	
		   @Override
		   protected void onProgressUpdate(Integer... values) {
		   }
	
		   @Override
		   protected void onPostExecute(Boolean installStatus) {
	    	sendmsg("dismissProgressDialog", "");
	    	
	    	if(installStatus) {
		    	sendmsg("installSucceed", "");
	    	}
	    	else {
		    	sendmsg("installFailed", "");
	    	}
	    	
	    	openView();
		    finish();
		   }
	   
	  }
	
	  
  private void openView() {
	  if(config.getMAIN_SCRIPT_NAME().endsWith("html")) {
			Intent intent = new Intent(getBaseContext(), Webview.class);
			intent.setAction(Intent.ACTION_VIEW);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getApplication().startActivity(intent);
	  }
	  else {
		  startService(new Intent(this, ScriptService.class));
	  }
  }
  
	private void createOurExternalStorageRootDir() {
		Utils.createDirectoryOnExternalStorage( this.getPackageName() );
	}
	
	// quick and dirty: only test a file
	private boolean isInstallNeeded() {
		File testedFile = new File(this.getFilesDir().getAbsolutePath()+ "/" + config.getMAIN_SCRIPT_NAME());
		if(!testedFile.exists()) {
			return true;
		}
		return false;
	}
	
	
	 private void copyResourcesToLocal() {
			String name, sFileName;
			InputStream content;
			
			R.raw a = new R.raw();
			java.lang.reflect.Field[] t = R.raw.class.getFields();
			Resources resources = getResources();
			
			boolean succeed = true;
			
			for (int i = 0; i < t.length; i++) {
				try {
					name = resources.getText(t[i].getInt(a)).toString();
					sFileName = name.substring(name.lastIndexOf('/') + 1, name.length());
					content = getResources().openRawResource(t[i].getInt(a));
					content.reset();

					// user project files
					if(sFileName.endsWith(config.getPROJECT_ZIP_NAME())) {
						succeed &= Utils.unzip(content, this.getFilesDir().getAbsolutePath()+ "/", true);
					}
					// interpreter bin -> /data/data/com.xxx/...
					else if (config.getINTERPRETER_ZIP_NAME() != null && sFileName.endsWith(config.getINTERPRETER_ZIP_NAME()) ) {
						succeed &= Utils.unzip(content, this.getFilesDir().getAbsolutePath()+ "/", true);
						FileUtils.chmod(new File(config.getINTERPRETER_BIN_PATH() ), 755);
					}
					// interpreter extras -> /sdcard/com.xxx/
					else if ( config.getINTERPRETER_EXTRAS_ZIP_NAME() != null && sFileName.endsWith(config.getINTERPRETER_EXTRAS_ZIP_NAME()) ) {
						Utils.createDirectoryOnExternalStorage( this.getPackageName() + "/" + "extras");
						Utils.createDirectoryOnExternalStorage( this.getPackageName() + "/" + "extras" + "/" + "tmp");
						succeed &= Utils.unzip(content, Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + this.getPackageName() + "/extras/", true);							
					}
					
				} catch (Exception e) {
					Log.e(config.getLOG_TAG(), "Failed to copyResourcesToLocal", e);
					succeed = false;
				}
			} // end for all files in res/raw
			
	 }

  @Override
  protected void onStart() {
	  super.onStart();
	
	  String s = "System infos:";
	  s += " OS Version: " + System.getProperty("os.version") + "(" + android.os.Build.VERSION.INCREMENTAL + ")";
	  s += " | OS API Level: " + android.os.Build.VERSION.SDK;
	  s += " | Device: " + android.os.Build.DEVICE;
	  s += " | Model (and Product): " + android.os.Build.MODEL + " ("+ android.os.Build.PRODUCT + ")";
	  
	  Log.i(config.getLOG_TAG(), s);

	  //finish();
  }
  
}
