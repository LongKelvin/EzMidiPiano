package com.kelvin.midi.ezmusic.view;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.kelvin.midi.ezmusic.R;
import com.kelvin.midi.ezmusic.customview.PianoLargeView;

import java.io.IOException;

import cn.sherlock.com.sun.media.sound.SF2Soundbank;
import cn.sherlock.com.sun.media.sound.SoftSynthesizer;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.Receiver;
import jp.kshoji.javax.sound.midi.ShortMessage;

public class PianoLargeViewActivity extends AppCompatActivity {
    private ShortMessage msg = new ShortMessage();

    final private String DEFAULT_INSTRUMENT = "GrandPiano";
    private SoftSynthesizer synth;
    PianoLargeView pianoLargeView;
    int piano_size_on_change = 36;
    int _noteOn = 0;
    int _noteOff = 0;
    private Receiver receiver;

    //View
    HorizontalScrollView horizontalScrollView;
    int pianoViewWidth = 3200;
    SeekBar pianoViewSeekBar;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //create landscape screen
        //request full screen for login activity
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_piano_large_view);

        horizontalScrollView = findViewById(R.id.scrollView);
        horizontalScrollView.setScrollBarSize(300);
        horizontalScrollView.setHorizontalScrollBarEnabled(true);

        //Init PianoView
        pianoLargeView = new PianoLargeView(this);
        pianoLargeView = findViewById(R.id.piano_large_view);
        pianoViewWidth = pianoLargeView.getWidth();


        //Set PianoView to Center
        SetPianoViewToCenter();

        //Seek bar
        pianoViewSeekBar = findViewById(R.id.pianoViewSeekBar);
        pianoViewSeekBar.setMax(36);
        pianoViewSeekBar.setProgress(36);
        this.pianoViewSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 36;

            // When Progress value changed.
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                if(progressValue<20)
                    progressValue = 20;

                progress = progressValue;
                pianoLargeView.setPianoViewWidth(progress);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


            }
        });


        pianoLargeView.setPianoViewListener(new PianoLargeView.PianoViewListener() {
            @Override
            public void onNoteOnListener(int noteOn) {
                _noteOn = noteOn;
                Log.e("PianoView_noteOn:: ", String.valueOf(_noteOn));
                //pianoLargeView.setKey(noteOn,true);
            }

            @Override
            public void onNoteOffListener(int noteOff) {
                _noteOff = noteOff;
                Log.e("PianoView_noteOff:: ", String.valueOf(_noteOff));
                //pianoLargeView.setKey(noteOff,false);
            }
        });

        //Setup Synthesizers SF2_Sound
        try {
            SF2Soundbank sf = new SF2Soundbank(getAssets().open(DEFAULT_INSTRUMENT + ".sf2"));
            synth = new SoftSynthesizer();
            synth.open();
            synth.loadAllInstruments(sf);
            synth.getChannels()[0].programChange(0);
            receiver = synth.getReceiver();
        } catch (IOException | MidiUnavailableException | IllegalStateException e) {
            e.printStackTrace();
        }

        pianoLargeView.setReceiverForSynthesizer(receiver);
    }

    private void SetPianoViewToCenter() {
        int hsvWidth = horizontalScrollView.getWidth();
        int offset = pianoViewWidth/2;

        // Horizontal smooth scroll offset
        horizontalScrollView.smoothScrollBy(500, 0);
    }
}