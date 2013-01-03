package com.e3roid.audio;

import java.io.ByteArrayInputStream;

import android.media.AudioFormat;
import android.media.AudioRecord;

import com.e3roid.lifecycle.E3Service;
import com.e3roid.util.Debug;

/**
 * AudioInputService class provides audio input recoding service.
 */
public class AudioInputService extends E3Service {

	protected boolean canceled  = false;
	protected boolean recording = false;
	
	protected AudioRecord recorder;
	protected final int readBufferSize;
	protected final byte[] readBuffer;
	
	protected final int audioSource;
	protected final int sampleRateInHz;
	protected final int channelConfig;
	protected final int audioFormat;
	
	protected AudioInputListener audioInputListener;
	
	/**
	 * Constructs audio input service with default buffer size.
	 * @param audioSource audio input source
	 * @param sampleRateInHz sample rate in hz
	 * @param channelConfig channel config
	 * @param audioFormat audio format
	 * @param listener AudioInputListener
	 */
	public AudioInputService(int audioSource, int sampleRateInHz, 
			int channelConfig, int audioFormat, AudioInputListener listener) {
		this(audioSource, sampleRateInHz, channelConfig, audioFormat,
				getMinBufferSize(sampleRateInHz, channelConfig, audioFormat), listener);
	}
	
	/**
	 * Constructs audio input serivice with given parameters.
	 * @param audioSource audio input source
	 * @param sampleRateInHz sample rate in hz
	 * @param channelConfig channel config
	 * @param audioFormat audio format
	 * @param readBufferSize read buffer size
	 * @param listener AudioInputListener
	 */
	public AudioInputService(int audioSource, int sampleRateInHz, 
		int channelConfig, int audioFormat, int readBufferSize, AudioInputListener listener) {
		this.readBufferSize = readBufferSize;
		this.readBuffer = new byte[readBufferSize];
		this.audioSource = audioSource;
		this.sampleRateInHz = sampleRateInHz;
		this.channelConfig = channelConfig;
		this.audioFormat = audioFormat;
		
		this.audioInputListener = listener;
	}
	
	protected static int getMinBufferSize(int sampleRateInHz, int channelConfig, int audioFormat) {
		if (audioFormat == AudioFormat.ENCODING_PCM_8BIT) {
			return 4096;
		} else {
			return AudioRecord.getMinBufferSize(
				sampleRateInHz, channelConfig, audioFormat) * 2;
		}
	}
	
	protected AudioRecord createRecorder() {
		return new AudioRecord(audioSource, sampleRateInHz,
					channelConfig, audioFormat, readBufferSize);
	}

	/**
	 * Starts audio input serivice.
	 */
	@Override
	public void run() {
		try {
			recording = true;
			canceled  = false;
			
			if (recorder == null || recorder.getState() != AudioRecord.STATE_INITIALIZED) {
				recorder = createRecorder();
			}
			
			recorder.startRecording();
			
			while(!canceled) {
				recorder.read(readBuffer, 0, readBufferSize);
				if (audioInputListener != null) {
					audioInputListener.onReadAudioInput(this, readBuffer);
				}
			}
		} finally {
			onStop();
		}
	}
	
	/**
	 * Set audio input listener
	 * @param listener AudioInputListener
	 */
	public void setAudioInputListener(AudioInputListener listener) {
		this.audioInputListener = listener;
	}
	
	/**
	 * Returns read buffer size
	 * @return read buffer size
	 */
	public int getReadBufferSize() {
		return readBufferSize;
	}

	/**
	 * Request to stop audio input service.
	 */
	public synchronized void requestStop() {
		canceled = true;
	}
	
	/**
	 * Stop audio input service.
	 */
	public synchronized void onStop() {
		try {
			if (recording && recorder.getState() == AudioRecord.STATE_INITIALIZED) {
				recorder.stop();
				recorder.release();
			}
		} catch (Exception e) {
			Debug.e(e);
		} finally {
			recording = false;
			canceled  = true;
		}
	}
	
	/**
	 * Called when the activity's onResume event occurs.
	 */
	@Override
	public void onResume() {
		// nothing to do
	}

	/**
	 * Called when the activity's onPause event occurs.
	 */
	@Override
	public void onPause() {
		requestStop();
	}

	/**
	 * Called when the activity's onDispose event occurs.
	 */
	@Override
	public void onDispose() {
		onStop();
	}

	/**
	 * Returns whether being recoding or not
	 * @return
	 */
	public boolean isRecording() {
		return recording;
	}

	/**
	 * Read byte buffers and returns wave value as 16bit audio.
	 * @param bin audio input buffer
	 * @return wave value as 16bit audio
	 */
	public static double readAs16bitWave(ByteArrayInputStream bin) {
		byte b1 = (byte)bin.read();
		byte b2 = (byte)bin.read();
		return (double)(b2 << 8 | b1 & 0xFF) / 32767.0;
	}
	
	/**
	 * Read byte buffers and returns wave value as 8bit audio.
	 * @param bin audio input buffer
	 * @return wave value as 8bit audio
	 */
	public static double readAs8bitWave(ByteArrayInputStream bin) {
		byte b = (byte)bin.read();
		return (double)(b - 128 << 8) / 32767.0;
	}
}
