package com.example.android.pictureinpicture;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import wu.a.floating.FloatingViewService;

/**
 * @author wangjx
 * @version 1.0.0
 * @date 2023/6/9
 */
public class ToFloatingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.floating_activity);
        Button startFloatWindow=(Button) findViewById(R.id.btn_floatWindows);
        startFloatWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean enable = FloatingViewService.canPermission(ToFloatingActivity.this);
                if (!enable) {
                    FloatingViewService.checkPermission(ToFloatingActivity.this);
                    return;
                }
                Intent intent=new Intent(ToFloatingActivity.this, FloatingViewService.class);
                startService(intent);
                finish();
            }
        });
    }

//    private void initLayoutParams() {
//        //屏幕宽高
//        int screenWidth = windowManager.getDefaultDisplay().getWidth();
//        int screenHeight = windowManager.getDefaultDisplay().getHeight();
//        //总是出现在应用程序窗口之上。
//        lp.type = WindowManager.LayoutParams.TYPE_PHONE;
//        // FLAG_NOT_TOUCH_MODAL不阻塞事件传递到后面的窗口
//        // FLAG_NOT_FOCUSABLE 悬浮窗口较小时，后面的应用图标由不可长按变为可长按,不设置这个flag的话，home页的划屏会有问题
//        lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
//        //悬浮窗默认显示的位置
//        lp.gravity = Gravity.START | Gravity.TOP;
//        //指定位置
//        lp.x = screenWidth - view.getLayoutParams().width * 2;
//        lp.y = screenHeight / 2 + view.getLayoutParams().height * 2;
//        //悬浮窗的宽高
//        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
//        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        lp.format = PixelFormat.TRANSPARENT;
//        windowManager.addView(this, lp);
//    }
}
