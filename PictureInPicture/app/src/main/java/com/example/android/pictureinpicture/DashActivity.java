package com.example.android.pictureinpicture;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @author wangjx
 * @version 1.0.0
 * @date 2023/6/8
 */
public class DashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dash_activity);
        findViewById(R.id.normal_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(DashActivity.this, NormalActivity.class);
                startActivity(i);
            }
        });
        findViewById(R.id.pip_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(DashActivity.this, MainActivity.class);
                startActivity(i);
            }
        });
    }
}
