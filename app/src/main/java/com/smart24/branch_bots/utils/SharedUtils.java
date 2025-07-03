package com.smart24.branch_bots.utils;

import static android.widget.Toast.LENGTH_LONG;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.Toast;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.smart24.branch_bots.R;

public class SharedUtils {
    private static void showSnackBar(Activity activity, String text, int bckgColor) {
        try {
            final View viewPos = activity.findViewById(R.id.nav_host_fragment_content_home);
            if (viewPos != null) {
                Snackbar snack = Snackbar.make(viewPos, text, Snackbar.LENGTH_LONG);
                View view = snack.getView();
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
                params.setMargins(0, 0, 0, 180);
                view.setLayoutParams(params);
                snack.setBackgroundTint(ContextCompat.getColor(activity, bckgColor));
                snack.show();
            } else {
                Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
        }
    }

    public static void showMessagePositive(Activity activity, String text) {
        showSnackBar(activity, text, R.color.logo_color);
    }

    public static void showMessageNegative(Activity activity, String text) {
        showSnackBar(activity, text, R.color.purple_200);
    }

    public static void showMessageInfo(Activity activity, String text) {
        showSnackBar(activity, text, R.color.purple_750);
    }


    public static boolean checkAndRequestPermissions(Activity activity) {
        int recordAudioPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
        int writeStoragePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readStoragePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        boolean isPermissionGranted = (recordAudioPermission == PackageManager.PERMISSION_GRANTED && writeStoragePermission == PackageManager.PERMISSION_GRANTED && readStoragePermission == PackageManager.PERMISSION_GRANTED);
        if (!isPermissionGranted) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        }
        return true;
    }

    public static void showToast(String text) {
        Toast.makeText(MyApp.getAppContext(), text, LENGTH_LONG).show();
    }
}
