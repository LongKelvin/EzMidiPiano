package com.kelvin.midi.ezmusic.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.kelvin.midi.ezmusic.object.Key;
import com.kelvin.midi.ezmusic.object.PianoViewProperty;

import java.util.ArrayList;
import java.util.Objects;

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.Receiver;
import jp.kshoji.javax.sound.midi.ShortMessage;

public class PianoLargeView extends View {

    //BEGIN_PATTERN
    //define custom interface
    public interface PianoViewListener {
        void onNoteOnListener(int noteOn);

        void onNoteOffListener(int noteOff);
    }

    private PianoViewListener pianoViewListener;

    // Assign the listener implementing events interface that will receive the events (passed in by the owner)
    public void setPianoViewListener(PianoViewListener listener) {
        this.pianoViewListener = listener;
    }


    //END_PATTERN
    //Variable
    private static int keyWidth;
    private static int keyHeight;

    //color of key
    private final Paint black;
    private final Paint blue;
    private final Paint white;

    private final ArrayList<Key> whites_key = new ArrayList<>();
    private final ArrayList<Key> blacks_key = new ArrayList<>();

    //list of note are black key
    // 22, 25, 27, 30, 32, 34, 37,  97, 99, 102, 104, 106 // black key
    private final int[] blacks_key_list = {37, 39, 42, 44, 46, 49, 51, 54, 56, 58, 61, 63,
            66, 68, 70, 73, 75, 78, 80, 82, 85, 87, 90, 92, 94,};
    private final int[] whites_key_list = {36, 38, 40, 41, 43, 45, 47, 48, 50, 52, 53, 55, 57, 59, 60,
            62, 64, 65, 67, 69, 71, 72, 74, 76, 77, 79, 81, 83, 84, 86, 88, 89, 91, 93, 95, 96};

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

    private ArrayList<Key> MIDI_KEY = new ArrayList<>();

    //Size of piano view
    //default by 7 -> 1 octave
    int piano_size = 36;

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
        this.pianoViewListener = null;
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
        this.pianoViewListener = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //get current value of width
        W = w;

        //Default_value
        keyWidth = w / piano_size;
        keyHeight = h;

        whites_key.clear();
        blacks_key.clear();

        _PianoViewProperty = new PianoViewProperty(w, h, oldw, oldh, keyWidth, keyHeight);

        int blacks_key_length = blacks_key_list.length;
        int whites_key_length = whites_key_list.length;
        int index1 = 0;
        int index2 = 0;

        Log.i("PI_onSizeChanged:: ", "Start");

