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
    private static final int EDIT_REQUEST_CODE = 1;
    private static final String PREFS_NAME = "NotePrefs";
    private static final String NOTES_KEY = "notes";
    private ArrayList<HashMap<String, String>> listItems;
    private SimpleAdapter listItemAdapter;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        progressBar = findViewById(R.id.progressBar); // 获取ProgressBar

        // 初始化时加载保存的数据
        listItems = loadNotes();

        ListView listView = getListView();
        View emptyView = findViewById(android.R.id.empty);
        listView.setEmptyView(emptyView);

        initListView();

        Button addButton = findViewById(R.id.add);
        addButton.setOnClickListener(v -> {
            showProgressBar();
            // 使用postDelayed模拟加载延迟（实际项目中可以替换为真实操作）
            new android.os.Handler().postDelayed(() -> {
                Intent intent = new Intent(Content.this, EditActivity.class);
                startActivityForResult(intent, EDIT_REQUEST_CODE);
                hideProgressBar();
            }, 500); // 0.5秒延迟
        });

        // 设置列表项点击监听（用于编辑）
        listView.setOnItemClickListener((parent, view, position, id) -> {
            showProgressBar();
            // 使用postDelayed模拟加载延迟
            new android.os.Handler().postDelayed(() -> {
                HashMap<String, String> selectedItem = listItems.get(position);
                Intent intent = new Intent(Content.this, EditActivity.class);
                intent.putExtra("content", selectedItem.get("ItemTitle"));
                intent.putExtra("time", selectedItem.get("Time"));
                intent.putExtra("position", position);
                startActivityForResult(intent, EDIT_REQUEST_CODE);
                hideProgressBar();
            }, 500); // 0.5秒延迟
        });


        // 设置列表项长按监听（用于删除）
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            new AlertDialog.Builder(Content.this)
                    .setTitle("删除笔记")
                    .setMessage("确定要删除这条笔记吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        listItems.remove(position);
                        saveNotes(); // 删除后立即保存
                        listItemAdapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("取消", null)
                    .show();
            return true; // 消费长按事件
        });

    }
    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        getListView().setVisibility(View.GONE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        getListView().setVisibility(View.VISIBLE);
    }

    // 保存笔记到SharedPreferences
    private void saveNotes() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        try{
            JSONArray jsonArray = new JSONArray();
            for (HashMap<String, String> note : listItems) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("title", note.get("ItemTitle"));
                jsonObject.put("time", note.get("Time"));
                jsonArray.put(jsonObject);
            }
            editor.putString(NOTES_KEY, jsonArray.toString());
            editor.apply();
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

    @Override
    protected void onPause() {
        super.onPause();
        saveNotes(); // 在Activity失去焦点时保存
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Content.EDIT_REQUEST_CODE && resultCode == RESULT_OK) {
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
            listItemAdapter.notifyDataSetChanged();
        }
    }

    private void initListView() {
        //listItems = new ArrayList<>();
        listItemAdapter = new SimpleAdapter(this, listItems, R.layout.list_item,
                new String[]{"ItemTitle", "Time"},
                new int[]{R.id.itemTitle, R.id.time}) {

            // 重写setViewText处理文本显示
            @Override
            public void setViewText(TextView v, String text) {
                if (v.getId() == R.id.itemTitle) {
                    // 标题处理：确保单行显示
                    v.setSingleLine(true);
                    v.setEllipsize(TextUtils.TruncateAt.END);
                }
                super.setViewText(v, text);
            }
        };

        setListAdapter(listItemAdapter);
        // 设置列表项固定高度
        getListView().setDividerHeight(1);  // 设置分割线高度
    }
}

