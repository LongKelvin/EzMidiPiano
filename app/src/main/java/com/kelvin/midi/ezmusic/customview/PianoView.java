package com.kelvin.midi.ezmusic.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.kelvin.midi.ezmusic.object.Key;

import java.util.ArrayList;
import java.util.Objects;

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.Receiver;
import jp.kshoji.javax.sound.midi.ShortMessage;

public class PianoView extends View {


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


    public PianoView(Context context, AttributeSet attrs) {
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


    }

    public PianoView(Context context) {
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

    }

    /**
     * This is called during layout when the size of this view has changed. If
     * you were just added to the view hierarchy, you're called with the old
     * values of 0.
     *
     * @param w    Current width of this view.
     * @param h    Current height of this view.
     * @param oldw Old width of this view.
     * @param oldh Old height of this view.
     */
    @RequiresApi(api = VERSION_CODES.N)
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        keyWidth = w / 35;
        keyHeight = h;

        int blacks_key_length = blacks_key_list.length;
        int whites_key_length = whites_key_list.length;
        int index1 = 0;
        int index2 = 0;

        //21 is the number of midi note started
        //max octave 6

        //SIze of keys are currently 61keys_6 octave
        int KEYS_SIZE = 61;
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

            if (i != 0 && i != 3 && i != 7 && i != 10 && i != 14 && i != 17 && i != 21 && i != 24 && i != 28 && i != 31 && i != 35 && i != 38
                    && i != 42 && i != 45 && i != 49 && i != 52) {

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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (Key k : whites_key) {
            canvas.drawRect(k.rect, k.isNoteOn ? blue : white);
        }

        for (int i = 1; i <= 61; i++) {
            canvas.drawLine(i * keyWidth, 0, i * keyWidth, keyHeight, black);
        }

        for (Key k : blacks_key) {
            canvas.drawRect(k.rect, k.isNoteOn ? blue : black);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        boolean isDownAction = action == MotionEvent.ACTION_DOWN; //|| action == MotionEvent.ACTION_MOVE;
        boolean isMoveAction = action == MotionEvent.ACTION_MOVE;
        boolean isUpAction = action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_MOVE;
        ArrayList<Integer> actionMoveList = new ArrayList<>();


        for (int touchIndex = 0; touchIndex < event.getPointerCount(); touchIndex++) {
            float x = event.getX(touchIndex);
            float y = event.getY(touchIndex);

            Key keyPressed = getKeyAtPosition(x, y);

            if (keyPressed != null) {

                actionMoveList.add(keyPressed.note);
                keyPressed.isNoteOn = isDownAction;
                keyPressed.isNoteOff = isUpAction;
                Log.i("ACTION_DOWN: Note ", String.valueOf(keyPressed.note));


                //Handle action down
                if (keyPressed.isNoteOn) {
                    try {
                        ShortMessage msg = new ShortMessage();
                        msg.setMessage(ShortMessage.NOTE_ON, 0, keyPressed.note, 80);
                        recv.send(msg, -1);

                        //Draw the key buffer to screen
                        invalidate();
                        releaseKey(keyPressed);
                        keyPressed.isNoteOff = true;
                        NoteIsPlaying = keyPressed.note;

                    } catch (InvalidMidiDataException | NullPointerException e) {
                        Log.e("PIANO VIEW", Objects.requireNonNull(e.getMessage()));
                        e.printStackTrace();
                    }
                }

                // handle action up
                Log.i("ACTION_UP:_check ", String.valueOf(isUpAction));
                if (keyPressed.isNoteOff && isUpAction) {

                    try {

                        Log.i("__ACTION_UP: ", String.valueOf(isUpAction));

                        ShortMessage msg = new ShortMessage();
                        msg.setMessage(ShortMessage.NOTE_OFF, 0, keyPressed.note, 0);
                        recv.send(msg, -1);

                        //releaseKey(keyPressed);

                    } catch (InvalidMidiDataException | NullPointerException e) {
                        Log.e("PIANO VIEW", e.getMessage());
                        e.printStackTrace();
                    }

                }

//                if (isMoveAction) {
//                    int flag = 0;
//                    if (actionMoveList.size() == 0) {
//                        actionMoveList.add(keyPressed.note);
//                    } else {
//                        for (int index = 0; index < actionMoveList.size(); index++) {
//                            if (actionMoveList.get(index) == keyPressed.note) {
//                                flag = 1;
//                            }
//                        }
//                        if (flag != 1) {
//                            actionMoveList.add(keyPressed.note);
//                            try {
//                                ShortMessage msg = new ShortMessage();
//                                msg.setMessage(ShortMessage.NOTE_ON, 0, keyPressed.note, 80);
//                                recv.send(msg, -1);
//
//                                //Draw the key buffer to screen
//                                invalidate();
//
//                                releaseKey(keyPressed);
//                                keyPressed.isNoteOff = true;
//
//                            } catch (InvalidMidiDataException | NullPointerException e) {
//                                Log.e("PIANO VIEW", e.getMessage());
//                                e.printStackTrace();
//                            }
//
//                        } else {
//                            try {
//
//                                ShortMessage msg = new ShortMessage();
//                                msg.setMessage(ShortMessage.NOTE_OFF, 0, keyPressed.note, 0);
//                                recv.send(msg, -1);
//
//                                releaseKey(keyPressed);
//
//                            } catch (InvalidMidiDataException | NullPointerException e) {
//                                Log.e("PIANO VIEW", e.getMessage());
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                }
//
//            }

//            ArrayList<Key> temporary = new ArrayList<>(whites_key);
//            temporary.addAll(blacks_key);
//
//            for (Key k : temporary) {
//
//                if (k.isNoteOn) {
//                    try {
//                        ShortMessage msg = new ShortMessage();
//                        msg.setMessage(ShortMessage.NOTE_ON, 0, k.note, 80);
//                        recv.send(msg, -1);
//
//                        //Draw the key buffer to screen
//                        invalidate();
//                        releaseKey(k);
//                        k.isNoteOff = true;
//
//                    } catch (InvalidMidiDataException | NullPointerException e) {
//                        Log.e("PIANO VIEW", e.getMessage());
//                        e.printStackTrace();
//                    }
//                }
//
//                Log.i("ACTION_UP:_check ", String.valueOf(isUpAction));
//                if (k.isNoteOff && isUpAction) {
//
//                    try {
//
//                        Log.i("__ACTION_UP: ", String.valueOf(isUpAction));
//
//                        ShortMessage msg = new ShortMessage();
//                        msg.setMessage(ShortMessage.NOTE_OFF, 0, k.note, 0);
//                        recv.send(msg, -1);
//
//                        releaseKey(k);
//
//                    } catch (InvalidMidiDataException | NullPointerException e) {
//                        Log.e("PIANO VIEW", e.getMessage());
//                        e.printStackTrace();
//                    }
//
//                }
//
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

    public int getNoteIsPlaying() {
        //MainActivity.activeNoteToScreen(NoteIsPlaying);
        return 0;
    }
}
