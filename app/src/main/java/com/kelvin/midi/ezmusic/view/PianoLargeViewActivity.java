package com.kelvin.midi.ezmusic.view;

import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.kelvin.midi.ezmusic.R;

public class PianoLargeViewActivity extends AppCompatActivity {

    PianoLargeView pianoLargeView;
    int piano_size_on_change = 7;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piano_large_view);

        //Init PianoView
        pianoLargeView = new PianoLargeView(this);
        pianoLargeView = findViewById(R.id.piano_large_view);


        Button btn_increase_piano_size = findViewById(R.id.btn_increase_piano_size);
        Button btn_decrease_piano_size = findViewById(R.id.btn_decrease_piano_size);

        btn_increase_piano_size.setOnClickListener(view -> {
            if (piano_size_on_change < 36)
                piano_size_on_change += 1;
            pianoLargeView.setPianoViewWidth(piano_size_on_change);
        });

        btn_decrease_piano_size.setOnClickListener(view -> {
            if (piano_size_on_change == 0)
                piano_size_on_change = 2;
            piano_size_on_change -= 1;
            pianoLargeView.setPianoViewWidth(piano_size_on_change);
        });
    }
}