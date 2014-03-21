package teo2490.mm2punto0;

import java.io.ByteArrayOutputStream;
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
import org.json.JSONException;
import org.json.JSONObject;

import teo2490.library.Base64;
import teo2490.library.JSONParser;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class NewPhotoActivity extends Activity {
	
	JSONParser jsonParser = new JSONParser();

	private static final int PICK_IMAGE = 1;
	private static final int PICK_Camera_IMAGE = 2;
	private ImageView imgView;
	private Button upload;
	private Bitmap bitmap;
	private ProgressDialog dialog;
	private String photoName;
	Uri imageUri;
	
	private boolean isLastThread = true;
	
	private static String url_create_product = "http://mm2punto0.altervista.org/server/create_photo.php";
	private static final String TAG_SUCCESS = "success";
	
	MediaPlayer mp=new MediaPlayer();
	
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_photo);
		
		if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
       	   getActionBar().setDisplayHomeAsUpEnabled(true);
       	}

		imgView = (ImageView) findViewById(R.id.imageView1);
		upload = (Button) findViewById(R.id.button1);
		
		upload.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (bitmap == null) {
					Toast.makeText(getApplicationContext(),
							R.string.Select, Toast.LENGTH_SHORT).show();
				} else {
					dialog = ProgressDialog.show(NewPhotoActivity.this, getString(R.string.Loading),
							getString(R.string.PleaseWait), true);
					//Create the name of the photo by the timestamp
					Long tsLong = System.currentTimeMillis()/1000;
					photoName = tsLong.toString();
					// Uploading photo in background thread
					new ImageGalleryTask().execute();
					// creating new product in background thread
	                new CreateNewPhoto().execute();
				}
			}
		});		
	}

	@Override
	 public boolean onCreateOptionsMenu(Menu menu) {
                 MenuInflater inflater = getMenuInflater();
                 inflater.inflate(R.menu.new_photo, menu);
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
	            
	        case R.id.camera:
	        	//define the file-name to save photo taken by Camera activity
	        	String fileName = photoName+".jpg";
	        	//create parameters for Intent with filename
	        	ContentValues values = new ContentValues();
	        	values.put(MediaStore.Images.Media.TITLE, fileName);
	        	values.put(MediaStore.Images.Media.DESCRIPTION,"Image captured by camera");
	        	//imageUri is the current activity attribute, define and save it for later usage (also in onSaveInstanceState)
	        	imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
	        	//create new Intent
	        	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	        	intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
	        	intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
	        	startActivityForResult(intent, PICK_Camera_IMAGE);
	            return true;
	        
	        case R.id.gallery:
	        	try {
				Intent gintent = new Intent();
				gintent.setType("image/*");
				gintent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(
				Intent.createChooser(gintent, "Select Picture"),
				PICK_IMAGE);
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(),
				e.getMessage(),
				Toast.LENGTH_LONG).show();
				Log.e(e.getClass().getName(), e.getMessage(), e);
			}
	        	return true;
        }
		return false;
    }

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Uri selectedImageUri = null;
		String filePath = null;
		switch (requestCode) {
				case PICK_IMAGE:
					if (resultCode == Activity.RESULT_OK) {
						selectedImageUri = data.getData();
					}
					break;
				case PICK_Camera_IMAGE:
					 if (resultCode == RESULT_OK) {
		 		        //use imageUri here to access the image
		 		    	selectedImageUri = imageUri;
		 		    	/*Bitmap mPic = (Bitmap) data.getExtras().get("data");
						selectedImageUri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), mPic, getResources().getString(R.string.app_name), Long.toString(System.currentTimeMillis())));*/
				    } else if (resultCode == RESULT_CANCELED) {
		 		        Toast.makeText(this, R.string.NotTaken, Toast.LENGTH_SHORT).show();
		 		    } else {
		 		    	Toast.makeText(this, R.string.NotTaken, Toast.LENGTH_SHORT).show();
		 		    }
					 break;
			}
		
			if(selectedImageUri != null){
					try {
						// OI FILE Manager
						String filemanagerstring = selectedImageUri.getPath();
			
						// MEDIA GALLERY
						String selectedImagePath = getPath(selectedImageUri);
			
						if (selectedImagePath != null) {
							filePath = selectedImagePath;
						} else if (filemanagerstring != null) {
							filePath = filemanagerstring;
						} else {
							Toast.makeText(getApplicationContext(), "Unknown path",
									Toast.LENGTH_LONG).show();
							Log.e("Bitmap", "Unknown path");
						}
			
						if (filePath != null) {
							decodeFile(filePath);
						} else {
							bitmap = null;
						}
					} catch (Exception e) {
						Toast.makeText(getApplicationContext(), "Internal error",
								Toast.LENGTH_LONG).show();
						Log.e(e.getClass().getName(), e.getMessage(), e);
					}
			}
	
	}

	class CreateNewPhoto extends AsyncTask<String, String, String> {
		 
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*
            pDialog = new ProgressDialog(NewProductActivity.this);
            pDialog.setMessage("Creating Product..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
            */
        }
 
        /**
         * Creating request and handling response
         * */
        protected String doInBackground(String... args) {
            String phName = photoName;
 
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("photo", phName));
             
            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_create_product,
                    "POST", params);
                        
            // check log cat for response
            Log.d("Create Response", json.toString());
 
            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);
 
                if (success == 1) {
                    // successfully created product MANCA!!
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    //i.putExtra(TAG_LID, lid);
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
        	if(isLastThread){
        		// dismiss the dialog once done
        		dialog.dismiss();
        	}
        	else{
        		isLastThread=true;
        	}
        }
    }
    
    /**
     * Background Async Task to upload the photo of the new gift
     * 
     */
    class ImageGalleryTask extends AsyncTask<Void, Void, String> {
    	
    	/**
         * Uploading photo and handling response
         * 
         * */
		@SuppressWarnings("unused")
		@Override
		protected String doInBackground(Void... unsued) {
				InputStream is;
			    BitmapFactory.Options bfo;
			    Bitmap bitmapOrg;
			    ByteArrayOutputStream bao ;
			   
			    bfo = new BitmapFactory.Options();
			    bfo.inSampleSize = 2;
			    //bitmapOrg = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/" + customImage, bfo);
			      
			    bao = new ByteArrayOutputStream();
			    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bao);
				byte [] ba = bao.toByteArray();
				String ba1 = Base64.encodeBytes(ba);
				ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("image",ba1));
				String defName = photoName+".bmp";
				Log.v("name", defName);
				nameValuePairs.add(new BasicNameValuePair("cmd",defName));
				//nameValuePairs.add(new BasicNameValuePair("cmd", photoName+".bmp"));
				Log.v("log_tag", System.currentTimeMillis()+".bmp");	       
				try{
				        HttpClient httpclient = new DefaultHttpClient();
				        HttpPost httppost = new 
                      //  Here you need to put your server file address
				        HttpPost("http://mm2punto0.altervista.org/upload_photo.php");
				        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				        HttpResponse response = httpclient.execute(httppost);
				        HttpEntity entity = response.getEntity();
				        is = entity.getContent();
				        Log.v("log_tag", "In the try Loop" );
				   }catch(Exception e){
				        Log.v("log_tag", "Error in http connection "+e.toString());
				   }
			return "Success";
			// (null);
		}

		@Override
		protected void onProgressUpdate(Void... unsued) {

		}
		
		/**
         * After completing background task (if there aren't other background tasks running),
         * dismiss the progress dialog
         * **/
		@Override
		protected void onPostExecute(String sResponse) {
        	if(isLastThread){
        		// dismiss the dialog once done
        		dialog.dismiss();
        		Toast.makeText(getApplicationContext(), R.string.PhotoCreated, Toast.LENGTH_LONG).show();
        	}
        	else{
        		isLastThread=true;
        	}
        }
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		@SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		if (cursor != null) {
			// HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
			// THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} else
			return null;
	}

	public void decodeFile(String filePath) {
		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, o);

		// The new size we want to scale to
		final int REQUIRED_SIZE = 1024;

		// Find the correct scale value. It should be the power of 2.
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE)
				break;
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}

		// Decode with inSampleSize
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		bitmap = BitmapFactory.decodeFile(filePath, o2);

		imgView.setImageBitmap(bitmap);

	}

}

