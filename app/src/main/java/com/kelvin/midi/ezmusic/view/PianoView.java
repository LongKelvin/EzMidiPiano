package com.kelvin.midi.ezmusic.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.kelvin.midi.ezmusic.object.Key;

import java.util.ArrayList;

import androidx.annotation.RequiresApi;

class PianoView extends View {


    private static int keyWidth;
    private static int keyHeight;

    private Paint black, blue, white;

    private ArrayList<Key> whites_key = new ArrayList<>();
    private ArrayList<Key> blacks_key = new ArrayList<>();
    private int[] blacks_key_list = {22, 25, 27, 30, 32, 34, 37, 39, 42, 44, 46, 49, 51, 54, 56, 58, 61, 63,
            66, 68, 70, 73, 75, 78, 80, 82, 85, 87, 90, 92, 94, 97, 99, 102, 104, 106};

    private int keylength = 52;  //white keys


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
        int MidiNote = 36;

        //21 is the number of midi note started
        //max octave 6
        for (int i = 0; i <= 61; i++) {
            int left = i * keyWidth;
            int right = left + keyWidth;


            RectF rect = new RectF(left, 0, right, h);
            whites_key.add(new Key(MidiNote, rect, false));


            // the index 0 3 7 10 is note that in the right of it not contain the black key.
            // mean that in the right of note contain a white note
            // Ex

            if (i != 0 && i != 3 && i != 7 && i != 10 && i != 14 && i != 17 && i != 21 && i != 24 && i != 28 && i != 31 && i != 35 && i != 38
                    && i != 42 && i != 45 && i != 49 && i != 52) {
                MidiNote += 1;
                rect = new RectF((float) (i - 1) * keyWidth + 0.5f * keyWidth + 0.25f * keyWidth, 0,
                        (float) i * keyWidth + 0.25f * keyWidth, 0.67f * keyHeight);
                blacks_key.add(new Key(MidiNote, rect, false));
            }
            MidiNote += 1;
        }
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
        boolean isDownAction = action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE;

        for (int touchIndex = 0; touchIndex < event.getPointerCount(); touchIndex++) {
            float x = event.getX(touchIndex);
            float y = event.getY(touchIndex);

            Key k = keyForCoords(x, y);

            if (k != null) {
                k.isNoteOn = isDownAction;
                Log.e("ACTION_DOWN: ", String.valueOf(k.note));
            }
        }

        return true;
    }

    private Key keyForCoords(float x, float y) {
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

}
