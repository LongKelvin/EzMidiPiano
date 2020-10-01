package com.kelvin.midi.ezmusic.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;

import androidx.annotation.NonNull;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.kelvin.midi.ezmusic.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import cn.sherlock.com.sun.media.sound.SF2Soundbank;
import cn.sherlock.com.sun.media.sound.SoftSynthesizer;
import jp.kshoji.driver.midi.device.MidiInputDevice;
import jp.kshoji.driver.midi.device.MidiOutputDevice;
import jp.kshoji.driver.midi.util.UsbMidiDriver;
import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.Receiver;
import jp.kshoji.javax.sound.midi.ShortMessage;


public class MainActivity extends Activity {

    private SoftSynthesizer synth;
    private Receiver receiver;
    private boolean isPedalHolding = false;
    private ShortMessage msg = new ShortMessage();

    private String DEFAULT_INSTRUMENT = "GrandPiano";

    //custom piano view
    public PianoView piano;


    ArrayAdapter<String> midiInputEventAdapter;
    ArrayAdapter<String> midiOutputEventAdapter;
    private ToggleButton thruToggleButton;
    Spinner cableIdSpinner;
    Spinner deviceSpinner;

    ArrayAdapter<UsbDevice> connectedDevicesAdapter;

    private UsbMidiDriver usbMidiDriver;

