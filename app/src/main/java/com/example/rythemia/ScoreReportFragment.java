package com.example.rythemia;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.rythemia.databinding.FragmentScoreReportBinding;

public class ScoreReportFragment extends Fragment {

    private FragmentScoreReportBinding binding;

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

        binding.insight.setText(insightText());

        NavController nav = NavHostFragment.findNavController(this);
        binding.appBar.backLabel.setText(R.string.lesson_sixteenth);
        binding.appBar.back.setOnClickListener(v -> nav.popBackStack());
        binding.appBar.avatar.setOnClickListener(v -> nav.navigate(R.id.action_score_to_profile));
        binding.homeButton.setOnClickListener(v -> nav.popBackStack(R.id.dashboardFragment, false));
        binding.retryButton.setOnClickListener(v -> nav.popBackStack());
    }

    /** The insight copy highlights the missed counts in the primary text color. */
    private CharSequence insightText() {
        SpannableStringBuilder text = new SpannableStringBuilder(HtmlCompat.fromHtml(
                getString(R.string.insight), HtmlCompat.FROM_HTML_MODE_LEGACY));
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
