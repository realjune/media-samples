package wu.a.floating;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.android.pictureinpicture.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangjx
 * @version 1.0.0
 * @date 2023/6/9
 */
public class FloatingViewService extends Service {
    private final static String TAG = "FloatingViewService";

    //用于在线程中创建或移除悬浮窗
    private Handler mh=new Handler();
    private WindowManager mWindowManager;
    private View mFloatingView;

    public FloatingViewService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        boolean home = isHome();
        Log.d("dddd", "onCreate home = " + home );
        initView();
    }

    private boolean isHome(){
        ActivityManager mActivityManager=(ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> rti=mActivityManager.getRunningTasks(1);
        return getHomes().contains(rti.get(0).topActivity.getPackageName());
    }

    //获取属于桌面的应用包名称
    private List<String> getHomes(){
        List<String> names=new ArrayList<String>();
        PackageManager packManager=this.getPackageManager();
        Intent intent=new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo=packManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri:resolveInfo) {
            names.add(ri.activityInfo.packageName);
        }
        return names;
    }

    private void initView() {
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_view, null);
        //设置WindowManger布局参数以及相关属性
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        if (Build.VERSION.SDK_INT < 19) {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
//        params.format = PixelFormat.TRANSPARENT;

        // 初始化位置
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 10;
        params.y = 100;
        // 获取WindowManager对象
        mWindowManager = (WindowManager) getApplication().getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingView, params);
        // 关闭FloatingView
        View closeBtn = mFloatingView.findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean home = isHome();
                Log.d("dddd", "close home = " + home );
                stopSelf();
            }
        });
        //录制按钮
        View screenBtn = mFloatingView.findViewById(R.id.screen_btn);
        screenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(FloatingViewService.this, "点击录制", Toast.LENGTH_LONG).show();
            }
        });

        //FloatingView的拖动事件
        mFloatingView.findViewById(R.id.floating_container).setOnTouchListener(new View.OnTouchListener() {
            //获取X坐标
            private int startX;
            //获取Y坐标
            private int startY;
            //初始化X的touch坐标
            private float startTouchX;
            //初始化Y的touch坐标
            private float startTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = params.x;
                        startY = params.y;
                        startTouchX = event.getRawX();
                        startTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = startX + (int) (event.getRawX() - startTouchX);
                        params.y = startY + (int) (event.getRawY() - startTouchY);
                        //更新View的位置
                        mWindowManager.updateViewLayout(mFloatingView, params);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 移除FloatingView
        if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
    }

    public static boolean canPermission(Context context) {
        return Settings.canDrawOverlays(context);
    }

    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 10010;

    public static boolean checkPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                Toast.makeText(context, "需要悬浮窗权限", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context.getPackageName()));
                List<ResolveInfo> infos = context.getPackageManager().queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
                if (infos == null || infos.isEmpty()) {
                    return true;
                }
//                startActivity(intent);
                if (!(context instanceof Activity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                } else {
                    ((Activity) context).startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
                }
                return false;
            }
        }
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                Log.i(TAG, "onActivityResult granted");
            }
        }
    }
}