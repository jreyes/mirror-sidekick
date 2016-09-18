package com.vaporwarecorp.mirror.sidekick.util;

import android.app.Activity;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PermissionUtil {

    private static final String[] PERMISSIONS_REQUIRED = {BLUETOOTH, BLUETOOTH_ADMIN,
            INTERNET, WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_CODE = 31417;

// -------------------------- STATIC METHODS --------------------------

    public static List<String> checkPermissions(Activity activity) {
        List<String> neededPermissions = new ArrayList<>();
        for (String permission : PERMISSIONS_REQUIRED) {
            if (ActivityCompat.checkSelfPermission(activity, permission) != PERMISSION_GRANTED) {
                neededPermissions.add(permission);
            }
        }
        return neededPermissions;
    }

    public static void requestPermissions(Activity activity, List<String> permissions) {
        ActivityCompat.requestPermissions(activity, permissions.toArray(new String[permissions.size()]), REQUEST_CODE);
    }
}
