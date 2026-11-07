package com.example.rythemia;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.example.rythemia.databinding.FragmentRegisterBinding;
import com.google.android.material.snackbar.Snackbar;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private AccountViewModel account;
    private final AccountValidator validator = new AccountValidator();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        account = new ViewModelProvider(this).get(AccountViewModel.class);

        FieldErrors.clearOnEdit(binding.username, binding.usernameError);
        FieldErrors.clearOnEdit(binding.email, binding.emailError);
        FieldErrors.clearOnEdit(binding.password, binding.passwordError);
        binding.createAccountButton.setOnClickListener(v -> register());
        binding.loginLink.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );
        account.status().observe(getViewLifecycleOwner(), this::render);
    }

    private void register() {
        String username = String.valueOf(binding.username.getText()).trim();
        String email = String.valueOf(binding.email.getText()).trim();
        String password = String.valueOf(binding.password.getText());
        AccountValidator.Errors errors = validator.forRegistration(username, email, password);
        FieldErrors.show(binding.username, binding.usernameError, message(errors.username));
        FieldErrors.show(binding.email, binding.emailError, message(errors.email));
        FieldErrors.show(binding.password, binding.passwordError, message(errors.password));
        if (!errors.any()) {
            WindowCompat.getInsetsController(requireActivity().getWindow(), binding.getRoot())
                    .hide(WindowInsetsCompat.Type.ime());
            account.register(username, email, password);
        }
    }

    private void render(@Nullable AccountViewModel.Status status) {
        binding.createAccountButton.setEnabled(status != AccountViewModel.Status.WORKING);
        if (status == null || status == AccountViewModel.Status.WORKING) {
            return;
        }
        switch (status) {
            case LOGGED_IN:
                NavController nav = NavHostFragment.findNavController(this);
                NavDestination here = nav.getCurrentDestination();
                if (here != null && here.getId() == R.id.registerFragment) {
                    // A Toast, unlike a Snackbar, stays up while the dashboard opens
                    Toast.makeText(requireContext(), R.string.account_created,
                            Toast.LENGTH_SHORT).show();
                    nav.navigate(R.id.action_register_to_dashboard);
                }
                return;
            case USERNAME_TAKEN:
                FieldErrors.show(binding.username, binding.usernameError,
                        getString(R.string.error_username_taken));
                break;
            case EMAIL_TAKEN:
                FieldErrors.show(binding.email, binding.emailError,
                        getString(R.string.error_email_taken));
                break;
            default:
                Snackbar.make(binding.getRoot(), R.string.error_generic, Snackbar.LENGTH_LONG)
                        .show();
        }
        account.resultShown();
    }

    /** Only the password-length message has a placeholder (the minimum length). */
    @Nullable
    private CharSequence message(@StringRes int error) {
        if (error == 0) {
            return null;
        }
        return error == R.string.error_password_short
                ? getString(error, AccountValidator.MIN_PASSWORD_LENGTH) : getString(error);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
