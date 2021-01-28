
package com.kelvin.midi.midilib.event;

public class PitchBend extends ChannelEvent
{
    public PitchBend(long tick, int channel, int lsb, int msb)
    {
        super(tick, ChannelEvent.PITCH_BEND, channel, lsb, msb);
    }

    public PitchBend(long tick, long delta, int channel, int lsb, int msb)
    {
        super(tick, delta, ChannelEvent.PITCH_BEND, channel, lsb, msb);
    }

    public int getLeastSignificantBits()
    {
        return mValue1;
    }

    public int getMostSignificantBits()
    {
        return mValue2;
    }

    public int getBendAmount()
    {
        int y = (mValue2 & 0x7F) << 7;
        int x = (mValue1);

        return y + x;
    }

    public void setLeastSignificantBits(int p)
    {
        mValue1 = p & 0x7F;
    }

    public void setMostSignificantBits(int p)
    {
        mValue2 = p & 0x7F;
    }

    public void setBendAmount(int amount)
    {
        amount = amount & 0x3FFF;
        mValue1 = (amount & 0x7F);
        mValue2 = amount >> 7;
    }
}
