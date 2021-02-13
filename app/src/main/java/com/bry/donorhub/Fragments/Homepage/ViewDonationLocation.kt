package com.bry.donorhub.Fragments.Homepage

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bry.donorhub.Model.Donation
import com.bry.donorhub.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson


class ViewDonationLocation : Fragment() {
    var DONATION = "DONATION"
    private lateinit var donation: Donation
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            donation = Gson().fromJson(it.getString(DONATION), Donation::class.java)
        }
    }

    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        var pos = donation.location
        move_cam_to_location(pos, 17f)

        googleMap.addMarker(MarkerOptions()
                .position(pos)
                .title("${donation.description} Donation."))

//        Toast.makeText(context, "Loaded batch ${donation.batch_id}", Toast.LENGTH_SHORT).show()

        if(!donation.batch_id.equals("")) {
            Firebase.firestore.collection("batches")
                    .document(donation.batch_id)
                    .get().addOnSuccessListener {
                        if(it.exists()){
                            var latLng = Gson().fromJson(it["location"] as String, LatLng::class.java)
                            var batch_name = it["name"] as String

                            mMap.addMarker(MarkerOptions()
                                    .position(latLng)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                    .title("${batch_name}"))

//                            Toast.makeText(context, "Loaded batch location", Toast.LENGTH_SHORT).show()

                            val start = donation.location
                            val end = latLng
                            val alLatLng: ArrayList<LatLng> = ArrayList()

                            var cLat: Double = (start.latitude + end.latitude) / 2
                            var cLon: Double = (start.longitude + end.longitude) / 2

                            //add skew and arcHeight to move the midPoint

                            //add skew and arcHeight to move the midPoint
                            if (Math.abs(start.longitude - end.longitude) < 0.0001) {
                                cLon -= 0.0195
                            } else {
                                cLat += 0.0195
                            }

                            val tDelta = 1.0 / 50

                            var t = 0.0
                            while (t <= 1.0) {
                                val oneMinusT = 1.0 - t
                                val t2 = Math.pow(t, 2.0)
                                val lon: Double = oneMinusT * oneMinusT * start.longitude + 2 * oneMinusT * t * cLon + t2 * end.longitude
                                val lat: Double = oneMinusT * oneMinusT * start.latitude + 2 * oneMinusT * t * cLat + t2 * end.latitude
                                alLatLng.add(LatLng(lat, lon))
                                t += tDelta
                            }


                            // draw polyline

                            // draw polyline
                            val line = PolylineOptions()
                            line.width(5F)
                            line.color(Color.BLACK)
                            line.addAll(alLatLng)
                            mMap.addPolyline(line)

                            val builder = LatLngBounds.Builder()
                            builder.include(start)
                            builder.include(end)
                            val bounds = builder.build()

                            val padding = 120 // offs from edges of the map in pixels
                            val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                            googleMap.moveCamera(cu);
                        }
                    }
        }

    }

    fun move_cam_to_location(latLng: LatLng, zoom: Float){
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val va = inflater.inflate(R.layout.fragment_view_donation_location, container, false)


        return va
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }


    companion object {

        @JvmStatic
        fun newInstance(donation: String) = ViewDonationLocation().apply {
                    arguments = Bundle().apply {
                        putString(DONATION, donation)
                    }
                }
    }
}