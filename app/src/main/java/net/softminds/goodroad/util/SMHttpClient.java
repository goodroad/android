package net.softminds.goodroad.util;

import android.util.Log;

import net.softminds.goodroad.common.Definitions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * Created by hjlee on 2017-08-02.
 */

public class SMHttpClient {
    private static final String TAG = SMHttpClient.class.getName();

    public static JSONObject execute(String method, String url, String pathVariable, String query, String content ) {
        return execute( method, url, pathVariable, query, content, true );
    }

    public static JSONObject execute(String method, String urlString, String pathVariable, String query, String content, boolean retryOnFail ) {
        HttpURLConnection conn = null;
        JSONObject ret = null;
        if( pathVariable != null ) {
            if( !urlString.endsWith("/") && !pathVariable.startsWith("/")) {
                urlString += "/";
            }
            urlString += pathVariable;
        }

        if( query != null ) {
            urlString += "?" + query;
        }
        Log.d(TAG,"Requested URL : " + urlString);
        Log.d(TAG,"Requested path variable : " + pathVariable);
        Log.d(TAG,"Requested query : " + query);
        Log.d(TAG,"Requested content : " + content);
        try {
            URL url = new URL(urlString); //요청 URL을 입력
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod(method); //요청 방식을 설정 (default : GET)
            conn.setDoInput(true); //input을 사용하도록 설정 (default : true)

            if( content == null  ) {
                conn.setDoOutput(false); //output을 사용하도록 설정 (default : false)
            } else {
                conn.setDoOutput(true); //output을 사용하도록 설정 (default : false)
            }

            conn.setConnectTimeout(15000); //타임아웃 시간 설정 (default : 무한대기)

            if( content != null ) {
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8")); //캐릭터셋 설정

                writer.write(content); //요청 파라미터를 입력
                writer.flush();
                writer.close();
                os.close();
            }
            conn.connect();

            int status = conn.getResponseCode();


            if( status == 200 ) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8")); //캐릭터셋 설정

                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append(line);
                }
                Log.d(TAG,"Response : " + sb.toString());
                ret = new JSONObject(sb.toString());
            }
        }catch (ConnectException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(conn != null) {
                conn.disconnect();
            }
        }
        return ret;
    }
}

