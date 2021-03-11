package com.kelvin.midi.ezmusic.view;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.kelvin.midi.ezmusic.R;
import com.kelvin.midi.ezmusic.object.KeyMap;

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
    int piano_size_on_change = 7;
    int _noteOn = 0;
    int _noteOff = 0;
    private Receiver receiver;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piano_large_view);

        //Init PianoView
        pianoLargeView = new PianoLargeView(this);
        pianoLargeView = findViewById(R.id.piano_large_view);


        Button btn_increase_piano_size = findViewById(R.id.btn_increase_piano_size);
        Button btn_decrease_piano_size = findViewById(R.id.btn_decrease_piano_size);
        Button btn_reset_piano_size = findViewById(R.id.btn_reset_piano_size);

        btn_increase_piano_size.setOnClickListener(view -> {
            if (piano_size_on_change < 36)
                piano_size_on_change += 1;
            pianoLargeView.setPianoViewWidth(piano_size_on_change);
        });

        btn_decrease_piano_size.setOnClickListener(view -> {
            if (piano_size_on_change == 0)
                piano_size_on_change = 2;
            piano_size_on_change -= 1;
            pianoLargeView.setPianoViewWidth(piano_size_on_change);
        });

        btn_reset_piano_size.setOnClickListener(view -> {
            pianoLargeView.resetPianoSize();
        });

        pianoLargeView.setPianoViewListener(new PianoLargeView.PianoViewListener() {
            @Override
            public void onNoteOnListener(int noteOn) {
                _noteOn = noteOn;
                Log.e("PianoView_noteOn:: ", String.valueOf(_noteOn));
            }

            @Override
            public void onNoteOffListener(int noteOff) {
                _noteOff = noteOff;
                Log.e("PianoView_noteOff:: ", String.valueOf(_noteOff));
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
}