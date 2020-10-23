package com.bry.donorhub.Fragments.Authentication

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import com.bry.donorhub.R


class SignUp : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"
    private lateinit var listener: SignUpNameInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is SignUpNameInterface){
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_sign_up, container, false)
        val log_in_instead = v.findViewById<TextView>(R.id.log_in_instead)
        val next_layout: RelativeLayout = v.findViewById(R.id.next_layout)
        val editText: EditText = v.findViewById(R.id.editText)

        log_in_instead.setOnClickListener {
            listener.OnSignUpNameLogInInsteadSelected()
        }

        next_layout.setOnClickListener{
            val typedStuff = editText.text.toString().trim()
            if(typedStuff.equals("")) editText.setError("Type Something!")
            else{
                listener.OnSignUpNameContinueSelected(typedStuff)
            }
        }

        return v
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignUp().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    interface SignUpNameInterface {
        fun OnSignUpNameLogInInsteadSelected()
        fun OnSignUpNameContinueSelected(name: String)
    }
}