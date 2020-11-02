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


class SignIn : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"
    private lateinit var listener: SignInInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is SignInInterface){
            listener = context
        }
    }

    var didPasscodeFail: () -> Unit = {}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_sign_in, container, false)
        val emailEditText: EditText = v.findViewById(R.id.emailEditText)
        val passwordEditText: EditText = v.findViewById(R.id.PasswordEditText)

        val sign_up_instead: TextView = v.findViewById(R.id.sign_up_instead)
        val begin_layout: RelativeLayout = v.findViewById(R.id.begin_layout)

        sign_up_instead.setOnClickListener {
            listener.OnSignInSignUpInsteadSelected()
        }

        begin_layout.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if(email.equals("")){
                emailEditText.setError("We need your email")
            }else if(password.equals("")){
                passwordEditText.setError("We need your password")
            }else{
                listener.OnSubmitLogInDetails(email,password)
            }
        }

        didPasscodeFail = {
            emailEditText.setError("Retype these details")
        }

        return v
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignIn().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    interface SignInInterface {
        fun OnSignInSignUpInsteadSelected()
        fun OnSubmitLogInDetails(email: String, password: String)
    }
}