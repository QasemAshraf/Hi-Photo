package com.android.instanano.view.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import android.os.Bundle;
import com.android.instanano.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpView();
    }

    private void setUpView(){
        bottomNavigationView = findViewById(R.id.buttonNavigationView);
        NavController navController = Navigation.findNavController(this ,R.id.nav_host_fragment_container);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        for (Fragment fragment : Objects.requireNonNull(getSupportFragmentManager()
//                .getPrimaryNavigationFragment()).getChildFragmentManager().getFragments()) {
//            fragment.onActivityResult(requestCode, resultCode, data);
//        }
//    }
}
