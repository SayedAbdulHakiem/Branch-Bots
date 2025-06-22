package com.smart24.alpha_robot.ui.chatbot;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.smart24.alpha_robot.R;
import com.smart24.alpha_robot.data.ChatMessage;
import com.smart24.alpha_robot.data.ChatVoiceMessage;
import com.smart24.alpha_robot.data.MessageTypeEnum;
import com.smart24.alpha_robot.data.TranscribedResponse;
import com.smart24.alpha_robot.databinding.FragmentChatBinding;
import com.smart24.alpha_robot.network.GroqApi;
import com.smart24.alpha_robot.network.GroqRetrofitClient;
import com.smart24.alpha_robot.utils.AudioRecorder;
import com.smart24.alpha_robot.utils.ConstantStrings;
import com.smart24.alpha_robot.utils.DateUtils;
import com.smart24.alpha_robot.utils.SharedUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatBotFragment extends Fragment {
    private FragmentChatBinding binding;
    private ChatBotAdapter chatBotAdapter;
    private AudioRecorder audioRecorder;
    private List<ChatMessage> chatMessageList = new ArrayList<>();


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentChatBinding.inflate(inflater, container, false);

        chatBotAdapter = new ChatBotAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        binding.chatBotMessageAdapter.setLayoutManager(layoutManager);
        binding.chatBotMessageAdapter.setAdapter(chatBotAdapter);
        chatMessageList = getDummyMessageList();
        audioRecorder = new AudioRecorder(requireActivity());
        updateAdapterList(chatMessageList);

        setOnClickListeners();

        return binding.getRoot();

    }

    private void setOnClickListeners() {
        binding.sendIcon.setOnClickListener(view -> {
            if (binding.textEd.getText() != null && binding.textEd.getText().length() > 0) {
                sendMessage(binding.textEd.getText().toString());
                binding.textEd.setText("");
            } else {
                SharedUtils.showMessageInfo(requireActivity(), getString(R.string.not_valid_message));
            }

        });

        binding.recordIcon.setOnClickListener(view -> {
            if (audioRecorder.isRecording()) {
                File recordingFile = stopAndGetRecording();
                sendMessage(recordingFile);
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

    private void sendMessage(String text) {
        chatMessageList.add(new ChatMessage(text, MessageTypeEnum.TEXT));
        updateAdapterList(chatMessageList);

    }

    private void sendMessage(File voiceFile) {
        ChatMessage chatMessage = new ChatVoiceMessage(voiceFile, null);
        GroqApi groqApi = GroqRetrofitClient.getRetrofitInstance().create(GroqApi.class);
        RequestBody requestFile = RequestBody.create(voiceFile, MediaType.parse("audio/*"));
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", voiceFile.getName(), requestFile);
        MultipartBody.Part modelPart = MultipartBody.Part.createFormData("model", ConstantStrings.GROQ_TRANSCRIPT_MODEL_ID);

        Call<TranscribedResponse> transcribedResponseCall = groqApi.transcribeAudio(ConstantStrings.GROQ_API_AUTHORIZATION, filePart, modelPart);
        transcribedResponseCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<TranscribedResponse> call, Response<TranscribedResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    TranscribedResponse transcribedResponse = response.body();
                    chatMessage.setText(transcribedResponse.getText());
                    chatMessageList.add(chatMessage);
                    updateAdapterList(chatMessageList);
                } else {
                    SharedUtils.showMessageNegative(requireActivity(), getString(R.string.third_party_call_failed) + " " + response.message());
                }

            }

            @Override
            public void onFailure(Call<TranscribedResponse> call, Throwable t) {
                SharedUtils.showMessageNegative(requireActivity(), getString(R.string.third_party_call_failed) + call.toString());

            }
        });

    }
    private List<ChatMessage> getDummyMessageList() {
        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatMessage("hello ChatBot ", MessageTypeEnum.TEXT));
        chatMessages.add(new ChatMessage("this is the application developer testing you and i need you to introduce yourself\ncan your say any thing? ", MessageTypeEnum.TEXT));
        chatMessages.add(new ChatMessage("hello Developer", MessageTypeEnum.TEXT, "2", "chatBot"));
        chatMessages.add(new ChatMessage("I'm the new Chat Bot Developed for Alpha Bot App by 24 Smart Tech Company", MessageTypeEnum.TEXT, "2", "chatBot"));
        chatMessages.add(new ChatMessage("How can i help you ?", MessageTypeEnum.VOICE, "2", "chatBot"));
        chatMessages.add(new ChatMessage("hello ChatBot ", MessageTypeEnum.VOICE));
        chatMessages.add(new ChatMessage("Hello Developer\nHow can i help you ?", MessageTypeEnum.TEXT, "2", "chatBot"));
        chatMessages.add(new ChatMessage("hello ChatBot ", MessageTypeEnum.TEXT));
        chatMessages.add(new ChatMessage("Hello Developer\nHow can i help you ?", MessageTypeEnum.VOICE, "2", "chatBot"));
        chatMessages.add(new ChatMessage("hello ChatBot ", MessageTypeEnum.TEXT));
        chatMessages.add(new ChatMessage("Hello Developer\nHow can i help you ?", MessageTypeEnum.TEXT, "2", "chatBot"));
        chatMessages.add(new ChatMessage("hello ChatBot ", MessageTypeEnum.TEXT));
        return chatMessages;
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
        audioRecorder.stopRecording();
        return new File(audioRecorder.getAudioFilePath());
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireActivity().recreate();
        binding = null;
        stopAndGetRecording();
        stopPlayingAudio();
    }

}