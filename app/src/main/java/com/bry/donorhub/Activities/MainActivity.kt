package com.bry.donorhub.Activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bry.donorhub.Constants
import com.bry.donorhub.Fragments.Authentication.SignIn
import com.bry.donorhub.Fragments.Authentication.SignUp
import com.bry.donorhub.Fragments.Authentication.SignUpEmail
import com.bry.donorhub.Fragments.Authentication.SignUpPhone
import com.bry.donorhub.Fragments.Homepage.*
import com.bry.donorhub.GpsUtils
import com.bry.donorhub.Model.Collectors
import com.bry.donorhub.Model.Donation
import com.bry.donorhub.Model.Number
import com.bry.donorhub.Model.Organisation
import com.bry.donorhub.R
import com.bry.donorhub.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.theartofdev.edmodo.cropper.CropImage
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(),
        SignUp.SignUpNameInterface,
        SignUpEmail.SignUpEmailInterface,
        SignUpPhone.SignUpPhoneInterface,
        SignIn.SignInInterface,
        PickOrganisation.PickOrganisationInterface,
        ViewOrganisation.ViewOrganisationInterface,
        NewDonation.NewDonationInterface,
        MyDonations.MyDonationsInterface,
        PickMapLocation.PickMapLocationInterface,
        ViewDonation.ViewDonationInterface
{
    val TAG = "MainActivity"
    val constants = Constants()

    val _sign_up = "_sign_up"
    val _pick_org = "_pick_org"
    val _sign_in = "_sign_in"
    val _sign_up_email = "_sign_up_email"
    val _sign_up_phone = "_sign_up_phone"
    val _my_donations = "_my_donations"
    val _view_organisation = "_view_organisation"
    val _new_donation = "_new_donation"
    val _view_donation = "_view_donation"
    val _map_fragment = "_map_fragment"
    val _view_donation_location = "_view_donation_location"

    private lateinit var binding: ActivityMainBinding
    val db = Firebase.firestore

    private var donations: ArrayList<Donation> = ArrayList()
    private var organisations: ArrayList<Organisation> = ArrayList()
    private var activities: ArrayList<Donation.activity> = ArrayList()
    var doubleBackToExitPressedOnce: Boolean = false
    var is_loading: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val actionBar: ActionBar = supportActionBar!!
        actionBar.hide()

        if(constants.SharedPreferenceManager(applicationContext).getPersonalInfo() == null){
            supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .replace(binding.money.id, SignUp.newInstance("", ""), _sign_up).commit()
        }else{
            loadDonationsAndOrganisations()
            openPickOrganisation()
        }

    }

    override fun onBackPressed() {
        if (supportFragmentManager.fragments.size > 1) {
            val trans = supportFragmentManager.beginTransaction()
            trans.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
            val currentFragPos = supportFragmentManager.fragments.size - 1

            trans.remove(supportFragmentManager.fragments.get(currentFragPos))
            trans.commit()
            supportFragmentManager.popBackStack()

        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }

            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show()

            Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
        }
    }

    fun hideLoadingScreen(){
        is_loading = false
        binding.loadingScreen.visibility = View.GONE
    }

    fun showLoadingScreen(){
        is_loading = true
        binding.loadingScreen.visibility = View.VISIBLE
        binding.loadingScreen.setOnTouchListener { v, _ -> true }

    }



    fun loadDonationsAndOrganisations(){
        showLoadingScreen()
        organisations.clear()
        donations.clear()
        activities.clear()

        db.collection("organisations").get().addOnSuccessListener {
            if(!it.isEmpty){
                for(doc in it.documents){
                    if(doc.contains("org_obj")){
                        val org = Gson().fromJson(doc["org_obj"] as String, Organisation::class.java)
                        organisations.add(org)
                    }
                }
            }
        }

        db.collection("donations").get().addOnSuccessListener {
            if(!it.isEmpty){
                for(doc in it.documents){
                    if(doc.contains("don_obj")){
                        val don = Gson().fromJson(doc["don_obj"] as String, Donation::class.java)
                        if(doc.contains("taken_down")){
                            don.is_taken_down = doc["taken_down"] as Boolean
                        }
                        if(doc.contains("collectors")){
                            don.collectors = Gson().fromJson(doc["collectors"] as String, Collectors::class.java)
                        }
                        if(doc.contains("pick_up_time")){
                            don.pick_up_time = doc["pick_up_time"] as Long
                        }
                        donations.add(don)
                    }
                }
            }
            hideLoadingScreen()
            Toast.makeText(applicationContext, "Done loading!", Toast.LENGTH_SHORT).show()
            whenDoneLoadingData()
        }

        db.collection("activities").get().addOnSuccessListener {
            if(!it.isEmpty){
                for(doc in it.documents){
                    var activity_id = doc["activity_id"] as String
                    var explanation = doc["explanation"] as String
                    var timestamp = doc["timestamp"] as Long
                    var donation_id = doc["donation"] as String

                    activities.add(Donation.activity(explanation, timestamp, donation_id, activity_id))
                }
            }
        }


    }

    fun whenDoneLoadingData(){

        if(supportFragmentManager.findFragmentByTag(_pick_org)!=null){
            (supportFragmentManager.findFragmentByTag(_pick_org) as PickOrganisation).when_data_updated(organisations)
        }

        if(supportFragmentManager.findFragmentByTag(_view_organisation)!=null){
            (supportFragmentManager.findFragmentByTag(_view_organisation) as ViewOrganisation)
        }

    }




    fun openPickOrganisation(){
        val orgs = Gson().toJson(Organisation.organisation_list(organisations))
        supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(binding.money.id, PickOrganisation.newInstance("", "", orgs), _pick_org).commit()
    }





    override fun OnSignUpNameLogInInsteadSelected() {
        supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(binding.money.id, SignIn.newInstance("", ""), _sign_in).commit()
    }

    var name: String = ""
    var email: String = ""
    var passcode: String = ""
    lateinit var number: Number
    override fun OnSignUpNameContinueSelected(name: String) {
        this.name = name

        supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(binding.money.id, SignUpEmail.newInstance("", ""), _sign_up_email).commit()
    }

    override fun OnSignUpEmailContinueSelected(email: String, password: String) {
        this.email = email
        this.passcode = password

        supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(binding.money.id, SignUpPhone.newInstance("", ""), _sign_up_phone).commit()
    }

    var mAuth: FirebaseAuth? = null
    override fun OnSignUpPhoneContinueSelected(phoneNo: Number) {
        this.number = phoneNo

        mAuth = FirebaseAuth.getInstance()
        if(isOnline()) {
            showLoadingScreen()
            val view = this.currentFocus
            if (view != null) {
                val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }

            mAuth!!.createUserWithEmailAndPassword(email,passcode)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("main", "authentication successful")
                            createFirebaseUserProfile(task.result!!.user, email, name, number)
                        } else {
                            Snackbar.make(binding.root, resources.getString(R.string.that_didnt_work), Snackbar.LENGTH_LONG).show()
                            hideLoadingScreen()
                        }
                    }
        }else{
            Snackbar.make(binding.root, getString(R.string.please_check_on_your_internet_connection),
                    Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun createFirebaseUserProfile(user: FirebaseUser?, email: String, name: String, numbr: Number) {
        val addProfileName = UserProfileChangeRequest.Builder().setDisplayName(name).build()
        val time = Calendar.getInstance().timeInMillis
        if (user != null) {
            user.updateProfile(addProfileName).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("main", "Created new username,")
                }
            }.addOnFailureListener {  }

            val myDataDoc = hashMapOf(
                    "email" to email,
                    "name" to name,
                    "uid" to user.uid,
                    "sign_up_time" to time,
                    "user_country" to numbr.country_name,
                    "phone_number" to Gson().toJson(numbr)
            )

            val uid = user.uid
            db.collection(constants.coll_users).document(uid)
                    .set(myDataDoc)
                    .addOnSuccessListener {
                        db.collection(constants.coll_users).document(uid)
                                .set(myDataDoc)
                                .addOnSuccessListener {
                                    Log.e(TAG, "created a new user!")

                                    constants.SharedPreferenceManager(applicationContext)
                                            .setPersonalInfo(numbr, email, name, time, user.uid)

                                    hideLoadingScreen()
                                    openPickOrganisation()
                                    loadDonationsAndOrganisations()
                                }
                    }.addOnFailureListener {  }

        }
    }

    fun isOnline(): Boolean {
        val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        //should check null because in airplane mode it will be null
        return netInfo != null && netInfo.isConnected
    }

    override fun OnSignInSignUpInsteadSelected() {
        supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(binding.money.id, SignUp.newInstance("", ""), _sign_up).commit()
    }

    var mAuthListener: FirebaseAuth.AuthStateListener? = null
    override fun OnSubmitLogInDetails(email: String, password: String) {
        if(isOnline()){
            showLoadingScreen()
            mAuth = FirebaseAuth.getInstance()
            mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                Log.d("main", "user status changes")
                val user = firebaseAuth.currentUser
                if (user != null) {
                    val uid = user.uid
                    db.collection(constants.coll_users).document(uid).get().addOnSuccessListener {
                        if (it.exists()) {
                            val name = it.get("name") as String
                            val sign_up_time = it.get("sign_up_time") as Long
                            val numbr = Gson().fromJson(it.get("phone_number") as String, Number::class.java)

                            constants.SharedPreferenceManager(applicationContext)
                                    .setPersonalInfo(numbr, email, name, sign_up_time, user.uid)

                            openPickOrganisation()
                            hideLoadingScreen()
                            loadDonationsAndOrganisations()
                            mAuth!!.removeAuthStateListener { mAuthListener!!}
                        }
                    }
                }
            }
            mAuth!!.addAuthStateListener(mAuthListener!!)

            val view = this.currentFocus
            if (view != null) {
                val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            mAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                Log.d("main", "signInWithEmail:onComplete" + task.isSuccessful)
                if (!task.isSuccessful) {
                    Snackbar.make(binding.root, "That didn't work. Please check your credentials and retry.", Snackbar.LENGTH_LONG).show()
                    if(supportFragmentManager.findFragmentByTag(_sign_in)!=null){
                        (supportFragmentManager.findFragmentByTag(_sign_in) as SignIn).didPasscodeFail()
                    }
                    mAuth!!.removeAuthStateListener { mAuthListener!!}
                    hideLoadingScreen()
                }
            }.addOnFailureListener { }
        }else{
            Snackbar.make(binding.root, getString(R.string.please_check_on_your_internet_connection), Snackbar.LENGTH_SHORT).show()
        }
    }





    override fun whenPickOrganisationOrgPicked(organisation: Organisation) {
        val dons = ""
        val orgs = Gson().toJson(organisation)
        supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .add(binding.money.id, ViewOrganisation.newInstance("", "", orgs, dons), _view_organisation).commit()
    }

    override fun whenReloadEverything() {
        loadDonationsAndOrganisations()
    }

    override fun whenPickOrganisationOpenMyDonations() {
        val my_donations: ArrayList<Donation> = ArrayList()
        val uid = FirebaseAuth.getInstance().currentUser!!.uid

        for(item in donations){
            if(item.uploader_id.equals(uid)){
                my_donations.add(item)
            }
        }

        val dons = Gson().toJson(Donation.donation_list(my_donations))
        supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .add(binding.money.id, MyDonations.newInstance("", "", dons), _my_donations).commit()
    }





    override fun whenViewOrganisationCreateNewDonation(organisation: Organisation) {
        val org = Gson().toJson(organisation)
        supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .add(binding.money.id, NewDonation.newInstance("", "", org), _new_donation).commit()
    }

    private val PICK_IMAGE_REQUEST = 420
    override fun whenNewDonationPickImage() {
//        CropImage.activity()
//                .setGuidelines(CropImageView.Guidelines.ON)
//                .start(this)

        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri: Uri = result.uri
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, resultUri)

                if(supportFragmentManager.findFragmentByTag(_new_donation)!=null){
                    (supportFragmentManager.findFragmentByTag(_new_donation) as NewDonation).onImagePicked(bitmap)
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }

        //if were picking an image
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == Activity.RESULT_OK
                && data != null && data.data != null) {
            if (data.data != null) {
                val mFilepath = data.data!!
                try {
                    val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, mFilepath)
                    if(supportFragmentManager.findFragmentByTag(_new_donation)!=null){
                        (supportFragmentManager.findFragmentByTag(_new_donation) as NewDonation).onImagePicked(bitmap)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }





    override fun whenNewDonationFinished(text: String, images: ArrayList<ByteArray>,
                                         organisation: Organisation, location: LatLng) {
        showLoadingScreen()
        val time = Calendar.getInstance().timeInMillis
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val ref = db.collection("donations")
                .document()

        val don = Donation(text, time, ref.id)
        don.is_taken_down = false
        don.uploader_id = uid
        don.organisation_id = organisation.org_id
        don.location = location

        for(image in images){
            val id = db.collection("gucci").document().id
            write_image(image, id, ref.id)
            val im = Donation.donation_image(id)
            don.images.add(im)
        }


        val data = hashMapOf(
                "don_obj" to Gson().toJson(don),
                "taken_down" to false,
                "time_of_creation" to time,
                "org_id" to ref.id,
                "uploader" to uid,
                "location" to Gson().toJson(location),
                "batch" to ""
        )

        donations.add(don)

        ref.set(data).addOnSuccessListener {
            hideLoadingScreen()
            Toast.makeText(applicationContext,"done!", Toast.LENGTH_SHORT).show()
            open_view_donation(don,organisation)
        }

    }

    override fun whenNewDonationPickLocation() {
        open_map_fragment()
    }

    private fun write_image(image: ByteArray, name: String, donation_id: String){
        val avatarRef = Firebase.storage.reference
                .child(Constants().donation_data)
                .child(donation_id)
                .child(name + ".jpg")

        val im = BitmapFactory.decodeByteArray(image, 0, image.size)
        val baos = ByteArrayOutputStream()
        im.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        avatarRef.putBytes(data).addOnFailureListener{
            Log.e(TAG,it.message.toString())
        }.addOnSuccessListener {
            Log.e(TAG,"Written image in the cloud!")
        }
    }




    fun open_view_donation(donation: Donation, organisation: Organisation){
        val don = Gson().toJson(donation)
        val org = Gson().toJson(organisation)

        var org_activities = ArrayList<Donation.activity>()

        for(item in activities){
            if(item.donation_id.equals(donation.donation_id)){
                org_activities.add(item)
            }
        }

        var act_string = Gson().toJson(Donation.activities(org_activities))

        supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .add(binding.money.id, ViewDonation.newInstance("", "", org, don, act_string), _view_donation).commit()
    }

    override fun whenMyDonationViewDonation(donation: Donation) {
        val donation_organisation = donation.organisation_id

        for(org in organisations){
            if(org.org_id.equals(donation_organisation)){
                open_view_donation(donation, org)
            }
        }
    }




    val requestCodeForViewingMyLoc = 13
    fun open_map_fragment(){
        supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            .add(binding.money.id, PickMapLocation(),_map_fragment).commit()

    }


    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val locationRequestCode = 1000

    fun load_my_location_for_map(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION), requestCodeForViewingMyLoc)
        } else {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            locationRequest = LocationRequest.create()
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            locationRequest.setInterval(7000)
            locationRequest.setFastestInterval(7000)


            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    if (locationResult == null) {
                        return
                    }
                    for (location in locationResult.locations) {
                        if (location != null) {
                            val wayLatitude = location.latitude
                            val wayLongitude = location.longitude
                            Log.e(TAG,"wayLatitude: ${wayLatitude} longitude: ${wayLongitude}")
                            if(is_map_fragment_open) {
                                if (supportFragmentManager.findFragmentByTag(_map_fragment) != null) {
                                    (supportFragmentManager.findFragmentByTag(_map_fragment) as PickMapLocation)
                                        .whenMyLocationGotten(
                                        LatLng(wayLatitude, wayLongitude)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            mFusedLocationClient.requestLocationUpdates(locationRequest,locationCallback,null)

            is_location_client_running = true

        }
    }

    var ACCESS_FINE_LOCATION_CODE = 3310
    override fun whenMapFragmentLoaded() {
        is_map_fragment_open = true
        GpsUtils(this).turnGPSOn(object : GpsUtils.onGpsListener {
            override fun gpsStatus(isGPSEnable: Boolean) {
                load_my_location_for_map()
            }
        })

    }

    var is_map_fragment_open = false
    var is_location_client_running = false
    override fun whenMapFragmentClosed() {
        is_map_fragment_open = false
        if (is_location_client_running) {
            mFusedLocationClient.removeLocationUpdates(locationCallback)
            is_location_client_running = false
        }
    }

    override fun whenMapLocationPicked(latLng: LatLng) {
        Toast.makeText(applicationContext, "Location set!", Toast.LENGTH_SHORT).show()
        if(supportFragmentManager.findFragmentByTag(_new_donation)!=null){
            (supportFragmentManager.findFragmentByTag(_new_donation) as NewDonation).onLocationPicked(latLng)
        }
        onBackPressed()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            requestCodeForViewingMyLoc -> {
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    load_my_location_for_map()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun viewDonationLocation(donation: Donation) {
        var don = Gson().toJson(donation)
        Log.e(TAG, "viewing donation")
        supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .add(binding.money.id, ViewDonationLocation.newInstance(don), _view_donation_location).commit()
    }

}