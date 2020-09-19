package com.kelvin.midi.ezmusic.view;

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.kelvin.midi.ezmusic.R;
import com.kelvin.midi.ezmusic.adapter.InstrumentsAdapter;
import com.kelvin.midi.ezmusic.object.Instruments;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class InstrumentsActivity extends ListActivity {
    public static  String RESULT_INSTRUMENT_PATH = "instrument_path";
    private String[] instrument_name;
    private String[] instrument_path;
    private TypedArray instrument_image;

    private List<Instruments> instrumentList;

    /**
     * Called when the activity is starting.  This is where most initialization
     * should go: calling {@link #setContentView(int)} to inflate the
     * activity's UI, using {@link #findViewById} to programmatically interact
     * with widgets in the UI, calling
     * {@link #managedQuery(Uri, String[], String, String[], String)} to retrieve
     * cursors for data being displayed, etc.
     *
     * <p>You can call {@link #finish} from within this function, in
     * which case onDestroy() will be immediately called after {@link #onCreate} without any of the
     * rest of the activity lifecycle ({@link #onStart}, {@link #onResume}, {@link #onPause}, etc)
     * executing.
     *
     * <p><em>Derived classes must call through to the super class's
     * implementation of this method.  If they do not, an exception will be
     * thrown.</em></p>
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     * @see #onStart
     * @see #onSaveInstanceState
     * @see #onRestoreInstanceState
     * @see #onPostCreate
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Select Instument Sound: SF2 Sound Font:");
        populateInstrumentList();
        ArrayAdapter<Instruments> adapter = new InstrumentsAdapter(this, instrumentList);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Instruments ins = instrumentList.get(position);
                Intent returnIntent = new Intent();
                returnIntent.putExtra(RESULT_INSTRUMENT_PATH, ins.getInstrumentsSoundPath());
                setResult(RESULT_OK, returnIntent);
                instrument_image.recycle(); //recycle images
                finish();
            }
        });

    }

    //get instrument list
    private void populateInstrumentList() {
        instrumentList = new ArrayList<>();
        instrument_name = getResources().getStringArray(R.array.instrument_name);
        instrument_path = getResources().getStringArray(R.array.instrument_path);
        instrument_image = getResources().obtainTypedArray(R.array.image);
        for(int i = 0; i < instrument_path.length; i++){
            instrumentList.add(new Instruments(instrument_name[i], instrument_path[i], instrument_image.getDrawable(i)));
        }
    }

}
