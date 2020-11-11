package com.kelvin.midi.ezmusic.object;

import android.util.Log;

import com.kelvin.midi.midilib.MidiFile;
import com.kelvin.midi.midilib.MidiTrack;
import com.kelvin.midi.midilib.event.MidiEvent;
import com.kelvin.midi.midilib.event.meta.Tempo;
import com.kelvin.midi.midilib.event.meta.TimeSignature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

/*
* This is a midi class to provide MidiFileCreator, It's base on midi lib
* */

public class MidiFileCreator {
    private boolean isRecording ;
    private MidiTrack tempoTrack;
    private MidiTrack noteTrack;
    private Tempo tempo;
    private ArrayList<MidiTrack> midiTracks ;
    private MidiFile midiFile;
    private TimeSignature timeSignature;


    public MidiFileCreator(int tempo_, int numerator, int denominator) {

        midiTracks = new ArrayList<>();
        tempoTrack = new MidiTrack();
        noteTrack = new MidiTrack();
        timeSignature = new TimeSignature();
        timeSignature.setTimeSignature(numerator,denominator,TimeSignature.DEFAULT_METER,TimeSignature.DEFAULT_DIVISION);
        this.tempo = new Tempo();
        tempo.setBpm(tempo_);

        tempoTrack.insertEvent(timeSignature);
        tempoTrack.insertEvent(tempo);

    }

    public MidiFileCreator() {
        midiTracks = new ArrayList<>();
        tempoTrack = new MidiTrack();
        noteTrack = new MidiTrack();
        timeSignature = new TimeSignature();
        this.tempo = new Tempo();
    }



    public void exportMidiFile(File fileName) {

        midiTracks.add(tempoTrack);
        midiTracks.add(noteTrack);

        try {
            MidiFile midi = new MidiFile(MidiFile.DEFAULT_RESOLUTION, midiTracks);
            midi.writeToFile(fileName);
        }
        catch(IOException e)
        {
            Log.e("EXPORT_FILE_ERROR:: ", Objects.requireNonNull(e.getMessage()));
        }
    }

    public void insertNote(int channel, int note, int velocity, int tick, int duration){
        noteTrack.insertNote(channel, note , velocity, tick, duration);
    }

    public void insertEvent(MidiEvent midiEvent){
        noteTrack.insertEvent(midiEvent);
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }

    public Tempo getTempo() {
        return tempo;
    }

    public void setTempo(Tempo tempo) {
        this.tempo = tempo;
    }
    public void setTempo(int tempo){
        this.tempo.setBpm((float)tempo);
    }


    public void setMidiFile(MidiFile midiFile) {
        this.midiFile = midiFile;
    }

    public TimeSignature getTimeSignature() {
        return timeSignature;
    }

    public void setTimeSignature(TimeSignature timeSignature) {
        this.timeSignature = timeSignature;
    }
    public void setTimeSignature(int numerator,int denominator )
    {
        timeSignature.setTimeSignature(numerator,denominator,TimeSignature.DEFAULT_METER,TimeSignature.DEFAULT_DIVISION);
    }

}
