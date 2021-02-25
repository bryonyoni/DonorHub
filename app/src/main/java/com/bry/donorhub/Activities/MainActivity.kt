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
import com.bry.donorhub.Model.*
import com.bry.donorhub.Model.Number
import com.bry.donorhub.R
import com.bry.donorhub.databinding.ActivityMainBinding
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
import java.nio.charset.StandardCharsets
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


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
        ViewDonation.ViewDonationInterface,
        EditDonation.EditDonationInterface
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
    val _edit_donation = "_edit_donation"

    private lateinit var binding: ActivityMainBinding
    val db = Firebase.firestore

    private var donations: ArrayList<Donation> = ArrayList()
    private var organisations: ArrayList<Organisation> = ArrayList()
    private var activities: ArrayList<Donation.activity> = ArrayList()
    private var users : ArrayList<Constants.user> = ArrayList()
    var doubleBackToExitPressedOnce: Boolean = false
    var is_loading: Boolean = false

    private var blockchain: ArrayList<Block> = ArrayList()
    private var donationHashMap: HashMap<String, String> = HashMap()
    private var keys: HashMap<String, String> = HashMap()

    var privateKey: PrivateKey? = null
    var publicKey: PublicKey? = null
    var signature: ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val actionBar: ActionBar = supportActionBar!!
        actionBar.hide()

        if(constants.SharedPreferenceManager(applicationContext).getPersonalInfo() == null){
            supportFragmentManager.beginTransaction().setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
            )
                    .replace(binding.money.id, SignUp.newInstance("", ""), _sign_up).commit()
        }else{
            loadDonationsAndOrganisations()
            openPickOrganisation()
        }

        val pubKey = constants.SharedPreferenceManager(applicationContext).fetchPubKey()
        if(pubKey.equals("")){
            //we need to gen a pub key
            Log.e(TAG, "generating a pub-priv key")
            generateMyKeyPair()
        }

        val keystring = constants.SharedPreferenceManager(applicationContext).getDataKeysForDonations()
        if(!keystring.equals("")){
            keys = Gson().fromJson(keystring, symm_keys::class.java).keys_hash
        }


        //
        val block1 = Block("test data for block 1", "0")
        val block2 = Block("test data for block 2", block1.calculateHash())
        val block3 = Block("test data for block 3", block2.calculateHash())

        Log.e(TAG, "block1: ${block1.data} Hash: ${block1.calculateHash()}")
        Log.e(TAG, "block2: ${block2.data} Hash: ${block2.calculateHash()}")
        Log.e(TAG, "block3: ${block3.data} Hash: ${block3.calculateHash()}")

        val block2clone = Block("clone data for block 2", block1.calculateHash())
        block2clone.timestamp = block2.timestamp
        Log.e(TAG, "block2clone: ${block2clone.data} Hash: ${block2clone.calculateHash()}")


        //
        Security.addProvider(org.spongycastle.jce.provider.BouncyCastleProvider())
        generateKeyPair()


        Log.e(TAG, "public key: ${Base64.getEncoder().encodeToString(publicKey?.encoded)}")

        val enc = Base64.getEncoder().encodeToString(publicKey?.encoded)
        val kf = KeyFactory.getInstance("RSA", "SC")
        val x509ks = X509EncodedKeySpec(Base64.getDecoder().decode(enc))
        val decoded_pub: PublicKey = kf.generatePublic(x509ks)

        Log.e(TAG, "decoded public key: ${Base64.getEncoder().encodeToString(decoded_pub.encoded)}")

        val enc2 = Base64.getEncoder().encodeToString(privateKey?.encoded)
        var p8ks = PKCS8EncodedKeySpec(Base64.getDecoder().decode(enc2))
        val kf2 = KeyFactory.getInstance("RSA", "SC")
        val privKeyA = kf2.generatePrivate(p8ks)

        generateSignature(privateKey, "my secure data!")

        val isSignatureValid = verifySignature("my secure data!", publicKey!!, signature!!)

        Log.e(TAG, "is signature valid??: $isSignatureValid")


        val msg = "my secure data"
        val charset = StandardCharsets.UTF_8
        val encrypt: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        encrypt.init(Cipher.ENCRYPT_MODE, publicKey)
        // encrypt with known character encoding, you should probably use hybrid cryptography instead
        val encryptedMessage: ByteArray = encrypt.doFinal(msg.toByteArray(charset))

        Log.e(TAG, "encrypted message: ${encryptedMessage.toString(charset)}")

        val decrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        decrypt.init(Cipher.DECRYPT_MODE, privateKey)
        val decryptedMessage = String(decrypt.doFinal(encryptedMessage), StandardCharsets.UTF_8)

        Log.e(TAG, "decrypted message: ${decryptedMessage}")


