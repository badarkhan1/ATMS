package com.example.atms;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.example.atms.Models.DeserializedModels.Data;
import com.example.atms.Models.EventBusModels.ConstraintResponse;
import com.example.atms.Models.EventBusModels.DeviceResponse;
import com.example.atms.NetworkRemoteSingleton.Remote;
import com.example.atms.Services.AuthenticationService;
import com.google.firebase.iid.FirebaseInstanceId;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;
import com.scichart.charting.model.dataSeries.IDataSeries;
import com.scichart.charting.model.dataSeries.IXyDataSeries;
import com.scichart.charting.visuals.SciChartSurface;
import com.scichart.charting.visuals.axes.AutoRange;
import com.scichart.charting.visuals.axes.NumericAxis;
import com.scichart.charting.visuals.renderableSeries.FastLineRenderableSeries;
import com.scichart.charting.visuals.renderableSeries.data.ISeriesRenderPassData;
import com.scichart.charting.visuals.renderableSeries.data.LineRenderPassData;
import com.scichart.charting.visuals.rendering.RenderPassState;
import com.scichart.core.framework.UpdateSuspender;
import com.scichart.core.model.DoubleValues;
import com.scichart.core.utility.Dispatcher;
import com.scichart.data.model.DoubleRange;
import com.scichart.data.model.ISciList;
import com.scichart.data.numerics.ResamplingMode;
import com.scichart.data.numerics.pointresamplers.IPointResamplerFactory;
import com.scichart.drawing.canvas.RenderSurface;
import com.scichart.drawing.common.IAssetManager2D;
import com.scichart.drawing.utility.ColorUtil;
import com.scichart.extensions.builders.SciChartBuilder;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener , View.OnClickListener{

    private final static int FIFO_CAPACITY = 10;
    private final static long TIME_INTERVAL = 1000;
    private final static double ONE_OVER_TIME_INTERVAL = 8.0;
    private final static double VISIBLE_RANGE_MAX = FIFO_CAPACITY * ONE_OVER_TIME_INTERVAL;
    private final static double GROW_BY = VISIBLE_RANGE_MAX * 0.1;

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
    TextView userName , email, noDevice, liveMac;
    View view;
    SlidingUpPanelLayout slidingUpPanelLayout;
    SciChartSurface surface , surface2;
    SciChartBuilder sciChartBuilder;
    ImageView toggle;
    SharedPreferences sharedPreferences;
    Random random = new Random();
    Bundle bundle;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> schedule;

    private volatile boolean isRunning = true;
    private double t = 0;
    private double tt = 0;
    private double yValue = 0;
    private double yHumValue = 0;

    PusherOptions options;
    Pusher pusher;


    IXyDataSeries<Double, Double> ds1 , ds2;
    DoubleRange xVisibleRange =  new DoubleRange(-GROW_BY, VISIBLE_RANGE_MAX + GROW_BY);
    DoubleRange xVisibleRange2 = new DoubleRange(-GROW_BY, VISIBLE_RANGE_MAX + GROW_BY);

    OkHttpClient client;
    Request request;
    ProgressDialogue progressDialogue;
    ConstraintDialogue constraintDialogue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        options = new PusherOptions();
        options.setCluster("ap2");
        pusher = new Pusher("8095559eec95bef2b633", options);
        bundle = new Bundle();
        Log.d(TAG, "onCreate: " + FirebaseInstanceId.getInstance().getToken());
        Log.d(TAG, "onCreate: " + "Drawer onCreate");
        sharedPreferences = getApplicationContext().getSharedPreferences("device",MODE_PRIVATE);
//        sharedPreferences.edit().putString("device-mac","60:01:94:37:D1:34").apply();
//        sharedPreferences.edit().putString("device-id","1").apply();
        setDrawer();
        liveMac = (TextView) findViewById(R.id.live_mac);
        view = navigationView.inflateHeaderView(R.layout.nav_header_drawer);
        userName = view.findViewById(R.id.user_name);
        email = view.findViewById(R.id.user_email);
        imageView = view.findViewById(R.id.profile_pic);
        toggle = (ImageView) findViewById(R.id.toggle);
        toggle.setOnClickListener(this);
        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if(newState == SlidingUpPanelLayout.PanelState.COLLAPSED){
                    toggle.setImageResource(R.drawable.up_pressed);
                }else if(newState == SlidingUpPanelLayout.PanelState.EXPANDED){
                    toggle.setImageResource(R.drawable.down_pressed);
                }
            }
        });
        surface = (SciChartSurface) findViewById(R.id.live);
        surface2 = (SciChartSurface) findViewById(R.id.live2);
