package com.moms.babysounds.common;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
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
import android.util.Base64;
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

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.security.MessageDigest;
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


    public static void focusOutView(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            activity.getCurrentFocus().clearFocus();

            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(new View(activity).getWindowToken(), 0);
        }
    }

//    /**
//     * 프로그래스 on
//     *
//     * @param activity
//     * @param message
//     */
//    public static void progressOn(Activity activity, @Nullable String message) {
//
//        focusOutView(activity);
//
//        if (activity == null || activity.isFinishing()) {
//            return;
//        }
//        if (progressDialog != null && progressDialog.isShowing()) {
//            progressSET(message);
//        } else {
//            progressDialog = new Dialog(activity);
//            progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//            progressDialog.setContentView(R.layout.progress_loading);
//            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//            progressDialog.setCancelable(true);
//            progressDialog.show();
//        }
//        final ImageView img_loading_frame = (ImageView) progressDialog.findViewById(R.id.iv_frame_loading);
//        final AnimationDrawable frameAnimation = (AnimationDrawable) img_loading_frame.getBackground();
//        img_loading_frame.post(new Runnable() {
//            @Override
//            public void run() {
//                frameAnimation.start();
//            }
//        });
//        TextView tv_progress_message = progressDialog.findViewById(R.id.tv_progress_message);
//        if (!TextUtils.isEmpty(message)) {
//            tv_progress_message.setText(message);
//        }
//
//    }
//
//    private static void progressSET(String message) {
//        if (progressDialog == null || !progressDialog.isShowing()) {
//            return;
//        }
//        TextView tv_progress_message = (TextView) progressDialog.findViewById(R.id.tv_progress_message);
//        if (!TextUtils.isEmpty(message)) {
//            tv_progress_message.setText(message);
//        }
//    }

    /**
     * 프로그래스 off
     */
    public static void progressOff() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    /**
     * <h3>hideKeyboard()</h3>
     * <p>
     * This method is used to hide keyboard
     * </p>
     */
    public static void hideKeyboard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * 다이얼로그
     */
    public static void defaultDialog(Activity activity, @Nullable String title, String message, boolean cancelAble,
                                     @Nullable String positiveButtonText, @Nullable DialogInterface.OnClickListener positiveClickListener,
                                     @Nullable String negativeButtonText, @Nullable DialogInterface.OnClickListener negativeClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        if (title != null) {
            builder.setTitle(title);
        }
        builder.setMessage(message);
        builder.setCancelable(cancelAble);

        if (positiveButtonText != null) {
            builder.setPositiveButton(positiveButtonText, positiveClickListener);
        }
        if (negativeButtonText != null) {
            builder.setNegativeButton(negativeButtonText, negativeClickListener);
        }
        builder.show();
    }


    /**
     * <h2>validateTime</h2>
     * <p>
     * method to validate that whether the selected time
     * is greater than the current time or not
     * </p>
     *
     * @param current:  device current time
     * @param selected: selected time
     * @return boolean: true is selected time is greater than the current time
     */
    public static boolean validateTime(long current, long selected) {
        Log.d(TAG, "validateTime: " + "current:" + current + "selected:" + selected);
        return selected > current;
    }

    public static void editTextFocusOnView(Activity activity, EditText editText) {
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();

        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(editText, 0);
    }

    /**
     * <h2>isNetworkAvailable</h2>
     * <p>
     * This method is used for checking internet connection
     * </P>
     *
     * @param context current context.
     * @return boolean value.
     */
    public static boolean isNetworkAvailable(Context context) {

        ConnectivityManager connectivity;
        boolean isNetworkAvail = false;
        try {
            connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null) {
                    for (NetworkInfo anInfo : info)
                        if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isNetworkAvail;
    }

    /**
     * 본인 핸드폰 번호 가져오기
     * @param context
     * @return
     */
    public static String getPhoneNumber(Context context) {
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNum = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //마시멜로우 이상인지 체크
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            }
        }

        String phoneNumber = telManager.getLine1Number();
        Log.d(TAG, "getPhoneNumber: " + phoneNumber);
        if (phoneNumber.startsWith("+82")) {
            phoneNum = phoneNumber.replace("+82", "0");
        } else {
            phoneNum = phoneNumber;
        }

        return phoneNum;

    }

    public static void gpsServiceCheck(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        //GPS가 켜져있는지 체크
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //GPS 설정화면으로 이동
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            context.startActivity(intent);
        }
    }


    /**
     * 퍼미션 체크 메소드
     *
     * @param permissionCheckList     //     * @param descriptionMessage      "핸드폰번호와 인증번호를 자동으로 가져오려면 이 권한이 필요합니다."
     *                                //     * @param deniedMessage           "해당 권한을 거부하면 이 서비스를 이용할 수 없습니다.\n- 권한 승인 변경 방법\n[설정] > [애플리케이션] > [담너머] \n> [권한] > 모두 허용"
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
//                            .setRationaleMessage(descriptionMessage)
                                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                                .setPermissions(permissionCheckList)
                                .check();
                    } else {
                        TedPermission.with(context)
                                .setPermissionListener(permissionlistener)
//                            .setRationaleMessage(descriptionMessage)
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

    public static String getHashKey(Context context) {
        final String TAG = "KeyHash";
        String keyHash = null;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                keyHash = new String(Base64.encode(md.digest(), 0));
                Log.d(TAG, keyHash);
            }
        } catch (Exception e) {
            Log.e("name not found", e.toString());
        }

        if (keyHash != null) {
            return keyHash;
        } else {
            return null;
        }
    }

    public interface PermissionCheckResponseImpl {
        void granted();

        void denied();
    }

    public static Drawable setVectorForPreLollipop(int resourceId, Context activity) {
        Drawable icon;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            icon = VectorDrawableCompat.create(activity.getResources(), resourceId, activity.getTheme());
        } else {
            icon = activity.getResources().getDrawable(resourceId, activity.getTheme());
        }

        return icon;
    }

    /**
     * 버전코드 가져오기
     *
     * @param context
     * @return
     */
    public static String getVersionValue(Context context) {
        PackageInfo pi = null;
        try {
            pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

//        int versionCode = pi.versionCode;
        String versionName = pi.versionName;

        return versionName;
    }

    public int calcImageSize(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = activity.getWindowManager().getDefaultDisplay();
        display.getMetrics(metrics);
        return Math.min(Math.max(metrics.widthPixels, metrics.heightPixels), 2048);
    }

    /**
     * URI를 Filepath로 변환
     */
    public static String uriToFilePath(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToNext();
        String path = cursor.getString(cursor.getColumnIndex("_data"));
        cursor.close();
        return path;
    }


    public static String getPathFromUri(Context context, Uri uri) {

        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        cursor.moveToNext();

        String path = cursor.getString(cursor.getColumnIndex("_data"));

        cursor.close();
        return path;

    }

    public static Uri getUriFromPath(Context context, String path) {

        String fileName = "file://" + path;

        Uri fileUri = Uri.parse(fileName);

        String filePath = fileUri.getPath();

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,

                null, "_data = '" + filePath + "'", null, null);

        cursor.moveToNext();

        int id = cursor.getInt(cursor.getColumnIndex("_id"));

        Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);


        return uri;

    }

    public static boolean isClickAble() {
        if (SystemClock.elapsedRealtime() - lastButtonClickTime < 500) {
            lastButtonClickAble = false;
        } else {
            lastButtonClickAble = true;
        }
        lastButtonClickTime = SystemClock.elapsedRealtime();
        return lastButtonClickAble;
    }

    public static String formattedStringPrice(long price) {
        DecimalFormat myFormatter = new DecimalFormat("###,###,###");
        String formattedStringPrice = myFormatter.format(price);

        return formattedStringPrice;
    }

    public static String getTime(long second) {
        long hour = second / 3600;
        long min = (second % 3600 / 60);
        if (hour == 0) {
            return min + "분";
        } else {
            return hour + "시간 " + min + "분";
        }
    }

    public static String getKm(Context context, double m) {
        double km = (m / 1000);
        return String.format("%.1f", km) + "km";

    }

    public static Fragment getNowFragment(FragmentManager fragmentManager) {
        Fragment rtnfragment = null;
        for (Fragment fragment : fragmentManager.getFragments()) {
            if (fragment.isVisible()) {
                rtnfragment = fragment;
            }
            break;
        }
        return rtnfragment;
    }

    public static void quitFragment(FragmentActivity activity, int stack) {
        List<Fragment> fragments = activity.getSupportFragmentManager().getFragments();
        activity.getSupportFragmentManager().beginTransaction().remove(fragments.get(fragments.size() - 1 - stack)).commit();

    }

    public static String makeCardNumber(String cardNumber) {
        String firstCardNumber = cardNumber.substring(0, 4);
        String endCardNumber = cardNumber.substring(12);
        String cardNumberData = firstCardNumber + " **** " + endCardNumber;
        return cardNumberData;
    }


}
