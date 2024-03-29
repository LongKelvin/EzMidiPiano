package com.kelvin.midi.ezmusic.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.kelvin.midi.ezmusic.R;
import com.kelvin.midi.ezmusic.customview.PianoLargeView;
import com.kelvin.midi.ezmusic.customview.PianoView;
import com.kelvin.midi.ezmusic.customview.StaffView;
import com.kelvin.midi.ezmusic.object.ChordType;
import com.kelvin.midi.ezmusic.object.KeyMap;
import com.kelvin.midi.ezmusic.object.MidiFileCreator;
import com.kelvin.midi.midilib.event.NoteOff;
import com.kelvin.midi.midilib.event.NoteOn;
import com.midisheetmusic.ChooseSongActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import cn.sherlock.com.sun.media.sound.SF2Soundbank;
import cn.sherlock.com.sun.media.sound.SoftSynthesizer;
import jp.kshoji.driver.midi.device.MidiInputDevice;
import jp.kshoji.driver.midi.device.MidiOutputDevice;
import jp.kshoji.driver.midi.util.UsbMidiDriver;
import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.Receiver;
import jp.kshoji.javax.sound.midi.ShortMessage;

import static com.kelvin.midi.util.Util.print;
import static com.kelvin.midi.util.Util.print_;


public class MainActivity extends Activity implements PopupMenu.OnMenuItemClickListener {

    //midi file creator
    private int tempo_value = -1;
    private final int numerator = -1;
    private final int denominator = -1;
    private String timeSignature_val = "";
    private String songName = "";
    private boolean recordingDialogStatus = true;

    private int ticks = -1;

    //Note Name to Screen
    private KeyMap keyMap;
    private ArrayList<String> ListCurrentNote = new ArrayList<>();

    //Midi Path
    private final File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

    //Note Letter
    private String noteName = " ";
    private TextView NoteLabel;

    //Synthesizer
    private SoftSynthesizer synth;
    private Receiver receiver;
    private boolean isPedalHolding = false;
    private final ShortMessage msg = new ShortMessage();

    final private String DEFAULT_INSTRUMENT = "GrandPiano";

    //custom piano view
    public PianoView piano;
    private int countTimeDisplay = 0;

    //Staff View
    public StaffView staffView;

    //Chord detect
    public ArrayList<Integer> chord_input_note = new ArrayList<>();
    public ArrayList<ChordType> Chord_List = new ArrayList<>();
    // public boolean chord_detect_status = true;
    TextView txtChordDetect;


    ArrayAdapter<String> midiInputEventAdapter;
    ArrayAdapter<String> midiOutputEventAdapter;
    private ToggleButton thruToggleButton;
    Spinner cableIdSpinner;
    Spinner deviceSpinner;

    MidiOutputDevice outputDevice;

    ArrayAdapter<UsbDevice> connectedDevicesAdapter;

    private UsbMidiDriver usbMidiDriver;

    //Recording Midi File
    private MidiFileCreator newMidiFile = new MidiFileCreator();
    private boolean isRecording = false;
    private Button btn_recording;

