package com.gudim.clm.desktop.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.gudim.clm.desktop.dto.ItemInfoDTO;
import com.gudim.clm.desktop.dto.ItemList;
import com.gudim.clm.desktop.dto.UserInfoDTO;
import com.gudim.clm.desktop.dto.Wishlist;
import com.gudim.clm.desktop.util.CLMConstant;
import com.gudim.clm.desktop.util.CLMUtil;
import com.gudim.clm.desktop.util.GoogleUtil;
import lombok.Data;
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
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.poi.ss.usermodel.CellType.BLANK;

@Service
@Log4j2
@Data
public class CLMService {
    private String accessToken;

    public String downloadXLSXFromDrive() {
        try (OutputStream outputStream = Files.newOutputStream(Paths.get(CLMConstant.TEMP_FILE_NAME))) {
            Drive drive = GoogleUtil.getGoogleDriveData();
            File file = drive.files().get(CLMConstant.SHEETS_ID).setQuotaUser(UUID.randomUUID().toString()).execute();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            drive.files().export(file.getId(), CLMConstant.XLSX_MIME).executeAndDownloadTo(byteArrayOutputStream);
            byteArrayOutputStream.writeTo(outputStream);
            log.info(String.format(CLMConstant.CREATED_TEMP_FILE_MESSAGE, CLMConstant.TEMP_FILE_NAME));
            return "200";
        } catch (IOException | GeneralSecurityException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            GoogleUtil.removeCredential();
            return "404";
        }
    }

    public List<Wishlist> getWishlist() {
        XSSFSheet sheet;
        List<Wishlist> wishlists = new ArrayList<>();
        try (FileInputStream fileInputStream = new FileInputStream(CLMConstant.TEMP_FILE_NAME); XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream)) {
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
        int bound = nicknameRow.getLastCellNum();
        for (int nicknameCellNumber = CLMConstant.INTEGER_FOUR; nicknameCellNumber < bound; nicknameCellNumber++) {
            Cell nicknameCell = nicknameRow.getCell(nicknameCellNumber, MissingCellPolicy.RETURN_NULL_AND_BLANK);
            String nicknameCellColour = getColourHex(nicknameCell);
            String nickname = getCellValue(nicknameCell);
            if (StringUtils.isNotBlank(nickname)) {
                UserInfoDTO userInfoDTO = new UserInfoDTO();
                userInfoDTO.setNickname(nickname);
                populateItemInfos(sheet, nicknameCellNumber, nicknameCellColour, userInfoDTO);
                wishlist.getUserInfos().add(userInfoDTO);
            }
        }
    }

