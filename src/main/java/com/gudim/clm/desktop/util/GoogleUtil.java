package com.gudim.clm.desktop.util;

import static com.gudim.clm.desktop.util.CLMConstant.APPLICATION_NAME;
import static com.gudim.clm.desktop.util.CLMConstant.CREDENTIALS_JSON;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Builder;
import com.gudim.clm.desktop.CLMController;
import com.gudim.clm.desktop.service.CLMBrowserService;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@UtilityClass
@Log4j2
public class GoogleUtil {
	
	public static Drive getGoogleDriveData() throws GeneralSecurityException, IOException {
		GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
		NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		InputStream resourceAsStream = CLMController.class.getResourceAsStream(CREDENTIALS_JSON);
		Credential credential = getCredential(jsonFactory, httpTransport, resourceAsStream);
		return new Builder(httpTransport, jsonFactory, credential)
			.setApplicationName(APPLICATION_NAME).build();
	}
	
	public static Credential getCredential(JsonFactory jsonFactory, HttpTransport httpTransport,
	                                       InputStream resourceAsStream) throws IOException {
		FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(
			new File(System.getProperty("user.home"), ".store/oauth2_sample"));
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory,
		                                                             new InputStreamReader(
			                                                             resourceAsStream));
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport,
		                                                                           jsonFactory,
		                                                                           clientSecrets,
		                                                                           Collections.singleton(
			                                                                           "https://www.googleapis.com/auth/drive"))
			.setDataStoreFactory(dataStoreFactory).build();
		return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver(),
		                                         new CLMBrowserService()).authorize("user");
	}
}