//        surface.setRenderSurface(new RenderSurface(this));
//        surface.setTheme(R.style.SciChart_Bright_Spark);
//        surface.setBackgroundColor(Color.WHITE);
        noDevice = (TextView) findViewById(R.id.no_device);
        SciChartBuilder.init(this);
        sciChartBuilder = SciChartBuilder.instance();
        ds1 = sciChartBuilder.newXyDataSeries(Double.class, Double.class).withFifoCapacity(FIFO_CAPACITY).build();
        ds2 = sciChartBuilder.newXyDataSeries(Double.class, Double.class).withFifoCapacity(FIFO_CAPACITY).build();
        setUserCredentials();
        navigationView.getMenu().getItem(0).setChecked(true);

        // Restore saved instance state
        if (savedInstanceState != null) {
            t = savedInstanceState.getDouble("time");
            yValue = savedInstanceState.getDouble("yValue");

            final double xVisibleRangeMin = savedInstanceState.getDouble("xVisibleRangeMin");
            final double xVisibleRangeMax = savedInstanceState.getDouble("xVisibleRangeMax");
            xVisibleRange.setMinMaxDouble(xVisibleRangeMin, xVisibleRangeMax);

            final ISciList<Double> xValues1 = savedInstanceState.getParcelable("xValues1");
            final ISciList<Double> yValues1 = savedInstanceState.getParcelable("yValues1");
            ds1.append(xValues1, yValues1);
        }
