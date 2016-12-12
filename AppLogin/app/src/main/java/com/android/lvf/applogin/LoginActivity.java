package com.android.lvf.applogin;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by slowergun on 2016/12/7.
 */
public class LoginActivity extends Activity implements View.OnClickListener {
    private EditText mNameEt;
    private EditText mPwdEt;
    private Button   mLoginBtn;
    private Button   mQQLoginBtn;

    @Override
    public void onClick(View v) {
        if (v == mLoginBtn) {

        } else if (v == mQQLoginBtn) {

        } else {
            throw new RuntimeException("unsupported click view");
        }
    }
}