    // User interface
    final Handler midiInputEventHandler = new Handler(new Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (midiInputEventAdapter != null) {
                midiInputEventAdapter.add((String) msg.obj);
            }
            // message handled successfully
            return true;
        }
    });

    final Handler midiOutputEventHandler = new Handler(new Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (midiOutputEventAdapter != null) {
                midiOutputEventAdapter.add((String) msg.obj);

            }
            // message handled successfully
            return true;
        }
    });

    //Chord Detect
    final Handler chordDetectEventHandler = new Handler(new Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            txtChordDetect.setText((String) message.obj);
            return true;
        }

    });

    //Note Midi Detect
    final Handler noteDetectEventHandler = new Handler(new Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            //NoteLabel.setText((String) message.obj);
            String temp = "";
            for (String data: (ArrayList<String>)message.obj
                 ) {
                temp+=data;
            }

            NoteLabel.setText(temp);

            return true;
        }

    });


    /**
     * Choose device from spinner
     *
     * @return the MidiOutputDevice from spinner
     */


    @Nullable
    MidiOutputDevice getMidiOutputDeviceFromSpinner() {
        if (deviceSpinner != null && deviceSpinner.getSelectedItemPosition() >= 0 && connectedDevicesAdapter != null && !connectedDevicesAdapter.isEmpty()) {
            UsbDevice device = connectedDevicesAdapter.getItem(deviceSpinner.getSelectedItemPosition());
            if (device != null) {
                Set<MidiOutputDevice> midiOutputDevices = usbMidiDriver.getMidiOutputDevices(device);
                if (midiOutputDevices.size() > 0) {
                    // returns the first one.
                    System.out.println("MIDI_PORT: DEVICE_OUTPUT_PORT_CONNECTED");
                    outputDevice = (MidiOutputDevice) Objects.requireNonNull(midiOutputDevices.toArray())[0];
                    return (MidiOutputDevice) Objects.requireNonNull(midiOutputDevices.toArray())[0];
                }
            }
        }
        return null;
    }


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //create landscape screen
        //request full screen for login activity
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_midi_driver_2);

        if (shouldAskPermissions()) {
            askPermissions();
        }


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

        NoteLabel = findViewById(R.id.noteLetter);
        NoteLabel.setText(" ");
        keyMap = new KeyMap();
        keyMap.InitKeyMap();

        ListCurrentNote = new ArrayList<>();


        //Chord Control
        txtChordDetect = findViewById(R.id.chordSymbol);
        txtChordDetect.setText("");

        //Init StaffView
        staffView = new StaffView(this);
        staffView = findViewById(R.id.staff_view);
        staffView.setVisibility(View.VISIBLE);
        staffView.enableChordMode(false);


        //Init PianoView
        piano = new PianoView(this);
        piano = findViewById(R.id.piano_view);
        piano.setReceiverForSynthesizer(receiver);

        piano.setPianoViewListener(new PianoLargeView.PianoViewListener() {
            @Override
            public void onNoteOnListener(int noteOn) {
//                chord_input_note.add(noteOn);
//                print("add note " + noteOn);
//                Log.e("PianoView_noteOn:: ", String.valueOf(noteOn));
//                print(String.valueOf(chord_input_note));
//                DetectChordFromMidiNote();
                activeNoteToScreen(noteOn);
                staffView.setNoteToStaff(noteOn);
            }

            @Override
            public void onNoteOffListener(int noteOff) {
//                if (chord_input_note.size() > 4)
//                    chord_input_note.clear();
//                print("remove note " + noteOff);
//                Log.e("PianoView_noteOff:: ", String.valueOf(noteOff));
//                print(String.valueOf(chord_input_note));
                removeNoteOnScreen(noteOff);
                staffView.releaseNote();
            }
        });

        //recording tick
        ticks = -1;

        //Init USB Driver

        InitUsbDriver();

        //active note
        activeNoteToScreen(piano.getNoteIsPlaying());

        ListView midiInputEventListView = findViewById(R.id.midiInputEventListView);
        midiInputEventAdapter = new ArrayAdapter<>(this, R.layout.midi_event, R.id.midiEventDescriptionTextView);
        midiInputEventListView.setAdapter(midiInputEventAdapter);


        midiOutputEventAdapter = new ArrayAdapter<>(this, R.layout.midi_event, R.id.midiEventDescriptionTextView);


        deviceSpinner = findViewById(R.id.deviceNameSpinner);

        connectedDevicesAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, android.R.id.text1, new ArrayList<>());
        deviceSpinner.setAdapter(connectedDevicesAdapter);

        midiOutputEventAdapter.clear();
        //final Animation animCycle = AnimationUtils.loadAnimation(this, R.anim.cycle);
        ImageButton selectedSound = findViewById(R.id.btn_selectSound);
        final Intent InstrumentIntent = new Intent(this, InstrumentsActivity.class);
        selectedSound.setOnClickListener(new View.OnClickListener() {
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

        //Midi sheet active button
        ImageButton btn_midiSheet = findViewById(R.id.btn_option);
        btn_midiSheet.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when a view has been clicked.
             * Active Midi sheet music
             * The first screen is opening the Choose Song Activity
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MainActivity.this, v);
                popup.setOnMenuItemClickListener(MainActivity.this);
                popup.inflate(R.menu.popup);
                popup.show();

            }
        });

        btn_recording = findViewById(R.id.btn_recording);
        btn_recording.setOnClickListener(new View.OnClickListener() {

            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                // btn_recording.startAnimation(animCycle);
                if (!isRecording) {
                    //isRecording = true;

                    showRecordingDialog();

                } else {
                    isRecording = false;
                    btn_recording.setText(R.string.str_recording);
                    //Create file name
                    File file_output = new File(path, "/" + songName + ".mid");
                    newMidiFile.exportMidiFile(file_output);

                    sendMessageToUser("File " + songName + " was save in " + path);
                }

            }
        });


        Intent LargePianoViewIntent = new Intent(this, PianoLargeViewActivity.class);
        ImageButton btn_touch_mode = findViewById(R.id.btn_touchMode);
        btn_touch_mode.setOnClickListener(new View.OnClickListener() {

            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                startActivity(LargePianoViewIntent);
            }
        });

        //START_REGION_CHORD_DETECT
        InitChordDetectFunction();
