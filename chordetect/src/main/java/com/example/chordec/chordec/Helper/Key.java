package com.example.chordec.chordec.Helper;

import android.graphics.RectF;

public class Key {
    public int note;
    public RectF rect;
    public boolean isNoteOn;
    public boolean isNoteOff;
    public boolean isKeyDown;

    public long start_tick;
    public long end_tick;
    public long duration;

    public Key(int note, RectF rect, boolean isNoteOn) {
        this.note = note;
        this.rect = rect;
        this.isNoteOn = isNoteOn;
    }

    public long get_Start_tick() {
        return start_tick;
    }

    public void set_Start_tick(long start_tick) {
        this.start_tick = start_tick;
    }

    public long get_End_tick() {
        return end_tick;
    }

    public void set_End_tick(long end_tick) {
        this.end_tick = end_tick;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isKeyDown() {
        return isKeyDown;
    }

    public void setKeyDown(boolean keyDown) {
        isKeyDown = keyDown;
    }
}
