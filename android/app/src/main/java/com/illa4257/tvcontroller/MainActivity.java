package com.illa4257.tvcontroller;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                if(callUserInput){
                    callUserInput = false;
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(userInputMsg);
                    final EditText input = new EditText(MainActivity.this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    builder.setView(input);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            userInputMsg = input.getText().toString();
                            rUserInput = true;
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            userInputMsg = null;
                            rUserInput = true;
                            dialog.cancel();
                        }
                    });
                    MainActivity.this.runOnUiThread(()-> {
                        builder.show();
                    });
                }
            }
        },0,250);
    }

    public void onScanClick(View view){
        ScrollView dlist = findViewById(R.id.dlist);
        Button scan = (Button)findViewById(R.id.scan);
        TextView scanning = (TextView)findViewById(R.id.scanning);
        scanning.setText("Scanning...");
        scan.setEnabled(false);
        dlist.removeAllViews();
        findViewById(R.id.host).setVisibility(View.INVISIBLE);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        dlist.addView(layout);
        Thread t = new Thread(() -> {
            AtomicInteger pc = new AtomicInteger();
            try {
                List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface intf: interfaces) {
                    List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                    for(InetAddress addr : addrs){
                        if(!addr.isLoopbackAddress()){
                            String sAddr = addr.getHostAddress();
                            if(sAddr.indexOf(':')<0){
                                String sAddr2 = sAddr.substring(0,sAddr.lastIndexOf('.')+1);
                                for(int i=0;i<256;i++){
                                    String child = sAddr2+i;
                                    if(!child.equals(sAddr)) {
                                        Thread pt = new Thread(() -> {
                                            if (ping(child)) {
                                                TextView tvip = new TextView(this);
                                                tvip.setWidth(dlist.getWidth());
                                                tvip.setText("Checking "+child+"...");
                                                MainActivity.this.runOnUiThread(()->{
                                                        layout.addView(tvip);
                                                });
                                                if(isConnectable(child)){
                                                    Button cb = new Button(this);
                                                    cb.setWidth(dlist.getWidth());
                                                    cb.setText(child);
                                                    MainActivity.this.runOnUiThread(()->{
                                                            cb.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    dlist.setVisibility(View.INVISIBLE);
                                                                    scanning.setVisibility(View.INVISIBLE);
                                                                    scan.setVisibility(View.INVISIBLE);
                                                                    connect(child,view);
                                                                }
                                                            });
                                                            layout.addView(cb);
                                                    });
                                                }
                                                view.post(()->{
                                                    ViewGroup vg = (ViewGroup)tvip.getParent();
                                                    vg.removeView(tvip);
                                                });
                                            }
                                            pc.getAndIncrement();
                                        });
                                        pt.start();
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
            while(pc.get()<255){
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            view.post(new Runnable() {
                @Override
                public void run() {
                    scanning.setText("Scanned!");
                    scan.setEnabled(true);
                }
            });
        });
        t.start();
    }

    public boolean ping(String ip){
        boolean r = false;
        Runtime runtime = Runtime.getRuntime();
        try {
            Process mipap = runtime.exec("/system/bin/ping -c 1 "+ip);
            if(mipap.waitFor()==0)
                r = true;
        } catch(IOException | InterruptedException ignored){}
        return r;
    }

    public int PORT = 17899;
    public String clName = "TVCv1.0";
    public String pass;
    public Socket CL;
    public OutputStream CLout;
    public PrintWriter CLoutput;
    public BufferedReader CLinput;
    public String userInputMsg;
    public boolean callUserInput = false;
    public boolean rUserInput = false;

    public void connect(String ip, View view){
        (new Thread(()->{
            try {
                CL = new Socket(ip,PORT);
                CLout = CL.getOutputStream();
                CLoutput = new PrintWriter(CLout);
                CLinput = new BufferedReader(new InputStreamReader(CL.getInputStream()));

                CLoutput.println("connect "+clName);
                CLoutput.flush();

                boolean auth = true;
                boolean s = false;
                while (auth) {
                    String msg = CLinput.readLine();
                    if(msg==null){
                        auth = false;
                        break;
                    }
                    if(msg.equals("confirm")){
                        userInputMsg = "Code:";
                        callUserInput = true;
                        rUserInput = false;
                        while(!rUserInput){
                            try {
                                Thread.sleep(250);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        CLoutput.println("p"+userInputMsg);
                        CLoutput.flush();
                    }else{
                        auth = false;
                        s = true;
                    }
                }
                if(s){
                    this.runOnUiThread(()->{
                        GLOBALS.Set("cc",new CClient(CL,CLinput,CLoutput,true));
                        GLOBALS.Set("clName",clName);
                        Intent icyou = new Intent(MainActivity.this,CYou.class);
                        MainActivity.this.startActivity(icyou);
                    });
                } else {
                    closeCL();
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        })).start();
    }

    public void closeCL(){
        try {
            CLoutput.close();
            CLout.close();
            CL.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnectable(String ip){
        AtomicBoolean r = new AtomicBoolean(false);
        AtomicBoolean ended = new AtomicBoolean(false);
        long time = System.currentTimeMillis();
        Thread pt = new Thread(()->{
            try {
                Socket client = new Socket(ip,PORT);
                OutputStream out = client.getOutputStream();
                PrintWriter output = new PrintWriter(out);
                output.println("ping "+clName);
                output.flush();
                BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                final String result = input.readLine();
                if(result.equals("ok"))
                    r.set(true);
                output.close();
                out.close();
                client.close();
            }catch (IOException ignored){}
            ended.set(true);
        });
        pt.start();
        while(!ended.get()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long time2 = System.currentTimeMillis();
            long delta = time2-time;
            if(delta>5000){
                pt.interrupt();
                ended.set(true);
            }
        }
        return r.get();
    }

    public String str_random() {
        return str_random(16);
    }

    public String str_random(int len){
        // "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
        return str_random(len,"abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ123456789");
    }

    public String str_random(int len, String symb){
        char[] buf = new char[len];
        Random r = new Random();
        for(int i=0;i<buf.length;i++)
            buf[i] = symb.charAt(r.nextInt(symb.length()));
        return new String(buf);
    }

    public void onHostClick(View view){
        findViewById(R.id.host).setVisibility(View.INVISIBLE);
        findViewById(R.id.scan).setVisibility(View.INVISIBLE);
        findViewById(R.id.dlist).setVisibility(View.INVISIBLE);
        Thread ts = new Thread(()->{
            try {
                ServerSocket serv = new ServerSocket(PORT);
                pass = str_random(3);
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        TextView scanning = findViewById(R.id.scanning);
                        scanning.setText("Code: "+pass);
                    }
                });
                boolean ended = false;
                while(!ended){
                    Socket client = serv.accept();
                    onConnection(client);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        ts.start();
    }

    public void onConnection(Socket client){
        AtomicLong time = new AtomicLong(System.currentTimeMillis());
        AtomicInteger timeout = new AtomicInteger(5000);
        AtomicBoolean ended = new AtomicBoolean(false);
        Thread ts = new Thread(()->{
            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter output = new PrintWriter(client.getOutputStream());
                String msg1 = input.readLine();
                if(msg1.equals("ping "+clName)){
                    output.println("ok");
                    output.flush();
                }else if(msg1.equals("connect "+clName)){
                    output.println("confirm");
                    output.flush();
                    time.set(System.currentTimeMillis());
                    timeout.set(120000);
                    boolean auth = true;
                    boolean success = false;
                    while(auth){
                        String msg = input.readLine();
                        if(msg!=null)
                            if(msg.charAt(0)=='p'){
                                if(msg.substring(1).equals(pass)){
                                    auth = false;
                                    success = true;
                                    output.println("success");
                                }else
                                    output.println("confirm");
                                output.flush();
                            }
                    }
                    if(success){
                        this.runOnUiThread(()->{
                            GLOBALS.Set("cs",new CClient(client,input,output,false));
                            GLOBALS.Set("clName",clName);
                            Intent icme = new Intent(MainActivity.this,CMe.class);
                            MainActivity.this.startActivity(icme);
                        });
                        ended.set(true);
                        return;
                    }
                }
                output.close();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ended.set(true);
        });
        ts.start();
        while(!ended.get()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long time2 = System.currentTimeMillis();
            long delta = time2- time.get();
            if(delta> timeout.get()){
                ts.interrupt();
                ended.set(true);
                System.out.println("Interrupt!");
            }
        }
    }
}