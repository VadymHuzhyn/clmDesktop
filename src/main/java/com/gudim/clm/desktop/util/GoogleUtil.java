package com.gudim.clm.desktop.util;

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
import com.gudim.clm.desktop.exception.FileNonFoundException;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;

import static com.gudim.clm.desktop.util.CLMConstant.APPLICATION_NAME;
import static com.gudim.clm.desktop.util.CLMConstant.CREDENTIALS_JSON;
import static com.gudim.clm.desktop.util.CLMConstant.DATA_STORE_DIR;
import static com.gudim.clm.desktop.util.CLMConstant.DRIVE_SCOPE;

public class GoogleUtil {

    private GoogleUtil() {
    }

    public static Drive getGoogleDriveData() {
        try {
            GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            InputStream resourceAsStream = CLMController.class.getResourceAsStream(
                    CREDENTIALS_JSON);
            Credential credential = getCredential(jsonFactory, httpTransport,
                    resourceAsStream);
            return new Builder(httpTransport, jsonFactory, credential)
                    .setApplicationName(APPLICATION_NAME).build();
        } catch (GeneralSecurityException | IOException e) {
            throw new FileNonFoundException(ExceptionUtils.getStackTrace(e));
        }
    }

    public static Credential getCredential(JsonFactory jsonFactory, HttpTransport httpTransport,
                                           InputStream resourceAsStream) throws IOException {
        FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory,
                new InputStreamReader(
                        resourceAsStream));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport,
                jsonFactory,
                clientSecrets,
                DRIVE_SCOPE)
                .setDataStoreFactory(dataStoreFactory).build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver(),
                new CustomBrowser()).authorize("user");
    }
}
