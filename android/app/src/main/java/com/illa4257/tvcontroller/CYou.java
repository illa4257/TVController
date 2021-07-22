package com.illa4257.tvcontroller;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class CYou extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_c_you);
        cc = (CClient)GLOBALS.Get("cc");
        GLOBALS.Set("mp",mp = new MP());
        TextView time = findViewById(R.id.tsb);
        Button pr = findViewById(R.id.pr);
        tseekb = findViewById(R.id.tseekb);
        tseekb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                touched++;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                touched--;
                if(touched<=0){
                    touched = 0;
                    if(mp.status=="mp") {
                        cc.Send("moveTo "+tseekb.getProgress());
                        mp.currentPosition = tseekb.getProgress();
                    }
                }
            }
        });
        (new Thread(()->{
            while (true){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(cc.actions.size()>0){
                    A a = cc.actions.get(0);
                    cc.actions.remove(0);
                    if(a.name.equals("paused")){
                        mp.isPlaying = false;
                    }else if(a.name.equals("resumed")){
                        mp.isPlaying = true;
                    }else if(a.name.equals("mp")){
                        mp.status = "mp";
                        try {
                            mp.currentPosition = Integer.parseInt(a.strArgs.get(0));
                            mp.duration = Integer.parseInt(a.strArgs.get(1));
                            tseekb.setMax(mp.duration);
                            if(touched<=0)
                                tseekb.setProgress(mp.currentPosition);
                        }catch(NumberFormatException ignored){}
                    }
                }
                this.runOnUiThread(()->{
                    pr.setText(mp.isPlaying ? "||" : ">");
                    time.setText(getVideoTime(mp.currentPosition)+" / "+getVideoTime(mp.duration));
                });
            }
        })).start();
    }

    public void onPrClick(View view){
        if(mp.status=="mp")
            cc.Send(mp.isPlaying ? "pause" : "resume");
    }

    public void onOBrowserClick(View view){
        Intent icyb = new Intent(CYou.this,CYBrowser.class);
        this.startActivity(icyb);
    }

    public void onPTClick(View view){
        if(mp.status=="mp")
            cc.Send("move -10000");
    }
    public void onNTClick(View view){
        if(mp.status=="mp")
            cc.Send("move 10000");
    }

    public CClient cc;
    public MP mp;
    public SeekBar tseekb;
    public int touched = 0;

    public String getVideoTime(long ms){
        ms/=1000;
        String m = Integer.toString(Math.round((ms%3600)/60));
        if(m.length()==1)
            m = "0"+m;
        String s = Integer.toString(Math.round((ms%3600)%60));
        if(s.length()==1)
            s = "0"+s;
        return (ms/3600)+":"+m+":"+s;
    }
}

class MP {
    public String status = "none";
    public boolean isPlaying = false;
    public int currentPosition = 0;
    public int duration = 0;
}