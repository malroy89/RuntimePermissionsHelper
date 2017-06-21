package ru.malroy.requestpermissionhelper;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;

/**
 * Represents a delegate which could be used to integrate Runtime Permissions support to any Activity.
 *
 * Created by Dzmitry Lamaka on 27.01.2016.
 */
public final class PermissionsDelegate {
    /**
     * Request codes for permission requests can be in a range from 0 to 255.
     *
     * @see FragmentActivity#validateRequestPermissionsRequestCode(int)
     */
    @VisibleForTesting
    static final int PERMISSIONS_REQUEST_CODE = 0xff;
    @NonNull
    private static final String STATE_IS_IN_PERMISSION = "ru.malroy.permissionshelper.IS_IN_PERMISSION";
    @NonNull
    private final PermissionsDelegateCallback callback;
    private boolean isCheckRequired = true;
    private boolean isInPermission;

    public PermissionsDelegate(@NonNull final PermissionsDelegateCallback callback) {
        this.callback = callback;
    }

    /**
     * Called from {@link Activity#onCreate(Bundle)}.
     */
    @UiThread
    public void onCreate(@NonNull final Bundle savedInstanceState) {
        isInPermission = savedInstanceState.getBoolean(STATE_IS_IN_PERMISSION);
    }

    /**
     * Called from {@link Activity#onSaveInstanceState(Bundle)}.
     */
    @UiThread
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        outState.putBoolean(STATE_IS_IN_PERMISSION, isInPermission);
    }

    /**
     * Called from {@link Activity#onResume()}. The main reason is to handle an edge case when user
     * pauses our app, switches to Settings, denies a permission, and then resumes our app.
     */
    @UiThread
    public void onResume(@NonNull final Activity activity) {
        if (isCheckRequired) {
            forceCheckPermissions(activity);
            isCheckRequired = false;
        }
    }

    private void forceCheckPermissions(@NonNull final Activity activity) {
        if (isInPermission) {
            return;
        }
        String[] permissionsToGrant = callback.permissionsToGrant();
        if (lackPermissions(activity, permissionsToGrant)) {
            if (callback.rationaleShouldBeShown() && shouldShowRequestPermissionsRationale(activity, permissionsToGrant)) {
                callback.showRationale();
            } else {
                requestPermissions(activity, permissionsToGrant);
            }
        }
    }

    @UiThread
    public void requestPermissions(@NonNull final Activity activity) {
        String[] permissionsToGrant = callback.permissionsToGrant();
        requestPermissions(activity, permissionsToGrant);
    }

    /**
     * Called from {@link Activity#onRequestPermissionsResult(int, String[], int[])}. Here we check
     * if all needed permissions granted and call appropriate callbacks.
     */
    @UiThread
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (isAllPermissionsGranted(grantResults)) {
                callback.permissionsGranted();
            } else {
                callback.permissionsDenied();
            }
            isInPermission = false;
        }
    }

    private void requestPermissions(@NonNull final Activity activity, @NonNull final String... permissionsToGrant) {
        isInPermission = true;
        ActivityCompat.requestPermissions(activity, permissionsToGrant, PERMISSIONS_REQUEST_CODE);
    }

    private boolean lackPermissions(@NonNull final Activity activity, @NonNull final String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldShowRequestPermissionsRationale(@NonNull final Activity activity, @NonNull final String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAllPermissionsGranted(@NonNull final int[] grantsResult) {
        for (int result : grantsResult) {
            if (result == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    public interface PermissionsDelegateCallback {
        /**
         * Returns permissions which are needed in the scope of the current Activity.
         */
        @NonNull
        String[] permissionsToGrant();

        /**
         * Called when all needed permissions have been granted.
         */
        void permissionsGranted();

        /**
         * Called when at least one of the needed permissions has been denied.
         */
        void permissionsDenied();

        /**
         * Indicates that rationale should/shouldn't be shown.
         */
        boolean rationaleShouldBeShown();

        /**
         * Called when rationale should be shown. A client decides how it should look like.
         */
        void showRationale();

    }

}
