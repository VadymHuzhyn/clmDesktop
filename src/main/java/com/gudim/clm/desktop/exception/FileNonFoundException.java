package com.gudim.clm.desktop.exception;

public class FileNonFoundException
        extends RuntimeException {

    public FileNonFoundException(String errorMessage) {
        super(errorMessage);
    }
}