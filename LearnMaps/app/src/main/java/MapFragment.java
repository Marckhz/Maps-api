import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.elrobotista.learnmaps.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.zzc;
//import com.google.android.gms.common.api.zzl;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


/**
 * Created by elrobotista on 3/01/17.
 */



public class MapFragment extends SupportMapFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener

    /*
    ConnectionCallbacks y OnConnectionFailedListener estan diseñados para monitorear el estado de
    GoogleApiClient, el cual es usado en esta aplicacion para obtener la ubicacion actual del usuario.

    OnInfoWindowClickLIstener y OnMapClickListener son ejecutados cuando el usiario toca o mantiene presionado
    una porcion del mapa.

    OnMarkerClickListener is llamado cuando el usuario da click en un marcador sobre el mapa, el cual usualmente tambien
    despliega una ventana de informacion acerca de ese marcador.
    */


    {




    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        setHasOptionsMenu(true);
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())

                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        connected = false;

        initListeners();
        new Thread(new SocketThread()).start();
    }

    private void initListeners() {
        getMap().setOnMarkerClickListener(this);
        getMap().setOnMapLongClickListener(this);
        getMap().setOnMapClickListener(this);
        getMap().setOnInfoWindowClickListener(this);
    }


    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private Socket socket;
    private PrintStream out;
    private Boolean connected;


        public class SocketThread implements Runnable {
            @Override
            public void run() {

               try{

                   socket = new Socket("192.168.0.9", 5001);
                   out = new PrintStream(socket.getOutputStream(),true);

                   connected = true;


               }
               catch (java.io.IOException e){
                   e.printStackTrace();
               }


            }
        }



        private final int[] MAP_TYPES = {
            GoogleMap.MAP_TYPE_SATELLITE, // Tipo vista satelital sin nombre de calles,etc.
            GoogleMap.MAP_TYPE_NORMAL,    // Mapa generico con nombre de calles y etiquetas.
            GoogleMap.MAP_TYPE_HYBRID,    // Combina satelital y modo normal, desplegando imagenes satelitates y area con etiquetas.
            GoogleMap.MAP_TYPE_TERRAIN,   // Similar al generico, con texturas, elevaciones en el ambiente.
            GoogleMap.MAP_TYPE_NONE

    };

    private int curMapTypeIndex = 1;

        /* mGoogleApiClient y mCurrentLocation, son usados para obtener la ubicacion del usuario
           para inicializar la camara.

           Map_TYPES y curMapTypeIndex son usados para cambiar en diferentes tipos de mapas. Cada mapa tiene un proposito.

         */





    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onMapClick(LatLng latLng) {

        MarkerOptions options = new MarkerOptions().position( latLng);
        options.title( getAddressFromLatLng(latLng));

        options.icon(BitmapDescriptorFactory.defaultMarker());
        getMap().addMarker(options);



    }


    @Override
    public void onMapLongClick(LatLng latLng) {
        MarkerOptions options = new MarkerOptions().position(latLng);
        options.title( getAddressFromLatLng(latLng));

        options.icon(BitmapDescriptorFactory.fromBitmap(
                BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher)
        ));

    }



    private String getAddressFromLatLng (LatLng latLng) {
        Geocoder geocoder = new Geocoder( getActivity());

        String address = " ";
        try{
            address = geocoder
                    .getFromLocation(latLng.latitude, latLng.longitude, 1)
                    .get( 0).getAddressLine(0);

        }catch (IOException e){

        }

        return address;
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return true;
    }
    @Override
    public void onStart(){
        super.onStart();
        mGoogleApiClient.connect();
    }
    @Override
    public void onStop(){
        super.onStop();
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected() ) {
            mGoogleApiClient.disconnect();
        }
    }
    @Override
    public void onConnected(Bundle bundle) {
        mCurrentLocation = LocationServices
                .FusedLocationApi
                .getLastLocation(mGoogleApiClient);
        initCamera (mCurrentLocation);

    }

    private void initCamera (Location location) {
        CameraPosition position =CameraPosition.builder()
                .target(new LatLng(location.getLatitude() ,
                        location.getLongitude()  ) )
                .zoom(16f)
                .bearing(0.0f)
                .tilt(0.0f)
                .build();

        getMap().animateCamera(CameraUpdateFactory
                .newCameraPosition(position), null);

        getMap().setMapType(MAP_TYPES[curMapTypeIndex]);
        getMap().setTrafficEnabled(true);
        getMap().setMyLocationEnabled(true);
        getMap().getUiSettings().setZoomControlsEnabled(true);



    }






}










