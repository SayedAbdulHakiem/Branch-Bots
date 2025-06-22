package com.smart24.alpha_robot.utils;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import lombok.Getter;


public class AudioRecorder {

    private final Context context;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;

    @Getter
    private String audioFilePath;

    @Getter
    private boolean isRecording = false;

    public AudioRecorder(Context context) {
        this.context = context;
    }


    public void setAudioFilePath(String outPutFileName) {
        File outputDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "ChatBotAudioRecords");
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
                mediaRecorder = new MediaRecorder(context);
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                mediaRecorder.setOutputFile(audioFilePath);
                mediaRecorder.prepare();
                mediaRecorder.start();
                this.isRecording = true;
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
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;

        }
    }

    public void cancelRecording() {
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
    }


    public void startPlayAudio(Activity activity, String filePath) {
        // Stop any currently playing audio before starting a new one
        stopPlayingAudio();
        if (!(new File(filePath).exists())) {
            Toast.makeText(activity, "Not Found Audio File .", Toast.LENGTH_SHORT).show();
            return;
        }
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(filePath);
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
        }
    }

}