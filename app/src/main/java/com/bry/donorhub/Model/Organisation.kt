package com.bry.donorhub.Model

class Organisation(var name: String, var creation_date: Long, var location_name: String, var org_id: String) {
    var twitter = ""
    var instagram = ""
    var facebook = ""
    var pub_key = ""

    class organisation_list(var organisation_list: ArrayList<Organisation>)
}