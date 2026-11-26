package com.example.rythemia;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * Shows a validation message under a text field (a Text.FieldError line) and outlines the
 * field in red, in place of TextInputLayout so the custom inputs keep their look.
 */
final class FieldErrors {

    private static final float ERROR_STROKE_DP = 1.5f;

    private FieldErrors() {
    }

    /** Shows {@code message} under {@code field}, or clears its error when null. */
    static void show(@NonNull EditText field, @NonNull TextView error,
                     @Nullable CharSequence message) {
        // Remember the field's own background (corner radii differ between layouts) so
        // clearing the error restores it exactly
        Drawable normal = (Drawable) field.getTag(R.id.normal_background);
        if (normal == null) {
            normal = field.getBackground();
            field.setTag(R.id.normal_background, normal);
        }
        // Neither shape defines padding, so swapping them keeps the field's padding
        field.setBackground(message != null ? outlined(field.getContext(), normal) : normal);
        error.setText(message);
        error.setVisibility(message != null ? View.VISIBLE : View.GONE);
    }

    /** Clears the field's error as soon as the user edits it. */
    static void clearOnEdit(@NonNull EditText field, @NonNull TextView error) {
        field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (error.getVisibility() == View.VISIBLE) {
                    show(field, error, null);
                }
            }
        });
    }

    /** A copy of the field's shape with a red border, or the shape itself if it isn't one. */
    private static Drawable outlined(Context context, Drawable normal) {
        Drawable.ConstantState state = normal != null ? normal.getConstantState() : null;
        if (state == null) {
            return normal;
        }
        Drawable copy = state.newDrawable(context.getResources()).mutate();
        if (!(copy instanceof GradientDrawable)) {
            return normal;
        }
        int width = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                ERROR_STROKE_DP, context.getResources().getDisplayMetrics()));
        ((GradientDrawable) copy).setStroke(width,
                ContextCompat.getColor(context, R.color.feedback_miss));
        return copy;
    }
}