    // User interface
    final Handler midiInputEventHandler = new Handler(new Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (midiInputEventAdapter != null) {
                midiInputEventAdapter.add((String) msg.obj);
            }
            // message handled successfully
            return true;
        }
    });

    final Handler midiOutputEventHandler = new Handler(new Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (midiOutputEventAdapter != null) {
                midiOutputEventAdapter.add((String) msg.obj);
            }
            // message handled successfully
            return true;
        }
    });


    /**
     * Choose device from spinner
     *
     * @return the MidiOutputDevice from spinner
     */
    @Nullable
    MidiOutputDevice getMidiOutputDeviceFromSpinner() {
        if (deviceSpinner != null && deviceSpinner.getSelectedItemPosition() >= 0 && connectedDevicesAdapter != null && !connectedDevicesAdapter.isEmpty()) {
            UsbDevice device = connectedDevicesAdapter.getItem(deviceSpinner.getSelectedItemPosition());
            if (device != null) {
                Set<MidiOutputDevice> midiOutputDevices = usbMidiDriver.getMidiOutputDevices(device);
                if (midiOutputDevices.size() > 0) {
                    // returns the first one.
                    System.out.println("MIDI_PORT: DEVICE_OUTPUT_PORT_CONNECTED");
                    return (MidiOutputDevice) Objects.requireNonNull(midiOutputDevices.toArray())[0];
                }
            }
        }
        return null;
    }


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //create landscape screen
        //request full screen for login activity
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_midi_driver);


        //Setup Synthesizers SF2_Sound
        try {
            SF2Soundbank sf = new SF2Soundbank(getAssets().open(DEFAULT_INSTRUMENT + ".sf2"));
            synth = new SoftSynthesizer();
            synth.open();
            synth.loadAllInstruments(sf);
            synth.getChannels()[0].programChange(0);
            receiver = synth.getReceiver();
        } catch (IOException | MidiUnavailableException | IllegalStateException e) {
            e.printStackTrace();
        }

        //Init PianoView
        piano = new PianoView(this);
        piano = findViewById(R.id.piano_view);
        piano.setReceiverForSynthesizer(receiver);


        usbMidiDriver = new UsbMidiDriver(this) {
            @Override
            public void onDeviceAttached(@NonNull UsbDevice usbDevice) {
                // deprecated method.
                // do nothing
            }

            @Override
            public void onMidiInputDeviceAttached(@NonNull MidiInputDevice midiInputDevice) {

            }

            @Override
            public void onMidiOutputDeviceAttached(@NonNull final MidiOutputDevice midiOutputDevice) {
                runOnUiThread(() -> {
                    if (connectedDevicesAdapter != null) {
                        connectedDevicesAdapter.remove(midiOutputDevice.getUsbDevice());
                        connectedDevicesAdapter.add(midiOutputDevice.getUsbDevice());
                        connectedDevicesAdapter.notifyDataSetChanged();
                    } else {
                        connectedDevicesAdapter.add(midiOutputDevice.getUsbDevice());
                    }
                    Toast.makeText(MainActivity.this, "USB MIDI Device " + midiOutputDevice.getUsbDevice().getDeviceName() + " has been attached.", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onDeviceDetached(@NonNull UsbDevice usbDevice) {
                // deprecated method.
                // do nothing
            }

            @Override
            public void onMidiInputDeviceDetached(@NonNull MidiInputDevice midiInputDevice) {

            }

            @Override
            public void onMidiOutputDeviceDetached(@NonNull final MidiOutputDevice midiOutputDevice) {
                runOnUiThread(() -> {
                    if (connectedDevicesAdapter != null) {
                        connectedDevicesAdapter.remove(midiOutputDevice.getUsbDevice());
                        connectedDevicesAdapter.notifyDataSetChanged();
                    }
                    Toast.makeText(MainActivity.this, "USB MIDI Device " + midiOutputDevice.getUsbDevice().getDeviceName() + " has been detached.", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onMidiNoteOff(@NonNull final MidiInputDevice sender, int cable, int channel, int note, int velocity) {

                try {
                    ShortMessage msg = new ShortMessage();

                    if (isPedalHolding) {
                        // msg.setMessage(ShortMessage.NOTE_ON, 0, note, velocity);
                    } else {
                        msg.setMessage(ShortMessage.NOTE_OFF, 0, note, velocity);
                    }

                    // msg.setMessage(ShortMessage.NOTE_OFF, 0, note);
                    receiver.send(msg, -1);


                    // make key on in PianoView
                    piano.setKey(note, false);

                } catch (InvalidMidiDataException e) {
                    e.printStackTrace();
                }

                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "NOTE OFF : " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable + ", channel: " + channel + ", note: " + note + ", velocity: " + velocity));

            }

            @Override
            public void onMidiNoteOn(@NonNull final MidiInputDevice sender, int cable, int channel, int note, int velocity) {

                try {

                    msg.setMessage(ShortMessage.NOTE_ON, 0, note, velocity);
                    receiver.send(msg, -1);

                    // make key on in PianoView
                    piano.setKey(note, true);

                } catch (InvalidMidiDataException e) {
                    e.printStackTrace();
                }

                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "NOTE ON from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable + ",  channel: " + channel + ", note: " + note + ", velocity: " + velocity));

            }

            @Override
            public void onMidiPolyphonicAftertouch(@NonNull final MidiInputDevice sender, int cable, int channel, int note, int pressure) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "PolyphonicAftertouch from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable + ", channel: " + channel + ", note: " + note + ", pressure: " + pressure));

                if (thruToggleButton != null && thruToggleButton.isChecked() && getMidiOutputDeviceFromSpinner() != null) {
                    getMidiOutputDeviceFromSpinner().sendMidiPolyphonicAftertouch(cable, channel, note, pressure);
                    midiOutputEventHandler.sendMessage(Message.obtain(midiOutputEventHandler, 0, "PolyphonicAftertouch from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable + ", channel: " + channel + ", note: " + note + ", pressure: " + pressure));
                }
            }

            @Override
            public void onMidiControlChange(@NonNull final MidiInputDevice sender, int cable, int channel, int function, int value) {
                if (function == 64 && value == 127) {
                    isPedalHolding = true;
                } else if (function == 64 && value == 0) {
                    isPedalHolding = false;
                } else if (function == 1) {
                    try {
                        msg.setMessage(ShortMessage.CONTROL_CHANGE, 0, 1, value);
                        receiver.send(msg, -1);
                    } catch (InvalidMidiDataException e) {
                        e.printStackTrace();
                    }
                }
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "ControlChange from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable + ", channel: " + channel + ", function: " + function + ", value: " + value));

                if (thruToggleButton != null && thruToggleButton.isChecked() && getMidiOutputDeviceFromSpinner() != null) {
                    getMidiOutputDeviceFromSpinner().sendMidiControlChange(cable, channel, function, value);
                    midiOutputEventHandler.sendMessage(Message.obtain(midiOutputEventHandler, 0, "ControlChange from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable + ", channel: " + channel + ", function: " + function + ", value: " + value));
                }
            }

            @Override
            public void onMidiProgramChange(@NonNull final MidiInputDevice sender, int cable, int channel, int program) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "ProgramChange from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable + ", channel: " + channel + ", program: " + program));

                if (thruToggleButton != null && thruToggleButton.isChecked() && getMidiOutputDeviceFromSpinner() != null) {
                    getMidiOutputDeviceFromSpinner().sendMidiProgramChange(cable, channel, program);
                    midiOutputEventHandler.sendMessage(Message.obtain(midiOutputEventHandler, 0, "ProgramChange from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable + ", channel: " + channel + ", program: " + program));
                }


            }

            @Override
            public void onMidiChannelAftertouch(@NonNull final MidiInputDevice sender, int cable, int channel, int pressure) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "ChannelAftertouch from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable + ", channel: " + channel + ", pressure: " + pressure));

                if (thruToggleButton != null && thruToggleButton.isChecked() && getMidiOutputDeviceFromSpinner() != null) {
                    getMidiOutputDeviceFromSpinner().sendMidiChannelAftertouch(cable, channel, pressure);
                    midiOutputEventHandler.sendMessage(Message.obtain(midiOutputEventHandler, 0, "ChannelAftertouch from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable + ", channel: " + channel + ", pressure: " + pressure));
                }
            }

            @Override
            public void onMidiPitchWheel(@NonNull final MidiInputDevice sender, int cable, int channel, int amount) {
                try {
                    if (amount > 8192) {
                        msg.setMessage(0xE0, 64, 127);
                    } else if (amount < 8192) {
                        msg.setMessage(0xE0, 64, 0);
                    } else {
                        msg.setMessage(0xE0, 64, 64);
                    }

                    receiver.send(msg, -1);
                } catch (InvalidMidiDataException e) {
                    e.printStackTrace();
                }

                receiver.send(msg, -1);
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "PitchWheel from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable + ", channel: " + channel + ", amount: " + amount));

                if (thruToggleButton != null && thruToggleButton.isChecked() && getMidiOutputDeviceFromSpinner() != null) {
                    getMidiOutputDeviceFromSpinner().sendMidiPitchWheel(cable, channel, amount);
                    midiOutputEventHandler.sendMessage(Message.obtain(midiOutputEventHandler, 0, "PitchWheel from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable + ", channel: " + channel + ", amount: " + amount));
                }
            }

            @Override
            public void onMidiSystemExclusive(@NonNull final MidiInputDevice sender, int cable, final byte[] systemExclusive) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "SystemExclusive from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable + ", data:" + Arrays.toString(systemExclusive)));

                if (thruToggleButton != null && thruToggleButton.isChecked() && getMidiOutputDeviceFromSpinner() != null) {
                    getMidiOutputDeviceFromSpinner().sendMidiSystemExclusive(cable, systemExclusive);
                    midiOutputEventHandler.sendMessage(Message.obtain(midiOutputEventHandler, 0, "SystemExclusive from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable + ", data:" + Arrays.toString(systemExclusive)));
                }
            }

            @Override
            public void onMidiSystemCommonMessage(@NonNull final MidiInputDevice sender, int cable, final byte[] bytes) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "SystemCommonMessage from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable + ", bytes: " + Arrays.toString(bytes)));

                if (thruToggleButton != null && thruToggleButton.isChecked() && getMidiOutputDeviceFromSpinner() != null) {
                    getMidiOutputDeviceFromSpinner().sendMidiSystemCommonMessage(cable, bytes);
                    midiOutputEventHandler.sendMessage(Message.obtain(midiOutputEventHandler, 0, "SystemCommonMessage from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable + ", bytes: " + Arrays.toString(bytes)));
                }
            }

            @Override
            public void onMidiSingleByte(@NonNull final MidiInputDevice sender, int cable, int byte1) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "SingleByte from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable + ", data: " + byte1));

                if (thruToggleButton != null && thruToggleButton.isChecked() && getMidiOutputDeviceFromSpinner() != null) {
                    getMidiOutputDeviceFromSpinner().sendMidiSingleByte(cable, byte1);
                    midiOutputEventHandler.sendMessage(Message.obtain(midiOutputEventHandler, 0, "SingleByte from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable + ", data: " + byte1));
                }
            }

            @Override
            public void onMidiTimeCodeQuarterFrame(@NonNull MidiInputDevice sender, int cable, int timing) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "TimeCodeQuarterFrame from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable + ", timing: " + timing));
            }

            @Override
            public void onMidiSongSelect(@NonNull MidiInputDevice sender, int cable, int song) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "SongSelect from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable + ", song: " + song));
            }

            @Override
            public void onMidiSongPositionPointer(@NonNull MidiInputDevice sender, int cable, int position) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "SongPositionPointer from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable + ", position: " + position));
            }

            @Override
            public void onMidiTuneRequest(@NonNull MidiInputDevice sender, int cable) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "TuneRequest from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable));
            }

            @Override
            public void onMidiTimingClock(@NonNull MidiInputDevice sender, int cable) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "TimingClock from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable));
            }

            @Override
            public void onMidiStart(@NonNull MidiInputDevice sender, int cable) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "Start from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable));
            }

            @Override
            public void onMidiContinue(@NonNull MidiInputDevice sender, int cable) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "Continue from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable));
            }

            @Override
            public void onMidiStop(@NonNull MidiInputDevice sender, int cable) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "Stop from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable));
            }

            @Override
            public void onMidiActiveSensing(@NonNull MidiInputDevice sender, int cable) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "ActiveSensing from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable));
            }

            @Override
            public void onMidiReset(@NonNull MidiInputDevice sender, int cable) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "Reset from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable));
            }

            @Override
            public void onMidiMiscellaneousFunctionCodes(@NonNull final MidiInputDevice sender, int cable, int byte1, int byte2, int byte3) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "MiscellaneousFunctionCodes from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable + ", byte1: " + byte1 + ", byte2: " + byte2 + ", byte3: " + byte3));

                if (thruToggleButton != null && thruToggleButton.isChecked() && getMidiOutputDeviceFromSpinner() != null) {
                    getMidiOutputDeviceFromSpinner().sendMidiMiscellaneousFunctionCodes(cable, byte1, byte2, byte3);
                    midiOutputEventHandler.sendMessage(Message.obtain(midiOutputEventHandler, 0, "MiscellaneousFunctionCodes from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable + ", byte1: " + byte1 + ", byte2: " + byte2 + ", byte3: " + byte3));
                }
            }

            @Override
            public void onMidiCableEvents(@NonNull final MidiInputDevice sender, int cable, int byte1, int byte2, int byte3) {
                midiInputEventHandler.sendMessage(Message.obtain(midiInputEventHandler, 0, "CableEvents from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable + ", byte1: " + byte1 + ", byte2: " + byte2 + ", byte3: " + byte3));

                if (thruToggleButton != null && thruToggleButton.isChecked() && getMidiOutputDeviceFromSpinner() != null) {
                    getMidiOutputDeviceFromSpinner().sendMidiCableEvents(cable, byte1, byte2, byte3);
                    midiOutputEventHandler.sendMessage(Message.obtain(midiOutputEventHandler, 0, "CableEvents from: " + sender.getUsbDevice().getDeviceName() + ", cable: " + cable + ", byte1: " + byte1 + ", byte2: " + byte2 + ", byte3: " + byte3));
                }
            }
        };

        usbMidiDriver.open();

        ListView midiInputEventListView = findViewById(R.id.midiInputEventListView);
        midiInputEventAdapter = new ArrayAdapter<>(this, R.layout.midi_event, R.id.midiEventDescriptionTextView);
        midiInputEventListView.setAdapter(midiInputEventAdapter);

        ListView midiOutputEventListView = findViewById(R.id.midiOutputEventListView);
        midiOutputEventAdapter = new ArrayAdapter<>(this, R.layout.midi_event, R.id.midiEventDescriptionTextView);
        midiOutputEventListView.setAdapter(midiOutputEventAdapter);


        deviceSpinner = findViewById(R.id.deviceNameSpinner);

        connectedDevicesAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, android.R.id.text1, new ArrayList<>());
        deviceSpinner.setAdapter(connectedDevicesAdapter);


        final Animation animCycle = AnimationUtils.loadAnimation(this, R.anim.cycle);
        ImageButton selectedSound = findViewById(R.id.btn_selectSound);
        final Intent InstrumentIntent = new Intent(this, InstrumentsActivity.class);
        selectedSound.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {

                selectedSound.startAnimation(animCycle);
                startActivityForResult(InstrumentIntent, 1);
            }
        });



        Button btn_recording = findViewById(R.id.btn_recording);
        btn_recording.setOnClickListener(new View.OnClickListener() {

            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                btn_recording.startAnimation(animCycle);
                if(btn_recording.getText()=="@string/recording"){
                    btn_recording.setText(R.string.strop_recording);

                }
            }
        });

        ImageButton btn_touch_mode = findViewById(R.id.btn_touchMode);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        usbMidiDriver.close();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            String instrument_path = data.getStringExtra(InstrumentsActivity.RESULT_INSTRUMENT_PATH);
            Toast.makeText(this, "You selected instrument: " + instrument_path, Toast.LENGTH_LONG).show();

            try {
                if (synth != null)
                    synth.close();

                synth = new SoftSynthesizer();
                try {
                    SF2Soundbank sf = new SF2Soundbank(getAssets().open(instrument_path + ".sf2"));
                    synth = new SoftSynthesizer();
                    synth.open();
                    synth.loadAllInstruments(sf);
                    synth.getChannels()[0].programChange(0);
                    receiver = synth.getReceiver();

                    //set receive for pianoView
                    piano.setReceiverForSynthesizer(receiver);
                } catch (Exception e) {
                    Log.e("LOAD SOUND: ", "CAN NOT LOAD SOUND!");
                    showMessage("Can not load sound, Please try again!");
                    SF2Soundbank sf = new SF2Soundbank(getAssets().open(DEFAULT_INSTRUMENT + ".sf2"));
                    synth = new SoftSynthesizer();
                    synth.open();
                    synth.loadAllInstruments(sf);
                    synth.getChannels()[0].programChange(0);
                    receiver = synth.getReceiver();

                    //set receive for pianoView
                    piano.setReceiverForSynthesizer(receiver);
                }


            } catch (IOException | MidiUnavailableException e) {
                e.printStackTrace();
            }

        }
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

}
