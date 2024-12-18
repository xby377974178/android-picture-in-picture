package com.example.basic;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;


public abstract class TRTCBaseActivity extends AppCompatActivity {

    protected static final int REQ_PERMISSION_CODE = 0x1000;
    protected int mGrantedCount = 0;

    protected abstract void onPermissionGranted();

    protected boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<>();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (PackageManager.PERMISSION_GRANTED != ActivityCompat
                        .checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)) {
                    Log.e("xbo", "checkPermission: add BLUETOOTH_SCAN");
                    permissions.add(Manifest.permission.BLUETOOTH_SCAN);
                }
                if (PackageManager.PERMISSION_GRANTED != ActivityCompat
                        .checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE)) {
                    Log.e("xbo", "checkPermission: add BLUETOOTH_ADVERTISE");
                    permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE);
                }
                if (PackageManager.PERMISSION_GRANTED != ActivityCompat
                        .checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)) {
                    Log.e("xbo", "checkPermission: add BLUETOOTH_CONNECT");
                    permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
                }
            }

            if (PackageManager.PERMISSION_GRANTED != ActivityCompat
                    .checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat
                    .checkSelfPermission(this, Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat
                    .checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat
                    .checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }


            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(TRTCBaseActivity.this, permissions.toArray(new String[0]),
                        REQ_PERMISSION_CODE);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION_CODE:
                for (int ret : grantResults) {
                    if (PackageManager.PERMISSION_GRANTED == ret) {
                        mGrantedCount++;
                    }
                }

                Log.e("xbo", "onRequestPermissionsResult: mGrantedCount = " + mGrantedCount + " permissions.length = " + permissions.length);

//                if (mGrantedCount == permissions.length) {
                    onPermissionGranted();
//                } else {
//                    Toast.makeText(this, getString(R.string.common_please_input_roomid_and_userid), Toast.LENGTH_SHORT)
//                            .show();
//                }
                mGrantedCount = 0;
                break;
            default:
                break;
        }
    }
}
