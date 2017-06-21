package ru.malroy.permissionshelper;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import ru.malroy.requestpermissionhelper.PermissionsDelegate;

public class MainActivity extends AppCompatActivity implements PermissionsDelegate.PermissionsDelegateCallback {
    @Nullable
    private PermissionsDelegate permissionsDelegate;

    @NonNull
    protected PermissionsDelegate getPermissionsDelegate() {
        if (permissionsDelegate == null) {
            permissionsDelegate = new PermissionsDelegate(this);
        }
        return permissionsDelegate;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            getPermissionsDelegate().onCreate(savedInstanceState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getPermissionsDelegate().onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPermissionsDelegate().onResume(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        getPermissionsDelegate().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @NonNull
    @Override
    public String[] permissionsToGrant() {
        return new String[] { Manifest.permission.CAMERA, Manifest.permission.CALL_PHONE, Manifest.permission.ACCESS_FINE_LOCATION };
    }

    @Override
    public void permissionsGranted() {
        Snackbar.make(findViewById(android.R.id.content), "Permissions were successfully granted", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void permissionsDenied() {
        Snackbar.make(findViewById(android.R.id.content), "One or all of the permissions were denied", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public boolean rationaleShouldBeShown() {
        return true;
    }

    @Override
    public void showRationale() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.permissions_rational_title)
                .setMessage(R.string.permissions_rational_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull final DialogInterface dialog, final int which) {
                        getPermissionsDelegate().requestPermissions(MainActivity.this);
                    }
                }).show();
    }
}
