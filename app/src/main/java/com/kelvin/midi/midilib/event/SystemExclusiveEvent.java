

package com.kelvin.midi.midilib.event;

import java.io.IOException;
import java.io.OutputStream;

import com.kelvin.midi.midilib.util.VariableLengthInt;

/**
 * Full Disclosure, SysEx events may not be properly handled by this library.
 * 
 */
public class SystemExclusiveEvent extends MidiEvent
{
    private int mType;
    private VariableLengthInt mLength;
    private byte[] mData;

    public SystemExclusiveEvent(int type, long tick, byte[] data)
    {
        this(type, tick, 0, data);
    }

    public SystemExclusiveEvent(int type, long tick, long delta, byte[] data)
    {
        super(tick, delta);

        mType = type & 0xFF;
        if(mType != 0xF0 && mType != 0xF7)
        {
            mType = 0xF0;
        }

        mLength = new VariableLengthInt(data.length);
        mData = data;
    }

    public byte[] getData()
    {
        return mData;
    }

    public void setData(byte[] data)
    {
        mLength.setValue(data.length);
        mData = data;
    }

    @Override
    public boolean requiresStatusByte(MidiEvent prevEvent)
    {
        return true;
    }

    @Override
    public void writeToFile(OutputStream out, boolean writeType) throws IOException
    {
        super.writeToFile(out, writeType);

        out.write(mType);
        out.write(mLength.getBytes());
        out.write(mData);
    }

    @Override
    public int compareTo(MidiEvent other)
    {
        if(this.mTick < other.mTick)
        {
            return -1;
        }
        if(this.mTick > other.mTick)
        {
            return 1;
        }

        if(this.mDelta.getValue() > other.mDelta.getValue())
        {
            return -1;
        }
        if(this.mDelta.getValue() < other.mDelta.getValue())
        {
            return 1;
        }

        if(other instanceof SystemExclusiveEvent)
        {
            String curr = new String(mData);
            String comp = new String(((SystemExclusiveEvent) other).mData);
            return curr.compareTo(comp);
        }

        return 1;
    }

    @Override
    protected int getEventSize()
    {
        return 1 + mLength.getByteCount() + mData.length;
    }

}
