package com.gudim.clm.desktop.util;


import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.gudim.clm.desktop.exception.IncorrectUriException;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class CustomBrowser implements AuthorizationCodeInstalledApp.Browser {


    @Override
    public void browse(String url) throws IOException {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (URISyntaxException e) {
            throw new IncorrectUriException(ExceptionUtils.getStackTrace(e));
        }
    }
}