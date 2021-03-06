package com.example.u5_proyecto_cd_artes_v2

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.u5_proyecto_cd_artes_v2.databinding.ActivityMapsBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    val posicion = ArrayList<Data>()
    val lugares = ArrayList<String>()
    var baseRemota = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //PERMISOS
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
        }

        //RECUPERACION DE COORDENAS FIREBASE ------------------------------------------
        FirebaseFirestore.getInstance()
            .collection("cdArtes")
            .addSnapshotListener { query, error ->
                var res=""
                lugares.clear() // si no se pone te estara duplicando datos
                posicion.clear()

                if(error!=null){
                    //si hubo error
                    AlertDialog.Builder(this)
                        .setMessage(error.message)
                        .show()
                    return@addSnapshotListener //pasa salirme
                }

                for(documento in query!!){//ciclo que recoje los datos de la colleccion
                    var cadena = "Nombre: ${documento.getString("nombre")}\n" +
                            " punto 1: ${documento.getGeoPoint("punto1")!! }\n"+
                            " punto 2: ${documento.getGeoPoint("punto2")!! }\n\n"
                    lugares.add(cadena)

                    var data=Data(this)
                    //var data=Data(this)
                    data.nombre = documento.getString("nombre").toString()
                    data.posicion1=documento.getGeoPoint("punto1")!!
                    data.posicion2=documento.getGeoPoint("punto2")!!
                    posicion.add(data)
                    res+=data.toString()+"\n\n"
                }

                Toast.makeText(this,"DATOS-GEOPOINTS \n\n"+res, Toast.LENGTH_LONG).show()
                AlertDialog.Builder(this)
                    .setMessage("       - INFO DE FIREBASE - \n\n"+lugares)
                    .setPositiveButton("OK"){p,q-> }
                    .show()
            } //fin evento snapshoot


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        crearMarcadores()

        //ANIMACION DE CAMARA
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(LatLng(21.51108496011311, -104.90307329600743),17f),3000,null //Cuanto zoom y cuando durar??
        )

        //CLIC EN MAPA
        mMap.setOnMapClickListener {
            miUbicacion()
        }
    }


    private  fun miUbicacion(){
        LocationServices.getFusedLocationProviderClient(this)
            .lastLocation.addOnSuccessListener {
                var geoPos = GeoPoint(it.latitude,it.longitude)
                //  Toast.makeText(this,""+it.latitude+""+""+it.longitude,Toast.LENGTH_SHORT).show()
                var bandera=false
                for(item in posicion){
                    AlertDialog.Builder(this)
                        .setMessage("(M-01) Sus Coordenadas Actuales: "+it.latitude+","+it.longitude+"\n\n")
                        .setPositiveButton("OK"){p,q-> }
                        .show()

                    //SI Compara
                    if(item.estoyEn(geoPos)){
                        AlertDialog.Builder(this)
                            .setMessage("Usted esta en "+item.nombre)
                            .setPositiveButton("OK"){p,q-> }
                            .show()
                        bandera=true
                    }
                }

                if(bandera==false){
                    AlertDialog.Builder(this)
                        .setMessage("No se encontro ninguna Ubicacion Cercana")
                        .setPositiveButton("OK"){p,q-> }
                        .show()
                }
            }.addOnFailureListener {
                AlertDialog.Builder(this)
                    .setMessage("ERROR DE UBICACION")
                    .setPositiveButton("OK"){p,q-> }
                    .show()
            }
    }

    //CREAR LOS MARCADORES
    private fun crearMarcadores() {

        val jardinDignidad=MarkerOptions().position(LatLng(21.512381454724817, -104.90404305341924)).title("Jard??n de la Dignidad")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            .snippet("Parque")
            .flat(true).rotation(0f)


        val cdArtes=MarkerOptions().position(LatLng(21.51103975981268, -104.90306478583207)).title("Ciudad de las Artes")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            .snippet("Parque")

        val afterTacos=MarkerOptions().position(LatLng(21.51165095509239, -104.90403546698616)).title("Tacos El After")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
            .snippet("Alimentos")

        val skatePark=MarkerOptions().position(LatLng(21.51197562204376, -104.9027154276223)).title("Skate Park")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            .snippet("Zona Recreativa")

        val estatuas=MarkerOptions().position(LatLng(21.510740472140885, -104.90317061360524)).title("Estatuas Conmemorativas")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            .snippet("Zona Conmemorativa")

        val escuelaMusica=MarkerOptions().position(LatLng(21.511986208992873, -104.90210851297405)).title("Escuela de Musica")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            .snippet("Istituci??n")

        val zonaTacos =MarkerOptions().position(LatLng(    21.51091873667444, -104.9037786567254)).title("Zona de Tacos")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
            .snippet("Alimentos")

        val dolores =MarkerOptions().position(LatLng(    21.510396830580504, -104.90331087782104)).title("Dolores")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
            .snippet("Alimentos")

        val laSanta =MarkerOptions().position(LatLng(    21.510330049125745, -104.90314384918146)).title("La Santa")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
            .snippet("Bar")

        val cremeria =MarkerOptions().position(LatLng(    21.51248992670049, -104.90184382014519)).title("Cremeria Yoli")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            .snippet("Negocio")

        mMap.setOnMapClickListener {
            Toast.makeText(this,"Clic en el Mapa", Toast.LENGTH_SHORT).show()
        }

        mMap.addMarker(cdArtes)
        mMap.addMarker(jardinDignidad)
        mMap.addMarker(afterTacos)
        mMap.addMarker(skatePark)
        mMap.addMarker(escuelaMusica)
        mMap.addMarker(estatuas)
        mMap.addMarker(dolores)
        mMap.addMarker(zonaTacos)
        mMap.addMarker(laSanta)
        mMap.addMarker(cremeria)
    }
}