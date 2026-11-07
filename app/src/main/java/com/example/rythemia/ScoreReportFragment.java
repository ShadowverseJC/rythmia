package com.example.rythemia;

import android.content.Context;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.rythemia.data.AppDatabase;
import com.example.rythemia.data.Attempt;
import com.example.rythemia.data.Background;
import com.example.rythemia.data.Session;
import com.example.rythemia.databinding.FragmentScoreReportBinding;
import com.example.rythemia.practice.RunResult;
import com.example.rythemia.practice.Tempo;
import com.google.android.material.snackbar.Snackbar;

/**
 * Reports a finished run and records it as an attempt. Only the tempo comes from the run so
 * far: the grade, accuracy and note breakdown still show the sample run that
 * {@link RunResult#sample} reports, until playback can produce real results.
 */
public class ScoreReportFragment extends Fragment {

    private FragmentScoreReportBinding binding;
    private RunResult run;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        run = RunResult.fromBundle(requireArguments());
        // Saved state means this report was already shown (and the run recorded) before a
        // rotation or process restart
        if (savedInstanceState == null) {
            record(run);
        }
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentScoreReportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.tempoChip.setText(getString(R.string.bpm_format, run.bpm));
        binding.tempoValue.setText(String.valueOf(run.bpm));
        binding.insight.setText(insightText());

        NavController nav = NavHostFragment.findNavController(this);
        binding.appBar.backLabel.setText(R.string.lesson_sixteenth);
        binding.appBar.back.setOnClickListener(v -> nav.popBackStack());
        binding.appBar.avatar.setOnClickListener(v -> nav.navigate(R.id.action_score_to_profile));
        binding.homeButton.setOnClickListener(v -> nav.popBackStack(R.id.dashboardFragment, false));
        binding.retryButton.setOnClickListener(v -> nav.popBackStack());
    }

    private void record(RunResult run) {
        Context app = requireContext().getApplicationContext();
        long userId = new Session(app).userId();
        if (userId == Session.NO_USER) {
            return;
        }
        Attempt attempt = new Attempt();
        attempt.userId = userId;
        attempt.lessonId = run.lessonId;
        attempt.bpm = run.bpm;
        attempt.hits = run.hits;
        attempt.misses = run.misses;
        attempt.timestamp = System.currentTimeMillis();
        Background.run(() -> AppDatabase.get(app).attempts().insert(attempt), (id, error) -> {
            if (error != null && binding != null) {
                Snackbar.make(binding.getRoot(), R.string.error_save_run, Snackbar.LENGTH_LONG)
                        .show();
            }
        });
    }

    /**
     * Suggests practicing a little slower than this run, and highlights the missed counts in
     * the primary text color.
     */
    private CharSequence insightText() {
        SpannableStringBuilder text = new SpannableStringBuilder(HtmlCompat.fromHtml(
                getString(R.string.insight, Tempo.clamp(run.bpm - 10), run.bpm),
                HtmlCompat.FROM_HTML_MODE_LEGACY));
        int primary = ContextCompat.getColor(requireContext(), R.color.text_primary);
        for (StyleSpan bold : text.getSpans(0, text.length(), StyleSpan.class)) {
            text.setSpan(new ForegroundColorSpan(primary), text.getSpanStart(bold),
                    text.getSpanEnd(bold), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return text;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
