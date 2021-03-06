package com.runningtracker.running;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.runningtracker.running.model.LocationHistoryCallback;

import java.util.ArrayList;
import java.util.List;

import runningtracker.R;
import com.runningtracker.common.InitializationFirebase;
import com.runningtracker.common.MyLocation;
import com.runningtracker.data.model.running.LocationObject;

public class TrackTabFragment extends Fragment implements OnMapReadyCallback {

    public TrackTabFragment() {
    }

    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private static PresenterRunning presenterRunning;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the gridview_dashboard_item for this fragment
        View inflatedLayout = inflater.inflate(R.layout.activity_result_tab_map, container, false);
        ResultActivity.tabFragmentLayouts.add((ViewGroup) inflatedLayout);

        presenterRunning = new PresenterRunning();

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            mapFragment.getMapAsync(this);
        }
        // R.id.map is a FrameLayout, not a Fragment
        getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();

        return inflatedLayout;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        mMap.setMyLocationEnabled(true);
        MyLocation myLocation = new MyLocation();
        Location L = myLocation.getMyLocation(getActivity());
        if (L != null) {
            LatLng latLng;
            latLng = new LatLng(L.getLatitude(), L.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
            mMap.moveCamera(cameraUpdate);
        }
        drawDistanceHistory();
    }
    /**
     * Draw distance history of user
    * */
    public void drawDistanceHistory(){

        InitializationFirebase initializationFirebase = new InitializationFirebase();
        FirebaseFirestore firestore  = initializationFirebase.createFirebase();
        /**
         * If value default
        * */
        if(ResultActivity.idDateHistory == null) {
            presenterRunning.getDataLocation(firestore, new LocationHistoryCallback() {
                @Override
                public void dataLocation(List<LocationObject> locationObject) {
                    if (locationObject.size() > 1) {
                        List<Marker> originMarkers = new ArrayList<>();
                        List<Marker> destinationMarkers = new ArrayList<>();

                        originMarkers.add(mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                                .title("Bắt đầu")
                                .position(new LatLng(locationObject.get(0).getLatitudeValue(), locationObject.get(0).getLongitudeValue()))));
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(locationObject.get(0).getLatitudeValue(),
                                locationObject.get(0).getLongitudeValue()), 15);
                        mMap.animateCamera(cameraUpdate);
                        int sizeObject = locationObject.size();
                        List<LatLng> polyline = new ArrayList<>();
                        for (int i = 0; i < sizeObject; i++) {
                            polyline.add(new LatLng(locationObject.get(i).getLatitudeValue(), locationObject.get(i).getLongitudeValue()));
                        }
                        mMap.addPolyline(new PolylineOptions()
                                .addAll(polyline)
                                .width(10)
                                .color(Color.BLUE));

                        destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
                                .title("Kết thúc")
                                .position(new LatLng(locationObject.get(locationObject.size() - 1).getLatitudeValue(), locationObject.get(locationObject.size() - 1).getLongitudeValue()))));
                    }
                }
            });
            /**
             * If user chose date history tracking
            * */
        }else{
            presenterRunning.getListLocationHistory(ResultActivity.idDateHistory, firestore, new LocationHistoryCallback() {
                @Override
                public void dataLocation(List<LocationObject> locationObject) {

                    List<Marker> originMarkers = new ArrayList<>();
                    List<Marker> destinationMarkers = new ArrayList<>();

                    originMarkers.add(mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                            .title("Bắt đầu")
                            .position(new LatLng(locationObject.get(0).getLatitudeValue(), locationObject.get(0).getLongitudeValue()))));
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(locationObject.get(0).getLatitudeValue(),
                            locationObject.get(0).getLongitudeValue()), 15);
                    mMap.animateCamera(cameraUpdate);
                    int sizeObject = locationObject.size();
                    List<LatLng> polyline = new ArrayList<>();
                    for (int i = 0; i < sizeObject; i++) {
                        polyline.add(new LatLng(locationObject.get(i).getLatitudeValue(), locationObject.get(i).getLongitudeValue()));
                    }
                    mMap.addPolyline(new PolylineOptions()
                            .addAll(polyline)
                            .width(10)
                            .color(Color.BLUE));

                    destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
                            .title("Kết thúc")
                            .position(new LatLng(locationObject.get(locationObject.size() - 1).getLatitudeValue(), locationObject.get(locationObject.size() - 1).getLongitudeValue()))));
                }
            });
        }

    }
}
