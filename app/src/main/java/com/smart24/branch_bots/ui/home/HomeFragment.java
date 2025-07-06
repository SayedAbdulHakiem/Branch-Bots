package com.smart24.branch_bots.ui.home;

import android.os.Bundle;
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
import com.smart24.branch_bots.data.Question;
import com.smart24.branch_bots.databinding.FragmentHomeBinding;
import com.smart24.branch_bots.shared.viewmodel.ChatBotServiceViewModel;
import com.smart24.branch_bots.shared.viewmodel.MoveViewModel;
import com.smart24.branch_bots.shared.viewmodel.MyAudioMultiMediaViewModel;
import com.smart24.branch_bots.utils.SharedUtils;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import pl.droidsonroids.gif.GifDrawable;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private QuestionsAdapter questionsAdapter;
    private List<Question> predifinedQuestionsList = new ArrayList<>();
    @Getter
    private ChatBotServiceViewModel chatBotServiceViewModel;
    @Getter
    private MyAudioMultiMediaViewModel myAudioMultiMediaViewModel;
    @Getter
    MoveViewModel moveViewModel;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);


        myAudioMultiMediaViewModel = new ViewModelProvider(requireActivity()).get(MyAudioMultiMediaViewModel.class);
        myAudioMultiMediaViewModel.setRequiredDependencies(requireActivity());
        chatBotServiceViewModel = new ViewModelProvider(requireActivity()).get(ChatBotServiceViewModel.class);
        chatBotServiceViewModel.setRequiredDependencies(requireActivity());
        moveViewModel = new ViewModelProvider(requireActivity()).get(MoveViewModel.class);
        questionsAdapter = new QuestionsAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        binding.predefinedQuestionsRv.setLayoutManager(layoutManager);
        binding.predefinedQuestionsRv.setAdapter(questionsAdapter);

        predifinedQuestionsList = getPredifinedQuestionsList();
        if (!predifinedQuestionsList.isEmpty()) {
            updateQuestionAdapterList(predifinedQuestionsList);
        }

        playFaceGif();
        setOnClickListeners();
        setViewModelObservers();

        return binding.getRoot();
    }

    private void setOnClickListeners() {
        binding.micBtn.setOnClickListener(view -> {
            if (chatBotServiceViewModel.isRecording()) {
                chatBotServiceViewModel.stopRecordAndStartTranscript();
                binding.micBtn.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.mic_ic));
            } else {
                chatBotServiceViewModel.startRecording(requireActivity());
                binding.micBtn.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.mic_filled_ic));
            }
        });

        //        TODO the following line just for example of sdk usage
        binding.moveForwardBtn.setOnClickListener(view -> {
            moveViewModel.moveForward();
            SharedUtils.showMessageInfo(requireActivity(), getString(R.string.move_forward_called));
        });
        binding.moveBackBtn.setOnClickListener(view -> {
            moveViewModel.moveBack();
            SharedUtils.showMessageInfo(requireActivity(), getString(R.string.move_back_called));
        });
        binding.moveRightBtn.setOnClickListener(view -> {
            moveViewModel.moveRight();
            SharedUtils.showMessageInfo(requireActivity(), getString(R.string.move_right_called));
        });
        binding.moveLeftBtn.setOnClickListener(view -> {
            moveViewModel.moveLeft();
            SharedUtils.showMessageInfo(requireActivity(), getString(R.string.move_left_called));
        });



    }

    private void setViewModelObservers() {
        myAudioMultiMediaViewModel.getAudioPlayingMld().observe(requireActivity(), aBoolean -> {
            if (aBoolean) {
                binding.micBtn.setEnabled(false);
                binding.micBtn.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.mic_filled_ic));
                binding.voiceFrequencyIc.setVisibility(View.VISIBLE);
            } else {
                binding.micBtn.setEnabled(true);
                binding.micBtn.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.mic_ic));
                binding.voiceFrequencyIc.setVisibility(View.GONE);
                binding.selectedQuestionTv.setVisibility(View.GONE);
            }
        });

        myAudioMultiMediaViewModel.getRecordingMld().observe(requireActivity(), aBoolean -> {
            if (aBoolean) {
                binding.selectedQuestionTv.setVisibility(View.GONE);
            } else {

            }
        });
    }
    private void playFaceGif() {
        binding.faceGif.setImageResource(R.drawable.face_2_1);
        final MediaController mc = new MediaController(requireActivity());
        mc.setMediaPlayer((GifDrawable) binding.faceGif.getDrawable());
        mc.show();
    }

    private List<Question> getPredifinedQuestionsList() {
        return List.of(new Question("مقدمة", "ما هو برانش بوت؟"),
                new Question("مقدمة", "كيف حالك؟"),
                new Question("مقدمة", "كيف يمكمك مساعدتي؟"),
                new Question("مقدمة", "ما هي درجة الحرارة اليوم في الرياض؟"));
    }


    private void updateQuestionAdapterList(List<Question> questions) {
        this.questionsAdapter.setDataList(questions);
        this.questionsAdapter.notifyItemInserted(questions.size() - 1);

    }

    public void onSelectedQuestion(Question question) {
        binding.selectedQuestionTv.setVisibility(View.VISIBLE);
        binding.selectedQuestionTv.setText(question.getText());
        chatBotServiceViewModel.sendQuestionToChatBot(question.getText());
    }
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}