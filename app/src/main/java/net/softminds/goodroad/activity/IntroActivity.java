package net.softminds.goodroad.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.softminds.goodroad.R;

public class IntroActivity extends AppCompatActivity {

    private static String TAG = IntroActivity.class.getName();

    WebView mWvIntro;
    LinearLayout mLayoutLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        mLayoutLoading = (LinearLayout)findViewById(R.id.layout_loading_intro);

        mWvIntro = (WebView) findViewById(R.id.wv_intro);
        mWvIntro.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG,"onPageFinished start : " + url);
                super.onPageFinished(view, url);

                view.loadUrl("javascript:$('.contents.submargin').css('margin-top','0');$('#top_menu').remove();$('.about-market').remove()");
                Log.d(TAG,"onPageFinished end : " + url);
                mLayoutLoading.setVisibility(View.GONE);
                mWvIntro.setVisibility(View.VISIBLE);
            }
        });

        mWvIntro.getSettings().setJavaScriptEnabled(true);
        mWvIntro.loadUrl("http://goodroad.co.kr/html/about.html");
        ImageButton ibBack = (ImageButton) findViewById(R.id.btn_back);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title);
        tvTitle.setText(getTitle());
    }
}
