package com.illa4257.tvcontroller;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;

public class CYBML extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_c_y_b_m_l);
        ml = (ArrayList<Media>)getIntent().getExtras().getSerializable("ml");
        ScrollView l = findViewById(R.id.ml);
        l.removeAllViews();
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        l.addView(layout);
        for(Media m : ml){
            Button media = new Button(this);
            String info = m.info;
            int limit = 64;
            String durl = m.url;
            if(durl.length()>limit) {
                durl = durl.substring(0,limit);
            }
            media.setText(info+'\n'+durl);
            media.setOnClickListener(v -> {
                CClient cc = (CClient) GLOBALS.Get("cc");
                cc.Send("url "+m.url);
                ((MP)GLOBALS.Get("mp")).isPlaying = true;
            });
            layout.addView(media);
        }
    }

    public ArrayList<Media> ml;
    public void onCloseClick(View view){ finish(); }
}