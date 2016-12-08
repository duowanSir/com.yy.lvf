package com.android.lvf.applogin;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;

import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONObject;

/**
 * Created by slowergun on 2016/12/7.
 */
public class LoginClient {
    private static class Instance {
        private static LoginClient mLoginClient = new LoginClient();
    }

    public interface ILoginProcess {
        void onPreLogin();

        void onLoginSuccess();

        void onLoginFailed();

        void onLoginException(Throwable e);
    }

    public enum LoginType {
        QQ, PROACTIVE, PASSIVE
    }

    private static final String QQ_APP_ID = "";

    private Context       mContext;
    private Tencent       mTencent;
    private ILoginProcess mLoginProcess;

    private LoginClient() {
    }

    public void qqLogin(Object ui) {
        if (mTencent == null) {
            mTencent = Tencent.createInstance(QQ_APP_ID, mContext);
        }
        if (ui instanceof Activity) {
            mTencent.login((Activity) ui, "", mQQLoginListener);
        } else if (ui instanceof Fragment) {
            mTencent.login((android.support.v4.app.Fragment) ui, "", mQQLoginListener);
        } else {
            throw new RuntimeException("unsupported ui");
        }
    }

    private IUiListener mQQLoginListener = new IUiListener() {
        @Override
        public void onComplete(Object o) {
            try {
                JSONObject res = (JSONObject) o;
                String qqOpenId = res.getString("openid");
                String accessToken = res.getString("access_token");
                String expiries = res.getString("expires_in");
                mTencent.setOpenId(qqOpenId);
                mTencent.setAccessToken(accessToken, expiries);
                mLoginProcess.onPreLogin();// qq登录前
                mLoginCall = LoginClient.getInstace().thirdLogin(LoginClient.ThirdLoginType.QQ, qqOpenId, accessToken, new TaskExecutor.Callback<LoginClient.ThirdLoginResult>() {
                    @Override
                    public void onCallback(LoginClient.ThirdLoginResult result) {
                        if (result.code == LoginClient.LOGIN_ERR_SUCCESS) {
                            mUid = result.uid;
                            if (result.isNewUser) { //新用户，同步用户信息
                                UserInfo info = new UserInfo(ThirdLoginActivity.this.getApplicationContext(), tencent.getQQToken());
                                info.getUserInfo(mGetQQUserInfoIListener);
                            } else { //自己进行登陆
                                huaLogin();
                            }
                        } else {
                            hideProgressView();
                            HuaToast.error(String.format("QQ授权登录失败，请重试(1,%s)", result.code));
                        }
                    }
                });
            } catch (Exception e) {
                hideProgressView();
                e.printStackTrace();
            }

        }

        @Override
        public void onError(UiError uiError) {
        }

        @Override
        public void onCancel() {
        }
    };

    private void thirdLogin(LoginType type, String openId, String token) {

    }
}
