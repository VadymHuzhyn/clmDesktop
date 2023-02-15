package com.gudim.clm.desktop.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.gudim.clm.desktop.dto.ItemInfoDTO;
import com.gudim.clm.desktop.dto.ItemList;
import com.gudim.clm.desktop.dto.UserInfoDTO;
import com.gudim.clm.desktop.dto.Wishlist;
import com.gudim.clm.desktop.util.GoogleUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
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

    public List<Wishlist> getWishlist() {
        XSSFSheet sheet;
        List<Wishlist> wishlists = new ArrayList<>();
        try (FileInputStream fileInputStream = new FileInputStream(TEMP_FILE_NAME); XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream)) {
            for (int sheetNumber = NumberUtils.INTEGER_ONE; sheetNumber < workbook.getNumberOfSheets(); sheetNumber++) {
                Wishlist wishlist = new Wishlist();
                wishlist.setCharType(getCharacterType(sheetNumber));
                sheet = workbook.getSheetAt(sheetNumber);
                Row nicknameRow = sheet.getRow(sheet.getFirstRowNum());
                populateUserInfos(sheet, wishlist, nicknameRow);
                wishlists.add(wishlist);
            }
        } catch (IOException ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
        }
        log.info("Parse xlsx is done");
        return wishlists;
    }

    private void populateUserInfos(XSSFSheet sheet, Wishlist wishlist, Row nicknameRow) {
        IntStream.range(INTEGER_FOUR, nicknameRow.getLastCellNum()).forEach(nicknameCellNumber -> {
            Cell nicknameCell = nicknameRow.getCell(nicknameCellNumber, MissingCellPolicy.RETURN_NULL_AND_BLANK);
            String nicknameCellColour = getColourHex(nicknameCell);
            String nickname = getCellValue(nicknameCell);
            if (StringUtils.isNotBlank(nickname)) {
                UserInfoDTO userInfoDTO = new UserInfoDTO();
                userInfoDTO.setNickname(nickname);
                populateItemInfos(sheet, nicknameCellNumber, nicknameCellColour, userInfoDTO);
                wishlist.getUserInfos().add(userInfoDTO);
            }
        });
    }

    private void populateItemInfos(XSSFSheet sheet, int nicknameCellNumber, String nicknameCellColour, UserInfoDTO userInfoDTO) {
        for (int itemRowNumber = NumberUtils.INTEGER_ONE; itemRowNumber < Math.min(INTEGER_OHF, sheet.getLastRowNum()); itemRowNumber++) {
            Row itemRow = sheet.getRow(itemRowNumber);
            String itemId = getItemId(nicknameCellColour, itemRowNumber, itemRow);
            String bossName = getBossName(populateBossNames(sheet), itemRowNumber);
            Cell wishNumberCell = itemRow.getCell(nicknameCellNumber, MissingCellPolicy.RETURN_NULL_AND_BLANK);
            String wishNumberCellColour = getColourHex(wishNumberCell);
            String wishNumber = getCellValue(wishNumberCell).replace(StringUtils.SPACE, StringUtils.EMPTY);
            if (StringUtils.isNotBlank(itemId) && StringUtils.isNotBlank(wishNumber)) {
                List<String> wishNumberList = Arrays.asList(wishNumber.replaceAll(DELIMITER_REGEX, DOT).split(DOT_REGEX));
                List<ItemInfoDTO> itemInfoDTOS = userInfoDTO.getItems();
                boolean isMarked = StringUtils.equals(MARK_HEX_COLOUR, wishNumberCellColour);
                String wishNumberValue = wishNumberList.get(NumberUtils.INTEGER_ZERO).replace(PLUS, StringUtils.EMPTY);
                itemInfoDTOS.add(itemInfoMapper(itemId, wishNumberValue, isMarked, bossName));
                if (wishNumberList.size() == NumberUtils.INTEGER_TWO) {
                    isMarked = StringUtils.contains(wishNumber, PLUS);
                    wishNumberValue = wishNumberList.get(NumberUtils.INTEGER_ONE).replace(PLUS, StringUtils.EMPTY);
                    itemInfoDTOS.add(itemInfoMapper(itemId, wishNumberValue, isMarked, bossName));
                }
            }
        }
    }

    private String getBossName(LinkedHashMap<String, List<Integer>> bossNameMap, int itemRowNumber) {
        String bossName = StringUtils.EMPTY;
        for (Map.Entry<String, List<Integer>> entry : bossNameMap.entrySet()) {
            if (entry.getValue().stream().anyMatch(rowNumber -> itemRowNumber == rowNumber)) {
                bossName = entry.getKey();
            }
        }
        return bossName;
    }

    private LinkedHashMap<String, List<Integer>> populateBossNames(XSSFSheet sheet) {
        LinkedHashMap<String, List<Integer>> boss = new LinkedHashMap<>();
        String bossNameCellValue = StringUtils.EMPTY;
        for (int rowNum = NumberUtils.INTEGER_ONE; rowNum < Math.min(INTEGER_OHF, sheet.getLastRowNum()); rowNum++) {
            Row row = sheet.getRow(rowNum);
            Cell bossCell = row.getCell(NumberUtils.INTEGER_ZERO, MissingCellPolicy.RETURN_NULL_AND_BLANK);
            String temp = getCellValue(bossCell);
            if (!StringUtils.EMPTY.equals(temp)) {
                bossNameCellValue = temp;
            }
            List<Integer> rowNumber = new ArrayList<>();
            rowNumber.add(rowNum);
            if (boss.containsKey(bossNameCellValue)) {
                boss.get(bossNameCellValue).add(rowNum);
            } else {
                boss.put(bossNameCellValue, rowNumber);
            }
        }
        return boss;
    }

    private ItemInfoDTO itemInfoMapper(String itemId, String wishNumber, boolean isMarked, String bossName) {
        ItemInfoDTO clmItemInfo = new ItemInfoDTO();
        clmItemInfo.setItemId(itemId);
        clmItemInfo.setBossName(bossName);
        clmItemInfo.setWishNumber(wishNumber);
        clmItemInfo.setMarker(isMarked);
        return clmItemInfo;
    }

    public Map<String, List<ItemList>> getItemList() {
        XSSFSheet sheet;
        Map<String, List<ItemList>> items = new HashMap<>();
        try (FileInputStream fileInputStream = new FileInputStream(TEMP_FILE_NAME); XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream)) {
            for (int sheetNumber = NumberUtils.INTEGER_ONE; sheetNumber < workbook.getNumberOfSheets(); sheetNumber++) {
                sheet = workbook.getSheetAt(sheetNumber);
                String characterType = getCharacterType(sheetNumber);
                Row nicknameRow = sheet.getRow(sheet.getFirstRowNum());
                populateUser(sheet, items, characterType, nicknameRow);
            }
        } catch (IOException ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
        }
        log.info("Parse xlsx is done");
        return items;
    }

    private void populateUser(XSSFSheet sheet, Map<String, List<ItemList>> items, String characterType, Row nicknameRow) {
        IntStream.range(INTEGER_FOUR, nicknameRow.getLastCellNum()).forEach(nicknameCellNumber -> {
            Cell nicknameCell = nicknameRow.getCell(nicknameCellNumber, MissingCellPolicy.RETURN_NULL_AND_BLANK);
            String nicknameCellColour = getColourHex(nicknameCell);
            String nickname = getCellValue(nicknameCell);
            if (StringUtils.isNotBlank(nickname)) {
                populateItem(sheet, items, characterType, nicknameCellNumber, nicknameCellColour, nickname);
            }
        });
    }

    private void populateItem(XSSFSheet sheet, Map<String, List<ItemList>> items, String characterType, int nicknameCellNumber, String nicknameCellColour, String nickname) {
        for (int itemRowNumber = NumberUtils.INTEGER_ONE; itemRowNumber < Math.min(INTEGER_OHF, sheet.getLastRowNum()); itemRowNumber++) {
            Row itemRow = sheet.getRow(itemRowNumber);
            String itemId = getItemId(nicknameCellColour, itemRowNumber, itemRow);
            Cell wishNumberCell = itemRow.getCell(nicknameCellNumber, MissingCellPolicy.RETURN_NULL_AND_BLANK);
            String wishNumberCellColour = getColourHex(wishNumberCell);
            String wishNumber = getCellValue(wishNumberCell).replace(StringUtils.SPACE, StringUtils.EMPTY);
            if (StringUtils.isNotBlank(itemId) && StringUtils.isNotBlank(wishNumber)) {
                List<String> wishNumberList = Arrays.asList(wishNumber.replaceAll(DELIMITER_REGEX, DOT).split(DOT_REGEX));
                if (wishNumberList.size() == NumberUtils.INTEGER_ONE && !StringUtils.equals(MARK_HEX_COLOUR, wishNumberCellColour)) {
                    wishNumber = wishNumberList.get(NumberUtils.INTEGER_ZERO).replace(PLUS, StringUtils.EMPTY);
                    populateItemsMap(items, itemId, itemListMapper(characterType, nickname, wishNumber));
                }
                if (wishNumberList.size() == NumberUtils.INTEGER_TWO && !StringUtils.equals(MARK_HEX_COLOUR, wishNumberCellColour)) {
                    wishNumberList.forEach(value -> populateItemsMap(items, itemId, itemListMapper(characterType, nickname, value)));
                } else if (wishNumberList.size() == NumberUtils.INTEGER_TWO && StringUtils.equals(MARK_HEX_COLOUR, wishNumberCellColour) && !StringUtils.contains(wishNumber, PLUS)) {
                    wishNumber = wishNumberList.get(NumberUtils.INTEGER_ONE).replace(PLUS, StringUtils.EMPTY);
                    populateItemsMap(items, itemId, itemListMapper(characterType, nickname, wishNumber));
                }
            }
        }
    }

    private ItemList itemListMapper(String characterType, String nickname, String wishNumber) {
        ItemList itemlist = new ItemList();
        itemlist.setCharacterType(characterType);
        itemlist.setNickname(nickname);
        itemlist.setWishNumber(wishNumber);
        return itemlist;
    }

    private void populateItemsMap(Map<String, List<ItemList>> items, String itemId, ItemList itemlist) {
        List<ItemList> itemLists = new ArrayList<>();
        itemLists.add(itemlist);
        if (items.containsKey(itemId)) {
            items.get(itemId).add(itemlist);
        } else {
            items.put(itemId, itemLists);
        }
    }

    private String getCharacterType(int sheetNumber) {
        if (sheetNumber == NumberUtils.INTEGER_ONE) {
            return CHARACTER_TYPE_CASTER;
        } else if (sheetNumber == NumberUtils.INTEGER_TWO) {
            return CHARACTER_TYPE_MELEE;
        } else {
            throw new IllegalStateException(String.format(UNEXPECTED_VALUE_ERROR_MESSAGE, sheetNumber));
        }
    }

    private String getCellValue(Cell cell) {
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

    private CellType getCellType(Cell cell) {
        CellType cellType;
        try {
            cellType = cell.getCellType();
        } catch (NullPointerException e) {
            cellType = BLANK;
        }
        return cellType;
    }

    private boolean isDecimalPointZero(double numericCellValue) {
        BigDecimal bd = BigDecimal.valueOf((numericCellValue - Math.floor(numericCellValue)) * INTEGER_HUNDRED);
        bd = bd.setScale(INTEGER_FOUR, RoundingMode.HALF_DOWN);
        return bd.intValue() == NumberUtils.INTEGER_ZERO;
    }

    private String getItemId(String nicknameCellColour, int itemRowNumber, Row itemRow) {
        String itemId;
        if (itemRowNumber < INTEGER_SIX) {
            String tokenId = getCellValue(itemRow.getCell(INTEGER_THREE, MissingCellPolicy.RETURN_NULL_AND_BLANK));
            itemId = updateTokenId(nicknameCellColour, tokenId);
        } else {
            itemId = getCellValue(itemRow.getCell(INTEGER_THREE, MissingCellPolicy.RETURN_NULL_AND_BLANK));
        }
        return itemId;
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

    private String getColourHex(Cell wishNumberCell) {
        String hexString = StringUtils.EMPTY;
        CellStyle cellStyle = wishNumberCell.getCellStyle();
        Color color = cellStyle.getFillForegroundColorColor();
        if (ObjectUtils.isNotEmpty(color)) {
            if (color instanceof XSSFColor) {
                hexString = ((XSSFColor) color).getARGBHex();
            } else if (color instanceof HSSFColor && !(color.equals(HSSFColor.HSSFColorPredefined.AUTOMATIC.getColor()))) {
                hexString = ((HSSFColor) color).getHexString();
            }
        }
        return hexString;
    }

    public StringBuilder generateWishlistTable(List<Wishlist> wishlists) {
        log.info("Started writing data to CLMWishlists LuaTable");
        StringBuilder wishlistsSB = new StringBuilder();
        wishlistsSB.append(INIT_EMPTY_CLM_WISHLISTS);
        StringJoiner joinerCharTypeName = new StringJoiner(COMMA);
        wishlists.forEach(wishlist -> {
            String charType = wishlist.getCharType();
            joinerCharTypeName.add(String.format(VALUE_LIST, charType));
            wishlistsSB.append(String.format(INIT_EMPTY_LIST_CLM_WISHLISTS, charType));
            wishlistsSB.append(String.format(INIT_ARRAY_CLM_WISHLISTS, charType));
            StringJoiner joinerNickname = new StringJoiner(COMMA);
            StringJoiner joinerUserData = new StringJoiner(COMMA);
            wishlist.getUserInfos().forEach(userInfoDTO -> {
                StringJoiner joinerUserItem = new StringJoiner(COMMA);
                List<ItemInfoDTO> items = userInfoDTO.getItems();
                IntStream.range(0, items.size()).forEach(i -> {
                    ItemInfoDTO itemInfoDTO = items.get(i);
                    String format = String.format(VALUE_IN_LIST, i + NumberUtils.INTEGER_ONE, itemInfoDTO.getItemId(), itemInfoDTO.getWishNumber(), itemInfoDTO.getMarker(), itemInfoDTO.getBossName());
                    joinerUserItem.add(format);
                });
                String nickname = userInfoDTO.getNickname();
                joinerUserData.add(String.format(ARRAY_NICKNAME, nickname, joinerUserItem));
                joinerNickname.add(String.format(VALUE_LIST, nickname));
            });
            wishlistsSB.append(joinerUserData).append(StringUtils.LF).append(CLOSE_CURLY_BRACES).append(StringUtils.LF);
            wishlistsSB.insert(NumberUtils.INTEGER_ZERO, String.format(ARRAY_CLM_NICKNAME, charType, joinerNickname));
        });
        wishlistsSB.insert(NumberUtils.INTEGER_ZERO, INIT_ARRAY_CLM_NICKNAME);
        wishlistsSB.insert(NumberUtils.INTEGER_ZERO, String.format(INIT_CLM_WISHLISTS_TYPE, joinerCharTypeName));
        log.info("Completed writing data to CLMItems LuaTable");
        return wishlistsSB;
    }

    public StringBuilder generateItemListTable(Map<String, List<ItemList>> clmItems) {
        log.info("Started writing data to CLMItems LuaTable");
        StringBuilder itemsSB = new StringBuilder();
        itemsSB.append(INIT_EMPTY_CLM_ITEMS);
        clmItems.forEach((key, value) -> {
            itemsSB.append(String.format(INIT_CLM_ITEMS, key));
            StringJoiner joiner = new StringJoiner(COMMA);
            IntStream.range(NumberUtils.INTEGER_ZERO, value.size()).forEach(i -> {
                ItemList itemlist = value.get(i);
                joiner.add((String.format(CLM_ITEMS_TEMPLATE, i + NumberUtils.INTEGER_ONE, itemlist.getCharacterType(), itemlist.getNickname(), itemlist.getWishNumber())));
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


