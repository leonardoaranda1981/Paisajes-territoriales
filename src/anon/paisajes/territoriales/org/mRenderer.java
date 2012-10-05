package anon.paisajes.territoriales.org;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.location.Location;
import android.opengl.GLSurfaceView;
import android.util.Log;

public class mRenderer implements GLSurfaceView.Renderer {

	public List<Location> points = new ArrayList<Location>();
	public List<Float> vertex = new ArrayList<Float>();;
	int vertices[];
	byte indices[];
	
	IntBuffer mVertexBuffer;
	ByteBuffer mIndexBuffer;
	
	boolean translate = false;
	double minLat, minLon, maxLat, maxLon;
	int width, height, cont=0;
	
	//float tx=0.0f, ty=0.0f, tz=0.0f;
	
	//@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		//gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
	}
	
	//@Override
	public void onDrawFrame(GL10 gl) {

		route();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glFrontFace(GL10.GL_CW);
		gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer);
		gl.glColor4f(0.0f, 1.0f, 0.0f, 0.5f);
    	//gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);
				
        gl.glPushMatrix();

        	//if(translate){
        		//gl.glTranslatef(-tx, -ty, -tz);
        		//translate = false;
        	//}	
        	gl.glRotatef(Movements.x*18, 0.0f, 1.0f, 0.0f);
        	gl.glRotatef(Movements.y*18, 1.0f, 0.0f, 0.0f);
        	gl.glRotatef(Movements.z*18, 0.0f, 0.0f, 1.0f);
        	gl.glDrawElements(GL10.GL_LINES, indices.length, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
    	gl.glPopMatrix();
	}

	//@Override
	public void onSurfaceChanged(GL10 gl, int vwidth, int vheight) {

		gl.glViewport(0, 0, vwidth, vheight);
	}
	
	
	public void route(){
		
		convert();
		
		if(vertex.size()>0){
			
			vertices = new int[vertex.size()];
			
			int i = 0;
			for(Float f : vertex){
				vertices[i] = (int)f.floatValue()-20000;
				i++;
			}
			
			/*
			if(vertex.size() == 1){
				translate = true;
				tx = vertices[0];
				ty = vertices[1];
				tz = vertices[2];
			}
			*/
			
			indices = new byte[(((vertices.length/3)-1)*2)];
			
			Log.d("INDEX SIZE", ((Integer)(((vertices.length/3)-1)*2)).toString());
			
			int k = 0;
			
			for(int j=0; j<((((vertices.length/3)-1)*2)-1); j+=2){
				indices[j] = (byte) k;
				indices[j+1] = (byte) (k+1);
				k++;
			}			
		}
		
		else{
			vertices = new int[6];
			vertices[0]=0;
			vertices[1]=0;
			vertices[2]=0;
			
			vertices[3]=50000;
			vertices[4]=50000;
			vertices[5]=0;
			
			indices = new byte[2];
			indices [0]= 0;
			indices [1]= 1;			
		}
		
		
		for(int i=0; i<vertices.length; i=i+3){
			Log.d("ARRAY X", ((Integer)vertices[i]).toString());
			Log.d("ARRAY Y", ((Integer)vertices[i+1]).toString());
			Log.d("ARRAY Z", ((Integer)vertices[i+2]).toString());
		}
		
		/*
		for(int i=0; i<indices.length; i++){
			Log.d("INDEX", ((Byte)indices[i]).toString());
		}
		*/
		
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length*4);
	    vbb.order(ByteOrder.nativeOrder());
	    mVertexBuffer = vbb.asIntBuffer();
	    mVertexBuffer.put(vertices);
	    mVertexBuffer.position(0);
	    
	    mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
        mIndexBuffer.put(indices);
        mIndexBuffer.position(0);
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
		width = 30000;
		height = 50000;
	}
	
	public boolean nuevoPunto(Location point){
   		
		points.add(point);
   		return true;
   	}
	
	public void convert(){
		
		vertex.clear();
	
		minLat = getMinLat();
    	minLon = getMinLon();
    	maxLat = getMaxLat();
    	maxLon = getMaxLon();

        for (Location point : points) {
        	
        	double longitud = getXdesdeLongitud(point.getLongitude());
        	float pixLong = ((float) interpolate(longitud, minLon, maxLon, 10, width-10))*100;
        	
        	double latitude =  getYdesdeLatitud(point.getLatitude());
        	float pixLat = ((float) interpolate(latitude,minLat, maxLat, 10, height-10))*100;
        	
        	float pixAlt = 0.0f;
        	
        	vertex.add(pixLong);
        	vertex.add(pixLat);
        	vertex.add(pixAlt);
        }      
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
  	  //Log.d("", "longitud ="+lon+", marcatorLon="+longitud);
  	  return longitud;
 	}
 	
 	double mercatorLat(double lat) {

 		double l = Math.toRadians(lat);
 		 double latitud = Math.log(Math.tan(l) + 1 / Math.cos(1));

  	  //Log.d("", "lat ="+lat+", latRadians="+l+", latitudMercator="+latitud);
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