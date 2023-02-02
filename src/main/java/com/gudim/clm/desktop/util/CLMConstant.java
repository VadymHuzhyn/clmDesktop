package com.gudim.clm.desktop.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CLMConstant {
	
	public static final String APPLICATION_NAME     = "CLM";
	public static final String TITLE                = "Consul Loot Master v0.0.1";
	public static final String USER_ID              = "user";
	public static final String USER_HOME_DIR        = "user.home";
	public static final String STORE_CHILD_DIR      = ".store/oauth2_sample";
	public static final String CREDENTIALS_JSON     = "/credentials.json";
	public static final String GOOGLE_DRIVE_API_URL = "https://www.googleapis.com/auth/drive";
	//	public static final String SHEETS_ID            = "1yj2BccuTImY17eGaSwCjAZI_yWF9ug8A2iW57a6KcOM";
	public static final String SHEETS_ID            = "1GZor1yPCKZpc2kAajFBxljNvAPDz2WjUuJnM53_JPVw";
	
	public static final String TEMP_FILE_NAME = "temp.xlsx";
	public static final String XLSX_MIME      = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	
	public static final String DELIMITER_REGEX    = "(\\.)|(,)|(\\/)|(\\\\)|(\\|)|(\\-)|( )";
	public static final String DOT_REGEX          = "\\.";
	public static final String DOT                = ".";
	public static final String COMMA              = ",";
	public static final String CLOSE_CURLY_BRACES = "}";
	public static final String NUMBER_SIGN        = "#";
	
	public static final String ADDON_PATH             = "Interface\\AddOns\\CLM";
	public static final String PATH_CLM_WISHLISTS_LUA = "\\CLM_wishlists.lua";
	public static final String PATH_CLM_ITEMS_LUA     = "\\CLM_items.lua";
	
	public static final String INCORRECT_DIRECTORY_HEADER  = "Incorrect directory";
	public static final String INCORRECT_DIRECTORY_MESSAGE = "The path is incorrect, please enter the correct path to the Consul Loot Master addon";
	
	public static final String REMOVE_FILE_ERROR_MESSAGE      = "Non found %s file or directory";
	public static final String UNEXPECTED_VALUE_ERROR_MESSAGE = "Unexpected cell value type: %s";
	public static final String MESSAGE_HAS_BEEN_REMOVED       = "%s has been removed";
	public static final String SAVE_LUA_TABLE_ERROR           = "Can`t write data: \n%s\nto %s";
	public static final String SAVE_LUA_TABLE                 = "The file: \"%s\" has been generated";
	public static final String CREATED_TEMP_FILE_MESSAGE      = "Temp file %s has been created";
	public static final String CHARACTER_TYPE_MELEE           = "melee";
	public static final String CHARACTER_TYPE_CASTER          = "caster";
	
	public static final String CLM_ITEMS_TEMPLATE            = "[%s] = {[\"characterType\"] = \"%s\",[\"nickname\"] = \"%s\",[\"wishNumber\"] = \"%s\"}";
	public static final String INIT_EMPTY_CLM_ITEMS          = "CLM_items = {}";
	public static final String INIT_CLM_ITEMS                = "CLM_items[%s] = {";
	public static final String INIT_EMPTY_CLM_WISHLISTS      = "CLM_wishlists = {}";
	public static final String INIT_CLM_WISHLISTS_TYPE       = "CLM_wishlists_type = {\"%s\", \"%s\"}\n";
	public static final String INIT_EMPTY_LIST_CLM_WISHLISTS = "CLM_wishlists[\"%s\"] = {}\n";
	public static final String INIT_ARRAY_CLM_WISHLISTS      = "CLM_wishlists[\"%s\"] = {";
	public static final String VALUE_IN_LIST                 = "[%s] = %s";
	public static final String ARRAY_NICKNAME                = "[\"%s\"] = {";
	
	public static final int NICKNAME_CELL = 4;
	public static final int INT_4         = 4;
	public static final int INT_100       = 100;
	public static final int INT_150       = 150;
}
