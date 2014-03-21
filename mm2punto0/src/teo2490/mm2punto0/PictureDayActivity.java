package teo2490.mm2punto0;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import teo2490.library.JSONParser;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PictureDayActivity extends Activity {
	
	private ImageView imageView;
	private TextView textView;
	private TextView tvDisplayDate;
	
	Bitmap image;
	private String phrase;
	private String photoName;
	
	private ProgressDialog pDialog;
	private boolean isLastThread = false;
	
	// JSON parser class
	JSONParser jsonParser = new JSONParser();

	// single product url
	private static final String url_phrase = "http://mm2punto0.altervista.org/server/get_phrase.php";
	private static final String url_photo = "http://mm2punto0.altervista.org/server/get_photo.php";
	
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_PHRASE = "testo";
	private static final String TAG_PHOTO = "nome";
	

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture_day);
		
		if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
       	   getActionBar().setDisplayHomeAsUpEnabled(true);
       	}
		
		imageView = (ImageView) findViewById(R.id.imageView1);
		imageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent intent = new Intent();
            	intent.setAction(android.content.Intent.ACTION_VIEW); intent.setDataAndType(getImageUri(getApplicationContext(), image),"image/*");
            	startActivity(intent);
            }
        });
		textView = (TextView) findViewById(R.id.textView1);
		tvDisplayDate = (TextView) findViewById(R.id.tvDate);


	    final Calendar c = Calendar.getInstance();
	    int yy = c.get(Calendar.YEAR);
	    int m = c.get(Calendar.MONTH);
	    int dd = c.get(Calendar.DAY_OF_MONTH);
	    String mm = getMonthForInt(m);
	    String weekDay;
	    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.ITALY);

	    weekDay = dayFormat.format(c.getTime());

	    // set current date into textview
	    tvDisplayDate.setText(new StringBuilder()
	    .append(weekDay).append(" ").append(dd).append(" ").append(mm).append(" ").append(yy));
		
		pDialog = ProgressDialog.show(this, getString(R.string.Loading),
				getString(R.string.PleaseWait), true); 
		 
		new GetPhrase().execute();
		//new GetImage().execute();
	}
	
	String getMonthForInt(int num) {
        String month = "wrong";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        if (num >= 0 && num <= 11 ) {
            month = months[num];
        }
        return month;
    }
	
	public Uri getImageUri(Context inContext, Bitmap inImage) {
		  ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		  inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
		  String path = Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
		  return Uri.parse(path);
		}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.picture_day, menu);
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
	
	 /**
	 * Background Async Task to get phrase
	 * */
	class GetPhrase extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();   			
		}

		protected String doInBackground(String... params) {

		
					// Check for success tag
					int success;
					int msuccess;
					try {
						List<NameValuePair> params1 = new ArrayList<NameValuePair>();
						params1.add(new BasicNameValuePair("const", "1"));

						// getting product details by making HTTP request
						// Note that product details url will use GET request
						JSONObject json = jsonParser.makeHttpRequest(
								url_phrase, "POST", params1);

						// check your log for json response
						Log.d("Single Product Details", json.toString());
						
						// json success tag
						success = json.getInt(TAG_SUCCESS);
						if (success == 1) {
							// successfully received product details
							JSONArray productObj = json.getJSONArray(TAG_PHRASE); // JSON Array
							
							// get first product object from JSON Array
							JSONObject product = productObj.getJSONObject(0);							

							phrase = product.getString(TAG_PHRASE);
						}else{
							// product with pid not found
						}
						
						//------------------
						
						List<NameValuePair> params2 = new ArrayList<NameValuePair>();
						params2.add(new BasicNameValuePair("const", "1"));

						// getting product details by making HTTP request
						// Note that product details url will use GET request
						JSONObject jsonm = jsonParser.makeHttpRequest(
								url_photo, "POST", params2);

						// check your log for json response
						Log.d("Photo Details", jsonm.toString());
						
						// json success tag
						msuccess = jsonm.getInt(TAG_SUCCESS);
						if (msuccess == 1) {
							// successfully received merchant details
							JSONArray merchantObj = jsonm
									.getJSONArray(TAG_PHOTO); // JSON Array
							
							// get first merchant object from JSON Array
							JSONObject merchant = merchantObj.getJSONObject(0);
						
							photoName = merchant.getString(TAG_PHOTO);
							
							new DownloadImage().execute();
						}
						else {
							JSONArray merchantObj = jsonm
									.getJSONArray(TAG_PHOTO); // JSON Array
							
							// get first merchant object from JSON Array
							JSONObject merchant = merchantObj.getJSONObject(0);
						
							String ph = merchant.getString("id");
							Toast.makeText(getApplicationContext(), ph, Toast.LENGTH_LONG).show();
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					

			return null;
		}


		/**
		 * After completing background task Dismiss the progress dialog and setting the data downloaded 
		 * from the server in the UI
		 * **/
		protected void onPostExecute(String file_url) {
			if(isLastThread){
			pDialog.dismiss();
			}else{
				isLastThread = true;
			}
			if(phrase=="null"){
				phrase = ":)!";
			}
			
			textView = (TextView) findViewById(R.id.textView1);
			textView.setText(phrase);
		}
	}

	class DownloadImage extends AsyncTask<Void,Void,Void>
    {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            
        }

        /**
         * Downloading the image from the server
         */
        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            try
            {
            //URL url = new URL( "http://a3.twimg.com/profile_images/670625317/aam-logo-v3-twitter.png");
            String dwn = "http://mm2punto0.altervista.org/uploads/"+photoName+".bmp";
            image = downloadBitmap(dwn);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        /**
		 * After completing background task (if this is the last tag active) dismiss the progress dialog
		 * 
		 */
        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if(image!=null)
            {
                //imgPhoto.setImageBitmap(image);
            }
         // dismiss the dialog once got all details
            if(isLastThread){
            	pDialog.dismiss();
            }else{
            	isLastThread = true;
            }
         			
         			ImageView imgPhoto = (ImageView) findViewById(R.id.imageView1);
         			imgPhoto.setImageBitmap(image);
        }   
        
        
    }
    
    /**
     * This method get the stream from HTTP and converti it in Bitmap format
     * @param url
     * @return The bitmap image of a gift
     */
     private Bitmap downloadBitmap(String url) {
         // initilize the default HTTP client object
         final DefaultHttpClient client = new DefaultHttpClient();

         //forming a HttoGet request 
         final HttpGet getRequest = new HttpGet(url);
         try {

             HttpResponse response = client.execute(getRequest);

             //check 200 OK for success
             final int statusCode = response.getStatusLine().getStatusCode();

             if (statusCode != HttpStatus.SC_OK) {
                 Log.w("ImageDownloader", "Error " + statusCode + 
                         " while retrieving bitmap from " + url);
                 return null;

             }

             final HttpEntity entity = response.getEntity();
             if (entity != null) {
                 InputStream inputStream = null;
                 try {
                     // getting contents from the stream 
                     inputStream = entity.getContent();

                     // decoding stream data back into image Bitmap that android understands
                     image = BitmapFactory.decodeStream(inputStream);


                 } finally {
                     if (inputStream != null) {
                         inputStream.close();
                     }
                     entity.consumeContent();
                 }
             }
         } catch (Exception e) {
             // You Could provide a more explicit error message for IOException
             getRequest.abort();
             Log.e("ImageDownloader", "Something went wrong while" +
                     " retrieving bitmap from " + url + e.toString());
         } 

         return image;
     }
}
