package com.smart24.branch_bots.shared.viewmodel;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.smart24.branch_bots.utils.SharedUtils;

import java.io.File;
import java.io.IOException;

import lombok.Getter;


public class MyAudioMultiMediaViewModel extends ViewModel {

    private Activity activity;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;

    @Getter
    private String audioFilePath;
    private MutableLiveData<Boolean> audioPlayingMld;
    private MutableLiveData<Boolean> recordingMld;


    public void setRequiredDependencies(Activity activity) {
        this.activity = activity;
    }

    public MutableLiveData<Boolean> getAudioPlayingMld() {
        if (audioPlayingMld == null) {
            audioPlayingMld = new MutableLiveData<>(false);
        }
        return audioPlayingMld;
    }

    public MutableLiveData<Boolean> getRecordingMld() {
        if (recordingMld == null) {
            recordingMld = new MutableLiveData<>(false);
        }
        return recordingMld;
    }


    public void setAudioFilePath(String outPutFileName) {
        File outputDir = new File(activity.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "ChatBotAudioRecords");
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                SharedUtils.showToast("Could not create a directory for recording");
            }
        }
        audioFilePath = new File(outputDir, outPutFileName + ".m4a").getAbsolutePath();
    }

    public void startRecording(String outPutFileName) throws IOException {
        if (mediaRecorder == null) {
            try {
                setAudioFilePath(outPutFileName);
                mediaRecorder = new MediaRecorder(activity);
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                mediaRecorder.setOutputFile(audioFilePath);
                mediaRecorder.prepare();
                mediaRecorder.start();
                getRecordingMld().setValue(Boolean.TRUE);
            } catch (IOException e) {
                SharedUtils.showToast("Failed to start recording: " + e.getMessage());
                if (mediaRecorder != null) {
                    mediaRecorder.release();
                    mediaRecorder = null;
                }
                throw e;
            }
        }
    }

    public void stopRecording() {
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                getRecordingMld().setValue(Boolean.FALSE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelRecording() {
        try {
            if (mediaRecorder != null) {
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
                File file = new File(audioFilePath);
                if (file.exists()) {
                    file.delete();
                }
                SharedUtils.showToast("Recording cancelled and file deleted (if existed).");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void startPlayAudio(Activity activity, String filePath) {
        // Stop any currently playing audio before starting a new one
        stopPlayingAudio();
        File audioFile = new File(filePath);
        if (!audioFile.exists()) {
            Toast.makeText(activity, "Not Found Audio File .", Toast.LENGTH_SHORT).show();
            return;
        }
        mediaPlayer = new MediaPlayer();
        try {
            getAudioPlayingMld().setValue(Boolean.TRUE);
            mediaPlayer.setDataSource(audioFile.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> {
                stopPlayingAudio();
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(activity, "Error during playback. What: " + what + ", Extra: " + extra, Toast.LENGTH_LONG).show();
                stopPlayingAudio();
                return true;
            });

        } catch (IOException e) {
            Toast.makeText(activity, "Error preparing audio for playback: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            stopPlayingAudio();
        }
    }

    public void stopPlayingAudio() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            getAudioPlayingMld().setValue(Boolean.FALSE);
        }
    }

}