//        Log.e(TAG, "my symm key: ${symmKey}")

//        tryTheThing()
    }

    fun tryTheThing(){
        Log.e(TAG, "Running the thing........................")
        val my_symm_key = "key123"
        val enc_obj = runSymmetricEncryption("the quick brown", my_symm_key)

        val encPubKey = Base64.getEncoder().encodeToString(publicKey?.encoded)
        val kf = KeyFactory.getInstance("RSA", "SC")
        val x509ks = X509EncodedKeySpec(Base64.getDecoder().decode(encPubKey))
        val decoded_pub: PublicKey = kf.generatePublic(x509ks)

        val encPriKey = Base64.getEncoder().encodeToString(privateKey?.encoded)
        var p8ks = PKCS8EncodedKeySpec(Base64.getDecoder().decode(encPriKey))
        val kf2 = KeyFactory.getInstance("RSA", "SC")
        val privKeyA = kf2.generatePrivate(p8ks)

        val ref = db.collection("test_stuff").document()

        val charset = StandardCharsets.UTF_8
        val encrypt: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        encrypt.init(Cipher.ENCRYPT_MODE, decoded_pub)
        val encryptedMessage: ByteArray = encrypt.doFinal(my_symm_key.toByteArray(charset))

        val enc_data = Base64.getEncoder().encodeToString(encryptedMessage)
        val signature_word = "gucci123"
        val signatures = generateSignature(privKeyA, signature_word)
        val sig_string = Base64.getEncoder().encodeToString(signatures)

        val data = hashMapOf(
                "data" to enc_obj,
                "enc_data_key" to enc_data,
                "signature" to sig_string,
                "uploader_pub_key" to encPubKey,
                "doc_id" to ref.id
        )

//        Log.e(TAG, "Decrypting the thing........................")
//
//        val dec_sig = Base64.getDecoder().decode(sig_string)
//
//        val isSignatureValid = verifySignature(signature_word, decoded_pub, dec_sig)
//
//        Log.e(TAG, "is their signature valid??: ${isSignatureValid}")
//
//        val decrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding")
//        decrypt.init(Cipher.DECRYPT_MODE, privKeyA)
//
//        //try using encrypted message as string
//        val decryptedEncMsg = Base64.getDecoder().decode(enc_data)
//
//        val decryptedMessage = String(decrypt.doFinal(decryptedEncMsg), StandardCharsets.UTF_8)
//        Log.e(TAG, "decrypted key: ${decryptedMessage} message ${runSymmetricDecryption(enc_obj,decryptedMessage)}")




        ref.set(data).addOnSuccessListener {
            db.collection("test_stuff").document(ref.id).get().addOnSuccessListener {
                Log.e(TAG, "Decrypting the thing........................")
                val kfdec = KeyFactory.getInstance("RSA", "SC")
                val x509ksdec = X509EncodedKeySpec(Base64.getDecoder().decode(it["uploader_pub_key"] as String))
                val uploaderPub: PublicKey = kfdec.generatePublic(x509ksdec)
                val dec_sig = Base64.getDecoder().decode(it["signature"] as String)

                val isSignatureValiddec = verifySignature(signature_word, uploaderPub, dec_sig)

                Log.e(TAG, "is their signature valid??: ${isSignatureValiddec}")
                val decryptdec = Cipher.getInstance("RSA/ECB/PKCS1Padding")
                decryptdec.init(Cipher.DECRYPT_MODE, privKeyA)

                //try using encrypted message as string
                val decryptedEncMsgdec = Base64.getDecoder().decode(it["enc_data_key"] as String)
                val decryptedMessagedec = String(decryptdec.doFinal(decryptedEncMsgdec), StandardCharsets.UTF_8)
                val mySecureData = runSymmetricDecryption((it["data"] as String),decryptedMessagedec)
                Log.e(TAG, "decrypted key: ${decryptedMessagedec} message" +
                        " ${mySecureData}")
            }
        }
    }



    fun decryptDataIfPossible(data: String, privateKey: String, signature: String, author_pubKey: String): String{
        val kf = KeyFactory.getInstance("RSA", "SC")
        var p8ks = PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey))
        val myPrivKey = kf.generatePrivate(p8ks)

        var p8ks_auth = X509EncodedKeySpec(Base64.getDecoder().decode(author_pubKey))
        val authPubKey = kf.generatePublic(p8ks_auth)

        if(verifySignature(data, authPubKey, Base64.getDecoder().decode(signature))){
            //the signature matches, so the data was written by the expected author
            val decrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            decrypt.init(Cipher.DECRYPT_MODE, myPrivKey)
            //
//            val decryptedData = String(decrypt.doFinal(Base64.getDecoder().decode(data)), StandardCharsets.UTF_8)
            val decryptedData = Base64.getEncoder().encodeToString(decrypt.doFinal(Base64.getDecoder().decode(data)))
            Log.e(TAG, "decrypted data: ${decryptedData}")

            return decryptedData
        }else{
            Log.e(TAG, "signature mismatch, data not from author")
        }

        return ""
    }

    fun generateKeyPair() {
        try {
            val keyGen: KeyPairGenerator = KeyPairGenerator.getInstance("RSA")
//            val random: SecureRandom = SecureRandom.getInstance("SHA1PRNG")
//            val ecSpec = ECGenParameterSpec("secp224k1")

            // Initialize the key generator and generate a KeyPair
            keyGen.initialize(1024) //256 bytes provides an acceptable security level
            val keyPair: KeyPair = keyGen.generateKeyPair()

            // Set the public and private keys from the keyPair
            privateKey = keyPair.getPrivate()
            publicKey = keyPair.getPublic()

        } catch (e: java.lang.Exception) {
            throw RuntimeException(e)
        }
    }

    fun applyECDSASig(privateKey: PrivateKey?, input: String): ByteArray? {
        val dsa: Signature
        var output: ByteArray? = ByteArray(0)
        try {
            dsa = Signature.getInstance("RSA", "SC")
            dsa.initSign(privateKey)
            val strByte = input.toByteArray()
//            val strByte = Base64.getDecoder().decode(input)
            dsa.update(strByte)
            val realSig = dsa.sign()
            output = realSig
        } catch (e: java.lang.Exception) {
            throw java.lang.RuntimeException(e)
        }
        return output
    }

    fun verifyECDSASig(publicKey: PublicKey?, data: String, signature: ByteArray?): Boolean {
        return try {
            val ecdsaVerify = Signature.getInstance("RSA", "SC")
            ecdsaVerify.initVerify(publicKey)
            ecdsaVerify.update(data.toByteArray())
//            ecdsaVerify.update(Base64.getDecoder().decode(data))
            ecdsaVerify.verify(signature)
        } catch (e: java.lang.Exception) {
            throw java.lang.RuntimeException(e)
        }
    }


    fun generateSignature(privateKey: PrivateKey?, data: String): ByteArray? {
//        val data: String = StringUtil.getStringFromKey(sender) +
//                StringUtil.getStringFromKey(reciepient).toString() +
//                java.lang.Float.toString(value)
        signature = applyECDSASig(privateKey, data)
        return  signature
    }

    fun verifySignature(data: String, publicKey: PublicKey, signature: ByteArray): Boolean {
//        val data: String = StringUtil.getStringFromKey(sender) +
//                StringUtil.getStringFromKey(reciepient).toString() + java.lang.Float.toString(value)
        return verifyECDSASig(publicKey, data, signature)
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
        users.clear()
        val uid = FirebaseAuth.getInstance().currentUser!!.uid

        db.collection("organisations").get().addOnSuccessListener {
            if(!it.isEmpty){
                for(doc in it.documents){
                    if(doc.contains("org_obj")){
                        val org = Gson().fromJson(
                                doc["org_obj"] as String,
                                Organisation::class.java
                        )
                        if(doc.contains("pub_key")) {
                            org.pub_key = doc["pub_key"] as String
                            organisations.add(org)
                        }
                    }
                }
            }
        }

        db.collection("donations").get().addOnSuccessListener {
            if(!it.isEmpty){
                for(doc in it.documents){
                    if(doc.contains("don_obj")
                        && doc.contains("signature")
                        && ((doc["uploader"] as String) == uid)
                    ){
                        if(keys.containsKey(doc.id)) {
                            val key = keys.get(doc.id)
                            val mySecureData = runSymmetricDecryption((doc["don_obj"] as String), key!!)

                            val don = Gson().fromJson(mySecureData, Donation::class.java)
                            if (doc.contains("taken_down")) {
                                don.is_taken_down = doc["taken_down"] as Boolean
                            }
                            if (doc.contains("collectors")) {
                                don.collectors = Gson().fromJson(
                                        doc["collectors"] as String,
                                        Collectors::class.java
                                )
                            }
                            if (doc.contains("pick_up_time")) {
                                don.pick_up_time = doc["pick_up_time"] as Long
                            }

                            if (doc.contains("batch")) {
                                don.batch_id = doc["batch"] as String
                            }

                            donations.add(don)
                        }
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

                    activities.add(
                            Donation.activity(
                                    explanation,
                                    timestamp,
                                    donation_id,
                                    activity_id
                            )
                    )
                }
            }
        }

        db.collection(constants.coll_users).get().addOnSuccessListener {
            for(user in it.documents) {
                val name = user.get("name") as String
                val email = user.get("email") as String
                val uid = user.get("uid") as String
                val user_country = user.get("user_country") as String
                val sign_up_time = user.get("sign_up_time") as Long
                val numbr = Gson().fromJson(user.get("phone_number") as String, Number::class.java)

                val us = Constants().user(numbr, email, name, sign_up_time, uid)
                users.add(us)
            }
        }

    }

    fun whenDoneLoadingData(){

        if(supportFragmentManager.findFragmentByTag(_pick_org)!=null){
            (supportFragmentManager.findFragmentByTag(_pick_org) as PickOrganisation).when_data_updated(
                    organisations
            )
        }

        if(supportFragmentManager.findFragmentByTag(_view_organisation)!=null){
            (supportFragmentManager.findFragmentByTag(_view_organisation) as ViewOrganisation)
        }

    }




    fun openPickOrganisation(){
        val orgs = Gson().toJson(Organisation.organisation_list(organisations))
        supportFragmentManager.beginTransaction().setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left
        )
                .replace(binding.money.id, PickOrganisation.newInstance("", "", orgs), _pick_org).commit()
    }





    override fun OnSignUpNameLogInInsteadSelected() {
        supportFragmentManager.beginTransaction().setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left
        )
                .replace(binding.money.id, SignIn.newInstance("", ""), _sign_in).commit()
    }

    var name: String = ""
    var email: String = ""
    var passcode: String = ""
    lateinit var number: Number
    override fun OnSignUpNameContinueSelected(name: String) {
        this.name = name

        supportFragmentManager.beginTransaction().setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left
        )
                .replace(binding.money.id, SignUpEmail.newInstance("", ""), _sign_up_email).commit()
    }

    override fun OnSignUpEmailContinueSelected(email: String, password: String) {
        this.email = email
        this.passcode = password

        supportFragmentManager.beginTransaction().setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left
        )
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

            mAuth!!.createUserWithEmailAndPassword(email, passcode)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("main", "authentication successful")
                            createFirebaseUserProfile(task.result!!.user, email, name, number)
                        } else {
                            Snackbar.make(
                                    binding.root,
                                    resources.getString(R.string.that_didnt_work),
                                    Snackbar.LENGTH_LONG
                            ).show()
                            hideLoadingScreen()
                        }
                    }
        }else{
            Snackbar.make(
                    binding.root, getString(R.string.please_check_on_your_internet_connection),
                    Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun createFirebaseUserProfile(
            user: FirebaseUser?,
            email: String,
            name: String,
            numbr: Number
    ) {
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
        supportFragmentManager.beginTransaction().setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left
        )
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
                            val numbr = Gson().fromJson(
                                    it.get("phone_number") as String,
                                    Number::class.java
                            )

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
                    Snackbar.make(
                            binding.root,
                            "That didn't work. Please check your credentials and retry.",
                            Snackbar.LENGTH_LONG
                    ).show()
                    if(supportFragmentManager.findFragmentByTag(_sign_in)!=null){
                        (supportFragmentManager.findFragmentByTag(_sign_in) as SignIn).didPasscodeFail()
                    }
                    mAuth!!.removeAuthStateListener { mAuthListener!!}
                    hideLoadingScreen()
                }
            }.addOnFailureListener { }
        }else{
            Snackbar.make(
                    binding.root,
                    getString(R.string.please_check_on_your_internet_connection),
                    Snackbar.LENGTH_SHORT
            ).show()
        }
    }





    override fun whenPickOrganisationOrgPicked(organisation: Organisation) {
        val dons = ""
        val orgs = Gson().toJson(organisation)
        supportFragmentManager.beginTransaction().setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left
        )
                .add(
                        binding.money.id,
                        ViewOrganisation.newInstance("", "", orgs, dons),
                        _view_organisation
                ).commit()
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
        supportFragmentManager.beginTransaction().setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left
        )
                .add(binding.money.id, MyDonations.newInstance("", "", dons), _my_donations).commit()
    }





    override fun whenViewOrganisationCreateNewDonation(organisation: Organisation) {
        val org = Gson().toJson(organisation)
        supportFragmentManager.beginTransaction().setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left
        )
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

    override fun whenEditDonationPickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun whenEditDonationFinished(donation: Donation) {
        showLoadingScreen()

        val ref = db.collection("donations")
            .document(donation.donation_id)

        ref.update(
                mapOf(
                        "don_obj" to Gson().toJson(donation),
                        "taken_down" to donation.is_taken_down,
                )
        ).addOnSuccessListener {
            hideLoadingScreen()
            Toast.makeText(applicationContext, "done!", Toast.LENGTH_SHORT).show()
            onBackPressed()
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri: Uri = result.uri
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, resultUri)

                if(supportFragmentManager.findFragmentByTag(_new_donation)!=null){
                    (supportFragmentManager.findFragmentByTag(_new_donation) as NewDonation).onImagePicked(
                            bitmap
                    )
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
                    val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(
                            contentResolver,
                            mFilepath
                    )
                    if(supportFragmentManager.findFragmentByTag(_new_donation)!=null){
                        (supportFragmentManager.findFragmentByTag(_new_donation) as NewDonation).onImagePicked(
                                bitmap
                        )
                    }
                    if(supportFragmentManager.findFragmentByTag(_edit_donation)!=null){
                        (supportFragmentManager.findFragmentByTag(_edit_donation) as EditDonation).onImagePicked(
                                bitmap
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }





    override fun whenNewDonationFinished(
            text: String, images: ArrayList<ByteArray>,
            organisation: Organisation, location: LatLng, quantity: String, mass: String
    ) {
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
        don.quantity = quantity
        don.mass = "$mass Kg"

        for(image in images){
            val id = db.collection("gucci").document().id
            write_image(image, id, ref.id)
            val im = Donation.donation_image(id)
            don.images.add(im)
        }


        val source = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val my_symm_key =  java.util.Random().ints(24, 0, source.length)
                .toArray()
                .map(source::get)
                .joinToString("")

        val dat = Gson().toJson(don)
        val enc_obj = runSymmetricEncryption(dat, my_symm_key)

        val encPubKey = organisation.pub_key
        val kf = KeyFactory.getInstance("RSA", "SC")
        val x509ks = X509EncodedKeySpec(Base64.getDecoder().decode(encPubKey))
        val decoded_pub: PublicKey = kf.generatePublic(x509ks)

        val myPubKeyString = constants.SharedPreferenceManager(applicationContext).fetchPubKey()
        val kf3 = KeyFactory.getInstance("RSA", "SC")
        val x509ks3 = X509EncodedKeySpec(Base64.getDecoder().decode(myPubKeyString))
        val myPubKey: PublicKey = kf3.generatePublic(x509ks3)

        val encPriKey = constants.SharedPreferenceManager(applicationContext).fetchPrivKey()
        var p8ks = PKCS8EncodedKeySpec(Base64.getDecoder().decode(encPriKey))
        val kf2 = KeyFactory.getInstance("RSA", "SC")
        val privKeyA = kf2.generatePrivate(p8ks)


        val charset = StandardCharsets.UTF_8
        val encrypt: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        encrypt.init(Cipher.ENCRYPT_MODE, decoded_pub)
        val encryptedMessage: ByteArray = encrypt.doFinal(my_symm_key.toByteArray(charset))

        val enc_data = Base64.getEncoder().encodeToString(encryptedMessage)
        val signature_word = uid
        val signatures = generateSignature(privKeyA, signature_word)
        val sig_string = Base64.getEncoder().encodeToString(signatures)

        val data = hashMapOf(
                "don_obj" to enc_obj,
                "enc_data_key" to enc_data,
                "taken_down" to false,
                "time_of_creation" to time,
                "org_id" to ref.id,
                "uploader" to uid,
                "location" to Gson().toJson(LatLng(0.0,0.0)),
                "batch" to "",
                "signature" to sig_string,
                "uploader_pub_key" to myPubKeyString
        )

        donations.add(don)

        Log.e(TAG, "compare .........--------------")
        Log.e(TAG,"signature_word(uploader) --- ${uid}")
        Log.e(TAG,"uploaderPub key --- ${myPubKeyString}")
        Log.e(TAG,"signature  --- ${sig_string}")

        ref.set(data).addOnSuccessListener {
            keys.put(ref.id, my_symm_key)
            constants.SharedPreferenceManager(applicationContext)
                    .setDataKeysForDonations(Gson().toJson(symm_keys(keys)))

            hideLoadingScreen()
            Toast.makeText(applicationContext, "done!", Toast.LENGTH_SHORT).show()
            open_view_donation(don, organisation)
        }

    }

    class symm_keys(var keys_hash: HashMap<String, String>)

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
            Log.e(TAG, it.message.toString())
        }.addOnSuccessListener {
            Log.e(TAG, "Written image in the cloud!")
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

        var user = Gson().toJson(users[0])

        if (donation.collectors!=null) {
            for (item in users) {
                if (item.uid.equals(donation.collectors?.uid)) {
                    user = Gson().toJson(item)
                }
            }
        }

        supportFragmentManager.beginTransaction().setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left
        )
                .add(
                        binding.money.id,
                        ViewDonation.newInstance("", "", org, don, act_string, user),
                        _view_donation
                ).commit()
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
        supportFragmentManager.beginTransaction().setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left
        )
            .add(binding.money.id, PickMapLocation(), _map_fragment).commit()

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
            ActivityCompat.requestPermissions(
                    this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            ), requestCodeForViewingMyLoc
            )
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
                            Log.e(TAG, "wayLatitude: ${wayLatitude} longitude: ${wayLongitude}")
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
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)

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
            (supportFragmentManager.findFragmentByTag(_new_donation) as NewDonation).onLocationPicked(
                    latLng
            )
        }
        onBackPressed()
    }


    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String?>,
            grantResults: IntArray
    ) {
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
        supportFragmentManager.beginTransaction().setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left
        )
                .add(
                        binding.money.id,
                        ViewDonationLocation.newInstance(don),
                        _view_donation_location
                ).commit()
    }

    override fun editDonation(donation: Donation, organ: Organisation) {
        supportFragmentManager.beginTransaction().setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left
        )
            .add(
                    binding.money.id, EditDonation.newInstance(
                    "", "",
                    Gson().toJson(donation), Gson().toJson(organ)
            ), _edit_donation
            ).commit()
    }


    fun generateMyKeyPair() {
        try {
            val keyGen: KeyPairGenerator = KeyPairGenerator.getInstance("RSA")
//            val random: SecureRandom = SecureRandom.getInstance("SHA1PRNG")
//            val ecSpec = ECGenParameterSpec("secp224k1")

            // Initialize the key generator and generate a KeyPair
            keyGen.initialize(4096) //256 bytes provides an acceptable security level
            val keyPair: KeyPair = keyGen.generateKeyPair()

            // Set the public and private keys from the keyPair
            var privateKey = keyPair.getPrivate()
            var publicKey = keyPair.getPublic()

            val enc_pub = Base64.getEncoder().encodeToString(publicKey?.encoded)
            val enc_priv = Base64.getEncoder().encodeToString(privateKey?.encoded)

            constants.SharedPreferenceManager(applicationContext).stashPrivKey(enc_priv)
            constants.SharedPreferenceManager(applicationContext).stashPubKey(enc_pub)

        } catch (e: java.lang.Exception) {
            throw RuntimeException(e)
        }
    }

    fun encryptDataWithOrgKey(data: String, org_key: String): String{
        val charset = StandardCharsets.UTF_8
        val encrypt: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        val kf = KeyFactory.getInstance("RSA", "SC")

        val x509ks = X509EncodedKeySpec(Base64.getDecoder().decode(org_key))
        val decoded_pub: PublicKey = kf.generatePublic(x509ks)

        encrypt.init(Cipher.ENCRYPT_MODE, decoded_pub)
        // encrypt with known character encoding, you should probably use hybrid cryptography instead
        val data_byte_array: ByteArray = Base64.getDecoder().decode(data)
        val encryptedMessage: ByteArray = encrypt.doFinal(data_byte_array)

        val encodedmessage: String = Base64.getEncoder().encodeToString(encryptedMessage)

        Log.e(TAG, "encrypted message: ${encodedmessage}")

        return encodedmessage
    }

    fun generateSignatureForData(data: String): String{
        val myPrivKey = constants.SharedPreferenceManager(applicationContext).fetchPrivKey()
        val kf = KeyFactory.getInstance("RSA", "SC")
        var p8ks = PKCS8EncodedKeySpec(Base64.getDecoder().decode(myPrivKey))

        val privKeyA = kf.generatePrivate(p8ks)
        val signature_byte_array = applyECDSASig(privKeyA, data)

        return Base64.getEncoder().encodeToString(signature_byte_array)
    }


    fun decryptDataWithMyKey(data: String): String{
        val kf = KeyFactory.getInstance("RSA", "SC")
        val myPrivKey = constants.SharedPreferenceManager(applicationContext).fetchPrivKey()
        var p8ks = PKCS8EncodedKeySpec(Base64.getDecoder().decode(myPrivKey))
        val privKeyA = kf.generatePrivate(p8ks)

        val decrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        decrypt.init(Cipher.DECRYPT_MODE, privKeyA)
        val decrypted_data = String(decrypt.doFinal(data.toByteArray(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)

        return decrypted_data
    }


    fun runSymmetricEncryption(data: String, code: String): String{
        val key = code

        val plaintext = data
        val symmetricEncryption = SymmetricEncryption()

        //encode
        val encrypted = symmetricEncryption.encrypt(
                plaintext, key
        )

        return encrypted
    }

    fun runSymmetricDecryption(data: String, code: String): String{
        val symmetricEncryption = SymmetricEncryption()

        val decrypted = symmetricEncryption.decrypt(
                ciphertext = data,
                secret = code
        )

        return decrypted
    }

}