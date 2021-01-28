

package com.kelvin.midi.midilib.event;

public class Controller extends ChannelEvent
{
    public Controller(long tick, int channel, int controllerType, int value)
    {
        super(tick, ChannelEvent.CONTROLLER, channel, controllerType, value);
    }

    public Controller(long tick, long delta, int channel, int controllerType, int value)
    {
        super(tick, delta, ChannelEvent.CONTROLLER, channel, controllerType, value);
    }

    public int getControllerType()
    {
        return mValue1;
    }

    public int getValue()
    {
        return mValue2;
    }

    public void setControllerType(int t)
    {
        mValue1 = t;
    }

    public void setValue(int v)
    {
        mValue2 = v;
    }
}
