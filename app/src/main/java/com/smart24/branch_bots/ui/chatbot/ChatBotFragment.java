package com.smart24.branch_bots.ui.chatbot;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.smart24.branch_bots.R;
import com.smart24.branch_bots.data.ChatMessage;
import com.smart24.branch_bots.databinding.FragmentChatBinding;
import com.smart24.branch_bots.shared.viewmodel.ChatBotServiceViewModel;
import com.smart24.branch_bots.shared.viewmodel.MyAudioMultiMediaViewModel;
import com.smart24.branch_bots.utils.SharedUtils;

import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;

public class ChatBotFragment extends Fragment {
    private FragmentChatBinding binding;
    private ChatBotAdapter chatBotAdapter;
    private List<ChatMessage> chatMessageList = new ArrayList<>();
    private MyAudioMultiMediaViewModel myAudioMultiMediaViewModel;
    private ChatBotServiceViewModel chatBotServiceViewModel;


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
        myAudioMultiMediaViewModel = new ViewModelProvider(requireActivity()).get(MyAudioMultiMediaViewModel.class);
        myAudioMultiMediaViewModel.setRequiredDependencies(requireActivity());
        chatBotServiceViewModel = new ViewModelProvider(requireActivity()).get(ChatBotServiceViewModel.class);
        chatBotServiceViewModel.setRequiredDependencies(requireActivity());
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
            if (chatBotServiceViewModel.isRecording()) {
                chatBotServiceViewModel.stopRecordAndStartTranscript();
                binding.recordIcon.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.speaker_filled_ic));
            } else {
                chatBotServiceViewModel.startRecording(requireActivity());
                binding.recordIcon.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.mic_filled_ic));
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
        chatBotServiceViewModel.sendQuestionToChatBot(chatMessage.getText());
        chatMessageList.add(chatMessage);
        updateAdapterList(chatMessageList);
    }


    private List<ChatMessage> getDummyMessageList() {
        List<ChatMessage> chatMessages = new ArrayList<>();
        return chatMessages;
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
        chatBotServiceViewModel.stopAndGetRecording();
        chatBotServiceViewModel.stopPlayingAudio();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        chatBotServiceViewModel.stopPlayingAudio();
    }
}