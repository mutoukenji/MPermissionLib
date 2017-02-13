package me.yaog.mpermissionlib;

import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Android 6.0 权限管理通用Activity
 * @author mutoukenji
 * @version 1.0
 */

public class MActivity extends AppCompatActivity {

    protected SparseArray<PermissionRequest> requestList = new SparseArray<>();

    /**
     * 请求权限.
     * 请求权限，在 Android M 及以上系统中，会弹出权限确认窗口，在之前的版本中，直接返回 true.
     * 建议同一类请求放在一起请求一次，比如将
     * {@code android.Manifest.permission.WRITE_EXTERNAL_STORAGE}
     * 和 {@code android.Manifest.permission.READ_EXTERNAL_STORAGE} 组成一组.
     * @param requestCode   请求ID，可以随意定义
     * @param permissions   请求的权限内容，{@linkplain android.Manifest.permission permission}
     *                      中的定义项
     * @param callbackEvent 异步回调接口，在 Android M 及以上系统中，对于原先未获得权限的请求，
     *                      在用户授予权限后，触发
     *                      {@linkplain PermissionEvent#permissionGranted() permissionGranted()}
     *                      事件，在用户拒绝权限后，触发
     *                      {@linkplain PermissionEvent#permissionDenied(String[])
     *                      permissionDenied(String[] permissions)} 事件
     * @return 是否已获取权限，已获取的返回 true ，尚未获得的返回 false ，在 Android M 及以上系统中，
     * 在返回值为 false 的时候，还应等待回调事件，根据回调事件判断是否获取了权限
     */
    @SuppressWarnings({"SimplifiableIfStatement", "unused"})
    protected boolean requirePermissions(int requestCode, @NonNull String[] permissions,
                                         PermissionEvent callbackEvent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkPermission(permissions, requestCode, callbackEvent);
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkPermission(@NonNull String[] permissions, int requestCode,
                                    PermissionEvent callbackEvent) {
        boolean hasPermission = true;
        List<String> lackPermissions = new ArrayList<>();
        for (String permission : permissions) {
            int permissionState = checkSelfPermission(permission);
            if (permissionState != PackageManager.PERMISSION_GRANTED) {
                hasPermission = false;
                lackPermissions.add(permission);
            }
        }
        if (!lackPermissions.isEmpty()) {
            PermissionRequest request = new PermissionRequest();
            request.callbackEvent = callbackEvent;
            request.permissions = lackPermissions.toArray(new String[lackPermissions.size()]);
            requestList.put(requestCode, request);
            requestPermissions(request.permissions, requestCode);
        }
        return hasPermission;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestList.indexOfKey(requestCode) >= 0) {
            PermissionRequest request = requestList.get(requestCode);
            requestList.remove(requestCode);
            if (request.callbackEvent != null) {
                List<String> deniedList = new ArrayList<>();
                for (String requestPermission : request.permissions) {
                    int permissionIndex = -1;
                    for (int i = 0; i < permissions.length; i++) {
                        String permission = permissions[i];
                        if (permission.equals(requestPermission)) {
                            permissionIndex = i;
                            break;
                        }
                    }
                    if (permissionIndex >= 0) {
                        int permissionState = grantResults[permissionIndex];
                        if (permissionState == PackageManager.PERMISSION_DENIED) {
                            deniedList.add(requestPermission);
                        }
                    } else {
                        Log.e("RequestPermission", "Permission "
                                + requestPermission + " has no result!");
                    }
                }
                if (deniedList.isEmpty()) {
                    request.callbackEvent.permissionGranted();
                } else {
                    request.callbackEvent
                            .permissionDenied(deniedList.toArray(new String[deniedList.size()]));
                }
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 权限请求回调接口.
     * 在 Android M 及以上系统中会触发
     */
    public interface PermissionEvent {
        /**
         * 成功获取权限
         */
        void permissionGranted();

        /**
         * 用户拒绝授予权限
         *
         * @param permissions 被拒绝授予的权限
         */
        void permissionDenied(String[] permissions);
    }

    private class PermissionRequest {
        String[] permissions;
        PermissionEvent callbackEvent;
    }
}
