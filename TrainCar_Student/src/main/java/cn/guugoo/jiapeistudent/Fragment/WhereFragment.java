package cn.guugoo.jiapeistudent.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.alibaba.fastjson.JSONObject;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;

import java.util.ArrayList;
import java.util.List;

import cn.guugoo.jiapeistudent.Adapter.SiteAdapter;
import cn.guugoo.jiapeistudent.App.Constant;
import cn.guugoo.jiapeistudent.App.MyApplication;
import cn.guugoo.jiapeistudent.Data.ReturnData;
import cn.guugoo.jiapeistudent.Data.Site;
import cn.guugoo.jiapeistudent.MinorActivity.WhereSelectTImeActivity;
import cn.guugoo.jiapeistudent.R;
import cn.guugoo.jiapeistudent.Tools.DES;
import cn.guugoo.jiapeistudent.Tools.LocationService;
import cn.guugoo.jiapeistudent.Tools.MyHandler;
import cn.guugoo.jiapeistudent.Tools.MyThread;
import cn.guugoo.jiapeistudent.Tools.MyToast;


/**
 * Created by Administrator on 2016/8/8.
 */
public class WhereFragment extends Fragment {
    private static final String TAG = "WhereFragment";
    private ListView listView;
    private SharedPreferences sp;
    private List<Site> listData;
    private SiteAdapter adapter;
    private View fragmentView;
    //纬度
    private double latitude = 0;
    //经度
    private double longitude = 0;
    private ImageView search;
    private EditText search_text;
    private LocationService locationService;
    private Handler handler;
//    protected Handler handler = new MyHandler(getContext()) {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            if (msg.what == 1) {
//                Log.d(TAG, "handleMessage: " + msg.obj);
//                ReturnData data = JSONObject.parseObject((String) msg.obj, ReturnData.class);
//                if (data.getStatus() == 0) {
//                    List<Site> sites = JSONObject.parseArray(data.getData(), Site.class);
//                    listData.addAll(sites);
//                    adapter.notifyDataSetChanged();
//                } else {
//                    MyToast.makeText(getContext(), data.getMessage());
//                }
//            }
//        }
//    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_where, container, false);
        handler = new MyHandler(getActivity()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    Log.d(TAG, "handleMessage: " + msg.obj);
                    ReturnData data = JSONObject.parseObject((String) msg.obj, ReturnData.class);
                    if (data.getStatus() == 0) {
                        List<Site> sites = JSONObject.parseArray(data.getData(), Site.class);
                        listData.addAll(sites);
                        adapter.notifyDataSetChanged();
                    } else {
                        MyToast.makeText(getContext(), data.getMessage());
                    }
                }
            }
        };
        findById();
        init();
        return fragmentView;
    }

    private void findById() {
        listView = (ListView) fragmentView.findViewById(R.id.where_list);
        listData = new ArrayList<>();
        adapter = new SiteAdapter(getContext(), R.layout.adapter_site, listData);
        listView.setAdapter(adapter);
        sp = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        search = (ImageView) fragmentView.findViewById(R.id.search_image);
        search_text = (EditText) fragmentView.findViewById(R.id.search_text);
        search_text.setHint("请输入场地名称");
    }

    private void init() {
        getWhere();
//        getSiteData();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Site site = listData.get(position);
                Intent intent = new Intent(getActivity(), WhereSelectTImeActivity.class);
                intent.putExtra("BranchId", site.getBid());
                Log.d(TAG, "onItemClick: " + site.getBid());
                startActivity(intent);
            }
        });
        search_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                select();
            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                select();
            }
        });

    }

    private void select() {
        listData.clear();
        JSONObject json = new JSONObject(true);
        json.put("SchoolId", sp.getInt("SchoolId", 0));
        json.put("StudentId", sp.getInt("Id", 0));
        json.put("Subject", sp.getInt("CurrentSubject", 0));
        json.put("Longitude", longitude);
        json.put("Latitude", latitude);
        json.put("AreaName", search_text.getText());
        Log.d(TAG, "getSiteData: " + json.toString());
        new MyThread(Constant.URL_SurroundingArea, handler, DES.encryptDES(json.toString())).start();
    }

    private void getSiteData() {
        Log.d(TAG, "getSiteData: 123456");
        JSONObject json = new JSONObject(true);
        json.put("SchoolId", sp.getInt("SchoolId", 0));
        json.put("StudentId", sp.getInt("Id", 0));
        json.put("Subject", sp.getInt("CurrentSubject", 0));
        Log.d(TAG, "getSiteData: "+longitude);
        Log.d(TAG, "getSiteData: "+latitude);
        json.put("Longitude", longitude);
        json.put("Latitude", latitude);
        json.put("AreaName", "");
        Log.d(TAG, "getSiteData: " + json.toString());
        new MyThread(Constant.URL_SurroundingArea, handler, DES.encryptDES(json.toString())).start();

        locationService.stop();
    }

    private void getWhere() {
        getLocation();
        locationService.start();// 定位SDK
    }
    private void  getLocation(){
        // -----------location config ------------
        locationService = ((MyApplication) getActivity().getApplication()).locationService;
        //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
        locationService.registerListener(mListener);
        //注册监听
        locationService.setLocationOption(locationService.getDefaultLocationClientOption());
        Log.d(TAG, "getLocation: ");
    }
    /*****
     *
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     *
     */
    private BDLocationListener mListener = new BDLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            Log.d(TAG, "getLocation: ++");
            // TODO Auto-generated method stub
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                double Latitude = location.getLatitude();// 纬度
                double Longitude = location.getLongitude();// 经度
                logMsg(Longitude,Latitude);

            }
        }
    };
    public void logMsg(final double Longitude, final double Latitude) {
        if(Latitude!=4.9E-324&&Longitude!=4.9E-324){
            latitude = Latitude;// 纬度
            longitude = Longitude;// 经度
            Log.d(TAG, "onReceiveLocation: "+latitude);
            Log.d(TAG, "onReceiveLocation: "+longitude);
            getSiteData();
        }else {
            Log.d(TAG, "onReceiveLocation:Error "+Latitude);
            Log.d(TAG, "onReceiveLocation:Error "+Longitude);
            MyToast.makeText(getActivity(),"获取地址失败！");
            getSiteData();
        }
    }


}
