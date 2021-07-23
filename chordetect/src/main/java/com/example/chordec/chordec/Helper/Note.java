package com.example.chordec.chordec.Helper;

import com.example.chordec.chordec.SoundSampler.KeyMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Note {

    public Note(int noteNumber, String noteName) {
        NoteNumber = noteNumber;
        NoteName = noteName;
    }

    public Note() {

    }

    private int NoteNumber;
    private String NoteName;

    public int getNoteNumber() {
        return NoteNumber;
    }

    public void setNoteNumber(int noteNumber) {
        NoteNumber = noteNumber;
    }

    public String getNoteName() {
        return NoteName;
    }

    public void setNoteName(String noteName) {
        NoteName = noteName;
    }

    public ArrayList<Note> createRootNoteForChord() {
        KeyMap k = new KeyMap();
        ArrayList<Note> ListOfRootNote = new ArrayList<>();
        HashMap<Integer, String> keyMap = k.InitKeyMap();

        for (Map.Entry map : keyMap.entrySet()) {
           // System.out.println(map.getKey() + " " + map.getValue());
            int noteVal = (int) map.getKey();
            String noteName = (String) map.getValue();
            if (noteVal >= 60 && noteVal < 72) {
                Note note = new Note(noteVal, noteName);
                ListOfRootNote.add(note);

            }

        }
        for(int i=0;i< ListOfRootNote.size(); i++)
            System.out.println(ListOfRootNote.get(i).NoteNumber + " _" + ListOfRootNote.get(i).NoteName);
        return ListOfRootNote;
    }
}
