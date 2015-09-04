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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ecust.main.R;

//左侧侧滑菜单
public class fragment_MLKZ_LeftMenu extends Fragment implements View.OnClickListener {

    public fragment_MLKZ_LeftMenu() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mlkz_home_page_fragment_leftmenu, container, false);

        //寻找头像图片，点击后登录
        ViewGroup viewGroup = (ViewGroup) view.findViewById(R.id.mlkz_home_menu_login_group);
        viewGroup.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mlkz_home_menu_login_group:
                //点击头像登录，或显示详细数据
                Intent intent = new Intent(getActivity(), act_MLKZ_Login.class);
                startActivityForResult(intent, 0);

                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) return;

        //设置登陆成功的用户名
        String username = data.getStringExtra(act_MLKZ_Login.USERNAME);
        TextView tvUserName = (TextView) getActivity().findViewById(R.id.mlkz_home_menu_login_username);
        tvUserName.setText(username);
    }
}
