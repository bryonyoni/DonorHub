package com.bry.donorhub

import android.animation.ValueAnimator
import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Base64
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream
import java.util.HashMap

class Constants{
    val vib_time: Long = 20
    val local_image = "local_image"
    val donation_data = "donation_data"


    fun getCurrency(country_code: String):String{
        val retMap: Map<String, String> = Gson().fromJson(
            "{\"BD\": \"BDT\", \"BE\": \"EUR\", \"BF\": \"XOF\", \"BG\": \"BGN\", \"BA\": \"BAM\", \"BB\": \"BBD\", \"WF\": \"XPF\", \"BL\": \"EUR\", \"BM\": \"BMD\", \"BN\": \"BND\", \"BO\": \"BOB\", \"BH\": \"BHD\", \"BI\": \"BIF\", \"BJ\": \"XOF\", \"BT\": \"BTN\", \"JM\": \"JMD\", \"BV\": \"NOK\", \"BW\": \"BWP\", \"WS\": \"WST\", \"BQ\": \"USD\", \"BR\": \"BRL\", \"BS\": \"BSD\", \"JE\": \"GBP\", \"BY\": \"BYR\", \"BZ\": \"BZD\", \"RU\": \"RUB\", \"RW\": \"RWF\", \"RS\": \"RSD\", \"TL\": \"USD\", \"RE\": \"EUR\", \"TM\": \"TMT\", \"TJ\": \"TJS\", \"RO\": \"RON\", \"TK\": \"NZD\", \"GW\": \"XOF\", \"GU\": \"USD\", \"GT\": \"GTQ\", \"GS\": \"GBP\", \"GR\": \"EUR\", \"GQ\": \"XAF\", \"GP\": \"EUR\", \"JP\": \"JPY\", \"GY\": \"GYD\", \"GG\": \"GBP\", \"GF\": \"EUR\", \"GE\": \"GEL\", \"GD\": \"XCD\", \"GB\": \"GBP\", \"GA\": \"XAF\", \"SV\": \"USD\", \"GN\": \"GNF\", \"GM\": \"GMD\", \"GL\": \"DKK\", \"GI\": \"GIP\", \"GH\": \"GHS\", \"OM\": \"OMR\", \"TN\": \"TND\", \"JO\": \"JOD\", \"HR\": \"HRK\", \"HT\": \"HTG\", \"HU\": \"HUF\", \"HK\": \"HKD\", \"HN\": \"HNL\", \"HM\": \"AUD\", \"VE\": \"VEF\", \"PR\": \"USD\", \"PS\": \"ILS\", \"PW\": \"USD\", \"PT\": \"EUR\", \"SJ\": \"NOK\", \"PY\": \"PYG\", \"IQ\": \"IQD\", \"PA\": \"PAB\", \"PF\": \"XPF\", \"PG\": \"PGK\", \"PE\": \"PEN\", \"PK\": \"PKR\", \"PH\": \"PHP\", \"PN\": \"NZD\", \"PL\": \"PLN\", \"PM\": \"EUR\", \"ZM\": \"ZMK\", \"EH\": \"MAD\", \"EE\": \"EUR\", \"EG\": \"EGP\", \"ZA\": \"ZAR\", \"EC\": \"USD\", \"IT\": \"EUR\", \"VN\": \"VND\", \"SB\": \"SBD\", \"ET\": \"ETB\", \"SO\": \"SOS\", \"ZW\": \"ZWL\", \"SA\": \"SAR\", \"ES\": \"EUR\", \"ER\": \"ERN\", \"ME\": \"EUR\", \"MD\": \"MDL\", \"MG\": \"MGA\", \"MF\": \"EUR\", \"MA\": \"MAD\", \"MC\": \"EUR\", \"UZ\": \"UZS\", \"MM\": \"MMK\", \"ML\": \"XOF\", \"MO\": \"MOP\", \"MN\": \"MNT\", \"MH\": \"USD\", \"MK\": \"MKD\", \"MU\": \"MUR\", \"MT\": \"EUR\", \"MW\": \"MWK\", \"MV\": \"MVR\", \"MQ\": \"EUR\", \"MP\": \"USD\", \"MS\": \"XCD\", \"MR\": \"MRO\", \"IM\": \"GBP\", \"UG\": \"UGX\", \"TZ\": \"TZS\", \"MY\": \"MYR\", \"MX\": \"MXN\", \"IL\": \"ILS\", \"FR\": \"EUR\", \"IO\": \"USD\", \"SH\": \"SHP\", \"FI\": \"EUR\", \"FJ\": \"FJD\", \"FK\": \"FKP\", \"FM\": \"USD\", \"FO\": \"DKK\", \"NI\": \"NIO\", \"NL\": \"EUR\", \"NO\": \"NOK\", \"NA\": \"NAD\", \"VU\": \"VUV\", \"NC\": \"XPF\", \"NE\": \"XOF\", \"NF\": \"AUD\", \"NG\": \"NGN\", \"NZ\": \"NZD\", \"NP\": \"NPR\", \"NR\": \"AUD\", \"NU\": \"NZD\", \"CK\": \"NZD\", \"XK\": \"EUR\", \"CI\": \"XOF\", \"CH\": \"CHF\", \"CO\": \"COP\", \"CN\": \"CNY\", \"CM\": \"XAF\", \"CL\": \"CLP\", \"CC\": \"AUD\", \"CA\": \"CAD\", \"CG\": \"XAF\", \"CF\": \"XAF\", \"CD\": \"CDF\", \"CZ\": \"CZK\", \"CY\": \"EUR\", \"CX\": \"AUD\", \"CR\": \"CRC\", \"CW\": \"ANG\", \"CV\": \"CVE\", \"CU\": \"CUP\", \"SZ\": \"SZL\", \"SY\": \"SYP\", \"SX\": \"ANG\", \"KG\": \"KGS\", \"KE\": \"KES\", \"SS\": \"SSP\", \"SR\": \"SRD\", \"KI\": \"AUD\", \"KH\": \"KHR\", \"KN\": \"XCD\", \"KM\": \"KMF\", \"ST\": \"STD\", \"SK\": \"EUR\", \"KR\": \"KRW\", \"SI\": \"EUR\", \"KP\": \"KPW\", \"KW\": \"KWD\", \"SN\": \"XOF\", \"SM\": \"EUR\", \"SL\": \"SLL\", \"SC\": \"SCR\", \"KZ\": \"KZT\", \"KY\": \"KYD\", \"SG\": \"SGD\", \"SE\": \"SEK\", \"SD\": \"SDG\", \"DO\": \"DOP\", \"DM\": \"XCD\", \"DJ\": \"DJF\", \"DK\": \"DKK\", \"VG\": \"USD\", \"DE\": \"EUR\", \"YE\": \"YER\", \"DZ\": \"DZD\", \"US\": \"USD\", \"UY\": \"UYU\", \"YT\": \"EUR\", \"UM\": \"USD\", \"LB\": \"LBP\", \"LC\": \"XCD\", \"LA\": \"LAK\", \"TV\": \"AUD\", \"TW\": \"TWD\", \"TT\": \"TTD\", \"TR\": \"TRY\", \"LK\": \"LKR\", \"LI\": \"CHF\", \"LV\": \"EUR\", \"TO\": \"TOP\", \"LT\": \"LTL\", \"LU\": \"EUR\", \"LR\": \"LRD\", \"LS\": \"LSL\", \"TH\": \"THB\", \"TF\": \"EUR\", \"TG\": \"XOF\", \"TD\": \"XAF\", \"TC\": \"USD\", \"LY\": \"LYD\", \"VA\": \"EUR\", \"VC\": \"XCD\", \"AE\": \"AED\", \"AD\": \"EUR\", \"AG\": \"XCD\", \"AF\": \"AFN\", \"AI\": \"XCD\", \"VI\": \"USD\", \"IS\": \"ISK\", \"IR\": \"IRR\", \"AM\": \"AMD\", \"AL\": \"ALL\", \"AO\": \"AOA\", \"AQ\": \"\", \"AS\": \"USD\", \"AR\": \"ARS\", \"AU\": \"AUD\", \"AT\": \"EUR\", \"AW\": \"AWG\", \"IN\": \"INR\", \"AX\": \"EUR\", \"AZ\": \"AZN\", \"IE\": \"EUR\", \"ID\": \"IDR\", \"UA\": \"UAH\", \"QA\": \"QAR\", \"MZ\": \"MZN\"}",
            object : TypeToken<HashMap<String?, String?>?>() {}.type
        )

        return retMap.get(country_code).toString()
    }/*gets a country's currency code from its country code*/

