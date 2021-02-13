package com.bry.donorhub.Model

import com.google.android.gms.maps.model.LatLng

class Donation(var description: String, var creation_time: Long, var donation_id: String) {
    var is_taken_down: Boolean = false
    var uploader_id: String = ""
    var organisation_id: String = ""
    var location: LatLng = LatLng(0.0,0.0)
    var collectors: Collectors? = null
    var pick_up_time: Long = 0
    var mass = ""
    var quantity = ""
    var batch_id = ""
    var images: ArrayList<donation_image> = ArrayList()
    var activies: ArrayList<activity> = ArrayList()

    class donation_list(var donation_list: ArrayList<Donation>)

    class donation_image (var name: String){
    }

    class activity(var explanation: String, var time: Long, var donation_id: String, var activity_id: String)

    class activities(var activities: ArrayList<activity>)
}