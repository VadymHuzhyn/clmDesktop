package com.gudim.clm.desktop.util;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
        try {
            InputStream inputStream = CLMUtil.class.getClassLoader().getResourceAsStream(fileName);
            String itemsInfo = IOUtils.toString(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8);
            return new JSONObject(itemsInfo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
