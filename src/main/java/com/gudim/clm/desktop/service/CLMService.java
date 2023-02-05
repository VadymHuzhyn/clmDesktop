package com.gudim.clm.desktop.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.gudim.clm.desktop.dto.CLMLuaTable;
import com.gudim.clm.desktop.dto.CLMWishlist;
import com.gudim.clm.desktop.dto.items.CLMItem;
import com.gudim.clm.desktop.dto.wishlists.CLMCharType;
import com.gudim.clm.desktop.dto.wishlists.CLMUser;
import com.gudim.clm.desktop.dto.wishlists.CLMUserItem;
import com.gudim.clm.desktop.util.GoogleUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.IntStream;

import static com.gudim.clm.desktop.util.CLMConstant.*;
import static org.apache.poi.ss.usermodel.CellType.BLANK;

@Service
@Log4j2
public class CLMService {

    private static String getCharacterType(int sheetNumber) {
        if (sheetNumber == NumberUtils.INTEGER_ONE) {
            return CHARACTER_TYPE_CASTER;
        } else if (sheetNumber == NumberUtils.INTEGER_TWO) {
            return CHARACTER_TYPE_MELEE;
        } else {
            throw new IllegalStateException(String.format(UNEXPECTED_VALUE_ERROR_MESSAGE, sheetNumber));
        }
    }

