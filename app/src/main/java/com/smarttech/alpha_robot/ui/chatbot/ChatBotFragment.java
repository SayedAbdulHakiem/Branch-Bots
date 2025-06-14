package com.smarttech.alpha_robot.ui.chatbot;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.smarttech.alpha_robot.data.ChatMessage;
import com.smarttech.alpha_robot.data.MessageTypeEnum;
import com.smarttech.alpha_robot.databinding.FragmentChatBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatBotFragment extends Fragment {

    private ChatBotAdapter chatBotAdapter;

    private List<ChatMessage> chatMessageList = new ArrayList<>();

    private FragmentChatBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentChatBinding.inflate(inflater, container, false);

        chatBotAdapter = new ChatBotAdapter(this);
        binding.chatBotMessageAdapter.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.chatBotMessageAdapter.setAdapter(chatBotAdapter);
        chatMessageList = getDummyMessageList();
        updateAdapterList(chatMessageList);

        return binding.getRoot();

    }

    private List<ChatMessage> getDummyMessageList() {
        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatMessage("hello ChatBot ", MessageTypeEnum.TEXT, "1", "sayed", new Date().getTime(), true));
        chatMessages.add(new ChatMessage("this is the application developer testing you and i need you to introduce yourself\ncan your say any thing? ", MessageTypeEnum.TEXT, "1", "sayed", new Date().getTime(), true));
        chatMessages.add(new ChatMessage("hello Developer", MessageTypeEnum.TEXT, "2", "chatBot", new Date().getTime(), false));
        chatMessages.add(new ChatMessage("I'm the new Chat Bot Developed for Alpha Bot App by 24 Smart Tech Company", MessageTypeEnum.TEXT, "2", "chatBot", new Date().getTime(), false));
        chatMessages.add(new ChatMessage("How can i help you ?", MessageTypeEnum.VOICE, "2", "chatBot", new Date().getTime(), false));
        chatMessages.add(new ChatMessage("hello ChatBot ", MessageTypeEnum.VOICE, "1", "sayed", new Date().getTime(), true));
        chatMessages.add(new ChatMessage("Hello Developer\nHow can i help you ?", MessageTypeEnum.TEXT, "2", "chatBot", new Date().getTime(), false));
        chatMessages.add(new ChatMessage("hello ChatBot ", MessageTypeEnum.TEXT, "1", "sayed", new Date().getTime(), true));
        chatMessages.add(new ChatMessage("Hello Developer\nHow can i help you ?", MessageTypeEnum.VOICE, "2", "chatBot", new Date().getTime(), false));
        chatMessages.add(new ChatMessage("hello ChatBot ", MessageTypeEnum.TEXT, "1", "sayed", new Date().getTime(), true));
        chatMessages.add(new ChatMessage("Hello Developer\nHow can i help you ?", MessageTypeEnum.TEXT, "2", "chatBot", new Date().getTime(), false));
        chatMessages.add(new ChatMessage("hello ChatBot ", MessageTypeEnum.TEXT, "1", "sayed", new Date().getTime(), true));
        chatMessages.add(new ChatMessage("Hello Developer\nHow can i help you ?", MessageTypeEnum.TEXT, "2", "chatBot", new Date().getTime(), false));
        chatMessages.add(new ChatMessage("hello ChatBot ", MessageTypeEnum.TEXT, "1", "sayed", new Date().getTime(), true));
        chatMessages.add(new ChatMessage("Hello Developer\nHow can i help you ?", MessageTypeEnum.TEXT, "2", "chatBot", new Date().getTime(), false));
        chatMessages.add(new ChatMessage("hello ChatBot ", MessageTypeEnum.TEXT, "1", "sayed", new Date().getTime(), true));
        chatMessages.add(new ChatMessage("Hello Developer\nHow can i help you ?", MessageTypeEnum.VOICE, "2", "chatBot", new Date().getTime(), false));
        chatMessages.add(new ChatMessage("hello ChatBot ", MessageTypeEnum.TEXT, "1", "sayed", new Date().getTime(), true));
        chatMessages.add(new ChatMessage("Hello Developer\nHow can i help you ?", MessageTypeEnum.TEXT, "2", "chatBot", new Date().getTime(), false));
        chatMessages.add(new ChatMessage("hello ChatBot ", MessageTypeEnum.TEXT, "1", "sayed", new Date().getTime(), true));
        chatMessages.add(new ChatMessage("Hello Developer\nHow can i help you ?", MessageTypeEnum.TEXT, "2", "chatBot", new Date().getTime(), false));
        chatMessages.add(new ChatMessage("hello ChatBot ", MessageTypeEnum.TEXT, "1", "sayed", new Date().getTime(), true));
        chatMessages.add(new ChatMessage("Hello Developer\nHow can i help you ?", MessageTypeEnum.TEXT, "2", "chatBot", new Date().getTime(), false));
        chatMessages.add(new ChatMessage("hello ChatBot ", MessageTypeEnum.TEXT, "1", "sayed", new Date().getTime(), true));
        chatMessages.add(new ChatMessage("Hello Developer\nHow can i help you ?", MessageTypeEnum.TEXT, "2", "chatBot", new Date().getTime(), false));
        chatMessages.add(new ChatMessage("hello ChatBot ", MessageTypeEnum.TEXT, "1", "sayed", new Date().getTime(), true));
        chatMessages.add(new ChatMessage("Hello Developer\nHow can i help you ?", MessageTypeEnum.TEXT, "2", "chatBot", new Date().getTime(), false));
        chatMessages.add(new ChatMessage("hello ChatBot ", MessageTypeEnum.TEXT, "1", "sayed", new Date().getTime(), true));
        chatMessages.add(new ChatMessage("Hello Developer\nHow can i help you ?", MessageTypeEnum.TEXT, "2", "chatBot", new Date().getTime(), false));
        chatMessages.add(new ChatMessage("hello ChatBot ", MessageTypeEnum.TEXT, "1", "sayed", new Date().getTime(), true));
        chatMessages.add(new ChatMessage("Hello Developer\nHow can i help you ?", MessageTypeEnum.TEXT, "2", "chatBot", new Date().getTime(), false));
        chatMessages.add(new ChatMessage("hello ChatBot ", MessageTypeEnum.TEXT, "1", "sayed", new Date().getTime(), true));
        chatMessages.add(new ChatMessage("Hello Developer\nHow can i help you ?", MessageTypeEnum.TEXT, "2", "chatBot", new Date().getTime(), false));
        chatMessages.add(new ChatMessage("hello ChatBot ", MessageTypeEnum.TEXT, "1", "sayed", new Date().getTime(), true));
        return chatMessages;
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
    }

}