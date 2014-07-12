package com.app.yakbak;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class YakBakActivity extends Activity {
		private static final int RECORDER_SAMPLERATE = 8000;
		private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
		private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
		private AudioRecord recorder = null;
		private Thread recordingThread = null;
		private boolean isRecording = false;
		String filePath = "/sdcard/voice8K16bitmono.pcm";
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
		    super.onCreate(savedInstanceState);
		    setContentView(R.layout.activity_yak_bak);

		    setButtonHandlers();
		    enableButtons(false);

		    int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
		            RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);

		    System.out.println("BUFFER SIZE VALUE IS " + bufferSize);

		}

		private void setButtonHandlers() {
		    ((Button) findViewById(R.id.btnRecord)).setOnClickListener(btnClick);
		    ((Button) findViewById(R.id.btnStop)).setOnClickListener(btnClick);
		}

		private void enableButton(int id, boolean isEnable) {
		    ((Button) findViewById(id)).setEnabled(isEnable);
		}

		private void enableButtons(boolean isRecording) {
		    enableButton(R.id.btnRecord, !isRecording);
		    enableButton(R.id.btnStop, isRecording);
		}

		int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we
		                                // use only 1024
		int BytesPerElement = 2; // 2 bytes in 16bit format

		private void startRecording() {

		    recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
		            RECORDER_SAMPLERATE, RECORDER_CHANNELS,
		            RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);

		    recorder.startRecording();
		    isRecording = true;

		    recordingThread = new Thread(new Runnable() {

		        public void run() {

		            writeAudioDataToFile();

		        }
		    }, "AudioRecorder Thread");
		    recordingThread.start();
		}

		private byte[] short2byte(short[] sData) {
		    int shortArrsize = sData.length;
		    byte[] bytes = new byte[shortArrsize * 2];

		    for (int i = 0; i < shortArrsize; i++) {
		        bytes[i * 2] = (byte) (sData[i] & 0x00FF);
		        bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
		        sData[i] = 0;
		    }
		    return bytes;

		}

		private void writeAudioDataToFile() {
		    // Write the output audio in byte

		    
		    short sData[] = new short[BufferElements2Rec];

		    FileOutputStream os = null;
		    try {
		        os = new FileOutputStream(filePath);
		    } catch (FileNotFoundException e) {
		        e.printStackTrace();
		    }

		    while (isRecording) {
		        // gets the voice output from microphone to byte format

		        recorder.read(sData, 0, BufferElements2Rec);
		        System.out.println("Short wirting to file" + sData.toString());
		        try {
		            // // writes the data to file from buffer
		            // // stores the voice buffer

		            byte bData[] = short2byte(sData);

		            os.write(bData, 0, BufferElements2Rec * BytesPerElement);
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		    }

		    try {
		        os.close();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}

		private void stopRecording() {
		    // stops the recording activity
		    if (null != recorder) {
		        isRecording = false;

		        recorder.stop();
		        recorder.release();

		        recorder = null;
		        recordingThread = null;
		    }
		}

		private View.OnClickListener btnClick = new View.OnClickListener() {
		    public void onClick(View v) {
		        switch (v.getId()) {
		        case R.id.btnRecord: {
		            enableButtons(true);
		            startRecording();
		            break;
		        }
		        case R.id.btnStop: {
		            enableButtons(false);
		            stopRecording();
		            break;
		        }
		        case R.id.btnPlay: {
		        	try {
		        		PlayShortAudioFileViaAudioTrack(filePath);
		        	} catch (IOException e) {
		        		e.printStackTrace();
		        	}
		        }
		        }
		    }
		};

		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {

		    if (keyCode == KeyEvent.KEYCODE_BACK) {

		        finish();
		    }
		    return super.onKeyDown(keyCode, event);
		}
		
		private void PlayShortAudioFileViaAudioTrack(String filePath) throws IOException {
		// We keep temporarily filePath globally as we have only two sample sounds now..
			if (filePath==null) return;

			//Reading the file..
			byte[] byteData = null; 
			File file = null; 
			file = new File(filePath); // for ex. path= "/sdcard/samplesound.pcm" or "/sdcard/samplesound.wav"
			byteData = new byte[(int) file.length()];
			FileInputStream in = null;
		try {
			in = new FileInputStream( file );
			in.read( byteData );
			in.close(); 

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Set and push to audio track..
		int intSize = android.media.AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
		AudioFormat.ENCODING_PCM_8BIT); 
		AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, 8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
		AudioFormat.ENCODING_PCM_8BIT, intSize, AudioTrack.MODE_STREAM); 
		if (at!=null) { 
			at.play();
			// Write the byte array to the track
			at.write(byteData, 0, byteData.length); 
			at.stop();
			at.release();
		}
		else
			Log.d("TCAudio", "audio track is not initialised ");

		}
	}


