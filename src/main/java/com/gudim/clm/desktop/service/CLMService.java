package com.gudim.clm.desktop.service;

import static com.gudim.clm.desktop.util.CLMConstant.CHARACTER_TYPE_CASTER;
import static com.gudim.clm.desktop.util.CLMConstant.CHARACTER_TYPE_DD;
import static com.gudim.clm.desktop.util.CLMConstant.CHARACTER_TYPE_HEAL;
import static com.gudim.clm.desktop.util.CLMConstant.CHARACTER_TYPE_TANK;
import static com.gudim.clm.desktop.util.CLMConstant.CLM_ITEMS_TEMPLATE;
import static com.gudim.clm.desktop.util.CLMConstant.CLOSE_CURLY_BRACES;
import static com.gudim.clm.desktop.util.CLMConstant.COMMA;
import static com.gudim.clm.desktop.util.CLMConstant.DOT_REGEX;
import static com.gudim.clm.desktop.util.CLMConstant.INIT_ARRAY_CLM_WISHLISTS;
import static com.gudim.clm.desktop.util.CLMConstant.INIT_CLM_ITEMS;
import static com.gudim.clm.desktop.util.CLMConstant.INIT_CLM_WISHLISTS_TYPE;
import static com.gudim.clm.desktop.util.CLMConstant.INIT_EMPTY_ARRAY_CLM_WISHLISTS;
import static com.gudim.clm.desktop.util.CLMConstant.INIT_EMPTY_CLM_ITEMS;
import static com.gudim.clm.desktop.util.CLMConstant.INIT_EMPTY_CLM_WISHLISTS;
import static com.gudim.clm.desktop.util.CLMConstant.INIT_EMPTY_LIST_CLM_WISHLISTS;
import static com.gudim.clm.desktop.util.CLMConstant.INT_100;
import static com.gudim.clm.desktop.util.CLMConstant.INT_150;
import static com.gudim.clm.desktop.util.CLMConstant.INT_3;
import static com.gudim.clm.desktop.util.CLMConstant.INT_4;
import static com.gudim.clm.desktop.util.CLMConstant.MESSAGE_HAS_BEEN_REMOVED;
import static com.gudim.clm.desktop.util.CLMConstant.REMOVE_FILE_ERROR_MESSAGE;
import static com.gudim.clm.desktop.util.CLMConstant.SAVE_LUA_TABLE;
import static com.gudim.clm.desktop.util.CLMConstant.SAVE_LUA_TABLE_ERROR;
import static com.gudim.clm.desktop.util.CLMConstant.SHEETS_ID;
import static com.gudim.clm.desktop.util.CLMConstant.TEMP_FILE_NAME;
import static com.gudim.clm.desktop.util.CLMConstant.UNEXPECTED_VALUE_ERROR_MESSAGE;
import static com.gudim.clm.desktop.util.CLMConstant.VALUE_IN_LIST;
import static com.gudim.clm.desktop.util.CLMConstant.XLSX_MIME;
import static org.apache.poi.ss.usermodel.CellType.BLANK;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.gudim.clm.desktop.dto.CLMLuaTableDTO;
import com.gudim.clm.desktop.dto.CLMMapDTO;
import com.gudim.clm.desktop.dto.CLMUserInfoDTO;
import com.gudim.clm.desktop.util.GoogleUtil;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
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
		} else if (sheetNumber == INT_3) {
			return CHARACTER_TYPE_TANK;
		} else {
			throw new IllegalStateException(
				String.format(UNEXPECTED_VALUE_ERROR_MESSAGE, sheetNumber));
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
				DecimalFormat decimalFormat = new DecimalFormat("#");
				decimalFormat.setRoundingMode(RoundingMode.CEILING);
				result = isDecimalPointZero(cell.getNumericCellValue()) ? decimalFormat.format(
					cell.getNumericCellValue()) : Double.valueOf(cell.getNumericCellValue());
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
				log.error(String.format(UNEXPECTED_VALUE_ERROR_MESSAGE, cellType));
		}
		return result.toString();
	}
	
	private static boolean isDecimalPointZero(double numericCellValue) {
		BigDecimal bd = BigDecimal.valueOf(
			(numericCellValue - Math.floor(numericCellValue)) * INT_100);
		bd = bd.setScale(INT_4, RoundingMode.HALF_DOWN);
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
			File file = drive.files().get(SHEETS_ID).setQuotaUser(UUID.randomUUID().toString())
			                 .execute();
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			drive.files().export(file.getId(), XLSX_MIME)
			     .executeAndDownloadTo(byteArrayOutputStream);
			byteArrayOutputStream.writeTo(outputStream);
			log.info(String.format("Temp file %s has been created", TEMP_FILE_NAME));
		} catch (IOException | GeneralSecurityException e) {
			log.error(ExceptionUtils.getStackTrace(e));
		}
	}
	
	public CLMMapDTO getDataFromXLSX() {
		XSSFSheet sheet;
		CLMMapDTO clmMapDTO = new CLMMapDTO();
		HashMap<String, List<CLMUserInfoDTO>> clmItemMap = new HashMap<>();
		HashMap<String, HashMap<String, List<CLMUserInfoDTO>>> clmWishlistMap = new HashMap<>();
		log.info("Start parse xlsx");
		try (FileInputStream fileInputStream = new FileInputStream(TEMP_FILE_NAME);
		     XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream)) {
			for (int sheetNumber = 1; sheetNumber < workbook.getNumberOfSheets(); sheetNumber++) {
				String characterType = getCharacterType(sheetNumber);
				HashMap<String, List<CLMUserInfoDTO>> wishlistMap = new HashMap<>();
				sheet = workbook.getSheetAt(sheetNumber);
				Row nicknameRow = sheet.getRow(sheet.getFirstRowNum());
				for (int nicknameCell = INT_3; nicknameCell < nicknameRow.getLastCellNum();
				     nicknameCell++) {
					Cell nicknameCellValue = nicknameRow.getCell(nicknameCell,
					                                             MissingCellPolicy.RETURN_NULL_AND_BLANK);
					String nickname = getCellValue(nicknameCellValue);
					List<CLMUserInfoDTO> clmWishlistDTOList = new ArrayList<>();
					for (int rowNum = sheet.getFirstRowNum() + NumberUtils.INTEGER_ONE;
					     rowNum < Math.min(INT_150, sheet.getLastRowNum()); rowNum++) {
						List<CLMUserInfoDTO> clmItemDTOList = new ArrayList<>();
						CLMUserInfoDTO clmWishlistDTO = new CLMUserInfoDTO();
						Row itemRow = sheet.getRow(rowNum);
						CLMUserInfoDTO clmItemDTO = new CLMUserInfoDTO();
						clmItemDTO.setNickname(nickname);
						clmItemDTO.setCharacterType(characterType);
						String itemId = getCellValue(itemRow.getCell(NumberUtils.INTEGER_TWO,
						                                             MissingCellPolicy.RETURN_NULL_AND_BLANK));
						String wishNumber = getCellValue(
							itemRow.getCell(nicknameCell, MissingCellPolicy.RETURN_NULL_AND_BLANK));
						if (isValidCellData(itemId, wishNumber, nickname)) {
							setWishNumber(clmItemDTO, clmWishlistDTO, wishNumber);
							clmItemDTO.setItemId(itemId);
							clmWishlistDTO.setItemId(itemId);
							clmWishlistDTOList.add(clmWishlistDTO);
							updateItemsMap(clmItemMap, clmItemDTO, clmItemDTOList, itemId);
						}
					}
					if (StringUtils.isNotBlank(nickname) && CollectionUtils.isNotEmpty(
						clmWishlistDTOList)) {
						wishlistMap.put(nickname, clmWishlistDTOList);
					}
				}
				clmWishlistMap.put(characterType, wishlistMap);
			}
		} catch (IOException e) {
			log.error(ExceptionUtils.getStackTrace(e));
		}
		clmMapDTO.setClmWishlistMap(clmWishlistMap);
		log.info("Data for wishlist populated");
		clmMapDTO.setClmItemMap(clmItemMap);
		log.info("Data for items populated");
		return clmMapDTO;
	}
	
	private void updateItemsMap(Map<String, List<CLMUserInfoDTO>> clmItemMap,
	                            CLMUserInfoDTO clmItemDTO, List<CLMUserInfoDTO> clmItemDTOList,
	                            String itemId) {
		clmItemDTOList.add(clmItemDTO);
		if (clmItemMap.containsKey(itemId)) {
			clmItemMap.get(itemId).add(clmItemDTO);
		} else {
			clmItemMap.putIfAbsent(itemId, clmItemDTOList);
		}
	}
	
	private void setWishNumber(CLMUserInfoDTO clmItemDTO, CLMUserInfoDTO clmWishlistDTO,
	                           String wishNumber) {
		List<String> splitCellValue = Arrays.asList(wishNumber.split(DOT_REGEX));
		String wishNumberDTO = splitCellValue.get(NumberUtils.INTEGER_ZERO);
		clmItemDTO.setWishNumber(wishNumberDTO);
		clmWishlistDTO.setWishNumber(wishNumberDTO);
		if (splitCellValue.size() == NumberUtils.INTEGER_TWO) {
			wishNumberDTO = splitCellValue.get(NumberUtils.INTEGER_ONE);
			clmItemDTO.setSecondWishNumber(wishNumberDTO);
			clmWishlistDTO.setSecondWishNumber(wishNumberDTO);
		}
	}
	
	private boolean isValidCellData(String itemId, String wishNumber, String nickname) {
		return StringUtils.isNotBlank(itemId)
			&& StringUtils.isNotBlank(wishNumber)
			&& StringUtils.isNotBlank(nickname);
	}
	
	public CLMLuaTableDTO luaTableMapper(CLMMapDTO clmMapDTO) {
		CLMLuaTableDTO clmLuaTableDTO = new CLMLuaTableDTO();
		StringBuilder sbWishlists = new StringBuilder();
		StringBuilder sbCLMItems = new StringBuilder();
		populateSbCLMItems(clmMapDTO, sbCLMItems);
		clmLuaTableDTO.setSbCLMItems(sbCLMItems);
		populateSbWishlist(clmMapDTO, sbWishlists);
		clmLuaTableDTO.setSbWishlists(sbWishlists);
		return clmLuaTableDTO;
	}
	
	private void populateSbCLMItems(CLMMapDTO clmMapDTO, StringBuilder sbCLMItems) {
		log.info("Started writing data to CLMItems LuaTable");
		sbCLMItems.append(INIT_EMPTY_CLM_ITEMS).append(StringUtils.LF);
		clmMapDTO.getClmItemMap().forEach((key, value) -> {
			sbCLMItems.append(String.format(INIT_CLM_ITEMS, key));
			int bound = value.size();
			IntStream.range(NumberUtils.INTEGER_ZERO, bound).forEach(i -> {
				CLMUserInfoDTO clmUserInfoDTO = value.get(i);
				int listNumber = i + NumberUtils.INTEGER_ONE;
				if (StringUtils.isBlank(clmUserInfoDTO.getSecondWishNumber())) {
					sbCLMItems.append(String.format(CLM_ITEMS_TEMPLATE, listNumber,
					                                clmUserInfoDTO.getCharacterType(),
					                                clmUserInfoDTO.getNickname(),
					                                clmUserInfoDTO.getWishNumber()));
				} else if (StringUtils.isNotBlank(clmUserInfoDTO.getSecondWishNumber())) {
					sbCLMItems.append(
						String.format(CLM_ITEMS_TEMPLATE, listNumber + NumberUtils.INTEGER_ONE,
						              clmUserInfoDTO.getCharacterType(),
						              clmUserInfoDTO.getNickname(),
						              clmUserInfoDTO.getSecondWishNumber()));
				}
				sbCLMItems.append(listNumber == value.size() ? CLOSE_CURLY_BRACES : COMMA);
			});
			sbCLMItems.append(StringUtils.LF);
		});
		log.info("Completed writing data to CLMItems LuaTable");
	}
	
	private void populateSbWishlist(CLMMapDTO clmMapDTO, StringBuilder sbWishlists) {
		log.info("Started writing data to CLMWishlists LuaTable");
		sbWishlists.append(INIT_EMPTY_CLM_WISHLISTS).append(StringUtils.LF);
		sbWishlists.append(
			String.format(INIT_CLM_WISHLISTS_TYPE, CHARACTER_TYPE_HEAL, CHARACTER_TYPE_DD,
			              CHARACTER_TYPE_CASTER, CHARACTER_TYPE_TANK, StringUtils.LF));
		Map<String, HashMap<String, List<CLMUserInfoDTO>>> clmWishlistMap = clmMapDTO.getClmWishlistMap();
		clmWishlistMap.forEach(
			(characterType, mapEntryValue) -> mapEntryValue.forEach((nickname, value) -> {
				sbWishlists.append(
					String.format(INIT_EMPTY_LIST_CLM_WISHLISTS, nickname, StringUtils.LF));
				sbWishlists.append(
					String.format(INIT_EMPTY_ARRAY_CLM_WISHLISTS, nickname, characterType,
					              StringUtils.LF));
				sbWishlists.append(
					String.format(INIT_ARRAY_CLM_WISHLISTS, nickname, characterType));
				value.forEach(clmUserInfoDTO -> {
					String itemId = clmUserInfoDTO.getItemId();
					sbWishlists.append(
						String.format(VALUE_IN_LIST, clmUserInfoDTO.getWishNumber(), itemId));
					String secondWishNumber = clmUserInfoDTO.getSecondWishNumber();
					if (StringUtils.isNotBlank(secondWishNumber)) {
						sbWishlists.append(String.format(VALUE_IN_LIST, secondWishNumber, itemId));
					}
				});
				String convertedSBWishlists = sbWishlists.toString();
				if (COMMA.equals(convertedSBWishlists.substring(
					convertedSBWishlists.length() - NumberUtils.INTEGER_ONE))) {
					sbWishlists.deleteCharAt(sbWishlists.length() - NumberUtils.INTEGER_ONE);
				}
				sbWishlists.append(CLOSE_CURLY_BRACES).append(StringUtils.LF);
			}));
		log.info("Completed writing data to CLMItems LuaTable");
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


