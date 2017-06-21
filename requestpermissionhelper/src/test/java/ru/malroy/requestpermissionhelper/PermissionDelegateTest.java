package ru.malroy.requestpermissionhelper;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TargetApi(Build.VERSION_CODES.M)
@Config(constants = BuildConfig.class, manifest = Config.NONE, sdk = 23)
@RunWith(RobolectricTestRunner.class)
public class PermissionDelegateTest {
    @Mock
    private PermissionsDelegate.PermissionsDelegateCallback permissionsDelegateCallback;
    @Mock
    private Bundle bundle;

    private Activity activity;

    private PermissionsDelegate subject;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        activity = spy(Robolectric.buildActivity(Activity.class).create().get());
        subject = new PermissionsDelegate(permissionsDelegateCallback);
    }

    @Test
    public void onCreate_shouldGetDataFromBundle() throws Exception {
        subject.onCreate(bundle);
        verify(bundle).getBoolean(anyString());
    }

    @Test
    public void onSaveInstanceState_shouldSetDataToBundle() throws Exception {
        subject.onSaveInstanceState(bundle);
        verify(bundle).putBoolean(anyString(), anyBoolean());
    }

    @Test
    public void onResume_whenCheckIsNeeded_whenLackOfPermissions_shouldShowRationale() throws Exception {
        when(permissionsDelegateCallback.permissionsToGrant()).thenReturn(new String[] {Manifest.permission.CAMERA});
        when(permissionsDelegateCallback.rationaleShouldBeShown()).thenReturn(true);
        when(activity.shouldShowRequestPermissionRationale(anyString())).thenReturn(true);
        subject.onResume(activity);
        verify(permissionsDelegateCallback).showRationale();
    }

    @Test
    public void onResume_whenCheckIsNeeded_whenLackOfPermissions_whenRationaleShouldNotBeShown_shouldGrantPermission() throws Exception {
        String[] permissions = new String[] {Manifest.permission.CAMERA};
        when(permissionsDelegateCallback.permissionsToGrant()).thenReturn(permissions);
        when(permissionsDelegateCallback.rationaleShouldBeShown()).thenReturn(false);
        subject.onResume(activity);
        verify(permissionsDelegateCallback, never()).showRationale();
        verify(activity).requestPermissions(eq(permissions), anyInt());
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Test
    public void onResume_whenCheckIsNeeded_whenPermissionsGranted_shouldDoNothing() throws Exception {
        String permission = Manifest.permission.CAMERA;
        when(activity.checkPermission(eq(permission), anyInt(), anyInt())).thenReturn(PackageManager.PERMISSION_GRANTED);
        when(permissionsDelegateCallback.permissionsToGrant()).thenReturn(new String[] {Manifest.permission.CAMERA});
        subject.onResume(activity);
        verify(permissionsDelegateCallback, never()).showRationale();
        verify(activity, never()).requestPermissions(any(String[].class), anyInt());
    }

    @Test
    public void requestPermissions() throws Exception {
        String[] permissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.CALL_PHONE};
        when(permissionsDelegateCallback.permissionsToGrant()).thenReturn(permissions);
        subject.requestPermissions(activity);
        verify(activity).requestPermissions(eq(permissions), anyInt());
    }

    @Test
    public void onRequestPermissionsResult_whenAllPermissionsGranted_shouldCallPermissionsGranted() throws Exception {
        subject.onRequestPermissionsResult(PermissionsDelegate.PERMISSIONS_REQUEST_CODE, new String[] {Manifest.permission.CAMERA},
                new int[] {PackageManager.PERMISSION_GRANTED});
        verify(permissionsDelegateCallback).permissionsGranted();
    }

    @Test
    public void onRequestPermissionsResult_whenOneOfPermissionsIsNotGranted_shouldCallPermissionsDenied() throws Exception {
        subject.onRequestPermissionsResult(PermissionsDelegate.PERMISSIONS_REQUEST_CODE, new String[] {Manifest.permission.CAMERA, Manifest.permission.CALL_PHONE},
                new int[] {PackageManager.PERMISSION_GRANTED, PackageManager.PERMISSION_DENIED});
        verify(permissionsDelegateCallback).permissionsDenied();
    }
}
