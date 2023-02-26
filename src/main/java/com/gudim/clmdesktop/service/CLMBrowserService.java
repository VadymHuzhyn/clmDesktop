package com.gudim.clmdesktop.service;

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Service
@Log4j2
public class CLMBrowserService implements AuthorizationCodeInstalledApp.Browser {

    @Override
    public void browse(String url) throws IOException {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (URISyntaxException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }
}