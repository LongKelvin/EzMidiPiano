package com.kelvin.midi.ezmusic.object;

import android.util.Pair;

import java.util.HashMap;

public class KeyMap {
    private HashMap<Integer, String> keyMap;

    private final int notePerOctave = 12;
    private String noteName = "";
    private int noteValue = 0;

    public KeyMap() {
        keyMap = new HashMap<>();
    }

    public HashMap<Integer, String> InitKeyMap() {
        for (int index = 36; index <= 47; index++) {
            noteName = GenerateNoteName(index);
            for (int j = 1; j <= 5; j++) {
                noteValue = index + (j * notePerOctave);
                keyMap.put(noteValue, noteName);
            }
        }
        return keyMap;
    }

    public String GenerateNoteName(int note) {
        String noteName = "";
        switch (note) {
            case 36:
                noteName = "C";
                break;
            case 37:
                noteName = "C#";
                break;
            case 38:
                noteName = "D";
                break;
            case 39:
                noteName = "D#";
                break;
            case 40:
                noteName = "E";
                break;
            case 41:
                noteName = "F";
                break;
            case 42:
                noteName = "F#";
                break;
            case 43:
                noteName = "G";
                break;
            case 44:
                noteName = "G#";
                break;
            case 45:
                noteName = "A";
                break;
            case 46:
                noteName = "Bb";
                break;
            case 47:
                noteName = "B";
                break;
            default:
                noteName = " ";
                break;
        }
        return noteName;
    }

    public String GetStringNoteName(int noteValue) {
        String noteName = keyMap.get(noteValue);
        if (noteName != null)
            return keyMap.get(noteValue);
        else return "";
    }

}
