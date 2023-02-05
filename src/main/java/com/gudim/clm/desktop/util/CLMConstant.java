package com.gudim.clm.desktop.util;

import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CLMConstant {

    public static final String APPLICATION_NAME = "CLM";
    public static final String TITLE = "Consul Loot Master v0.0.4";
    public static final String USER_ID = "user";
    public static final String USER_HOME_DIR = "user.home";
    public static final String STORE_CHILD_DIR = ".store/oauth2_sample";
    public static final String CREDENTIALS_JSON = "/credentials.json";
    public static final String GOOGLE_DRIVE_API_URL = "https://www.googleapis.com/auth/drive";
    public static final String SHEETS_ID = "1qOuISkQ7goUL2ij99Fv-6qERegUGglVKj6VtpqmMmq8";
    public static final String TEMP_FILE_NAME = "temp.xlsx";
    public static final String XLSX_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String PATTERN_MM_DD = "MM/dd";
    public static final String DOT_REGEX = "\\.";
    public static final String DELIMITER_REGEX = "(\\,)|(\\/)|(\\\\)|(\\|)|(\\-)";
    public static final String PLUS = "+";
    public static final String DOT = ".";
    public static final String COMMA = ",";
    public static final String CLOSE_CURLY_BRACES = "}";
    public static final String NUMBER_SIGN = "#";
    public static final String ADDON_PATH = "Interface\\AddOns\\CLM";
    public static final String PATH_CLM_WISHLISTS_LUA = "\\CLM_wishlists.lua";
    public static final String PATH_CLM_ITEMS_LUA = "\\CLM_items.lua";
    public static final String INCORRECT_DIRECTORY_HEADER = "Incorrect directory";
    public static final String INCORRECT_DIRECTORY_MESSAGE = "The path is incorrect, please enter the correct path to the Consul Loot Master addon";
    public static final String REMOVE_FILE_ERROR_MESSAGE = "Non found %s file or directory";
    public static final String UNEXPECTED_VALUE_ERROR_MESSAGE = "Unexpected cell value type: %s";
    public static final String MESSAGE_HAS_BEEN_REMOVED = "%s has been removed";
    public static final String SAVE_LUA_TABLE_ERROR = "Can`t write data: \n%s\nto %s";
    public static final String SAVE_LUA_TABLE = "The file: \"%s\" has been generated";
    public static final String CREATED_TEMP_FILE_MESSAGE = "Temp file %s has been created";
    public static final String CHARACTER_TYPE_MELEE = "melee";
    public static final String CHARACTER_TYPE_CASTER = "caster";
    public static final String CLM_ITEMS_TEMPLATE = "\n\t[%s] = {[\"characterType\"] = \"%s\", [\"nickname\"] = \"%s\", [\"wishNumber\"] = %s}";
    public static final String INIT_EMPTY_CLM_ITEMS = "CLMItems = {}\n";
    public static final String INIT_CLM_ITEMS = "CLMItems[%s] = {";
    public static final String INIT_EMPTY_CLM_WISHLISTS = "CLMWishlists = {}\n";
    public static final String INIT_CLM_WISHLISTS_TYPE = "CLMWishlistsType = {%s\n}\n";
    public static final String INIT_EMPTY_LIST_CLM_WISHLISTS = "CLMWishlists[\"%s\"] = {}\n";
    public static final String INIT_ARRAY_CLM_WISHLISTS = "CLMWishlists[\"%s\"] = {";
    public static final String INIT_ARRAY_CLM_NICKNAME = "CLMNickname = {}\n";
    public static final String ARRAY_NICKNAME = "\n\t[\"%s\"] = {%s\n\t}";
    public static final String ARRAY_CLM_NICKNAME = "CLMNickname[\"%s\"] = {%s\n}\n";
    public static final String VALUE_IN_LIST = "\n\t\t[%s] = {[\"itemId\"] = %s, [\"marker\"] = %s}";
    public static final String VALUE_LIST = "\n\t\"%s\"";
    public static final int INTEGER_THREE = 3;
    public static final int INTEGER_FOUR = 4;
    public static final int INTEGER_HUNDRED = 100;
    public static final int INTEGER_OHF = 150;
    public static final int INTEGER_SIX = 6;
    public static final String MARK_HEX_COLOUR = "FF980000";
    public static final String PRIEST_HEX_COLOUR = "FFF3F3F3";
    public static final String PALADIN_HEX_COLOUR = "FFEA9999";
    public static final String WARLOCK_HEX_COLOUR = "FF674EA7";
    public static final String WARRIOR_HEX_COLOUR = "FFB45F06";
    public static final String HUNTER_HEX_COLOUR = "FF93C47D";
    public static final String SHAMAN_HEX_COLOUR = "FF3C78D8";
    public static final String MAGE_HEX_COLOUR = "FFC9DAF8";
    public static final String ROGUE_HEX_COLOUR = "FFFFD966";
    public static final String DEATH_KNIGHT_HEX_COLOUR = "FFEA4335";
    public static final String DRUID_HEX_COLOUR = "FFFF6D01";
    public static final String HEAD_SLOT = "Head";
    public static final String SHOULDERS_SLOT = "Shoulders";
    public static final String TORSO_SLOT = "Torso";
    public static final String GLOVES_SLOT = "Gloves";
    public static final String LEGS_SLOT = "Legs";
    public static final Map<String, String> PPW_MAP = new ImmutableMap.Builder<String, String>().put(HEAD_SLOT, "45638").put(SHOULDERS_SLOT, "45656").put(TORSO_SLOT, "45632").put(GLOVES_SLOT, "45641").put(LEGS_SLOT, "45653").build();
    public static final Map<String, String> WHS_MAP = new ImmutableMap.Builder<String, String>().put(HEAD_SLOT, "45639").put(SHOULDERS_SLOT, "45657").put(TORSO_SLOT, "45633").put(GLOVES_SLOT, "45642").put(LEGS_SLOT, "45654").build();
    public static final Map<String, String> DMRD_MAP = new ImmutableMap.Builder<String, String>().put(HEAD_SLOT, "45640").put(SHOULDERS_SLOT, "45658").put(TORSO_SLOT, "45634").put(GLOVES_SLOT, "45643").put(LEGS_SLOT, "45655").build();
}
