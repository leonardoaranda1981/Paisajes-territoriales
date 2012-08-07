package anon.paisajes.territoriales.org;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import anon.paisajes.territoriales.org.mRenderer.Movements;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.io.Writer;

public class PaisajesTerritorialesActivity extends Activity implements SensorEventListener {
	
	private LocationManager locManager;
	private LocationListener locListener;
	
	private TextView lblAltitud; 
	private TextView lblLatitud; 
	private TextView lblLongitud;
	private TextView lblPrecision;
	private TextView lblEstado;
	private	TextView networkStatus;
	
	private static final int MENU_SAVE= Menu.FIRST;
    private static final int MENU_SEND_3G = Menu.FIRST + 1;
    private static final int MENU_SEND_WIFI = Menu.FIRST + 2;
    private static final int STOP_TRACK = Menu.FIRST + 3;
    	
	private GPXformatter gpxF;
	
	ConnectivityManager connectivity;
	ConnectivityReceiver connection;
	NetworkInfo wifiInfo, mobileInfo;
	DrawView drawView;
	
	//Gráficos
	GLView glView;
	SensorManager sm;
	Location mlocation;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Gráficos
        glView = new GLView(this);
        setContentView(glView);
        
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, acc,sm.SENSOR_DELAY_NORMAL);
        
        lblAltitud = (TextView)findViewById(R.id.lblAltitud);
        lblLatitud = (TextView)findViewById(R.id.lblLatitud);
        lblLongitud = (TextView)findViewById(R.id.lblLongitud);
        lblPrecision = (TextView)findViewById(R.id.lblPrecision);
        lblEstado = (TextView)findViewById(R.id.lblEstado);
        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
       
        //drawView = new DrawView(this);
        //setContentView(drawView);
        //drawView.requestFocus();
            
    	comenzarLocalizacion();
    }
    
    @Override
	  public void onResume(){
		  super.onResume();
		  connection.bind(getApplicationContext());
		  glView.onResume();
	  }
    
	  @Override
	  public void onPause(){
		  super.onPause();
		  connection.unbind(getApplicationContext());
		  sm.unregisterListener(this);
		  glView.onPause();
	  }
	  @Override
	    public boolean onCreateOptionsMenu(final Menu pMenu) {
	   
	            MenuItem saveButton = pMenu.add(0, MENU_SAVE, Menu.NONE, R.string.btnSave);
	            MenuItem saveSendButton = pMenu.add(0,  MENU_SEND_WIFI, Menu.NONE, R.string.btnSend);
	            MenuItem SendButton = pMenu.add(0, MENU_SEND_3G, Menu.NONE, R.string.btnBroadcast);
	            MenuItem stopButton = pMenu.add(0, STOP_TRACK, Menu.NONE, R.string.btnStop);

	            return true;
	 }
	  public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
	        switch (item.getItemId()) {
	        case MENU_SAVE:
	        	crearGPX();
	            return true;

	        case MENU_SEND_3G:
	        		//refresh();
	                return true;
	        }
	        return false;
	    }
    private void comenzarLocalizacion()
    {
    	//Obtenemos una referencia al LocationManager
    	locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    	
    	//Obtenemos la última posición conocida
    	//Location loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    	connection = new ConnectivityReceiver(getApplicationContext());
    	//Mostramos la última posición conocida
    	//mostrarPosicion(loc);
    	
    	networkStatus = (TextView) findViewById(R.id.lblNetworkStatus);
    	//Nos registramos para recibir actualizaciones de la posición
    	locListener = new LocationListener() {
	    	public void onLocationChanged(Location location) {
	    		//mostrarPosicion(location);
	    	//	RecordedGeoPoint g1 = new RecordedGeoPoint(location.getLatitude(), location.getLongitude(), location.getAltitude());
	    		Log.d("NEW", "New point!!!");
	    		glView.setPoint(location);
	    		//localizaciones.add(g1);
	    	}
	    	public void onProviderDisabled(String provider){
	    		//lblEstado.setText("Provider OFF");
	    	}
	    	public void onProviderEnabled(String provider){
	    		//lblEstado.setText("Provider ON ");
	    	}
	    	public void onStatusChanged(String provider, int status, Bundle extras){
	    		Log.i("", "Provider Status: " + status);
	    		//lblEstado.setText("Provider Status: " + status);
	    	}
    	};
    	
    	locManager.requestLocationUpdates(
    			LocationManager.GPS_PROVIDER, 1000, 0, locListener);
    }
    private void mostrarPosicion(Location loc) {
    	if(loc != null)
    	{
    		
    		lblLatitud.setText("Latitud: " + String.valueOf(loc.getLatitude()));
    		lblLongitud.setText("Longitud: " + String.valueOf(loc.getLongitude()));
    		lblAltitud.setText("Altitud: "+String.valueOf(loc.getAltitude())); 
    		lblPrecision.setText("Precision: " + String.valueOf(loc.getAccuracy()));
    		Log.i("", String.valueOf(loc.getLatitude() + " - " + String.valueOf(loc.getLongitude())));
    	}
    	else
    	{
    		lblLatitud.setText("Latitud: (sin_datos)");
    		lblLongitud.setText("Longitud: (sin_datos)");
    		lblPrecision.setText("Precision: (sin_datos)");
    	}
    }
    public void crearGPX(){
    	try{
	    	if(drawView.points.size()>1){
	    		 String outFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "track.xml";
	    		 String f = gpxF.createAlt(drawView.points, 3);
	    		 Writer w = new FileWriter(outFile);
	    		 w.write(f);
	    		 w.close();
	    	}
    	}catch(Exception e){
    		Log.i("Exception", "Exception GPX"+e);
    		
    	}
    }
    
    
    public void postData(Location []loc){
    	
    	
    	for(int i = 0; i<loc.length; i++){
	    	ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
	
	    	nameValuePairs.add(new BasicNameValuePair("nombreRuta","nombreRuta"));
	        nameValuePairs.add(new BasicNameValuePair("gps_latitud",String.valueOf(loc[0].getLatitude())));
	        nameValuePairs.add(new BasicNameValuePair("gps_longitud",String.valueOf(loc[0].getLongitude())));
	        nameValuePairs.add(new BasicNameValuePair("gps_altirud",String.valueOf(loc[0].getAltitude())));
	        nameValuePairs.add(new BasicNameValuePair("gps_timeStamp",String.valueOf(loc[0].getTime())));
	    	
	    	//nameValuePairs.add(new BasicNameValuePair("gps_latitud","50.0"));
	        //nameValuePairs.add(new BasicNameValuePair("gps_longitud","100.0"));
	
	        //http post
	        try{
	            HttpClient httpclient = new DefaultHttpClient();
	            HttpPost httppost = new HttpPost("http://imagen-movimiento.org/update.php");
	            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	            HttpResponse response = httpclient.execute(httppost);
	            HttpEntity entity = response.getEntity();
	            InputStream is = entity.getContent();
	            Log.i("Connection made", response.getStatusLine().toString());
	        }
	
	        catch(Exception e)
	        {
	            Log.e("log_tag", "Error in http connection "+e.toString());
	        }           
	    }
    }

	public void onAccuracyChanged(Sensor arg0, int arg1) {}

	public void onSensorChanged(SensorEvent event) {
		
		Float x = event.values[0];
		Float y = event.values[1];
		Float z = event.values[2];
		Movements.setValues(x,y,z);
		//Log.d("acc",x.toString()+"      "+y.toString()+"       "+z.toString());
	}
}

class GLView extends GLSurfaceView{

	mRenderer renderer = new mRenderer();
	DisplayMetrics dm = new DisplayMetrics();
	int height, width;
	
	public GLView(Context context) {
		super(context);
		((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)) .getDefaultDisplay().getMetrics(dm);
        height = dm.heightPixels;
        width = dm.widthPixels;        
		setRenderer(renderer);
	}
	
	public void setPoint(Location point){
		renderer.setMeasures(height, width);
		renderer.nuevoPunto(point);
	}
}