//        Button btnResetChord = findViewById(R.id.btn_reset_chord);
//        btnResetChord.setOnClickListener(v -> {
//            chord_input_note.clear();
//        });
        //END_REGION

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        usbMidiDriver.close();
        if (synth != null) {
            synth.close();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            String instrument_path = data.getStringExtra(InstrumentsActivity.RESULT_INSTRUMENT_PATH);
            String instrument_name = data.getStringExtra(InstrumentsActivity.RESULT_INSTRUMENT_NAME);
            String instrument_image_path = data.getStringExtra(InstrumentsActivity.RESULT_INSTRUMENT_IMAGE);
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
                    piano.setReceiverForSynthesizer(receiver);

                    //set instruments images
                    ImageView img = findViewById(R.id.instruments_image);
                    @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = getResources().getDrawable(getResources()
                            .getIdentifier(instrument_image_path, "drawable", getPackageName()));
                    img.setImageDrawable(drawable);

                    //set instruments name
                    TextView ins_name = findViewById(R.id.ins_name);
                    ins_name.setText(instrument_name);

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
                    piano.setReceiverForSynthesizer(receiver);

                    //set instruments images
                    ImageView img = findViewById(R.id.instruments_image);
                    @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = getResources().getDrawable(getResources()
                            .getIdentifier("grand_piano", "drawable", getPackageName()));
                    img.setImageDrawable(drawable);

                    //set instruments name
                    TextView ins_name = findViewById(R.id.ins_name);
                    ins_name.setText(R.string.GrandPiano);
                }
            } catch (IOException | MidiUnavailableException e) {
                e.printStackTrace();
            }

        }
    }

    /*
      Recording Midi FIle
     */


    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void showRecordingDialog() {
        // create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.midi_recording_dialog2, null);
        builder.setView(customLayout);
        // add a button

        Button button_ok = customLayout.findViewById(R.id.button_ok);
        Button button_cancel = customLayout.findViewById(R.id.button_cancel);
        EditText tempo_spinner = customLayout.findViewById(R.id.tempo_spinner);
        EditText timeSignature_spinner = customLayout.findViewById(R.id.time_signature);
        EditText songName_ = customLayout.findViewById(R.id.song_name);

        // create and show the recording dialog
        AlertDialog dialog = builder.create();
        dialog.show();
        button_ok.setOnClickListener(v -> {
            songName = songName_.getText().toString();

            Log.i("Midi File -> SongName ", songName);
            Log.i("Midi File TimeSignature", timeSignature_val);
            Log.i("Midi File -> Tempo :: ", String.valueOf(tempo_value));

            if ((tempo_value != -1) && !timeSignature_val.equals("") && !songName.equals("")) {
                sendMessageToUser("Song Property was settings successful");
                String[] time_signature_ = timeSignature_val.split("/");
                newMidiFile = new MidiFileCreator(tempo_value, Integer.parseInt(time_signature_[0]), Integer.parseInt(time_signature_[1]));
                isRecording = true;
//
//                for (int i = 0; i < 5; i++) {
//                    int channel = 0, pitch = 60 + i, velocity = 100;
//                    NoteOn on = new NoteOn(i * 480, channel, pitch, velocity);
//                    NoteOff off = new NoteOff(i * 480 + 120, channel, pitch, 0);
//
//                    newMidiFile.insertEvent(on);
//                    newMidiFile.insertEvent(off);
//
//
//                    // There is also a utility function for notes that it should be use
//                    // instead of the above.
//                    newMidiFile.insertNote(channel, pitch + 2, velocity, i * 480, 120);
//                }


                dialog.cancel();
                recordingDialogStatus = true;
                btn_recording.setText(R.string.stop_recording);
            }
        });

        button_cancel.setOnClickListener(v -> {
            dialog.cancel();
            recordingDialogStatus = false;
        });

        tempo_spinner.setOnTouchListener((v, event) -> {

            if (event.getAction() == MotionEvent.ACTION_UP) {
                showTempoPicker(tempo_spinner);
            }
            return true;
        });

        timeSignature_spinner.setOnTouchListener((v, event) -> {

            if (event.getAction() == MotionEvent.ACTION_UP) {
                showTimeSignature(timeSignature_spinner);
            }
            return true;
        });
    }

    public void showTempoPicker(EditText tempo_editText) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View customLayout = getLayoutInflater().inflate(R.layout.tempo_selector, null);
        builder.setTitle("Tempo");
        builder.setView(customLayout);

        NumberPicker tempo_picker;

        tempo_picker = customLayout.findViewById(R.id.tempo_value);
        tempo_picker.setMinValue(50);
        tempo_picker.setMaxValue(500);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Tempo Number picker
        tempo_picker.setOnValueChangedListener((numberPicker, i, i1) -> {
            int tempo_value_ = tempo_picker.getValue();
            tempo_value = tempo_value_;
            tempo_editText.setText(String.valueOf(tempo_value_));
            Log.i("Tempo value", String.valueOf(tempo_value_));

        });

        tempo_picker.setOnClickListener(v -> {
            if (tempo_value == -1) {
                tempo_value = 120;
                tempo_editText.setText(String.valueOf(tempo_value));
            } else {
                dialog.cancel();
            }
        });
    }

    public void showTimeSignature(EditText timeSignature_editText) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View customLayout = getLayoutInflater().inflate(R.layout.tempo_selector, null);
        builder.setTitle("Tempo");
        builder.setView(customLayout);

        NumberPicker timeSignature;

        timeSignature = customLayout.findViewById(R.id.tempo_value);
        timeSignature.setMinValue(0);
        timeSignature.setMaxValue(8);

        //This is very dirty way for the string arr
        //recommend XML file for the resources
        //:))

        //TIme signature array
        String[] timeSignature_;
        timeSignature_ = new String[]{"2/4", "3/4", "4/4", "5/4", "6/4", "6/8", "7/8", "9/8", "12/8"};

        AlertDialog dialog = builder.create();
        dialog.show();


        // TimeSignature number picker
        timeSignature.setDisplayedValues(timeSignature_);
        timeSignature.setOnValueChangedListener((numberPicker, i, i1) -> {
            int timeSignature_value = timeSignature.getValue();
            Log.i("time_signal", String.valueOf(timeSignature_[timeSignature_value]));
            timeSignature_editText.setText((timeSignature_[timeSignature_value]));
            timeSignature_val = String.valueOf(timeSignature_[timeSignature_value]);
        });

        timeSignature.setOnClickListener(v -> {
            if (timeSignature_val.equals("")) {
                sendMessageToUser("Please select time signature for song recording");
            } else {
                dialog.cancel();
            }

        });
    }

    public void sendMessageToUser(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    protected boolean shouldAskPermissions() {
        return true;
    }

    @TargetApi(23)
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }

    public void activeNoteToScreen(int note) {

//        if (noteName == null || noteName.isEmpty()) {
//            noteName = "";
//        }
        String stringNoteName = (keyMap.GetStringNoteName(note));
        if(!stringNoteName.equals(""))
            ListCurrentNote.add(stringNoteName);
//        if (noteName != null){
//            noteName += stringNoteName;
//            ListCurrentNote.add(noteName)
//
//        }



        try {
            noteDetectEventHandler.sendMessage(Message.obtain(noteDetectEventHandler, 0, ListCurrentNote));
            //NoteLabel.setText(noteName);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    public void removeNoteOnScreen(int note) {
        Log.e("LIST NOTE SIZE : ",String.valueOf(ListCurrentNote.size()));
        countTimeDisplay += 1;
        String noteRemove = keyMap.GetStringNoteName(note);
        if (noteRemove == null || noteRemove.equals("")){
           ListCurrentNote.clear();
            noteDetectEventHandler.sendMessage(Message.obtain(noteDetectEventHandler, 0, ListCurrentNote));
            Log.e("NOTE ", "RETURN NULL");
            return;
        }


//        for (String data:ListCurrentNote
//             ) {
//            if(data.equals(noteRemove)){
//                ListCurrentNote.remove(ListCurrentNote.indexOf(data));
//                Log.e("REMOVE_: ", data);
//            }
//        }
        try {
            if(ListCurrentNote.contains(noteRemove)){
                ListCurrentNote.remove(noteRemove);
                Log.e("REMOVE_NOTE: ", noteRemove);
            }

        }
        catch (Exception e){
            e.printStackTrace();
            ListCurrentNote.clear();
        }

        noteDetectEventHandler.sendMessage(Message.obtain(noteDetectEventHandler, 0, ListCurrentNote));

//        //noteName = noteName.replace(noteRemove, "");
//        if (noteName != null) {
//            try {
//                NoteLabel.setText(noteName);
//            } catch (Exception exception) {
//                exception.printStackTrace();
//            }
//
//        }

//        Log.e("COUNT_TIME: ", String.valueOf(countTimeDisplay));
//        Log.e("NOTENAME: ", String.valueOf(noteName));
//        //reset value of string note name
//        if (countTimeDisplay > 30) {
//            countTimeDisplay = 0;
//            noteName = "";
//            NoteLabel.setText("");
//        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.menu_start_midisheet) {
            final Intent MidiSheetIntent = new Intent(this, ChooseSongActivity.class);
            startActivity(MidiSheetIntent);
            return true;
        }

        if (menuItem.getItemId() == R.id.menu_start_chord_activity) {
            final Intent MidiChordIntent = new Intent(this, MidiChordActivity.class);
            startActivity(MidiChordIntent);
            return true;
        }

//        if (menuItem.getItemId() == R.id.menu_start_pianoLargeView) {
//            final Intent LargePianoViewIntent = new Intent(this, PianoLargeViewActivity.class);
//            startActivity(LargePianoViewIntent);
//            return true;
//        }

        if (menuItem.getItemId() == R.id.menu_start_note_detection) {
            final Intent MicroNoteDetectIntent = new Intent(this, com.example.chordec.chordec.MainActivity.class);
            startActivity(MicroNoteDetectIntent);
            return true;
        }
        return false;
    }

    public void InitChordDetectFunction() {
        //CHORD_ANALYZE_REGION
        System.out.print("__Start chord analyze__");
        chord_input_note = new ArrayList<>();
//        for (int index = 0; index < 7; index++) {
//            String type = "";
//            if (index == 0)
//                type = "major";
//            if (index == 1)
//                type = "minor";
//            if (index == 2)
//                type = "sus4";
//            if (index == 3)
//                type = "dim";
//            if (index == 4)
//                type = "major7";
//            if (index == 5)
//                type = "minor7";
//            if (index == 6)
//                type = "7";
//
//            new ChordType();
//            ChordType chord;
//            chord = ChordType.GenerateChordType(type);
//            Chord_List.add(chord);
//        }
        ChordType chordType = new ChordType();
        Chord_List = chordType.GenerateBasicChord();
        System.out.println(Chord_List);
        System.out.println("\n\n");

    }

    public void DetectChordFromMidiNote() {
        print_("INPUT_SIZE_DETECT ", String.valueOf(chord_input_note.size()));
        ArrayList<Integer> temp_list = new ArrayList<>();
        Collections.sort(chord_input_note);

        int root_note = chord_input_note.get(0);
        for (int index = 0; index < chord_input_note.size(); index++) {
            temp_list.add(chord_input_note.get(index) - root_note);
        }
        print("chord_input_ " + chord_input_note);
        print_("chord temp: ", String.valueOf(temp_list));
        print_("chord temp SIZE : ", String.valueOf(temp_list.size()));
        print_("chord temp  : ", String.valueOf(temp_list));

//        Log.i("Chord Note: ", String.valueOf(chord_input_note));
//        if (temp_list.size() > 5) {
//            temp_list.clear();
//            chord_input_note.clear();
//        }
        int type = -1;
        if (temp_list.size() > 2)
            type = ChordType.FindChordType(temp_list);

//        System.out.println("Chord Type: " + type);

        String chord_extension = "";
        for (ChordType chord : Chord_List) {
            if (chord.type == type) {
                if (chord.chord_note.equals(temp_list)) {
                    chord_extension = chord.name;
                    Log.i("Chord: ", chord.name + " Detect");
                    break;
                }
            }
        }

//        ArrayList<ChordType> temp = new ArrayList<>();
//        for (ChordType chord : Chord_List) {
//            if (chord.type == type) {
//                if (Collections.indexOfSubList(chord.chord_note, temp_list) != -1) {
//                    temp.add(chord);
//                }
//            }
//        }
//        int index = 0;
//        for(ChordType chord: temp){
//            index = 0;
//            int min = temp.get(0).chord_note.size();
//            if(chord.chord_note.size()< min){
//                min = chord.chord_note.size();
//                index = temp.indexOf(chord);
//            }
//        }
//        try{
//            if (temp.size()>0)
//                chord_extension = temp.get(index).name;
//        }
//        catch (Exception e){e.printStackTrace();}


        try {
            String chord_key = keyMap.GetStringNoteName(chord_input_note.get(0));
            print_("CHORD DETECT ", chord_key + " " + chord_extension);
            chordDetectEventHandler.sendMessage(Message.obtain(chordDetectEventHandler, 0, chord_key + " " + chord_extension));
        } catch (Exception e) {
            e.printStackTrace();
        }
//        final Runtime runtime = Runtime.getRuntime();
//        final long usedMemInMB=(runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
//        final long maxHeapSizeInMB=runtime.maxMemory() / 1048576L;
//        final long availHeapSizeInMB = maxHeapSizeInMB - usedMemInMB;
//
//        print_(String.valueOf(usedMemInMB)," used memory");
//        print_(String.valueOf(availHeapSizeInMB)," available memory");
//        print_(String.valueOf(maxHeapSizeInMB),"heap memory");
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
    protected void onRestart() {
        super.onRestart();
        InitUsbDriver();

    }

    private void InitUsbDriver() {
        usbMidiDriver = new UsbMidiDriver(this) {
            @Override
            public void onDeviceAttached(@NonNull UsbDevice usbDevice) {
                // deprecated method.
                // do nothing
            }

            @Override
            public void onMidiInputDeviceAttached(@NonNull MidiInputDevice midiInputDevice) {
            }

            @Override
            public void onMidiOutputDeviceAttached(@NonNull final MidiOutputDevice midiOutputDevice) {
                runOnUiThread(() -> {
                    if (connectedDevicesAdapter != null) {
                        connectedDevicesAdapter.remove(midiOutputDevice.getUsbDevice());
                        connectedDevicesAdapter.add(midiOutputDevice.getUsbDevice());
                        connectedDevicesAdapter.notifyDataSetChanged();
                    } else {
                        connectedDevicesAdapter.add(midiOutputDevice.getUsbDevice());
                    }
                    Toast.makeText(MainActivity.this, "USB MIDI Device " + midiOutputDevice.getUsbDevice().getDeviceName() + " has been attached.", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onDeviceDetached(@NonNull UsbDevice usbDevice) {
                // deprecated method.
                // do nothing
            }

            @Override
            public void onMidiInputDeviceDetached(@NonNull MidiInputDevice midiInputDevice) {

            }

            @Override
            public void onMidiOutputDeviceDetached(@NonNull final MidiOutputDevice midiOutputDevice) {
                runOnUiThread(() -> {
                    if (connectedDevicesAdapter != null) {
                        connectedDevicesAdapter.remove(midiOutputDevice.getUsbDevice());
                        connectedDevicesAdapter.notifyDataSetChanged();
                    }
                    Toast.makeText(MainActivity.this, "USB MIDI Device " + midiOutputDevice.getUsbDevice().getDeviceName() + " has been detached.", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onMidiNoteOff(@NonNull final MidiInputDevice sender, int cable, int channel, int note, int velocity) {
                try {
                    print_("INPUT_SIZE_OFF ", String.valueOf(chord_input_note.size()));
                    if (chord_input_note.size() > 0) {
                        if (chord_input_note.contains(note)) {
                            print_("INDEX : ", "CONTAINS");
                            int index = chord_input_note.indexOf(note);
                            print("INDEX OF NOTE OFF: " + index);
                            chord_input_note.remove(index);
                        }

                    } else {
                        chord_input_note.clear();
                        chord_input_note = new ArrayList<>();
                    }

                    Log.e("Chord Detect remove ", String.valueOf(note));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    ShortMessage msg = new ShortMessage();

                    if (isPedalHolding) {
                        // msg.setMessage(ShortMessage.NOTE_ON, 0, note, velocity);
                    } else {
                        msg.setMessage(ShortMessage.NOTE_OFF, 0, note, velocity);
                    }

                    //msg.setMessage(ShortMessage.NOTE_OFF, 0, note);
                    receiver.send(msg, -1);


                    try {
                        // make key on in PianoView
                        piano.setKey(note, false);
                        runOnUiThread(() -> {
                            removeNoteOnScreen(note);
                        });

                        staffView.releaseNote();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                        piano.setKey(note, false);
                        runOnUiThread(() -> {
                            NoteLabel.setText("");
                        });

                        staffView.releaseNote();
                    }


                    //recording
                    if (isRecording) {
                        ticks += 120;
                        NoteOff noteOff = new NoteOff(ticks, channel, note, velocity);
                        newMidiFile.insertEvent(noteOff);
                    }

                } catch (InvalidMidiDataException e) {
                    Log.e("NOTE_OFF_ERR->", "CAN NOT SEND MIDI OFF");
                    ListCurrentNote.clear();
                    runOnUiThread(()->{
                        NoteLabel.setText("");
                    });
                    e.printStackTrace();
                }

                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "Note off :  note: " + note + ", velocity: " + velocity));

            }

            @Override
            public void onMidiNoteOn(@NonNull final MidiInputDevice sender, int cable, int channel, int note, int velocity) {
                print_("INPUT_SIZE_ON ", String.valueOf(chord_input_note.size()));
                try {
                    msg.setMessage(ShortMessage.NOTE_ON, 0, note, velocity);
                    receiver.send(msg, -1);

                    // make key on in PianoView
                    piano.setKey(note, true);

                    //recording
                    if (isRecording) {
                        if (ticks == -1) {
                            ticks = 0;
                        } else {
                            ticks += 240;
                        }
                        NoteOn noteOn = new NoteOn(ticks, channel, note, velocity);
                        newMidiFile.insertEvent(noteOn);
                    }

//                    try {
//                        chord_input_note.add(note);
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    if (chord_input_note.size() >= 3)
//                        //DetectChordFromMidiNote();


                    activeNoteToScreen(note);
                    staffView.setNoteToStaff(note);

                } catch (InvalidMidiDataException e) {
                    e.printStackTrace();
                }

                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "Note on: " + "note: " + note + " velocity: " + velocity));


            }

            @Override
            public void onMidiPolyphonicAftertouch(@NonNull final MidiInputDevice sender, int cable, int channel, int note, int pressure) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "PolyphonicAftertouch : " + ", cable: " + cable + ", channel: " + channel + ", note: " + note + ", pressure: " + pressure));

                if (thruToggleButton != null && thruToggleButton.isChecked() && getMidiOutputDeviceFromSpinner() != null) {
                    getMidiOutputDeviceFromSpinner().sendMidiPolyphonicAftertouch(cable, channel, note, pressure);
                    midiOutputEventHandler.sendMessage(Message.obtain(midiOutputEventHandler, 0, "PolyphonicAftertouch from: " + ", cable: " + cable + ", channel: " + channel + ", note: " + note + ", pressure: " + pressure));
                }
            }

            @Override
            public void onMidiControlChange(@NonNull final MidiInputDevice sender, int cable, int channel, int function, int value) {
                if (function == 64 && value == 127) {
                    isPedalHolding = true;
                } else if (function == 64 && value == 0) {
                    isPedalHolding = false;
                } else if (function == 1) {
                    try {
                        msg.setMessage(ShortMessage.CONTROL_CHANGE, 0, 1, value);
                        receiver.send(msg, -1);
                    } catch (InvalidMidiDataException e) {
                        e.printStackTrace();
                    }
                }
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "ControlChange from: " + ", cable: " + cable + ", channel: " + channel + ", function: " + function + ", value: " + value));

                if (thruToggleButton != null && thruToggleButton.isChecked() && getMidiOutputDeviceFromSpinner() != null) {
                    getMidiOutputDeviceFromSpinner().sendMidiControlChange(cable, channel, function, value);
                    midiOutputEventHandler.sendMessage(Message.obtain(midiOutputEventHandler, 0, "ControlChange from: " + ", cable: " + cable + ", channel: " + channel + ", function: " + function + ", value: " + value));
                }
            }

            @Override
            public void onMidiProgramChange(@NonNull final MidiInputDevice sender, int cable, int channel, int program) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "ProgramChange from: " + ", cable: " + cable + ", channel: " + channel + ", program: " + program));

                if (thruToggleButton != null && thruToggleButton.isChecked() && getMidiOutputDeviceFromSpinner() != null) {
                    getMidiOutputDeviceFromSpinner().sendMidiProgramChange(cable, channel, program);
                    midiOutputEventHandler.sendMessage(Message.obtain(midiOutputEventHandler, 0, "ProgramChange from: " + ", cable: " + cable + ", channel: " + channel + ", program: " + program));
                }


            }

            @Override
            public void onMidiChannelAftertouch(@NonNull final MidiInputDevice sender, int cable, int channel, int pressure) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "ChannelAftertouch from: " + ", cable: " + cable + ", channel: " + channel + ", pressure: " + pressure));

                if (thruToggleButton != null && thruToggleButton.isChecked() && getMidiOutputDeviceFromSpinner() != null) {
                    getMidiOutputDeviceFromSpinner().sendMidiChannelAftertouch(cable, channel, pressure);
                    midiOutputEventHandler.sendMessage(Message.obtain(midiOutputEventHandler, 0, "ChannelAftertouch from: " + ", cable: " + cable + ", channel: " + channel + ", pressure: " + pressure));
                }
            }

            @Override
            public void onMidiPitchWheel(@NonNull final MidiInputDevice sender, int cable, int channel, int amount) {
                try {
                    if (amount > 8192) {
                        msg.setMessage(0xE0, 64, 127);
                    } else if (amount < 8192) {
                        msg.setMessage(0xE0, 64, 0);
                    } else {
                        msg.setMessage(0xE0, 64, 64);
                    }

                    receiver.send(msg, -1);
                } catch (InvalidMidiDataException e) {
                    e.printStackTrace();
                }

                receiver.send(msg, -1);
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "PitchWheel: amount: " + amount));

                if (thruToggleButton != null && thruToggleButton.isChecked() && getMidiOutputDeviceFromSpinner() != null) {
                    getMidiOutputDeviceFromSpinner().sendMidiPitchWheel(cable, channel, amount);
                    midiOutputEventHandler.sendMessage(Message.obtain(midiOutputEventHandler, 0, "PitchWheel : " + "amount: " + amount));
                }
            }

            @Override
            public void onMidiSystemExclusive(@NonNull final MidiInputDevice sender, int cable, final byte[] systemExclusive) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "SystemExclusive from: " + ", cable: " + cable + ", data:" + Arrays.toString(systemExclusive)));

                if (thruToggleButton != null && thruToggleButton.isChecked() && getMidiOutputDeviceFromSpinner() != null) {
                    getMidiOutputDeviceFromSpinner().sendMidiSystemExclusive(cable, systemExclusive);
                    midiOutputEventHandler.sendMessage(Message.obtain(midiOutputEventHandler, 0, "SystemExclusive from: " + ", cable: " + cable + ", data:" + Arrays.toString(systemExclusive)));
                }
            }

            @Override
            public void onMidiSystemCommonMessage(@NonNull final MidiInputDevice sender, int cable, final byte[] bytes) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "SystemCommonMessage from: " + ", cable: " + cable + ", bytes: " + Arrays.toString(bytes)));

                if (thruToggleButton != null && thruToggleButton.isChecked() && getMidiOutputDeviceFromSpinner() != null) {
                    getMidiOutputDeviceFromSpinner().sendMidiSystemCommonMessage(cable, bytes);
                    midiOutputEventHandler.sendMessage(Message.obtain(midiOutputEventHandler, 0, "SystemCommonMessage from: " + ", cable: " + cable + ", bytes: " + Arrays.toString(bytes)));
                }
            }

            @Override
            public void onMidiSingleByte(@NonNull final MidiInputDevice sender, int cable, int byte1) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "SingleByte from: " + ", cable: " + cable + ", data: " + byte1));

                if (thruToggleButton != null && thruToggleButton.isChecked() && getMidiOutputDeviceFromSpinner() != null) {
                    getMidiOutputDeviceFromSpinner().sendMidiSingleByte(cable, byte1);
                    midiOutputEventHandler.sendMessage(Message.obtain(midiOutputEventHandler, 0, "SingleByte from: " + ", cable: " + cable + ", data: " + byte1));
                }
            }

            @Override
            public void onMidiTimeCodeQuarterFrame(@NonNull MidiInputDevice sender, int cable, int timing) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "TimeCodeQuarterFrame from: " + ", cable: " + cable + ", timing: " + timing));
            }

            @Override
            public void onMidiSongSelect(@NonNull MidiInputDevice sender, int cable, int song) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "SongSelect from: " + ", cable: " + cable + ", song: " + song));
            }

            @Override
            public void onMidiSongPositionPointer(@NonNull MidiInputDevice sender, int cable, int position) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "SongPositionPointer from: " + ", cable: " + cable + ", position: " + position));
            }

            @Override
            public void onMidiTuneRequest(@NonNull MidiInputDevice sender, int cable) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "TuneRequest from: " + ", cable: " + cable));
            }

            @Override
            public void onMidiTimingClock(@NonNull MidiInputDevice sender, int cable) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "TimingClock from: " + ", cable: " + cable));
            }

            @Override
            public void onMidiStart(@NonNull MidiInputDevice sender, int cable) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "Start from: " + ", cable: " + cable));
            }

            @Override
            public void onMidiContinue(@NonNull MidiInputDevice sender, int cable) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "Continue from: " + ", cable: " + cable));
            }

            @Override
            public void onMidiStop(@NonNull MidiInputDevice sender, int cable) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "Stop from: " + ", cable: " + cable));
            }

            @Override
            public void onMidiActiveSensing(@NonNull MidiInputDevice sender, int cable) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "ActiveSensing from: " + ", cable: " + cable));
            }

            @Override
            public void onMidiReset(@NonNull MidiInputDevice sender, int cable) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "Reset from: " + ", cable: " + cable));
            }

            @Override
            public void onMidiMiscellaneousFunctionCodes(@NonNull final MidiInputDevice sender, int cable, int byte1, int byte2, int byte3) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "MiscellaneousFunctionCodes from: " + ", cable: " + cable + ", byte1: " + byte1 + ", byte2: " + byte2 + ", byte3: " + byte3));

                if (thruToggleButton != null && thruToggleButton.isChecked() && getMidiOutputDeviceFromSpinner() != null) {
                    getMidiOutputDeviceFromSpinner().sendMidiMiscellaneousFunctionCodes(cable, byte1, byte2, byte3);
                    midiOutputEventHandler.sendMessage(Message.obtain(midiOutputEventHandler, 0, "MiscellaneousFunctionCodes from: " + ", cable: " + cable + ", byte1: " + byte1 + ", byte2: " + byte2 + ", byte3: " + byte3));
                }
            }

            @Override
            public void onMidiCableEvents(@NonNull final MidiInputDevice sender, int cable, int byte1, int byte2, int byte3) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "CableEvents from: " + ", cable: " + cable + ", byte1: " + byte1 + ", byte2: " + byte2 + ", byte3: " + byte3));

                if (thruToggleButton != null && thruToggleButton.isChecked() && getMidiOutputDeviceFromSpinner() != null) {
                    getMidiOutputDeviceFromSpinner().sendMidiCableEvents(cable, byte1, byte2, byte3);
                    midiOutputEventHandler.sendMessage(Message.obtain(midiOutputEventHandler, 0, "CableEvents from: " + ", cable: " + cable + ", byte1: " + byte1 + ", byte2: " + byte2 + ", byte3: " + byte3));
                }
            }
        };

        usbMidiDriver.open();
    }

}
