package com.bry.donorhub.Fragments.Homepage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bry.donorhub.Constants
import com.bry.donorhub.Model.Donation
import com.bry.donorhub.Model.Organisation
import com.bry.donorhub.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import java.util.ArrayList


class ViewOrganisation : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"
    private val ARG_ORGANISATION = "ARG_ORGANISATION"
    private val ARG_DONATION = "ARG_DONATION"
    private lateinit var organisation: Organisation
    private lateinit var donation: String
    private lateinit var listener: ViewOrganisationInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            organisation = Gson().fromJson(it.getString(ARG_ORGANISATION), Organisation::class.java)
            donation = it.getString(ARG_DONATION) as String
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is ViewOrganisationInterface){
            listener = context
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val va = inflater.inflate(R.layout.fragment_view_organisation, container, false)
        val organisation_name: TextView = va.findViewById(R.id.organisation_name)
        val location: TextView = va.findViewById(R.id.location)
        val open_new_donation_relative: RelativeLayout = va.findViewById(R.id.open_new_donation_relative)
        val activities_recyclerview: RecyclerView = va.findViewById(R.id.activities_recyclerview)
        val org_image: ImageView = va.findViewById(R.id.org_image)

        val twitter: TextView = va.findViewById(R.id.twitter)
        val facebook: TextView = va.findViewById(R.id.facebook)
        val instagram: TextView = va.findViewById(R.id.instagram)

        organisation_name.text = organisation.name
        location.text = organisation.location_name

        open_new_donation_relative.setOnClickListener {
            listener.whenViewOrganisationCreateNewDonation(organisation)
        }

        if(!donation.equals("")){
            val don_obj = Gson().fromJson(donation, Donation::class.java)
            if(don_obj.activies.isNotEmpty()){
                activities_recyclerview.adapter = ActivitiesListAdapter(don_obj)
                activities_recyclerview.layoutManager = LinearLayoutManager(context)
            }
        }

        val d = organisation.org_id
        val storageReference = Firebase.storage.reference
                .child("organisation_backgrounds")
                .child("${d}.jpg")

        Constants().load_normal_job_image(storageReference, org_image, context!!)

        twitter.setOnClickListener {
            var url = "http://www.twitter.com"
            if(!organisation.twitter.equals("")){
                url = organisation.twitter
            }
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }

        facebook.setOnClickListener {
            var url = "http://www.facebook.com"
            if(!organisation.facebook.equals("")){
                url = organisation.facebook
            }
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }

        instagram.setOnClickListener {
            var url = "http://www.instagram.com"
            if(!organisation.instagram.equals("")){
                url = organisation.instagram
            }
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }

        return va
    }


    internal inner class ActivitiesListAdapter(var donation: Donation) : RecyclerView.Adapter<ViewHolderActivities>() {

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolderActivities {
            val vh = ViewHolderActivities(LayoutInflater.from(context).inflate(R.layout.recycler_item_activities, viewGroup, false))
            return vh
        }

        override fun onBindViewHolder(v: ViewHolderActivities, position: Int) {
            val activity = donation.activies[position]

            v.activity_explanation.text = activity.explanation
            v.activity_time.text = Constants().construct_elapsed_time(activity.time)
        }

        override fun getItemCount() = donation.activies.size

    }

    internal inner class ViewHolderActivities (view: View) : RecyclerView.ViewHolder(view) {
        val activity_explanation: TextView = view.findViewById(R.id.activity_explanation)
        val activity_time: TextView = view.findViewById(R.id.activity_time)
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String, organisation: String, donation: String) =
                ViewOrganisation().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                        putString(ARG_ORGANISATION,organisation)
                        putString(ARG_DONATION,donation)
                    }
                }
    }


    interface ViewOrganisationInterface{
        fun whenViewOrganisationCreateNewDonation(organisation: Organisation)
    }

}