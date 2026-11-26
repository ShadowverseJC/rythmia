package com.example.rythemia;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;

import com.example.rythemia.account.AccountValidator;
import com.example.rythemia.account.AccountViewModel;
import com.example.rythemia.databinding.FragmentLoginBinding;
import com.google.android.material.snackbar.Snackbar;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private AccountViewModel account;
    private final AccountValidator validator = new AccountValidator();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        account = new ViewModelProvider(this).get(AccountViewModel.class);

        FieldErrors.clearOnEdit(binding.username, binding.usernameError);
        FieldErrors.clearOnEdit(binding.password, binding.passwordError);
        binding.loginButton.setOnClickListener(v -> logIn());
        binding.createAccountLink.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_login_to_register)
        );
        account.status().observe(getViewLifecycleOwner(), this::render);
    }

    private void logIn() {
        String username = String.valueOf(binding.username.getText()).trim();
        String password = String.valueOf(binding.password.getText());
        AccountValidator.Errors errors = validator.forLogin(username, password);
        FieldErrors.show(binding.username, binding.usernameError, message(errors.username));
        FieldErrors.show(binding.password, binding.passwordError, message(errors.password));
        if (!errors.any()) {
            WindowCompat.getInsetsController(requireActivity().getWindow(), binding.getRoot())
                    .hide(WindowInsetsCompat.Type.ime());
            account.logIn(username, password);
        }
    }

    private void render(@Nullable AccountViewModel.Status status) {
        binding.loginButton.setEnabled(status != AccountViewModel.Status.WORKING);
        if (status == null || status == AccountViewModel.Status.WORKING) {
            return;
        }
        if (status == AccountViewModel.Status.LOGGED_IN) {
            NavController nav = NavHostFragment.findNavController(this);
            NavDestination here = nav.getCurrentDestination();
            if (here != null && here.getId() == R.id.loginFragment) {
                nav.navigate(R.id.action_login_to_dashboard);
            }
            return;
        }
        // Don't say whether the username or the password was wrong
        Snackbar.make(binding.getRoot(), status == AccountViewModel.Status.WRONG_CREDENTIALS
                ? R.string.login_failed : R.string.error_generic, Snackbar.LENGTH_LONG).show();
        account.resultShown();
    }

    @Nullable
    private CharSequence message(@StringRes int error) {
        return error == 0 ? null : getString(error);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
