package com.sicamp.goodroad;

import android.graphics.Bitmap;
import android.net.http.AndroidHttpClient;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Objects;

public class HttpProxy {
    private final String TAG = HttpProxy.class.getSimpleName();

    private static HttpProxy mInstance = null;

    public static HttpProxy getInstance() {
        if (mInstance == null) {
            mInstance = new HttpProxy();
        }
        return mInstance;
    }

    public JSONObject httpGet(final String url, final JSONObject headers) throws JSONException {
        return connectHttp(url, headers, null, Method.GET);
    }

    public Object httpPost(final String url, final JSONObject headers, final JSONObject params) throws JSONException {
        return connectHttp(url, headers, params, Method.POST);
    }

    public Object httpPostFile(final String url, final JSONObject headers, final JSONObject params) throws JSONException {
        return connect(url, headers, params, Method.POST_FILE);
    }

    private JSONObject connect(final String url, final JSONObject headers, final JSONObject params, final Method method) throws JSONException {
        try {
            URL myUrl = new URL(url);

            InputStream inputStream = null;
            HttpClient httpClient = AndroidHttpClient.newInstance("Android");
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
            JSONObject rspObj = readIt(inputStream);
            Log.d(TAG, "response : " + rspObj.toString());
            return rspObj;

        } catch (MalformedURLException e) {
            Log.e(TAG, TAG + " MalformedURLException : " + e.toString());
            throw new RuntimeException(TAG + " MalformedURLException : " + e.toString());
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, TAG + " UnsupportedEncodingException : " + e.toString());
            throw new RuntimeException(TAG + " UnsupportedEncodingException : " + e.toString());
        } catch (ClientProtocolException e) {
            Log.e(TAG, TAG + " ClientProtocolException : " + e.toString());
            throw new RuntimeException(TAG + " ClientProtocolException : " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, TAG + " IOException : " + e.toString());
            throw new RuntimeException(TAG + " IOException : " + e.toString());
        }

    }

    private JSONObject connectHttp(final String url, final JSONObject headers, final JSONObject params, final Method method) throws JSONException {
        Log.d(TAG, "connectHttp(" + url + ")(" + params + ")" + ", headers : " + headers);
        HttpURLConnection conn = null;

        try {
            URL myUrl = new URL(url);
            conn = (HttpURLConnection) myUrl.openConnection();
            //header
            setHttpUrlHeader(headers, conn);
            conn.setUseCaches(false);
            conn.setDoInput(true);

            switch (method) {
                case GET:
                    conn.setRequestMethod("GET");
                    break;
                case POST:
                    conn.setRequestMethod("POST");
                    if (params != null) {
                        OutputStream os = conn.getOutputStream();
                        BufferedWriter wd = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                        wd.write(params.toString());
                        wd.flush();
                        wd.close();
                    }
                    break;
                case PUT:
                    conn.setRequestMethod("PUT");
                    if (params != null) {
                        conn.setDoOutput(true);
                        OutputStream os = conn.getOutputStream();
                        BufferedWriter wd = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                        wd.write(params.toString());
                        wd.flush();
                        wd.close();
                    }
                    break;
                case DELETE:
                    conn.setRequestMethod("DELETE");
                    break;
            }
            conn.connect();
            Log.d(TAG, "response : " + conn.getResponseCode() + ", " + conn.getResponseMessage());

            int status = conn.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK || status == HttpURLConnection.HTTP_CREATED) {
                InputStream is = conn.getInputStream();
                JSONObject rspObj = readIt(is);
                Log.d(TAG, "response : " + rspObj.toString());
                Log.i(TAG, "getResponseCode : " + status);
                return rspObj;
            } else {
                InputStream inputStream = conn.getErrorStream();
                JSONObject object = readIt(inputStream);
                Log.e(TAG, "HTTP error : " + object.toString());

                String resultCode = object.getString("resultCode");
                if (TextUtils.isEmpty(resultCode)) {
                    throw new RuntimeException("HTTP Connection Error : " + object.toString());
                } else {
                    throw new Exception(resultCode);
                }
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, TAG + " MalformedURLException : " + e.toString());
            throw new RuntimeException(TAG + " MalformedURLException : " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, TAG + " IOException : " + e.toString());
            throw new RuntimeException(TAG + " IOException : " + e.toString());
        } catch (Exception e) {
            Log.e(TAG, TAG + " Exception : " + e.toString());
            throw new RuntimeException(TAG + " Exception : " + e.toString());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void setHttpUrlHeader(JSONObject headers, HttpURLConnection conn) {
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

    private void setCommonHeaders(HttpURLConnection conn) {
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("Accept", "application/json");
    }

    private JSONObject readIt(InputStream stream) throws IOException, JSONException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        String line;
        StringBuilder response = new StringBuilder();
        while ((line = rd.readLine()) != null) {
            response.append(line);
        }
        try {
            return new JSONObject(response.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            throw e;
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
