package com.gudim.clm.desktop.service;

import static com.gudim.clm.desktop.util.CLMConstant.AFFEX_ROW;
import static com.gudim.clm.desktop.util.CLMConstant.CHARACTER_TYPE_CASTER;
import static com.gudim.clm.desktop.util.CLMConstant.CHARACTER_TYPE_DD;
import static com.gudim.clm.desktop.util.CLMConstant.CHARACTER_TYPE_HEAL;
import static com.gudim.clm.desktop.util.CLMConstant.CHARACTER_TYPE_TANK;
import static com.gudim.clm.desktop.util.CLMConstant.CHAR_DOT;
import static com.gudim.clm.desktop.util.CLMConstant.CLM_WISHLISTS_INIT;
import static com.gudim.clm.desktop.util.CLMConstant.CLM_WISHLISTS_START_ROW;
import static com.gudim.clm.desktop.util.CLMConstant.CLM_WISHLISTS_TYPE_INIT;
import static com.gudim.clm.desktop.util.CLMConstant.CLOSE_ROW;
import static com.gudim.clm.desktop.util.CLMConstant.COMMA;
import static com.gudim.clm.desktop.util.CLMConstant.DOT_REGEX;
import static com.gudim.clm.desktop.util.CLMConstant.END_ROW_EMPTY;
import static com.gudim.clm.desktop.util.CLMConstant.END_ROW_INDEX;
import static com.gudim.clm.desktop.util.CLMConstant.FILE_NAME;
import static com.gudim.clm.desktop.util.CLMConstant.MESSAGE_HAS_BEEN_REMOVED;
import static com.gudim.clm.desktop.util.CLMConstant.MIDDLE_ROW;
import static com.gudim.clm.desktop.util.CLMConstant.NICKNAME_CELL;
import static com.gudim.clm.desktop.util.CLMConstant.REMOVE_FILE_ERROR_MESSAGE;
import static com.gudim.clm.desktop.util.CLMConstant.ROW_END_FIRST_VALUE;
import static com.gudim.clm.desktop.util.CLMConstant.SAVE_LUA_TABLE_ERROR;
import static com.gudim.clm.desktop.util.CLMConstant.SHEETS_ID;
import static com.gudim.clm.desktop.util.CLMConstant.START_ROW_INDEX;
import static com.gudim.clm.desktop.util.CLMConstant.UNEXPECTED_VALUE_ERROR_MESSAGE;
import static com.gudim.clm.desktop.util.CLMConstant.XLSX_MIME;
import static org.apache.poi.ss.usermodel.CellType.BLANK;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.gudim.clm.desktop.dto.LuaTableDTO;
import com.gudim.clm.desktop.dto.UserWishDTO;
import com.gudim.clm.desktop.util.CLMConstant;
import com.gudim.clm.desktop.util.GoogleUtil;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class CLMService {
	
	private static String getCharacterType(int sheetNumber) {
		if (sheetNumber == NumberUtils.INTEGER_ZERO) {
			return CHARACTER_TYPE_HEAL;
		} else if (sheetNumber == NumberUtils.INTEGER_ONE) {
			return CHARACTER_TYPE_DD;
		} else if (sheetNumber == NumberUtils.INTEGER_TWO) {
			return CHARACTER_TYPE_CASTER;
		} else if (sheetNumber == 3) {
			return CHARACTER_TYPE_TANK;
		} else {
			throw new IllegalStateException(
				UNEXPECTED_VALUE_ERROR_MESSAGE + StringUtils.SPACE + sheetNumber);
		}
	}
	
	private static String getCellValue(Cell cell) {
		Object result;
		CellType cellType = getCellType(cell);
		switch (cellType) {
			case STRING:
				result = cell.getStringCellValue();
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
			case ERROR:
			case BLANK:
				result = StringUtils.EMPTY;
				break;
			default:
				throw new IllegalStateException(
					UNEXPECTED_VALUE_ERROR_MESSAGE + StringUtils.SPACE + cellType);
		}
		return result.toString();
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
		try (OutputStream outputStream = Files.newOutputStream(Paths.get(FILE_NAME))) {
			Drive drive = GoogleUtil.getGoogleDriveData();
			File file = drive.files().get(SHEETS_ID).setQuotaUser(UUID.randomUUID().toString())
			                 .execute();
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			drive.files().export(file.getId(), XLSX_MIME)
			     .executeAndDownloadTo(byteArrayOutputStream);
			byteArrayOutputStream.writeTo(outputStream);
		} catch (IOException | GeneralSecurityException e) {
			log.error(ExceptionUtils.getStackTrace(e));
		}
	}
	
	public LuaTableDTO luaTableMapper() {
		LuaTableDTO luaTableDTO = new LuaTableDTO();
		StringBuilder sbWishlists = new StringBuilder();
		StringBuilder sbCLMItems = new StringBuilder();
		HashMap<String, List<UserWishDTO>> clmItemsMap = new HashMap<>();
		XSSFSheet sheet;
		try (FileInputStream fileInputStream = new FileInputStream(FILE_NAME);
		     XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream)) {
			sbWishlists.append(CLM_WISHLISTS_INIT).append(StringUtils.LF);
			sbWishlists.append(CLM_WISHLISTS_TYPE_INIT).append(StringUtils.LF);
			for (int sheetNumber = 1; sheetNumber < workbook.getNumberOfSheets(); sheetNumber++) {
				String characterType = getCharacterType(sheetNumber);
				sheet = workbook.getSheetAt(sheetNumber);
				Row nicknameRow = sheet.getRow(sheet.getFirstRowNum());
				for (int nicknameCell = NICKNAME_CELL; nicknameCell < nicknameRow.getLastCellNum();
				     nicknameCell++) {
					String nickname = getCellValue(
						nicknameRow.getCell(nicknameCell, MissingCellPolicy.RETURN_NULL_AND_BLANK));
					if (StringUtils.isNotEmpty(nickname)) {
						sbWishlists.append(CLM_WISHLISTS_START_ROW).append(nickname)
						           .append(END_ROW_EMPTY).append(StringUtils.LF);
						sbWishlists.append(CLM_WISHLISTS_START_ROW).append(nickname)
						           .append(MIDDLE_ROW).append(characterType).append(END_ROW_EMPTY)
						           .append(StringUtils.LF);
						sbWishlists.append(CLM_WISHLISTS_START_ROW).append(nickname)
						           .append(MIDDLE_ROW).append(characterType).append(AFFEX_ROW);
						for (int rowNum = sheet.getFirstRowNum() + NumberUtils.INTEGER_ONE;
						     rowNum < Math.min(ROW_END_FIRST_VALUE, sheet.getLastRowNum());
						     rowNum++) {
							UserWishDTO userWishDTO = new UserWishDTO();
							userWishDTO.setNickname(nickname);
							userWishDTO.setCharacterType(characterType);
							List<UserWishDTO> userWishDTOList = new ArrayList<>();
							Row itemRow = sheet.getRow(rowNum);
							Cell itemIdCell = itemRow.getCell(NumberUtils.INTEGER_TWO,
							                                  MissingCellPolicy.RETURN_NULL_AND_BLANK);
							Cell wishNumberCell = itemRow.getCell(nicknameCell,
							                                      MissingCellPolicy.RETURN_NULL_AND_BLANK);
							String itemId = getCellValue(itemIdCell);
							String wishNumber = getCellValue(wishNumberCell);
							if (StringUtils.isNotBlank(itemId) && StringUtils.isNotBlank(
								wishNumber)) {
								itemId = cropIncorrectString(itemId);
								List<String> splitCellValue = Arrays.asList(
									cropIncorrectString(wishNumber).split(DOT_REGEX));
								String wishNumberDTO = splitCellValue.get(NumberUtils.INTEGER_ZERO);
								sbWishlists.append(START_ROW_INDEX).append(wishNumberDTO)
								           .append(END_ROW_INDEX).append(itemId).append(COMMA);
								userWishDTO.setWishNumber(wishNumberDTO);
								if (splitCellValue.size() == NumberUtils.INTEGER_TWO
									&& !CLMConstant.STRING_ZERO.equals(splitCellValue.get(
									splitCellValue.size() - NumberUtils.INTEGER_ONE))) {
									wishNumberDTO = splitCellValue.get(NumberUtils.INTEGER_ONE);
									sbWishlists.append(START_ROW_INDEX).append(wishNumberDTO)
									           .append(END_ROW_INDEX).append(itemId).append(COMMA);
									userWishDTO.setSecondWishNumber(wishNumberDTO);
								}
								userWishDTOList.add(userWishDTO);
								if (clmItemsMap.containsKey(itemId)) {
									clmItemsMap.get(itemId).add(userWishDTO);
								} else {
									clmItemsMap.put(itemId, userWishDTOList);
								}
							}
						}
						String convertedSBWishlists = sbWishlists.toString();
						if (COMMA.equals(convertedSBWishlists.substring(
							convertedSBWishlists.length() - NumberUtils.INTEGER_ONE))) {
							sbWishlists.deleteCharAt(
								sbWishlists.length() - NumberUtils.INTEGER_ONE);
						}
						sbWishlists.append(CLOSE_ROW).append(StringUtils.LF);
					}
				}
			}
			sbCLMItems.append("CLM_items = {}").append(StringUtils.LF);
			for (Entry<String, List<UserWishDTO>> entry : clmItemsMap.entrySet()) {
				sbCLMItems.append("CLM_items[").append(entry.getKey()).append("] = {");
				List<UserWishDTO> value = entry.getValue();
				for (int i = 0; i < value.size(); i++) {
					UserWishDTO userWishDTO = value.get(i);
					int listNumber = i + 1;
					if (StringUtils.isBlank(userWishDTO.getSecondWishNumber())) {
						sbCLMItems.append(START_ROW_INDEX).append(listNumber)
						          .append("] = {[\"characterType\"] = \"")
						          .append(userWishDTO.getCharacterType()).append("\",")
						          .append("[\"nickname\"] = \"").append(userWishDTO.getNickname())
						          .append("\",").append("[\"wishNumber\"] = \"")
						          .append(userWishDTO.getWishNumber()).append("\"").append("}");
					} else if (StringUtils.isNotBlank(userWishDTO.getSecondWishNumber())) {
						sbCLMItems.append(START_ROW_INDEX).append(listNumber + 1)
						          .append("] = {[\"characterType\"] = \"")
						          .append(userWishDTO.getCharacterType()).append("\",")
						          .append("[\"nickname\"] = \"").append(userWishDTO.getNickname())
						          .append("\",").append("[\"wishNumber\"] = \"")
						          .append(userWishDTO.getSecondWishNumber()).append("\"")
						          .append("}");
					}
					sbCLMItems.append(listNumber == value.size() ? "}" : COMMA);
				}
				sbCLMItems.append(StringUtils.LF);
			}
			luaTableDTO.setSbWishlists(sbWishlists);
			luaTableDTO.setSbCLMItems(sbCLMItems);
		} catch (IOException e) {
			log.error(ExceptionUtils.getStackTrace(e));
		}
		return luaTableDTO;
	}
	
	public void saveLuaTableFile(StringBuilder stringBuilder, String path) {
		java.io.File file = new java.io.File(path);
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.append(stringBuilder);
		} catch (IOException e) {
			log.error(String.format(SAVE_LUA_TABLE_ERROR, stringBuilder, path));
		}
	}
	
	private String cropIncorrectString(String incorrectString) {
		Character charDot = CHAR_DOT;
		if (charDot.equals(
			incorrectString.charAt(incorrectString.length() - NumberUtils.INTEGER_TWO))) {
			incorrectString = incorrectString.substring(NumberUtils.INTEGER_ZERO,
			                                            incorrectString.length()
				                                            - NumberUtils.INTEGER_TWO);
		}
		return incorrectString;
	}
	
	public void removeTempFile() {
		try {
			Files.delete(Paths.get(FILE_NAME));
			log.info(FILE_NAME + StringUtils.SPACE + MESSAGE_HAS_BEEN_REMOVED);
		} catch (IOException e) {
			log.error(FILE_NAME + StringUtils.SPACE + REMOVE_FILE_ERROR_MESSAGE);
		}
	}
}


