package com.kelvin.midi.ezmusic.view;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kelvin.midi.ezmusic.R;
import com.kelvin.midi.midilib.MidiFile;
import com.kelvin.midi.midilib.MidiTrack;
import com.kelvin.midi.midilib.event.NoteOff;
import com.kelvin.midi.midilib.event.NoteOn;
import com.kelvin.midi.midilib.event.meta.Tempo;
import com.kelvin.midi.midilib.event.meta.TimeSignature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import jp.kshoji.javax.sound.midi.UsbMidiSystem;

public class MidiFileRecord extends AppCompatActivity {
    UsbMidiSystem usbMidiSystem;
    EditText inputText;
    TextView response;
    Button saveButton,readButton;

    private String filename = "SampleFile.mid";
    private String filepath = "MidiFiles";
    File myExternalFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_midi_file_record);

        if (shouldAskPermissions()) {
            askPermissions();
        }



        // 1. Create some MidiTracks
        MidiTrack tempoTrack = new MidiTrack();
        MidiTrack noteTrack = new MidiTrack();

        // 2. Add events to the tracks
        // 2a. Track 0 is typically the tempo map
        TimeSignature ts = new TimeSignature();
        ts.setTimeSignature(4, 4, TimeSignature.DEFAULT_METER, TimeSignature.DEFAULT_DIVISION);

        Tempo t = new Tempo();
        t.setBpm(228);

        tempoTrack.insertEvent(ts);
        tempoTrack.insertEvent(t);

        // 2b. Track 1 will have some notes in it
        for(int i = 0; i < 5; i++)
        {
            int channel = 0, pitch = 60 + i, velocity = 100;
            NoteOn on = new NoteOn(i * 480, channel, pitch, velocity);
            NoteOff off = new NoteOff(i * 480 + 120, channel, pitch, 0);

            noteTrack.insertEvent(on);
            noteTrack.insertEvent(off);

            // There is also a utility function for notes that you should use
            // instead of the above.
            noteTrack.insertNote(channel, pitch + 2, velocity, i * 480, 120);
        }

        // It's best not to manually insert EndOfTrack events; MidiTrack will
        // call closeTrack() on itself before writing itself to a file

        // 3. Create a MidiFile with the tracks we created
        ArrayList<MidiTrack> tracks = new ArrayList<MidiTrack>();
        tracks.add(tempoTrack);
        tracks.add(noteTrack);

        MidiFile midi = new MidiFile(MidiFile.DEFAULT_RESOLUTION, tracks);

        // 4. Write the MIDI data to a file
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            saveButton.setEnabled(false);
        }
        else {
            myExternalFile = new File(getExternalFilesDir(filepath), filename);
        }


        try
        {

            Log.e("DEBUG_: ","START_WRITING_MIDI_FILE");

            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
            File file_output = new File(path, "/" + filename);
            Log.e("PATH: ",file_output.getPath());
            midi.writeToFile(file_output);

            Log.e("DEBUG_: ","COMPLETED_WRITING_MIDI_FILE");
        }
        catch(IOException e)
        {
            Log.e("FILE_ERROR:: ", Objects.requireNonNull(e.getMessage()));
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (usbMidiSystem != null) {
            usbMidiSystem.terminate();
        }
    }

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState);
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(extStorageState);
    }

    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
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
}