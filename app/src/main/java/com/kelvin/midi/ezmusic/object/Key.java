package com.kelvin.midi.ezmusic.object;

import android.graphics.RectF;

public class Key {
    public int note;
    public RectF rect;
    public boolean isNoteOn;
    public boolean isNoteOff;

    public Key(int note, RectF rect, boolean isNoteOn) {
        this.note = note;
        this.rect = rect;
//        if (isNoteOn) {
//            this.isNoteOn = true;
//            this.isNoteOff = false;
//        } else {
//            this.isNoteOff = true;
//            this.isNoteOn = false;
//        }
        this.isNoteOn = isNoteOn;



    }


}
