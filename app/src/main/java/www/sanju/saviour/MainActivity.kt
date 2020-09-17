package www.sanju.saviour

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy.KEEP
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    private var mCircle: Circle? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AndroidNetworking.initialize(applicationContext)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map_main) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        // get accidents from thinkSpeak
        fetchLatestAccident()
        initWorker()
    }


    private fun initWorker() {

        // worker constraints
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()


        // periodic worker for every 15 minutes
        val notificationWorkRequest: PeriodicWorkRequest = PeriodicWorkRequest.Builder(MyWorker::class.java, 15,
                TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()


        // enqueue work
        val workManager = WorkManager.getInstance(applicationContext)
        workManager.enqueueUniquePeriodicWork(JOB_TAG, KEEP, notificationWorkRequest)

    }

    private fun fetchLatestAccident() {
        AndroidNetworking.get(URL)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(object : StringRequestListener {
                    override fun onResponse(response: String) {
                        Log.i(TAG, "onResponse: $response")
                        if (response.isNotEmpty()) {
                            try {
                                val result = JSONObject(response)
                                val lat = result.getString("field1")
                                val lon = result.getString("field2")
                                val userid = result.getInt("entry_id")
                                val id = userid.toString()
                                val latitude = lat.toDouble()
                                val longitude = lon.toDouble()
                                showLocation(latitude, longitude, id)
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }

                        } else {
                            Toast.makeText(this@MainActivity, "No Accident Detected", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onError(error: ANError) {
                        Toast.makeText(this@MainActivity, " $error", Toast.LENGTH_SHORT).show()
                    }
                })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    private fun showLocation(latitude: Double, longitude: Double, id: String) {
        val accidentLocation = LatLng(latitude, longitude)

        // circle option & custom marker
        mCircle = mMap!!.addCircle(CircleOptions()
                .center(accidentLocation)
                .radius(50.0)
                .strokeColor(resources.getColor(R.color.accident_zone))
                .fillColor(resources.getColor(R.color.accident_zone_trans)))
        mMap!!.addMarker(MarkerOptions().position(accidentLocation).title(id + " " + USER_NAME)
                .icon(bitmapDescriptorFromVector(this@MainActivity, R.drawable.ic_warning_black_24dp)))
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding

            return
        }
        mMap!!.isMyLocationEnabled = true
        mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(accidentLocation, 18f))
    }

    // custom marker for saviour
    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    companion object {
        const val USER_NAME = "Karthick"
        private const val URL = "https://api.thingspeak.com/channels/1009199/feeds/last.json?api_key=VRMW2SVE9KK9YD50"
        private const val TAG = "MainActivity"
        private const val JOB_TAG = "AccidentWorker"
    }
}