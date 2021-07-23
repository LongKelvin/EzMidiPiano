package com.kelvin.midi.metronome.object;

import android.util.Log;

public class MetronomeSingleton {

    private static MetronomeSingleton _instance = null;
    private Metronome _metronome = null;

    private MetronomeSingleton(){

        _metronome = new Metronome();
    }

    public static MetronomeSingleton getInstance(){

        if(_instance == null){
            _instance = new MetronomeSingleton();
            Log.v("Singleton", "Initializing new singleton instance");
        }

        return _instance;
    }

    public Metronome getMetronome(){
        return _metronome;
    }
}
