package anon.paisajes.territoriales.org;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.location.Location;
import android.opengl.GLSurfaceView;
import android.util.Log;

public class mRenderer implements GLSurfaceView.Renderer {

	public List<Location> points = new ArrayList<Location>();
	public List<Float> vertex = new ArrayList<Float>();
	float vertices[];
	
	FloatBuffer mVertexBuffer;
	ByteBuffer mIndexBuffer;
	
	double minLat, minLon, maxLat, maxLon;
	int width, height;
	
	//@Override
	//Aquí se llaman a las funciones de inicialización (glClearColor)y habilitación(glEnable)
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		//route();
	}
	
	//@Override
	//Esta es la función de dibujo que repite en pantalla tantas veces por segundo lo que tenga definido
	public void onDrawFrame(GL10 gl) {

		route();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glFrontFace(GL10.GL_CW);
		//Arreglo de vertices coordenadas, tipo, offset, apuntador
		gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer);
		//Arreglo de colores
		gl.glColor4f(0.0f, 1.0f, 0.0f, 0.5f);
    	//gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);
		
    	//Matriz de transformación
        gl.glPushMatrix();
        	
        	//La matriz realiza una rotación dependiendo de los movimientos del acelerometro.
        	gl.glRotatef(Movements.x*18, 0.0f, 1.0f, 0.0f);
        	gl.glRotatef(Movements.y*18, 1.0f, 0.0f, 0.0f);
        	gl.glRotatef(Movements.z*18, 0.0f, 0.0f, 1.0f);
        	gl.glDrawElements(GL10.GL_POINTS, vertices.length/3, GL10.GL_UNSIGNED_BYTE, mVertexBuffer);
        	
    	gl.glPopMatrix();
    	
    	Log.d("SIZE", ((Integer)vertex.size()).toString());
	}

	//@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {

		gl.glViewport(0, 0, width, height);
	}
	
	public void route(){

		if(vertex.size()>0){
			vertices = new float[vertex.size()];
			int i = -1;
			for(Float f : vertex){
				vertices[i++] = f.floatValue();				
			}		
		}
		
		else{
			vertices = new float[3];
			vertices[0]=0.0f;
			vertices[1]=0.0f;
			vertices[2]=0.0f;
		}
		
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length*4);
	    vbb.order(ByteOrder.nativeOrder());
	    mVertexBuffer = vbb.asFloatBuffer();
	    mVertexBuffer.put(vertices);
	    mVertexBuffer.position(0);
	}
	
	public static class Movements{
		
		static Float x=0.0f; 
		static Float y=0.0f;
		static Float z=0.0f;
		
	    public static void setValues(Float xv, Float yv, Float zv){
	    	x = xv;
	    	y = yv;
	    	z = zv;
	    }
	}
	
	public void setMeasures(int w, int h){
		width = w;
		height = h;
	}
	
	public boolean nuevoPunto(Location point){
   		
		points.add(point);
   		minLat = getMinLat();
    	minLon = getMinLon();
    	maxLat = getMaxLat();
    	maxLon = getMaxLon();
    	
        for (Location iterpoint : points) {
        	
        	double longitud = getXdesdeLongitud(iterpoint.getLongitude());
        	float pixLong = (float) interpolate(longitud, minLon, maxLon, 10, width-10);
        	
        	double latitude =  getYdesdeLatitud(iterpoint.getLatitude());
        	float pixLat = (float) interpolate(latitude,minLat, maxLat, 10, height-10);
        	
        	float pixAlt = 0.0f;
        	
        	vertex.add(pixLong);
        	Log.d("LONG", vertex.get(vertex.size()-2).toString());
        	vertex.add(pixLat);
        	Log.d("LAT", vertex.get(vertex.size()-1).toString());
        	vertex.add(pixAlt);
        	Log.d("ALT", vertex.get(vertex.size()).toString());
        }
        
   		return true;
   	}
	
	double getXdesdeLongitud(double lon) {
  	  double longitud = mercatorLon(lon);
  	  longitud = (1+(longitud/Math.PI))/2;
  	 
  	  return longitud;
  }
  
	double getYdesdeLatitud(double lat) {
  	  double latitude = mercatorLat(lat);
  	  
  
  	   latitude = (1-(latitude/Math.PI))/2;
  	  return latitude;
 	}
  
 	double mercatorLon(double lon) {
  	  double longitud = Math.toRadians (lon);
  	  Log.i("", "longitud ="+lon+", marcatorLon="+longitud);
  	  return longitud;
 	}
 	
 	double mercatorLat(double lat) {

 		double l = Math.toRadians(lat);
 		 double latitud = Math.log(Math.tan(l) + 1 / Math.cos(1));

  	  Log.i("", "lat ="+lat+", latRadians="+l+", latitudMercator="+latitud);
  	  return latitud;
  }
 	
 	private double getMinLat(){
 		double a = 1;
 		
 		for(int i = 0; i<points.size(); i++){
 			if(a >getYdesdeLatitud(points.get(i).getLatitude())){ 
 				a =getYdesdeLatitud(points.get(i).getLatitude());	
 			}
 		}
 		return a;
 	}
 	
 	private double getMaxLat(){
 		double a = 0;
 		for(int i = 0; i<points.size(); i++){
 			if(a <getYdesdeLatitud(points.get(i).getLatitude())){ 
 				a = getYdesdeLatitud(points.get(i).getLatitude());	
 			}
 		}
 		return a;
 	}
 	
 	private double getMinLon(){
 		double a = 1;
 		
 		for(int i = 0; i<points.size(); i++){
 			if(a >getXdesdeLongitud(points.get(i).getLongitude())){ 
 				a = getXdesdeLongitud(points.get(i).getLongitude());	
 			}
 		}
 		return a;
 	}
 	
 	private double getMaxLon(){
 		double a = 0;
 		for(int i = 0; i<points.size(); i++){
 			if(a <getXdesdeLongitud(points.get(i).getLongitude())){ 
 				a =getXdesdeLongitud (points.get(i).getLongitude());	
 			}
 		}
 		return a;
 	}
 	
 	static double interpolate(double value, double low1, double high1, double low2 , double high2){
 	    return low2 + (value - low1) * (high2 - low2) / (high1 - low1);
 	}

}