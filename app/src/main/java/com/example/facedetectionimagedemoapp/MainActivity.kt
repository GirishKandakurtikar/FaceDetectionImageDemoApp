package com.example.facedetectionimagedemoapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.facedetectionimagedemoapp.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceDetectorOptions.LandmarkMode

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding:ActivityMainBinding
    private lateinit var detector:FaceDetector

    private companion object{
        private const val SCALING_FACTOR = 10
        private const val TAG = "FACE_DETECT_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val realTimeFdo = FaceDetectorOptions.Builder()
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .build()

        //Init FaceDetector
        detector = FaceDetection.getClient(realTimeFdo)

        //1) Image from Drawable
        val bitmap1 = BitmapFactory.decodeResource(resources,R.drawable.person_image)


        //2) Image from ImageView
        /*val bitmapDrawable = viewBinding.originalIv.drawable as BitmapDrawable
        val bitmap2 = bitmapDrawable.bitmap*/

        //3) Image from Uri
        /*val imageUri:Uri? = null
        try {
            val bitmap3 = MediaStore.Images.Media.getBitmap(contentResolver,imageUri)
        }catch (e:Exception){
            Log.e(TAG,"onCreate",e)
        }*/

        //handle click. start detecting face
        viewBinding.detectFaceBtn.setOnClickListener{
            analyzePhoto(bitmap1)
        }
    }

    private fun analyzePhoto(bitmap: Bitmap){
        Log.d(TAG,"analyzePhoto : ")

        //Make image smaller to do processing faster
        val smallerBitmap = Bitmap.createScaledBitmap(
            bitmap,
            bitmap.width/ SCALING_FACTOR,
            bitmap.height/ SCALING_FACTOR,
            false
        )

        //Input image for analyzing
        val inputImage = InputImage.fromBitmap(smallerBitmap,0)
        //Start detecting
        detector.process(inputImage)
            .addOnSuccessListener {faces->
                Log.d(TAG,"analyzePhoto : Successfully detected the face")

                for(face in faces){
                    val rect = face.boundingBox
                    rect.set(rect.left* SCALING_FACTOR,
                            rect.top* (SCALING_FACTOR-1),
                            rect.right*(SCALING_FACTOR),
                        rect.bottom* SCALING_FACTOR+90
                    )
                }
            Log.d(TAG,"Analyze no. of faces ${faces.size}")
                cropDetectedFace(bitmap,faces)
                Toast.makeText(this,"Face Detected ..",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{e->
                Log.e(TAG, "analyzePhoto : ",e)

                Toast.makeText(this,"Failed due to ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun cropDetectedFace(bitmap: Bitmap,faces: List<Face>){
            Log.d(TAG,"cropDetectedFace: ")

        //Face was detected. Get cropped image as bitmap
        val rect = faces[0].boundingBox //there might be multiple images
        val x = Math.max(rect.left,0)
        val y = Math.max(rect.top,0)
        val width = rect.width()
        val height = rect.height()

        //cropped bitmap
        val croppedBitmap = Bitmap.createBitmap(
            bitmap,
            x,
            y,
            if(x+width > bitmap.width) bitmap.width - x else width,
            if(y+height > bitmap.height) bitmap.height - y else height
        )
        //set cropped bitmap to imageview
        viewBinding.croppedIv.setImageBitmap(croppedBitmap)
    }

}