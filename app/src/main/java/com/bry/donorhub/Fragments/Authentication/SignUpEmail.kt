package com.bry.donorhub.Fragments.Authentication

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.bry.donorhub.R
import java.util.*
import kotlin.collections.ArrayList


class SignUpEmail : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"
    private lateinit var listener: SignUpEmailInterface
    private val easyPasswords: List<String> = ArrayList(
        Arrays.asList(
            "12345678",
            "98765432",
            "qwertyui",
            "asdfghjk",
            "zxcvbnm1",
            "123456ab",
            "123456qw",
            "987654qw",
            "987654as",
            ""
        )
    )
    private val passwordLength = 8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onAttach(
        context: Context
    ) {
        super.onAttach(context)
        if(context is SignUpEmailInterface){
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_sign_up_email, container, false)
        val emailEditText: EditText = v.findViewById(R.id.emailEditText)
        val passwordEditText: EditText = v.findViewById(R.id.PasswordEditText)
        val confirmPasswordEditText: EditText = v.findViewById(R.id.confirmPasswordEditText)
        val next_layout: RelativeLayout = v.findViewById(R.id.next_layout)
        val strength_expalainer: TextView = v.findViewById(R.id.strength_expalainer)
        val strength_progress_bar: ProgressBar = v.findViewById(R.id.strength_progress_bar)
        val confirm_progress_bar: ProgressBar = v.findViewById(R.id.confirm_progress_bar)

        val progressDrawable: Drawable = confirm_progress_bar.getProgressDrawable().mutate()
        progressDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        confirm_progress_bar.setProgressDrawable(progressDrawable)

        confirmPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val confirm: String = p0.toString()
                val currentLength: Int = confirm.length

                val typedPassword: String = passwordEditText.text.toString().trim()
                val maxLength: Int = passwordEditText.text.toString().trim().length

                if(maxLength != 0 && currentLength != 0){
                    confirm_progress_bar.visibility = View.VISIBLE

                    var correctionLength = 0

                    if(maxLength>=currentLength) {
                        val loopLn = currentLength-1
                        for (i in 0..loopLn) {

                            if (confirm.get(i).equals(typedPassword.get(i))) {
                                correctionLength++
                            }else{
                                break
                            }
                        }
                    }
                    Log.e("signup","correction: "+correctionLength)
                    val percent: Double = (correctionLength.toDouble() / maxLength.toDouble())*100

                    val anim = ProgressBarAnimation(confirm_progress_bar, confirm_progress_bar.progress.toFloat(), percent.toFloat())
                    anim.duration = 400
                    anim.interpolator = LinearOutSlowInInterpolator()
                    confirm_progress_bar.startAnimation(anim)


                }else{
                    confirm_progress_bar.visibility = View.INVISIBLE
                }
            }

        })

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val length: Int = p0!!.length

                if(length == 0){
                    strength_progress_bar.visibility = View.INVISIBLE
                    strength_expalainer.visibility = View.INVISIBLE
                }else{
                    strength_progress_bar.visibility = View.VISIBLE
                    strength_expalainer.visibility = View.VISIBLE
                }

                val percent: Double = (length.toDouble() / passwordLength.toDouble())*100

                val anim = ProgressBarAnimation(strength_progress_bar, strength_progress_bar.progress.toFloat(), percent.toFloat())
                anim.duration = 400
                anim.interpolator = LinearOutSlowInInterpolator()
                strength_progress_bar.startAnimation(anim)

            }

        })

        next_layout.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirm_password = confirmPasswordEditText.text.toString().trim()

            if(isValidEmail(email, emailEditText)
                && isValidPassword(
                    password,
                    confirm_password,
                    passwordEditText,
                    confirmPasswordEditText)
            ){
                listener.OnSignUpEmailContinueSelected(email, password)
            }
        }

        return v
    }

    private fun isValidEmail(
        email: String,
        mEmailEditText: EditText
    ): Boolean {
        if (email == "") {
            mEmailEditText.setError("We need your passwords")
            return false
        }
        val isGoodEmail =
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        if (!email.contains("@")) {
            mEmailEditText.setError("Thats not your email")
            return false
        }
        var counter = 0
        for (i in 0 until email.length) {
            if (email[i] == '.') {
                counter++
            }
        }
        if (counter != 1 && counter != 2 && counter != 3) {
            mEmailEditText.setError("That email wont work")
            return false
        }
        var counter2 = 0
        var continueIncrement = true
        for (i in 0 until email.length) {
            if (email[i] == '@') {
                continueIncrement = false
            }
            if (continueIncrement) counter2++
        }
        if (counter2 <= 3) {
            mEmailEditText.setError("That is not an email address")
            return false
        }
        if (!isGoodEmail) {
            mEmailEditText.setError("That email wont work")
            return false
        }
        return isGoodEmail
    }

    private fun isValidPassword(
        password: String,
        confirmPassword: String,
        mPasswordEditText: EditText,
        mConfirmPasswordEditText: EditText
    ): Boolean {
        if (password == "") {
            mPasswordEditText.setError("We need a password")
            return false
        }else if(confirmPassword == ""){
            mConfirmPasswordEditText.setError("confirm your password")
        } else if (password.length < passwordLength) {
            mPasswordEditText.setError("at least 8 characters")
            return false
        } else if (password != confirmPassword) {
            mPasswordEditText.setError("Passwords dont match")
            return false
        } else if (easyPasswords.contains(password)) {
            mPasswordEditText.setError("Use a strong passwords")
            return false
        }
        return true
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignUpEmail().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    class ProgressBarAnimation(
        private val progressBar: ProgressBar,
        private val from: Float,
        private val to: Float
    ) :
        Animation() {
        override fun applyTransformation(
            interpolatedTime: Float,
            t: Transformation?
        ) {
            super.applyTransformation(interpolatedTime, t)
            val value = from + (to - from) * interpolatedTime
            progressBar.progress = value.toInt()
        }

    }

    interface SignUpEmailInterface {
        fun OnSignUpEmailContinueSelected(email: String, password: String)
    }

}