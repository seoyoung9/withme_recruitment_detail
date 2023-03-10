package com.oneg.maplocation

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.animation.Transformation
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.oneg.maplocation.databinding.ActivityMapsBinding
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.IOException
import java.util.*
import java.util.jar.Manifest

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION )
    val PERM_FLAG = 99
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        button.setOnClickListener {
            startActivity(Intent(this@MapsActivity,NaviActivity::class.java))
        }

        if(isPermitted()){
            startProcess()
        }else   {
            ActivityCompat.requestPermissions(this, permissions,PERM_FLAG)
        }
    }
    fun isPermitted():Boolean{
        for (perm in permissions){
            if(ContextCompat.checkSelfPermission(this, perm) != PERMISSION_GRANTED){
                return false
            }
        }
        return true
    }

    fun startProcess() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        //android??? ???????????? ?????????????????? ???????????? ????????????.
        mapFragment.getMapAsync(this)
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setUpdateLocationListener()

    }

    //???????????? ???????????? ??????
    lateinit var fusedLocationClient:FusedLocationProviderClient
    lateinit var locationCallback:LocationCallback

    @SuppressLint("MissingPermission")
    fun setUpdateLocationListener(){
        //??????????????? ????????????
        val locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
        }
        locationCallback = object : LocationCallback (){
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.let{
                    for((i, location)in it.locations.withIndex()){
                        Log.d("????????????", "$i ${location.latitude}, ${location.longitude}")
                        setLastLocation(location)

                        var mGeocoder = Geocoder(applicationContext, Locale.KOREAN)
                        var mResultList: List<Address>? = null
                        var currentLocation : String = ""
                        val textView = findViewById<TextView>(R.id.textView)
                        try {
                            mResultList = mGeocoder.getFromLocation(
                                location.latitude!!, location.longitude!!,1
                            )
                        } catch (e: IOException){
                            e.printStackTrace()
                        }
                        if (mResultList != null){
                            Log.d("checkcurrentlocation", mResultList[0].getAddressLine(0))
                            currentLocation = mResultList[0].getAddressLine(0)
                            currentLocation = currentLocation.substring(9,20)
                        }
                        textView.text = currentLocation
                    }
                }
            }
        }
        //???????????? ???????????? ??????()
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    fun setLastLocation(location: Location){
        //????????????????????????
        var bitmapDrawable:BitmapDrawable
        if(Build.VERSION.SDK_INT >=  Build.VERSION_CODES.LOLLIPOP){
            bitmapDrawable = getDrawable(R.drawable.marker) as BitmapDrawable
        } else {
            bitmapDrawable = resources.getDrawable(R.drawable.marker) as BitmapDrawable
        }
        //?????? ????????????
        val scaledBitmap = Bitmap.createScaledBitmap(bitmapDrawable.bitmap, 80, 100, false)
        val descriptor = BitmapDescriptorFactory.fromBitmap(scaledBitmap)
        val myLocation = LatLng(location.latitude, location.longitude)
        //??????
        val marker = MarkerOptions()
            .position(myLocation)
            .title("??? ????????? ???????????? ??????")
            .icon(descriptor)
        //???????????????
        val cameraOption = CameraPosition.Builder()
            .target(myLocation)
            .zoom(15f)
            .build()
        val camera = CameraUpdateFactory.newCameraPosition(cameraOption)

        mMap.addMarker(marker)
        mMap.moveCamera(camera)
    }
    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            PERM_FLAG ->{
                var check = true
                for(grant in grantResults){
                    if(grant != PERMISSION_GRANTED){
                        check = false
                        break
                    }
                }
                if(check){
                    startProcess()
                }else{
                    Toast.makeText(this, "????????? ?????????????????? ?????? ????????? ??? ????????????",Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }
}