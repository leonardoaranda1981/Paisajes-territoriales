package anon.paisajes.territoriales.org;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

public class PaisajesTerritorialesB extends Activity  {
	GLView glView;
	int duration;
	String nombreRuta = "";
	
	 public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        duration = Toast.LENGTH_LONG;
	        //Gráficos
	        glView = new GLView(this);
	        setContentView(glView);
	        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
	        Bundle bundle = this.getIntent().getExtras();
	        String rutaGpx = bundle.getString("param1");
	        
	        inicarParsing(rutaGpx);
	        
	 }
	 @Override
	  public void onResume(){
		  super.onResume();
		 // connection.bind(getApplicationContext());
		  glView.onResume();
	  }
   
	  @Override
	  public void onPause(){
		  super.onPause();
		 // connection.unbind(getApplicationContext());
		 // sm.unregisterListener(this);
		  glView.onPause();
	  }
	  private void inicarParsing(String rutaGpx){
	    	 GpxParserSax parser = new GpxParserSax(rutaGpx);
	    	 glView.renderer.points= parser.parse();
	    	 
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

}
