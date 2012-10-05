package anon.paisajes.territoriales.org;

import java.util.ArrayList;
import java.util.List;
import java.lang.Math.*;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.view.WindowManager;
import org.osmdroid.util.GeoPoint;


public class DrawView extends View implements OnTouchListener {
    private static final String TAG = "DrawView";
    
    public List<Location> points = new ArrayList<Location>();
    Paint paint = new Paint();
    Button inicio; 
    
    DisplayMetrics dm = new DisplayMetrics();
    final int height; 
    final int width;
    double minLat, minLon, maxLat, maxLon;
    
    public DrawView(Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
       // inicio.
        this.setOnTouchListener(this);

        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(2);
               
        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)) .getDefaultDisplay().getMetrics(dm);

         height = dm.heightPixels;
         width = dm.widthPixels;
    }

    @Override
    public void onDraw(Canvas canvas) {
    	
    	minLat = getMinLat();
    	minLon = getMinLon();
    	maxLat = getMaxLat();
    	maxLon = getMaxLon();
    	Log.i("","minLat="+minLat+", minLon= "+minLon+", maxLat="+ maxLat+", maxLon"+maxLon);
        for (Location point : points) {
        	
        	double longitud = getXdesdeLongitud(point.getLongitude());
        	float pixLong = (float) interpolate(longitud, minLon, maxLon, 10, width-10);
        	
        	double latitude =  getYdesdeLatitud(point.getLatitude());
        	float pixLat = (float) interpolate(latitude,minLat, maxLat, 10, height-10);
        	 
        	Log.i("tag", "pxLog="+pixLong+ ", pxLat="+pixLat);
            canvas.drawPoint(pixLong,pixLat, paint);
        }
    }
    
    
    double getXdesdeLongitud(double lon) {
    	  double longitud = mercatorLon(lon);
    	  longitud = (1+(longitud/Math.PI))/2;
    	 // float pixLong = (float) interpolate(longitud, 0, 1, 0, width);
    	 
    	  return longitud;
    }
    
    double getYdesdeLatitud(double lat) {
    	  double latitude = mercatorLat(lat);
    	  
    
    	   latitude = (1-(latitude/Math.PI))/2;
    	//  float pixLat = height/5; 
    	  
    	  // Log.i("tag", "pxLat="+pixLat);
    	  return latitude;
   	}
   	double mercatorLon(double lon) {
    	  double longitud = Math.toRadians (lon);
    	  Log.i("", "longitud ="+lon+", marcatorLon="+longitud);
    	  return longitud;
   	}
   	double mercatorLat(double lat) {
   		//y = log(tan(lat) + sec(lat))
   		double l = Math.toRadians(lat);
   		 double latitud = Math.log(Math.tan(l) + 1 / Math.cos(1));
    //	  double latitud = Math.log(Math.tan(Math.toRadians(lat)+1/Math.cos(Math.toRadians(lat))));
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
   	
   	public boolean nuevoPunto(Location point){
   		points.add(point);
        invalidate();
   		return true;
   	}
   	
   	static double interpolate(double value, double low1, double high1, double low2 , double high2){
   	    return low2 + (value - low1) * (high2 - low2) / (high1 - low1);
   	}
   	
    public boolean onTouch(View view, MotionEvent event) {
        // if(event.getAction() != MotionEvent.ACTION_DOWN)
        // return super.onTouchEvent(event);
    	
    	/*
        GeoPoint point = new Point();
        point.x = event.getX();
        point.y = event.getY();
        points.add(point);
        invalidate();
        Log.d(TAG, "point: " + point);*/
        return true;
    }
}


