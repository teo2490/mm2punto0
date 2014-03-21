package teo2490.mm2punto0;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	private Button btnPic;
	private Button btnNewPic;
	private Button btnNewPhr;

	/**
	 * On creation kind of device is checked and the orientation is set.
	 * EditText, Textiew and Button are placed.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
		
		btnPic = (Button) findViewById(R.id.button1);
		btnNewPic = (Button) findViewById(R.id.button2);
		btnNewPhr = (Button) findViewById(R.id.button3);
		
		btnPic.setOnClickListener(new View.OnClickListener() {

			/**
			 * Picture of the Day Activity is started
			 */
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, PictureDayActivity.class);
	        	startActivity(intent);
	        	//finish();
			}
		});
		
		btnNewPic.setOnClickListener(new View.OnClickListener() {

			/**
			 * Add Picture Activity is started
			 */
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, NewPhotoActivity.class);
	        	startActivity(intent);
	        	//finish();
			}
		});
		
		btnNewPhr.setOnClickListener(new View.OnClickListener() {

			/**
			 * New Phrase Activity is started
			 */
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, NewPhraseActivity.class);
	        	startActivity(intent);
	        	//finish();
			}
		});
	}

}
