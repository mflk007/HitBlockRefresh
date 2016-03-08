# HitBlockRefresh
有趣好玩的下拉刷新库, 你还记得小时候打的黑白掌上游戏机么？

# Preview

<img src="preview/HitBlockRefresh.gif"/>

# Usage

    布局文件中：
    <com.zuck.swipe.hitblockrefresh.view.HitBlockRefreshView
        android:id="@+id/refresh_hit_block"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:block_horizontal_num="3"
        app:ball_speed="medium">

        <ListView
            android:id="@+id/list_view"
            android:layout_width="fill_parent"
            android:layout_height="fill_parent"
            android:scrollbars="none" >
        </ListView>
    </com.zuck.swipe.hitblockrefresh.view.HitBlockRefreshView>

    Activity中：
        refreshView = (HitBlockRefreshView) findViewById(R.id.refresh_hit_block);

        listView = (ListView) findViewById(R.id.list_view);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, createDate());

        listView.setAdapter(arrayAdapter);
        refreshView.setOnRefreshListener(new HitBlockRefreshView.HitBlockRefreshListener() {
            @Override
            public void onRefreshing() {
                try {
                    // 模拟网络请求耗时动作
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHandler.sendEmptyMessage(0);
            }
        });

    当刷新完毕后需要在主线程中调用：
        refreshView.finishRefreshing();

# Attributes
    block_horizontal_num ：矩形块列数
    ball_speed : 小球弹射速度
    block_color : 矩形块颜色
    ball_color : 小球颜色
    racket_color : 挡板颜色

#Thanks
UI设计来自于：https://github.com/dasdom/BreakOutToRefresh

#Licence
Hitomis



