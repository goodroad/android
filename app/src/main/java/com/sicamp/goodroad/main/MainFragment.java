package com.sicamp.goodroad.main;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.sicamp.goodroad.HttpProxy;
import com.sicamp.goodroad.R;

import org.json.JSONException;
import org.json.JSONObject;

public class MainFragment extends Fragment {
    private static final String TAG = MainFragment.class.getSimpleName();

    private static final String URL = "http://rest.goodroad.co.kr/api/reports";

    private Button mBtnReport;

    public static MainFragment getInstance() {
        return new MainFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mBtnReport = (Button) view.findViewById(R.id.btn_report);
        mBtnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject params = new JSONObject();
                    params.put("group1", "포유류");
                    params.put("group2", "test");
                    params.put("lng", 127.69);
                    params.put("lat", 35.9948);

                    PostReport postReport = new PostReport();
                    postReport.execute(params);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        return view;
    }


    private class PostReport extends AsyncTask<JSONObject, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            try {
                String postUrl = URL;
                HttpProxy.getInstance().httpPost(URL, null, params[0]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


}
