



package com.midisheetmusic;


/** @class MidiFileException
 * A MidiFileException is thrown when an error occurs
 * while parsing the Midi File.  The constructore takes
 * the file offset (in bytes) where the error occurred,
 * and a string describing the error.
 */
public class MidiFileException extends RuntimeException {
    public MidiFileException(String s, int offset) {
        super(s + " at offset " + offset);
    }
}

