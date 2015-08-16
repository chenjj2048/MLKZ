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

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;

import ecust.main.R;
import lib.BaseActivity.MyBaseActivity;
import lib.clsGlobal.logUtil;

//梅陇客栈登陆页面
public class act_MLKZ_Login extends MyBaseActivity implements TextWatcher, View.OnClickListener {
    private myEditText username;
    private myEditText password;
    private Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mlkz_login);

        initCompents();
    }

    public void initCompents() {
        username = (myEditText) findViewById(R.id.mlkz_login_username);
        password = (myEditText) findViewById(R.id.mlkz_login_password);
        login = (Button) findViewById(R.id.mlkz_login_button);

        username.addTextChangedListener(this);
        password.addTextChangedListener(this);
        login.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (username.getText().length() <= 0) {
            logUtil.toast("请输入用户名！");
            return;
        }

        if (password.getText().length() <= 0) {
            logUtil.toast("请输入密码！");
            return;
        }

        //进行登陆
        login(username.getText().toString(), password.getText().toString());
    }

    //登陆获取cookie
    public void login(String strUsername, String strPassword) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (username.getText().length() > 0 && password.getText().length() > 0) {
            //能被点击
            login.setBackgroundResource(R.drawable.shape_mlkz_login_button_can_press);
        } else {
            //不能点击
            login.setBackgroundResource(R.drawable.shape_mlkz_login_button_cannot_press);
        }
    }
}
