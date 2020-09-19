package com.kelvin.midi.ezmusic.object;

import android.graphics.drawable.Drawable;

public class Instruments {
    private String instrumentsName;
    private String instrumentsSoundPath;
    private Drawable instrumentsImage;

    public Instruments(String instrumentsName, String instrumentsSoundPath, Drawable instrumentsImage) {
        this.instrumentsName = instrumentsName;
        this.instrumentsSoundPath = instrumentsSoundPath;
        this.instrumentsImage = instrumentsImage;
    }

    public String getInstrumentsName() {
        return instrumentsName;
    }

    public String getInstrumentsSoundPath() {
        return instrumentsSoundPath;
    }

    public Drawable getInstrumentsImage() {
        return instrumentsImage;
    }
}
