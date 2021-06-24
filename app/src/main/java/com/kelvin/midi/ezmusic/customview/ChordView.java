package com.kelvin.midi.ezmusic.customview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.kelvin.midi.ezmusic.object.Key;

import java.util.ArrayList;

import jp.kshoji.javax.sound.midi.Receiver;

public class ChordView extends View {
    private static int keyWidth;
    private static int keyHeight;

    //color of key
    private Paint black, blue, white;

    private ArrayList<Key> whites_key = new ArrayList<>();
    private ArrayList<Key> blacks_key = new ArrayList<>();

    //list of note are black key
    // 22, 25, 27, 30, 32, 34, 37,  97, 99, 102, 104, 106 // black key
    private int[] blacks_key_list = {37, 39, 42, 44, 46, 49, 51, 54, 56, 58, 61, 63,
            66, 68, 70, 73, 75, 78, 80, 82, 85, 87, 90, 92, 94,};
    private int[] whites_key_list = {36, 38, 40, 41, 43, 45, 47, 48, 50, 52, 53, 55, 57, 59, 60,
            62, 64, 65, 67, 69, 71, 72, 74, 76, 77, 79, 81, 83, 84, 86, 88, 89, 91, 93, 95, 96};

    private ArrayList<Key> MIDI_KEY;

    //create instance of receiver from MainActivity
    private Receiver recv;

    private int NoteIsPlaying;

    //BEGIN_PATTERN
    //define custom interface
    public interface ChordViewListener {
        void onNoteOnListener(int noteOn);

        void onNoteOffListener(int noteOff);
    }

    //private PianoLargeView.ChordViewListener ChordViewListener;

    // Assign the listener implementing events interface that will receive the events (passed in by the owner)
//    public void setChordViewListener(PianoLargeView.ChordViewListener listener) {
//        this.ChordViewListener = listener;
//    }
    public ChordView(Context context, AttributeSet attrs) {
        super(context, attrs);

        keyWidth = 0;

        black = new Paint();
        white = new Paint();
        blue = new Paint();

        black.setColor(Color.BLACK);
        white.setColor(Color.WHITE);
        blue.setColor(Color.BLUE);

        black.setStyle(Paint.Style.FILL);
        white.setStyle(Paint.Style.FILL);
        blue.setStyle(Paint.Style.FILL);

        //this.ChordViewListener = null;
    }

    public ChordView(Context context) {
        super(context);
        keyWidth = 0;

        black = new Paint();
        white = new Paint();
        blue = new Paint();

        black.setColor(Color.BLACK);
        white.setColor(Color.WHITE);
        blue.setColor(Color.BLUE);

        black.setStyle(Paint.Style.FILL);
        white.setStyle(Paint.Style.FILL);
        blue.setStyle(Paint.Style.FILL);
        //this.ChordViewListener = null;
    }
}
