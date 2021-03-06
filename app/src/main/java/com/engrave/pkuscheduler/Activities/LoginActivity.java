package com.engrave.pkuscheduler.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.engrave.pkuscheduler.Models.CourseLoginInfoModel;
import com.engrave.pkuscheduler.R;
import com.engrave.pkuscheduler.Utils.PkuCourse.PkuCourseLoginClient;
import com.microsoft.officeuifabric.drawer.Drawer;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private TextView LoginStudentIdView;
    private EditText LoginPasswordView;
    private ImageButton LoginSubmitButton;
    private LottieAnimationView LottieButton;
    private LottieAnimationView LottieButtonLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        setUpActionBar();
        LoginStudentIdView = findViewById(R.id.Login_input_id);
        LoginPasswordView = findViewById(R.id.Login_input_password);
        LoginSubmitButton = findViewById(R.id.login_button);
        LottieButton = findViewById(R.id.login_button_lottie);
        LottieButtonLoading = findViewById(R.id.login_button_loading);
    }

    public void setUpActionBar(){
        Objects.requireNonNull(getSupportActionBar()).hide();
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(0x00ffffff);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    @SuppressLint("StaticFieldLeak")
    private class UserLoginTask extends AsyncTask<Void, Void, Integer> {

        private final PkuCourseLoginClient loginClient;

        UserLoginTask(String _studentId, String _password) {
            loginClient = new PkuCourseLoginClient(_studentId,_password);
        }


        private boolean isNetworkAvailable() {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            if (!isNetworkAvailable()) {
                return -1;
            }
            String bgWorkResult;
            try {
                Boolean hasPortalCookies =loginClient.FetchCourseCookies_Portals();
                Boolean hasIaaaToken = loginClient.FetchIaaaToken();
                Boolean hasCookies = loginClient.OathValidate();

                //Log.e("221312312312321312", JSON.toJSONString(loginClient.GetLoginInfo()));
                SharedPreferences sharedPreferences_LoginInfo = getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences_LoginInfo.edit();

                if(hasIaaaToken&&hasCookies&&hasPortalCookies){
                    CourseLoginInfoModel courseLoginInfoModel = loginClient.GetLoginInfo();
                    editor.putString("studentId", courseLoginInfoModel.studentId);
                    editor.putString("password", courseLoginInfoModel.password);/*
                    editor.putString("jSessionId", courseLoginInfoModel.jSessionId_Frameset);
                    editor.putString("guid", courseLoginInfoModel.guid);
                    editor.putString("sessionId", courseLoginInfoModel.sessionId);*/
                    editor.apply();
                    return 0;
                }
                else{
                    editor.putBoolean("isLogged",false);
                    if(!hasIaaaToken)return 1;
                    if(!hasCookies)return 2;
                    if(!hasPortalCookies)return 3;
                }
                return 4;
            } catch (Exception e) {
//                e.printStackTrace();
                return 4;
            }
        }

        @Override
        protected void onPostExecute(final Integer returnStatus) {

            if (returnStatus == 0) {
                TransitionToVerificationActivity();
            } else {
                Toast.makeText(getApplicationContext(), "Something went wrong...", Toast.LENGTH_LONG).show();
                ResetLottieButton();
            }
        }

        @Override
        protected void onCancelled() {
            Toast.makeText(getApplicationContext(),"Cancel",Toast.LENGTH_LONG).show();
        }
    }

    public void AttemptLogin(View view){
        LoginSubmitButton.setElevation(0);
        LoginSubmitButton.setBackground(getDrawable(R.drawable.disabled_button));
        LoginSubmitButton.setClickable(false);
        UserLoginTask userLoginTask = new UserLoginTask(
                LoginStudentIdView.getText().toString(),
                LoginPasswordView.getText().toString()
        );
        StartLottieButton();
        userLoginTask.execute((Void) null);

    }

    public void ResetLottieButton(){
        LoginSubmitButton.setElevation(16);
        LoginSubmitButton.setBackground(getDrawable(R.drawable.ripple_corner_24dp_accent_red));
        LoginSubmitButton.setClickable(true);
        LottieButton.pauseAnimation();
        LottieButton.setFrame(0);
        LottieButtonLoading.pauseAnimation();
        LottieButtonLoading.setFrame(0);
    }
    public void StartLottieButton(){
        LottieButton.playAnimation();
        LottieButtonLoading.playAnimation();
    }
    public void ShowPrivacyPolicyDrawer(View view){
        Drawer drawer = Drawer.newInstance(R.layout.drawer_privacy_policy);
        drawer.show(getSupportFragmentManager(),null);
    }

    public void TransitionToVerificationActivity(){
        Intent intent = new Intent(this, VerificationActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}
