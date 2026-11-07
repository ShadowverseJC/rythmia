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
import com.example.rythemia.databinding.ViewLessonCardBinding;
import com.example.rythemia.databinding.ViewLessonDetailsBinding;
import com.example.rythemia.databinding.ViewLessonRowBinding;
import com.example.rythemia.databinding.ViewTrackHeaderBinding;

import java.util.Objects;

/**
 * Portrait shows the tracks as expandable cards with each lesson opening in place. Landscape
 * lists tracks and lessons side by side and shows the selected lesson in a card beside them,
 * so its rows have no chevrons or inline details (those binding fields are null there).
 */
public class DashboardFragment extends Fragment {

    /** A lesson in the Beginner Rhythm track and the row that lists it. */
    private static final class Lesson {
        final ViewLessonRowBinding row;
        @StringRes final int name;
        @DrawableRes final int star;
        @StringRes final int tier;
        @StringRes final int technique;
        @StringRes final int summary;

        Lesson(ViewLessonRowBinding row, @StringRes int name, @DrawableRes int star,
               @StringRes int tier, @StringRes int technique, @StringRes int summary) {
            this.row = row;
            this.name = name;
            this.star = star;
            this.tier = tier;
            this.technique = technique;
            this.summary = summary;
        }
    }

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
                R.string.track_beginner_meta);
        bindTrack(binding.trackIntermediate, "2", R.string.track_intermediate,
                R.string.track_locked_meta);
        bindTrack(binding.trackAdvanced, "3", R.string.track_advanced,
                R.string.track_locked_meta);

        Lesson[] lessons = {
                new Lesson(binding.lessonQuarter, R.string.lesson_quarter, R.drawable.ic_star_gold,
                        R.string.tier_gold, R.string.technique_downstrokes,
                        R.string.lesson_quarter_summary),
                new Lesson(binding.lessonEighth, R.string.lesson_eighth, R.drawable.ic_star_silver,
                        R.string.tier_silver, R.string.technique_alternate_picking,
                        R.string.lesson_eighth_summary),
                new Lesson(binding.lessonSixteenth, R.string.lesson_sixteenth,
                        R.drawable.ic_star_bronze, R.string.tier_bronze,
                        R.string.technique_alternate_picking, R.string.lesson_sixteenth_summary),
        };
        // Sixteenth Notes is the current lesson.
        Lesson current = lessons[2];
        for (Lesson lesson : lessons) {
            lesson.row.name.setText(lesson.name);
            lesson.row.tierStar.setImageResource(lesson.star);
            lesson.row.tierLabel.setText(lesson.tier);
        }

        NavController nav = NavHostFragment.findNavController(this);
        // Every lesson opens the Sixteenth Notes exercise until lesson content exists.
        View.OnClickListener openExercise = v -> nav.navigate(R.id.action_dashboard_to_exercise);
        binding.appBar.avatar.setOnClickListener(v -> nav.navigate(R.id.action_dashboard_to_profile));

        ViewLessonCardBinding card = binding.lessonCard;
        if (card != null) {
            // Landscape: Beginner is the active track; tapping a lesson shows it in the card.
            binding.trackBeginner.getRoot().setSelected(true);
            for (Lesson lesson : lessons) {
                lesson.row.header.setOnClickListener(v -> showInCard(card, lessons, lesson));
            }
            card.details.playButton.setOnClickListener(openExercise);
            showInCard(card, lessons, current);
        } else {
            // Portrait: Beginner is the active track, so it and the current lesson start open.
            ViewTrackHeaderBinding beginner = binding.trackBeginner;
            setTrackExpanded(beginner, binding.beginnerLessons, true);
            beginner.getRoot().setOnClickListener(v ->
                    setTrackExpanded(beginner, binding.beginnerLessons,
                            binding.beginnerLessons.getVisibility() != View.VISIBLE));
            for (Lesson lesson : lessons) {
                ViewLessonDetailsBinding details = Objects.requireNonNull(lesson.row.details);
                bindDetails(details, lesson);
                details.playButton.setOnClickListener(openExercise);
                lesson.row.header.setOnClickListener(v -> setExpanded(lesson.row,
                        details.getRoot().getVisibility() != View.VISIBLE));
            }
            setExpanded(current.row, true);
        }
    }

    private static void bindTrack(ViewTrackHeaderBinding track, String level, @StringRes int name,
                                  @StringRes int meta) {
        track.level.setText(level);
        track.name.setText(name);
        track.meta.setText(meta);
    }

    private static void bindDetails(ViewLessonDetailsBinding details, Lesson lesson) {
        details.technique.setText(lesson.technique);
        details.summary.setText(lesson.summary);
    }

    /** Marks {@code shown} as the selected row and fills the card with it. */
    private static void showInCard(ViewLessonCardBinding card, Lesson[] lessons, Lesson shown) {
        for (Lesson lesson : lessons) {
            lesson.row.getRoot().setSelected(lesson == shown);
        }
        card.title.setText(shown.name);
        bindDetails(card.details, shown);
    }

    private static void setTrackExpanded(ViewTrackHeaderBinding track, View lessons,
                                         boolean expanded) {
        lessons.setVisibility(expanded ? View.VISIBLE : View.GONE);
        Objects.requireNonNull(track.chevron).animate().rotation(expanded ? 180f : 0f)
                .setDuration(150);
    }

    private static void setExpanded(ViewLessonRowBinding lesson, boolean expanded) {
        Objects.requireNonNull(lesson.details).getRoot()
                .setVisibility(expanded ? View.VISIBLE : View.GONE);
        Objects.requireNonNull(lesson.chevron).animate().rotation(expanded ? 180f : 0f)
                .setDuration(150);
        lesson.getRoot().setBackgroundResource(
                expanded ? R.drawable.bg_raised_row_active : R.drawable.bg_raised_row);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
