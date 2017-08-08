package net.softminds.goodroad.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import net.softminds.goodroad.R;

public class NewsActivity extends AppCompatActivity {

    private static String TAG = NewsActivity.class.getName();

    boolean mInit = false;
    WebView mWvNews;
    LinearLayout mLayoutLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        mLayoutLoading = (LinearLayout)findViewById(R.id.layout_loading_news);

        mWvNews = (WebView) findViewById(R.id.wv_news);
        mWvNews.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if( mInit == true ) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG,"onPageFinished start : " + url);
                super.onPageFinished(view, url);

                view.loadUrl("javascript:$('.contents.submargin').css('margin-top','0');$('#top_menu').remove();$('.about-market').remove()");
                Log.d(TAG,"onPageFinished end : " + url);
                mLayoutLoading.setVisibility(View.GONE);
                mWvNews.setVisibility(View.VISIBLE);
                mInit = true;
            }
        });

        mWvNews.getSettings().setJavaScriptEnabled(true);
        mWvNews.loadUrl("http://goodroad.co.kr/html/news.html");
    }
}
