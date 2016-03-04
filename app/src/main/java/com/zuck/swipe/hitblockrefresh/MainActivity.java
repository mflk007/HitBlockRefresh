package com.zuck.swipe.hitblockrefresh;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.zuck.swipe.hitblockrefresh.view.HitBlockRefreshView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private HitBlockRefreshView refreshView;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        refreshView = (HitBlockRefreshView) findViewById(R.id.refresh_hit_block);

        listView = (ListView) findViewById(R.id.list_view);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, createDate());

        listView.setAdapter(arrayAdapter);
        refreshView.setOnRefreshListener(new HitBlockRefreshView.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    // 模拟网络请求耗时动作
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHandler.sendEmptyMessage(0);
            }
        });
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            refreshView.finishRefreshing();
        }
    };

    private List<String> createDate() {
        List<String> dataList = new ArrayList<>();
        dataList.add("A");
        dataList.add("B");
        dataList.add("C");
        dataList.add("D");
        dataList.add("E");
        dataList.add("F");
        dataList.add("G");
        dataList.add("H");
        dataList.add("L");
        dataList.add("M");
        return dataList;
    }
}
