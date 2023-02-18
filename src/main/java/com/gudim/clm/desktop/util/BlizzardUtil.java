package com.gudim.clm.desktop.util;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@UtilityClass
@Log4j2
public class BlizzardUtil {

    public String getToken() {
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create("grant_type=client_credentials", mediaType);
            Request request = new Request.Builder()
                    .url("https://oauth.battle.net/token")
                    .method("POST", body)
                    .addHeader("Authorization", "Basic " + toBase64(String.format("%s:%s", "927531fb95524ae0a647afb1d4ca722e", "GuaHqTVbYrIfn3g8PhIIJZXeJiKlZ080")))
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();
            String content = client.newCall(request).execute().body().string();
            return new JSONObject(content).getString("access_token");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private String toBase64(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }


    public JSONObject getItemInfo(String accessToken, String itemId, String url) {
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(String.format(url, itemId))
                    .addHeader("Authorization", String.format("Bearer %s", accessToken))
                    .build();
            String content = client.newCall(request).execute().body().string();
            return new JSONObject(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
