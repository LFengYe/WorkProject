package com.DLPort.NewsActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.DLPort.R;
import com.DLPort.app.Constant;
import com.DLPort.myactivity.BaseActivity;
import com.DLPort.mytool.GlobalParams;
import com.DLPort.mytool.MyHandler;
import com.DLPort.mytool.MyThread;
import com.DLPort.mytool.MyToast;
import com.DLPort.myview.TitleView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2016/5/18.
 */
public class guanyuActivity extends BaseActivity {
    private int currentPage =1;
    private static final String TAG="guanyuActivity";
    private ArrayAdapter<String> adapter;
    private ListView listView;
    private String[] Str;

    private Handler handler =new MyHandler(this) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                try {
                    JSONObject jsonUser = null;
                    jsonUser = new JSONObject((String) msg.obj);

                    Log.d(TAG, "Login的响应" + jsonUser.toString());
                    int status = jsonUser.getInt("Status");
                    Log.i(TAG, "状态" + status);
                    if (0 == status) {
                        pullUnOrderList(jsonUser);
                        currentPage +=1;
                    }else {
                        MyToast.makeText(guanyuActivity.this, "获取失败");
                    }
                } catch (JSONException e) {
                    Log.d(TAG, "异常——————————————————————————————");
                    e.printStackTrace();
                }
            }else {
                Log.d(TAG,msg.what+" 服务器异常");
                MyToast.makeText(guanyuActivity.this, msg.what+" 服务器异常");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_guanyu);
        initTitle();
        init();
    }

    private void init() {
        listView = (ListView) findViewById(R.id.guanyu_list);


        if(GlobalParams.isNetworkAvailable(guanyuActivity.this)) {
            JSONObject json = new JSONObject();
            try {
                json.put("PageIndex", currentPage);
                json.put("PageSize", 20);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new MyThread(Constant.URL_PostGetguanyu,handler,json,this).start();

        } else{
            MyToast.makeText(guanyuActivity.this, "亲,网络未连接");
        }

    }
    public void pullUnOrderList (JSONObject obj) {


        String data = null;
        try {
            data = obj.getString("Data");
            Log.d(TAG, "Data===" + data);
            JSONObject jsonObject = new JSONObject(data);
            String s = jsonObject.getString("MessageList");
            JSONArray jsonArray = new JSONArray(s);
            Log.d(TAG, "Array==="+jsonArray.toString());
            Str = new String[jsonArray.length()];
            for (int i= 0 ;i<jsonArray.length();i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                Str[i] = json.getString("MsgContent");
                Log.d(TAG,Str[i]);
            }
            adapter = new ArrayAdapter<String>(guanyuActivity.this,R.layout.text,Str);
            listView.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    private void initTitle() {
        TitleView titleView = (TitleView) findViewById(R.id.guanyu_title);
        titleView.setLeftViewVisible(true);
        titleView.setMiddleTextVisible(true);
        titleView.setWineText(R.string.news);
        titleView.setMiddleText(R.string.news4);
        View view =findViewById(R.id.title_back);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


}
