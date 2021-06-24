package com.kelvin.midi.ezmusic.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MotionEventCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kelvin.midi.ezmusic.R;
import com.kelvin.midi.ezmusic.customview.ChordView;
import com.kelvin.midi.ezmusic.customview.Staff;
import com.kelvin.midi.ezmusic.object.ChordType;
import com.kelvin.midi.ezmusic.object.KeyMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import cn.sherlock.com.sun.media.sound.SF2Soundbank;
import cn.sherlock.com.sun.media.sound.SoftSynthesizer;
import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.Receiver;
import jp.kshoji.javax.sound.midi.ShortMessage;

public class MidiChordActivity extends AppCompatActivity {
    public ChordView chordView;
    public Staff staffView;
    public ArrayList<Integer> currentChordList;

    //ChordName
    TextView txt_chordName;

    //Chord Detect
    final Handler chordDetectEventHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            txt_chordName.setText((String) message.obj);
            return true;
        }

    });

    //Synthesizer
    private SoftSynthesizer synth;
    private Receiver receiver;
    private boolean isPedalHolding = false;
    private final ShortMessage msg = new ShortMessage();

    final private String DEFAULT_INSTRUMENT = "GrandPiano";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_midi_chord);

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

        //Init ChordView
        chordView = new ChordView(this);
        chordView = findViewById(R.id.chord_view);
        chordView.setReceiverForSynthesizer(receiver);

        //Init StaffView
        staffView = new Staff(this);
        staffView = findViewById(R.id.staff_view);
        staffView.setVisibility(View.VISIBLE);

        //Init ChordName TextView
        txt_chordName = findViewById(R.id.txt_chordName);
        txt_chordName.setText("");

        //Init chord list
        currentChordList = new ArrayList<Integer>();
        List<Integer> chordList ;


        Button btnSetKey = findViewById(R.id.btnSetKey);
        btnSetKey.setOnClickListener(v -> {
            chordView.setKey(53, true);
            chordView.setKey(57, true);
            chordView.setKey(60, true);
        });

        Button btnClearChord = findViewById(R.id.btnDeleteKey);
        btnClearChord.setOnClickListener(v -> {
            chordView.releaseChordKey();
            staffView.releaseNote();
            txt_chordName.setText("");
        });


        int rootKey = 57; //key C
        ArrayList<ChordType> ChordNoteMidi = new ArrayList<>();
        ChordType chordType = new ChordType();
        ArrayList<ChordType> chordLoop = chordType.GenerateBasicChord();
        for (ChordType chord : chordLoop
        ) {
            ChordType temp = new ChordType();
            for (int chordNote : chord.chord_note
            ) {
                temp.AddChordNote(rootKey + chordNote);
                temp.name = chord.name;
            }
            ChordNoteMidi.add(temp);
        }

        chordList = new ArrayList<>();
        AtomicInteger selected = new AtomicInteger();
        Button btnSetStaffNote = findViewById(R.id.btnSetStaffNote);
        btnSetStaffNote.setOnClickListener(v -> {
            if (selected.get() >= ChordNoteMidi.size()) {
                selected.set(0);
            }

            chordList.clear();
            for (int note : ChordNoteMidi.get(selected.get()).chord_note
            ) {
                chordList.add(note);
            }

            staffView.setNoteToStaff((ArrayList<Integer>) chordList);

            chordView.releaseChordKey();
            for (int note : chordList
            ) {
                chordView.setKey(note, true);
            }
            KeyMap k = new KeyMap();
            String root = k.GenerateNoteName(rootKey);
            String extension = ChordNoteMidi.get(selected.get()).name;

            txt_chordName.setText(root + " " + extension);

            selected.set(selected.get() + 1);
        });

        Button btnPlayChord = findViewById(R.id.btnPlayChord);
        btnPlayChord.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    if(chordList.get(0) + 12<72){
                        for(int index = 0;index<chordList.size();index++)
                        {
                            chordList.set(index, chordList.get(index)+ 12) ;
                        }
                    }
                   chordView.StartPlayingChord((ArrayList<Integer>) chordList);
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                   chordView.StopPlayingChord((ArrayList<Integer>) chordList);
                }
                return true;
            }
        });

        Button btnSelectSound = findViewById(R.id.btnSelectSound);
        final Intent InstrumentIntent = new Intent(this, InstrumentsActivity.class);
        btnSelectSound.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {

                // selectedSound.startAnimation(animCycle);
                startActivityForResult(InstrumentIntent, 1);
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            String instrument_path = data.getStringExtra(InstrumentsActivity.RESULT_INSTRUMENT_PATH);
            Toast.makeText(this, "You selected instrument: " + instrument_path, Toast.LENGTH_LONG).show();

            try {
                if (synth != null)
                    synth.close();

                synth = new SoftSynthesizer();
                try {
                    SF2Soundbank sf = new SF2Soundbank(getAssets().open(instrument_path + ".sf2"));
                    synth = new SoftSynthesizer();
                    synth.open();
                    synth.loadAllInstruments(sf);
                    synth.getChannels()[0].programChange(0);
                    receiver = synth.getReceiver();

                    //set receive for pianoView
                    chordView.setReceiverForSynthesizer(receiver);

                } catch (Exception e) {
                    Log.e("LOAD SOUND: ", "CAN NOT LOAD SOUND!");
                    showMessage("Can not load sound, Please try again!");
                    SF2Soundbank sf = new SF2Soundbank(getAssets().open(DEFAULT_INSTRUMENT + ".sf2"));
                    synth = new SoftSynthesizer();
                    synth.open();
                    synth.loadAllInstruments(sf);
                    synth.getChannels()[0].programChange(0);
                    receiver = synth.getReceiver();

                    //set receive for pianoView
                    chordView.setReceiverForSynthesizer(receiver);

                }
            } catch (IOException | MidiUnavailableException e) {
                e.printStackTrace();
            }

        }
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}