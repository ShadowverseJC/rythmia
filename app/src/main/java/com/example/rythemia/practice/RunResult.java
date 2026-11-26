package com.example.rythemia.practice;

import android.os.Bundle;

import androidx.annotation.NonNull;

/** How one run of a lesson went; passed from the exercise screen to the score report. */
public final class RunResult {

    private static final String KEY_LESSON = "lesson_id";
    private static final String KEY_BPM = "bpm";
    private static final String KEY_HITS = "hits";
    private static final String KEY_MISSES = "misses";

    @NonNull public final String lessonId;
    public final int bpm;
    public final int hits;
    public final int misses;

    public RunResult(@NonNull String lessonId, int bpm, int hits, int misses) {
        this.lessonId = lessonId;
        this.bpm = bpm;
        this.hits = hits;
        this.misses = misses;
    }

    /**
     * Stand-in until playback can listen to the guitar: the sample run the score report shows
     * (12 of 16 notes, missing the notes marked on its staff), at the chosen tempo.
     */
    @NonNull
    public static RunResult sample(@NonNull String lessonId, int bpm) {
        return new RunResult(lessonId, bpm, 12, 4);
    }

    @NonNull
    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_LESSON, lessonId);
        bundle.putInt(KEY_BPM, bpm);
        bundle.putInt(KEY_HITS, hits);
        bundle.putInt(KEY_MISSES, misses);
        return bundle;
    }

    @NonNull
    public static RunResult fromBundle(@NonNull Bundle bundle) {
        String lessonId = bundle.getString(KEY_LESSON);
        return new RunResult(lessonId != null ? lessonId : Lessons.SIXTEENTH,
                bundle.getInt(KEY_BPM, Tempo.DEFAULT), bundle.getInt(KEY_HITS),
                bundle.getInt(KEY_MISSES));
    }
}
