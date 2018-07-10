package runningtracker.suggestplace;

import android.location.Location;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import runningtracker.data.model.suggestplace.SuggestLocation;
import runningtracker.model.suggets_place.Distance;
import runningtracker.model.suggets_place.Duration;
import runningtracker.model.suggets_place.ItemSuggest;
import runningtracker.model.suggets_place.Route;
import runningtracker.common.DistanceTwoPoint;
import runningtracker.data.source.suggestlocation.SuggestRepository;
import runningtracker.data.source.suggestlocation.SuggetsCallback;

public class DirectionFinderPresenter {
    private static final String TAG = "DirectionFinderPresenter" ;

    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyDnwLF2-WfK8cVZt9OoDYJ9Y8kspXhEHfI";
    private DirectionFinderListener listener;
    private String origin;
    private String destination;
    private SuggestRepository suggestRepository = new SuggestRepository();

    public DirectionFinderPresenter(DirectionFinderListener listener, String origin, String destination) {
        this.listener = listener;
        this.origin = origin;
        this.destination = destination;
    }

    public DirectionFinderPresenter(){
    }

    public void execute() throws UnsupportedEncodingException {
        listener.onDirectionFinderStart();
        new DownloadRawData().execute(createUrl());
    }

    private String createUrl() throws UnsupportedEncodingException {
        String urlOrigin = URLEncoder.encode(origin, "utf-8");
        String urlDestination = URLEncoder.encode(destination, "utf-8");

        return DIRECTION_URL_API + "origin=" + urlOrigin + "&destination=" + urlDestination + "&key=" + GOOGLE_API_KEY;
    }

    private class DownloadRawData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String link = params[0];
            try {
                URL url = new URL(link);
                InputStream is = url.openConnection().getInputStream();
                StringBuffer buffer = new StringBuffer();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                parseJSon(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseJSon(String data) throws JSONException {
        if (data == null)
            return;

        List<Route> routes = new ArrayList<Route>();
        JSONObject jsonData = new JSONObject(data);
        JSONArray jsonRoutes = jsonData.getJSONArray("routes");
        for (int i = 0; i < jsonRoutes.length(); i++) {
            JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
            Route route = new Route();

            JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
            JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
            JSONObject jsonLeg = jsonLegs.getJSONObject(0);
            JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
            JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
            JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
            JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");

            //route.distance = new Distance(jsonDistance.getString("text"), jsonDistance.getInt("value"));
           // route.duration = new Duration(jsonDuration.getString("text"), jsonDuration.getInt("value"));
            route.endAddress = jsonLeg.getString("end_address");
            route.startAddress = jsonLeg.getString("start_address");
            route.startLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
            route.endLocation = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
            route.points = decodePolyLine(overview_polylineJson.getString("points"));

            routes.add(route);
        }

        listener.onDirectionFinderSuccess(routes);
    }

    private List<LatLng> decodePolyLine(final String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }

    /**
     * @param : list item chosen type
     * @return : list location suggest
    * */
    public List<Location> getResultPlace(List<SuggestLocation> suggestLocationList, ArrayList<ItemSuggest> itemSuggestList){
        List<Location> listLocation = new ArrayList<>();
        for(int i = 0; i < suggestLocationList.size(); i++){
            SuggestLocation suggestLocation = new SuggestLocation();

            suggestLocation = suggestLocationList.get(i);
            if((suggestLocation.getTypePlace()-1) == itemSuggestList.get(0).getPosition()){
                Location location = new Location("A");
                location.setLatitude(suggestLocation.getLatitudeValue());
                location.setLongitude(suggestLocation.getLongitudeValue());
                listLocation.add(location);
            }
        }
        return listLocation;
    }

    /**
     * @param : list item chosen, list location get list location suggest, Google map
     * @return : list location suggest
    * */
    public void setMarkerLocation(final ArrayList<ItemSuggest> itemSuggestList, final GoogleMap mMap, final SuggestCallback suggestCallback)
    {
         final List<Location>[] listLocation = new ArrayList[1];
        suggestRepository.getAllSuggestLocation(new SuggetsCallback() {
            @Override
            public void onSuccess(List<SuggestLocation> suggestLocationList) {
                listLocation[0] = getResultPlace(suggestLocationList, itemSuggestList);
                for(int i = 0; i < listLocation[0].size(); i++){
                    LatLng location = new LatLng(listLocation[0].get(i).getLatitude(), listLocation[0].get(i).getLongitude());
                    mMap.addMarker(new MarkerOptions().position(location).title("Địa điểm gợi ý"));
                }
                suggestCallback.getListLocation( listLocation[0]);
            }
        });
    }


    public Location locationDistanceMin(Location location, List<Location> listLocation){
        DistanceTwoPoint distance = new DistanceTwoPoint();
        Location minLocation = new Location("A");

        float temp = 10000;

        for(int i = 0; i < listLocation.size(); i++){

            Location tempLocation = new Location("");
            tempLocation.setLatitude( listLocation.get(i).getLatitude());
            tempLocation.setLongitude( listLocation.get(i).getLongitude());

            if(distance.DistanceLocation(location,tempLocation) < temp ){
                temp = distance.DistanceLocation(location,tempLocation);
                minLocation = tempLocation;
            }
        }
        return minLocation;
    }
}
