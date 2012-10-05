package anon.paisajes.territoriales.org;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.lang.StringBuilder;
import java.util.List;
import java.util.ArrayList;
//import org.osmdroid.util.GeoPoint;

import android.location.Location;
import android.location.LocationManager;

public class GpxHandler extends DefaultHandler{
		private List<Location> nodos;
	    private Location coordenadaActual = new Location(LocationManager.GPS_PROVIDER);
	    private StringBuilder sbTexto;
	    private boolean elevation = false;
	 
	    public List<Location> getListaCoordenadas(){
	        return nodos;
	    }
	 
	    @Override
	    public void characters(char[] ch, int start, int length)
	                   throws SAXException {
	 
	        super.characters(ch, start, length);
	 
	        
	        
	        if (this.coordenadaActual != null){
	        	
	        	if (elevation == true){
	        		sbTexto.append(ch, start, length);
	        		String valueAlt = sbTexto.toString();
	             	double  alt = Double.parseDouble(valueAlt);
	             	coordenadaActual.setAltitude(alt);
	        	}
	        }
	           
	       
	    }
	 
	    @Override
	    public void endElement(String uri, String localName, String name)
	                   throws SAXException {
	 
	        super.endElement(uri, localName, name);
	        if (this.coordenadaActual != null) {
		       	 
	            if (localName.equals("time")) {
	         //   	coordenadaActual.setTiempo(sbTexto.toString());
	            }else if(localName.equals("name")){
	      //      	coordenadaActual.setNombre(sbTexto.toString());
	            }else if (localName.equals("wpt")) {
	                nodos.add(coordenadaActual);
	            }else if (localName.equals("ele")){
	            	elevation = false; 
	            }
	            sbTexto.setLength(0);
	        }
	    }
	 
	    @Override
	    public void startDocument() throws SAXException {
	 
	        super.startDocument();
	 
	        nodos = new ArrayList<Location>();
	        sbTexto = new StringBuilder();
	    }
	 
	    @Override
	    public void startElement(String uri, String localName,
	                   String name, Attributes atts) throws SAXException {
	 
	        super.startElement(uri, localName, name, atts);
	 
	        if (localName.equals("wpt")) {
	        	String attrValueLat = atts.getValue("lat");
	        	String attrValueLon = atts.getValue("lon");
	        	double  la = Double.parseDouble(attrValueLat);
	        	double  lo = Double.parseDouble(attrValueLon);
	        	coordenadaActual.setLongitude(lo);
	        	coordenadaActual.setLatitude(la);
	        	sbTexto.setLength(0);
	        }
	        if (localName.equals("ele")) {
	        	elevation = true; 
	        	
	        	
	        }
	       
	        
	    }
	    

}
