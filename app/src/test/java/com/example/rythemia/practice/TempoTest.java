package com.example.rythemia.practice;

import static org.junit.Assert.assertEquals;

import com.example.rythemia.R;

import org.junit.Test;

public class TempoTest {

    @Test
    public void clamp_keepsBpmInRange() {
        assertEquals(40, Tempo.clamp(0));
        assertEquals(40, Tempo.clamp(39));
        assertEquals(120, Tempo.clamp(120));
        assertEquals(240, Tempo.clamp(241));
        assertEquals(240, Tempo.clamp(Integer.MAX_VALUE));
    }

    @Test
    public void marking_followsTheTempoBands() {
        assertEquals(R.string.tempo_largo, Tempo.marking(40));
        assertEquals(R.string.tempo_adagio, Tempo.marking(60));
        assertEquals(R.string.tempo_andante, Tempo.marking(76));
        assertEquals(R.string.tempo_moderato, Tempo.marking(108));
        assertEquals(R.string.tempo_allegro, Tempo.marking(120));
        assertEquals(R.string.tempo_presto, Tempo.marking(168));
        assertEquals(R.string.tempo_prestissimo, Tempo.marking(200));
    }
}
