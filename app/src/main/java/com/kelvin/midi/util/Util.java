package com.kelvin.midi.util;

import android.util.Log;

public class Util {
    //Util
    void print(String info, String msg) {
        Log.i(info, msg);
    }

    public static void print(String msg) {
        Log.i("", msg);
    }

    public static void print_(String info, String msg) {
        Log.e(info, msg);
    }
}
