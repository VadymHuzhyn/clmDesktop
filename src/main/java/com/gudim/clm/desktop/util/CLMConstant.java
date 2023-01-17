package com.gudim.clm.desktop.util;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CLMConstant {
	
	public static final Set<String> DRIVE_SCOPE                    = Collections.singleton(
		"https://www.googleapis.com/auth/drive");
	public static final File        DATA_STORE_DIR                 = new File(
		System.getProperty("user.home"), ".store/oauth2_sample");
	public static final String      APPLICATION_NAME               = "clm";
	public static final String      CREDENTIALS_JSON               = "/credentials.json";
	//	public static final String      SHEETS_ID                      = "10C6G6zdskkXG2NsN4KJlNXVweYOSrhaJvAa2A1M46rg";
	//	public static final String      SHEETS_ID                      = "16g2a0n9LFhoqTGrGg4azgaSJmQ_pjFL1HMFnONhkvMQ";
	public static final String      SHEETS_ID                      = "1yj2BccuTImY17eGaSwCjAZI_yWF9ug8A2iW57a6KcOM";
	public static final String      FILE_NAME                      = "sample.xlsx";
	public static final String      XLSX_MIME                      = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	public static final String      STRING_ZERO                    = "0";
	public static final String      DOT_REGEX                      = "\\.";
	public static final String      COMMA                          = ",";
	public static final String      ADDON_PATH                     = "Interface\\AddOns\\CLM";
	public static final String      REMOVE_FILE_ERROR_MESSAGE      = ": no such file or directory";
	public static final String      UNEXPECTED_VALUE_ERROR_MESSAGE = "Unexpected value:";
	public static final String      INCORRECT_DIRECTORY_HEADER     = "Incorrect directory";
	public static final String      INCORRECT_DIRECTORY_MESSAGE    = "The path is incorrect, please enter the correct path to the Consul Loot Master addon";
	public static final String      CLM_WISHLISTS_INIT             = "CLM_wishlists = {}";
	public static final String      CHARACTER_TYPE_HEAL            = "heal";
	public static final String      CHARACTER_TYPE_DD              = "dd";
	public static final String      CHARACTER_TYPE_CASTER          = "caster";
	public static final String      CHARACTER_TYPE_TANK            = "tank";
	public static final String      CLM_WISHLISTS_TYPE_INIT        = "CLM_wishlists_type = {\"heal\", \"dd\", \"caster\", \"tank\"}";
	public static final String      CLM_WISHLISTS_START_ROW        = "CLM_wishlists[\"";
	public static final String      END_ROW_EMPTY                  = "\"] = {}";
	public static final String      MIDDLE_ROW                     = "\"][\"";
	public static final String      AFFEX_ROW                      = "\"][1] = {";
	public static final String      CLOSE_ROW                      = "}";
	public static final String      PATH_CLM_WISHLISTS_LUA         = "CLM_wishlists.lua";
	public static final String      PATH_CLM_ITEMS_LUA             = "CLM_items.lua";
	public static final String      START_ROW_INDEX                = "[";
	public static final String      END_ROW_INDEX                  = "] = ";
	public static final String      MESSAGE_HAS_BEEN_REMOVED       = "has been removed";
	public static final String      SAVE_LUA_TABLE_ERROR           = "Can`t write data: \n %s \n to %s";
	public static final char        CHAR_DOT                       = '.';
	public static final int         ROW_END_FIRST_VALUE            = 150;
	public static final int         NICKNAME_CELL                  = 3;
}
