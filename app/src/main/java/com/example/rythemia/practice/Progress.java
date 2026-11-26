package com.example.rythemia.practice;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.example.rythemia.R;
import com.example.rythemia.data.Attempt;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Everything the dashboard, profile and exercise screens show about a user's progress, worked
 * out from their attempts: the best star per lesson, total stars, rank, top speed and strongest
 * technique.
 */
public final class Progress {

    public static final int STARS_PER_LESSON = 3;
    public static final int TOTAL_STARS =
            Lessons.TRACKS * Lessons.LESSONS_PER_TRACK * STARS_PER_LESSON;

    /** The star a run earns from its accuracy: 50% bronze, 75% silver, 90% gold. */
    public enum Tier {
        NONE(R.string.tier_none, 0),
        BRONZE(R.string.tier_bronze, R.drawable.ic_star_bronze),
        SILVER(R.string.tier_silver, R.drawable.ic_star_silver),
        GOLD(R.string.tier_gold, R.drawable.ic_star_gold);

        @StringRes public final int label;
        /** 0 for {@link #NONE}. */
        @DrawableRes public final int icon;

        Tier(@StringRes int label, @DrawableRes int icon) {
            this.label = label;
            this.icon = icon;
        }

        public int stars() {
            return ordinal();
        }

        @NonNull
        public static Tier of(int hits, int misses) {
            int accuracy = accuracy(hits, misses);
            if (accuracy >= 90) return GOLD;
            if (accuracy >= 75) return SILVER;
            if (accuracy >= 50) return BRONZE;
            return NONE;
        }
    }

    private final Map<String, Tier> best = new HashMap<>();
    private final Map<String, Integer> topSpeed = new HashMap<>();
    /** Per technique: {sum of accuracies, number of runs}, in first-seen order. */
    private final Map<Integer, int[]> techniqueAccuracy = new LinkedHashMap<>();
    @Nullable private final String lastLessonId;

    /** @param attempts a user's attempts, newest first */
    public Progress(@NonNull List<Attempt> attempts) {
        lastLessonId = attempts.isEmpty() ? null : attempts.get(0).lessonId;
        for (Attempt attempt : attempts) {
            Tier tier = Tier.of(attempt.hits, attempt.misses);
            Tier previous = best.get(attempt.lessonId);
            if (previous == null || tier.compareTo(previous) > 0) {
                best.put(attempt.lessonId, tier);
            }
            if (tier != Tier.NONE) {
                Integer fastest = topSpeed.get(attempt.lessonId);
                if (fastest == null || attempt.bpm > fastest) {
                    topSpeed.put(attempt.lessonId, attempt.bpm);
                }
            }
            Lessons.Lesson lesson = Lessons.find(attempt.lessonId);
            if (lesson != null) {
                int[] sum = techniqueAccuracy.get(lesson.technique);
                if (sum == null) {
                    sum = new int[2];
                    techniqueAccuracy.put(lesson.technique, sum);
                }
                sum[0] += accuracy(attempt.hits, attempt.misses);
                sum[1]++;
            }
        }
    }

    /** Whole-number percentage of notes hit. */
    public static int accuracy(int hits, int misses) {
        int notes = hits + misses;
        return notes == 0 ? 0 : hits * 100 / notes;
    }

    @NonNull
    public Tier best(@NonNull String lessonId) {
        Tier tier = best.get(lessonId);
        return tier != null ? tier : Tier.NONE;
    }

    /** Stars across every lesson: each lesson counts its best run. */
    public int stars() {
        int stars = 0;
        for (Tier tier : best.values()) {
            stars += tier.stars();
        }
        return stars;
    }

    public int stars(@NonNull List<Lessons.Lesson> lessons) {
        int stars = 0;
        for (Lessons.Lesson lesson : lessons) {
            stars += best(lesson.id).stars();
        }
        return stars;
    }

    /** The fastest tempo of any run that earned a star, or 0 if none has. */
    public int topSpeed() {
        int fastest = 0;
        for (int bpm : topSpeed.values()) {
            fastest = Math.max(fastest, bpm);
        }
        return fastest;
    }

    /** The fastest tempo of a starred run of this lesson, or 0 if none. */
    public int topSpeed(@NonNull String lessonId) {
        Integer bpm = topSpeed.get(lessonId);
        return bpm != null ? bpm : 0;
    }

    /** The technique with the best average accuracy, or 0 before any runs. */
    @StringRes
    public int strength() {
        int strongest = 0;
        double bestAverage = -1;
        for (Map.Entry<Integer, int[]> entry : techniqueAccuracy.entrySet()) {
            double average = (double) entry.getValue()[0] / entry.getValue()[1];
            if (average > bestAverage) {
                bestAverage = average;
                strongest = entry.getKey();
            }
        }
        return strongest;
    }

    /** The lesson of the newest run, or null before any runs. */
    @Nullable
    public String lastLessonId() {
        return lastLessonId;
    }

    @StringRes
    public static int rank(int stars) {
        if (stars < 5) return R.string.rank_beginner;
        if (stars < 10) return R.string.rank_beginner_intermediate;
        if (stars < 15) return R.string.rank_intermediate;
        if (stars < 20) return R.string.rank_intermediate_advanced;
        return R.string.rank_advanced;
    }
}
