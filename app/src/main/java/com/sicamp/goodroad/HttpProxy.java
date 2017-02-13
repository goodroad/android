package com.sicamp.goodroad;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

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
                        conn.setDoOutput(true);
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
            if (status == HttpURLConnection.HTTP_OK) {
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
                } else if(!resultCode.equals("201")){
                    throw new Exception(resultCode);
                } else {
                    InputStream is = conn.getInputStream();
                    JSONObject rspObj = readIt(is);
                    return rspObj;
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
        GET(0), POST(1), PUT(2), DELETE(3);

        private final int type;

        Method(final int type) {
            this.type = type;
        }

        public int getMethod() {
            return this.type;
        }
    }
}