    fun touch_vibrate(context: Context?){
        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(Constants().vib_time, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(vib_time)
        }
    }/*Vibrates phone when called*/

    fun construct_elapsed_time(time: Long): String{
        val a_year = 1000L*60L*60L*24L*365L
        val a_month = 1000L*60L*60L*24L*30L
        val a_week = 1000L*60L*60L*24L*7L
        val a_day = 1000L*60L*60L*24L
        val an_hour = 1000L*60L*60L
        val a_minute = 1000L*60L
        val a_second = 1000L

        Log.d("view job", "appication time: "+time+ " a year in mills: "+a_year)
        if(time>=a_year){
            //time is greater than a year, we will parse the time in years
            val time_in_years = (time.toDouble()/a_year.toDouble()).toInt()
            var t = " yrs"
            if(time_in_years==1) t = " yr"
            return time_in_years.toString()+t
        }
        else if(time>=a_month){
            //time is greater than a month, we will parse the time in months
            val time_in_months = (time.toDouble()/a_month.toDouble()).toInt()
            var t = " months"
            if(time_in_months==1) t = " month"
            return time_in_months.toString()+t
        }
        else if(time>=a_week){
            //time is greater than a week, we will parse the time in week
            val time_in_weeks = (time.toDouble()/a_week.toDouble()).toInt()
            var t = " wks"
            if(time_in_weeks==1) t = " wk"
            return time_in_weeks.toString()+t
        }
        else if(time>=a_day){
            //time is greater than a day, we will parse the time in day
            val time_in_days = (time.toDouble()/a_day.toDouble()).toInt()
            var t = " days"
            if(time_in_days==1) t = " day"
            return time_in_days.toString()+t
        }
        else if(time>=an_hour){
            //time is greater than an hour, we will parse the time in hours
            val time_in_hours = (time.toDouble()/an_hour.toDouble()).toInt()
            var t = " hrs"
            if(time_in_hours==1) t = " hr"
            return time_in_hours.toString()+ t
        }
        else if(time>=a_minute){
            //time is greater than a minute, we will parse the time in minutes
            val time_in_minutes = (time.toDouble()/a_minute.toDouble()).toInt()
            var t = " min"
            if(time_in_minutes==1) t = " min"
            return time_in_minutes.toString()+ t
        }
        else{
            val time_in_seconds = (time.toDouble()/a_second.toDouble()).toInt()
            var t = " sec"
            if(time_in_seconds==1) t = " sec"
            return time_in_seconds.toString()+t
        }

    }


