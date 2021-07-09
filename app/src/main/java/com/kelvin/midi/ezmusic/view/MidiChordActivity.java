package com.kelvin.midi.ezmusic.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.kelvin.midi.ezmusic.R;
import com.kelvin.midi.ezmusic.customview.ChordView;
import com.kelvin.midi.ezmusic.customview.StaffView;
import com.kelvin.midi.ezmusic.object.ChordType;
import com.kelvin.midi.ezmusic.object.KeyMap;
import com.kelvin.midi.ezmusic.object.Note;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import cn.sherlock.com.sun.media.sound.SF2Soundbank;
import cn.sherlock.com.sun.media.sound.SoftSynthesizer;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.Receiver;
import jp.kshoji.javax.sound.midi.ShortMessage;

public class MidiChordActivity extends AppCompatActivity {
    public ChordView chordView;
    public StaffView staffView;
    public ArrayList<Integer> currentChordList;

    //Button array
    ArrayList<Button> ChordArrayButton;
    ArrayList<Button> RootNoteArrayButton;

    //ChordName
    TextView txt_chordName;

    //ChordList
    ArrayList<ChordType> ChordNoteMidi;
    List<Integer> chordList;
    public int RootNote = 60;

    ArrayList<Note> ListOfRootNote;

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

    // Chord Dialog
    AlertDialog.Builder chord_dialog_builder;
    AlertDialog chord_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //request full screen for login activity
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_midi_chord2);

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
        staffView = new StaffView(this);
        staffView = findViewById(R.id.staff_view);
        staffView.setVisibility(View.VISIBLE);
        staffView.enableChordMode(true);

        //Init ChordName TextView
        txt_chordName = findViewById(R.id.txt_chordName);
        txt_chordName.setText("");

        //Init chord list
        currentChordList = new ArrayList<Integer>();

        //Init RootNote List
        Note rootNote = new Note();
        ListOfRootNote = rootNote.createRootNoteForChord();

        Button btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(v -> {
            chordView.releaseChordKey();
            staffView.releaseNote();
            txt_chordName.setText("");
        });

        ChordNoteMidi = new ArrayList<>();
        ChordType chordType = new ChordType();
        ChordNoteMidi = chordType.GenerateBasicChord();

        chordList = new ArrayList<>();
        AtomicInteger selected = new AtomicInteger();
        ImageButton btnNextChord = findViewById(R.id.btnNextChord);
        btnNextChord.setOnClickListener(v -> {
            if (selected.get() >= ChordNoteMidi.size()) {
                selected.set(0);
            }

            chordList.clear();
            for (int note : ChordNoteMidi.get(selected.get()).chord_note
            ) {
                chordList.add(RootNote + note);
            }

            staffView.setNoteToStaff((ArrayList<Integer>) chordList);

            chordView.releaseChordKey();
            for (int note : chordList
            ) {
                chordView.setKey(note, true);
            }
            KeyMap k = new KeyMap();
            String root = k.GenerateNoteName(RootNote);
            String extension = ChordNoteMidi.get(selected.get()).name;

            txt_chordName.setText(root + " " + extension);

            selected.set(selected.get() + 1);
        });

        ImageButton btnPreviousChord = findViewById(R.id.btnSPreviousChord);
        btnPreviousChord.setOnClickListener(v -> {
            if (selected.get() <0) {
                selected.set(ChordNoteMidi.size()-1);
            }

            chordList.clear();
            for (int note : ChordNoteMidi.get(selected.get()).chord_note
            ) {
                chordList.add(RootNote + note);
            }

            staffView.setNoteToStaff((ArrayList<Integer>) chordList);

            chordView.releaseChordKey();
            for (int note : chordList
            ) {
                chordView.setKey(note, true);
            }
            KeyMap k = new KeyMap();
            String root = k.GenerateNoteName(RootNote);
            String extension = ChordNoteMidi.get(selected.get()).name;

            txt_chordName.setText(root + " " + extension);

            if(selected.get()<=0){
                selected.set(ChordNoteMidi.size()-1);
                Log.i("SELECTED_VALUE:: ", String.valueOf(selected.get()));
            }
            else{
                selected.set(selected.get() - 1);
                Log.i("SELECTED_VALUE:: ", String.valueOf(selected.get()));
            }

        });


        //Init Chord Dialog
        showChordTable(ChordNoteMidi, ListOfRootNote);
        chord_dialog.hide();

        ImageButton btnPlayChord = findViewById(R.id.btnPlayChord);
        btnPlayChord.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    if (chordList.size() == 0)
                        return false;
                    if (chordList.get(0) + 12 < 72) {
                        for (int index = 0; index < chordList.size(); index++) {
                            chordList.set(index, chordList.get(index) + 12);
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

        Button btnSelectChord = findViewById(R.id.btnSelectChord);
        btnSelectChord.setOnClickListener(v -> {
            chord_dialog.show();
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

    @SuppressLint("ClickableViewAccessibility")
    public void showChordTable(ArrayList<ChordType> listChordData, ArrayList<Note> listRootNote) {
        // create an alert builder
        chord_dialog_builder = new AlertDialog.Builder(this,R.style.MyDialogTheme);
        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.midi_chord_dialog, null);
        chord_dialog_builder.setView(customLayout);
        // add a button

        Button button_ok = customLayout.findViewById(R.id.button_ok);
        Button button_cancel = customLayout.findViewById(R.id.button_cancel);
        TableLayout table_chord = customLayout.findViewById(R.id.table_chord);
        TableLayout table_rootNote = customLayout.findViewById(R.id.table_rootNote);

        //create selected val
        AtomicInteger selectedChordIndex = new AtomicInteger();
        AtomicInteger selectedRootNoteIndex = new AtomicInteger();
        //Init the button array
        ChordArrayButton = new ArrayList<>();
        RootNoteArrayButton = new ArrayList<>();
        // temp val for checking button is clicked or not
        final int[] pressed_btn = {0};
        final int[] pressed_btn_rootNote = {0};

        //Init RootNote
        for (int index = 0; index < listRootNote.size(); index++) {
            TableRow tableRow = new TableRow(this);
            TableRow.LayoutParams param = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT, 1.0f);
            tableRow.setLayoutParams(param);
            tableRow.setGravity(Gravity.CENTER_VERTICAL);

            for (int horizon_btn = 0; horizon_btn < 5 && index < listRootNote.size(); horizon_btn++) {
                Button rootNote = new Button(this);
                rootNote.setId(index);
                rootNote.setText(listRootNote.get(index).getNoteName());
                rootNote.setTag(index);
                rootNote.setBackgroundColor(Color.WHITE);
                rootNote.setVisibility(View.VISIBLE);
                rootNote.setAllCaps(false);

                RootNoteArrayButton.add(rootNote);
                rootNote.setOnClickListener(v -> {
                    if (pressed_btn_rootNote[0] != 0) {
                        ResetBackgroundColor(selectedRootNoteIndex.get(), RootNoteArrayButton);
                    }

                    String btnIndexTag = rootNote.getTag().toString();
                    selectedRootNoteIndex.set(Integer.parseInt(btnIndexTag));
                    rootNote.setBackgroundColor(Color.GREEN);
                    pressed_btn_rootNote[0] = 1;
                });

                TableRow.LayoutParams par = new TableRow.LayoutParams(horizon_btn);
                rootNote.setLayoutParams(par);
                tableRow.addView(rootNote);
                index++;
            }

            table_rootNote.addView(tableRow);
        }


        //Init Chord Extension
        for (int index = 0; index < listChordData.size(); index++) {
            TableRow tableRow = new TableRow(this);
            TableRow.LayoutParams param = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT, 1.0f);
            tableRow.setLayoutParams(param);
            tableRow.setGravity(Gravity.CENTER_VERTICAL);

            for (int horizon_btn = 0; horizon_btn < 5 && index < listChordData.size(); horizon_btn++) {
                Button btnChord = new Button(this);
                btnChord.setId(index);
                btnChord.setText(listChordData.get(index).name);
                btnChord.setTag(index);
                btnChord.setBackgroundColor(Color.WHITE);
                btnChord.setVisibility(View.VISIBLE);
                btnChord.setAllCaps(false);

                ChordArrayButton.add(btnChord);

                btnChord.setOnClickListener(v -> {
                    if (pressed_btn[0] != 0) {
                        ResetBackgroundColor(selectedChordIndex.get(), ChordArrayButton);
                    }

                    System.out.println("Button " + btnChord.getTag().toString());
                    String btnIndexTag = btnChord.getTag().toString();
                    selectedChordIndex.set(Integer.parseInt(btnIndexTag));
                    btnChord.setBackgroundColor(Color.GREEN);
                    pressed_btn[0] = 1;
                });

                TableRow.LayoutParams par = new TableRow.LayoutParams(horizon_btn);
                btnChord.setLayoutParams(par);
                tableRow.addView(btnChord);
                index++;
            }

            table_chord.addView(tableRow);
        }


        // create and show the recording dialog
        chord_dialog = chord_dialog_builder.create();
        chord_dialog.show();


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(chord_dialog.getWindow().getAttributes());
        int dialogWindowWidth = (int) (displayWidth * 0.6f);
        int dialogWindowHeight = (int) (displayHeight * 1.03f);
        layoutParams.width = dialogWindowWidth;
        layoutParams.height = dialogWindowHeight;
        chord_dialog.getWindow().setAttributes(layoutParams);

        //Button handle
        button_ok.setOnClickListener(v -> {
            //SOMETHING HERE
            setChord(selectedRootNoteIndex.get(), selectedChordIndex.get());
            chord_dialog.hide();
        });

        button_cancel.setOnClickListener(v -> {
            chord_dialog.hide();

        });

    }

    public void setChord(int rootNoteIndex, int chord_extensionIndex) {
        int rootNote = ListOfRootNote.get(rootNoteIndex).getNoteNumber();
        ChordType chord = ChordNoteMidi.get(chord_extensionIndex);
        if (chord == null)
            return;
        //Check If RootNote is too high
        //System.out.println("LASTNOTE: " + chord.chord_note.get(2));
        if ((chord.chord_note.get(chord.chord_note.size() - 1) + rootNote) + 4 >= 72) {
            rootNote -= 12;
        }

        RootNote = rootNote;
        chordList.clear();
        chordView.releaseChordKey();

        for (int note : chord.chord_note
        ) {
            chordView.setKey(rootNote + note, true);
            chordList.add(rootNote + note);
        }

        staffView.setNoteToStaff((ArrayList<Integer>) chordList);

        String root = ListOfRootNote.get(rootNoteIndex).getNoteName();
        System.out.println("ROOT NAME " + root);
        System.out.println("ROOT NOTE " + rootNote);
        String extension = chord.name;

        txt_chordName.setText(root + " " + extension);
    }

    public void ResetBackgroundColor(int buttonId, ArrayList<Button> buttons) {
        for (int i = 0; i < buttons.size(); i++) {
            if (buttons.get(i).getId() == buttonId) {
                buttons.get(i).setBackgroundColor(Color.WHITE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (synth != null) {
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (synth != null) {
            synth.close();
        }

        chord_dialog.cancel();
    }

}