package com.smart24.branch_bots.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.smart24.branch_bots.R;
import com.smart24.branch_bots.data.Question;
import com.smart24.branch_bots.databinding.FragmentHomeBinding;
import com.smart24.branch_bots.network.GroqApi;
import com.smart24.branch_bots.network.GroqRetrofitClient;
import com.smart24.branch_bots.network.Smart24RetrofitClient;
import com.smart24.branch_bots.network.SmartTechApi;
import com.smart24.branch_bots.utils.AudioRecorder;

import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private QuestionsAdapter questionsAdapter;
    private AudioRecorder audioRecorder;
    private List<Question> predifinedQuestionsList = new ArrayList<>();
    private GroqApi groqApi;
    private SmartTechApi smartTechApi;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        questionsAdapter = new QuestionsAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        binding.predefinedQuestionsRv.setLayoutManager(layoutManager);
        binding.predefinedQuestionsRv.setAdapter(questionsAdapter);
        audioRecorder = new AudioRecorder(requireActivity());
        groqApi = GroqRetrofitClient.getRetrofitInstance().create(GroqApi.class);
        smartTechApi = Smart24RetrofitClient.getRetrofitInstance().create(SmartTechApi.class);
        predifinedQuestionsList = getPredifinedQuestionsList();
        if (!predifinedQuestionsList.isEmpty()) {
            updateQuestionAdapterList(predifinedQuestionsList);
        }

        playFaceGif();

        return binding.getRoot();
    }

    private void playFaceGif() {
        binding.faceGif.setImageResource(R.drawable.face_2_1);
        final MediaController mc = new MediaController(requireActivity());
        mc.setMediaPlayer((GifDrawable) binding.faceGif.getDrawable());
        mc.show();
    }

    private List<Question> getPredifinedQuestionsList() {
        return List.of(new Question("Introduce question", "introduce Branch Bots"),
                new Question("Introduce question", "introduce yourself"),
                new Question("How Are you?", "how are you today ?"),
                new Question("Speak arabic", "speak arabic"));
    }

    private void updateQuestionAdapterList(List<Question> questions) {
        this.questionsAdapter.setDataList(questions);
        this.questionsAdapter.notifyItemInserted(questions.size() - 1);

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