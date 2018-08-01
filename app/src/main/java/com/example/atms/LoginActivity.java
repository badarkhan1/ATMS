package com.example.atms;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.atms.Services.AuthenticationService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class LoginActivity extends AppCompatActivity implements TextWatcher{

    Button btn_signin;
    ProgressBar progressBar;
    Handler handler;
    EditText et_email, et_password;
    TransitionDrawable transition;
//    Animation downToUp,upToDown;

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(isLoggedIn()){
            redirectToHomeActivity();
            finish();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        onCreateInitializations();
    }

    private void onCreateInitializations() {
        btn_signin = (Button) findViewById(R.id.btn_signin);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this,R.color.colorProgress), PorterDuff.Mode.SRC_IN);
        et_email = (EditText) findViewById(R.id.et_email);
        et_password = (EditText) findViewById(R.id.et_password);
        btn_signin.setEnabled(false);
        btn_signin.setTextColor(ContextCompat.getColor(this,R.color.halfWhite));
        handler = new Handler();
        transition = (TransitionDrawable) btn_signin.getBackground();
//        downToUp = AnimationUtils.loadAnimation(this,R.anim.bottom_up);
//        upToDown = AnimationUtils.loadAnimation(this,R.anim.top_bottom);
//        findViewById(R.id.lower_layout).setAnimation(downToUp);
//        findViewById(R.id.logo).setAnimation(upToDown);
        EventBus.getDefault().register(this);
        et_email.addTextChangedListener(this);
        et_password.addTextChangedListener(this);
    }

    private void redirectToHomeActivity() {
        startActivity(new Intent(this,DrawerActivity.class));
        finish();
    }

    @Subscribe
    public void authenticationServiceLoginResponse(Integer response){
        resetButtonEffect();
        switch (response){
            case AuthenticationService.LOGIN_SUCCESS:
                redirectToHomeActivity();
                break;
            case AuthenticationService.LOGIN_FAILED:
                failedLoginButtonEffect();
                break;
            default:break;
        }
    }

    public void attemptLogin(View view) {
        attemptingLoginButtonEffect();
        Intent intent = new Intent(LoginActivity.this,AuthenticationService.class);
        intent.setAction("com.example.atms.ACTION_LOGIN");
        intent.putExtra("email",et_email.getText().toString());
        intent.putExtra("password",et_password.getText().toString());
        startService(intent);
    }

    public void attemptingLoginButtonEffect(){
        et_email.setEnabled(false);
        et_password.setEnabled(false);
        btn_signin.setEnabled(false);
        btn_signin.setText("");
        transition.startTransition(80);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void resetButtonEffect(){
        et_email.setEnabled(true);
        et_password.setEnabled(true);
        btn_signin.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        transition.reverseTransition(80);
        btn_signin.setText(R.string.sign_in);
    }

    public void failedLoginButtonEffect(){

        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Invalid Credentials").setMessage("Incorrect email or password");
        builder.setPositiveButton("Try again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setNegativeButton("Forgot Password", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();
    }

    public boolean isLoggedIn() {
        return !TextUtils.isEmpty(getApplicationContext().getSharedPreferences("login",MODE_PRIVATE).getString("token",""));
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if(!et_email.getText().toString().equals("") && !et_password.getText().toString().equals("")){
            btn_signin.setEnabled(true);
            btn_signin.setTextColor(Color.WHITE);
        }else{
            btn_signin.setEnabled(false);
            btn_signin.setTextColor(ContextCompat.getColor(this,R.color.halfWhite));
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}