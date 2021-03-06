package com.DLPort.NewsActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.DLPort.R;
import com.DLPort.app.Constant;
import com.DLPort.myactivity.BaseActivity;
import com.DLPort.myadapter.CarBusinessChangeAdapter;
import com.DLPort.mydata.Inform;
import com.DLPort.mytool.GlobalParams;
import com.DLPort.mytool.MyHandler;
import com.DLPort.mytool.MyThread;
import com.DLPort.mytool.MyToast;
import com.DLPort.myview.PullToRefreshLayout;
import com.DLPort.myview.PullableListView;
import com.DLPort.myview.TitleView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by fuyzh on 16/5/20.
 */
public class CarOwnerBusinessChangeActivity extends BaseActivity {
    private static final String TAG = "CarBusinessChange";
    private static final int pageSize = 10;

    private PullToRefreshLayout refreshLayout;
    private PullableListView pullableListView;

//    private PullToRefreshLayout recordRefreshLayout;//控制刷新完成
//    private PullableListView recordPullableListView;//控制加载完成

    SharedPreferences preferences;
    private boolean isCompleted = true;
    private int currentPage = 1;
    private int requestType = 0;
    private ArrayList<Inform> informList;
    private CarBusinessChangeAdapter adapter;

    private Handler handler = new MyHandler(this) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                try {
                    JSONObject obj = new JSONObject((String) msg.obj);
                    Log.i(TAG, (String) msg.obj);
                    int status = obj.getInt("Status");
                    if (status == 0) {
                        JSONObject data = new JSONObject(obj.getString("Data"));
                        JSONArray listData = new JSONArray(data.getString("InformList"));
                        if (listData.length() > 0) {
                            if (requestType == 0)
                                informList.clear();
                            ArrayList<Inform> increaseList = progressListData(listData);
                            if (null != increaseList)
                                informList.addAll(increaseList);
                            currentPage += 1;
                            adapter.notifyDataSetChanged();
                            if (requestType == 1) {
                                refreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
                            }
                            if (requestType == 2) {
                                if (increaseList.size() > 0)
                                    pullableListView.finishLoading();
                                else
                                    pullableListView.noMoreLoading();
                            }
                        } else {
                            if (requestType == 1) {
                                refreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
                            }
                            if (requestType == 2) {
                                pullableListView.noMoreLoading();
                            }
                            MyToast.makeText(CarOwnerBusinessChangeActivity.this, getResources().getString(R.string.no_data));
                        }
                        isCompleted = true;
                    } else {
                        isCompleted = true;
                        if (requestType == 1) {
                            refreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
                        }
                        if (requestType == 2) {
                            pullableListView.noMoreLoading();
                        }
                        MyToast.makeText(CarOwnerBusinessChangeActivity.this, getResources().getString(R.string.no_data));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        private ArrayList<Inform> progressListData(JSONArray jsonData) {
            ArrayList<Inform> list = new ArrayList<>();
            try {
                for (int i = 0; i < jsonData.length(); i++) {
                    Inform tmp = new Inform();
                    JSONObject object = jsonData.getJSONObject(i);
                    tmp.setId(object.getInt("Id"));
                    tmp.setCarOwnerId(object.getString("CarOwnerId"));
                    tmp.setCarGoOwnerId(object.getString("CargoOwnerId"));
                    tmp.setMsgContent(object.getString("MsgContent"));
                    tmp.setMsgStatue(object.getInt("Statue"));
                    tmp.setCreateTime(object.getString("CreateTime"));
                    tmp.setOrderId(object.getString("OrderId"));
                    tmp.setFlage(object.getInt("Flage"));
                    tmp.setCarOwnerName(object.getString("CarOwnerName"));
                    tmp.setCarGoOwnerName(object.getString("CargoOwnerName"));
                    tmp.setCarOwnerTel(object.getString("CarOwnerTel"));
                    tmp.setCarGoOwnerTel(object.getString("CargoOwnerTel"));
                    tmp.setVehNof(object.getString("VehNof"));
                    tmp.setPresentNo(object.getString("PresentNo"));
                    list.add(tmp);
                }
                return list;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_children);
        initTitle();
        initView();
    }

    private void initTitle() {
        TitleView titleView = (TitleView) findViewById(R.id.notice_children_title);
        titleView.setLeftViewVisible(true);
        titleView.setMiddleTextVisible(true);
        titleView.setWineText(R.string.news3);
        titleView.setMiddleText(R.string.business_change);
        View view = findViewById(R.id.title_back);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initView() {
        /*加载更多*/
        pullableListView = (PullableListView) findViewById(R.id.notice_children_list);
        pullableListView.setOnLoadListener(new PullableListView.OnLoadListener() {
            @Override
            public void onLoad(PullableListView pullableListView) {
                if (GlobalParams.isNetworkAvailable(CarOwnerBusinessChangeActivity.this)) {
                    if (isCompleted) {
                        requestType = 2;
                        getInfromList(currentPage, pageSize);
                    } else {
                        pullableListView.finishLoading();
                        MyToast.makeText(CarOwnerBusinessChangeActivity.this, getResources().getString(R.string.loading));
                    }
                } else {
                    pullableListView.finishLoading();
                    MyToast.makeText(CarOwnerBusinessChangeActivity.this, getResources().getString(R.string.no_network_promote));
                }
            }
        });

        /*下拉刷新*/
        refreshLayout = (PullToRefreshLayout) findViewById(R.id.notice_children_view);
        refreshLayout.setOnRefreshListener(new PullToRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
                if (GlobalParams.isNetworkAvailable(CarOwnerBusinessChangeActivity.this)) {
                    if (isCompleted) {
                        informList.clear();
                        adapter.notifyDataSetChanged();
                        currentPage = 1;
                        requestType = 1;
                        getInfromList(currentPage, pageSize);
                        pullableListView.finishLoading();
                    } else {
                        pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
                        MyToast.makeText(CarOwnerBusinessChangeActivity.this, getResources().getString(R.string.loading));
                    }
                } else {
                    pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
                    MyToast.makeText(CarOwnerBusinessChangeActivity.this, getResources().getString(R.string.no_network_promote));
                }
            }
        });

        preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        informList = new ArrayList<>();
        adapter = new CarBusinessChangeAdapter(this, informList, R.layout.car_business_change_item);
        pullableListView.setAdapter(adapter);
        if (GlobalParams.isNetworkAvailable(CarOwnerBusinessChangeActivity.this)) {
            getInfromList(1, pageSize);
        } else {
            MyToast.makeText(CarOwnerBusinessChangeActivity.this, getResources().getString(R.string.no_network_promote));
        }
    }

    private void getInfromList(int pageIndex, int pageSize) {
        JSONObject json = new JSONObject();
        try {
            json.put("UserID", preferences.getString("UserId", ""));
            json.put("PageIndex", pageIndex);
            json.put("PageSize", pageSize);
            json.put("Type", 0);
            isCompleted = false;

            new MyThread(Constant.URL_InformationPostAllInform, handler, json,CarOwnerBusinessChangeActivity.this).start();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
