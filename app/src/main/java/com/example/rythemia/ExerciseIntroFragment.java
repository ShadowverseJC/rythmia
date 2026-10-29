package com.example.rythemia;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.rythemia.databinding.FragmentExerciseIntroBinding;

public class ExerciseIntroFragment extends Fragment {

    private FragmentExerciseIntroBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentExerciseIntroBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController nav = NavHostFragment.findNavController(this);
        binding.appBar.backLabel.setText(R.string.track_beginner);
        binding.appBar.back.setOnClickListener(v -> nav.popBackStack());
        binding.appBar.avatar.setOnClickListener(v -> nav.navigate(R.id.action_exercise_to_profile));
        binding.homeButton.setOnClickListener(v -> nav.popBackStack(R.id.dashboardFragment, false));
        // No playback yet: Play jumps straight to a sample score report.
        binding.playButton.setOnClickListener(v -> nav.navigate(R.id.action_exercise_to_score));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
