package com.example.atms;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.atms.Models.DeserializedModels.AccountUpdateResponse;
import com.example.atms.NetworkClient.AtmsClient;
import com.example.atms.NetworkRemoteSingleton.Remote;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ProfileActivity extends AppCompatActivity {

    private final int CHANGE_PROFILE_PIC = 1;
    private Retrofit retrofit;
    private AtmsClient atmsClient;
    private static final String TAG = "MTAG";

    @BindView(R.id.name)
    EditText name;
    @BindView(R.id.email) EditText email;
    @BindView(R.id.contact) EditText contact;
    @BindView(R.id.address) EditText address;
    @BindView(R.id.profilePic)
    ImageView profilePic;
    private String imagePath=null;
    FrameLayout layout;
    ProgressBar profileProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        retrofit = Remote.getRetrofit();
        atmsClient = retrofit.create(AtmsClient.class);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        layout = (FrameLayout) findViewById(R.id.frame);
        profileProgress = (ProgressBar) findViewById(R.id.profile_pro);
        Picasso.with(this).load(Remote.IP + Remote.USER_IMAGES_URL + getSharedPreferences("login",MODE_PRIVATE).getString("image",""))
        .into(profilePic);
        name.setText(getSharedPreferences("login",MODE_PRIVATE).getString("name",""));
        email.setText(getSharedPreferences("login",MODE_PRIVATE).getString("email",""));
        contact.setText(getSharedPreferences("login",MODE_PRIVATE).getString("contact",""));
        address.setText(getSharedPreferences("login",MODE_PRIVATE).getString("address",""));
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this,ProfilePicActivity.class);
                if(imagePath != null){
                    intent.putExtra("imagePath",imagePath);
                }
                startActivity(intent);
            }
        });
        profilePic.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    profilePic.setColorFilter(Color.argb(25,0,0,0));
                }

                if(motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_SCROLL){
                    profilePic.clearColorFilter();
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.check){
            if(name.getText().toString().equals("") || email.getText().toString().equals("") ||
                    contact.getText().toString().equals("") || address.getText().toString().equals("")){
                Snackbar.make(layout,"provide all requested information",Snackbar.LENGTH_LONG).show();
            }else{
                profileProgress.setVisibility(View.VISIBLE);
                updateUserAccount();
            }
        }else if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.selectPic)
    public void selectProfilePic(){
        Intent intent = new Intent();
//        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), CHANGE_PROFILE_PIC);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data != null && data.getData() != null){
            if(requestCode == CHANGE_PROFILE_PIC){
                Uri uri = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imagePath = cursor.getString(columnIndex);
                    Picasso.with(this).load(new File(imagePath)).into(profilePic);
                    Log.d("MTAG", "onActivityResult: " + imagePath);
                }
            }
        }
    }

    public void updateUserAccount(){
        MultipartBody.Part body = null;
        if(imagePath!=null){
            File file = new File(imagePath);
            RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"),file);
            body = MultipartBody.Part.createFormData("image",file.getName(),requestBody);
        }
        RequestBody bodyName    = RequestBody.create(MultipartBody.FORM,name.getText().toString());
        RequestBody bodyEmail   = RequestBody.create(MultipartBody.FORM,email.getText().toString());
        RequestBody bodyContact = RequestBody.create(MultipartBody.FORM,contact.getText().toString());
        RequestBody bodyAddress = RequestBody.create(MultipartBody.FORM,address.getText().toString());

        Call<AccountUpdateResponse> call = atmsClient.updateUserAccountInfo(getSharedPreferences("login",MODE_PRIVATE).getInt("user_id",0),
                "Bearer " + getSharedPreferences("login",MODE_PRIVATE).getString("token",""),body,bodyName,bodyEmail,bodyContact,bodyAddress);
        call.enqueue(new Callback<AccountUpdateResponse>() {
            @Override
            public void onResponse(Call<AccountUpdateResponse> call, Response<AccountUpdateResponse> response) {
                if(response.isSuccessful()){
                    Log.d(TAG, "onResponse: " + "Profile update response successfull");
                    updateSharedPreferences();
                    if(!response.body().getImage().equals("success")){
                        getSharedPreferences("login",MODE_PRIVATE).edit().putString("image",response.body().getImage()).apply();
                    }
                    Intent intent2 = new Intent();
                    intent2.putExtra("answer_string","blah");
                    setResult(RESULT_OK,intent2);
                    finish();
                }else{
                    Log.d(TAG, "onResponse: " + "Profile update response unsuccessfull");
                    Snackbar.make(layout,"Oops. Seems like somthing went wrong.",Snackbar.LENGTH_LONG);
                }
                profileProgress.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<AccountUpdateResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: " + "Profile update response failed");
                profileProgress.setVisibility(View.GONE);
                Snackbar.make(layout,"Oops. Something went wrong",Snackbar.LENGTH_LONG);
            }
        });
    }

    private void updateSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("login",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name",name.getText().toString());
        editor.putString("email",email.getText().toString());
        if(imagePath != null){
            editor.putString("imagePath",imagePath);
        }
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Profile Activity: onResume()");
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        Log.d(TAG, "Profile Activity: onPause()");
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "Profile Activity: onSaveInstanceState()");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Profile Activity: onDestroy()");
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Profile Activity: onStop()");
        super.onStop();
    }
}