    private void populateItemInfos(XSSFSheet sheet, int nicknameCellNumber, String nicknameCellColour, UserInfoDTO userInfoDTO) {
        for (int itemRowNumber = NumberUtils.INTEGER_ONE; itemRowNumber < Math.min(CLMConstant.INTEGER_OHF, sheet.getLastRowNum()); itemRowNumber++) {
            Row itemRow = sheet.getRow(itemRowNumber);
            String itemId = getItemId(nicknameCellColour, itemRowNumber, itemRow);
            String bossName = getBossName(populateBossNames(sheet), itemRowNumber);
            Cell wishNumberCell = itemRow.getCell(nicknameCellNumber, MissingCellPolicy.RETURN_NULL_AND_BLANK);
            String wishNumberCellColour = getColourHex(wishNumberCell);
            String wishNumber = getCellValue(wishNumberCell).replace(StringUtils.SPACE, StringUtils.EMPTY);
            if (StringUtils.isNotBlank(itemId) && StringUtils.isNotBlank(wishNumber)) {
                List<String> wishNumberList = Arrays.asList(wishNumber.replaceAll(CLMConstant.DELIMITER_REGEX, CLMConstant.DOT).split(CLMConstant.DOT_REGEX));
                List<ItemInfoDTO> itemInfoDTOS = userInfoDTO.getItems();
                boolean isMarked = StringUtils.equals(CLMConstant.MARK_HEX_COLOUR, wishNumberCellColour);
                String wishNumberValue = wishNumberList.get(NumberUtils.INTEGER_ZERO).replace(CLMConstant.PLUS, StringUtils.EMPTY);
                itemInfoDTOS.add(itemInfoMapper(itemId, wishNumberValue, isMarked, bossName));
                if (wishNumberList.size() == NumberUtils.INTEGER_TWO) {
                    isMarked = StringUtils.contains(wishNumber, CLMConstant.PLUS);
                    wishNumberValue = wishNumberList.get(NumberUtils.INTEGER_ONE).replace(CLMConstant.PLUS, StringUtils.EMPTY);
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
        for (int rowNum = NumberUtils.INTEGER_ONE; rowNum < Math.min(CLMConstant.INTEGER_OHF, sheet.getLastRowNum()); rowNum++) {
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
        try (FileInputStream fileInputStream = new FileInputStream(CLMConstant.TEMP_FILE_NAME); XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream)) {
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
        IntStream.range(CLMConstant.INTEGER_FOUR, nicknameRow.getLastCellNum()).forEach(nicknameCellNumber -> {
            Cell nicknameCell = nicknameRow.getCell(nicknameCellNumber, MissingCellPolicy.RETURN_NULL_AND_BLANK);
            String nicknameCellColour = getColourHex(nicknameCell);
            String nickname = getCellValue(nicknameCell);
            if (StringUtils.isNotBlank(nickname)) {
                populateItem(sheet, items, characterType, nicknameCellNumber, nicknameCellColour, nickname);
            }
        });
    }

    private void populateItem(XSSFSheet sheet, Map<String, List<ItemList>> items, String characterType, int nicknameCellNumber, String nicknameCellColour, String nickname) {
        for (int itemRowNumber = NumberUtils.INTEGER_ONE; itemRowNumber < Math.min(CLMConstant.INTEGER_OHF, sheet.getLastRowNum()); itemRowNumber++) {
            Row itemRow = sheet.getRow(itemRowNumber);
            String itemId = getItemId(nicknameCellColour, itemRowNumber, itemRow);
            Cell wishNumberCell = itemRow.getCell(nicknameCellNumber, MissingCellPolicy.RETURN_NULL_AND_BLANK);
            String wishNumberCellColour = getColourHex(wishNumberCell);
            String wishNumber = getCellValue(wishNumberCell).replace(StringUtils.SPACE, StringUtils.EMPTY);
            if (StringUtils.isNotBlank(itemId) && StringUtils.isNotBlank(wishNumber)) {
                List<String> wishNumberList = Arrays.asList(wishNumber.replaceAll(CLMConstant.DELIMITER_REGEX, CLMConstant.DOT).split(CLMConstant.DOT_REGEX));
                if (wishNumberList.size() == NumberUtils.INTEGER_ONE && !StringUtils.equals(CLMConstant.MARK_HEX_COLOUR, wishNumberCellColour)) {
                    wishNumber = wishNumberList.get(NumberUtils.INTEGER_ZERO).replace(CLMConstant.PLUS, StringUtils.EMPTY);
                    populateItemsMap(items, itemId, itemListMapper(characterType, nickname, wishNumber));
                }
                if (wishNumberList.size() == NumberUtils.INTEGER_TWO && !StringUtils.equals(CLMConstant.MARK_HEX_COLOUR, wishNumberCellColour)) {
                    wishNumberList.forEach(value -> populateItemsMap(items, itemId, itemListMapper(characterType, nickname, value)));
                } else if (wishNumberList.size() == NumberUtils.INTEGER_TWO && StringUtils.equals(CLMConstant.MARK_HEX_COLOUR, wishNumberCellColour) && !StringUtils.contains(wishNumber, CLMConstant.PLUS)) {
                    wishNumber = wishNumberList.get(NumberUtils.INTEGER_ONE).replace(CLMConstant.PLUS, StringUtils.EMPTY);
                    populateItemsMap(items, itemId, itemListMapper(characterType, nickname, wishNumber));
                }
            }
        }
    }

    private ItemList itemListMapper(String characterType, String nickname, String wishNumber) {
        ItemList itemlist = new ItemList();
        itemlist.setCharacterType(characterType);
        itemlist.setNickname(nickname);
        itemlist.setWishNumber(Integer.valueOf(wishNumber));
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
            return CLMConstant.CHARACTER_TYPE_CASTER;
        } else if (sheetNumber == NumberUtils.INTEGER_TWO) {
            return CLMConstant.CHARACTER_TYPE_MELEE;
        } else {
            throw new IllegalStateException(String.format(CLMConstant.UNEXPECTED_VALUE_ERROR_MESSAGE, sheetNumber));
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
                    SimpleDateFormat sdf = new SimpleDateFormat(CLMConstant.PATTERN_MM_DD);
                    result = sdf.format(cell.getDateCellValue());
                } else {
                    DecimalFormat decimalFormat = new DecimalFormat(CLMConstant.NUMBER_SIGN);
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
                throw new IllegalStateException(String.format(CLMConstant.UNEXPECTED_VALUE_ERROR_MESSAGE, cellType));
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
        BigDecimal bd = BigDecimal.valueOf((numericCellValue - Math.floor(numericCellValue)) * CLMConstant.INTEGER_HUNDRED);
        bd = bd.setScale(CLMConstant.INTEGER_FOUR, RoundingMode.HALF_DOWN);
        return bd.intValue() == NumberUtils.INTEGER_ZERO;
    }

    private String getItemId(String nicknameCellColour, int itemRowNumber, Row itemRow) {
        String itemId;
        if (itemRowNumber < CLMConstant.INTEGER_SIX) {
            String tokenId = getCellValue(itemRow.getCell(CLMConstant.INTEGER_THREE, MissingCellPolicy.RETURN_NULL_AND_BLANK));
            itemId = updateTokenId(nicknameCellColour, tokenId);
        } else {
            itemId = getCellValue(itemRow.getCell(CLMConstant.INTEGER_THREE, MissingCellPolicy.RETURN_NULL_AND_BLANK));
        }
        return itemId;
    }

    private String updateTokenId(String classColour, String itemId) {
        String result;
        switch (classColour) {
            case CLMConstant.PRIEST_HEX_COLOUR:
            case CLMConstant.PALADIN_HEX_COLOUR:
            case CLMConstant.WARLOCK_HEX_COLOUR:
                result = CLMConstant.PPW_MAP.get(itemId);
                break;
            case CLMConstant.WARRIOR_HEX_COLOUR:
            case CLMConstant.HUNTER_HEX_COLOUR:
            case CLMConstant.SHAMAN_HEX_COLOUR:
                result = CLMConstant.WHS_MAP.get(itemId);
                break;
            case CLMConstant.DRUID_HEX_COLOUR:
            case CLMConstant.MAGE_HEX_COLOUR:
            case CLMConstant.ROGUE_HEX_COLOUR:
            case CLMConstant.DEATH_KNIGHT_HEX_COLOUR:
                result = CLMConstant.DMRD_MAP.get(itemId);
                break;
            default:
                throw new IllegalStateException(String.format(CLMConstant.UNEXPECTED_VALUE_ERROR_MESSAGE, classColour));
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
        wishlistsSB.append(CLMConstant.INIT_EMPTY_CLM_WISHLISTS);
        StringJoiner joinerCharTypeName = new StringJoiner(CLMConstant.COMMA);
        JSONObject itemsInfo = CLMUtil.getJsonObjectFromResource("Item.json");
        wishlists.forEach(wishlist -> {
            String charType = wishlist.getCharType();
            joinerCharTypeName.add(String.format(CLMConstant.VALUE_LIST, charType));
            wishlistsSB.append(String.format(CLMConstant.INIT_EMPTY_LIST_CLM_WISHLISTS, charType));
            wishlistsSB.append(String.format(CLMConstant.INIT_ARRAY_CLM_WISHLISTS, charType));
            StringJoiner joinerNickname = new StringJoiner(CLMConstant.COMMA);
            StringJoiner joinerUserData = new StringJoiner(CLMConstant.COMMA);
            wishlist.getUserInfos().forEach(userInfoDTO -> {
                StringJoiner joinerUserItem = new StringJoiner(CLMConstant.COMMA);
                List<ItemInfoDTO> items = userInfoDTO.getItems();
                IntStream.range(0, items.size()).forEach(i -> {
                    ItemInfoDTO itemInfoDTO = items.get(i);
                    JSONObject itemInfo = itemsInfo.getJSONObject(itemInfoDTO.getItemId());
                    Integer icon = itemInfo.getInt("icon");
                    String name = itemInfo.getString("name");
                    String link = itemInfo.getString("link");
                    String format = String.format(CLMConstant.VALUE_IN_LIST, i + NumberUtils.INTEGER_ONE, itemInfoDTO.getItemId(), icon, name, link, itemInfoDTO.getWishNumber(), itemInfoDTO.getMarker(), itemInfoDTO.getBossName());
                    joinerUserItem.add(format);
                });
                String nickname = userInfoDTO.getNickname();
                joinerUserData.add(String.format(CLMConstant.ARRAY_NICKNAME, nickname, joinerUserItem));
                joinerNickname.add(String.format(CLMConstant.VALUE_LIST, nickname));
            });
            wishlistsSB.append(joinerUserData).append(StringUtils.LF).append(CLMConstant.CLOSE_CURLY_BRACES).append(StringUtils.LF);
            wishlistsSB.insert(NumberUtils.INTEGER_ZERO, String.format(CLMConstant.ARRAY_CLM_NICKNAME, charType, joinerNickname));
        });
        wishlistsSB.insert(NumberUtils.INTEGER_ZERO, CLMConstant.INIT_ARRAY_CLM_NICKNAME);
        wishlistsSB.insert(NumberUtils.INTEGER_ZERO, String.format(CLMConstant.INIT_CLM_WISHLISTS_TYPE, joinerCharTypeName));
        log.info("Completed writing data to CLMItems LuaTable");
        return wishlistsSB;
    }

    public StringBuilder generateItemListTable(Map<String, List<ItemList>> clmItems) {
        log.info("Started writing data to CLMItems LuaTable");
        StringBuilder itemsSB = new StringBuilder();
        itemsSB.append(CLMConstant.INIT_EMPTY_CLM_ITEMS);
        clmItems.entrySet().forEach(entry -> {
            String key = entry.getKey();
            List<ItemList> value = formatItemList(entry);
            itemsSB.append(String.format(CLMConstant.INIT_CLM_ITEMS, key));
            StringJoiner joiner = new StringJoiner(CLMConstant.COMMA);
            IntStream.range(NumberUtils.INTEGER_ZERO, value.size()).forEach(i -> {
                ItemList itemlist = value.get(i);
                joiner.add((String.format(CLMConstant.CLM_ITEMS_TEMPLATE, i + NumberUtils.INTEGER_ONE, itemlist.getCharacterType(), itemlist.getNickname(), itemlist.getWishNumber())));
            });
            itemsSB.append(joiner).append(StringUtils.LF).append(CLMConstant.CLOSE_CURLY_BRACES).append(StringUtils.LF);
        });
        log.info("Completed writing data to CLMItems LuaTable");
        return itemsSB;
    }

    private List<ItemList> formatItemList(Map.Entry<String, List<ItemList>> entry) {
        List<ItemList> value = entry.getValue();
        Set<ItemList> itemListSet = new HashSet<>(value);
        value = new ArrayList<>(itemListSet);
        return value.stream().sorted(Comparator.comparing(ItemList::getCharacterType).thenComparing(ItemList::getWishNumber).thenComparing(ItemList::getNickname)).collect(Collectors.toList());
    }

    @Nullable
    public List<Wishlist> getWishlist(JsonElement jsonElement) {
        Map<String, Map<String, Map<String, ItemInfoDTO>>> stringMapMap = new Gson().fromJson(jsonElement, new TypeToken<Map<String, Map<String, Map<String, ItemInfoDTO>>>>() {
        }.getType());
        List<Wishlist> wishlists = new ArrayList<>();
        for (Map.Entry<String, Map<String, Map<String, ItemInfoDTO>>> charTypeEntry : stringMapMap.entrySet()) {
            Wishlist wishlist = new Wishlist();
            wishlist.setCharType(charTypeEntry.getKey());
            List<UserInfoDTO> userInfos = wishlist.getUserInfos();
            for (Map.Entry<String, Map<String, ItemInfoDTO>> stringMapEntry : charTypeEntry.getValue().entrySet()) {
                UserInfoDTO userInfoDTO = new UserInfoDTO();
                userInfoDTO.setNickname(stringMapEntry.getKey());
                List<ItemInfoDTO> items = userInfoDTO.getItems();
                for (Map.Entry<String, ItemInfoDTO> stringBooleanEntry : stringMapEntry.getValue().entrySet()) {
                    ItemInfoDTO itemInfoDTO = new ItemInfoDTO();
                    itemInfoDTO.setMarker(stringBooleanEntry.getValue().getMarker());
                    itemInfoDTO.setItemId(stringBooleanEntry.getValue().getItemId());
                    itemInfoDTO.setWishNumber(stringBooleanEntry.getKey());
                    items.add(itemInfoDTO);
                }
                userInfos.add(userInfoDTO);
            }
            wishlists.add(wishlist);
        }
        return wishlists;
    }

    public void updateData(List<Wishlist> wishlists) {
        try (FileInputStream fileInputStream = new FileInputStream(CLMConstant.TEMP_FILE_NAME); XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream)) {
            for (int sheetNumber = NumberUtils.INTEGER_ONE; sheetNumber < workbook.getNumberOfSheets(); sheetNumber++) {
                XSSFSheet sheet = workbook.getSheetAt(sheetNumber);
                for (Wishlist wishlist : wishlists) {
                    if (wishlist.getCharType().equals(getCharacterType(sheetNumber))) {
                        Row nicknameRow = sheet.getRow(sheet.getFirstRowNum());
                        for (int nicknameCellNumber = CLMConstant.INTEGER_FOUR; nicknameCellNumber < nicknameRow.getLastCellNum(); nicknameCellNumber++) {
                            Cell nicknameCell = nicknameRow.getCell(nicknameCellNumber, MissingCellPolicy.RETURN_NULL_AND_BLANK);
                            String nicknameCellColour = getColourHex(nicknameCell);
                            String nickname = getCellValue(nicknameCell);
                            for (UserInfoDTO userInfo : wishlist.getUserInfos()) {
                                if (userInfo.getNickname().equals(nickname)) {
                                    for (int itemRowNumber = NumberUtils.INTEGER_ONE; itemRowNumber < Math.min(CLMConstant.INTEGER_OHF, sheet.getLastRowNum()); itemRowNumber++) {
                                        Row itemRow = sheet.getRow(itemRowNumber);
                                        String itemId = getItemId(nicknameCellColour, itemRowNumber, itemRow);
                                        for (ItemInfoDTO item : userInfo.getItems()) {
                                            if (item.getItemId().equals(itemId)) {
                                                Cell wishNumberCell = itemRow.getCell(nicknameCellNumber, MissingCellPolicy.RETURN_NULL_AND_BLANK);
                                                String wishNumberCellColour = getColourHex(wishNumberCell);
                                                String wishNumber = getCellValue(wishNumberCell);
                                                if (StringUtils.isNotBlank(itemId) && StringUtils.isNotBlank(wishNumber)) {
                                                    String[] wishNumberList = wishNumber.replaceAll(CLMConstant.DELIMITER_REGEX, CLMConstant.DOT).split(CLMConstant.DOT_REGEX);
                                                    for (String w : wishNumberList) {

                                                    }

                                                    boolean isMarked = StringUtils.equals(CLMConstant.MARK_HEX_COLOUR, wishNumberCellColour);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