    inner class SharedPreferenceManager(val applicationContext: Context){


        fun set_local_image(id: String, image: String){
            val pref: SharedPreferences = applicationContext.getSharedPreferences(local_image, Context.MODE_PRIVATE)
            pref.edit().putString(id, image).apply()
        }

        fun remove_local_image(id: String){
            val pref: SharedPreferences = applicationContext.getSharedPreferences(local_image, Context.MODE_PRIVATE)
            pref.edit().remove(id).apply()
        }

        fun get_local_image(id: String): String{
            val pref: SharedPreferences = applicationContext.getSharedPreferences(local_image, Context.MODE_PRIVATE)
            return pref.getString(id,"")!!
        }

        fun clear_local_images(){
            applicationContext.getSharedPreferences(local_image, Context.MODE_PRIVATE).edit().clear().apply()
        }
    }


    var continue_loading = false
    var alpha_half = 0.6f
    var alpha_full = 1f
    var duration = 700L
    private fun start_image_loading_screen(image_view: ImageView){
        val valueAnimator = ValueAnimator.ofFloat(alpha_full, alpha_half)
        val listener = ValueAnimator.AnimatorUpdateListener{
            val value = it.animatedValue as Float
            if(continue_loading){
                image_view.alpha = value
                if(value==alpha_half){
                    start_backward_image_loading_screen(image_view)
                }
            }else{
                valueAnimator.removeAllUpdateListeners()
                image_view.alpha = alpha_full
            }

        }
        valueAnimator.addUpdateListener(listener)
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.duration = duration
        valueAnimator.start()

    }

