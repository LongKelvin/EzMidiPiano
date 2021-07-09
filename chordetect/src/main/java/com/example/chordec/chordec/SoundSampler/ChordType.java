package com.example.chordec.chordec.SoundSampler;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.Receiver;
import jp.kshoji.javax.sound.midi.ShortMessage;



public class ChordType {
    Receiver receiverTransmits;
    public String name;
    public List<Integer> chord_note;
    public int type;
    private ArrayList<String> basic_chord;

    public static List<String> getBasic_chord_list() {
        return basic_chord_list;
    }

    private static final List<String> basic_chord_list =
            Arrays.asList("major", "minor", "dim", "sus4", "major7", "7",
                    "minor7", "aug", "minor7b5", "major7b5", "major7#5");

    public ChordType(String name, List<Integer> chord_note) {
        this.name = name;
        this.chord_note = chord_note;
        this.basic_chord = new ArrayList<>();
    }

    public ChordType() {
        this.chord_note = new ArrayList<>();
    }

    public void AddChordNote(int note) {
        this.chord_note.add(note);
    }

    public void AddChordNote(ArrayList<Integer> list_note) {
        this.chord_note = list_note;
    }

    public void setChordName(String name) {
        this.name = name;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "\nChordType{" +
                "name='" + name + '\'' +
                ", chord_note=" + chord_note +
                ", type=" + type +
                '}';
    }

    public static ChordType GenerateChordType(String type) {
        ChordType chord = new ChordType();

        switch (type) {
            case "major": {
                chord.setChordName("major");
                chord.AddChordNote(0);
                chord.AddChordNote(4);
                chord.AddChordNote(7);
                chord.setType(4);
                break;
            }
            case "minor": {
                chord.setChordName("minor");
                chord.AddChordNote(0);
                chord.AddChordNote(3);
                chord.AddChordNote(7);
                chord.setType(3);
                break;
            }
            case "dim": {
                chord.setChordName("dim");
                chord.AddChordNote(0);
                chord.AddChordNote(3);
                chord.AddChordNote(6);
                chord.setType(3);
                break;
            }
            case "sus4": {
                chord.setChordName("sus4");
                chord.AddChordNote(0);
                chord.AddChordNote(5);
                chord.AddChordNote(7);
                chord.setType(5);
                break;
            }
            case "major7": {
                chord.setChordName("major7");
                chord.AddChordNote(0);
                chord.AddChordNote(4);
                chord.AddChordNote(7);
                chord.AddChordNote(11);
                chord.setType(4);
                break;
            }
            case "7": {
                chord.setChordName("7");
                chord.AddChordNote(0);
                chord.AddChordNote(4);
                chord.AddChordNote(7);
                chord.AddChordNote(10);
                chord.setType(4);

                break;
            }
            case "minor7": {
                chord.setChordName("minor7");
                chord.AddChordNote(0);
                chord.AddChordNote(3);
                chord.AddChordNote(7);
                chord.AddChordNote(10);
                chord.setType(3);
                break;
            }

            case "aug": {
                chord.setChordName("aug");
                chord.AddChordNote(0);
                chord.AddChordNote(4);
                chord.AddChordNote(8);
                chord.setType(4);
                break;
            }
            case "minor7b5": {
                chord.setChordName("minor7b5");
                chord.AddChordNote(0);
                chord.AddChordNote(3);
                chord.AddChordNote(6);
                chord.AddChordNote(10);
                chord.setType(3);
                break;
            }
            case "major7b5": {
                chord.setChordName("major7b5");
                chord.AddChordNote(0);
                chord.AddChordNote(4);
                chord.AddChordNote(6);
                chord.AddChordNote(11);
                chord.setType(3);
                break;
            }
            case "major7#5": {
                chord.setChordName("major7#5");
                chord.AddChordNote(0);
                chord.AddChordNote(4);
                chord.AddChordNote(8);
                chord.AddChordNote(11);
                chord.setType(3);
                break;
            }
            default:
        }
        return chord;
    }

    public static int FindChordType(ArrayList<Integer> list_note) {

        if (list_note.get(1) == 4)
            return 4;//major
        if (list_note.get(1) == 3)
            return 3;//minor
        if (list_note.get(1) == 5)
            return 5;//sus4
        else
            return -1;
    }

    public static ArrayList<Integer> ConvertMidiNoteToChordType(ArrayList<Integer> input_note) {
        int root_note = input_note.get(0);
        ArrayList<Integer> chord_type = new ArrayList<>();
        for (int note : input_note) {
            note = note - root_note;
            chord_type.add(note);
        }
        System.out.println(chord_type);
        return chord_type;
    }

    public ArrayList<ChordType> GenerateBasicChord() {
        if (basic_chord == null) {
            basic_chord = new ArrayList<>();
        }
        ChordType chord = new ChordType();
        ArrayList<ChordType> Chord_Basic_List = new ArrayList<>();
        basic_chord.addAll(basic_chord_list);

        for (String chord_type :
                basic_chord) {
            //print_("ChordType", chord_type);
            chord = GenerateChordType(chord_type);
            Chord_Basic_List.add(chord);
        }
        return Chord_Basic_List;
    }

    /*list note is note in a chord
      type is arr or play the same time
    * */
    public void playChord(int[] listNote) {
        for (int note : listNote
        ) {
            long start_time = 0;
            try {
                start_time = System.currentTimeMillis();
                ShortMessage msg = new ShortMessage();
                msg.setMessage(ShortMessage.NOTE_ON, 0, note, 100);
                receiverTransmits.send(msg, -1);
            } catch (InvalidMidiDataException | NullPointerException e) {
                e.printStackTrace();
            }

            if (System.currentTimeMillis() - start_time > 5000) {
                try {
                    ShortMessage msg = new ShortMessage();
                    msg.setMessage(ShortMessage.NOTE_OFF, 0, note, 0);
                    receiverTransmits.send(msg, -1);

                } catch (InvalidMidiDataException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setReceiverForSynthesizer(Receiver receiver) {
        try {

            this.receiverTransmits = receiver;
        } catch (IllegalStateException e) {
            Log.e("RECEIVER:  ", "RECEIVER NOT FOUND");
            e.printStackTrace();
        }
    }

}
