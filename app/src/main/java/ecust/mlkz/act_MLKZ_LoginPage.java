package ecust.mlkz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import lib.clsGlobal.Global;
import ecust.main.R;
import lib.clsHttpAccess;

public class act_MLKZ_LoginPage extends Activity {
    private final String Activity_Tag = "Class_LoginPage";                                         //标签Tag
    private final int minPasswordLength = 6;                                                 //最小密码长度
    private EditText username;                                                                      //用户名文本框
    private EditText password;                                                                      //密码文本框
    private Button login;                                                                            //登陆按钮
    private TextView register;                                                                      //注册账号
    private boolean isLogining = false;
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            ChangeLoginButtonStyle();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.mlkz_login_button:                                                             //点击登录
                    if (isLogining) break;
                    if (username.length() > 0 && password.length() >= minPasswordLength) {
                        //使用用户名、密码进行登陆
                        isLogining = true;
                        login.setText("登录中");
                        login.setBackgroundColor(getResources().getColor(R.color.green1));

                        String str_username = username.getText().toString();                        //获取用户名
                        String str_password = password.getText().toString();                        //获取密码

                        clsHttpAccess.LoginMLKZ(str_username, str_password);                        //登陆梅陇客栈，获取cookie
                    } else {
                        Global.toastMsg("请完整填写用户名及密码！");
                    }
                    break;

                case R.id.mlkz_login_register:                                                           //点击注册账号
                    //跳转至注册信息界面
                    Intent activity_register = new Intent();
                    activity_register.setClass(act_MLKZ_LoginPage.this, act_MLKZ_Register.class);
                    startActivity(activity_register);
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        Global.activity = this;                                                                       //设置当前活动窗体
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.mlkz_login_page);

        //获取控件引用
        username = (EditText) findViewById(R.id.mlkz_login_username);
        password = (EditText) findViewById(R.id.mlkz_login_password);
        login = (Button) findViewById(R.id.mlkz_login_button);
        register = (TextView) findViewById(R.id.mlkz_login_register);

        username.setText("彩笔怪盗基德");                                                           //用户名！！！！！！！！！！！
        password.setText("jaychou");                                                                //密码！！！！！！！！！！！！

        //设置事件处理
        username.addTextChangedListener(textWatcher);
        password.addTextChangedListener(textWatcher);

        login.setOnClickListener(onClickListener);
        register.setOnClickListener(onClickListener);
    }

    /**
     * 根据内容改变登录按钮Style
     */
    private void ChangeLoginButtonStyle() {
        if (username.length() > 0 && password.length() >= minPasswordLength) {
            login.setBackgroundResource(R.drawable.selector_login_button);
        } else {
            //信息没填写完整，不给点击
            login.setBackgroundColor(getResources().getColor(R.color.green1));
        }
    }

    /**
     * 处理登录结果
     *
     * @param html 网页源码
     */
    public void handle_Login(String html) {
        isLogining = false;
        if (html.contains("欢迎您回来")) {
            Global.toastMsg("登录成功");
            login.setText("登录成功");

            //跳转至主界面梅陇客栈
            Intent activity_new = new Intent();
            activity_new.setClass(this, act_MLKZ_Home.class);
            startActivity(activity_new);                                                            //启动MLKZ主页
            finish();                                                                               //登录成功，销毁界面
        } else {
            Global.toastMsg(html);                                                                  //登录失败
            login.setText("登录");
            password.setText("");                                                                   //清空密码
            ChangeLoginButtonStyle();                                                                //修改Button样式
        }
    }
}
