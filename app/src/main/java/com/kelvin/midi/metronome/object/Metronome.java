package com.kelvin.midi.metronome.object;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.TextView;

import com.kelvin.midi.ezmusic.R;

import java.util.Timer;
import java.util.TimerTask;

public class Metronome {

    private final int DEFAULT_BPM = 120;
    private final int MAX_BPM = 200;
    private int _timeSignature1, _timeSignature2;
    private int _bpm, _accentNote, _currentNote;
    private boolean _isRunning;
    private long _lastTickTime, _tickPeriodMs;
    private Timer _timer;
    private Context _currentContext;
    private MediaPlayer _mediaPlayer, _accentMediaPlayer;
    private TextView _bpmTextView = null;


    public Metronome() {

        setBpm(DEFAULT_BPM);
        _isRunning = false;
        _currentNote = 1;
        _timeSignature1 = 4;
        _timeSignature2 = 4;
        _accentNote = 1;
    }

    public void start(Context context) {

        _currentContext = context;

        if (!_isRunning) {

            _isRunning = true;
            _currentNote = 1;
            _mediaPlayer = MediaPlayer.create(context, R.raw.low_seiko_sq50);
            _accentMediaPlayer = MediaPlayer.create(context, R.raw.high_seiko_sq50);
            _tickPeriodMs = (long) ((60.0 / (float) getBpm()) * 1000.0) * 4 / _timeSignature2;
            _timer = new Timer();
            Log.v("Metronome", "Starting metronome at " + getBpm() + " BPM ( " + _tickPeriodMs + " miliseconds per beat)");

            _timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    tick();
                }
            }, 0, _tickPeriodMs);
        }
    }

    public void stop() {

        _mediaPlayer.release();
        _timer.cancel();
        _isRunning = false;
    }

    private void tick() {

        long currentPeriodMs;

        currentPeriodMs = System.currentTimeMillis() - _lastTickTime;
        _lastTickTime = System.currentTimeMillis();

        if (_currentNote == _accentNote) {
            //Plays accent sound
            try {
                _accentMediaPlayer.start();
                 Log.v("Metronome", "Time since last tick is " + currentPeriodMs + "ms (" + getBpm() + " BPM)");
            } catch (Exception e) {
                Log.e("Metronome", e.toString());
            }
        } else {
            //Plays regular sound
            try {
                _mediaPlayer.start();
                Log.v("Metronome", "Time since last tick is " + currentPeriodMs + "ms (" + getBpm() + " BPM)");
            } catch (Exception e) {
                Log.e("Metronome", e.toString());
            }
        }

        Log.v("Metronome", "Played note " + _currentNote + " of " + _timeSignature1);
        _currentNote++;
        if (_currentNote > _timeSignature1) {
            _currentNote = 1;
        }

    }

    private void refreshBpmTextView() {

        if (_bpmTextView != null) {
            _bpmTextView.setText(Integer.toString(getBpm()) + " BPM");
        }
    }

    private float calculatePrecision(long currentPeriodMs) {

        return (float) (((float) (currentPeriodMs - _tickPeriodMs)) / ((float) _tickPeriodMs / 100.0));
    }


    public void setBpmTextView(TextView textView) {
        _bpmTextView = textView;
        refreshBpmTextView();
    }

    public void setBpm(int bpm) {

        if (bpm <= MAX_BPM) {
            _bpm = bpm;
            if (_isRunning) {
                stop();
                start(_currentContext);
            }
            refreshBpmTextView();
        } else {
            Log.v("Metronome", "BPM = " + bpm + " not allowed.");
        }
    }

    public void increaseBpm(int value) {

        if (_bpm + value <= MAX_BPM) {
            _bpm = _bpm + value;
            Log.v("Metronome", "BPM set to " + getBpm());
            if (_isRunning) {
                stop();
                start(_currentContext);
            }
        }
        refreshBpmTextView();
    }

    public void decreaseBpm(int value) {

        if (_bpm - value >= 1) {
            _bpm = _bpm - value;
            Log.v("Metronome", "BPM set to " + getBpm());
            if (_isRunning) {
                stop();
                start(_currentContext);
            }
            refreshBpmTextView();
        }
    }

    public void setTimeSignature1(int value) {
        _timeSignature1 = value;
    }

    public void setTimeSignature2(int value) {
        _timeSignature2 = value;
    }

    public void setAccentNote(int value) {
        if (value <= _timeSignature1)
            _accentNote = value;
    }

    public int getBpm() {
        return _bpm;
    }

    public boolean getIsRunning() {
        return _isRunning;
    }

    public int getTimeSignature1() {
        return _timeSignature1;
    }

    public int getAccentNote() {
        return _accentNote;
    }
}
