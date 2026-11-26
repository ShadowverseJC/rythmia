package com.example.rythemia;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.fragment.NavHostFragment;

import com.example.rythemia.data.Session;
import com.example.rythemia.databinding.ActivityMainBinding;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Keep every screen clear of the status bar, navigation bar, keyboard and camera
        // cutout (in landscape the cutout sits on a side edge, outside the system bars).
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.ime()
                    | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        // Someone still logged in from last time goes straight to the dashboard. The graph is
        // set here rather than in the layout so the start can depend on the session; after a
        // rotation the nav controller restores its own back stack instead.
        NavHostFragment host = (NavHostFragment) Objects.requireNonNull(
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment));
        NavController nav = host.getNavController();
        NavGraph graph = nav.getNavInflater().inflate(R.navigation.nav_graph);
        graph.setStartDestination(new Session(this).isLoggedIn()
                ? R.id.dashboardFragment : R.id.loginFragment);
        nav.setGraph(graph);
    }
}
