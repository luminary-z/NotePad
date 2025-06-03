package com.example.notepad;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Date;
import java.util.Locale;

public class EditActivity extends Activity {
    private EditText editText;
    private Button saveButton;
    private boolean hasUnsavedChanges = false; // 未保存更改标志
    private String originalContent = ""; // 原始内容


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置进入动画
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_edit);

        setContentView(R.layout.activity_edit);
        editText = findViewById(R.id.editText);
        saveButton = findViewById(R.id.saveButton);

        // 如果是编辑现有项，预填内容
        if (getIntent().hasExtra("content")) {
            originalContent = getIntent().getStringExtra("content");
            editText.setText(originalContent);
        }

        // 设置文本变化监听器
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 检测内容变更，设置未保存标志
                hasUnsavedChanges = !s.toString().equals(originalContent);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        // 保存按钮事件
       saveButton.setOnClickListener(v -> saveAndReturn());
    }

    public void finish() {
        super.finish();
        // 设置退出动画
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    // 保存并返回
    private void saveAndReturn() {
        String content = editText.getText().toString();
        // 获取当前时间作为时间戳
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(new Date());

        // 返回数据给Content
        Intent resultIntent = new Intent();
        resultIntent.putExtra("content", content);
        resultIntent.putExtra("time", currentTime);
        resultIntent.putExtra("position", getIntent().getIntExtra("position", -1));
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    // 显示未保存更改对话框
    private void showUnsavedChangesDialog() {
        new AlertDialog.Builder(this)
                .setTitle("未保存的更改")
                .setMessage("您有未保存的更改，是否要保存？")
                .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveAndReturn();
                    }
                })
                .setNegativeButton("不保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishWithoutSaving();
                    }
                })
                .setNeutralButton("取消", null)
                .show();
    }

    // 不保存直接返回
    private void finishWithoutSaving() {
        setResult(RESULT_CANCELED);
        finish();
    }
    // 返回键处理
    public void onBackPressed() {
        // 未保存更改提示
        if (hasUnsavedChanges) {
            showUnsavedChangesDialog();// 三按钮对话框（保存/不保存/取消）
        } else {
            super.onBackPressed();
        }
    }

}