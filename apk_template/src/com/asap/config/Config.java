package com.asap.config;

import java.util.Map;

public class Config {

	private static final Config instance = new Config();

	public static Config getConfigSingleton() {
		return instance;
	}
	
	private Config() {
	}
	
	private boolean IS_FOREGROUND_SERVICE = true;

	private String MAIN_SCRIPT_NAME;
	private String PROJECT_ZIP_NAME = "project.zip";
	private String INTERPRETER_ZIP_NAME = null;
	private String INTERPRETER_EXTRAS_ZIP_NAME = null;
	
    private String INTERPRETER_BIN_PATH;
    private String INTERPRETER_NAME;
	private String INTERPRETER_NICE_NAME;
	
    private Map<String, String> ENV_VARS;
    
    private String[] SCRIPT_ARGS = new String[0];
    
	private String LOG_TAG = "AndroidScriptingApps";

	public boolean isIS_FOREGROUND_SERVICE() {
		return IS_FOREGROUND_SERVICE;
	}

	public void setIS_FOREGROUND_SERVICE(boolean iS_FOREGROUND_SERVICE) {
		IS_FOREGROUND_SERVICE = iS_FOREGROUND_SERVICE;
	}

	public String getMAIN_SCRIPT_NAME() {
		return MAIN_SCRIPT_NAME;
	}

	public void setMAIN_SCRIPT_NAME(String mAIN_SCRIPT_NAME) {
		MAIN_SCRIPT_NAME = mAIN_SCRIPT_NAME;
	}

	public String getPROJECT_ZIP_NAME() {
		return PROJECT_ZIP_NAME;
	}

	public void setPROJECT_ZIP_NAME(String pROJECT_ZIP_NAME) {
		PROJECT_ZIP_NAME = pROJECT_ZIP_NAME;
	}

	public String getINTERPRETER_ZIP_NAME() {
		return INTERPRETER_ZIP_NAME;
	}

	public void setINTERPRETER_ZIP_NAME(String iNTERPRETER_ZIP_NAME) {
		INTERPRETER_ZIP_NAME = iNTERPRETER_ZIP_NAME;
	}

	public String getINTERPRETER_EXTRAS_ZIP_NAME() {
		return INTERPRETER_EXTRAS_ZIP_NAME;
	}

	public void setINTERPRETER_EXTRAS_ZIP_NAME(String iNTERPRETER_EXTRAS_ZIP_NAME) {
		INTERPRETER_EXTRAS_ZIP_NAME = iNTERPRETER_EXTRAS_ZIP_NAME;
	}

	public String getINTERPRETER_BIN_PATH() {
		return INTERPRETER_BIN_PATH;
	}

	public void setINTERPRETER_BIN_PATH(
			String iNTERPRETER_BIN_PATH) {
		INTERPRETER_BIN_PATH = iNTERPRETER_BIN_PATH;
	}

	public String getINTERPRETER_NAME() {
		return INTERPRETER_NAME;
	}

	public void setINTERPRETER_NAME(String iNTERPRETER_NAME) {
		INTERPRETER_NAME = iNTERPRETER_NAME;
	}

	public String getINTERPRETER_NICE_NAME() {
		return INTERPRETER_NICE_NAME;
	}

	public void setINTERPRETER_NICE_NAME(String iNTERPRETER_NICE_NAME) {
		INTERPRETER_NICE_NAME = iNTERPRETER_NICE_NAME;
	}

	public Map<String, String> getENV_VARS() {
		return ENV_VARS;
	}

	public void setENV_VARS(Map<String, String> eNV_VARS) {
		ENV_VARS = eNV_VARS;
	}

	public String[] getSCRIPT_ARGS() {
		return SCRIPT_ARGS;
	}

	public void setSCRIPT_ARGS(String[] sCRIPT_ARGS) {
		SCRIPT_ARGS = sCRIPT_ARGS;
	}

	public String getLOG_TAG() {
		return LOG_TAG;
	}

	public void setLOG_TAG(String lOG_TAG) {
		LOG_TAG = lOG_TAG;
	}
	
}
