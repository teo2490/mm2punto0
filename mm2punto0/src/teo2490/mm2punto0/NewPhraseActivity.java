package teo2490.mm2punto0;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import teo2490.library.JSONParser;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class NewPhraseActivity extends Activity {
	
	JSONParser jsonParser = new JSONParser();

	private ProgressDialog pDialog;
	
	private Button btnCreate;
	private EditText etxPhrase;
	
	// url to create new product
    private static String url_create_phrase = "http://mm2punto0.altervista.org/server/create_phrase.php";
 
    // JSON Node names
    private static final String TAG_SUCCESS = "success";
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_phrase);
		
		if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
       	   getActionBar().setDisplayHomeAsUpEnabled(true);
       	}
		
		btnCreate = (Button) findViewById(R.id.button1);
		etxPhrase = (EditText) findViewById(R.id.editText1);
		
		// button click event
		btnCreate.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View view) {
	                new CreateNewPhrase().execute();
            }
        });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.new_phrase, menu);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	   switch (item.getItemId()) {
	      case android.R.id.home:
				Intent back = new Intent(getApplicationContext(), MainActivity.class);
	        	back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        	startActivity(back);
	        	finish();
                return true;
	         
	      default:
	    	  return super.onOptionsItemSelected(item);
	   }
	}

	class CreateNewPhrase extends AsyncTask<String, String, String> {
		 
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            
            pDialog = new ProgressDialog(NewPhraseActivity.this);
            pDialog.setMessage(getString(R.string.PleaseWait));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
            
        }
 
        /**
         * Creating request and handling response
         * */
        protected String doInBackground(String... args) {
            String phrase = etxPhrase.getText().toString();
            //phrase = phrase.replaceAll("\'", "\\'");
            Log.d("AAA", phrase);          
 
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("phrase", phrase));
            
            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_create_phrase,
                    "POST", params);
                        
            // check log cat for response
            Log.d("Create Response", json.toString());
 
            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);
 
                if (success == 1) {
                    // successfully created product
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
 
                    // closing this screen
                    finish();
                } else {
                    // failed to create product
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
 
            return null;
        }
 
        /**
         * After completing background task (if there aren't other background tasks running),
         * dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
        		// dismiss the dialog once done
        		pDialog.dismiss();
        		Toast.makeText(getApplicationContext(), R.string.PhraseCreated, Toast.LENGTH_LONG).show();
        	}
        }
    }
