package com.example.screenshotboundremoval

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


//TODO: welcome screen, image selection screen pimp, Logo

const val N_DISMISSED_IMAGES = "com.example.screenshotboundremoval.N_DISMISSED_IMAGES"
const val DISMISSED_ALL_IMAGES = "com.example.screenshotboundremoval.DISMISSED_ALL_IMAGES"
const val ATTEMPTED_FOR_MULTIPLE_IMAGES = "com.example.screenshotboundremoval.ATTEMPTED_FOR_MULTIPLE_IMAGES"


class MainActivity : Activity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // procedure result display if existent
        if (intent.getBooleanExtra(DISMISSED_ALL_IMAGES, false))
            when(intent.getBooleanExtra(ATTEMPTED_FOR_MULTIPLE_IMAGES, false)){
                true -> displayMessage("Couldn't find cropping bounds for \n any of the selected images", this)
                false -> displayMessage("Couldn't find cropping bounds for selected image", this)
            }

        intent.getIntExtra(SAVED_CROPS, -1).let{
            when(it){
                0 -> displayMessage("Didn't save anything", this)
                1 -> displayMessage("Saved 1 cropped image", this)
                in 1..Int.MAX_VALUE -> displayMessage("Saved $this cropped images", this)
            }
        }

        image_selection_button.setOnClickListener {
            requestActivityPermissions()

            if (nRequiredPermissions == 0)
                pickImageFromGallery()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    override fun onResume() {
        super.onResume()

        // Get updates from the accelerometer and magnetometer at a constant rate.
        // To make batch operations more efficient and reduce power consumption,
        // provide support for delaying updates to the application.
        //
        // In this example, the sensor reporting delay is small enough such that
        // the application receives an update before the system checks the sensor
        // readings again.
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onPause() {
        super.onPause()

        // Don't receive any more updates from either sensor.
        sensorManager.unregisterListener(this)
    }

    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }
        updateOrientationAngles()
        println("YARP")
        orientationAngles.forEach { print("$it\t") }
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    fun updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )

        // "mRotationMatrix" now has up-to-date information.
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
    }

    // -------------------------------------------------------------------
    companion object{
        private const val IMAGE_PICK_CODE = 69
        private const val READ_PERMISSION_CODE = 420
        private const val WRITE_PERMISSION_CODE = 47
    }

    private val permission2Code: Map<String, Int> = mapOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE to WRITE_PERMISSION_CODE,
        Manifest.permission.READ_EXTERNAL_STORAGE to READ_PERMISSION_CODE
    )

    private var nRequiredPermissions: Int = 0

    // ----------------
    // PERMISSION QUERY
    // ----------------
    private fun checkPermission(permission: String){
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED){
            requestPermissions(arrayOf(permission), permission2Code[permission]!!)
            nRequiredPermissions ++
        }
    }

    private fun requestActivityPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            READ_PERMISSION_CODE -> permissionRequestResultHandling(grantResults, "Read")
            WRITE_PERMISSION_CODE -> permissionRequestResultHandling(grantResults, "Write")
        }
    }

    private fun permissionRequestResultHandling(grantResults: IntArray, requestDescription: String){
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED)
            Toast.makeText(this, "$requestDescription permission denied", Toast.LENGTH_SHORT).show()
        else
            nRequiredPermissions --
    }

    // ----------------
    // IMAGE SELECTION
    // ----------------
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    // ----------------
    // SAVING
    // ----------------
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE){

            var nDismissedImages = 0
            val itemCount: Int = data?.clipData?.itemCount!!
            for (i in 0 until itemCount) {
                // retrieve uri and resolve into bitmap
                val imageUri: Uri = data.clipData?.getItemAt(i)?.uri!!
                val image: Bitmap? = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))

                // crop image
                val croppedImage: Bitmap? = Cropper(image!!).getCroppedImage()
                if (croppedImage != null)
                    ImageCash.cash[imageUri] = croppedImage
                else
                    nDismissedImages += 1
            }
            if (nDismissedImages != itemCount) startExaminationActivity(nDismissedImages) else
                restartMainActivity(itemCount != 1)
        }
    }

    // --------------
    // FOLLOW-UP ACTIVITIES
    // --------------
    private fun startExaminationActivity(dismissedCrops: Int){
        startActivity(Intent(this, ProcedureActivity::class.java).
            putExtra(N_DISMISSED_IMAGES, dismissedCrops)
        )
    }

    private fun restartMainActivity(multipleImages: Boolean){
        startActivity(Intent(this, MainActivity::class.java)
            .putExtra(DISMISSED_ALL_IMAGES, true)
            .putExtra(ATTEMPTED_FOR_MULTIPLE_IMAGES, multipleImages)
        )
    }
}