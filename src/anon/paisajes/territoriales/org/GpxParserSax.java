package anon.paisajes.territoriales.org;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.net.URL;
import javax.xml.parsers.SAXParser;
import java.net.MalformedURLException;
import javax.xml.parsers.SAXParserFactory;
import org.osmdroid.util.GeoPoint;

import android.location.Location;

public class GpxParserSax {
	 private String gpxRuta;
	 
	    public GpxParserSax(String ruta)
	    {
	       
	            this.gpxRuta = ruta;
	       
	    }
	 
	    public List<Location> parse()
	    {
	        SAXParserFactory factory = SAXParserFactory.newInstance();
	 
	        try
	        {
	            SAXParser parser = factory.newSAXParser();
	            GpxHandler handler = new GpxHandler();////
	            parser.parse(this.getInputStream(), handler);
	            return handler.getListaCoordenadas(); ////
	        }
	        catch (Exception e)
	        {
	            throw new RuntimeException(e);
	        }
	    }
	 
	    private InputStream getInputStream()
	    {
	        try
	        {
	        	File file = new File(gpxRuta);
                InputStream response= new FileInputStream(file);
	            return response;
	        }
	        catch (IOException e)
	        {
	            throw new RuntimeException(e);
	        }
	    }

}
