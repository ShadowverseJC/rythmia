package com.example.rythemia.practice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.example.rythemia.R;
import com.example.rythemia.data.Attempt;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ProgressTest {

    @Test
    public void tier_followsAccuracy() {
        assertEquals(Progress.Tier.NONE, Progress.Tier.of(0, 0));
        assertEquals(Progress.Tier.NONE, Progress.Tier.of(7, 9));    // 43%
        assertEquals(Progress.Tier.BRONZE, Progress.Tier.of(8, 8));  // 50%
        assertEquals(Progress.Tier.SILVER, Progress.Tier.of(12, 4)); // 75%
        assertEquals(Progress.Tier.GOLD, Progress.Tier.of(15, 1));   // 93%
        assertEquals(3, Progress.Tier.GOLD.stars());
    }

    @Test
    public void emptyHistory_hasNoProgress() {
        Progress progress = new Progress(Collections.emptyList());

        assertEquals(0, progress.stars());
        assertEquals(Progress.Tier.NONE, progress.best(Lessons.SIXTEENTH));
        assertEquals(0, progress.topSpeed());
        assertEquals(0, progress.strength());
        assertNull(progress.lastLessonId());
        assertEquals(R.string.rank_beginner, Progress.rank(progress.stars()));
    }

    @Test
    public void best_keepsEachLessonsHighestTier() {
        Progress progress = new Progress(newestFirst(
                attempt(Lessons.SIXTEENTH, 100, 8, 8),   // bronze
                attempt(Lessons.SIXTEENTH, 120, 15, 1),  // gold
                attempt(Lessons.SIXTEENTH, 140, 12, 4),  // silver
                attempt(Lessons.QUARTER, 90, 12, 4)));   // silver

        assertEquals(Progress.Tier.GOLD, progress.best(Lessons.SIXTEENTH));
        assertEquals(Progress.Tier.SILVER, progress.best(Lessons.QUARTER));
        assertEquals(Progress.Tier.NONE, progress.best(Lessons.EIGHTH));
        assertEquals(5, progress.stars());
        assertEquals(5, progress.stars(Lessons.BEGINNER));
    }

    @Test
    public void topSpeed_onlyCountsRunsThatEarnedAStar() {
        Progress progress = new Progress(newestFirst(
                attempt(Lessons.SIXTEENTH, 200, 2, 14),  // no star: doesn't count
                attempt(Lessons.SIXTEENTH, 150, 12, 4),
                attempt(Lessons.QUARTER, 160, 8, 8)));

        assertEquals(160, progress.topSpeed());
        assertEquals(150, progress.topSpeed(Lessons.SIXTEENTH));
        assertEquals(0, progress.topSpeed(Lessons.EIGHTH));
    }

    @Test
    public void strength_isTheTechniqueWithTheBestAverageAccuracy() {
        Progress progress = new Progress(newestFirst(
                attempt(Lessons.QUARTER, 100, 4, 0),     // downstrokes 100%
                attempt(Lessons.SIXTEENTH, 100, 12, 4),  // alternate picking 75%
                attempt(Lessons.EIGHTH, 100, 8, 0)));    // alternate picking 100% -> 87.5% avg

        assertEquals(R.string.technique_downstrokes, progress.strength());
    }

    @Test
    public void lastLessonId_isTheMostRecentRun() {
        List<Attempt> attempts = newestFirst(
                attempt(Lessons.QUARTER, 100, 4, 0),
                attempt(Lessons.EIGHTH, 100, 8, 0));

        assertEquals(Lessons.EIGHTH, new Progress(attempts).lastLessonId());
    }

    @Test
    public void rank_risesWithStars() {
        assertEquals(R.string.rank_beginner, Progress.rank(4));
        assertEquals(R.string.rank_beginner_intermediate, Progress.rank(5));
        assertEquals(R.string.rank_intermediate, Progress.rank(10));
        assertEquals(R.string.rank_intermediate_advanced, Progress.rank(15));
        assertEquals(R.string.rank_advanced, Progress.rank(20));
        assertEquals(27, Progress.TOTAL_STARS);
    }

    private static Attempt attempt(String lessonId, int bpm, int hits, int misses) {
        Attempt attempt = new Attempt();
        attempt.lessonId = lessonId;
        attempt.bpm = bpm;
        attempt.hits = hits;
        attempt.misses = misses;
        return attempt;
    }

    /** Gives the attempts increasing timestamps, then orders them newest first like the DAO. */
    private static List<Attempt> newestFirst(Attempt... oldestFirst) {
        for (int i = 0; i < oldestFirst.length; i++) {
            oldestFirst[i].timestamp = 1_000L * (i + 1);
        }
        List<Attempt> attempts = new ArrayList<>(Arrays.asList(oldestFirst));
        Collections.reverse(attempts);
        return attempts;
    }
}
