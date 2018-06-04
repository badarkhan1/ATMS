package com.example.atms;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;

import com.example.atms.NetworkRemoteSingleton.Remote;
import com.squareup.picasso.Picasso;

import java.io.File;

public class ProfilePicActivity extends AppCompatActivity {

    Toolbar toolbar;
    ImageView dp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_pic);
        toolbar = (Toolbar) findViewById(R.id.profilePic_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.arrow_left);
        dp = (ImageView) findViewById(R.id.dp);
        Intent intent = getIntent();
        if(intent.getExtras() != null){
            Picasso.with(this).load(new File(intent.getExtras().getString("imagePath"))).into(dp);
        }else{
            Picasso.with(this).load(Remote.IP + Remote.USER_IMAGES_URL + getSharedPreferences("login",MODE_PRIVATE).getString("image","")).into(dp);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
