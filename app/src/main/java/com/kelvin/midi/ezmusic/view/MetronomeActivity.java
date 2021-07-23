package com.kelvin.midi.ezmusic.view;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kelvin.midi.ezmusic.R;
import com.kelvin.midi.metronome.object.MetronomeSingleton;

public class MetronomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metronome);

        MetronomeSingleton.getInstance();


    }

    public void startMetronome(View view) {

        if (MetronomeSingleton.getInstance().getMetronome().getIsRunning()) {
            //Metronome is running
            MetronomeSingleton.getInstance().getMetronome().stop();
            Button mButton = (Button) findViewById(R.id.activate_metronome);
            mButton.setText(R.string.start_metronome);
        } else {
            MetronomeSingleton.getInstance().getMetronome().setBpmTextView((TextView) findViewById(R.id.bpmTextView));
            MetronomeSingleton.getInstance().getMetronome().start(this);
            Button mButton = (Button) findViewById(R.id.activate_metronome);
            mButton.setText(R.string.stop_metronome);
        }
    }
}