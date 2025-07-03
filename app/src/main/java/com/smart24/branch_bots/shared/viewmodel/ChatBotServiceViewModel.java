package com.smart24.branch_bots.shared.viewmodel;

import android.os.Environment;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.smart24.branch_bots.R;
import com.smart24.branch_bots.data.AnswerResponse;
import com.smart24.branch_bots.data.QuestionRequest;
import com.smart24.branch_bots.data.SpeechToTextResponse;
import com.smart24.branch_bots.data.TextToSpeechRequest;
import com.smart24.branch_bots.network.GroqApi;
import com.smart24.branch_bots.network.GroqRetrofitClient;
import com.smart24.branch_bots.network.Smart24RetrofitClient;
import com.smart24.branch_bots.network.SmartTechApi;
import com.smart24.branch_bots.utils.ConstantStrings;
import com.smart24.branch_bots.utils.DateUtils;
import com.smart24.branch_bots.utils.SharedUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatBotServiceViewModel extends ViewModel {
    private FragmentActivity activity;
    private MyAudioMultiMediaViewModel myAudioMultiMediaViewModel;
    private GroqApi groqApi;
    private SmartTechApi smartTechApi;
    private MutableLiveData<Boolean> audioPlayingMld;
    private MutableLiveData<Boolean> messageSendingMld;


    private ChatBotServiceViewModel() {

    }

    public void setRequiredDependencies(FragmentActivity activity) {
        this.activity = activity;
        groqApi = GroqRetrofitClient.getRetrofitInstance().create(GroqApi.class);
        smartTechApi = Smart24RetrofitClient.getRetrofitInstance().create(SmartTechApi.class);
        myAudioMultiMediaViewModel = new ViewModelProvider(activity).get(MyAudioMultiMediaViewModel.class);
    }

    public void sendQuestionToChatBot(String questionText) {
//        chatMessageList.add(chatMessage);
        QuestionRequest questionRequest = new QuestionRequest();
        questionRequest.setQuestion(questionText);
        questionRequest.setStreaming(false);
        Call<AnswerResponse> sendQuestionCall = smartTechApi.askQuestion(questionRequest);
        sendQuestionCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<AnswerResponse> call, Response<AnswerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String answerResponse = response.body().getText();
                    textToSpeechCall(answerResponse, "chatBotId", "chatBotName");

                } else {
                    SharedUtils.showMessageNegative(activity, activity.getString(R.string.third_party_call_failed) + " " + response.message());
                }

            }

            @Override
            public void onFailure(Call<AnswerResponse> call, Throwable t) {
                SharedUtils.showMessageNegative(activity, activity.getString(R.string.third_party_call_failed) + call.toString());

            }
        });

    }

    public void speechToTextCall(File voiceFile) {
        RequestBody requestFile = RequestBody.create(voiceFile, MediaType.parse("audio/*"));
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", voiceFile.getName(), requestFile);
        MultipartBody.Part modelPart = MultipartBody.Part.createFormData("model", ConstantStrings.GROQ_TRANSCRIPT_MODEL_ID);

        Call<SpeechToTextResponse> transcribedResponseCall = groqApi.speechToText(ConstantStrings.GROQ_API_AUTHORIZATION, filePart, modelPart);
        transcribedResponseCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<SpeechToTextResponse> call, Response<SpeechToTextResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SpeechToTextResponse speechToTextResponse = response.body();
                    sendQuestionToChatBot(speechToTextResponse.getText());
                } else {
                    SharedUtils.showMessageNegative(activity, activity.getString(R.string.third_party_call_failed) + " " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SpeechToTextResponse> call, Throwable t) {
                SharedUtils.showMessageNegative(activity, activity.getString(R.string.third_party_call_failed) + call.toString());

            }
        });

    }

    private void textToSpeechCall(String input, String senderId, String senderName) {
        TextToSpeechRequest ttsBody = new TextToSpeechRequest();
        ttsBody.setInput(input);
        ttsBody.setModel(ConstantStrings.GROQ_SPEECH_MODEL_ID);
        ttsBody.setVoice(ConstantStrings.GROQ_SPEECH_VOICE);
        ttsBody.setResponseFormat(ConstantStrings.GROQ_SPEECH_RESPONSE_TYPE);

        Call<ResponseBody> textToSpeechCall = groqApi.textToSpeech(ConstantStrings.GROQ_API_AUTHORIZATION, ttsBody);
        textToSpeechCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String audioFileName = ConstantStrings.SPEECH_FILE_NAME + DateUtils.getCurrentTimeStamp() + "." + ConstantStrings.GROQ_SPEECH_RESPONSE_TYPE;
                    File speechAudio = convertResponseBodyToFile(response.body(), audioFileName);
                    if (speechAudio != null) {
                        myAudioMultiMediaViewModel.startPlayAudio(activity, speechAudio.getAbsolutePath());
                    } else {
                        SharedUtils.showMessageNegative(activity, activity.getString(R.string.error_while_saving_audio_file) + " " + response.message());
                    }

                } else {
                    SharedUtils.showMessageNegative(activity, activity.getString(R.string.third_party_call_failed) + " " + response.message());
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                SharedUtils.showMessageNegative(activity, activity.getString(R.string.third_party_call_failed) + call.toString());

            }
        });
    }

    public File saveAudioFile(ResponseBody body, String fileName) throws IOException {
        File outputDir = new File(activity.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "GroqTTS");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        File outputFile = new File(outputDir, fileName);

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            byte[] fileReader = new byte[4096]; // Buffer size
            long fileSize = body.contentLength();
            long fileSizeDownloaded = 0;

            inputStream = body.byteStream();
            outputStream = new FileOutputStream(outputFile);

            while (true) {
                int read = inputStream.read(fileReader);
                if (read == -1) {
                    break;
                }
                outputStream.write(fileReader, 0, read);
                fileSizeDownloaded += read;
                // You can add progress updates here if needed
            }

            outputStream.flush();
            return outputFile;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    public File convertResponseBodyToFile(ResponseBody responseBody, String fileName) {
        try {
            File outputDir = new File(activity.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "GroqTTS");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            // Create file in internal storage or external storage
            File audioFile = new File(outputDir, fileName);

            // Create input stream from response body
            InputStream inputStream = responseBody.byteStream();

            // Create output stream to write to file
            FileOutputStream outputStream = new FileOutputStream(audioFile);

            // Buffer for reading data
            byte[] buffer = new byte[4096];
            int bytesRead;

            // Write data to file
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            // Close streams
            outputStream.close();
            inputStream.close();

            return audioFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public void startRecording(FragmentActivity activity) {
        this.activity = activity;
        if (!SharedUtils.checkAndRequestPermissions(activity)) {
            SharedUtils.showMessageNegative(activity, activity.getString(R.string.require_permission));
            return;
        }
        try {
            myAudioMultiMediaViewModel.startRecording(getRandomRecordName());
        } catch (Exception e) {
            SharedUtils.showMessageNegative(activity, activity.getString(R.string.recording_error) + " " + e.getMessage());
        }
    }

    public String getRandomRecordName() {
        return "message_" + DateUtils.getCurrentTimeStamp();
    }

    public void stopRecordAndStartTranscript() {
        File audioFile = stopAndGetRecording();
        speechToTextCall(audioFile);
    }

    public File stopAndGetRecording() {
        File file = null;
        try {
            myAudioMultiMediaViewModel.stopRecording();
            file = new File(myAudioMultiMediaViewModel.getAudioFilePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }


    public void startPlayRecordedAudio(String audioFilePath) {
        myAudioMultiMediaViewModel.startPlayAudio(activity, audioFilePath);
    }

    public void stopPlayingAudio() {
        myAudioMultiMediaViewModel.stopPlayingAudio();
    }

    public boolean isRecording() {
        return myAudioMultiMediaViewModel.getRecordingMld().getValue();
    }

}
