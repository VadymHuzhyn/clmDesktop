package com.gudim.clm.desktop.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gudim.clm.desktop.exception.FileNonFoundException;
import com.gudim.clm.desktop.model.Character;
import com.gudim.clm.desktop.model.Item;
import com.gudim.clm.desktop.model.Wish;
import com.gudim.clm.desktop.util.CLMConstant;
import com.gudim.clm.desktop.util.GoogleUtil;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.gudim.clm.desktop.util.CLMConstant.COMMA;
import static com.gudim.clm.desktop.util.CLMConstant.DOT_REGEX;
import static com.gudim.clm.desktop.util.CLMConstant.FILE_NAME;
import static com.gudim.clm.desktop.util.CLMConstant.NICKNAME_CELL;
import static com.gudim.clm.desktop.util.CLMConstant.ROW_END_FIRST_VALUE;
import static com.gudim.clm.desktop.util.CLMConstant.SHEETS_ID;
import static com.gudim.clm.desktop.util.CLMConstant.XLSX_MIME;

@Service
public class CLMService {


    public static String getCharacterTypeName(int sheetNumber) {
        if (sheetNumber == NumberUtils.INTEGER_ZERO) {
            return "heal";
        } else if (sheetNumber == NumberUtils.INTEGER_ONE) {
            return "caster";
        } else if (sheetNumber == NumberUtils.INTEGER_TWO) {
            return "dd";
        } else {
            throw new IllegalStateException("Unexpected value: " + sheetNumber);
        }
    }

    public static String getCellValue(Cell cell) {
        Object result;
        switch (cell.getCellType()) {
            case STRING:
                result = cell.getRichStringCellValue().getString();
                break;
            case NUMERIC:
                result = cell.getNumericCellValue();
                break;
            case BOOLEAN:
                result = cell.getBooleanCellValue();
                break;
            case FORMULA:
                result = cell.getCellFormula();
                break;
            case _NONE:
            case BLANK:
                result = StringUtils.EMPTY;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + cell.getCellType());
        }
        return result.toString();
    }

    public java.io.File getFile() {
        return new DirectoryChooser().showDialog(new Stage());
    }

    public void downloadXLSXFromDrive() {
        try (OutputStream outputStream = Files.newOutputStream(Paths.get(FILE_NAME))) {
            Drive drive = GoogleUtil.getGoogleDriveData();
            File file = drive.files().get(SHEETS_ID).setQuotaUser(UUID.randomUUID().toString()).execute();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            drive.files().export(file.getId(), XLSX_MIME).executeAndDownloadTo(byteArrayOutputStream);
            byteArrayOutputStream.writeTo(outputStream);
        } catch (IOException e) {
            throw new FileNonFoundException(ExceptionUtils.getStackTrace(e));
        }
    }

    public String convertXLSXToJson() {
        String payloadStr = StringUtils.EMPTY;
        XSSFSheet sheet;
        try (FileInputStream fileInputStream = new FileInputStream(
                FILE_NAME); XSSFWorkbook workbook = new XSSFWorkbook(
                fileInputStream)) {
            Wish wish = new Wish();
            for (int sheetNumber = 2; sheetNumber < workbook.getNumberOfSheets();
                 sheetNumber++) { //todo change sheetNumber to NumberUtils.INTEGER_ZERO
                wish.setCharacterTypeName(getCharacterTypeName(sheetNumber));
                sheet = workbook.getSheetAt(sheetNumber);
                List<Character> characterList = new ArrayList<>();
                Row nicknameRow = sheet.getRow(sheet.getFirstRowNum());
                for (int nicknameCell = NICKNAME_CELL; nicknameCell < nicknameRow.getLastCellNum();
                     nicknameCell++) {
                    List<Item> itemList = new ArrayList<>();
                    for (int rowNum = sheet.getFirstRowNum() + NumberUtils.INTEGER_ONE;
                         rowNum < Math.min(ROW_END_FIRST_VALUE, sheet.getLastRowNum());
                         rowNum++) {
                        populateItem(sheet, nicknameCell, itemList, rowNum);
                    }
                    if (!itemList.isEmpty()) {
                        Character character = new Character();
                        character.setNickname(getCellValue(nicknameRow.getCell(nicknameCell,
                                Row.MissingCellPolicy.RETURN_NULL_AND_BLANK)));
                        character.setItem(itemList);
                        characterList.add(character);
                    }
                }
                wish.setCharacters(characterList);
                Gson gson = new GsonBuilder().create();
                payloadStr = gson.toJson(wish);
            }
        } catch (IOException e) {
            throw new FileNonFoundException(ExceptionUtils.getStackTrace(e));
        }
        return payloadStr;
    }

    private void populateItem(XSSFSheet sheet, int nicknameCell, List<Item> itemList, int rowNum) {
        Item item = new Item();
        Row itemRow = sheet.getRow(rowNum);
        Cell itemIdCell = itemRow.getCell(NumberUtils.INTEGER_TWO,
                MissingCellPolicy.RETURN_NULL_AND_BLANK);
        Cell wishNumberCell = itemRow.getCell(nicknameCell,
                MissingCellPolicy.RETURN_NULL_AND_BLANK);
        if (ObjectUtils.isEmpty(itemIdCell) || ObjectUtils.isEmpty(wishNumberCell)) {
            return;
        }
        List<String> split = Arrays.asList(getCellValue(wishNumberCell).split(DOT_REGEX));
        String wishNumber;
        if (split.size() == NumberUtils.INTEGER_TWO && !CLMConstant.STRING_ZERO.equals(
                split.get(split.size() - NumberUtils.INTEGER_ONE))) {
            wishNumber = split.get(NumberUtils.INTEGER_ZERO)
                    + COMMA + split.get(
                    NumberUtils.INTEGER_ONE);
        } else {
            wishNumber = split.get(
                    NumberUtils.INTEGER_ZERO);
        }
        if (StringUtils.isNotEmpty(wishNumber)) {
            item.setWishNumber(wishNumber);
            item.setItemId(getCellValue(itemIdCell));
            itemList.add(item);
        }
    }

    public void generateLuaTableWishlist(String addonPath, String wishlistJSON) {
        Globals gl = JsePlatform.standardGlobals();
        gl.loadfile("script/decodeLuaTable.lua").call();
        String path = addonPath + "\\wishlist.lua";
        gl.get("printTable").call(LuaValue.valueOf(wishlistJSON), LuaValue.valueOf(path));
    }
}