//        getSharedPreferences("last-device",MODE_PRIVATE).edit().putString("device-mac","60:01:94:37:D1:34").commit();
        // add the statistics fragment
        fm = getSupportFragmentManager();
        changeFragment(new StatisticsFragment(),false,false);
        noDevice.setVisibility(View.GONE);
        surface.setVisibility(View.VISIBLE);
        surface2.setVisibility(View.VISIBLE);
        initiateChart();
        initiateChart2();
        if(!sharedPreferences.getString("device-mac","").equals("")){
            liveMac.setText(sharedPreferences.getString("device-mac",""));
            initiateRealTimeStream();
        }else{
            liveMac.setText(R.string.no_device_selected);
        }
        client = new OkHttpClient();
        progressDialogue = new ProgressDialogue();
        constraintDialogue = new ConstraintDialogue();
    }

    private void updateRealTimeStream(String lastId) {
        pusher.unsubscribe(lastId);
        resetChart();
        resetChart2();
//        t = xVisibleRange.getMin();
        liveMac.setText(sharedPreferences.getString("device-mac",""));
        initiateRealTimeStream();
    }

    private void initiateRealTimeStream() {

        Channel channel = pusher.subscribe(String.valueOf(sharedPreferences.getString("device-id","")));

        channel.bind("ReadingEvent", new SubscriptionEventListener() {
            @Override
            public void onEvent(String channelName, String eventName, final String data) {
                Log.d(TAG, "onEvent: " + userName.getText());
                Log.d(TAG, "onEvent: " + data);
                UpdateSuspender.using(surface, new Runnable() {
                    JSONObject jsonObject;
                    @Override
                    public void run() {
                        UpdateSuspender.using(surface, new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    jsonObject = new JSONObject(data);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    yValue = jsonObject.getDouble("temp");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                ds1.append(t, yValue);

                                t += ONE_OVER_TIME_INTERVAL;

                                if(t > VISIBLE_RANGE_MAX)
                                    xVisibleRange.setMinMax(xVisibleRange.getMin() + ONE_OVER_TIME_INTERVAL, xVisibleRange.getMax() + ONE_OVER_TIME_INTERVAL);
                            }
                        });
                    }
                });
                UpdateSuspender.using(surface, new Runnable() {
                    JSONObject jsonObject;
                    @Override
                    public void run() {
                        UpdateSuspender.using(surface2, new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    jsonObject = new JSONObject(data);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    yHumValue = jsonObject.getDouble("hum");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                ds2.append(tt, yHumValue);

                                tt += ONE_OVER_TIME_INTERVAL;

                                if(tt > VISIBLE_RANGE_MAX)
                                    xVisibleRange2.setMinMax(xVisibleRange2.getMin() + ONE_OVER_TIME_INTERVAL, xVisibleRange2.getMax() + ONE_OVER_TIME_INTERVAL);
                            }
                        });
                    }
                });
            }
        });

        pusher.connect();

    }

    protected void initiateChart2() {
        UpdateSuspender.using(surface2, new Runnable() {
            @Override
            public void run() {
                final NumericAxis xAxis = sciChartBuilder.newNumericAxis()
                        .withVisibleRange(xVisibleRange2)
                        .withAutoRangeMode(AutoRange.Never)
                        .withAxisTitle("Clock Tics")
                        .build();

                final NumericAxis yAxis = sciChartBuilder.newNumericAxis()
                        .withGrowBy(new DoubleRange(1.0d, 1.0d))
                        .withAutoRangeMode(AutoRange.Always)
                        .withAxisTitle("Humidity %")
                        .build();

                final AnimatingLineRenderableSeries rs2 = new AnimatingLineRenderableSeries();
                rs2.setDataSeries(ds2);
                rs2.setStrokeStyle(sciChartBuilder.newPen().withColor(ColorUtil.argb(0xFF, 0x40, 0x83, 0xB7)).withAntiAliasing(true).withThickness(3).build());

                Collections.addAll(surface2.getXAxes(), xAxis);
                Collections.addAll(surface2.getYAxes(), yAxis);
                Collections.addAll(surface2.getRenderableSeries(), rs2);
            }
        });
    }

    protected void initiateChart() {
        UpdateSuspender.using(surface, new Runnable() {
            @Override
            public void run() {
                final NumericAxis xAxis = sciChartBuilder.newNumericAxis()
                        .withVisibleRange(xVisibleRange)
                        .withAutoRangeMode(AutoRange.Never)
                        .withAxisTitle("Clock Tics")
                        .build();

                final NumericAxis yAxis = sciChartBuilder.newNumericAxis()
                        .withGrowBy(new DoubleRange(1.0d, 1.0d))
                        .withAutoRangeMode(AutoRange.Always)
                        .withAxisTitle("Temperature C")
                        .build();

                final AnimatingLineRenderableSeries rs1 = new AnimatingLineRenderableSeries();
                rs1.setDataSeries(ds1);
                rs1.setStrokeStyle(sciChartBuilder.newPen().withColor(ColorUtil.argb(0xFF, 0x40, 0x83, 0xB7)).withAntiAliasing(true).withThickness(3).build());

                Collections.addAll(surface.getXAxes(), xAxis);
                Collections.addAll(surface.getYAxes(), yAxis);
                Collections.addAll(surface.getRenderableSeries(), rs1);
            }
        });

//        schedule = scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
//            @Override
//            public void run() {
//                if (!isRunning) {
//                    return;
//                }
//                UpdateSuspender.using(surface, insertRunnable);
//            }
//        }, 0, 100, TimeUnit.MILLISECONDS);
    }

