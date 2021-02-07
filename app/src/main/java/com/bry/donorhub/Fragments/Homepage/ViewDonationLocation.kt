package com.bry.donorhub.Fragments.Homepage

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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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
        var pos = donation.location
        move_cam_to_location(pos, 17f)

        googleMap.addMarker(MarkerOptions()
                .position(pos)
                .title("donation.description"))

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