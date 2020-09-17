package www.sanju.saviour

import android.content.Context
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import org.json.JSONException
import org.json.JSONObject

class Repo(applicationContext: Context) {


    fun fetchLatestAccident(): Accidents {

        var accidents = Accidents()

        AndroidNetworking.get(URL)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(object : StringRequestListener {
                    override fun onResponse(response: String) {
                        if (response.isNotEmpty()) {
                            try {
                                val result = JSONObject(response)
                                val lat = result.getString("field1")
                                val lon = result.getString("field2")
                                val userId = result.getInt("entry_id")

                                // check for null content
                                if (!lat.isNullOrEmpty() || !lon.isNullOrEmpty()) {
                                    accidents = Accidents(
                                            userId.toString(),
                                            lat.toDouble(),
                                            lon.toDouble())
                                }

                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }


                        } else {
                            print("No results found")

                        }
                    }

                    override fun onError(error: ANError) {
                        print("Error found $error")

                    }
                })

        return accidents
    }


    companion object {
        private const val URL = "https://api.thingspeak.com/channels/1009199/feeds/last.json?api_key=VRMW2SVE9KK9YD50"
    }
}