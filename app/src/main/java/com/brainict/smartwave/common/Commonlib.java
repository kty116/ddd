package com.brainict.smartwave.common;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.brainict.smartwave.activity.MainActivity;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.text.DecimalFormat;
import java.util.List;

public class Commonlib {

    public static final String SHIPPING_DATA = "shipping_data";
    public static final String RECENT_ADDRESS_DATA = "recent_address_data";
    public static final String FAVORITE_ADDRESS_DATA = "favorite_address_data";


    public static String TOOLBAR_TITLE = "toolbar_title";
    public static String RESULT_ADDRESS = "result_address";

    public static final int GET_START_ADDRESS = 1000;
    public static final int GET_STOP_ADDRESS = 1001;
    public static final int FAVORITE_DIALOG = 1002;
    public static final int MIC_BUTTON_CLICK = 1003;


    public static final String TAG = Commonlib.class.getSimpleName();
    private static Dialog progressDialog;
    private static Dialog AletDialog;
    private static long lastButtonClickTime;
    private static boolean lastButtonClickAble;

    /**
     * 퍼미션 체크 메소드
     *
     * @param context
     * @param permissionCheckList
     * @param isDeniedMessage
     * @param permissionCheckResponse
     */

    public static void permissionCheck(final Context context,
                                       final String[] permissionCheckList, boolean isDeniedMessage,
                                       final PermissionCheckResponseImpl permissionCheckResponse) {
        boolean isPermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //마시멜로우 이상인지 체크


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //마시멜로우 이상인지 체크

                int[] permissionChecks = new int[permissionCheckList.length];

                for (int i = 0; i < permissionChecks.length; i++) {
                    permissionChecks[i] = ContextCompat.checkSelfPermission(context, permissionCheckList[i]);
                    if (permissionChecks[i] == PackageManager.PERMISSION_DENIED) {
                        isPermission = false;
                    }
                }

                if (!isPermission) {

                    PermissionListener permissionlistener = new PermissionListener() {
                        @Override
                        public void onPermissionGranted() {
                            permissionCheckResponse.granted();
                        }

                        @Override
                        public void onPermissionDenied(List<String> deniedPermissions) {
                            permissionCheckResponse.denied();
                        }
                    };

                    if (isDeniedMessage) {

                        TedPermission.with(context)
                                .setPermissionListener(permissionlistener)
                                .setDeniedMessage("요청 권한을 거부하면 이 서비스를 이용할 수 없습니다.\n\n [설정] > [권한]에서 해당 권한을 활성화 해주세요.")
                                .setPermissions(permissionCheckList)
                                .check();
                    } else {
                        TedPermission.with(context)
                                .setPermissionListener(permissionlistener)
                                .setPermissions(permissionCheckList)
                                .check();
                    }
                } else {
                    permissionCheckResponse.granted();
                }
            }

        } else {
            permissionCheckResponse.granted();
        }
    }

    public interface PermissionCheckResponseImpl {
        void granted();

        void denied();
    }

    public static String getRunActivity(Context context)   {

        ActivityManager activity_manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> task_info = activity_manager.getRunningTasks(9999);

        StringBuilder result = new StringBuilder();
        for (ActivityManager.RunningTaskInfo activity : task_info){
            result.append(activity.topActivity.getClassName());
        }
        return result.toString();
    }

}
