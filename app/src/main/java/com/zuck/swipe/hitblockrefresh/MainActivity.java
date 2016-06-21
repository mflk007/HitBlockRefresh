package com.zuck.swipe.hitblockrefresh;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zuck.swipe.hitblockrefresh.view.FunGameRefreshView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FunGameRefreshView refreshView;


    private List<String> dataList;

    private RecyclerView rvTest;

    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataList = createDate();
        refreshView = (FunGameRefreshView) findViewById(R.id.refresh_fun_game);

        rvTest = (RecyclerView) findViewById(R.id.rvTest);

        adapter = new MyAdapter();
        adapter.mData = dataList;
        rvTest.setLayoutManager(new LinearLayoutManager(this));
        rvTest.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        refreshView.setOnRefreshListener(new FunGameRefreshView.FunGameRefreshListener() {
            @Override
            public void onRefreshing() {
                try {
                    // 模拟网络请求耗时动作
                    Thread.sleep(3000);
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
            dataList.add("X");
            adapter.notifyDataSetChanged();
            refreshView.finishRefreshing();
        }
    };

    private List<String> createDate() {
        ArrayList list = new ArrayList<>();
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");
        list.add("E");
        list.add("F");
        list.add("G");
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");
        list.add("E");
        list.add("F");
        list.add("G");
        return list;
    }


    public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        public List<String> mData = new ArrayList<>();
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(MainActivity.this).inflate(R.layout.item_test, parent,false));
            return holder;
        }
        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.tvTest.setText(dataList.get(position));
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTest;
        public MyViewHolder(View itemView) {
            super(itemView);
            tvTest = (TextView) itemView.findViewById(R.id.tvTest);
        }
    }
}
