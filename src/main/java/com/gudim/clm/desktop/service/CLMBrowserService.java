package com.gudim.clm.desktop.service;

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.exception.ExceptionUtils;

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