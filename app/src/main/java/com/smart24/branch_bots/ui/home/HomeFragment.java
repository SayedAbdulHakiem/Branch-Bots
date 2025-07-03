package com.smart24.branch_bots.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.smart24.branch_bots.R;
import com.smart24.branch_bots.databinding.FragmentHomeBinding;

import pl.droidsonroids.gif.GifDrawable;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);

        playFaceGif();

        return binding.getRoot();
    }

    private void playFaceGif() {
        binding.faceGif.setImageResource(R.drawable.face_2_1);
        final MediaController mc = new MediaController(requireActivity());
        mc.setMediaPlayer((GifDrawable) binding.faceGif.getDrawable());
        mc.show();
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