package com.bry.donorhub.Fragments.Homepage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bry.donorhub.Constants
import com.bry.donorhub.Model.Donation
import com.bry.donorhub.Model.Organisation
import com.bry.donorhub.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import java.io.ByteArrayOutputStream


class EditDonation : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"
    private val ARG_ORGANISATION = "ARG_ORGANISATION"
    private val ARG_DONATION = "ARG_DONATION"
    private lateinit var organisation: Organisation
    private lateinit var donation: Donation
    private lateinit var listener: EditDonationInterface

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
        if(context is EditDonationInterface){
            listener = context
        }
    }

    var onImagePicked: (pic_name: Bitmap) -> Unit = {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val va = inflater.inflate(R.layout.fragment_edit_donation, container, false)
        val finish_layout: RelativeLayout = va.findViewById(R.id.finish_layout)
        val descriptionEditText: EditText = va.findViewById(R.id.descriptionEditText)
        val add_image: ImageView = va.findViewById(R.id.add_image)
        val added_images_recyclerview: RecyclerView = va.findViewById(R.id.added_images_recyclerview)
        val takenDownSwitch: Switch = va.findViewById(R.id.takenDownSwitch)
        val mass_editText: EditText = va.findViewById(R.id.mass_editText)
        val quantity_editText: EditText = va.findViewById(R.id.quantity_editText)

        descriptionEditText.setText(donation.description)
        mass_editText.setText(donation.mass)
        quantity_editText.setText(donation.quantity)

        onImagePicked = {
            val baos = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.JPEG, 100, baos)

            val id = Firebase.firestore.collection("gucci").document().id
            write_image(baos.toByteArray(), id, donation.donation_id, added_images_recyclerview)
            val im = Donation.donation_image(id)
            donation.images.add(im)

            added_images_recyclerview.adapter?.notifyDataSetChanged()
            Toast.makeText(context, "image added",Toast.LENGTH_SHORT).show()
        }

        added_images_recyclerview.adapter = ImageListAdapter()
        added_images_recyclerview.layoutManager = LinearLayoutManager(context,
                LinearLayoutManager.HORIZONTAL, false)

        add_image.setOnClickListener{
            listener.whenEditDonationPickImage()
        }

        finish_layout.setOnClickListener {
            val t = descriptionEditText.text.toString().trim()
            val mass = mass_editText.text.toString().trim()
            val quantity = quantity_editText.text.toString().trim()

            if(t.equals("")){
                descriptionEditText.error = "Type something"
            }else if(donation.images.isEmpty()){
                Toast.makeText(context, "add some images first!",Toast.LENGTH_SHORT).show()
            }else if(mass.equals("")){
                mass_editText.setError("Type something")
            }else if(quantity.equals("")){
                quantity_editText.setError("Type something")
            }

            else{
                Toast.makeText(context, "upload donation", Toast.LENGTH_SHORT).show()
                donation.description = t
                donation.mass = mass
                donation.quantity = quantity
                listener.whenEditDonationFinished(donation)
            }
        }


        takenDownSwitch.isChecked = donation.is_taken_down

        takenDownSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            donation.is_taken_down = isChecked
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
            val image = donation.images[position]

            val storageReference: StorageReference = Firebase.storage.reference
                    .child(Constants().donation_data)
                    .child(donation.donation_id)
                    .child(image.name + ".jpg")
            Log.e("ViewDonation", "storage reference is: ${storageReference.path}")
            Constants().load_round_job_image(storageReference, viewHolder.image_view, context!!)

            viewHolder.image_view.setOnClickListener {
                donation.images.removeAt(position)
                this@ImageListAdapter.notifyDataSetChanged()
                Toast.makeText(context, "image removed",Toast.LENGTH_SHORT).show()
            }

        }

        override fun getItemCount() = donation.images.size

    }

    internal inner class ViewHolderImages (view: View) : RecyclerView.ViewHolder(view) {
        val image_view: ImageView = view.findViewById(R.id.image)
    }

    private fun write_image(image: ByteArray, name: String, donation_id: String, added_images_recyclerview: RecyclerView){
        val avatarRef = Firebase.storage.reference
                .child(Constants().donation_data)
                .child(donation_id)
                .child(name + ".jpg")

        val im = BitmapFactory.decodeByteArray(image, 0, image.size)
        val baos = ByteArrayOutputStream()
        im.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        avatarRef.putBytes(data).addOnFailureListener{
//            Log.e(TAG,it.message.toString())
        }.addOnSuccessListener {
            Log.e("EditDonation","Written image in the cloud!")
            added_images_recyclerview.adapter?.notifyDataSetChanged()
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String, donation: String, organisation: String) =
                EditDonation().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                        putString(ARG_ORGANISATION,organisation)
                        putString(ARG_DONATION,donation)
                    }
                }
    }


    interface EditDonationInterface{
        fun whenEditDonationPickImage()
        fun whenEditDonationFinished(donation: Donation)
    }
}