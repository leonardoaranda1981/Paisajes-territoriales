package anon.paisajes.territoriales.org;

import java.io.File;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.View;


public class listActivity extends ListActivity {
	
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        
	       
	        File carpeta = new File ("/gpx/");
	        File [] archivos = carpeta.listFiles();
	        String [] nombrearchivos = new String [archivos.length]; 
	        for (int i = 0; i<nombrearchivos.length; i++){
	        	nombrearchivos[i] = archivos[i].getName();
	        	
	        }
	        
	        
	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
	                android.R.layout.simple_list_item_1,nombrearchivos);
	            setListAdapter(adapter);
	        
	       
	        
	        
	     }
	 @Override 
	    public void onListItemClick(ListView l, View v, int position, long id) {
	        String ruta = l.getItemAtPosition(position).toString();
	        llamaVisualizacion( ruta);
	    }
	 public void llamaVisualizacion(String ruta){
		 Intent newIntent = new Intent(this.getApplicationContext(), PaisajesTerritorialesB.class);
	        Bundle bundle = new Bundle();
	        bundle.putString("param1", ruta);
	        newIntent.putExtras(bundle);
	        startActivityForResult(newIntent, 0);
		 
	 }
}
