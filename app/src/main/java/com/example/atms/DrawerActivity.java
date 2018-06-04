package com.example.atms;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.atms.Models.DeserializedModels.Data;
import com.example.atms.NetworkRemoteSingleton.Remote;
import com.example.atms.Services.AuthenticationService;
import com.scichart.extensions.builders.SciChartBuilder;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;

public class DrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MTAG";
    private static final int UPDATE_USER_PROFILE = 1;
    private com.example.atms.Models.DeserializedModels.Data Data;
    protected DrawerLayout drawer;
    protected NavigationView navigationView;
    protected FragmentManager fm;
    protected FragmentTransaction fragmentTransaction;
    protected Toolbar toolbar;
    CircleImageView imageView;
    boolean profileMenuIcon = true;
    boolean infoIcon = false;
    Fragment fragment = null;
    String title = null;
    TextView userName , email;
    View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        setDrawer();
        view = navigationView.inflateHeaderView(R.layout.nav_header_drawer);
        userName = view.findViewById(R.id.user_name);
        email = view.findViewById(R.id.user_email);
        imageView = view.findViewById(R.id.profile_pic);

        EventBus.getDefault().register(this);
        setUserCredentials();
        navigationView.getMenu().getItem(0).setChecked(true);

        // add the statistics fragment
        fm = getSupportFragmentManager();
        changeFragment(new StatisticsFragment(),false,false);
        SciChartBuilder.init(this);
    }

    private void setDrawer() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Statistics");

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setUserCredentials() {
        Data user = getUser();
        userName.setText(user.getName());
        email.setText(user.getEmail());
        Picasso.with(this).load(Remote.IP + Remote.USER_IMAGES_URL + user.getImage()).centerCrop()
                .resize(80,80).into(imageView);
        Log.d(TAG, "setUserCredentials: " + Remote.IP + Remote.USER_IMAGES_URL + user.getImage());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

//        int fragments= getSupportFragmentManager().getBackStackEntryCount();
//        if(fragments == 1){
//            finish();
//            return;
//        }

        else{
            StatisticsFragment fragment = (StatisticsFragment) getSupportFragmentManager().findFragmentByTag("com.example.atms.StatisticsFragment");
            if(fragment != null){
                finish();
            }else{
                profileMenuIcon = true;
                infoIcon = false;
                toolbar.setTitle("Statistics");
                invalidateOptionsMenu();
                changeFragment(new StatisticsFragment(),false,false);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer, menu);
        menu.findItem(R.id.profile_menu_icon).setVisible(profileMenuIcon);
        menu.findItem(R.id.info).setVisible(infoIcon);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.dots) {
            BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
            bottomSheetFragment.show(getSupportFragmentManager(),bottomSheetFragment.getTag());
            return true;
        }else if(id == R.id.profile_menu_icon){
            Intent intent = new Intent(this,ProfileActivity.class);
            startActivityForResult(intent,UPDATE_USER_PROFILE);
        }else if(id == R.id.info){
            AlertDialog.Builder builder = new AlertDialog.Builder(DrawerActivity.this);
            builder.setTitle("Info").setMessage("Temperature & Humdity Forecast for Lahore. \n\n Next 5 Days With Interval of 3 hours");
            builder.create().show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void setNavigationItemChecked(int id){
        Menu menu = navigationView.getMenu();
        MenuItem item = menu.getItem(id);
        item.setChecked(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            if (requestCode == UPDATE_USER_PROFILE) {
                updateDrawerActivity();
            }
        }
    }

    private void updateDrawerActivity() {
        userName.setText(getSharedPreferences("login",MODE_PRIVATE).getString("name",""));
        email.setText(getSharedPreferences("login",MODE_PRIVATE).getString("email",""));
        if(!getSharedPreferences("login",MODE_PRIVATE).getString("imagePath","").equals("")){
            Picasso.with(this).load(new File(getSharedPreferences("login",MODE_PRIVATE).getString("imagePath",""))).into(imageView);
        }
        Snackbar.make(drawer,"Account information updated successfully.",Snackbar.LENGTH_LONG).show();
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        navigationView.setCheckedItem(id);

        if(item.getItemId() == R.id.logout){

        }else {
            if (id == R.id.statistics) {
                fragment = new StatisticsFragment();
                title = "Statistics";
                infoIcon = false;
                profileMenuIcon = true;
            } else if (id == R.id.analytics) {
                fragment = new AnalyticsFragment();
                title = "Analytics";
                infoIcon = true;
                profileMenuIcon = false;
            } else if (id == R.id.devices) {
                fragment = new DeviceFragment();
                title = "Devices";
                infoIcon = false;
                profileMenuIcon = false;
            }
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    toolbar.setTitle(title);
//                    invalidateOptionsMenu();
//                }
//            },500);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    toolbar.setTitle(title);
                    invalidateOptionsMenu();
                    changeFragment(fragment,false,false);
                }
            },400);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Drawer Activity: onResume()");
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        Log.d(TAG, "Drawer Activity: onPause()");
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "Drawer Activity: onSaveInstanceState()");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Drawer Activity: onDestroy()");
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Drawer Activity: onStop()");
        super.onStop();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(DrawerActivity.this);
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
        user.setImage(getApplicationContext().getSharedPreferences("login",MODE_PRIVATE).getString("image",""));
//        Log.d(TAG, "getUser: " + getApplicationContext().getSharedPreferences("login",MODE_PRIVATE).getString("name",""));
//        Log.d(TAG, "getUser: " + getApplicationContext().getSharedPreferences("login",MODE_PRIVATE).getString("email",""));
        return user;
    }

    private void changeFragment(Fragment frag, boolean saveInBackstack, boolean animate) {
        String backStateName = ((Object) frag).getClass().getName();

        try {
            FragmentManager manager = getSupportFragmentManager();
            boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

            if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) {
                //fragment not in back stack, create it.
                FragmentTransaction transaction = manager.beginTransaction();

//                if (animate) {
//                    Log.d(TAG, "Change Fragment: animate");
//                    transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
//                }

                transaction.replace(R.id.container, frag, backStateName);

                if (saveInBackstack) {
                    Log.d(TAG, "Change Fragment: addToBackTack " + backStateName);
                    transaction.addToBackStack(backStateName);
                } else {
                    Log.d(TAG, "Change Fragment: NO addToBackTack");
                }

                transaction.commit();
            } else {
                // custom effect if fragment is already instanciated
            }
        } catch (IllegalStateException exception) {
            Log.w(TAG, "Unable to commit fragment, could be activity as been killed in background. " + exception.toString());
        }
    }
}
