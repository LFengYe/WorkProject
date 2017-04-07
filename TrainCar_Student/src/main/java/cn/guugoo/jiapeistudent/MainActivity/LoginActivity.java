package cn.guugoo.jiapeistudent.MainActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

import cn.guugoo.jiapeistudent.App.ActivityCollector;
import cn.guugoo.jiapeistudent.App.Constant;
import cn.guugoo.jiapeistudent.Data.ReturnData;
import cn.guugoo.jiapeistudent.Data.Student;
import cn.guugoo.jiapeistudent.MinorActivity.SelectSchoolActivity;
import cn.guugoo.jiapeistudent.R;
import cn.guugoo.jiapeistudent.Tools.DES;
import cn.guugoo.jiapeistudent.Tools.MyHandler;
import cn.guugoo.jiapeistudent.Tools.MyThread;
import cn.guugoo.jiapeistudent.Tools.MyToast;
import cn.guugoo.jiapeistudent.Tools.Utils;
import cn.jpush.android.api.InstrumentedActivity;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

/**
 * Created by Administrator on 2016/8/3.
 */
public class LoginActivity extends InstrumentedActivity {
    private static final  String TAG = "LoginActivity";
    private EditText user,passWord;
    private Button login;
    private TextView signIn,find;
    private SharedPreferences sp;
    private Dialog dialog;
    private CheckBox checkBox;
    private Handler handler = new MyHandler(this){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 1){
                Log.d(TAG, "handleMessage: "+msg.obj);
                ReturnData data=JSONObject.parseObject((String) msg.obj,ReturnData.class);
                if(data.getStatus()==0){
                    /**
                    * 登陆成功，下次可以直接登陆
                    */
                    sp.edit().putBoolean("LOGINOK",true).apply();
                    Student student = JSONObject.parseObject(data.getData(),Student.class);
                    save(student);
                    dialog.dismiss();
                    /**
                     * 登录成功, 设置极光推送标签和别名
                     */
                    HashSet<String> tags = new HashSet<String>();
                    tags.add(sp.getString("Tel",""));
                    JPushInterface.setAliasAndTags(LoginActivity.this, user.getText().toString() , tags, new TagAliasCallback() {
                        @Override
                        public void gotResult(int arg0, String arg1, Set<String> arg2) {

                        }
                    });
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    intent.putExtra("json","");
                    startActivity(intent);
                    finish();
                }else {
                    MyToast.makeText(LoginActivity.this,data.getMessage());
                }
                dialog.dismiss();
            }else {
                dialog.dismiss();
                MyToast.makeText(LoginActivity.this,R.string.Toast_internet_no);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        findView();
        init();
        setBarStyle();
    }

    public void setBarStyle() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 设置状态栏透明
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
    private void findView() {

        user = (EditText) findViewById(R.id.login_user);
        passWord = (EditText) findViewById(R.id.login_password);
        login = (Button) findViewById(R.id.login_button);
        signIn = (TextView) findViewById(R.id.sign_in_Button);
        find = (TextView) findViewById(R.id.find_password);
        checkBox = (CheckBox) findViewById(R.id.login_checkbox);
        sp =getSharedPreferences("user", Context.MODE_PRIVATE);
        user.setText(sp.getString("UserName", ""));
        dialog = Utils.loginDialog(LoginActivity.this);
        if(sp.getBoolean("AUTO_ISCHECK",false)) {
            if(sp.getBoolean("LOGINOK",false)) {
                passWord.setText(sp.getString("PassWord", ""));
                login();
            }
        }
    }

    private void init() {

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(LoginActivity.this,SelectSchoolActivity.class);
                startActivity(intent);
            }
        });
        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(LoginActivity.this,SignInActivity.class);
                intent.putExtra("Type",2);
                startActivity(intent);
            }
        });
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBox.isChecked()) {
                    sp.edit().putBoolean("AUTO_ISCHECK", true).apply();
                } else {
                    sp.edit().putBoolean("AUTO_ISCHECK", false).apply();
                }
            }
        });
    }

    private boolean isEmpty(){
        if(TextUtils.isEmpty(user.getText())){
            MyToast.makeText(LoginActivity.this,R.string.Toast_phone);
            return false;
        }
        if(TextUtils.isEmpty(passWord.getText())){
            MyToast.makeText(LoginActivity.this,R.string.Toast_password);
        }
        return true;
    }

    private void save(Student student) {

        SharedPreferences.Editor edit = sp.edit();
        edit.putInt("Id",student.getId());
        edit.putString("Name",student.getName());
        edit.putString("Tel",student.getTel());
        edit.putInt("CurrentSubject",student.getCurrentSubject());
        edit.putInt("SchoolId",student.getSchoolId());
        edit.putInt("Hours",student.getHours());
        edit.putBoolean("IsSign",student.isSign());
        edit.putString("token",student.getToken());
        edit.putString("HeadPortrait",student.getHeadPortrait());
        edit.putString("Nickname",student.getNickname());
        edit.putString("Sex",student.getSex());
        edit.putString("Address",student.getAddress());
        edit.putString("InvitationCode",student.getInvitationCode());
        edit.putString("PracticeCount",student.getPracticeCount());
        edit.putString("Schedule",student.getSchedule());
        edit.putString("UserName",user.getText().toString().trim());
        edit.putString("PassWord",passWord.getText().toString().trim());
        edit.apply();

    }
    private void login(){
        if(Utils.isNetworkAvailable(LoginActivity.this)){
            if(isEmpty()){
                JSONObject json= new JSONObject(true);
                json.put("Tel",user.getText());
                json.put("PassWord",passWord.getText());
                Log.d(TAG, "onClick: "+DES.encryptDES(json.toString()));
                dialog.show();
                new MyThread(Constant.URL_Login, handler, DES.encryptDES(json.toString()),1).start();
            }
        }else {
            MyToast.makeText(LoginActivity.this,R.string.Toast_internet);
        }

    }


}
