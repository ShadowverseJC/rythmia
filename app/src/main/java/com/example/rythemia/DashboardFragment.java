package com.example.rythemia;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.rythemia.data.AppDatabase;
import com.example.rythemia.data.Background;
import com.example.rythemia.data.Session;
import com.example.rythemia.databinding.FragmentDashboardBinding;
import com.example.rythemia.databinding.ViewLessonCardBinding;
import com.example.rythemia.databinding.ViewLessonDetailsBinding;
import com.example.rythemia.databinding.ViewLessonRowBinding;
import com.example.rythemia.databinding.ViewTrackHeaderBinding;
import com.example.rythemia.practice.Lessons;
import com.example.rythemia.practice.Progress;
import com.example.rythemia.practice.UserProgress;
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

/**
 * Portrait shows the tracks as expandable cards with each lesson opening in place. Landscape
 * lists tracks and lessons side by side and shows the selected lesson in a card beside them,
 * so its rows have no chevrons or inline details (those binding fields are null there).
 * Greeting, stars, rank and each lesson's best star come from the logged-in user's attempts.
 */
public class DashboardFragment extends Fragment {

    /** A lesson in the Beginner Rhythm track and the row that lists it. */
    private static final class Lesson {
        final ViewLessonRowBinding row;
        final Lessons.Lesson info;

        Lesson(ViewLessonRowBinding row, Lessons.Lesson info) {
            this.row = row;
            this.info = info;
        }
    }

    private static final int STARS_PER_TRACK =
            Lessons.LESSONS_PER_TRACK * Progress.STARS_PER_LESSON;

    private FragmentDashboardBinding binding;
    private Lesson[] lessons;

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

        binding.starsTotal.setText(getString(R.string.stars_total, Progress.TOTAL_STARS));
        bindTrack(binding.trackBeginner, "1", R.string.track_beginner);
        bindTrack(binding.trackIntermediate, "2", R.string.track_intermediate);
        bindTrack(binding.trackAdvanced, "3", R.string.track_advanced);
        // Only Beginner has lessons so far, so the other tracks have nothing to earn yet
        binding.trackIntermediate.meta.setText(trackMeta(0));
        binding.trackAdvanced.meta.setText(trackMeta(0));

        ViewLessonRowBinding[] rows =
                {binding.lessonQuarter, binding.lessonEighth, binding.lessonSixteenth};
        lessons = new Lesson[rows.length];
        for (int i = 0; i < rows.length; i++) {
            lessons[i] = new Lesson(rows[i], Lessons.BEGINNER.get(i));
            rows[i].name.setText(lessons[i].info.name);
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
                lesson.row.header.setOnClickListener(v -> showInCard(card, lesson));
            }
            card.details.playButton.setOnClickListener(openExercise);
        } else {
            // Portrait: Beginner is the active track, so it starts open.
            ViewTrackHeaderBinding beginner = binding.trackBeginner;
            setTrackExpanded(beginner, binding.beginnerLessons, true);
            beginner.getRoot().setOnClickListener(v ->
                    setTrackExpanded(beginner, binding.beginnerLessons,
                            binding.beginnerLessons.getVisibility() != View.VISIBLE));
            for (Lesson lesson : lessons) {
                ViewLessonDetailsBinding details = Objects.requireNonNull(lesson.row.details);
                bindDetails(details, lesson.info);
                details.playButton.setOnClickListener(openExercise);
                lesson.row.header.setOnClickListener(v -> setExpanded(lesson.row,
                        details.getRoot().getVisibility() != View.VISIBLE));
            }
        }

        Session session = new Session(requireContext());
        long userId = session.userId();
        AppDatabase db = AppDatabase.get(requireContext());
        Background.run(() -> UserProgress.load(db, userId), (data, error) -> {
            if (binding == null) {
                return;
            }
            if (error != null) {
                Snackbar.make(binding.getRoot(), R.string.error_load_progress,
                        Snackbar.LENGTH_LONG).show();
            } else if (data == null) {
                // The saved login no longer matches an account
                session.logOut();
                nav.navigate(R.id.action_dashboard_to_login);
            } else {
                showProgress(data);
            }
        });
    }

    private void showProgress(UserProgress data) {
        Progress progress = data.progress;
        binding.greeting.setText(getString(R.string.dashboard_greeting, data.user.username));

        int stars = progress.stars();
        binding.rankValue.setText(Progress.rank(stars));
        binding.starsValue.setText(String.valueOf(stars));
        LinearLayout.LayoutParams fill =
                (LinearLayout.LayoutParams) binding.starsProgress.getLayoutParams();
        fill.weight = stars;
        ((LinearLayout) binding.starsProgress.getParent()).setWeightSum(Progress.TOTAL_STARS);
        binding.starsProgress.setLayoutParams(fill);
        binding.trackBeginner.meta.setText(trackMeta(progress.stars(Lessons.BEGINNER)));

        for (Lesson lesson : lessons) {
            Progress.Tier best = progress.best(lesson.info.id);
            lesson.row.tierStar.setVisibility(best.icon != 0 ? View.VISIBLE : View.GONE);
            if (best.icon != 0) {
                lesson.row.tierStar.setImageResource(best.icon);
            }
            lesson.row.tierLabel.setText(best.label);
        }

        // Open the lesson practiced most recently, or the first one for a new account
        Lesson current = find(progress.lastLessonId());
        ViewLessonCardBinding card = binding.lessonCard;
        if (card != null) {
            showInCard(card, current);
        } else {
            setExpanded(current.row, true);
        }
    }

    private Lesson find(@Nullable String lessonId) {
        for (Lesson lesson : lessons) {
            if (lesson.info.id.equals(lessonId)) {
                return lesson;
            }
        }
        return lessons[0];
    }

    private String trackMeta(int stars) {
        return getString(R.string.track_meta, Lessons.LESSONS_PER_TRACK, stars, STARS_PER_TRACK);
    }

    private static void bindTrack(ViewTrackHeaderBinding track, String level,
                                  @StringRes int name) {
        track.level.setText(level);
        track.name.setText(name);
    }

    private static void bindDetails(ViewLessonDetailsBinding details, Lessons.Lesson lesson) {
        details.technique.setText(lesson.technique);
        details.summary.setText(lesson.summary);
    }

    /** Marks {@code shown} as the selected row and fills the card with it. */
    private void showInCard(ViewLessonCardBinding card, Lesson shown) {
        for (Lesson lesson : lessons) {
            lesson.row.getRoot().setSelected(lesson == shown);
        }
        card.title.setText(shown.info.name);
        bindDetails(card.details, shown.info);
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
        lessons = null;
    }
}
