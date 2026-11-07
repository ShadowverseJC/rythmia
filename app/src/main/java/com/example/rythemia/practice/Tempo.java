package com.example.rythemia.practice;

import androidx.annotation.StringRes;

import com.example.rythemia.R;

/** The tempo range a lesson can be practiced at, and the Italian marking for a tempo. */
public final class Tempo {

    public static final int MIN = 40;
    public static final int MAX = 240;
    public static final int STEP = 5;
    public static final int DEFAULT = 120;

    private Tempo() {
    }

    public static int clamp(int bpm) {
        return Math.max(MIN, Math.min(MAX, bpm));
    }

    @StringRes
    public static int marking(int bpm) {
        if (bpm < 60) return R.string.tempo_largo;
        if (bpm < 76) return R.string.tempo_adagio;
        if (bpm < 108) return R.string.tempo_andante;
        if (bpm < 120) return R.string.tempo_moderato;
        if (bpm < 168) return R.string.tempo_allegro;
        if (bpm < 200) return R.string.tempo_presto;
        return R.string.tempo_prestissimo;
    }
}
