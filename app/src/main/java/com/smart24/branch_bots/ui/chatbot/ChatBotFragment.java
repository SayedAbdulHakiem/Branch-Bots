package com.smart24.branch_bots.ui.chatbot;

import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.smart24.branch_bots.R;
import com.smart24.branch_bots.data.AnswerResponse;
import com.smart24.branch_bots.data.ChatMessage;
import com.smart24.branch_bots.data.ChatVoiceMessage;
import com.smart24.branch_bots.data.QuestionRequest;
import com.smart24.branch_bots.data.SpeechToTextResponse;
import com.smart24.branch_bots.data.TextToSpeechRequest;
import com.smart24.branch_bots.databinding.FragmentChatBinding;
import com.smart24.branch_bots.network.GroqApi;
import com.smart24.branch_bots.network.GroqRetrofitClient;
import com.smart24.branch_bots.network.Smart24Api;
import com.smart24.branch_bots.network.Smart24RetrofitClient;
import com.smart24.branch_bots.utils.AudioRecorder;
import com.smart24.branch_bots.utils.ConstantStrings;
import com.smart24.branch_bots.utils.DateUtils;
import com.smart24.branch_bots.utils.SharedUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import pl.droidsonroids.gif.GifDrawable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatBotFragment extends Fragment {
    private FragmentChatBinding binding;
    private ChatBotAdapter chatBotAdapter;
    private AudioRecorder audioRecorder;
    private List<ChatMessage> chatMessageList = new ArrayList<>();
    private GroqApi groqApi;
    private Smart24Api smart24Api;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentChatBinding.inflate(inflater, container, false);

        chatBotAdapter = new ChatBotAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setSmoothScrollbarEnabled(true);
        binding.chatMessagesRv.setLayoutManager(layoutManager);
        binding.chatMessagesRv.setAdapter(chatBotAdapter);
        chatMessageList = getDummyMessageList();
        audioRecorder = new AudioRecorder(requireActivity());
        groqApi = GroqRetrofitClient.getRetrofitInstance().create(GroqApi.class);
        smart24Api = Smart24RetrofitClient.getRetrofitInstance().create(Smart24Api.class);
        if (!chatMessageList.isEmpty()) {
            updateAdapterList(chatMessageList);
        }

        setOnClickListeners();
        playFaceGif();
        return binding.getRoot();

    }

    private void setOnClickListeners() {
        binding.sendIcon.setOnClickListener(view -> {
            if (binding.textEd.getText() != null && binding.textEd.getText().length() > 0) {
                ChatMessage chatMessage = new ChatMessage(binding.textEd.getText().toString());
                sendQuestionToChatBot(chatMessage);
                binding.textEd.setText("");
            } else {
                SharedUtils.showMessageInfo(requireActivity(), getString(R.string.not_valid_message));
            }

        });

        binding.recordIcon.setOnClickListener(view -> {
            if (audioRecorder.isRecording()) {
                File recordingFile = stopAndGetRecording();
                speechToTextCall(recordingFile);
                binding.recordIcon.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.speak));
            } else {
                startRecording();
                binding.recordIcon.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.microphone_ic));
            }
        });


        binding.textEd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (s.length() > 0) {
                    binding.sendIcon.setVisibility(View.VISIBLE);
                    binding.recordIcon.setVisibility(View.GONE);
                } else {
                    binding.recordIcon.setVisibility(View.VISIBLE);
                    binding.sendIcon.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void playFaceGif() {
        binding.faceGif.setImageResource(R.drawable.face_2_1);
        final MediaController mc = new MediaController(requireActivity());
        mc.setMediaPlayer((GifDrawable) binding.faceGif.getDrawable());
        mc.show();
    }

    private void sendQuestionToChatBot(ChatMessage chatMessage) {
        chatMessageList.add(chatMessage);
        updateAdapterList(chatMessageList);
        QuestionRequest questionRequest = new QuestionRequest();
        questionRequest.setQuestion(chatMessage.getText());
        questionRequest.setStreaming(false);
        Call<AnswerResponse> sendQuestionCall = smart24Api.askQuestion(questionRequest);
        sendQuestionCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<AnswerResponse> call, Response<AnswerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String answerResponse = response.body().getText();
                    textToSpeechCall(answerResponse, "chatBotId", "chatBotName");

                } else {
                    SharedUtils.showMessageNegative(requireActivity(), getString(R.string.third_party_call_failed) + " " + response.message());
                }

            }

            @Override
            public void onFailure(Call<AnswerResponse> call, Throwable t) {
                SharedUtils.showMessageNegative(requireActivity(), getString(R.string.third_party_call_failed) + call.toString());

            }
        });

    }

    private void speechToTextCall(File voiceFile) {
        ChatVoiceMessage chatMessage = new ChatVoiceMessage(voiceFile, null);
        RequestBody requestFile = RequestBody.create(voiceFile, MediaType.parse("audio/*"));
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", voiceFile.getName(), requestFile);
        MultipartBody.Part modelPart = MultipartBody.Part.createFormData("model", ConstantStrings.GROQ_TRANSCRIPT_MODEL_ID);

        Call<SpeechToTextResponse> transcribedResponseCall = groqApi.speechToText(ConstantStrings.GROQ_API_AUTHORIZATION, filePart, modelPart);
        transcribedResponseCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<SpeechToTextResponse> call, Response<SpeechToTextResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SpeechToTextResponse speechToTextResponse = response.body();
                    chatMessage.setText(speechToTextResponse.getText());
                    sendQuestionToChatBot(chatMessage);
                } else {
                    SharedUtils.showMessageNegative(requireActivity(), getString(R.string.third_party_call_failed) + " " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SpeechToTextResponse> call, Throwable t) {
                SharedUtils.showMessageNegative(requireActivity(), getString(R.string.third_party_call_failed) + call.toString());

            }
        });

    }

    private void textToSpeechCall(String input, String senderId, String senderName) {
        ChatVoiceMessage chatMessage = new ChatVoiceMessage(null, input, senderId, senderName);

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
                        chatMessage.setVoiceFile(speechAudio);
                        chatMessageList.add(chatMessage);
                        updateAdapterList(chatMessageList);
                        audioRecorder.startPlayAudio(requireActivity(), speechAudio.getAbsolutePath());
                    } else {
                        SharedUtils.showMessageNegative(requireActivity(), getString(R.string.error_while_saving_audio_file) + " " + response.message());
                    }

                } else {
                    SharedUtils.showMessageNegative(requireActivity(), getString(R.string.third_party_call_failed) + " " + response.message());
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                SharedUtils.showMessageNegative(requireActivity(), getString(R.string.third_party_call_failed) + call.toString());

            }
        });
    }
    private List<ChatMessage> getDummyMessageList() {
        List<ChatMessage> chatMessages = new ArrayList<>();
        return chatMessages;
    }

    private File saveAudioFile(ResponseBody body, String fileName) throws IOException {
        File outputDir = new File(requireActivity().getExternalFilesDir(Environment.DIRECTORY_MUSIC), "GroqTTS");
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

    private File convertResponseBodyToFile(ResponseBody responseBody, String fileName) {
        try {
            File outputDir = new File(requireActivity().getExternalFilesDir(Environment.DIRECTORY_MUSIC), "GroqTTS");
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


    private void startRecording() {
        if (!SharedUtils.checkAndRequestPermissions(requireActivity())) {
            SharedUtils.showMessageNegative(requireActivity(), getString(R.string.require_permission));
            return;
        }
        try {
            audioRecorder.startRecording(getRandomRecordName());
        } catch (Exception e) {
            SharedUtils.showMessageNegative(requireActivity(), getString(R.string.recording_error) + " " + e.getMessage());
        }
    }

    private String getRandomRecordName() {
        return "message_" + DateUtils.getCurrentTimeStamp();
    }

    private File stopAndGetRecording() {
        File file = null;
        try {
            audioRecorder.stopRecording();
            file = new File(audioRecorder.getAudioFilePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }


    private void startPlayRecordedAudio(String audioFilePath) {
        audioRecorder.startPlayAudio(requireActivity(), audioFilePath);
    }

    private void stopPlayingAudio() {
        audioRecorder.stopPlayingAudio();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private void updateAdapterList(List<ChatMessage> chatMessageList) {
        this.chatBotAdapter.setDataList(chatMessageList);
        this.chatBotAdapter.notifyItemInserted(chatMessageList.size() - 1);
        binding.chatMessagesRv.scrollToPosition(chatMessageList.size() - 1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireActivity().recreate();
        binding = null;
        stopAndGetRecording();
        stopPlayingAudio();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlayingAudio();
    }
}