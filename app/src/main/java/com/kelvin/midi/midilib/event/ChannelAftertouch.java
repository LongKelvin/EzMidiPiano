

package com.kelvin.midi.midilib.event;

public class ChannelAftertouch extends ChannelEvent
{
    public ChannelAftertouch(long tick, int channel, int amount)
    {
        super(tick, ChannelEvent.CHANNEL_AFTERTOUCH, channel, amount, 0);
    }

    public ChannelAftertouch(long tick, long delta, int channel, int amount)
    {
        super(tick, delta, ChannelEvent.CHANNEL_AFTERTOUCH, channel, amount, 0);
    }

    public int getAmount()
    {
        return mValue1;
    }

    public void setAmount(int p)
    {
        mValue1 = p;
    }
}
