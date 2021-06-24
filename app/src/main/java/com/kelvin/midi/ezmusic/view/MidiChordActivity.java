package com.kelvin.midi.ezmusic.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kelvin.midi.ezmusic.R;
import com.kelvin.midi.ezmusic.customview.ChordView;
import com.kelvin.midi.ezmusic.customview.Staff;
import com.kelvin.midi.ezmusic.object.ChordType;
import com.kelvin.midi.ezmusic.object.KeyMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MidiChordActivity extends AppCompatActivity {
    public ChordView chordView;
    public Staff staffView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_midi_chord);

        //Init ChordView
        chordView = new ChordView(this);
        chordView = findViewById(R.id.chord_view);

        //Init StaffView
        staffView = new Staff(this);
        staffView = findViewById(R.id.staff_view);
        staffView.setVisibility(View.VISIBLE);

        //Init ChordName TextView
        txt_chordName = findViewById(R.id.txt_chordName);
        txt_chordName.setText("");


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

        List<Integer> chordList = new ArrayList<>();
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
    }


}