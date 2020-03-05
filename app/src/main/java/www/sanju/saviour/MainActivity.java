package www.sanju.saviour;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Circle mCircle;
    public static final String USER_NAME = "Karthick";


    private static final String URL = "https://api.thingspeak.com/channels/1009199/feeds/last.json?api_key=VRMW2SVE9KK9YD50";
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        AndroidNetworking.initialize(getApplicationContext());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_main);
        mapFragment.getMapAsync(this);

        fetchLatestAccident();
    }



    private void fetchLatestAccident() {
        AndroidNetworking.get(URL)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "onResponse: " + response);

                        if (response.length() > 0) {

                            try {
                                JSONObject result = new JSONObject(response);

                                String lat = result.getString("field1");
                                String lon = result.getString("field2");
                                int userid = result.getInt("entry_id");
                                String id = String.valueOf(userid);


                                double latitude=Double.parseDouble(lat);
                                double longitude=Double.parseDouble(lon);

                                showLocation(latitude, longitude, id);





                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                           // Toast.makeText(MainActivity.this, "Data fetched" + response, Toast.LENGTH_SHORT).show();

                        } else {

                            Toast.makeText(MainActivity.this, "No Accident Detected", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        Toast.makeText(MainActivity.this, " " + error, Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

    }

    private void showLocation(double latitude, double longitude, String id) {

        LatLng accidentLocation = new LatLng(latitude, longitude);

//        // circle option & custom marker
                         mCircle = mMap.addCircle(new CircleOptions()
                                .center(accidentLocation)
                                .radius(50)
                                .strokeColor(getResources().getColor(R.color.accident_zone))
                                .fillColor(getResources().getColor(R.color.accident_zone_trans)));

                        mMap.addMarker(new MarkerOptions().position(accidentLocation).title(id.concat(" ").concat(USER_NAME))
                                .icon(bitmapDescriptorFromVector(MainActivity.this, R.drawable.ic_warning_black_24dp)));
        mMap.setMyLocationEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(accidentLocation, 18f));
    }


    // custom marker for saviour
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


}
