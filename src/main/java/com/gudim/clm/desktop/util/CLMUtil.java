package com.gudim.clm.desktop.util;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

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
}
