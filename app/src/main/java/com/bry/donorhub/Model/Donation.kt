package com.bry.donorhub.Model

class Donation(var description: String, var creation_time: Long, var donation_id: String) {
    var images: ArrayList<donation_image> = ArrayList()
    var activies: ArrayList<activity> = ArrayList()

    class donation_list(var donation_list: ArrayList<Donation>)

    class donation_image (var name: String){
    }

    class activity(var explanation: String, var time: Long)
}