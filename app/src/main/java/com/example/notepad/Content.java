package com.example.notepad;

import android.app.ListActivity;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

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
    private ArrayList<HashMap<String, String>> listItems;
    private SimpleAdapter listItemAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        ListView listView = getListView();
        View emptyView = findViewById(android.R.id.empty);
        listView.setEmptyView(emptyView);

        initListView();

        Button addButton = findViewById(R.id.add);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(Content.this, EditActivity.class);
            startActivityForResult(intent, EDIT_REQUEST_CODE);
        });

    }

    private void initListView() {
        listItems = new ArrayList<>();
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
        getListView().setDividerHeight(1);  // 可选：设置分割线高度
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_REQUEST_CODE && resultCode == RESULT_OK) {
            String content = data.getStringExtra("content");
            String time = data.getStringExtra("time");

            HashMap<String, String> map = new HashMap<>();
            map.put("ItemTitle", content);
            map.put("Time", time);

            listItems.add(map);
            listItemAdapter.notifyDataSetChanged();
        }
    }
}
