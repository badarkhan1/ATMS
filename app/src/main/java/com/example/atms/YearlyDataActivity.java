package com.example.atms;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.atms.Models.EventBusModels.AnalyticsResponse;
import com.example.atms.Models.EventBusModels.YearlyResponse;
import com.example.atms.NetworkRemoteSingleton.Remote;
import com.scichart.charting.model.dataSeries.IXyDataSeries;
import com.scichart.charting.modifiers.CursorModifier;
import com.scichart.charting.modifiers.ModifierGroup;
import com.scichart.charting.modifiers.PinchZoomModifier;
import com.scichart.charting.modifiers.ZoomPanModifier;
import com.scichart.charting.visuals.SciChartSurface;
import com.scichart.charting.visuals.axes.IAxis;
import com.scichart.charting.visuals.renderableSeries.FastMountainRenderableSeries;
import com.scichart.core.framework.UpdateSuspender;
import com.scichart.drawing.canvas.RenderSurface;
import com.scichart.extensions.builders.SciChartBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class YearlyDataActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    Toolbar toolbar;
    TextView dev_mac;
    SciChartSurface surface;
    SciChartBuilder sciChartBuilder;
    CursorModifier cursorModifier;
    ModifierGroup zoomingModifiers;
    String devMac;
    String devId;
    AppCompatSpinner spinner;
    ArrayAdapter spinnerAdapter;
    private static final String TAG = "MTAG";
    ProgressBar progressBar;
    IAxis xBottomAxis;
    IAxis yRightAxis;
    IXyDataSeries<Date, Double> dataSeries;
    FastMountainRenderableSeries rSeries , currentSeries;
    private String selectedSeries;
    private int seriesColor;
    private int startColor;
    private int endColor;
    TextView noData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yearly_data);
        toolbar = (Toolbar) findViewById(R.id.yearly_tool);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.arrow_left);
        Bundle bundle = getIntent().getExtras();
        noData = (TextView) findViewById(R.id.no_data);
        dev_mac = (TextView) findViewById(R.id.dev_mac);
        if(bundle != null){
            devMac = bundle.getString("dev-mac");
            devId  = String.valueOf(bundle.getString("dev-id"));
            dev_mac.setText(devMac);
        }
        surface = (SciChartSurface) findViewById(R.id.yearly_surface);
        progressBar = (ProgressBar) findViewById(R.id.yearly_pro);
        surface.setRenderSurface(new RenderSurface(this));
        surface.setBackgroundColor(Color.WHITE);
        surface.setTheme(R.style.SciChart_ChromeStyle);
        spinner = (AppCompatSpinner) findViewById(R.id.tool_head);
        spinnerAdapter = ArrayAdapter.createFromResource(this,R.array.data_attributes,R.layout.spinner_list_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(this);
        SciChartBuilder.init(this);
        sciChartBuilder = SciChartBuilder.instance();
        initChart();
    }

    public void initChart(){
        //        final PriceSeries priceData = DataManager.getInstance().getPriceDataIndu(getActivity());
        xBottomAxis = sciChartBuilder.newDateAxis().withGrowBy(0.1d, 0.1d).build();
        yRightAxis = sciChartBuilder.newNumericAxis().withGrowBy(0.1d, 0.1d).build();
        dataSeries = sciChartBuilder.newXyDataSeries(Date.class, Double.class).build();

        rSeries = sciChartBuilder.newMountainSeries()
                .withDataSeries(dataSeries)
                .withAreaFillLinearGradientColors(startColor,endColor)
                .build();

        PinchZoomModifier pinchZoomModifier = new PinchZoomModifier();
        pinchZoomModifier.setReceiveHandledEvents(true);

        ZoomPanModifier zoomPanModifier = new ZoomPanModifier();
        zoomPanModifier.setReceiveHandledEvents(true);

        zoomingModifiers = new ModifierGroup();
        Collections.addAll(zoomingModifiers.getChildModifiers(), pinchZoomModifier, zoomPanModifier);
        zoomingModifiers.setIsEnabled(false);

        cursorModifier = new CursorModifier();
        final ModifierGroup  chartModifiers = sciChartBuilder.newModifierGroup()
                .withLegendModifier().withShowCheckBoxes(false).build()
                .withModifier(cursorModifier)
                .withModifier(zoomingModifiers)
                .withZoomExtentsModifier().build()
                .build();

        Collections.addAll(surface.getXAxes(), xBottomAxis);
        Collections.addAll(surface.getYAxes(), yRightAxis);
//        Collections.addAll(surface.getChartModifiers(), sciChartBuilder.newModifierGroupWithDefaultModifiers().build());
        // Add the interactions to the ChartModifiers collection of the surface
        Collections.addAll(surface.getChartModifiers(), chartModifiers);
//                sciChartBuilder.newAnimator(rSeries).withWaveTransformation().withInterpolator(new DecelerateInterpolator()).withDuration(3000).withStartDelay(350).start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void initiateChart(YearlyResponse yearlyResponse) {
        if(yearlyResponse.getSelected().equals("noData")){
            progressBar.setVisibility(View.GONE);
            noData.setVisibility(View.VISIBLE);
        }else{
            switch (yearlyResponse.getSelected()){
                case "temp":
                    selectedSeries = "Temperature";
                    seriesColor    = 0xAAFFC9A8;
                    startColor     = 0xAAFF8D42;
                    endColor       = 0x88090E11;
                    break;
                case "hum":
                    selectedSeries = "Humidity";
                    seriesColor    = 0xAAFFC9A8;
                    startColor     = 0xAAFF8D42;
                    endColor       = 0x880AAE11;
                    break;
                case "oxygen":
                    selectedSeries = "Oxygen";
                    seriesColor    = 0xAAFFC9A8;
                    startColor     = 0xAAFF8D42;
                    endColor       = 0x88AF0E11;
                    break;
                default: break;
            }
            dataSeries.clear();
            surface.getRenderableSeries().remove(rSeries);
            dataSeries = sciChartBuilder.newXyDataSeries(Date.class, Double.class).withSeriesName(selectedSeries).build();
            dataSeries.append(yearlyResponse.getDates(),yearlyResponse.getData());
            rSeries = sciChartBuilder.newMountainSeries()
                    .withDataSeries(dataSeries)
                    .withAreaFillLinearGradientColors(startColor,endColor)
                    .build();

            Collections.addAll(surface.getRenderableSeries(),rSeries);
            progressBar.setVisibility(View.GONE);
            surface.setVisibility(View.VISIBLE);
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

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        EventBus.getDefault().register(this);
        super.onResume();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        noData.setVisibility(View.GONE);
        surface.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        String selected;
        switch (i){
            case 0: selected = "temp"; break;
            case 1: selected = "hum"; break;
            case 2: selected = "oxygen"; break;
            default: selected = "temp"; break;
        }
        makeRequest(selected);
    }

    private void makeRequest(final String selected) {
        OkHttpClient client = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        Request request = builder.url(Remote.IP + "public/api/reading/resize-temp/" + devMac + "/" + selected + "")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: " + "Yearly response failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                List<Date> dates = new ArrayList<>();
                List<Double> data = new ArrayList<>();

                JSONArray jsonArray=null;
                if(response.isSuccessful()){
                    Log.d(TAG, "onResponse: " + "Yearly response success");
                    try {
                        jsonArray = new JSONArray(response.body().string());
//                        Log.d(TAG, "onResponse: " + jsonObject.getString("cod"));

                        if(jsonArray.length() != 0){
                            for(int i = 0; i < jsonArray.length(); i++){
                                JSONArray subArray = jsonArray.getJSONArray(i);
                                dates.add(new Date(subArray.getLong(0)));
                                data.add(subArray.getDouble(1));
                            }
                            EventBus.getDefault().post(new YearlyResponse(dates,data,selected));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    Log.d(TAG, "onResponse: " + "Yearly response unsuccessfull");
                    EventBus.getDefault().post(new YearlyResponse(dates,data,"noData"));
                }
            }
        });
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}




















