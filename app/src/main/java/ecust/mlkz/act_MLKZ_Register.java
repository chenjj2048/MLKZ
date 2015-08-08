package ecust.mlkz;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import lib.clsGlobal.Global;
import ecust.main.R;


public class act_MLKZ_Register extends Activity {
    private String Activity_Tag = "Class_RegisterAccount";                                            //标签Tag

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.mlkz_register);
    }

    @Override
    protected void onResume() {
        Global.activity = this;                                                                       //设置当前活动窗体
        super.onResume();
    }
}
