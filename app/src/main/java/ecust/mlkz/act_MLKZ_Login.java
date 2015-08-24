/**
 * =============================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2,
 * as published by the Free Software Foundation.
 * .
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * .
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * =============================================================================
 * .
 * Created by 彩笔怪盗基德 on 2015/8/16
 * Copyright (C) 2015 彩笔怪盗基德
 * 托管地址：https://github.com/chenjj2048
 * .
 */

package ecust.mlkz;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import ecust.main.R;
import lib.BaseActivity.MyBaseActivity;
import lib.clsUtils.clsSoftKeyBoard;
import lib.clsUtils.logUtil;

//梅陇客栈登陆页面
public class act_MLKZ_Login extends MyBaseActivity implements TextWatcher, View.OnClickListener,
        View.OnTouchListener, cls_MLKZ_Login.OnLoginStatusReturn {
    public static final String COOKIE = "cookie";
    public static final String USERNAME = "username";
    private Dialog loadingDialog;      //加载中Dialog
    private myEditText edittext_Username;
    private myEditText edittext_password;
    private Button btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mlkz_login);

        initCompents();
    }

    public void initCompents() {
        edittext_Username = (myEditText) findViewById(R.id.mlkz_login_username);
        edittext_password = (myEditText) findViewById(R.id.mlkz_login_password);
        btn_login = (Button) findViewById(R.id.mlkz_login_button);

        edittext_Username.addTextChangedListener(this);
        edittext_password.addTextChangedListener(this);
        btn_login.setOnClickListener(this);
        btn_login.setOnTouchListener(this);

        loadingDialog = getLoadingDialog(this);

        //获取成功登陆过的用户名
        String strRecentUsername = new cls_MLKZ_Login(this).new getLoginPreference().getUsername();
        edittext_Username.setText(strRecentUsername);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //登陆按钮按下后，隐藏软键盘
                new clsSoftKeyBoard().hideIME(this, v);
                break;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if (edittext_Username.getText().length() <= 0) {
            logUtil.toast("请输入用户名！");
            return;
        }

        if (edittext_password.getText().length() <= 0) {
            logUtil.toast("请输入密码！");
            return;
        }

        if (edittext_password.getText().length() < 6) {
            logUtil.toast("密码过短，请重新输入！");
            return;
        }

        //进行登陆
        loginAndGetCookie(edittext_Username.getText().toString(), edittext_password.getText().toString());
        edittext_password.setText("");
    }

    //登陆消息返回
    @Override
    public void OnLoginStatusReturn(String username, String password, String rtnMessage, String cookie) {
        loadingDialog.dismiss();
        if (rtnMessage == null || rtnMessage.length() <= 0) {
            logUtil.toast("登陆失败");
            return;
        }
        if (rtnMessage.contains("欢迎您回来")) {
            //设置返回消息
            Intent intent = new Intent();
            intent.putExtra(COOKIE, cookie);
            intent.putExtra(USERNAME, edittext_Username.getText().toString());
            setResult(0, intent);
            finish();
            logUtil.toast("登陆成功");
            return;
        }

        logUtil.toast(rtnMessage);      //失败消息
    }

    //登陆获取cookie
    public void loginAndGetCookie(String strUsername, String strPassword) {
        //显示加载对话框
        loadingDialog.show();

        //进行登陆
        cls_MLKZ_Login mLogin = new cls_MLKZ_Login(this).setUsername(strUsername).setPassword(strPassword);
        mLogin.setOnLoginStatusReturn(this);
        mLogin.login();     //开始登陆
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (edittext_Username.getText().length() > 0 && edittext_password.getText().length() > 0) {
            //能被点击
            btn_login.setBackgroundResource(R.drawable.shape_mlkz_login_button_can_press);
        } else {
            //不能点击
            btn_login.setBackgroundResource(R.drawable.shape_mlkz_login_button_cannot_press);
        }
    }

    //获取加载对话框
    public Dialog getLoadingDialog(Context context) {
        //设置View
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.loading_dialog, null);// 得到加载view

        // 创建自定义样式dialog
        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);

        loadingDialog.setCancelable(false);// 不可以用“返回键”取消
        loadingDialog.setContentView(v);

        return loadingDialog;
    }
}
