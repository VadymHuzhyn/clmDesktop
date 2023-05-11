package com.gudim.clm.desktop.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

import static com.gudim.clm.desktop.util.CLMConstant.*;

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

    public void saveFile(StringBuilder stringBuilder, String filePath) {
        Path path = Paths.get(filePath);
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.append(stringBuilder);
            log.info(String.format(SAVE_FILE, path));
        } catch (IOException e) {
            log.error(String.format(SAVE_FILE_ERROR, stringBuilder, path));
        }
    }

    public JsonElement getLuaTable(String luaFile, String luaTableName) {
        Globals gl = JsePlatform.standardGlobals();
        gl.loadfile("script/Util.lua").call();
        String json = gl.get("getJSON").call(LuaValue.valueOf(luaFile), LuaValue.valueOf(luaTableName)).toString();
        return JsonParser.parseString(json);
    }

}