        for (int i = 0; i <= KEYS_SIZE; i++) {
            int left = i * keyWidth;
            int right = left + keyWidth;


            Log.i("PI_onSizeChanged:: ", "keyWidth = " + keyWidth);
            RectF rect = new RectF(left, 0, right, h);
            // set midi note for this keys
            if (index1 < whites_key_length) {
                whites_key.add(new Key(whites_key_list[index1], rect, false));
                index1++;
            }
            Log.i("PI_onSizeChanged:: ", "white_keys_rect = " + rect);


            // the index 0 3 7 10 is note that in the left of it not contain the black key.
            // mean that in the left of note contain a white note
            // Ex

            if (i != 0 && i != 3 && i != 7 && i != 10 && i != 14 && i != 17 && i != 21 && i != 24 && i != 28 && i != 31 && i != 35 && i != 38
                    && i != 42 && i != 45 && i != 49 && i != 52) {

                rect = new RectF((float) (i - 1) * keyWidth + 0.5f * keyWidth + 0.25f * keyWidth, 0,
                        (float) i * keyWidth + 0.25f * keyWidth, 0.67f * keyHeight);

                Log.i("PI_onSizeChanged:: ", "black_keys_rect = " + rect);
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
        Log.i("PI_onDraw:: ", "start  ");
        for (Key k : whites_key) {
            canvas.drawRect(k.rect, k.isNoteOn ? blue : white);
            Log.i("PI_onDraw:: ", "white_keys_rect = " + k.rect);
        }

        for (int i = 1; i <= KEYS_SIZE; i++) {
            canvas.drawLine(i * keyWidth, 0, i * keyWidth, keyHeight, black);
        }

        for (Key k : blacks_key) {
            canvas.drawRect(k.rect, k.isNoteOn ? blue : black);
            Log.i("PI_onDraw:: ", "blacks_key_rect = " + k.rect);
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setPianoViewWidth(int _NewSize_) {
        Log.i("PianoLargeView:: ", "Goto setPianoViewWidth");
        Log.i("PianoLargeView:: ", "piano_size = " + piano_size);

        piano_size = _NewSize_;

        if (piano_size == 0)
            piano_size = 1;

        //update view
        keyWidth = W / piano_size;

        onSizeChanged(_PianoViewProperty.getW(), _PianoViewProperty.getH(), _PianoViewProperty.getOldw(), _PianoViewProperty.getOldh());

        invalidate();
        requestLayout();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void resetPianoSize() {
        setPianoViewWidth(7);
    }


//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        int action = event.getAction();
//        boolean isDownAction = action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE;
//       // boolean isMoveAction = action == MotionEvent.ACTION_MOVE;
//       // boolean isUpAction = action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_MOVE;
//       // ArrayList<Integer> actionMoveList = new ArrayList<>();
//
//
//        for (int touchIndex = 0; touchIndex < event.getPointerCount(); touchIndex++) {
//            float x = event.getX(touchIndex);
//            float y = event.getY(touchIndex);
//
//            Key keyPressed = getKeyAtPosition(x, y);
//
//            if (keyPressed != null) {
//
//               // actionMoveList.add(keyPressed.note);
//                keyPressed.isKeyDown = isDownAction;
//               // keyPressed.isNoteOff = isUpAction;
//               // Log.i("ACTION_DOWN: Note ", String.valueOf(keyPressed.note));
//
//
//                //Handle action down
//                if (keyPressed.isNoteOn) {
//                    try {
////                        ShortMessage msg = new ShortMessage();
////                        msg.setMessage(ShortMessage.NOTE_ON, 0, keyPressed.note, 80);
////                        recv.send(msg, -1);
//
//                        //Draw the key buffer to screen
//                        invalidate();
//                        releaseKey(keyPressed);
//                        keyPressed.isNoteOff = true;
//                        pianoViewListener.onNoteOnListener(keyPressed.note);
//
//                    } catch (/*InvalidMidiDataException |*/ NullPointerException e) {
//                        Log.e("PIANO VIEW", Objects.requireNonNull(e.getMessage()));
//                        e.printStackTrace();
//                    }
//                }
//
//                // handle action up
//                Log.i("ACTION_UP:_check ", String.valueOf(isUpAction));
//                if (keyPressed.isNoteOff && isUpAction) {
//
//                    try {
//
//                        Log.i("__ACTION_UP: ", String.valueOf(isUpAction));
//
////                        ShortMessage msg = new ShortMessage();
////                        msg.setMessage(ShortMessage.NOTE_OFF, 0, keyPressed.note, 0);
////                        recv.send(msg, -1);
//
//                        //releaseKey(keyPressed);
//                        pianoViewListener.onNoteOffListener(keyPressed.note);
//
//                    } catch (/*InvalidMidiDataException |*/ NullPointerException e) {
//                        Log.e("PIANO VIEW", e.getMessage());
//                        e.printStackTrace();
//                    }
//
//                }
//
//            }
//
//        }
//        return true;
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        boolean isDownAction = action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE;
        for (int touchIndex = 0; touchIndex < event.getPointerCount(); touchIndex++) {
            float x = event.getX(touchIndex);
            float y = event.getY(touchIndex);

            Key keyPressed = getKeyAtPosition(x, y);

            if (keyPressed != null) {
                keyPressed.isKeyDown = isDownAction;
            }

            ArrayList<Key> pianoKey = new ArrayList<>(whites_key);
            pianoKey.addAll(blacks_key);

            for (Key k : pianoKey) {
                if (k.isKeyDown) {
                    if (!k.isNoteOn) {
                        try {
                            ShortMessage msg = new ShortMessage();
                            msg.setMessage(ShortMessage.NOTE_ON, 0, keyPressed.note, 110);
                            recv.send(msg, -1);
                            pianoViewListener.onNoteOnListener(keyPressed.note);
                            int index = pianoKey.indexOf(k);
                            pianoKey.get(index).isNoteOn = true;
                            Handler handler = new Handler();
                            handler.postDelayed(() -> {
                                //releaseKey(keyPressed);

                                pianoViewListener.onNoteOffListener(keyPressed.note);

                                try {
                                    pianoKey.get(index).isNoteOn = false;
                                    ShortMessage msg1 = new ShortMessage();
                                    msg1.setMessage(ShortMessage.NOTE_OFF, 0, keyPressed.note, 0);
                                    recv.send(msg1, -1);
                                } catch (InvalidMidiDataException | NullPointerException e) {
                                    e.printStackTrace();
                                }
                            }, 1500); //1500 = 1.5 seconds, time in milli before it happens.

                        } catch (InvalidMidiDataException | NullPointerException e) {
                            e.printStackTrace();
                        }

                    }

                }

            }
        }
        return true;
    }


    private Key getKeyAtPosition(float x, float y) {
        for (Key k : blacks_key) {
            if (k.rect.contains(x, y)) {
                return k;
            }
        }

        for (Key k : whites_key) {
            if (k.rect.contains(x, y)) {
                return k;
            }
        }

        return null;
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
        Pair<Integer, Integer> temp;
        int index = getKeyIndexByNoteNumber_(noteNumber, whites_key);
        temp = new Pair<>(index, 0);
        if (index == -1) {
            index = getKeyIndexByNoteNumber_(noteNumber, blacks_key);
            temp = new Pair<>(index, 1);
        }

        return temp;
    }


    public void setKey(int noteNumber, boolean isNoteOn) {
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
}
