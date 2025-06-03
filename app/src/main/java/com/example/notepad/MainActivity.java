package com.example.notepad;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 获取进入按钮并设置点击事件
        Button enter = findViewById(R.id.enter);
        enter.setOnClickListener(v -> {
            // 跳转到内容列表界面(Content)
            Intent intent = new Intent(MainActivity.this, Content.class);
            startActivity(intent);
        });

    }
}