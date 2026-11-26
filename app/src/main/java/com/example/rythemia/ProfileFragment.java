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

import com.example.rythemia.data.AppDatabase;
import com.example.rythemia.data.Background;
import com.example.rythemia.data.Session;
import com.example.rythemia.databinding.FragmentProfileBinding;
import com.example.rythemia.databinding.ViewStatTileBinding;
import com.example.rythemia.practice.Progress;
import com.example.rythemia.practice.UserProgress;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindStat(binding.statRank, R.drawable.bg_badge_pink_small, R.drawable.ic_medal,
                R.string.rank);
        bindStat(binding.statStars, R.drawable.bg_badge_gold_small, R.drawable.ic_star_gold,
                R.string.stars);
        bindStat(binding.statSpeed, R.drawable.bg_badge_cyan_small, R.drawable.ic_speed,
                R.string.top_speed);
        bindStat(binding.statStrength, R.drawable.bg_badge_violet_small, R.drawable.ic_strength,
                R.string.strength);

        Session session = new Session(requireContext());
        NavController nav = NavHostFragment.findNavController(this);
        binding.appBar.backLabel.setText(R.string.back_practice);
        binding.appBar.avatar.setVisibility(View.GONE);
        binding.appBar.back.setOnClickListener(v -> nav.popBackStack());
        binding.logoutButton.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.log_out, (dialog, which) -> {
                    session.logOut();
                    nav.navigate(R.id.action_profile_to_login);
                })
                .show());

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
                nav.navigate(R.id.action_profile_to_login);
            } else {
                showProfile(data);
            }
        });
    }

    private void showProfile(UserProgress data) {
        Progress progress = data.progress;
        int stars = progress.stars();
        int topSpeed = progress.topSpeed();

        binding.displayName.setText(data.user.username);
        binding.rankChip.setText(Progress.rank(stars));
        binding.starsChip.setText(getResources().getQuantityString(R.plurals.stars_count, stars,
                stars));
        binding.statRank.value.setText(Progress.rank(stars));
        binding.statStars.value.setText(getString(R.string.stars_fraction, stars,
                Progress.TOTAL_STARS));
        binding.statSpeed.value.setText(topSpeed > 0
                ? getString(R.string.bpm_format, topSpeed) : getString(R.string.no_value));
        binding.statStrength.value.setText(progress.strength() != 0
                ? getString(progress.strength()) : getString(R.string.no_value));
        binding.accountUsername.setText(data.user.username);
        binding.accountEmail.setText(data.user.email);
    }

    private static void bindStat(ViewStatTileBinding tile, @DrawableRes int badge,
                                 @DrawableRes int icon, @StringRes int label) {
        tile.badge.setBackgroundResource(badge);
        tile.icon.setImageResource(icon);
        tile.label.setText(label);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
