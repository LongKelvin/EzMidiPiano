package com.kelvin.midi.ezmusic.view;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.kelvin.midi.ezmusic.R;
import com.kelvin.midi.ezmusic.customview.ChordView;
import com.kelvin.midi.ezmusic.customview.PianoView;
import com.kelvin.midi.ezmusic.customview.Staff;

public class MidiChordActivity extends AppCompatActivity {
    public ChordView chordView;
    public Staff staffView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_midi_chord);

        //Init PianoView
        chordView = new ChordView(this);
        chordView = findViewById(R.id.chord_view);

        staffView = new Staff(this);
        staffView = findViewById(R.id.staff_view);
        staffView.setVisibility(View.VISIBLE);



        Button btnSetKey = findViewById(R.id.btnSetKey);
        btnSetKey.setOnClickListener(v->{
            chordView.setKey(53,true);
            chordView.setKey(57,true);
            chordView.setKey(60,true);
        });

        Button btnClearChord = findViewById(R.id.btnDeleteKey);
        btnClearChord.setOnClickListener(v->{
            chordView.releaseChordKey();
        });
    }
}