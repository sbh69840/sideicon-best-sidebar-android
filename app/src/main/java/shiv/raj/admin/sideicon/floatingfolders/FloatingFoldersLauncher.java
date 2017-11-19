package shiv.raj.admin.sideicon.floatingfolders;

import shiv.raj.admin.sideicon.FirstLaunch;
import shiv.raj.admin.sideicon.StandOutWindow;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.widget.Toast;

import com.example.admin.sideicon.R;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Set;

public class FloatingFoldersLauncher extends Activity {
	/**
	 * Called when the activity is first created.
	 */
	public final static int REQUEST_CODE = -1010101;
	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	private GoogleApiClient client;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);
		if (isFirstRun) {

			if (Build.VERSION.SDK_INT >= 23) {
				Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
				startActivityForResult(intent, REQUEST_CODE);
				if(!Settings.canDrawOverlays(getApplicationContext())){
					startActivity(new Intent(this, FirstLaunch.class));
					Toast.makeText(this, "Read this and allow the permission(if sdk greater than marshmallow)and later run the app from the app drawer", Toast.LENGTH_LONG).show();
				}
				}else{
				startActivity(new Intent(this, FirstLaunch.class));
				Toast.makeText(this, "First run", Toast.LENGTH_SHORT).show();
			}

			getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("isFirstRun", false).apply();
		}
if(Build.VERSION.SDK_INT<23) {



	StandOutWindow.closeAll(this, FloatingFolder.class);
	FloatingFolder.showFolders(this);



}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if(Settings.canDrawOverlays(getApplicationContext())){


				StandOutWindow.closeAll(this, FloatingFolder.class);
				FloatingFolder.showFolders(this);

			}
		}
		finish();


		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
	}



	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	public Action getIndexApiAction() {
		Thing object = new Thing.Builder()
				.setName("SideIcon Page") // TODO: Define a title for the content shown.
				// TODO: Make sure this auto-generated URL is correct.
				.setUrl(Uri.parse(""))
				.build();
		return new Action.Builder(Action.TYPE_VIEW)
				.setObject(object)
				.setActionStatus(Action.STATUS_TYPE_COMPLETED)
				.build();
	}

	@Override
	public void onStart() {
		super.onStart();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client.connect();
		AppIndex.AppIndexApi.start(client, getIndexApiAction());
	}

	@Override
	public void onStop() {
		super.onStop();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		AppIndex.AppIndexApi.end(client, getIndexApiAction());
		client.disconnect();
	}



}