    private fun start_backward_image_loading_screen(image_view: ImageView){
        val valueAnimator = ValueAnimator.ofFloat(alpha_half, alpha_full)

        val listener = ValueAnimator.AnimatorUpdateListener{
            val value = it.animatedValue as Float
            if(continue_loading){
                image_view.alpha = value
                if(value==alpha_full){
                    start_image_loading_screen(image_view)
                }
            }else{
                valueAnimator.removeAllUpdateListeners()
                image_view.alpha = alpha_full
            }
        }
        valueAnimator.addUpdateListener(listener)
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.duration = duration
        valueAnimator.start()
    }

    fun stop_image_loader(){
        continue_loading = false
    }

    fun start_image_loader(image_view: ImageView){
        continue_loading = true
        start_image_loading_screen(image_view)
    }/*This function and the three above handle animating a passed Imageview from a fragment as an
    image is loading from the firestore. It simply uses a value animator to mess with the alpha value of an image background while the image
     is loaded, creating a loading effect. Someone's probably made a library that does all this in like one line of code, so this may need revisiting...*/


    fun load_round_job_image(storageReference: StorageReference, user_image: ImageView, context: Context){
        if(!SharedPreferenceManager(context).get_local_image(storageReference.path).equals("")){
            val final = decodeImage(SharedPreferenceManager(context).get_local_image(storageReference.path))
            val image_circle = getCroppedBitmap(final)
            user_image.setImageBitmap(getResizedBitmap(image_circle,200))
        }else{
            val ONE_MEGABYTE: Long = 1024 * 1024
            start_image_loader(user_image)
            storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                stop_image_loader()
                val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
                val final = getCroppedBitmap(bmp)
                user_image.setImageBitmap(getResizedBitmap(final,200))
                SharedPreferenceManager((context)).set_local_image(storageReference.path, encodeImage(bmp,false)!!)

            }.addOnFailureListener {
                stop_image_loader()
            }
        }
    }

    fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap? {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    fun getCroppedBitmap(srcBitmap: Bitmap): Bitmap {
        // Calculate the circular bitmap width with border

        // Calculate the circular bitmap width with border
        val squareBitmapWidth: Int = Math.min(srcBitmap.getWidth(), srcBitmap.getHeight())
        // Initialize a new instance of Bitmap
        // Initialize a new instance of Bitmap
        val dstBitmap = Bitmap.createBitmap(
            squareBitmapWidth,  // Width
            squareBitmapWidth,  // Height
            Bitmap.Config.ARGB_8888 // Config
        )
        val canvas = Canvas(dstBitmap)
        // Initialize a new Paint instance
        // Initialize a new Paint instance
        val paint = Paint()
        paint.isAntiAlias = true
        val rect = Rect(0, 0, squareBitmapWidth, squareBitmapWidth)
        val rectF = RectF(rect)
        canvas.drawOval(rectF, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        // Calculate the left and top of copied bitmap
        // Calculate the left and top of copied bitmap
        val left: Float = ((squareBitmapWidth - srcBitmap.getWidth()) / 2).toFloat()
        val top: Float = ((squareBitmapWidth - srcBitmap.getHeight()) / 2).toFloat()
        canvas.drawBitmap(srcBitmap, left, top, paint)
        // Free the native object associated with this bitmap.
        // Free the native object associated with this bitmap.
        srcBitmap.recycle()
        // Return the circular bitmap
        // Return the circular bitmap
        return dstBitmap
    }/*Crops an bitmap into a circle shape*/

    fun encodeImage(bm: Bitmap, as_png: Boolean): String? {
        val baos = ByteArrayOutputStream()
        if(as_png){
            bm.compress(Bitmap.CompressFormat.PNG, 100, baos)
        } else{
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        }
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    fun decodeImage(base64String: String): Bitmap{
        val decodedByte: ByteArray = Base64.decode(base64String, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decodedByte, 0,  decodedByte.size)
        bitmap.setHasAlpha(true)

        return bitmap
    }

}