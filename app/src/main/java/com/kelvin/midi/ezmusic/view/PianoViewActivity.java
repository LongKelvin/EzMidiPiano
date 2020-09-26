package com.kelvin.midi.ezmusic.view;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.kelvin.midi.ezmusic.R;

public class PianoViewActivity extends AppCompatActivity {

    @RequiresApi(api = VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_piano_view);

        PianoView pianoview = (PianoView)findViewById(R.id.piano_view);
        pianoview.onSizeChanged(0,0,0,0);




    }


}