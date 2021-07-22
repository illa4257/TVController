package com.illa4257.tvcontroller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CClient {
    public List<A> actions = new ArrayList<>();
    public Socket client;
    public BufferedReader input;
    public PrintWriter output;
    public List<String> queue = new ArrayList<>();
    public Thread t;
    public Map<String,String> sr = new HashMap<>();
    public List<String> rr = new ArrayList<>();
    public CClient(Socket client, BufferedReader input, PrintWriter output, boolean skip){
        this.client = client;
        this.input = input;
        this.output = output;
        t = new Thread(()->{
            if(skip)
                WriteLn("ping");
            boolean result = false;
            String cmsg = null;
            while(true){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String msg = ReadLn();
                if(msg!=null) {
                    String r = "error";
                    if (msg.equals("ping")) {
                        r = "pong";
                        if (queue.size() > 0) {
                            r = queue.get(0);
                            queue.remove(0);
                            if (rr.contains(r)) {
                                result = true;
                                cmsg = r;
                                rr.remove(r);
                            }
                        }
                    }else if(msg.equals("pong") || msg.equals("error") || msg.equals("ok")){
                        r = "ping";
                        if (queue.size() > 0) {
                            r = queue.get(0);
                            queue.remove(0);
                            if (rr.contains(r)) {
                                result = true;
                                cmsg = r;
                                rr.remove(r);
                            }
                        }
                    }else if(msg.equals("pause")){
                        A a = new A();
                        a.name = "pause";
                        actions.add(a);
                        r = "paused";
                    }else if(msg.equals("resume")){
                        A a = new A();
                        a.name = "resume";
                        actions.add(a);
                        r = "resumed";
                    }else if(result){
                        sr.put(cmsg,msg);
                    }else if(msg.startsWith("url")){
                        r = "ok";
                        A a = new A();
                        a.name = "url";
                        a.strArgs.add(msg.substring(4));
                        actions.add(a);
                    }else if(msg.startsWith("mp")){
                        r = "ok";
                        if (queue.size() > 0) {
                            r = queue.get(0);
                            queue.remove(0);
                            if (rr.contains(r)) {
                                result = true;
                                cmsg = r;
                                rr.remove(r);
                            }
                        }
                        A a = new A();
                        a.name = "mp";
                        a.strArgs.add(msg.substring(3,msg.lastIndexOf(' ')));
                        a.strArgs.add(msg.substring(msg.lastIndexOf(' ')+1));
                        actions.add(a);
                    }else if(msg.startsWith("paused")){
                        r = "ok";
                        if (queue.size() > 0) {
                            r = queue.get(0);
                            queue.remove(0);
                            if (rr.contains(r)) {
                                result = true;
                                cmsg = r;
                                rr.remove(r);
                            }
                        }
                        A a = new A();
                        a.name = "paused";
                        actions.add(a);
                    }else if(msg.startsWith("resumed")){
                        r = "ok";
                        if (queue.size() > 0) {
                            r = queue.get(0);
                            queue.remove(0);
                            if (rr.contains(r)) {
                                result = true;
                                cmsg = r;
                                rr.remove(r);
                            }
                        }
                        A a = new A();
                        a.name = "resumed";
                        actions.add(a);
                    }else if(msg.startsWith("moveTo")){
                        r = "ok";
                        if (queue.size() > 0) {
                            r = queue.get(0);
                            queue.remove(0);
                            if (rr.contains(r)) {
                                result = true;
                                cmsg = r;
                                rr.remove(r);
                            }
                        }
                        A a = new A();
                        a.name = "moveTo";
                        a.strArgs.add(msg.substring(7));
                        actions.add(a);
                    }else if(msg.startsWith("move")){
                        r = "ok";
                        if (queue.size() > 0) {
                            r = queue.get(0);
                            queue.remove(0);
                            if (rr.contains(r)) {
                                result = true;
                                cmsg = r;
                                rr.remove(r);
                            }
                        }
                        A a = new A();
                        a.name = "move";
                        a.strArgs.add(msg.substring(5));
                        actions.add(a);
                    }
                    WriteLn(r);
                }
            }
        });
        t.start();
    }

    public void WriteLn(String msg){
        output.println(msg);
        output.flush();
    }

    public String ReadLn(){
        try {
            return input.readLine();
        } catch (IOException ignored) {
            return null;
        }
    }

    public void Send(String msg){
        queue.add(msg);
    }

    public String SendAndWait(String msg){
        rr.add(msg);
        Send(msg);
        while(!sr.containsKey(msg)){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String r = sr.get(msg);
        sr.remove(msg);
        return r;
    }

    public void Close(){
        try {
            t.interrupt();
            output.close();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class A {
    public String name;
    public List<String> strArgs = new ArrayList<>();
}