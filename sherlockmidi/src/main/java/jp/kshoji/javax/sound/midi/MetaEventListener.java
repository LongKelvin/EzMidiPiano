package jp.kshoji.javax.sound.midi;



import java.util.EventListener;

import androidx.annotation.NonNull;

/**
 * {@link EventListener} for MIDI Meta messages.
 * 
 * @author K.Shoji
 */
public interface MetaEventListener extends EventListener {

	/**
	 * Called at {@link MetaMessage} event has fired
	 * 
	 * @param meta the source event
	 */
	void meta(@NonNull MetaMessage meta);
}