    private static String getCellValue(Cell cell) {
        Object result = StringUtils.EMPTY;
        CellType cellType = getCellType(cell);
        switch (cellType) {
            case STRING:
                result = cell.getStringCellValue();
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_MM_DD);
                    result = sdf.format(cell.getDateCellValue());
                } else {
                    DecimalFormat decimalFormat = new DecimalFormat(NUMBER_SIGN);
                    decimalFormat.setRoundingMode(RoundingMode.CEILING);
                    result = isDecimalPointZero(cell.getNumericCellValue()) ? decimalFormat.format(cell.getNumericCellValue()) : Double.valueOf(cell.getNumericCellValue());
                }
                break;
            case BOOLEAN:
                result = cell.getBooleanCellValue();
                break;
            case FORMULA:
                result = cell.getCellFormula();
                break;
            case _NONE:
            case ERROR:
            case BLANK:
                break;
            default:
                throw new IllegalStateException(String.format(UNEXPECTED_VALUE_ERROR_MESSAGE, cellType));
        }
        return result.toString();
    }

    private static boolean isDecimalPointZero(double numericCellValue) {
        BigDecimal bd = BigDecimal.valueOf((numericCellValue - Math.floor(numericCellValue)) * INTEGER_HUNDRED);
        bd = bd.setScale(INTEGER_FOUR, RoundingMode.HALF_DOWN);
        return bd.intValue() == NumberUtils.INTEGER_ZERO;
    }

    private static CellType getCellType(Cell cell) {
        CellType cellType;
        try {
            cellType = cell.getCellType();
        } catch (NullPointerException e) {
            cellType = BLANK;
        }
        return cellType;
    }

    public void downloadXLSXFromDrive() {
        try (OutputStream outputStream = Files.newOutputStream(Paths.get(TEMP_FILE_NAME))) {
            Drive drive = GoogleUtil.getGoogleDriveData();
            File file = drive.files().get(SHEETS_ID).setQuotaUser(UUID.randomUUID().toString()).execute();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            drive.files().export(file.getId(), XLSX_MIME).executeAndDownloadTo(byteArrayOutputStream);
            byteArrayOutputStream.writeTo(outputStream);
            log.info(String.format(CREATED_TEMP_FILE_MESSAGE, TEMP_FILE_NAME));
        } catch (IOException | GeneralSecurityException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public CLMWishlist getDataFromXLSX() {
        XSSFSheet sheet;
        CLMWishlist clmWishlist = new CLMWishlist();
        try (FileInputStream fileInputStream = new FileInputStream(TEMP_FILE_NAME); XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream)) {
            for (int sheetNumber = NumberUtils.INTEGER_ONE; sheetNumber < workbook.getNumberOfSheets(); sheetNumber++) {
                CLMCharType clmCharType = new CLMCharType();
                String characterType = getCharacterType(sheetNumber);
                clmCharType.setCharTypeName(characterType);
                sheet = workbook.getSheetAt(sheetNumber);
                Row nicknameRow = sheet.getRow(sheet.getFirstRowNum());
                for (int nicknameCell = INTEGER_FOUR; nicknameCell < nicknameRow.getLastCellNum(); nicknameCell++) {
                    Cell nicknameCellValue = getCell(nicknameRow, nicknameCell);
                    String nicknameCellColour = getCellColour(nicknameCellValue);
                    String nickname = getCellValue(nicknameCellValue);
                    if (StringUtils.isNotBlank(nickname)) {
                        CLMUser clmUser = new CLMUser();
                        clmUser.setNickname(nickname);
                        for (int rowNum = NumberUtils.INTEGER_ONE; rowNum < INTEGER_SIX; rowNum++) {
                            Row row = sheet.getRow(rowNum);
                            String tokenCellValue = getCellValue(getCell(row, INTEGER_THREE));
                            String itemId = updateTokenId(nicknameCellColour, tokenCellValue);
                            populateData(clmWishlist, characterType, nicknameCell, nickname, clmUser, row, itemId);
                        }
                        for (int rowNum = INTEGER_SIX; rowNum < Math.min(INTEGER_OHF, sheet.getLastRowNum()); rowNum++) {
                            Row row = sheet.getRow(rowNum);
                            String itemId = getCellValue(getCell(row, INTEGER_THREE));
                            populateData(clmWishlist, characterType, nicknameCell, nickname, clmUser, row, itemId);
                        }
                        clmCharType.getUserInfos().add(clmUser);
                    }
                }
                clmWishlist.getWishlists().add(clmCharType);
            }
        } catch (IOException ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
        }
        log.info("Parse xlsx is done");
        return clmWishlist;
    }

    private void populateData(CLMWishlist clmWishlist, String characterType, int nicknameCell, String nickname, CLMUser clmUser, Row row, String itemId) {
        Cell wishNumberCell = getCell(row, nicknameCell);
        String wishNumberCellColour = getCellColour(wishNumberCell);
        String wishNumber = getCellValue(wishNumberCell).replace(StringUtils.SPACE, StringUtils.EMPTY);
        if (StringUtils.isNotBlank(itemId) && StringUtils.isNotBlank(wishNumber)) {
            List<String> splitCellValue = Arrays.asList(wishNumber.replaceAll(DELIMITER_REGEX, DOT).split(DOT_REGEX));
            populateCLMUserItem(clmUser, itemId, wishNumberCellColour, splitCellValue, wishNumber);
            populateCLMItems(clmWishlist, characterType, nickname, itemId, wishNumberCellColour, wishNumber, splitCellValue);
        }
    }

    private void populateCLMUserItem(CLMUser clmUser, String itemId, String wishNumberCellColour, List<String> splitCellValue, String wishNumber) {
        List<CLMUserItem> clmUserItems = clmUser.getItems();
        setCLMItemInfo(itemId, splitCellValue, NumberUtils.INTEGER_ZERO, StringUtils.equals(MARK_HEX_COLOUR, wishNumberCellColour), clmUserItems);
        if (splitCellValue.size() != NumberUtils.INTEGER_ONE) {
            setCLMItemInfo(itemId, splitCellValue, NumberUtils.INTEGER_ONE, StringUtils.contains(wishNumber, PLUS), clmUserItems);
        }
    }

    private void setCLMItemInfo(String itemId, List<String> splitCellValue, Integer integerZero, boolean isMarked, List<CLMUserItem> clmUserItems) {
        CLMUserItem clmItemInfo;
        clmItemInfo = new CLMUserItem();
        clmItemInfo.setItemId(itemId);
        clmItemInfo.setWishNumber(splitCellValue.get(integerZero).replace(PLUS, StringUtils.EMPTY));
        clmItemInfo.setMarker(isMarked ? Boolean.TRUE : Boolean.FALSE);
        clmUserItems.add(clmItemInfo);
    }


    private void populateCLMItems(CLMWishlist clmWishlist, String characterType, String nickname, String itemId, String wishNumberCellColour, String wishNumber, List<String> splitCellValue) {
        Map<String, List<CLMItem>> items = clmWishlist.getItems();
        if (splitCellValue.size() == NumberUtils.INTEGER_ONE && !StringUtils.equals(MARK_HEX_COLOUR, wishNumberCellColour)) {
            setCLMItem(characterType, nickname, splitCellValue.get(NumberUtils.INTEGER_ZERO), itemId, items);
        }
        if (splitCellValue.size() == NumberUtils.INTEGER_TWO && !StringUtils.equals(MARK_HEX_COLOUR, wishNumberCellColour)) {
            for (String value : splitCellValue) {
                setCLMItem(characterType, nickname, value, itemId, items);
            }
        } else if (splitCellValue.size() == NumberUtils.INTEGER_TWO && StringUtils.equals(MARK_HEX_COLOUR, wishNumberCellColour) && !StringUtils.contains(wishNumber, PLUS)) {
            setCLMItem(characterType, nickname, splitCellValue.get(NumberUtils.INTEGER_ONE), itemId, items);
        }
    }

    private void setCLMItem(String characterType, String nickname, String wishNumber, String itemId, Map<String, List<CLMItem>> items) {
        CLMItem clmItem = new CLMItem();
        clmItem.setCharacterType(characterType);
        clmItem.setNickname(nickname);
        clmItem.setWishNumber(wishNumber.replace(PLUS, StringUtils.EMPTY));
        addOrUpdateMap(itemId, clmItem, items);
    }

    private void addOrUpdateMap(String itemId, CLMItem clmItem, Map<String, List<CLMItem>> items) {
        List<CLMItem> contentList = new ArrayList<>();
        contentList.add(clmItem);
        if (items.containsKey(itemId)) {
            items.get(itemId).add(clmItem);
        } else {
            items.put(itemId, contentList);
        }
    }

    private String updateTokenId(String classColour, String itemId) {
        String result;
        switch (classColour) {
            case PRIEST_HEX_COLOUR:
            case PALADIN_HEX_COLOUR:
            case WARLOCK_HEX_COLOUR:
                result = PPW_MAP.get(itemId);
                break;
            case WARRIOR_HEX_COLOUR:
            case HUNTER_HEX_COLOUR:
            case SHAMAN_HEX_COLOUR:
                result = WHS_MAP.get(itemId);
                break;
            case DRUID_HEX_COLOUR:
            case MAGE_HEX_COLOUR:
            case ROGUE_HEX_COLOUR:
            case DEATH_KNIGHT_HEX_COLOUR:
                result = DMRD_MAP.get(itemId);
                break;
            default:
                throw new IllegalStateException(String.format(UNEXPECTED_VALUE_ERROR_MESSAGE, classColour));
        }
        return result;
    }

    private Cell getCell(Row itemRow, int cellNum) {
        return itemRow.getCell(cellNum, MissingCellPolicy.RETURN_NULL_AND_BLANK);
    }

    private String getCellColour(Cell cell) {
        String hexString = StringUtils.EMPTY;
        CellStyle cellStyle = cell.getCellStyle();
        Color color = cellStyle.getFillForegroundColorColor();
        if (color != null) {
            if (color instanceof XSSFColor) {
                hexString = ((XSSFColor) color).getARGBHex();
            } else if (color instanceof HSSFColor && !(color.equals(HSSFColor.HSSFColorPredefined.AUTOMATIC.getColor()))) {
                hexString = ((HSSFColor) color).getHexString();
            }
        }
        return hexString;
    }

    public CLMLuaTable luaTableMapper(CLMWishlist clmWishlist) {
        CLMLuaTable clmLuaTable = new CLMLuaTable();
        StringBuilder itemsSB = populateCLMItemsSB(clmWishlist.getItems());
        clmLuaTable.setItemsSB(itemsSB);
        StringBuilder wishlistsSB = populateWishlistSB(clmWishlist.getWishlists());
        clmLuaTable.setWishListsSB(wishlistsSB);
        return clmLuaTable;
    }

    private StringBuilder populateWishlistSB(List<CLMCharType> wishlists) {
        log.info("Started writing data to CLMWishlists LuaTable");
        StringBuilder wishlistsSB = new StringBuilder();
        wishlistsSB.append(INIT_EMPTY_CLM_WISHLISTS);
        StringJoiner joinerCharTypeName = new StringJoiner(COMMA);
        wishlists.forEach(clmCharType -> {
            String charTypeName = clmCharType.getCharTypeName();
            joinerCharTypeName.add(String.format(VALUE_LIST, charTypeName));
            wishlistsSB.append(String.format(INIT_EMPTY_LIST_CLM_WISHLISTS, charTypeName));
            wishlistsSB.append(String.format(INIT_ARRAY_CLM_WISHLISTS, charTypeName));
            StringJoiner joinerNickname = new StringJoiner(COMMA);
            StringJoiner joinerUserData = new StringJoiner(COMMA);
            clmCharType.getUserInfos().forEach(clmUser -> {
                StringJoiner joinerUserItem = new StringJoiner(COMMA);
                clmUser.getItems().stream().map(clmUserItem -> String.format(VALUE_IN_LIST, clmUserItem.getWishNumber(), clmUserItem.getItemId(), clmUserItem.getMarker())).forEach(joinerUserItem::add);
                String nickname = clmUser.getNickname();
                joinerUserData.add(String.format(ARRAY_NICKNAME, nickname, joinerUserItem));
                joinerNickname.add(String.format(VALUE_LIST, nickname));
            });
            wishlistsSB.append(joinerUserData).append(StringUtils.LF).append(CLOSE_CURLY_BRACES).append(StringUtils.LF);
            wishlistsSB.insert(NumberUtils.INTEGER_ZERO, String.format(ARRAY_CLM_NICKNAME, charTypeName, joinerNickname));
        });
        wishlistsSB.insert(NumberUtils.INTEGER_ZERO, INIT_ARRAY_CLM_NICKNAME);
        wishlistsSB.insert(NumberUtils.INTEGER_ZERO, String.format(INIT_CLM_WISHLISTS_TYPE, joinerCharTypeName));
        log.info("Completed writing data to CLMItems LuaTable");
        return wishlistsSB;
    }

    private StringBuilder populateCLMItemsSB(Map<String, List<CLMItem>> clmItems) {
        log.info("Started writing data to CLMItems LuaTable");
        StringBuilder itemsSB = new StringBuilder();
        itemsSB.append(INIT_EMPTY_CLM_ITEMS);
        clmItems.forEach((key, value) -> {
            itemsSB.append(String.format(INIT_CLM_ITEMS, key));
            StringJoiner joiner = new StringJoiner(COMMA);
            IntStream.range(NumberUtils.INTEGER_ZERO, value.size()).forEach(i -> {
                CLMItem clmItem = value.get(i);
                joiner.add((String.format(CLM_ITEMS_TEMPLATE, i + NumberUtils.INTEGER_ONE, clmItem.getCharacterType(), clmItem.getNickname(), clmItem.getWishNumber())));
            });
            itemsSB.append(joiner).append(StringUtils.LF).append(CLOSE_CURLY_BRACES).append(StringUtils.LF);
        });
        log.info("Completed writing data to CLMItems LuaTable");
        return itemsSB;
    }

    public void saveLuaTableFile(StringBuilder stringBuilder, String path) {
        java.io.File file = new java.io.File(path);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.append(stringBuilder);
            log.info(String.format(SAVE_LUA_TABLE, path));
        } catch (IOException e) {
            log.error(String.format(SAVE_LUA_TABLE_ERROR, stringBuilder, path));
        }
    }

    public void removeTempFile() {
        try {
            Files.delete(Paths.get(TEMP_FILE_NAME));
            log.info(String.format(MESSAGE_HAS_BEEN_REMOVED, TEMP_FILE_NAME));
        } catch (IOException e) {
            log.error(String.format(REMOVE_FILE_ERROR_MESSAGE, TEMP_FILE_NAME));
        }
    }
}


