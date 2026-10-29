package com.example.rythemia;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.rythemia.databinding.FragmentDashboardBinding;
import com.example.rythemia.databinding.ViewLessonRowBinding;
import com.example.rythemia.databinding.ViewTrackHeaderBinding;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.greeting.setText(getString(R.string.dashboard_greeting,
                getString(R.string.placeholder_username)));

        bindTrack(binding.trackBeginner, "1", R.string.track_beginner,
                R.string.track_beginner_meta, R.drawable.ic_chevron_down);
        bindTrack(binding.trackIntermediate, "2", R.string.track_intermediate,
                R.string.track_locked_meta, R.drawable.ic_chevron_down);
        bindTrack(binding.trackAdvanced, "3", R.string.track_advanced,
                R.string.track_locked_meta, R.drawable.ic_chevron_down);

        bindLesson(binding.lessonQuarter, R.string.lesson_quarter, R.drawable.ic_star_gold,
                R.string.tier_gold, R.string.technique_downstrokes, R.string.lesson_quarter_summary);
        bindLesson(binding.lessonEighth, R.string.lesson_eighth, R.drawable.ic_star_silver,
                R.string.tier_silver, R.string.technique_alternate_picking,
                R.string.lesson_eighth_summary);
        bindLesson(binding.lessonSixteenth, R.string.lesson_sixteenth, R.drawable.ic_star_bronze,
                R.string.tier_bronze, R.string.technique_alternate_picking,
                R.string.lesson_sixteenth_summary);

        // Beginner is the active track, so it starts open.
        setTrackExpanded(binding.trackBeginner, binding.beginnerLessons, true);
        binding.trackBeginner.getRoot().setOnClickListener(v ->
                setTrackExpanded(binding.trackBeginner, binding.beginnerLessons,
                        binding.beginnerLessons.getVisibility() != View.VISIBLE));

        // Sixteenth Notes is the current lesson, so it starts open.
        setExpanded(binding.lessonSixteenth, true);

        NavController nav = NavHostFragment.findNavController(this);
        // Every lesson opens the Sixteenth Notes exercise until lesson content exists.
        View.OnClickListener openExercise = v -> nav.navigate(R.id.action_dashboard_to_exercise);
        binding.lessonQuarter.playButton.setOnClickListener(openExercise);
        binding.lessonEighth.playButton.setOnClickListener(openExercise);
        binding.lessonSixteenth.playButton.setOnClickListener(openExercise);
        binding.appBar.avatar.setOnClickListener(v -> nav.navigate(R.id.action_dashboard_to_profile));
    }

    private static void bindTrack(ViewTrackHeaderBinding track, String level, @StringRes int name,
                                  @StringRes int meta, @DrawableRes int chevron) {
        track.level.setText(level);
        track.name.setText(name);
        track.meta.setText(meta);
        track.chevron.setImageResource(chevron);
    }

    private static void setTrackExpanded(ViewTrackHeaderBinding track, View lessons,
                                         boolean expanded) {
        lessons.setVisibility(expanded ? View.VISIBLE : View.GONE);
        track.chevron.animate().rotation(expanded ? 180f : 0f).setDuration(150);
    }

    private static void bindLesson(ViewLessonRowBinding lesson, @StringRes int name,
                                   @DrawableRes int star, @StringRes int tier,
                                   @StringRes int technique, @StringRes int summary) {
        lesson.name.setText(name);
        lesson.tierStar.setImageResource(star);
        lesson.tierLabel.setText(tier);
        lesson.technique.setText(technique);
        lesson.summary.setText(summary);
        lesson.header.setOnClickListener(v ->
                setExpanded(lesson, lesson.details.getVisibility() != View.VISIBLE));
    }

    private static void setExpanded(ViewLessonRowBinding lesson, boolean expanded) {
        lesson.details.setVisibility(expanded ? View.VISIBLE : View.GONE);
        lesson.chevron.animate().rotation(expanded ? 180f : 0f).setDuration(150);
        lesson.getRoot().setBackgroundResource(
                expanded ? R.drawable.bg_raised_row_active : R.drawable.bg_raised_row);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
