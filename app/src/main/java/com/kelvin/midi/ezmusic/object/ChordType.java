package com.kelvin.midi.ezmusic.object;

import java.util.ArrayList;
import java.util.List;

public class ChordType {
    public String name;
    public List<Integer> chord_note;
    public int type;

    public ChordType(String name, List<Integer> chord_note) {
        this.name = name;
        this.chord_note = chord_note;
    }

    public ChordType() {
        this.chord_note = new ArrayList<>();
    }

    public void AddChordNote(int note){
        this.chord_note.add(note);
    }
    public void AddChordNote(ArrayList<Integer> list_note){
        this.chord_note = list_note;
    }

    public void setChordName(String name){
        this.name = name;
    }

    public void setType(int type){
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

    public static ChordType GenerateChordType(String type){
        ChordType chord = new ChordType();

        switch (type){
            case "major":
            {

                chord.setChordName("major");
                chord.AddChordNote(0);
                chord.AddChordNote(4);
                chord.AddChordNote(7);
                chord.setType(4);

                break;
            }
            case "minor":
            {

                chord.setChordName("minor");
                chord.AddChordNote(0);
                chord.AddChordNote(3);
                chord.AddChordNote(7);
                chord.setType(3);

                break;
            }
            case "dim":
            {

                chord.setChordName("dim");
                chord.AddChordNote(0);
                chord.AddChordNote(3);
                chord.AddChordNote(6);
                chord.setType(3);

                break;
            }
            case "sus4":
            {

                chord.setChordName("sus4");
                chord.AddChordNote(0);
                chord.AddChordNote(5);
                chord.AddChordNote(7);
                chord.setType(5);

                break;
            }
            case "major7":
            {
                chord.setChordName("major7");
                chord.AddChordNote(0);
                chord.AddChordNote(4);
                chord.AddChordNote(7);
                chord.AddChordNote(11);
                chord.setType(4);

                break;
            }
            case "7":
            {
                chord.setChordName("7");
                chord.AddChordNote(0);
                chord.AddChordNote(4);
                chord.AddChordNote(7);
                chord.AddChordNote(10);
                chord.setType(4);

                break;
            }
            case "minor7":
            {
                chord.setChordName("minor7");
                chord.AddChordNote(0);
                chord.AddChordNote(3);
                chord.AddChordNote(7);
                chord.AddChordNote(10);
                chord.setType(3);
                break;
            }
            default:
        }
        return chord;
    }
    public static int FindChordType(ArrayList<Integer> list_note){

            if(list_note.get(1)==4)
                return 4;//major
            if(list_note.get(1)==3)
                return 3;//minor
            if(list_note.get(1)==5)
                return 5;//sus4
            else
                return -1;

    }

    public static ArrayList<Integer> ConvertMidiNoteToChordType(ArrayList<Integer> input_note){
        int root_note = input_note.get(0);
        ArrayList<Integer> chord_type = new ArrayList<>();
        for (int note:input_note) {
            note = note - root_note;
            chord_type.add(note);
        }
        System.out.println(chord_type);
        return chord_type;
    }

}
