package wu.a.pip;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.util.Log;
import android.util.Rational;

import androidx.annotation.DrawableRes;

import com.example.android.pictureinpicture.R;

import java.util.ArrayList;

/**
 * @author wangjx
 * @version 1.0.0
 * @date 2023/6/6
 */
public class PipWrap {
    private static final String TAG = "ss_PipUtil";
    private Activity mActivity;
    /**
     * A {@link BroadcastReceiver} to receive action item events from Picture-in-Picture mode.
     */
    private BroadcastReceiver mReceiver;
    private OnPipListener mOnPipListener;
    /**
     * Intent action for media controls from Picture-in-Picture mode.
     */
    private static final String ACTION_MEDIA_CONTROL = "media_control";

    /**
     * Intent extra for media controls from Picture-in-Picture mode.
     */
    private static final String EXTRA_CONTROL_TYPE = "control_type";

    /**
     * The request code for play action PendingIntent.
     */
    private static final int REQUEST_PLAY = 1;

    /**
     * The request code for pause action PendingIntent.
     */
    private static final int REQUEST_PAUSE = 2;

    /**
     * The request code for info action PendingIntent.
     */
    private static final int REQUEST_INFO = 3;

    /**
     * The intent extra value for play action.
     */
    private static final int CONTROL_TYPE_PLAY = 1;

    /**
     * The intent extra value for pause action.
     */
    private static final int CONTROL_TYPE_PAUSE = 2;

    /**
     * The arguments to be used for Picture-in-Picture mode.
     */
    @SuppressLint("NewApi")
    private final PictureInPictureParams.Builder mPictureInPictureParamsBuilder =
            new PictureInPictureParams.Builder();

    public PipWrap(Activity act) {
        this.mActivity = act;
    }

    public void setOnPipListener(OnPipListener listener) {
        this.mOnPipListener = listener;
    }



