package com.android.scripting.apps.process;

import android.os.Environment;

import com.android.scripting.apps.ScriptApplication;
import com.android.scripting.apps.config.Config;
import com.googlecode.android_scripting.Analytics;
import com.googlecode.android_scripting.AndroidProxy;
import com.googlecode.android_scripting.interpreter.Interpreter;
import com.googlecode.android_scripting.interpreter.MyInterpreter;
import com.googlecode.android_scripting.interpreter.InterpreterConstants;
import com.googlecode.android_scripting.jsonrpc.RpcReceiverManagerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InterpreterProcess extends Process {
  Config config = Config.getConfigSingleton();

  private final AndroidProxy mProxy;
  private final Interpreter mInterpreter;
  private String mCommand;
  
  private String pyname = config.getINTERPRETER_NAME();
  private File binary = null;
  private String niceName;
  private String interactiveCommand;
  private List<String> arguments;
  private Map<String, String> environmentVariables = null;
  
  /**
   * Creates a new {@link InterpreterProcess}.
   *
   * @param launchScript
   *          the absolute path to a script that should be launched with the interpreter
   * @param port
   *          the port that the AndroidProxy is listening on
   */
  public InterpreterProcess(MyInterpreter myInterpreter, AndroidProxy paramAndroidProxy) {
    mProxy = paramAndroidProxy;
    mInterpreter = myInterpreter.getInterpreter();

//    if(mInterpreter != null) {
//        binary = mInterpreter.getBinary();
//        niceName = mInterpreter.getNiceName();
//        pyname = mInterpreter.getName();
//        interactiveCommand = mInterpreter.getInteractiveCommand();
//        arguments = myInterpreter.getArguments();
//        environmentVariables = mInterpreter.getEnvironmentVariables();	
//    }
//    // this means we are using our embedded interpreter
//    else {
    	niceName = config.getINTERPRETER_NICE_NAME();
        pyname = config.getINTERPRETER_NAME();
        interactiveCommand = "";
        arguments = new ArrayList<String>();
//    }
    
//    Log.e("mInterpreter.getBinary() " + binary );
//    Log.e("mInterpreter.getNiceName() " + niceName);
//    Log.e("mInterpreter.getName() " + pyname);
//    Log.e("mInterpreter.getInteractiveCommand() " + interactiveCommand);
//    Log.e("myInterpreter.getArguments( " + arguments);
//    Log.e("mInterpreter.getEnvironmentVariables() " + environmentVariables);
//
//    Log.e("System.getenv() " + System.getenv());
//    Log.e("getHost() " + getHost());
//    Log.e("Integer.toString(getPort()) " + Integer.toString(getPort()));
//    if (paramAndroidProxy.getSecret() != null) {
//        Log.e("getSecret() " + getSecret());
//    }
    
    if(binary != null) {
        setBinary(binary);
    }
    setName(niceName);
    setCommand(interactiveCommand);
    addAllArguments(arguments);
    putAllEnvironmentVariables(System.getenv());
    putEnvironmentVariable("AP_HOST", getHost());
    putEnvironmentVariable("AP_PORT", Integer.toString(getPort()));
    if (paramAndroidProxy.getSecret() != null) {
      putEnvironmentVariable("AP_HANDSHAKE", getSecret());
    }
    
    if(environmentVariables != null) {
        putAllEnvironmentVariables(environmentVariables);
    }
  }

  protected void setCommand(String command) {
    mCommand = command;
  }

  public Interpreter getInterpreter() {
    return mInterpreter;
  }

  public String getHost() {
    return mProxy.getAddress().getHostName();
  }

  public int getPort() {
    return mProxy.getAddress().getPort();
  }

  public String getSecret() {
    return mProxy.getSecret();
  }

  @Override
  public void start(final Runnable shutdownHook) {
    start(shutdownHook, null);
  }

  @Override
  public String getWorkingDirectory() {
    return InterpreterConstants.SDCARD_SL4A_ROOT;
  }

  @Override
  public String getSdcardPackageDirectory() {
    return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + ScriptApplication.getThePackageName();
  }
  
  public RpcReceiverManagerFactory getRpcReceiverManagerFactory()
  {
    return this.mProxy.getRpcReceiverManagerFactory();
  }
  
  public void start(Runnable paramRunnable, List<String> paramList)
  {
    String[] arrayOfString = new String[1];
    arrayOfString[0] = pyname;
    Analytics.track(arrayOfString);
    if (!this.mCommand.equals(""))
      addArgument(this.mCommand);
    if (paramList != null)
      addAllArguments(paramList);
    super.start(paramRunnable);
  }
  
  @Override
  public void kill() {
    super.kill();
    mProxy.shutdown();
  }

}
