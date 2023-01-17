package com.gudim.clm.desktop.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CLMConstant {
	
	public static final String APPLICATION_NAME               = "CLM";
	public static final String CREDENTIALS_JSON               = "/credentials.json";
	public static final String SHEETS_ID                      = "1yj2BccuTImY17eGaSwCjAZI_yWF9ug8A2iW57a6KcOM";
	public static final String TEMP_FILE_NAME                 = "sample.xlsx";
	public static final String XLSX_MIME                      = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	public static final String STRING_ZERO                    = "0";
	public static final String DOT_REGEX                      = "\\.";
	public static final String COMMA                          = ",";
	public static final String ADDON_PATH                     = "Interface\\AddOns\\CLM";
	public static final String REMOVE_FILE_ERROR_MESSAGE      = ": no such file or directory";
	public static final String UNEXPECTED_VALUE_ERROR_MESSAGE = "Unexpected value:";
	public static final String INCORRECT_DIRECTORY_HEADER     = "Incorrect directory";
	public static final String INCORRECT_DIRECTORY_MESSAGE    = "The path is incorrect, please enter the correct path to the Consul Loot Master addon";
	public static final String CHARACTER_TYPE_HEAL            = "heal";
	public static final String CHARACTER_TYPE_DD              = "dd";
	public static final String CHARACTER_TYPE_CASTER          = "caster";
	public static final String CHARACTER_TYPE_TANK            = "tank";
	public static final String PATH_CLM_WISHLISTS_LUA         = "CLM_wishlists.lua";
	public static final String PATH_CLM_ITEMS_LUA             = "CLM_items.lua";
	public static final String MESSAGE_HAS_BEEN_REMOVED       = "has been removed";
	public static final String SAVE_LUA_TABLE_ERROR           = "Can`t write data: \n %s \n to %s";
	public static final char   CHAR_DOT                       = '.';
	public static final int    ROW_END_FIRST_VALUE            = 150;
	public static final int    NICKNAME_CELL                  = 3;
}
