package com.illa4257.tvcontroller;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.VideoView;

public class CMe extends AppCompatActivity {

    public Thread t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_c_me);
        cs = (CClient) GLOBALS.Get("cs");
        clName = (String) GLOBALS.Get("clName");
        vv = findViewById(R.id.vv);
        int offset = 1000;
        t = new Thread(()->{
            while(true){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                boolean callback = false;
                String msg = "";
                if(vv.isPlaying()){
                    callback = true;
                    msg = "mp "+vv.getCurrentPosition()+" "+vv.getDuration();
                }
                if(cs.actions.size()>0){
                    A a = cs.actions.get(0);
                    cs.actions.remove(0);
                    this.runOnUiThread(() -> {
                        if(a.name.equals("url")){
                            vv.setVideoPath(a.strArgs.get(0));
                            vv.start();
                        }else if(a.name.equals("pause")){
                            if(vv.isPlaying())
                                ct = vv.getCurrentPosition()+offset;
                            vv.pause();
                        }else if(a.name.equals("resume")){
                            vv.seekTo(Math.min(ct,vv.getDuration()));
                            vv.start();
                        }else if(a.name.equals("moveTo")){
                            try {
                                int m = Integer.parseInt(a.strArgs.get(0));
                                vv.pause();
                                vv.seekTo(Math.min(Math.max(m,0),vv.getDuration()));
                                vv.start();
                            }catch (NumberFormatException ignored){}
                        }else if(a.name.equals("move")){
                            try {
                                int m = Integer.parseInt(a.strArgs.get(0));
                                int t = vv.getCurrentPosition()+offset;
                                vv.pause();
                                vv.seekTo(Math.min(Math.max(t+m,0),vv.getDuration()));
                                vv.start();
                            }catch (NumberFormatException ignored){}
                        }
                    });
                }
                if(callback){
                    cs.Send(msg);
                }
            }
        });
        t.start();
    }

    public int ct;
    public CClient cs;
    public String clName;
    public VideoView vv;
}