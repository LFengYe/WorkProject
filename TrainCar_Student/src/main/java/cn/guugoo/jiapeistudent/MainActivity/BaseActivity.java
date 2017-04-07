package cn.guugoo.jiapeistudent.MainActivity;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Window;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;


import cn.guugoo.jiapeistudent.App.ActivityCollector;
import cn.guugoo.jiapeistudent.Data.ReturnData;
import cn.guugoo.jiapeistudent.Tools.MyHandler;
import cn.guugoo.jiapeistudent.Tools.MyToast;


/**
 * Created by Administrator on 2016/7/31.
 */
public  abstract class BaseActivity extends FragmentActivity {
    private static final String TAG = "BaseActivity";

    protected Handler handler = new MyHandler(this){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 1){
                try {
                    Log.d(TAG, "handleMessage: "+msg.obj);
                    ReturnData data= JSONObject.parseObject((String) msg.obj,ReturnData.class);
                    if(data.getStatus()==0){
                        processingData(data);
                    }else {
                        MyToast.makeText(BaseActivity.this,data.getMessage());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    MyToast.makeText(BaseActivity.this,"数据出错");
                }
            }
        }
    };

    protected  void processingData(ReturnData data){
    };



    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivty(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        initContentView(savedInstanceState);
        initTitle();
        findView();
        init();
        ActivityCollector.addActivty(this);
    }

    // 初始化UI，setContentView等
    protected abstract void initContentView(Bundle savedInstanceState);


    protected abstract void initTitle();

    protected abstract void findView();

    protected abstract void init();


}
