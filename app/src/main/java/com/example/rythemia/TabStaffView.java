package com.example.rythemia;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.annotation.FontRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.util.HashSet;
import java.util.Set;

/**
 * One bar of sixteenth notes on the open low E, drawn as two tab systems (beats 1–2 and 3–4).
 * Measurements mirror the "Tab Staff" component in the Figma file.
 */
public class TabStaffView extends View {

    private static final int MODE_INTRO = 0;
    private static final String[] STRING_NAMES = {"e", "B", "G", "D", "A"};
    private static final String[] COUNTS = {"", "e", "&", "a"};

    // Vertical metrics in dp
    private static final float PICK_ROW = 20f;
    private static final float STRING_ROW = 12f;
    private static final float NOTE_ROW = 28f;
    private static final float BEAM_ROW = 22f;
    private static final float COUNT_ROW = 14f;
    private static final float SYSTEM_GAP = 18f;
    private static final float NAMES_WIDTH = 20f; // 12dp column + 8dp gap

    private final float density;
    private final boolean intro;
    private final Set<Integer> missed = new HashSet<>();

    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Typeface monoMedium;
    private final Typeface monoBold;
    private final Drawable pickDown;
    private final Drawable pickUp;
    private final RectF rect = new RectF();

    public TabStaffView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabStaffView);
        intro = a.getInt(R.styleable.TabStaffView_staffMode, MODE_INTRO) == MODE_INTRO;
        String missedAttr = a.getString(R.styleable.TabStaffView_missedNotes);
        a.recycle();
        if (missedAttr != null) {
            for (String index : missedAttr.split(",")) {
                missed.add(Integer.parseInt(index.trim()));
            }
        }

        monoMedium = font(context, R.font.geist_mono_medium);
        monoBold = font(context, R.font.geist_mono_bold);
        pickDown = ContextCompat.getDrawable(context, R.drawable.ic_pick_down);
        pickUp = ContextCompat.getDrawable(context, R.drawable.ic_pick_up);
        stroke.setStyle(Paint.Style.STROKE);
        text.setTextAlign(Paint.Align.CENTER);
    }

    private static Typeface font(Context context, @FontRes int id) {
        Typeface typeface = ResourcesCompat.getFont(context, id);
        return typeface != null ? typeface : Typeface.MONOSPACE;
    }

    private float dp(float value) {
        return value * density;
    }

    private float systemHeight() {
        return dp((intro ? PICK_ROW : 0) + STRING_ROW * 5 + NOTE_ROW + 2 + BEAM_ROW + 6 + COUNT_ROW);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) Math.ceil(getPaddingTop() + getPaddingBottom()
                + systemHeight() * 2 + dp(SYSTEM_GAP));
        setMeasuredDimension(width, resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float top = getPaddingTop();
        drawSystem(canvas, top, 0, false);
        drawSystem(canvas, top + systemHeight() + dp(SYSTEM_GAP), 2, true);
    }

    private void drawSystem(Canvas canvas, float y0, int firstBeat, boolean last) {
        float left = getPaddingLeft();
        float staffX = left + dp(NAMES_WIDTH);
        float staffW = getWidth() - getPaddingRight() - staffX;
        float beatW = (staffW - dp(8) * 2 - dp(14)) / 2f;
        float cellW = beatW / 4f;

        float stringsTop = y0 + dp(intro ? PICK_ROW : 0);
        float noteTop = stringsTop + dp(STRING_ROW * 5);
        float noteCenter = noteTop + dp(NOTE_ROW / 2);
        float beamsTop = noteTop + dp(NOTE_ROW + 2);
        float countsCenter = beamsTop + dp(BEAM_ROW + 6 + COUNT_ROW / 2);

        // String names and lines
        text.setTextSize(dp(11));
        for (int s = 0; s < STRING_NAMES.length; s++) {
            float cy = stringsTop + dp(STRING_ROW * s + STRING_ROW / 2);
            drawText(canvas, STRING_NAMES[s], left + dp(6), cy, monoMedium, R.color.text_subtle);
            fill.setColor(color(R.color.staff_line));
            canvas.drawRect(staffX, cy - dp(0.5f), staffX + staffW, cy + dp(0.5f), fill);
        }
        drawText(canvas, "E", left + dp(6), noteCenter, monoBold, R.color.text_primary);
        fill.setColor(color(R.color.staff_line_strong));
        canvas.drawRect(staffX, noteCenter - dp(0.75f), staffX + staffW, noteCenter + dp(0.75f), fill);

        // Bar lines
        float barTop = stringsTop + dp(STRING_ROW / 2 - 0.5f);
        float barBottom = barTop + dp(69.25f);
        fill.setColor(color(R.color.staff_bar));
        canvas.drawRect(staffX, barTop, staffX + dp(1.5f), barBottom, fill);
        if (last) {
            canvas.drawRect(staffX + staffW - dp(1.5f), barTop, staffX + staffW, barBottom, fill);
        }

        for (int b = 0; b < 2; b++) {
            int beat = firstBeat + b;
            float beatX = staffX + dp(8) + b * (beatW + dp(14));

            for (int c = 0; c < 4; c++) {
                int note = beat * 4 + c;
                boolean isMissed = !intro && missed.contains(note);
                float cx = beatX + cellW * (c + 0.5f);

                if (intro) {
                    Drawable pick = c % 2 == 0 ? pickDown : pickUp;
                    int half = (int) dp(7);
                    int cy = (int) (y0 + dp(PICK_ROW / 2));
                    pick.setBounds((int) cx - half, cy - half, (int) cx + half, cy + half);
                    pick.draw(canvas);
                }

                drawNoteChip(canvas, cx, noteCenter, isMissed);

                fill.setColor(color(R.color.staff_stem));
                canvas.drawRect(cx - dp(1), beamsTop, cx + dp(1), beamsTop + dp(BEAM_ROW), fill);

                text.setTextSize(dp(11));
                String count = c == 0 ? String.valueOf(beat + 1) : COUNTS[c];
                boolean strong = c == 0 || isMissed;
                int countColor = isMissed ? R.color.feedback_miss_text
                        : c == 0 ? R.color.text_primary : R.color.text_subtle;
                drawText(canvas, count, cx, countsCenter, strong ? monoBold : monoMedium, countColor);
            }

            // Two beams join the four sixteenth-note stems
            float beamStart = beatX + cellW * 0.5f - dp(1);
            float beamEnd = beatX + cellW * 3.5f + dp(1);
            fill.setColor(color(R.color.staff_stem));
            canvas.drawRect(beamStart, beamsTop + dp(18), beamEnd, beamsTop + dp(22), fill);
            canvas.drawRect(beamStart, beamsTop + dp(11), beamEnd, beamsTop + dp(15), fill);
        }
    }

    private void drawNoteChip(Canvas canvas, float cx, float cy, boolean isMissed) {
        float half = dp(12);
        rect.set(cx - half, cy - half, cx + half, cy + half);
        int textColor;
        if (intro) {
            fill.setColor(color(R.color.tag_bg));
            canvas.drawRoundRect(rect, dp(7), dp(7), fill);
            textColor = R.color.tag_text;
        } else if (isMissed) {
            fill.setColor(color(R.color.bg_sunken));
            canvas.drawRoundRect(rect, dp(7), dp(7), fill);
            stroke.setStrokeWidth(dp(2));
            stroke.setColor(color(R.color.feedback_miss));
            rect.inset(dp(1), dp(1));
            canvas.drawRoundRect(rect, dp(6), dp(6), stroke);
            textColor = R.color.feedback_miss_text;
        } else {
            fill.setColor(color(R.color.feedback_hit));
            canvas.drawRoundRect(rect, dp(7), dp(7), fill);
            textColor = R.color.feedback_on_hit;
        }
        text.setTextSize(dp(13));
        drawText(canvas, "0", cx, cy, monoBold, textColor);
    }

    private void drawText(Canvas canvas, String value, float cx, float cy, Typeface typeface,
                          @ColorRes int colorRes) {
        text.setTypeface(typeface);
        text.setColor(color(colorRes));
        Paint.FontMetrics metrics = text.getFontMetrics();
        canvas.drawText(value, cx, cy - (metrics.ascent + metrics.descent) / 2f, text);
    }

    private int color(@ColorRes int id) {
        return ContextCompat.getColor(getContext(), id);
    }
}
