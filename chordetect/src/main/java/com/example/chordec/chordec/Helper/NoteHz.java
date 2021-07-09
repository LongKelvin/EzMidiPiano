package com.example.chordec.chordec.Helper;



import android.util.Log;

import com.example.chordec.chordec.SoundSampler.KeyMap;

import java.util.ArrayList;

public class NoteHz extends Note {
    private float basePitchHz;
    private ArrayList<NoteHz> ListOfPitch ;

    public NoteHz() {
        super();
        ListOfPitch = new ArrayList<>();
        basePitchHz = PianoHzFrequency.PianoHzPitch[0];

    }

    public ArrayList<NoteHz> GenerateNotePitch() {
        ArrayList<NoteHz> ListNoteHz = new ArrayList<>();
        int noteMidiNumber = 12;
        int temp_noteMidi =12; ;
        KeyMap key = new KeyMap();
        key.InitKeyMap();

        float currentPitchIndex = PianoHzFrequency.PianoHzPitch[0];
        for (int index = 0; index < 12; index++) {
            Log.i("Generate NoteHz:", "CurrentIndex = "+ String.valueOf(index));
            Log.i("Generate NoteHz:", "Pitch = "+ String.valueOf(currentPitchIndex));

            currentPitchIndex = PianoHzFrequency.PianoHzPitch[index];

            temp_noteMidi= noteMidiNumber;
            for (int j = 1; j <= 9; j++) {
                NoteHz note = new NoteHz();
                if (j != 1) {
                    currentPitchIndex = currentPitchIndex*2;
                    //This is the next pitch of note
                    //eg: C1 is 16.35 then the next C2 = 16.35*2
                }

                note.basePitchHz = currentPitchIndex;
                note.setNoteName(key.GenerateNoteName(noteMidiNumber));
                note.setNoteNumber(temp_noteMidi);
                ListNoteHz.add(note);
                temp_noteMidi+=12;
                Log.i("Generate NoteHz: ", note.toString());
            }


            noteMidiNumber+=1;
        }
        return ListNoteHz;
    }

    public int CheckNotePitch(float pitch) {
        if(ListOfPitch==null || ListOfPitch.size()==0)
            ListOfPitch = GenerateNotePitch();

//        for (NoteHz notehz: ListOfPitch) {
//            if(notehz.basePitchHz==pitch || (int)(notehz.basePitchHz) == (int)(pitch)){
//                Log.e("CHECK_PITCH: ", String.valueOf(pitch));
//                Log.e("CHECK_PITCH_INDEX: ", String.valueOf(ListOfPitch.indexOf(notehz)));
//                return ListOfPitch.indexOf(notehz);
//            }
//
//        }

        for( int index = 0;index< ListOfPitch.size()-1;index++){
            int pitchToInt = (int)pitch;
            float pitchOfList = ListOfPitch.get(index).basePitchHz;

            if(pitchOfList== pitch || (int)pitchOfList==pitchToInt){
                Log.i("DETECT_PITCH_IDX: ", String.valueOf(index));

                return index;
            }
        }
       return -1;
    }

    @Override
    public String toString() {
        return "\nPianoPitchHz{" +
                "name='" + getNoteName() + '\'' +
                ", pitch=" + basePitchHz +
                ", midiNote=" + getNoteNumber() +
                '}';
    }


}
