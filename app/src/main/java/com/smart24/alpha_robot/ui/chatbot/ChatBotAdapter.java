package com.smart24.alpha_robot.ui.chatbot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.smart24.alpha_robot.R;
import com.smart24.alpha_robot.data.ChatMessage;
import com.smart24.alpha_robot.data.ChatVoiceMessage;
import com.smart24.alpha_robot.data.MessageTypeEnum;
import com.smart24.alpha_robot.utils.AudioRecorder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lombok.Getter;


public class ChatBotAdapter extends RecyclerView.Adapter<ChatBotAdapter.AdapterViewHolder> {


    private static final int VIEW_TYPE_SENT_TEXT = 1;
    private static final int VIEW_TYPE_RECEIVED_TEXT = 2;

    private static final int VIEW_TYPE_SENT_VOICE = 3;
    private static final int VIEW_TYPE_RECEIVED_VOICE = 4;

    @Getter
    private List<ChatMessage> dataList = new ArrayList<>();
    private Fragment fragment;

    AudioRecorder audioRecorder;

    public ChatBotAdapter(Fragment fragment) {
        this.fragment = fragment;
        this.audioRecorder = new AudioRecorder(fragment.requireActivity());
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage chatMessage = dataList.get(position);
        if (MessageTypeEnum.TEXT.equals(chatMessage.getMessageType())) {
            return chatMessage.isSentByUser() ? VIEW_TYPE_SENT_TEXT : VIEW_TYPE_RECEIVED_TEXT;
        } else if (MessageTypeEnum.VOICE.equals(chatMessage.getMessageType())) {
            return chatMessage.isSentByUser() ? VIEW_TYPE_SENT_VOICE : VIEW_TYPE_RECEIVED_VOICE;
        }
        throw new IllegalArgumentException("invalid message type and sender identifier");
    }

    @NonNull
    @Override
    public AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        switch (viewType) {
            case VIEW_TYPE_SENT_TEXT: {
                return new AdapterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_chat_sent_text, parent, false));
            }
            case VIEW_TYPE_SENT_VOICE: {
                return new AdapterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_chat_sent_voice, parent, false));
            }
            case VIEW_TYPE_RECEIVED_TEXT: {
                return new AdapterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_chat_received_text, parent, false));
            }
            case VIEW_TYPE_RECEIVED_VOICE: {
                return new AdapterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_chat_received_voice, parent, false));
            }

        }
        throw new IllegalArgumentException("not supported view type");
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterViewHolder holder, int position) {
        int currentItemViewType = getItemViewType(position);
        if (currentItemViewType == VIEW_TYPE_RECEIVED_TEXT || currentItemViewType == VIEW_TYPE_SENT_TEXT) {
            holder.textMessageTv.setText(dataList.get(position).getText());
        } else {
            // TODO handle this seek bar and duration
            if (dataList.get(position) instanceof ChatVoiceMessage) {
                holder.itemView.setOnClickListener(view -> {
                    ChatVoiceMessage chatVoiceMessage = (ChatVoiceMessage) dataList.get(position);
                    audioRecorder.startPlayAudio(fragment.requireActivity(), chatVoiceMessage.getVoiceFile().getAbsolutePath());
                    holder.voiceSeekBar.setProgress(0);
                    holder.durationTv.setText(String.format("00:10"));
                });

            }

        }

        holder.dateTv.setText(holder.sdf.format(dataList.get(position).getTimeStamp()));
    }


    @Override
    public int getItemCount() {
        if (dataList == null) {
            return 0;
        }
        return dataList.size();
    }

    public void setDataList(List<ChatMessage> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }


    public static class AdapterViewHolder extends RecyclerView.ViewHolder {
        TextView textMessageTv, dateTv, durationTv;
        AppCompatSeekBar voiceSeekBar;
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        public AdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessageTv = itemView.findViewById(R.id.text_message);
            dateTv = itemView.findViewById(R.id.date);
            voiceSeekBar = itemView.findViewById(R.id.voice_seekbar);
            durationTv = itemView.findViewById(R.id.duration_tv);
        }
    }
}
