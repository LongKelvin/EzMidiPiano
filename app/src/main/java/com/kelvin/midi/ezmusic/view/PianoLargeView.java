package com.kelvin.midi.ezmusic.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.kelvin.midi.ezmusic.object.Key;
import com.kelvin.midi.ezmusic.object.PianoViewProperty;

import java.util.ArrayList;

import jp.kshoji.javax.sound.midi.Receiver;

public class PianoLargeView extends View {
    //Variable
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
    //this actually the synthesizer object
    //use for create sound when user click to the piano view
    private Receiver recv;

    //21 is the number of midi note started
    //max octave 6

    //SIze of keys are currently 61_keys_6 octave
    //But in this piano large view
    //only render an octave from note 60 to 72
    //mean 12 keys
    int KEYS_SIZE = 61;


    //Size of piano view
    // default by 7 -> 1 octave
    int piano_size = 7;

    // get view width
    private int W;

    PianoViewProperty _PianoViewProperty;

    public int getPianoSize() {
        return piano_size;
    }

    public void setPianoSize(int SIZE_CHANGE_VALUE) {
        this.piano_size = SIZE_CHANGE_VALUE;
    }

    public PianoLargeView(Context context) {
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

        _PianoViewProperty = new PianoViewProperty();
    }

    public PianoLargeView(Context context, @Nullable AttributeSet attrs) {
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

        _PianoViewProperty = new PianoViewProperty();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //get current value of width
        W = w;

        //Default_value
        keyWidth = (int) (w / piano_size);
        keyHeight = h;

        whites_key.clear();
        blacks_key.clear();

        _PianoViewProperty = new PianoViewProperty(w, h, oldw, oldh, keyWidth, keyHeight);

        int blacks_key_length = blacks_key_list.length;
        int whites_key_length = whites_key_list.length;
        int index1 = 0;
        int index2 = 0;

        Log.i("PI_onSizeChanged:: ","Start");

        for (int i = 0; i <= KEYS_SIZE; i++) {
            int left = i * keyWidth;
            int right = left + keyWidth;


            Log.i("PI_onSizeChanged:: ","keyWidth = "+ keyWidth);
            RectF rect = new RectF(left, 0, right, h);
            // set midi note for this keys
            if (index1 < whites_key_length) {
                whites_key.add(new Key(whites_key_list[index1], rect, false));
                index1++;
            }
            Log.i("PI_onSizeChanged:: ","white_keys_rect = "+ rect);


            // the index 0 3 7 10 is note that in the left of it not contain the black key.
            // mean that in the left of note contain a white note
            // Ex

            if (i != 0 && i != 3 && i != 7 && i != 10 && i != 14 && i != 17 && i != 21 && i != 24 && i != 28 && i != 31 && i != 35 && i != 38
                    && i != 42 && i != 45 && i != 49 && i != 52){

                rect = new RectF((float) (i - 1) * keyWidth + 0.5f * keyWidth + 0.25f * keyWidth, 0,
                        (float) i * keyWidth + 0.25f * keyWidth, 0.67f * keyHeight);

                Log.i("PI_onSizeChanged:: ","black_keys_rect = "+ rect);
                // set midi note for this keys
                if (index2 < blacks_key_length) {
                    blacks_key.add(new Key(blacks_key_list[index2], rect, false));
                    index2++;
                }
            }
        }

        // Init MIDI_KEY note number and index of key
        MIDI_KEY = new ArrayList<>(whites_key);
        MIDI_KEY.addAll(blacks_key);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.i("PI_onDraw:: ","start  ");
        for (Key k : whites_key) {
            canvas.drawRect(k.rect, k.isNoteOn ? blue : white);
            Log.i("PI_onDraw:: ","white_keys_rect = "+ k.rect);
        }

        for (int i = 1; i <= KEYS_SIZE; i++) {
            canvas.drawLine(i * keyWidth, 0, i * keyWidth, keyHeight, black);
        }

        for (Key k : blacks_key) {
            canvas.drawRect(k.rect, k.isNoteOn ? blue : black);
            Log.i("PI_onDraw:: ","blacks_key_rect = "+ k.rect);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setPianoViewWidth(int _NewSize_) {
        Log.i("PianoLargeView:: ", "Goto setPianoViewWidth");
        Log.i("PianoLargeView:: ", "piano_size = "+ piano_size);

        piano_size = _NewSize_;

        if (piano_size == 0)
            piano_size = 1;

        //update view
        keyWidth = (int) (W / piano_size);

        onSizeChanged(_PianoViewProperty.getW(),_PianoViewProperty.getH(),_PianoViewProperty.getOldw(),_PianoViewProperty.getOldh());

        invalidate();
        requestLayout();

    }

}