    /**
     * 检查是否支持画中画模式
     * 26
     * Android 8 以上 ， 低 RAM 设备可能无法使用画中画模式。在应用使用画中画之前，请务必通过调用 hasSystemFeature 方法
     * false 设备不支持画中画
     *
     * @return
     */
    public static boolean checkCanPip(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return false;
        }
        try {
            boolean isSupportPipMode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE);
            return isSupportPipMode;
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return false;
    }

    /**
     * 判断能否进入画中画模式，大多数判断
     *
     * @param context
     * @return
     */
    public boolean checkCanEnterPiPMode(Activity context) {
        if (!checkCanPip(context)) {
            log("当前设备不支持画中画模式哦");
            return false;
        }

        if (!checkOpenPip(context)) {// 是否打开画中画开关
            log("画中画未开启");
            return false;
        }

        return true;
    }

    /**
     * 检查是否打开了画中画模式
     *
     * @return
     */
    public static boolean checkOpenPip(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return false;
        }
        try {
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            if (AppOpsManager.MODE_ALLOWED == appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    context.getApplicationInfo().uid, context.getPackageName())) {
                //画中画开关开启
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false; //画中画开关关闭
    }

    /**
     * Update the state of pause/resume action item in Picture-in-Picture mode.
     *
     * @param play   The type of the action. either {@link #CONTROL_TYPE_PLAY} or {@link
     *               #CONTROL_TYPE_PAUSE}.
     *               The request code for the {@link PendingIntent}.
     */
    @SuppressLint("NewApi")
    public void updatePlayBtn(boolean play) {

        if (play) {
            int iconId = R.drawable.ic_pause_24dp;
//            int iconId = new ResourcesToolForPlugin(mActivity).getResourceIdForDrawable("tt_new_play_video");
//        iconId = new ResourcesToolForPlugin(MainActivity.this).getResourceIdForDrawable("ic_player_controller_pause");

            String title = ("pause");
            updatePlayBtn(iconId, title, CONTROL_TYPE_PAUSE, REQUEST_PAUSE);
        } else {

            int iconId = R.drawable.ic_play_arrow_24dp;
//            int iconId = new ResourcesToolForPlugin(mActivity).getResourceIdForDrawable("b933_play_icon");
//        iconId = new ResourcesToolForPlugin(MainActivity.this).getResourceIdForDrawable("ic_player_controller_pause");

            String title = ("play");
            updatePlayBtn(iconId, title, CONTROL_TYPE_PLAY, REQUEST_PLAY);
        }
    }


    /**
     * Update the state of pause/resume action item in Picture-in-Picture mode.
     *
     * @param iconId      The icon to be used.
     * @param title       The title text.
     * @param controlType The type of the action. either {@link #CONTROL_TYPE_PLAY} or {@link
     *                    #CONTROL_TYPE_PAUSE}.
     * @param requestCode The request code for the {@link PendingIntent}.
     */
    @SuppressLint("NewApi")
    void updatePlayBtn(@DrawableRes int iconId, String title, int controlType, int requestCode) {
        final ArrayList<RemoteAction> actions = new ArrayList<>();

        // This is the PendingIntent that is invoked when a user clicks on the action item.
        // You need to use distinct request codes for play and pause, or the PendingIntent won't
        // be properly updated.
        final PendingIntent intent =
                PendingIntent.getBroadcast(
                        mActivity,
                        requestCode,
                        new Intent(ACTION_MEDIA_CONTROL).putExtra(EXTRA_CONTROL_TYPE, controlType),
                        0);
        final Icon icon = Icon.createWithResource(mActivity, iconId);
//                Bitmap iconBmp = BitmapFactory.decodeResource(getResources(),  R.drawable.ic_player_controller_pause);
//                Icon icon = Icon.createWithBitmap(iconBmp);

        actions.add(new RemoteAction(icon, title, title, intent));

        // Another action item. This is a fixed action.
//        actions.add(
//                new RemoteAction(
//                        Icon.createWithResource(MainActivity.this, R.drawable.ic_info_24dp),
//                        "info",
//                        "description",
//                        PendingIntent.getActivity(
//                                MainActivity.this,
//                                REQUEST_INFO,
//                                new Intent(
//                                        Intent.ACTION_VIEW,
//                                        Uri.parse("https://peach.blender.org/")),
//                                0)));

        mPictureInPictureParamsBuilder.setActions(actions);

        // This is how you can update action items (or aspect ratio) for Picture-in-Picture mode.
        // Note this call can happen even when the app is not in PiP mode. In that case, the
        // arguments will be used for at the next call of #enterPictureInPictureMode.
        mActivity.setPictureInPictureParams(mPictureInPictureParamsBuilder.build());
    }

    public void onPictureInPictureModeChanged(Activity activity, boolean isInPictureInPictureMode, Configuration newConfig) {
        if (activity != mActivity) {
            return;
        }
        if (isInPictureInPictureMode) {
            // Starts receiving events from action items in PiP mode.
            mReceiver =
                    new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            if (intent == null
                                    || !ACTION_MEDIA_CONTROL.equals(intent.getAction())) {
                                return;
                            }

                            // This is where we are called back from Picture-in-Picture action
                            // items.
                            final int controlType = intent.getIntExtra(EXTRA_CONTROL_TYPE, 0);
                            switch (controlType) {
                                case CONTROL_TYPE_PLAY:
                                    if (mOnPipListener != null) {
                                        mOnPipListener.onPlayAction();
                                    }
                                    break;
                                case CONTROL_TYPE_PAUSE:
                                    if (mOnPipListener != null) {
                                        mOnPipListener.onPauseAction();
                                    }
                                    break;
                            }
                        }
                    };
            mActivity.registerReceiver(mReceiver, new IntentFilter(ACTION_MEDIA_CONTROL));
        } else {
            // We are out of PiP mode. We can stop receiving events from it.
            if (mReceiver != null) {
                mActivity.unregisterReceiver(mReceiver);
                mReceiver = null;
            }
            if (mOnPipListener != null) {
                mOnPipListener.onExitPip();
            }
        }
    }

    public void enterPipMode(Activity activity, int width, int height) {
        this.mActivity = activity;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Rational aspectRatio = new Rational(width, height);
            mPictureInPictureParamsBuilder.setAspectRatio(aspectRatio).build();
            mActivity.enterPictureInPictureMode(mPictureInPictureParamsBuilder.build());
        }
    }

    public void onDestroy(Context context) {
        // We are out of PiP mode. We can stop receiving events from it.
        if (mReceiver != null) {
            mActivity.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        mOnPipListener = null;
    }
    
    public static void log(String msg) {
        Log.d(TAG, msg);
    }

    public interface OnPipListener {

        void onPlayAction();

        void onPauseAction();

        void onExitPip();
    }

}
