package com.illa4257.tvcontroller;

import java.util.ArrayList;
import java.util.List;

public class GLOBALS {
    public static List<Object> OL = new ArrayList<>();
    public static List<String> LK = new ArrayList<>();

    public static void Set(String key, Object value){
        if(Exists(key)){
            OL.remove(OL.get(LK.indexOf(key)));
            LK.remove(key);
        }
        OL.add(value);
        LK.add(key);
    }
    
    public static boolean Exists(String key){
        return LK.contains(key);
    }

    public static Object Get(String key){
        return OL.get(LK.indexOf(key));
    }
}
