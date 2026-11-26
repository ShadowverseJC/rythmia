package com.example.rythemia;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.rythemia.data.AppDatabase;
import com.example.rythemia.data.Background;
import com.example.rythemia.data.Session;
import com.example.rythemia.databinding.FragmentExerciseIntroBinding;
import com.example.rythemia.practice.Lessons;
import com.example.rythemia.practice.Progress;
import com.example.rythemia.practice.RunResult;
import com.example.rythemia.practice.Tempo;

public class ExerciseIntroFragment extends Fragment {

    /** Only Sixteenth Notes has an exercise so far; every lesson's Play opens it. */
    private static final String LESSON = Lessons.SIXTEENTH;

    private FragmentExerciseIntroBinding binding;
    private Session session;
    private int bpm;

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
        session = new Session(requireContext());
        long userId = session.userId();

        // Tempo starts where this user left it, and is saved as it changes
        bpm = session.lastBpm(userId);
        binding.bpmValue.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        binding.slowerButton.setOnClickListener(v -> changeTempo(-Tempo.STEP));
        binding.fasterButton.setOnClickListener(v -> changeTempo(Tempo.STEP));
        showTempo();

        AppDatabase db = AppDatabase.get(requireContext());
        Background.run(() -> new Progress(db.attempts().forUser(userId)), (progress, error) -> {
            if (binding != null && progress != null) {
                showBest(progress);
            }
        });

        NavController nav = NavHostFragment.findNavController(this);
        binding.appBar.backLabel.setText(R.string.track_beginner);
        binding.appBar.back.setOnClickListener(v -> nav.popBackStack());
        binding.appBar.avatar.setOnClickListener(v -> nav.navigate(R.id.action_exercise_to_profile));
        binding.homeButton.setOnClickListener(v -> nav.popBackStack(R.id.dashboardFragment, false));
        // No playback yet: Play reports a sample run at the chosen tempo
        binding.playButton.setOnClickListener(v -> nav.navigate(R.id.action_exercise_to_score,
                RunResult.sample(LESSON, bpm).toBundle()));
    }

    private void changeTempo(int delta) {
        bpm = Tempo.clamp(bpm + delta);
        session.setLastBpm(session.userId(), bpm);
        showTempo();
    }

    private void showTempo() {
        binding.bpmValue.setText(String.valueOf(bpm));
        binding.tempoMarking.setText(Tempo.marking(bpm));
        setStepEnabled(binding.slowerButton, bpm > Tempo.MIN);
        setStepEnabled(binding.fasterButton, bpm < Tempo.MAX);
    }

    private static void setStepEnabled(ImageView button, boolean enabled) {
        button.setEnabled(enabled);
        button.setAlpha(enabled ? 1f : 0.4f);
    }

    private void showBest(Progress progress) {
        Progress.Tier best = progress.best(LESSON);
        binding.bestStar.setVisibility(best.icon != 0 ? View.VISIBLE : View.GONE);
        if (best.icon != 0) {
            binding.bestStar.setImageResource(best.icon);
        }
        binding.bestTier.setText(best.label);
        int topSpeed = progress.topSpeed(LESSON);
        binding.topSpeed.setText(getString(R.string.top_speed_label, topSpeed > 0
                ? getString(R.string.bpm_format, topSpeed) : getString(R.string.no_value)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
