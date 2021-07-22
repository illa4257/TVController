package com.illa4257.tvcontroller;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class CYBrowser extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_c_y_browser);
        cc = (CClient)GLOBALS.Get("cc");
        clName = (String) GLOBALS.Get("clName");
        Button mlbtn = findViewById(R.id.ml);
        browser = findViewById(R.id.browser);
        browser.setWebViewClient(new WebViewClient(){
            @Override
            public void doUpdateVisitedHistory(WebView view, String u, boolean isReload) {
                url.setText(u);
                ml = new ArrayList<>();
                super.doUpdateVisitedHistory(view, u, isReload);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                (new Thread(()->{
                    boolean a = false;
                    String info = "";
                    try {
                        URLConnection urlc = new URL(url).openConnection();
                        String ext = urlc.getHeaderField("Content-Type");
                        if(ext!=null) {
                            if(ext.startsWith("video/")){
                                a = true;
                                info = "Unknown. (" + ext.substring(ext.indexOf('/') + 1) + ")";
                            }else if(ext.startsWith("audio/")){
                                a = true;
                                info = "Unknown. (" + ext.substring(ext.indexOf('/') + 1) + ")";
                            }else if(ext.startsWith("application/vnd.apple.mpegurl")){
                                a = true;
                                info = "Unknown. (m3u8)";
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(a) {
                        Media m = new Media();
                        m.url = url;
                        m.info = info;
                        ml.add(m);
                    }
                    CYBrowser.this.runOnUiThread(()->{
                        mlbtn.setText(Integer.toString(ml.size()));
                    });
                })).start();
                super.onLoadResource(view, url);
            }
        });
        browser.getSettings().setJavaScriptEnabled(true);
        url = findViewById(R.id.url);
        url.setOnKeyListener((v, keyCode, event) -> {
            if((event.getAction()==KeyEvent.ACTION_DOWN) && (keyCode==KeyEvent.KEYCODE_ENTER)){
                onFindClick(null);
                return true;
            }
            return false;
        });
        onFindClick(null);
        this.getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(browser.canGoBack()){
                    browser.goBack();
                }else{
                    finish();
                }
            }
        });
    }

    public void onFindClick(View view){
        browser.loadUrl(url.getText().toString());
    }

    public void onBackClick(View view){
        this.finish();
    }

    public void onMlClick(View view){
        Intent cybml = new Intent(this, CYBML.class);
        cybml.putExtra("ml",ml);
        this.startActivity(cybml);
    }

    public ArrayList<Media> ml = new ArrayList<>();
    public EditText url;
    public WebView browser;
    public CClient cc;
    public String clName;
}

class Media implements Serializable {
    public String url;
    public String info;
}