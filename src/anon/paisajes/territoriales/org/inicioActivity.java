package anon.paisajes.territoriales.org;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class inicioActivity extends Activity{
	
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.iniciolayout);
	    Button a = (Button)findViewById(R.id.button1);
	    Button b = (Button)findViewById(R.id.button2);
	  
	    
	    a.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), PaisajesTerritorialesActivity.class);
                startActivityForResult(myIntent, 0);
            }

        });
	    b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), listActivity.class);
                startActivityForResult(myIntent, 0);
            }

        });
	    
	 }
}
