package com.e3roid.audio;

/**
 * A listener for receiving audio input.
 */
public interface AudioInputListener {
	void onReadAudioInput(AudioInputService service, byte[] buffer);
}
