package com.bry.donorhub.Fragments.Homepage

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bry.donorhub.Constants
import com.bry.donorhub.Model.Donation
import com.bry.donorhub.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage


class NewDonation : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"
    private lateinit var listener: NewDonationInterface
    private var picked_images: ArrayList<Bitmap> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is NewDonationInterface){
            listener = context
        }
    }

    var onImagePicked: (pic_name: Bitmap) -> Unit = {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val va = inflater.inflate(R.layout.fragment_new_donation, container, false)
        val finish_layout: RelativeLayout = va.findViewById(R.id.finish_layout)
        val descriptionEditText: EditText = va.findViewById(R.id.descriptionEditText)
        val add_image: ImageView = va.findViewById(R.id.add_image)
        val added_images_recyclerview: RecyclerView = va.findViewById(R.id.added_images_recyclerview)

        onImagePicked = {
            picked_images.add(it)
            added_images_recyclerview.adapter?.notifyDataSetChanged()
            Toast.makeText(context, "image added",Toast.LENGTH_SHORT).show()
        }

        added_images_recyclerview.adapter = ImageListAdapter()
        added_images_recyclerview.layoutManager = LinearLayoutManager(context)

        add_image.setOnClickListener{
            listener.whenNewDonationPickImage()
        }

        finish_layout.setOnClickListener {
            val t = descriptionEditText.text.toString().trim()
            if(t.equals("")){
                descriptionEditText.error = "Type something"
            }else if(picked_images.isEmpty()){
                Toast.makeText(context, "add some images first!",Toast.LENGTH_SHORT).show()
            }else{
                listener.whenNewDonationFinished(t,picked_images)
            }
        }

        return va
    }

    internal inner class ImageListAdapter : RecyclerView.Adapter<ViewHolderImages>() {

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolderImages {
            val vh = ViewHolderImages(LayoutInflater.from(context)
                .inflate(R.layout.recycler_item_donation_image, viewGroup, false))
            return vh
        }

        override fun onBindViewHolder(viewHolder: ViewHolderImages, position: Int) {
            val image = picked_images[position]
            viewHolder.image_view.setImageBitmap(Constants().getCroppedBitmap(image))

            viewHolder.image_view.setOnClickListener {
                picked_images.removeAt(position)
                this@ImageListAdapter.notifyDataSetChanged()
                Toast.makeText(context, "image removed",Toast.LENGTH_SHORT).show()
            }
        }

        override fun getItemCount() = picked_images.size

    }

    internal inner class ViewHolderImages (view: View) : RecyclerView.ViewHolder(view) {
        val image_view: ImageView = view.findViewById(R.id.image)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                NewDonation().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }

    interface NewDonationInterface{
        fun whenNewDonationPickImage()
        fun whenNewDonationFinished(text: String, images: ArrayList<Bitmap>)
    }
}