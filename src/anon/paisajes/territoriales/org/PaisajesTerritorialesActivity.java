package anon.paisajes.territoriales.org;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import java.util.List;
import java.io.Writer;

public class PaisajesTerritorialesActivity extends Activity implements SensorEventListener {
	
	private LocationManager locManager;
	private LocationListener locListener;
	
	
    private static final int STOP_TRACK = Menu.FIRST;
    	
	private GPXformatter gpxF;
	private boolean conectividad = false; 
	ConnectivityManager connectivity;
	ConnectivityReceiver connection;
	NetworkInfo wifiInfo, mobileInfo;
	DrawView drawView;
	//Gráficos
	GLView glView;
	SensorManager sm;
	Location mlocation;
	int duration;
	String nombreRuta = "";
	int modo; 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connection = new ConnectivityReceiver(getApplicationContext());
        duration = Toast.LENGTH_LONG;
        //Gráficos
        glView = new GLView(this);
        setContentView(glView);
        
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, acc,sm.SENSOR_DELAY_NORMAL);
        
       
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
       
        pedirNombreRuta();
    	
    	
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
	  public void onStop(){
		  super.onStop(); 
		  glView.onPause();
	  }
	  @Override
	    public boolean onCreateOptionsMenu(final Menu pMenu) {
	  
	            MenuItem stopButton = pMenu.add(0, STOP_TRACK, Menu.NONE, R.string.btnStop);

	            return true;
	 }
	  
	  public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
	        switch (item.getItemId()) {
	        
	        case STOP_TRACK:
        		if (modo == 0){
        			alertaGuardarEnviar();
        			
        			
        		}else if (modo == 1){
        			alertaGuardar();
        			///guardar (si o no)? si = creaGpx, no = descartaTrayecto; 
        	
        		}
                return true;
	        }
	        return false;
	    }
	  
	private void pedirNombreRuta(){
		 final AlertDialog.Builder alertNombre = new AlertDialog.Builder(this);
         final EditText input = new EditText(this);
         alertNombre.setTitle("nombre de la ruta");
         alertNombre.setView(input);
         alertNombre.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
             String value = input.getText().toString().trim();
             nombreRuta = value; 
                 Toast.makeText(getApplicationContext(), value,Toast.LENGTH_SHORT).show();
                 dialogoModo();
                 comenzarLocalizacion();
             }
             /*
             @Override
             public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                 if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                     return true; // Pretend we processed it
                 }
                 return false; // Any other keys are still processed as normal
             }*/
             
         });
         alertNombre.show();
	}
	private void alertaGuardar(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("¿Quieres guardar la ruta?")
		       .setCancelable(false)
		       .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   crearGPX(glView.renderer.points);
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                descartaTrayecto();
		           }
		       });
		AlertDialog alert = builder.create();
		
	}
	private void alertaGuardarEnviar(){
		final CharSequence[] items = {"Guardar y enviar?", "Unicamente Guardar", "Descartar"};
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	//builder.setTitle("¿qué");
    	builder.setItems(items, new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int item) {
    	    	if(item == 0){
    	    		locManager.removeUpdates(locListener);
    	    		enviarDatosGuardador();
    	    		crearGPX(glView.renderer.points);
    	    	}
    	    	if(item == 1){
    	    		locManager.removeUpdates(locListener);
    	    		crearGPX(glView.renderer.points);
    	    	}
    	    	if(item == 2){
    	    		locManager.removeUpdates(locListener);
    	    		descartaTrayecto();
    	    	}
    	    }
    	});
    	AlertDialog alert = builder.create();
    	alert.show();
	} 
	private void enviarDatosGuardador(){
		if(connection.hasConnection() == true){
			postData(glView.renderer.points);
			
		}else{
			Toast.makeText(getApplicationContext(), "No fue posible enviar el archivo debido a que no existe conexión con la red", Toast.LENGTH_SHORT).show();
			
		}
	}
	private void descartaTrayecto(){
		
		glView.renderer.points.clear();
		Intent intent = new Intent();
		setResult(RESULT_OK, intent);
		glView.onPause();
       	finish();
	}
	private void dialogoModo(){
		final CharSequence[] items = {"Transmitir en tiempo real", "Guardar y enviar despues"};
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
    	builder.setItems(items, new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int item) {
    	    	if(item == 0){
    	    		//aqui se necesita que antes de empezar a enviar datos cheque si hay conexion a Internet
    	    		Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
    	    		conectividad = connection.hasConnection();
    	    		if(conectividad == false){
    	    			
    	    			Toast.makeText(getApplicationContext(), "es necesario prender el 3G", Toast.LENGTH_SHORT).show();
    	    		}
    	    	}
    	    	if(item == 1){
    	    		
    	    		Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
    	    	}
    	        
    	    }
    	});
    	AlertDialog alert = builder.create();
    	alert.show();
		
	}  
    private void comenzarLocalizacion()
    {
    	/*
    	Location[] l_array = new Location[3];
    	for(int i=0; i<3; i++){
    		l_array[i] = new Location(LocationManager.GPS_PROVIDER);
    		l_array[i].setLatitude(i*(0.01)+(-122.084095));
    		l_array[i].setLongitude(i*(0.01)+37.422006);
    		glView.renderer.nuevoPunto(l_array[i]);
    	}*/
    	//Obtenemos una referencia al LocationManager
    	locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    	
    	//Obtenemos la última posición conocida
    	//Location loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    	//glView.setPoint(loc);
    	connection = new ConnectivityReceiver(getApplicationContext());
    
    	locListener = new LocationListener() {
	    	public void onLocationChanged(Location location) {
	    		//mostrarPosicion(location);
	    	//	RecordedGeoPoint g1 = new RecordedGeoPoint(location.getLatitude(), location.getLongitude(), location.getAltitude());
	    		glView.renderer.nuevoPunto(location);
	    		if (modo == 1 && connection.hasConnection()== true){
	    			
	    			brodcastData(location);
	    		}else if(modo == 1 && connection.hasConnection()== false) {
	    			
	    			Toast.makeText(getApplicationContext(), "necesitas estar conectado a Internet para transmitir lo datos", Toast.LENGTH_SHORT).show();
	    		}
	    		//glView.setPoint(location);
	    		//localizaciones.add(g1);
	    	}
	    	public void onProviderDisabled(String provider){
	    		//lblEstado.setText("Provider OFF");
	    		CharSequence text = "GPS desactivado";
	       	  
	      	   Toast toast = Toast.makeText(getApplicationContext(), text, duration);
	      	   toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
	      	   toast.show();
	    	}
	    	public void onProviderEnabled(String provider){
	    		CharSequence text = "GPS activado";
		       	  
		      	   Toast toast = Toast.makeText(getApplicationContext(), text, duration);
		      	   toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
		      	   toast.show();
	    	}
	    	public void onStatusChanged(String provider, int status, Bundle extras){
	    		Log.i("", "Provider Status: " + status);
	    		//lblEstado.setText("Provider Status: " + status);
	    	}
    	};
    	if(locManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false){

        	CharSequence text = "el GPS necesita estar activado para iniciar a generar el trayecto";
     	  
     	   Toast toast = Toast.makeText(getApplicationContext(), text, duration);
     	   toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
     	   toast.show();
    	}
    	locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 11, locListener);
    }
    
    
    public void crearGPX(List<Location>loc){
    	try{
	    	if(glView.renderer.points.size()>1){
	    		 String outFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+"RutasPT"+File.separator + nombreRuta+".gpx";
	    		 String f = gpxF.createAlt(loc, 3);
	    		 Writer w = new FileWriter(outFile);
	    		 w.write(f);
	    		 w.flush();
	    		 w.close();
	    		 Toast.makeText(getApplicationContext(), "arcvhivo guardado en:"+outFile, Toast.LENGTH_SHORT).show();
	    	}
    	}catch(Exception e){
    		Toast.makeText(getApplicationContext(), "error al guardar archivo", Toast.LENGTH_SHORT).show();
    		Log.i("Exception", "Exception GPX"+e);
    		
    	}
    }
    
    
    public void postData(List<Location>loc){
    	
    	int contador = 0;
    	for(int i = 0; i<loc.size(); i+=0){
	    	ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
	
	    	nameValuePairs.add(new BasicNameValuePair("nombre",nombreRuta));
	        nameValuePairs.add(new BasicNameValuePair("lat",String.valueOf(loc.get(i).getLatitude())));
	        nameValuePairs.add(new BasicNameValuePair("long",String.valueOf(loc.get(i).getLongitude())));
	        nameValuePairs.add(new BasicNameValuePair("alt",String.valueOf(loc.get(i).getAltitude())));
	        nameValuePairs.add(new BasicNameValuePair("fecha",String.valueOf(loc.get(i).getTime())));
	    
	        //http post
	        try{
	            HttpClient httpclient = new DefaultHttpClient();
	            HttpPost httppost = new HttpPost("http://contra-vigilancia.net/tracks/recorrido.php");
	            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	            HttpResponse response = httpclient.execute(httppost);
	            HttpEntity entity = response.getEntity();
	            InputStream is = entity.getContent();
	            Log.i("Connection made", response.getStatusLine().toString());
	            i++;
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
	private void brodcastData(Location loc){
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
		nameValuePairs.add(new BasicNameValuePair("nombre",nombreRuta));
        nameValuePairs.add(new BasicNameValuePair("lat",String.valueOf(loc.getLatitude())));
        nameValuePairs.add(new BasicNameValuePair("long",String.valueOf(loc.getLongitude())));
        nameValuePairs.add(new BasicNameValuePair("alt",String.valueOf(loc.getAltitude())));
        nameValuePairs.add(new BasicNameValuePair("fecha",String.valueOf(loc.getTime())));
    
        //http post
        try{
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://contra-vigilancia.net/tracks/recorrido.php");
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            Log.i("Connection made", response.getStatusLine().toString());
            
        }

        catch(Exception e)
        {
            Log.e("log_tag_c", "Error in http connection "+e.toString());
        }           
	}
}

class GLView extends GLSurfaceView{

	mRenderer renderer = new mRenderer();
	DisplayMetrics dm = new DisplayMetrics();
	
	public GLView(Context context) {
		super(context);
		((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)) .getDefaultDisplay().getMetrics(dm);
        renderer.setMeasures(dm.heightPixels, dm.widthPixels);
		setRenderer(renderer);
	}
}