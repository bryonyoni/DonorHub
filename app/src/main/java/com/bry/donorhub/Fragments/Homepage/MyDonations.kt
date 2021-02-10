package com.bry.donorhub.Fragments.Homepage

import android.content.Context
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
import com.bry.donorhub.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import java.util.*
import kotlin.collections.ArrayList


class MyDonations : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"
    private val ARG_DONATIONS = "ARG_DONATIONS"
    private lateinit var listener: MyDonationsInterface
    private lateinit var donations: ArrayList<Donation>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            donations = Gson().fromJson(it.getString(ARG_DONATIONS), Donation.donation_list::class.java).donation_list
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is MyDonationsInterface){
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val va = inflater.inflate(R.layout.fragment_my_donations, container, false)
        val my_donations_recyclerview: RecyclerView = va.findViewById(R.id.my_donations_recyclerview)


        my_donations_recyclerview.adapter = myDonationsListAdapter()
        my_donations_recyclerview.layoutManager = LinearLayoutManager(context)

        return va
    }

    internal inner class myDonationsListAdapter : RecyclerView.Adapter<ViewHolderDonations>() {

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolderDonations {
            val vh = ViewHolderDonations(LayoutInflater.from(context)
                .inflate(R.layout.recycler_item_donation, viewGroup, false))
            return vh
        }

        override fun onBindViewHolder(viewHolder: ViewHolderDonations, position: Int) {
            var donation = donations[position]

            viewHolder.donation_desc.text = donation.description
            val t_diff = Constants().construct_elapsed_time(Calendar.getInstance().timeInMillis - donation.creation_time)
            viewHolder.creation_time.text = "Request sent ${t_diff} ago."

            viewHolder.view_donation_relative.setOnClickListener {
                listener.whenMyDonationViewDonation(donation)
            }

            viewHolder.donation_images.adapter = ImageListAdapter(donation)
            viewHolder.donation_images.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)

        }

        override fun getItemCount() = donations.size

    }

    internal inner class ViewHolderDonations (view: View) : RecyclerView.ViewHolder(view) {
        val donation_desc: TextView = view.findViewById(R.id.donation_desc)
        val view_donation_relative: RelativeLayout = view.findViewById(R.id.view_donation_relative)
        val creation_time: TextView = view.findViewById(R.id.creation_time)
        val donation_images: RecyclerView = view.findViewById(R.id.donation_images)
    }



    internal inner class ImageListAdapter(var donation: Donation) : RecyclerView.Adapter<ViewHolderImages>() {

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolderImages {
            val vh = ViewHolderImages(LayoutInflater.from(context)
                .inflate(R.layout.recycler_item_donation_image, viewGroup, false))
            return vh
        }

        override fun onBindViewHolder(viewHolder: ViewHolderImages, position: Int) {
            val image = donation.images[position]
            viewHolder.image_view.setDrawingCacheEnabled(false)
            val storageReference: StorageReference = Firebase.storage.reference
                .child(Constants().donation_data)
                .child(donation.donation_id)
                .child(image.name + ".jpg")
            Constants().load_round_job_image(storageReference, viewHolder.image_view, context!!)

        }

        override fun getItemCount() = donation.images.size


    }

    internal inner class ViewHolderImages (view: View) : RecyclerView.ViewHolder(view) {
        val image_view: ImageView = view.findViewById(R.id.image)
    }



    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String, donations: String) =
            MyDonations().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                    putString(ARG_DONATIONS, donations)
                }
            }
    }


    interface MyDonationsInterface{
        fun whenMyDonationViewDonation(donation: Donation)
    }

}