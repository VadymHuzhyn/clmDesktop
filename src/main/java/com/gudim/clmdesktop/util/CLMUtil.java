package com.gudim.clmdesktop.util;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

import static com.gudim.clmdesktop.util.CLMConstant.*;

@UtilityClass
@Log4j2
public class CLMUtil {

    public void removeFile(String fileName) {
        try {
            File file = new File(fileName);
            BasicFileAttributes basicFileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            if (basicFileAttributes.isDirectory()) {
                FileUtils.deleteDirectory(file);
            } else if (basicFileAttributes.isRegularFile()) {
                Files.delete(file.toPath());
                log.info(String.format(MESSAGE_HAS_BEEN_REMOVED, TEMP_FILE_NAME));
            }
        } catch (IOException e) {
            log.error(String.format(REMOVE_FILE_ERROR_MESSAGE, TEMP_FILE_NAME));
        }
    }

    public JSONObject getJsonObjectFromResource(String fileName) {
        String itemsInfo = StringUtils.EMPTY;
        try (InputStream inputStream = CLMUtil.class.getClassLoader().getResourceAsStream(fileName)) {
            itemsInfo = IOUtils.toString(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error(String.format("Can`t get file %s", fileName));
        }
        return new JSONObject(itemsInfo);
    }

    public void saveLuaTableFile(StringBuilder stringBuilder, String path) {
        File file = new File(path);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.append(stringBuilder);
            log.info(String.format(SAVE_LUA_TABLE, path));
        } catch (IOException e) {
            log.error(String.format(SAVE_LUA_TABLE_ERROR, stringBuilder, path));
        }
    }
}
