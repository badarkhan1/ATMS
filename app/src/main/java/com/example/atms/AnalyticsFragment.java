package com.example.atms;


import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.atms.Models.EventBusModels.AnalyticsResponse;
import com.scichart.charting.model.dataSeries.IXyDataSeries;
import com.scichart.charting.modifiers.CursorModifier;
import com.scichart.charting.modifiers.ModifierGroup;
import com.scichart.charting.modifiers.PinchZoomModifier;
import com.scichart.charting.modifiers.ZoomPanModifier;
import com.scichart.charting.visuals.SciChartSurface;
import com.scichart.charting.visuals.annotations.AnnotationCoordinateMode;
import com.scichart.charting.visuals.annotations.HorizontalAnchorPoint;
import com.scichart.charting.visuals.annotations.TextAnnotation;
import com.scichart.charting.visuals.annotations.VerticalAnchorPoint;
import com.scichart.charting.visuals.axes.IAxis;
import com.scichart.charting.visuals.renderableSeries.FastLineRenderableSeries;
import com.scichart.charting.visuals.renderableSeries.FastMountainRenderableSeries;
import com.scichart.data.model.DoubleRange;
import com.scichart.drawing.canvas.RenderSurface;
import com.scichart.extensions.builders.SciChartBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AnalyticsFragment extends Fragment {

    private static final String TAG = "MTAG";
    SciChartSurface surface;
    String date;
    Double temp;
    SciChartBuilder sciChartBuilder = SciChartBuilder.instance();
    ProgressBar progressBar;
    private ModifierGroup zoomingModifiers;
    private CursorModifier cursorModifier;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: " + getActivity());
        EventBus.getDefault().register(this);
        makeRequest();
    }

    private void makeRequest() {
        OkHttpClient client = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        Request request = builder.url("http://api.openweathermap.org/data/2.5/forecast?q=LAHORE,PK&units=metric&appid=2e19b9b63e38d1c496604e53f897dbc5")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: " + "Open weather api response failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                List<Date> dates = new ArrayList<>();
                List<Double> temps = new ArrayList<>();
                List<Double> hums = new ArrayList<Double>();
//                SimpleDateFormat format = new SimpleDateFormat("yy-mm-dd hh:mm:ss", Locale.US);
                JSONObject jsonObject=null;
                if(response.isSuccessful()){
                    Log.d(TAG, "onResponse: " + "Open weather api response success");
                    try {
                        jsonObject = new JSONObject(response.body().string());
//                        Log.d(TAG, "onResponse: " + jsonObject.getString("cod"));
                        JSONArray jarray = jsonObject.getJSONArray("list");
                        for(int i = 0; i < jarray.length(); i++){
                            JSONObject day = jarray.getJSONObject(i);
                            JSONObject main = day.getJSONObject("main");
                            dates.add(new Date(day.getLong("dt")*1000));
                            temps.add(main.getDouble("temp_max"));
                            hums.add(main.getDouble("humidity"));
                        }
                        EventBus.getDefault().post(new AnalyticsResponse(dates,temps,hums));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    Log.d(TAG, "onResponse: " + "Open weather api response unsuccessfull");
                }
            }
        });
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getResponse(AnalyticsResponse response){

        final IAxis xAxis = sciChartBuilder.newDateAxis().withGrowBy(new DoubleRange(0.1d, 0.1d)).build();
        final IAxis yAxis = sciChartBuilder.newNumericAxis().withGrowBy(new DoubleRange(0.1d, 0.1d)).build();
        final IXyDataSeries<Date, Double> dataSeries = sciChartBuilder.newXyDataSeries(Date.class, Double.class).withSeriesName("Max. Temperature").build();
        final IXyDataSeries<Date, Double> humSeries = sciChartBuilder.newXyDataSeries(Date.class, Double.class).withSeriesName("Humidity").build();

        dataSeries.append(response.getDates(),response.getTemps());
        humSeries.append(response.getDates(),response.getHums());

        final FastMountainRenderableSeries rSeries = sciChartBuilder.newMountainSeries()
                .withDataSeries(dataSeries)
                .withStrokeStyle(0xDDDBE0E1, 1f, true)
                .withAreaFillLinearGradientColors(0xDDACBCCA, 0x88439AAF)
                .build();

        final FastLineRenderableSeries sSeries = sciChartBuilder.newLineSeries().withDataSeries(humSeries).withStrokeStyle(0xFF279B27, 1f, true).build();

//        final StackedMountainRenderableSeries s1 = sciChartBuilder.newStackedMountain().withDataSeries(dataSeries).withLinearGradientColors(0xDDDBE0E1, 0x88B6C1C3).build();
//        final StackedMountainRenderableSeries s2 = sciChartBuilder.newStackedMountain().withDataSeries(humSeries).withLinearGradientColors(0xDDACBCCA, 0x88439AAF).build();

//        final VerticallyStackedMountainsCollection seriesCollection = new VerticallyStackedMountainsCollection();
//        seriesCollection.add(s1);
//        seriesCollection.add(s2);

        TextAnnotation textAnnotation = sciChartBuilder.newTextAnnotation()
                .withX1(0.5)
                .withY1(0.5)
                .withFontStyle(Typeface.DEFAULT_BOLD, 42, 0x22FFFFFF)
                .withCoordinateMode(AnnotationCoordinateMode.Relative)
                .withHorizontalAnchorPoint(HorizontalAnchorPoint.Center)
                .withVerticalAnchorPoint(VerticalAnchorPoint.Center)
                .withText("ATMS")
                .withTextGravity(Gravity.CENTER)
                .build();

//        TextAnnotation textAnnotation = sciChartBuilder.newTextAnnotation()
//                .withX1(5.0)
//                .withY1(55.0)
//                .withText("Hello World!")
//                .withHorizontalAnchorPoint(HorizontalAnchorPoint.Center)
//                .withVerticalAnchorPoint(VerticalAnchorPoint.Center)
//                .withFontStyle(20, ColorUtil.White)
//                .build();

        // Create interactivity modifiers
//        ModifierGroup chartModifiers = sciChartBuilder.newModifierGroup()
//                .withPinchZoomModifier().withReceiveHandledEvents(true).build()
//                .withZoomPanModifier().withReceiveHandledEvents(true).build()
//                .build();

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

        surface.setTheme(R.style.SciChart_ChromeStyle);
        // Add the Y axis to the YAxes collection of the surface
        Collections.addAll(surface.getYAxes(), yAxis);

        // Add the X axis to the XAxes collection of the surface
        Collections.addAll(surface.getXAxes(), xAxis);

        // Add the annotation to the Annotations collection of the surface
        Collections.addAll(surface.getAnnotations(), textAnnotation);

        // Add the interactions to the ChartModifiers collection of the surface
        Collections.addAll(surface.getChartModifiers(), chartModifiers);

        // Add the temp series
        Collections.addAll(surface.getRenderableSeries(), rSeries);

        // Add the hum series
        Collections.addAll(surface.getRenderableSeries(), sSeries);
        surface.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_analytics, container, false);

        surface = view.findViewById(R.id.surface_view);
        surface.setRenderSurface(new RenderSurface(getActivity()));
        surface.setBackgroundColor(Color.WHITE);
        progressBar = view.findViewById(R.id.analytics_pro);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((DrawerActivity)getActivity()).setNavigationItemChecked(1);
    }

}
