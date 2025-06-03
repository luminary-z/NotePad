package com.example.notepad;

import static android.app.Activity.RESULT_OK;

import android.app.ListActivity;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.SharedPreferences;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class Content extends ListActivity {
    private static final int EDIT_REQUEST_CODE = 1; // 编辑请求码
    private static final String PREFS_NAME = "NotePrefs";
    private static final String NOTES_KEY = "notes";
    private ArrayList<HashMap<String, String>> listItems; // 存储笔记数据
    private SimpleAdapter listItemAdapter; // 列表适配器
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        progressBar = findViewById(R.id.progressBar); // 获取ProgressBar

        // 初始化时加载保存的数据
        listItems = loadNotes();

        // 设置列表为空时显示的视图
        ListView listView = getListView();
        View emptyView = findViewById(android.R.id.empty);
        listView.setEmptyView(emptyView);

        // 初始化列表视图
        initListView();

        // 添加按钮点击事件
        Button addButton = findViewById(R.id.add);
        addButton.setOnClickListener(v -> {
            showProgressBar();// 显示加载进度条
            // 使用延迟模拟加载过程
            new android.os.Handler().postDelayed(() -> {
                Intent intent = new Intent(Content.this, EditActivity.class);
                startActivityForResult(intent, EDIT_REQUEST_CODE);
                hideProgressBar();// 隐藏进度条
            }, 500); // 0.5秒延迟
        });

        // 列表项点击监听事件（点击列表进入编辑笔记）
        listView.setOnItemClickListener((parent, view, position, id) -> {
            showProgressBar();
            new android.os.Handler().postDelayed(() -> {
                // 获取选中的笔记数据
                HashMap<String, String> selectedItem = listItems.get(position);
                Intent intent = new Intent(Content.this, EditActivity.class);
                // 传递笔记内容、时间和位置
                intent.putExtra("content", selectedItem.get("ItemTitle"));
                intent.putExtra("time", selectedItem.get("Time"));
                intent.putExtra("position", position);
                startActivityForResult(intent, EDIT_REQUEST_CODE);
                hideProgressBar();
            }, 500); // 0.5秒延迟
        });


        // 列表项长按监听（用于删除笔记）
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            // 显示确认删除对话框
            new AlertDialog.Builder(Content.this)
                    .setTitle("删除笔记")
                    .setMessage("确定要删除这条笔记吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        listItems.remove(position);// 从列表中移除
                        saveNotes(); // 删除后立即保存更改
                        listItemAdapter.notifyDataSetChanged();// 更新列表
                    })
                    .setNegativeButton("取消", null)
                    .show();
            return true;
        });

    }

    // 初始化列表视图
    private void initListView() {
        // 创建SimpleAdapter适配器
        listItemAdapter = new SimpleAdapter(this, listItems, R.layout.list_item,
                new String[]{"ItemTitle", "Time"},
                new int[]{R.id.itemTitle, R.id.time}) {

            // 重写setViewText处理文本显示
            @Override
            public void setViewText(TextView v, String text) {
                if (v.getId() == R.id.itemTitle) {
                    // 标题处理：确保单行显示，超长省略
                    v.setSingleLine(true);
                    v.setEllipsize(TextUtils.TruncateAt.END);
                }
                super.setViewText(v, text);
            }
        };

        setListAdapter(listItemAdapter);// 设置列表适配器，将适配器绑定到ListView
        getListView().setDividerHeight(1);  // 设置列表项分割线高度
    }

    // 进度条控制

    // 显示进度条
    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        getListView().setVisibility(View.GONE);
    }

    // 隐藏进度条
    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        getListView().setVisibility(View.VISIBLE);
    }


    // 使用SharedPreferences持久化存储笔记数据
    private void saveNotes() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        try{
            // 将笔记列表转换为JSON格式保存
            JSONArray jsonArray = new JSONArray();
            for (HashMap<String, String> note : listItems) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("title", note.get("ItemTitle"));
                jsonObject.put("time", note.get("Time"));
                jsonArray.put(jsonObject);
            }
            editor.putString(NOTES_KEY, jsonArray.toString());
            editor.apply();// 异步提交保存
        } catch (JSONException e) {
            Log.e("SaveNotes", "Error saving notes", e);
        }
    }

    // 从SharedPreferences加载笔记
    private ArrayList<HashMap<String, String>> loadNotes() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String jsonString = preferences.getString(NOTES_KEY, "[]"); // 默认空数组
        ArrayList<HashMap<String, String>> notes = new ArrayList<>();

        try {
            // 解析JSON数据
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                HashMap<String, String> note = new HashMap<>();
                note.put("ItemTitle", jsonObject.optString("title", ""));
                note.put("Time", jsonObject.optString("time", ""));
                notes.add(note);
            }
        } catch (JSONException e) {
            Log.e("LoadNotes", "Error loading notes", e);
        }
        return notes;
    }


    // 处理编辑活动返回的结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Content.EDIT_REQUEST_CODE && resultCode == RESULT_OK) {
            // 获取返回的数据
            String content = data.getStringExtra("content");
            String time = data.getStringExtra("time");
            int position = data.getIntExtra("position", -1);

            HashMap<String, String> map = new HashMap<>();
            map.put("ItemTitle", content);
            map.put("Time", time);

            if (position == -1) {
                // 新增项目
                listItems.add(map);
            } else {
                // 更新现有项目
                listItems.set(position, map);
            }
            listItemAdapter.notifyDataSetChanged();// 更新列表显示
        }
    }

    // 生命周期管理，确保数据不丢失
    @Override
    protected void onPause() {
        super.onPause();
        saveNotes(); // 在Activity失去焦点时保存
    }

}

