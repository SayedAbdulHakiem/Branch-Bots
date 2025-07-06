package com.smart24.branch_bots.ui.home;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.csjbot.coshandler.aiui.aiui_soft.send_msg.http.RetrofitFactory;
import com.csjbot.coshandler.core.CsjRobot;
import com.csjbot.coshandler.listener.OnAuthenticationListener;
import com.smart24.branch_bots.R;
import com.smart24.branch_bots.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//        binding.chat.setVisibility(View.VISIBLE);
        binding.chat.setOnClickListener(view -> openChatFragment());


        // The following parts related to SDK config
        CsjRobot.authentication(this, "appKey", "appSecret", new OnAuthenticationListener() {
            @Override
            public void success() {

            }

            @Override
            public void error() {

            }
        });
        RetrofitFactory.initClient();
    }

    public void openChatFragment() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        navController.navigate(R.id.ChatFragment, null);
//        binding.chat.setVisibility(View.GONE);
    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}