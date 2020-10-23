package com.bry.donorhub.Fragments.Authentication

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import com.bry.donorhub.Constants
import com.bry.donorhub.Model.Number
import com.bry.donorhub.R
import com.hbb20.CountryCodePicker


class SignUpPhone : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"
    private lateinit var listener: SignUpPhoneInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is SignUpPhoneInterface){
            listener = context
        }
    }

    var whenPhoneNumberPassedIsInvalid: () -> Unit = {}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_sign_up_phone, container, false)
        val next_layout: RelativeLayout = v.findViewById(R.id.next_layout)
        val phoneEditText: EditText = v.findViewById(R.id.phoneEditText)
        val set_currency: TextView = v.findViewById(R.id.set_currency)

        val ccp: CountryCodePicker = v.findViewById(R.id.ccp)
        ccp.setDefaultCountryUsingNameCode("US")
        ccp.setAutoDetectedCountry(true)
        ccp.registerCarrierNumberEditText(phoneEditText)
        ccp.setNumberAutoFormattingEnabled(true)



        ccp.setPhoneNumberValidityChangeListener {
            Log.e("SignUpPhone", "Is Valid: "+it)
        }

        ccp.setOnCountryChangeListener {
            set_currency.text = "In "+Constants().getCurrency(ccp.selectedCountryNameCode)
            val locale =
                context!!.resources.configuration.locale.country
            Log.e("SignUpPhone","The locale is: "+locale)
        }

        next_layout.setOnClickListener {
            val phoneNo = phoneEditText.text.toString().trim()
            if(phoneNo.equals("")){
                phoneEditText.setError("Please fill this")
            }else if(!ccp.isValidFullNumber){
                phoneEditText.setError("That's not a real number!")
            } else{
                Log.e("SignUpPhone", "Is Valid: "+ccp.isValidFullNumber)
                Log.d("SighUpPhone","Phone No: "+ccp.selectedCountryCodeWithPlus+phoneNo.replace(" ".toRegex(), "")+" "+
                        ccp.selectedCountryName+" "+ccp.selectedCountryNameCode)
                val the_number = Number(
                    phoneNo.replace(" ".toRegex(), "").toLong(),
                    ccp.selectedCountryCodeWithPlus,
                    ccp.selectedCountryName,
                    ccp.selectedCountryNameCode,
                    Constants().getCurrency(ccp.selectedCountryNameCode)
                )


                listener.OnSignUpPhoneContinueSelected(the_number)
            }
        }

        whenPhoneNumberPassedIsInvalid = {
            phoneEditText.setError("That number is already in use!")
            Constants().touch_vibrate(context)
        }

        return v
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignUpPhone().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    interface SignUpPhoneInterface {
        fun OnSignUpPhoneContinueSelected(phoneNo: Number)
    }

}