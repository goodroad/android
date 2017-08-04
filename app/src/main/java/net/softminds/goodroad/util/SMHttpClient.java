package net.softminds.goodroad.util;

import android.net.http.AndroidHttpClient;
import android.util.Log;

import net.softminds.goodroad.exception.HttpResponseCodeException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.Header;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

/**
 * Created by hjlee on 2017-08-02.
 */

public class SMHttpClient {
    private static final String TAG = SMHttpClient.class.getName();

    public static JSONObject execute(String method, String url, String pathVariable, String query, String content ) throws Exception {
        return execute( method, url, pathVariable, query, content, true );
    }

    public static JSONObject execute(String method, String urlString, String pathVariable, String query, String content, boolean retryOnFail ) throws Exception {
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

            setHttpUrlHeader(null, conn);

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

            Log.d(TAG,"Response code : " + status);

            if( status == 301 || status == 302 ) {
                String location = conn.getHeaderField("Location");
                if( location == null ) {
                    throw new HttpResponseCodeException(new ProtocolVersion("http",1,1),status,conn.getResponseMessage());
                }
                return execute(method,location, pathVariable, query, content, retryOnFail);
            } else if( status == 200 ) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8")); //캐릭터셋 설정

                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append(line);
                }
                Log.d(TAG,"Response body : " + sb.toString());
                ret = new JSONObject(sb.toString());
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8")); //캐릭터셋 설정

                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append(line);
                }
                Log.d(TAG,"Response body : " + sb.toString());
                throw new HttpResponseCodeException(new ProtocolVersion("http",1,1),conn.getResponseCode(),conn.getResponseMessage());
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

    public static String postFile(final String url, final JSONObject headers, final JSONObject params) throws Exception {
        return connect(url, headers, params, Method.POST_FILE);
    }

    private static String connect(final String url, final JSONObject headers, final JSONObject params, final Method method) throws Exception {
        URL myUrl = new URL(url);
        String ret = "";

        Log.d(TAG,"Request URL : " + url);
        InputStream inputStream = null;
        HttpClient httpClient = AndroidHttpClient.newInstance("Android");
        try {
            HttpPost httpPost = new HttpPost(String.valueOf(myUrl));

            switch (method) {
                case POST_FILE:
                    File file = (File) params.get("file");
                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    builder.addPart("file", new FileBody(file));
                    httpPost.setEntity(builder.build());
                    break;
            }
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            inputStream = httpEntity.getContent();
            ret = readIt(inputStream);
            Log.d(TAG, "Response body : " + ret);
            Log.d(TAG, "Response code : " + httpResponse.getStatusLine().getStatusCode());

            int status = httpResponse.getStatusLine().getStatusCode();
//        if( httpResponse.getStatusLine().getStatusCode() == 301 || httpResponse.getStatusLine().getStatusCode() == 302 ) {
//            String location = null;
//            org.apache.http.Header[] responseHeaders = httpResponse.getAllHeaders();
//            for (Header header : responseHeaders) {
//                if( header.getName().equalsIgnoreCase("location") ) {
//                    location = header.getValue();
//                    Log.d(TAG,"Redirect to : " + location);
//                }
//            }
//
//            if( location == null ) {
//                throw new HttpResponseCodeException(httpResponse.getStatusLine());
//            }
//
//            return connect(location, headers, params, method);
//        }

            if (status != 200 && status != 302) {
                Log.e(TAG, ret);
                throw new HttpResponseCodeException(httpResponse.getStatusLine());
            }
            Log.d(TAG, ret);
        } catch ( Exception e ) {
            throw e;
        } finally {
            if( httpClient != null ) {
                httpClient.getConnectionManager().shutdown();
            }
        }
        return ret;

    }

    private static String readIt(InputStream stream) throws IOException, JSONException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        String line;
        StringBuilder response = new StringBuilder();
        while ((line = rd.readLine()) != null) {
            response.append(line);
        }
//        try {
//            return new JSONObject(response.toString());
            Log.d(TAG,response.toString());
            return response.toString();
//        } catch (JSONException e) {
//            e.printStackTrace();
//            throw e;
//        }
    }

    private static void setCommonHeaders(HttpURLConnection conn) {
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("Accept", "application/json");
    }

    private static void setHttpUrlHeader(JSONObject headers, HttpURLConnection conn) {
        setCommonHeaders(conn);
        if (headers != null) {
            Iterator iterator = headers.keys();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                try {
                    conn.setRequestProperty(key, headers.getString(key));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    enum Method {
        GET(0), POST(1), PUT(2), DELETE(3), POST_FILE(4);

        private final int type;

        Method(final int type) {
            this.type = type;
        }

        public int getMethod() {
            return this.type;
        }
    }
}

