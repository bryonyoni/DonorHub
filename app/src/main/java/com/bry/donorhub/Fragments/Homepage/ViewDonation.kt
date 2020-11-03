package com.bry.donorhub.Fragments.Homepage

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.OrientationEventListener
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import java.util.*

class ViewDonation : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"
    private val ARG_ORGANISATION = "ARG_ORGANISATION"
    private val ARG_DONATION = "ARG_DONATION"
    private lateinit var organisation: Organisation
    private lateinit var donation: Donation
    private lateinit var listener: ViewDonationInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            organisation = Gson().fromJson(it.getString(ARG_ORGANISATION), Organisation::class.java)
            donation = Gson().fromJson(it.getString(ARG_DONATION) as String, Donation::class.java)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is ViewDonationInterface){
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val va = inflater.inflate(R.layout.fragment_view_donation, container, false)
        val donation_desc: TextView = va.findViewById(R.id.donation_desc)
        val donation_time: TextView = va.findViewById(R.id.donation_time)
        val donation_images_recyclerview: RecyclerView = va.findViewById(R.id.donation_images_recyclerview)
        val organisation_name: TextView = va.findViewById(R.id.organisation_name)
        val organisation_location: TextView = va.findViewById(R.id.organisation_location)
        val other_organisations: RecyclerView = va.findViewById(R.id.other_organisations)


        donation_desc.text = donation.description
        donation_time.text = Constants().construct_elapsed_time(Calendar.getInstance().timeInMillis - donation.creation_time)

        organisation_name.text = organisation.name
        organisation_location.text = organisation.location_name

        donation_images_recyclerview.adapter = ImageListAdapter(donation)
        donation_images_recyclerview.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)


        return va
    }

    internal inner class ImageListAdapter(var donation: Donation) : RecyclerView.Adapter<ViewHolderImages>() {

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolderImages {
            val vh = ViewHolderImages(LayoutInflater.from(context)
                .inflate(R.layout.recycler_item_donation_image, viewGroup, false))
            return vh
        }

        override fun onBindViewHolder(viewHolder: ViewHolderImages, position: Int) {
            val image = donation.images[position]

            val storageReference: StorageReference = Firebase.storage.reference
                .child(Constants().donation_data)
                .child(donation.donation_id)
                .child(image.name + ".jpg")
            Log.e("ViewDonation", "storage reference is: ${storageReference.path}")
            Constants().load_round_job_image(storageReference, viewHolder.image_view, context!!)

        }

        override fun getItemCount() = donation.images.size


    }

    internal inner class ViewHolderImages (view: View) : RecyclerView.ViewHolder(view) {
        val image_view: ImageView = view.findViewById(R.id.image)
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String, organisation: String, donation: String) =
            ViewDonation().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                    putString(ARG_ORGANISATION,organisation)
                    putString(ARG_DONATION,donation)
                }
            }
    }

    interface ViewDonationInterface{

    }
}