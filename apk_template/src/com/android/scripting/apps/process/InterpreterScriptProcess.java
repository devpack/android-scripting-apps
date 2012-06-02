package com.android.scripting.apps.process;

import android.util.Log;

import com.googlecode.android_scripting.AndroidProxy;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration;
import com.googlecode.android_scripting.interpreter.MyInterpreter;

import java.io.File;

public class InterpreterScriptProcess extends InterpreterProcess
{
  private final File mScript;

  public InterpreterScriptProcess(File paramFile, InterpreterConfiguration paramInterpreterConfiguration, AndroidProxy paramAndroidProxy) {
    super(new MyInterpreter( null ) , paramAndroidProxy);
    this.mScript = paramFile;
    setName(paramFile.getName());
    
    String str = "";
//    if(paramInterpreterConfiguration.getInterpreterForScript("foo.py") != null) {
//    	str = paramInterpreterConfiguration.getInterpreterForScript("foo.py").getScriptCommand();
//    }
    Object[] arrayOfObject = new Object[1];
    arrayOfObject[0] = paramFile.getAbsolutePath();
    setCommand(String.format(str, arrayOfObject));
  }

  public String getPath() {
    return this.mScript.getPath();
  }
}