//    private final Runnable insertRunnable = new Runnable() {
//        @Override
//        public void run() {
//            yValue += random.nextDouble() - 0.5;
//
//            ds1.append(t, yValue);
//
//            t += ONE_OVER_TIME_INTERVAL;
//
//            if(t > VISIBLE_RANGE_MAX)
//                xVisibleRange.setMinMax(xVisibleRange.getMin() + ONE_OVER_TIME_INTERVAL, xVisibleRange.getMax() + ONE_OVER_TIME_INTERVAL);
//        }
//    };


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        isRunning = false;
        Log.d(TAG, "onSaveInstanceState: " + "Drawer onSavedInstanceState");
        outState.putDouble("time", t);
        outState.putDouble("yValue", yValue);
        outState.putDouble("xVisibleRangeMin", xVisibleRange.getMinAsDouble());
        outState.putDouble("xVisibleRangeMax", xVisibleRange.getMaxAsDouble());
        outState.putParcelable("xValues1", ds1.getXValues());
        outState.putParcelable("yValues1", ds1.getYValues());
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
            if(slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED){
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }else{
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
        Log.d(TAG, "onResume: Drawer onResume");
        EventBus.getDefault().register(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        Log.d(TAG, "Drawer Activity: onPause()");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Drawer Activity: onDestroy()");
        if (schedule != null)
            schedule.cancel(true);
        super.onDestroy();
    }

    private void resetChart() {
        UpdateSuspender.using(surface, new Runnable() {
            @Override
            public void run() {
                ds1.clear();
            }
        });
    }

    private void resetChart2() {
        UpdateSuspender.using(surface2, new Runnable() {
            @Override
            public void run() {
                ds2.clear();
            }
        });
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Drawer Activity: onStop()");
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceResponse(DeviceResponse deviceResponse){
        if(deviceResponse.getAction().equals("update-shared-preference")){
            String lastid = sharedPreferences.getString("device-id","");
            sharedPreferences.edit().putString("device-mac",deviceResponse.getMac())
                    .putString("device-id",String.valueOf(deviceResponse.getId())).apply();
            updateRealTimeStream(lastid);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                }
            },500);
        }else if(deviceResponse.getAction().equals("device-options")){
            DeviceOptionsBottomFragment deviceOptionsBottomFragment = new DeviceOptionsBottomFragment();
            bundle = new Bundle();
            bundle.putParcelable("device-response",deviceResponse);
            deviceOptionsBottomFragment.setArguments(bundle);
            deviceOptionsBottomFragment.show(getSupportFragmentManager(),deviceOptionsBottomFragment.getTag());
        }else if(deviceResponse.getAction().equals("con-dialogue")){
//            ConstraintDialogue constraintDialogue = new ConstraintDialogue();
//            bundle.putParcelable("device-response",deviceResponse);
//            constraintDialogue.setArguments(bundle);
//            constraintDialogue.show(getSupportFragmentManager(),constraintDialogue.getTag());

            progressDialogue.show(getSupportFragmentManager(),progressDialogue.getTag());
            makeConstraintRequest(deviceResponse);
        }
    }

    private void makeConstraintRequest(final DeviceResponse deviceResponse) {
        request = new Request.Builder().url(Remote.IP + "public/api/constraint/" + deviceResponse.getMac()).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: " + "constraints response failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    Log.d(TAG, "onResponse: " + "constraints response successful");
                    JSONObject jsonObject = new JSONObject();
                    String cResponse = response.body().string();
                    try {
                        jsonObject = new JSONObject(cResponse);
                        if (jsonObject.getString("status").equals("found")){
                            EventBus.getDefault().post(new ConstraintResponse("found",cResponse));
                        }else{
                            EventBus.getDefault().post(new ConstraintResponse("not_found",deviceResponse.getMac()));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    Log.d(TAG, "onResponse: " + "constraints response unsuccessful");
                }
            }
        });
    }

    public void showAlertDialogue(final ConstraintResponse constraintResponse){
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialogue.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(DrawerActivity.this,R.style.DialogueTheme);
                AlertDialog dialogue = builder.setTitle("No constraints found").setMessage("Apply constraints now ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                bundle = new Bundle();
                                bundle.putString("mac",constraintResponse.getResponse());
                                constraintDialogue.setArguments(bundle);
                                constraintDialogue.show(getSupportFragmentManager(),constraintDialogue.getTag());
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).create();
                dialogue.show();
            }
        },800);
    }

    @Subscribe
    public void onConstraintResponse(final ConstraintResponse constraintResponse){
        if(constraintResponse.getStatus().equals("found")){
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialogue.dismiss();
                    bundle = new Bundle();
                    bundle.putString("response",constraintResponse.getResponse());
                    constraintDialogue.setArguments(bundle);
                    constraintDialogue.show(getSupportFragmentManager(),constraintDialogue.getTag());

                }
            },500);
        }else if(constraintResponse.getStatus().equals("not_found")){
            showAlertDialogue(constraintResponse);
        }else if(constraintResponse.getStatus().equals("apply")){
            constraintDialogue.dismiss();
            progressDialogue.show(getSupportFragmentManager(),progressDialogue.getTag());
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    applyConstraints(constraintResponse);
                }
            },500);
        }else if(constraintResponse.getStatus().equals("applied")){
            progressDialogue.dismiss();
//            Snackbar.make(drawer,"Constrained updated successfully",Snackbar.LENGTH_LONG);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(DrawerActivity.this,R.style.DialogueTheme).setMessage("Constraints applied successfully")
                            .setPositiveButton("DONE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }).create().show();
                }
            });
        }else if(constraintResponse.getStatus().equals("not-applied")){
            showAlertDialogueTwo(constraintResponse);
        }
    }

    public void showAlertDialogueTwo(final ConstraintResponse constraintResponse){
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialogue.dismiss();
                new AlertDialog.Builder(DrawerActivity.this,R.style.DialogueTheme).setTitle("There was a problem")
                        .setMessage("Seems like something went wrong")
                        .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                bundle = new Bundle();
                                bundle.putString("response",constraintResponse.getResponse());
                                constraintDialogue.setArguments(bundle);
                                constraintDialogue.show(getSupportFragmentManager(),constraintDialogue.getTag());
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).create().show();

            }
        },800);
    }

    private void applyConstraints(final ConstraintResponse constraintResponse) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("device_key",constraintResponse.getConstraint().getDevice_key());
            jsonObject.put("min_temp",constraintResponse.getConstraint().getMinTemp());
            jsonObject.put("max_temp",constraintResponse.getConstraint().getMaxTemp());
            jsonObject.put("temp_active",1);
            jsonObject.put("min_hum",constraintResponse.getConstraint().getMinHum());
            jsonObject.put("max_hum",constraintResponse.getConstraint().getMaxHum());
            jsonObject.put("hum_active",1);
            jsonObject.put("min_oxy",constraintResponse.getConstraint().getMinOxy());
            jsonObject.put("max_oxy",constraintResponse.getConstraint().getMaxOxy());
            jsonObject.put("oxy_active",1);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String jsonBody = jsonObject.toString();
        RequestBody body = RequestBody.create(MediaType.parse("application/json"),jsonBody);
        Request request = new Request.Builder().url(Remote.IP + "public/api/constraint").post(body).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: " + "apply constraints failed");
                EventBus.getDefault().post(new ConstraintResponse("not-applied",constraintResponse.getResponse(),null));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    Log.d(TAG, "onResponse: " + "apply constraints successful");
                    EventBus.getDefault().post(new ConstraintResponse("applied",null,null));
                }else{
                    Log.d(TAG, "onResponse: " + "apply constraints unsuccessful");
                    EventBus.getDefault().post(new ConstraintResponse("not-applied",constraintResponse.getResponse(),null));
                }
            }
        });
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

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.toggle){
            if(slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED){
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }else if(slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED){
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        }
    }

    // AnimatingLineChartFragment

    private static class AnimatingLineRenderableSeries extends FastLineRenderableSeries implements Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {
        private static final float START_VALUE = 0f;
        private static final float END_VALUE = 1f;

        private double fromX, fromY, toX, toY;

        private final ValueAnimator animator;

        private volatile float animatedFraction;
        private volatile boolean isUpdatesAllowed;

        private AnimatingLineRenderableSeries() {
            animator = ValueAnimator.ofFloat(START_VALUE, END_VALUE);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.setDuration(TIME_INTERVAL);
            animator.addUpdateListener(this);
            animator.addListener(this);
        }

        @Override
        protected void internalUpdateRenderPassData(ISeriesRenderPassData renderPassDataToUpdate, IDataSeries<?, ?> dataSeries, ResamplingMode resamplingMode, IPointResamplerFactory factory) throws Exception {
            super.internalUpdateRenderPassData(renderPassDataToUpdate, dataSeries, resamplingMode, factory);

            // can't animate series with less than 2 points
            if(renderPassDataToUpdate.pointsCount() < 2 ) return;

            final LineRenderPassData lineRenderPassData = (LineRenderPassData) renderPassDataToUpdate;

            final DoubleValues xValues = lineRenderPassData.xValues;
            final DoubleValues yValues = lineRenderPassData.yValues;

            final int pointsCount = lineRenderPassData.pointsCount();
            this.fromX = xValues.get(pointsCount - 2);
            this.fromY = yValues.get(pointsCount - 2);
            this.toX = xValues.get(pointsCount - 1);
            this.toY = yValues.get(pointsCount - 1);

            // need to replace last point to prevent jumping of line because
            // animation runs from UI thread so there could be delay with animation start
            // so chart may render original render pass data few times before animation starts
            xValues.set(pointsCount - 1, fromX);
            yValues.set(pointsCount - 1, fromY);

            // do not update render pass data until animation starts
            isUpdatesAllowed = false;

            Dispatcher.postOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(animator.isRunning())
                        animator.cancel();

                    animator.start();
                }
            });
        }

        @Override
        protected void internalUpdate(IAssetManager2D assetManager, RenderPassState renderPassState) {
            super.internalUpdate(assetManager, renderPassState);

            if(!isUpdatesAllowed) return;

            final LineRenderPassData currentRenderPassData = (LineRenderPassData) getCurrentRenderPassData();

            final double x = fromX + (toX - fromX) * animatedFraction;
            final double y = interpolateLinear(x, fromX, fromY, toX, toY);

            final int indexToSet = currentRenderPassData.pointsCount() - 1;
            currentRenderPassData.xValues.set(indexToSet, x);
            currentRenderPassData.yValues.set(indexToSet, y);

            final float xCoord = currentRenderPassData.getXCoordinateCalculator().getCoordinate(x);
            final float yCoord = currentRenderPassData.getYCoordinateCalculator().getCoordinate(y);

            currentRenderPassData.xCoords.set(indexToSet, xCoord);
            currentRenderPassData.yCoords.set(indexToSet, yCoord);
        }

        private static double interpolateLinear(double x, double x1, double y1, double x2, double y2) {
            return y1 + (y2 - y1) * (x - x1) / (x2 - x1);
        }

        @Override
        public void onAnimationStart(Animator animation) {
            // allow updated of render pass data after animation starts
            isUpdatesAllowed = true;

            this.animatedFraction = START_VALUE;
            invalidateElement();
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            this.animatedFraction = END_VALUE;
            invalidateElement();
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            this.animatedFraction = START_VALUE;
            invalidateElement();
        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            this.animatedFraction = animation.getAnimatedFraction();
            invalidateElement();
        }
    }

}

















