package com.kelvin.midi.ezmusic.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import com.kelvin.midi.ezmusic.object.Key;

import java.util.ArrayList;

import jp.kshoji.javax.sound.midi.Receiver;

public class ChordView extends View {
    private static int keyWidth;
    private static int keyHeight;

    //color of key
    private Paint black, blue, white;

    private ArrayList<Key> whites_key ;
    private ArrayList<Key> blacks_key;

    //list of note are black key
    // 22, 25, 27, 30, 32, 34, 37,  97, 99, 102, 104, 106 // black key
    private int[] blacks_key_list = {49, 51, 54, 56, 58, 61, 63,
            66, 68, 70};
    private int[] whites_key_list = {48, 50, 52, 53, 55, 57, 59, 60,
            62, 64, 65, 67, 69, 71, 72};

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
        whites_key = new ArrayList<>();
        blacks_key = new ArrayList<>();
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

        whites_key = new ArrayList<>();
        blacks_key = new ArrayList<>();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        keyWidth = w / ((25*35)/61);
        keyHeight = h;

        Log.i("GOTO ON SIZE CHANGE", " OK ");

        int blacks_key_length = blacks_key_list.length;
        int whites_key_length = whites_key_list.length;
        int index1 = 0;
        int index2 = 0;

        //21 is the number of midi note started
        //max octave 6

        //SIze of keys are currently 61keys_6 octave
        int KEYS_SIZE = 25;
        for (int i = 0; i <= KEYS_SIZE; i++) {
            int left = i * keyWidth;
            int right = left + keyWidth;


            RectF rect = new RectF(left, 0, right, h);
            // set midi note for this keys
            if (index1 < whites_key_length) {
                whites_key.add(new Key(whites_key_list[index1], rect, false));
                index1++;
            }


            // the index 0 3 7 10 is note that in the right of it not contain the black key.
            // mean that in the right of note contain a white note
            // Ex

            if (i != 0 && i != 3 && i != 7 && i != 10 && i != 14 && i != 17 && i != 21 && i != 24 ) {

                rect = new RectF((float) (i - 1) * keyWidth + 0.5f * keyWidth + 0.25f * keyWidth, 0,
                        (float) i * keyWidth + 0.25f * keyWidth, 0.67f * keyHeight);

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
        Log.i("CHORDVIEW: ","WHITE_KEYS LENTH "+ whites_key.size());
        Log.i("CHORDVIEW: ","BLACK_KEYS LENTH "+ blacks_key.size());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        boolean isDownAction = action == MotionEvent.ACTION_DOWN;
        boolean isMoveAction = action == MotionEvent.ACTION_MOVE;
        boolean isUpAction = action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_MOVE;

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (Key k : whites_key) {
            canvas.drawRect(k.rect, k.isNoteOn ? blue : white);
        }

        for (int i = 1; i <= 25; i++) {
            canvas.drawLine(i * keyWidth, 0, i * keyWidth, keyHeight, black);
        }

        for (Key k : blacks_key) {
            canvas.drawRect(k.rect, k.isNoteOn ? blue : black);
        }

        Log.i("D_CHORDVIEW: ","WHITE_KEYS LENTH "+ whites_key.size());
        Log.i("D_CHORDVIEW: ","BLACK_KEYS LENTH "+ blacks_key.size());
    }

    private void releaseKey(final Key k) {
        handler.postDelayed(() -> {
            k.isNoteOn = false;
            handler.sendEmptyMessage(0);
        }, 100);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            invalidate();
        }
    };


    //This method use binary search to find the key contain noteNumber
    public int getKeyIndexByNoteNumber_(int noteNumber, ArrayList<Key> KeyList) {
        int left = 0, right = KeyList.size() - 1;
        while (left <= right) {
            int midValue = left + (right - left) / 2;
            // Check if noteNumber is present at mid
            if (KeyList.get(midValue).note == noteNumber)
                return midValue;
            // If noteNumber greater, ignore left half
            if (KeyList.get(midValue).note < noteNumber)
                left = midValue + 1;
            else
                right = midValue - 1;
        }

        //exit -1 when noteNumber note found
        return -1;
    }

    public Pair<Integer, Integer> getKeyByNoteNumber(int noteNumber) {
//        Log.i("G_CHORDVIEW: ","WHITE_KEYS LENTH "+ whites_key.size());
//        Log.i("G_CHORDVIEW: ","BLACK_KEYS LENTH "+ blacks_key.size());
        Pair<Integer, Integer> temp;
//        Log.i("CHORDVIEW_ WHITEK_L: ", String.valueOf(whites_key.size()));
        int index = getKeyIndexByNoteNumber_(noteNumber, whites_key);
        temp = new Pair<>(index, 0);
        if (index == -1) {
            index = getKeyIndexByNoteNumber_(noteNumber, blacks_key);
            temp = new Pair<>(index, 1);
        }

        return temp;
    }


    public void setKey(int noteNumber, boolean isNoteOn) {
//        if(noteNumber<48){
//            noteNumber += 12;
//        }
//        if(noteNumber>72)
//            noteNumber -=12;

        Pair<Integer, Integer> indexOfKey = getKeyByNoteNumber(noteNumber);

        if (indexOfKey.first != -1) {
            int index = indexOfKey.first;
            if (indexOfKey.second == 0) {
                whites_key.get(index).isNoteOn = isNoteOn;
                Log.i("SET KEY ON", "WHITE_KEY_ " + noteNumber);
            } else {
                blacks_key.get(index).isNoteOn = isNoteOn;
                Log.i("SET KEY ON", "BLACK_KEY_ " + noteNumber);
            }
        } else {
            Log.e("SET KEY ON", "NOTE NOT FOUND ::  " + noteNumber);
        }
        try {

            MIDI_KEY = new ArrayList<>(whites_key);
            MIDI_KEY.addAll(blacks_key);
            invalidate();
            Log.i("SET KEY ON", "INVALIDATE: " + noteNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setReceiverForSynthesizer(Receiver receiver) {
        try {

            this.recv = receiver;
        } catch (IllegalStateException e) {
            Log.e("PIANO VIEW ", "RECEIVER NOT FOUND");
            e.printStackTrace();
        }
    }

    public void releaseChordKey(){
        for (Key k : whites_key) {
           k.isNoteOn = false;
        }

        for (Key k : blacks_key) {
            k.isNoteOn = false;
        }
        invalidate();
        requestLayout();
    }
}
