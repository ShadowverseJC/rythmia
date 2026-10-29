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

import com.example.rythemia.databinding.FragmentProfileBinding;
import com.example.rythemia.databinding.ViewStatTileBinding;

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

        binding.displayName.setText(R.string.placeholder_username);

        bindStat(binding.statRank, R.drawable.bg_badge_pink_small, R.drawable.ic_medal,
                R.string.rank, R.string.rank_value_wrapping);
        bindStat(binding.statStars, R.drawable.bg_badge_gold_small, R.drawable.ic_star_gold,
                R.string.stars, R.string.stars_fraction);
        bindStat(binding.statSpeed, R.drawable.bg_badge_cyan_small, R.drawable.ic_speed,
                R.string.top_speed, R.string.top_speed_value);
        bindStat(binding.statStrength, R.drawable.bg_badge_violet_small, R.drawable.ic_strength,
                R.string.strength, R.string.strength_value);

        NavController nav = NavHostFragment.findNavController(this);
        binding.appBar.backLabel.setText(R.string.back_practice);
        binding.appBar.avatar.setVisibility(View.GONE);
        binding.appBar.back.setOnClickListener(v -> nav.popBackStack());
        binding.logoutButton.setOnClickListener(v -> nav.navigate(R.id.action_profile_to_login));
    }

    private static void bindStat(ViewStatTileBinding tile, @DrawableRes int badge,
                                 @DrawableRes int icon, @StringRes int label, @StringRes int value) {
        tile.badge.setBackgroundResource(badge);
        tile.icon.setImageResource(icon);
        tile.label.setText(label);
        tile.value.setText(value);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
