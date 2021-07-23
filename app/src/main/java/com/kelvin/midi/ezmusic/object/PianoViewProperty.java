package com.kelvin.midi.ezmusic.object;

import android.graphics.Canvas;

public class PianoViewProperty {
    private int keyWidth;
    private int keyHeight;
    private int w;
    private int h;
    private int oldw;
    private int oldh;

    public Canvas getCanvas() {
        return canvas;
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    Canvas canvas;

    public int getKeyWidth() {
        return keyWidth;
    }

    public void setKeyWidth(int keyWidth) {
        this.keyWidth = keyWidth;
    }

    public int getKeyHeight() {
        return keyHeight;
    }

    public void setKeyHeight(int keyHeight) {
        this.keyHeight = keyHeight;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public int getOldw() {
        return oldw;
    }

    public void setOldw(int oldw) {
        this.oldw = oldw;
    }

    public int getOldh() {
        return oldh;
    }

    public void setOldh(int oldh) {
        this.oldh = oldh;
    }

    public PianoViewProperty(int w, int h, int oldw, int oldh, int keyWidth, int keyHeight) {
        this.keyWidth = keyWidth;
        this.keyHeight = keyHeight;
        this.w = w;
        this.h = h;
        this.oldw = oldw;
        this.oldh = oldh;
    }

    public PianoViewProperty(int w, int h, int oldw, int oldh) {
        this.w = w;
        this.h = h;
        this.oldw = oldw;
        this.oldh = oldh;
    }

    public PianoViewProperty(int w, int h, int oldw, int oldh,Canvas canvas_) {
        this.w = w;
        this.h = h;
        this.oldw = oldw;
        this.oldh = oldh;
        this.canvas = canvas_;
    }

    public PianoViewProperty(int w, int h, int oldw, int oldh,int keyWidth, int keyHeight,Canvas canvas_) {
        this.keyWidth = keyWidth;
        this.keyHeight = keyHeight;
        this.w = w;
        this.h = h;
        this.oldw = oldw;
        this.oldh = oldh;
        this.canvas = canvas_;
    }

    public PianoViewProperty() {
    }
}
