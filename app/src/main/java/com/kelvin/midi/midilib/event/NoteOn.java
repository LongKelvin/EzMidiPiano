

package com.kelvin.midi.midilib.event;

public class NoteOn extends ChannelEvent
{
    public NoteOn(long tick, int channel, int note, int velocity)
    {
        super(tick, ChannelEvent.NOTE_ON, channel, note, velocity);
    }

    public NoteOn(long tick, long delta, int channel, int note, int velocity)
    {
        super(tick, delta, ChannelEvent.NOTE_ON, channel, note, velocity);
    }

    public int getNoteValue()
    {
        return mValue1;
    }

    public int getVelocity()
    {
        return mValue2;
    }

    public void setNoteValue(int p)
    {
        mValue1 = p;
    }

    public void setVelocity(int v)
    {
        mValue2 = v;
    }
}
