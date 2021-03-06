package com.runningtracker.history;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.runningtracker.running.ResultActivity;
import com.runningtracker.running.model.IdHistoryCallback;
import com.runningtracker.running.model.LocationHistoryCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.runningtracker.adapter.HistoryAdapter;
import runningtracker.R;
import com.runningtracker.common.InitializationFirebase;
import com.runningtracker.data.model.history.DateHistoryObject;
import com.runningtracker.data.model.running.LocationObject;

public class HistoryActivity extends AppCompatActivity implements OnMapReadyCallback, AdapterView.OnItemClickListener{

    private ListView lvDateTimeHistory;
    private ArrayList<DateHistoryObject> dateHistoryList;
    private HistoryAdapter historyAdapter;
    private HistoryPresenter historyPresenter;
    private FirebaseFirestore firestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyPresenter = new HistoryPresenter();
        InitializationFirebase initializationFirebase = new InitializationFirebase();
        firestore = initializationFirebase.createFirebase();

        createView();

        /**
         * Create map history
         * */
        MapFragment mapFragmentHistory = (MapFragment) getFragmentManager().findFragmentById(R.id.mapHistory);
        mapFragmentHistory.getMapAsync(this);

        /** call function mapAdapter
         * */

        mapAdapter();
        lvDateTimeHistory.setOnItemClickListener(this);
    }

    /**
     * Create view toolbar
    * */
    private void createView(){
        Toolbar actionBar = findViewById(R.id.actionbarHistory);
        /**Event back main dashboard*/
        actionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private Context getMainActivity(){
        return HistoryActivity.this;
    }

    private void mapAdapter() {
        lvDateTimeHistory = findViewById(R.id.lstDateTimeHistory);
        dateHistoryList = new ArrayList<>();
        historyPresenter.getListIdHistory(firestore, new IdHistoryCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> histories) {
                for(int i = 0; i < histories.size(); i++){
                    Map<String, Object> lastHistory = histories.get(i);
                    String historyID = lastHistory.get("id").toString();
                    long dateTime = Long.valueOf(historyID);
                    Date date =  new Date(dateTime);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
                    String dateHistory = simpleDateFormat.format(date);
                    dateHistoryList.add(new DateHistoryObject(dateHistory, historyID));
                }
                historyAdapter = new HistoryAdapter(getMainActivity(), R.layout.activity_datetime_history, dateHistoryList);
                lvDateTimeHistory.setAdapter(historyAdapter);
            }
        });
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
       // googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);

        /**
         * Draw road tracking of history nearer
        * */
        historyPresenter.getDataLocation(firestore, new LocationHistoryCallback() {
            @Override
            public void dataLocation(List<LocationObject> locationObject) {
                /**
                 * draw road with locationObject >= 2 location
                * */
                if (locationObject.size() > 1) {
                    List<Marker> originMarkers = new ArrayList<>();
                    List<Marker> destinationMarkers = new ArrayList<>();

                    originMarkers.add(googleMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                            .title("Bắt đầu")
                            .position(new LatLng(locationObject.get(0).getLatitudeValue(), locationObject.get(0).getLongitudeValue()))));
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(locationObject.get(0).getLatitudeValue(),
                            locationObject.get(0).getLongitudeValue()), 15);
                    googleMap.animateCamera(cameraUpdate);
                    int sizeObject = locationObject.size();
                    List<LatLng> polyline = new ArrayList<>();
                    for (int i = 0; i < sizeObject; i++) {
                        polyline.add(new LatLng(locationObject.get(i).getLatitudeValue(), locationObject.get(i).getLongitudeValue()));
                    }
                    googleMap.addPolyline(new PolylineOptions()
                            .addAll(polyline)
                            .color(Color.BLUE)
                            .width(10));

                    destinationMarkers.add(googleMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
                            .title("Kết thúc")
                            .position(new LatLng(locationObject.get(sizeObject - 1).getLatitudeValue(), locationObject.get(sizeObject - 1).getLongitudeValue()))));
                }
            }
        });

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        /**
         * Get value from point
        * */
        String selectedFromList = dateHistoryList.get(position).getDateTimeHide();
        /**
         * Set value new activity
        * */
        Intent nextActivity = new Intent(HistoryActivity.this, ResultActivity.class);
        nextActivity.putExtra("idDate", selectedFromList);
        startActivity(nextActivity);
    }
}
