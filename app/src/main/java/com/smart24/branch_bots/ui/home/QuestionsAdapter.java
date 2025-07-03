package com.smart24.branch_bots.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.smart24.branch_bots.R;
import com.smart24.branch_bots.data.Question;
import com.smart24.branch_bots.utils.AudioRecorder;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;


public class QuestionsAdapter extends RecyclerView.Adapter<QuestionsAdapter.AdapterViewHolder> {


    @Getter
    private List<Question> dataList = new ArrayList<>();
    private Fragment fragment;

    AudioRecorder audioRecorder;

    public QuestionsAdapter(Fragment fragment) {
        this.fragment = fragment;
        this.audioRecorder = new AudioRecorder(fragment.requireActivity());
    }


    @NonNull
    @Override
    public AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AdapterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_predefined_question, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterViewHolder holder, int position) {
        Question question = dataList.get(position);
        holder.questionTitleTv.setText(question.getTitle());
        holder.questionTextTv.setText(String.format("\"%s\"", question.getText()));
    }


    @Override
    public int getItemCount() {
        if (dataList == null) {
            return 0;
        }
        return dataList.size();
    }

    public void setDataList(List<Question> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }


    public static class AdapterViewHolder extends RecyclerView.ViewHolder {
        TextView questionTitleTv, questionTextTv;
        AppCompatImageView questionIconIv;

        public AdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            questionTitleTv = itemView.findViewById(R.id.question_title_tv);
            questionTextTv = itemView.findViewById(R.id.question_text_tv);
            questionIconIv = itemView.findViewById(R.id.question_icon);

        }
    }
}
