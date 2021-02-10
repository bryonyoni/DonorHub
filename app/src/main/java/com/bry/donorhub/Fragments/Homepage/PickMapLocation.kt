package com.bry.donorhub.Fragments.Homepage

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.bry.donorhub.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth

class PickMapLocation : Fragment() {
    val ZOOM = 17f
    private lateinit var mMap: GoogleMap
    var mLastKnownLocations: ArrayList<LatLng> = ArrayList()
    var has_map_been_loaded = false
    var my_marker: Marker? = null
    private lateinit var listener: PickMapLocationInterface
    var whenMyLocationGotten: (loc: LatLng) -> Unit = {}


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is PickMapLocationInterface){
            listener = context
        }
    }



    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        has_map_been_loaded = true
        mMap.uiSettings.isZoomControlsEnabled = true

        whenMyLocationGotten = {
            mLastKnownLocations.add(it)
            when_location_gotten()
        }

        listener.whenMapFragmentLoaded()

        move_cam_to_location(LatLng(-1.286389,36.817223), ZOOM)
    }


    fun when_location_gotten(){
        val last_loc = mLastKnownLocations.get(mLastKnownLocations.lastIndex)
        val ll = LatLng(last_loc.latitude,last_loc.longitude)
        load_my_location_on_map()
        if(mLastKnownLocations.size==1) {
            Log.e("when_location_gotten","adjusting to my location")
            move_cam_to_my_location(ZOOM)
        }

    }

    fun move_cam_to_my_location(zoom: Float){
        if(mLastKnownLocations.isNotEmpty()) {
            val last_loc = mLastKnownLocations.get(mLastKnownLocations.lastIndex)
            val ll = LatLng(last_loc.latitude, last_loc.longitude)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(ll.latitude, ll.longitude), zoom))
        }
    }

    fun move_cam_to_location(latLng: LatLng, zoom: Float){
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    private fun load_my_location_on_map() {
        val last_loc = mLastKnownLocations.get(mLastKnownLocations.lastIndex)

        var lat_lng = LatLng(last_loc.latitude,last_loc.longitude)
        var op = MarkerOptions().position(lat_lng)
        var final_icon: BitmapDrawable?  = context!!.resources.getDrawable(R.drawable.my_location_icon) as BitmapDrawable

        val height = 30
        val width = 30
        if(final_icon!=null) {
            val b: Bitmap = final_icon.bitmap
            val smallMarker: Bitmap = Bitmap.createScaledBitmap(b, width, height, false)
            op.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
        }

        if(my_marker!=null){
            my_marker!!.position = LatLng(last_loc.latitude, last_loc.longitude)
        }else{
            my_marker = mMap.addMarker(op)
        }


        val uid = FirebaseAuth.getInstance().currentUser?.uid
        try {
            addPulsatingEffect(last_loc, uid!!)
        }catch (e: Exception){
            e.printStackTrace()
        }


    }


    private var lastUserCircleList: HashMap<String, Circle> = HashMap()
    private val pulseDuration: Long = 2000
    private var lastPulseAnimatorList: HashMap<String, ValueAnimator> = HashMap()
    var rad = 200f
    private fun addPulsatingEffect(userLatlng: LatLng, user: String) {
        if (lastPulseAnimatorList.containsKey(user)) {
            lastPulseAnimatorList.get(user)!!.cancel()
        }
        if (lastUserCircleList.containsKey(user)) lastUserCircleList.get(user)!!.center = userLatlng
        var lastPulse = valueAnimate(ValueAnimator.AnimatorUpdateListener { animation ->
            if (lastUserCircleList.containsKey(user)) {
                lastUserCircleList.get(user)!!.setRadius((animation.getAnimatedValue() as Float).toDouble())
                var col = Color.parseColor("#2271cce7")
                lastUserCircleList.get(user)!!.fillColor = adjustAlpha(col, (rad - (animation.getAnimatedValue() as Float)) / rad)
            } else {
                var col = Color.parseColor("#2271cce7")
                var lastCircle = mMap.addCircle(
                    CircleOptions()
                    .center(userLatlng)
                    .radius((animation.getAnimatedValue() as Float).toDouble())
                    .fillColor(col)
                    .strokeWidth(0f)
                )
                if(lastUserCircleList.containsKey(user)){
                    lastUserCircleList.remove(user)
                }
                lastUserCircleList.put(user,lastCircle)
            }
        })
        if(lastPulseAnimatorList.containsKey(user)){
            lastPulseAnimatorList.remove(user)
        }
        lastPulseAnimatorList.put(user,lastPulse!!)
    }

    fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
//        Log.e("adjustAlpha","adjusted alpha is ${alpha}")
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    protected fun valueAnimate( updateListener: ValueAnimator.AnimatorUpdateListener?): ValueAnimator? {
//        Log.d("valueAnimate: ", "called")
        val va = ValueAnimator.ofFloat(0f, rad)
        va.duration = pulseDuration
        va.addUpdateListener(updateListener)
        va.interpolator = LinearOutSlowInInterpolator()
        va.start()
        return va
    }




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val va = inflater.inflate(R.layout.fragment_pick_map_location, container, false)
        val next_btn: RelativeLayout = va.findViewById(R.id.next_btn)


        next_btn.setOnClickListener {
            val setPos = mMap.getCameraPosition().target
            listener.whenMapLocationPicked(setPos)
        }

        return va
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onDestroy() {
        super.onDestroy()
        listener.whenMapFragmentClosed()
    }

    interface PickMapLocationInterface{
        fun whenMapFragmentLoaded()
        fun whenMapFragmentClosed()
        fun whenMapLocationPicked(latLng: LatLng)
    }
}