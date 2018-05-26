package com.example.atms;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.atms.Models.DeserializedModels.Data;
import com.example.atms.Services.AuthenticationService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class Drawer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MTAG";
    private com.example.atms.Models.DeserializedModels.Data Data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        EventBus.getDefault().register(this);
        setUserCredentials(navigationView.inflateHeaderView(R.layout.nav_header_drawer));
    }

    private void setUserCredentials(View headerView) {
        Data user = getUser();
        ((TextView) headerView.findViewById(R.id.user_name)).setText(user.getName());
        ((TextView) headerView.findViewById(R.id.user_email)).setText(user.getEmail());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.statistics) {
            // Handle the camera action
        } else if (id == R.id.analytics) {

        } else if (id == R.id.devices) {

        } else if (id == R.id.logout) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    public void attemptLogout(View view){
//        getApplicationContext().getSharedPreferences("login",MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(this,AuthenticationService.class);
        intent.setAction("com.example.atms.ACTION_LOGOUT");
        startService(intent);
    }

    @Subscribe
    public void authenticationServiceLogoutResponse(Integer response){
        switch (response){
            case AuthenticationService.LOGOUT_SUCCESS:
                redirectToLoginAvtivity();
                break;
            case AuthenticationService.LOGIN_FAILED:
                showFailedLogoutResponse();
                break;
            default:break;
        }
    }

    private void showFailedLogoutResponse() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Drawer.this);
        builder.setTitle("Something went wrong!").setMessage("There seems to be an issue.");
        builder.setPositiveButton("Try again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();
    }

    private void redirectToLoginAvtivity() {
        startActivity(new Intent(this,LoginActivity.class));
        finish();
    }

    public Data getUser() {
        Data user = new Data();
        user.setName(getApplicationContext().getSharedPreferences("login",MODE_PRIVATE).getString("name",""));
        user.setEmail(getApplicationContext().getSharedPreferences("login",MODE_PRIVATE).getString("email",""));
//        Log.d(TAG, "getUser: " + getApplicationContext().getSharedPreferences("login",MODE_PRIVATE).getString("name",""));
//        Log.d(TAG, "getUser: " + getApplicationContext().getSharedPreferences("login",MODE_PRIVATE).getString("email",""));
        return user;
    }
}
