package com.example.rdcsc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements SensorEventListener {
	
	private Client client = null;
	
	private SensorManager sensorManager;
	private boolean grantAccess = false;
	private boolean noProblemsWitStreams = true;
	
	private long startTime;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	}
	
	@Override
	public void onSensorChanged(final SensorEvent event) {
	  if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
		  if(client != null && grantAccess && noProblemsWitStreams){
				getAccelerometer(event);
		  }
	  }
	}
	
	  private void getAccelerometer(SensorEvent event) {
	    float[] values = event.values;
	    // Movement
	    float x = values[0];
	    float y = values[1];
	    float z = values[2];
	    long currentTime = System.currentTimeMillis();
	    
	    noProblemsWitStreams = client.sendMessage("INSERT INTO Accelerometer (TIMESTAMP, X, Y, Z) VALUES ("+(currentTime - startTime)+","+x+","+y+","+z+")");
	    if(!noProblemsWitStreams){
	    	final View serverConnectStatus = (View)findViewById(R.id.serverConnectStatus);
			serverConnectStatus.post(new Runnable(){
				public void run(){
					serverConnectStatus.setVisibility(View.VISIBLE);
					TextView tempText = (TextView) serverConnectStatus;
					tempText.setText("Connection is Lost");
				}
			});
			disconnectFromServer();
	    }
	  }
	  
	  public void grantAccessToAccel(View view){
		  boolean on = ((ToggleButton) view).isChecked();
		  if (on) {
			  	grantAccess = true;
			  	
		    } else {
		    	grantAccess = false;
		    }
	  }
	  
	  @Override
	  public void onAccuracyChanged(Sensor sensor, int accuracy) {

	  }
	  
	  @Override
	  protected void onResume() {
	    super.onResume();
	    // register this class as a listener for the orientation and
	    // accelerometer sensors
	    sensorManager.registerListener(this,
		        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
		        SensorManager.SENSOR_DELAY_NORMAL);
	  }
	  
	  @Override
	  protected void onPause() {
	    // unregister listener
	    super.onPause();
	    sensorManager.unregisterListener(this);
	  }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	// switch buttons visibility for connect/disconnect to server buttons
	private void switchClientButtons(final boolean connected){
		final View button1 = (View)findViewById(R.id.button_connect);
		button1.post(new Runnable(){
			public void run(){
				button1.setVisibility(connected ? 8 : 1);
			}
		});
		final View button2 = (View)findViewById(R.id.button_disconnect);
		button2.post(new Runnable(){
			public void run(){
				button2.setVisibility(connected ? 1 : 8);
			}
		});
	}
	
	private void switchToggleVisibility(final boolean makeVisible){
		final ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton1);
		toggle.post(new Runnable(){
			public void run(){
				toggle.setVisibility(makeVisible ? View.VISIBLE : View.GONE);
			}
		});
		
	}
	
	public void connectToServer(final View view){
		final Activity tempThis = this;
		new Thread(new Runnable(){
			public void run(){
				EditText serverName = (EditText)findViewById(R.id.edit_server_name);
				try {
					client = new Client(InetAddress.getByName(serverName.getText().toString()),tempThis);
					if(grantAccess){
						startTime = System.currentTimeMillis();
					}
					final View serverConnectStatus = (View)findViewById(R.id.serverConnectStatus);
					serverConnectStatus.post(new Runnable(){
						public void run(){
							serverConnectStatus.setVisibility(View.VISIBLE);
							TextView tempText = (TextView) serverConnectStatus;
							tempText.setText("Connected to Server");
						}
					});
					
					noProblemsWitStreams = client.sendMessage(Boolean.toString(grantAccess));
					if(!noProblemsWitStreams){
				    	final View serverConnectStatus2 = (View)findViewById(R.id.serverConnectStatus);
				    	serverConnectStatus2.post(new Runnable(){
							public void run(){
								serverConnectStatus2.setVisibility(View.VISIBLE);
								TextView tempText = (TextView) serverConnectStatus2;
								tempText.setText("Connection is Lost");
							}
						});
				    	disconnectFromServer();
				    }
				} catch (UnknownHostException e) {
					//if(BuildConfig.DEBUG)
		        		Log.e(Constants.TAG, "Can't find address of "+serverName.getText().toString()+" server", e);
					disconnectFromServer(view);
					final View serverConnectStatus = (View)findViewById(R.id.serverConnectStatus);
					serverConnectStatus.post(new Runnable(){
						public void run(){
							serverConnectStatus.setVisibility(View.VISIBLE);
							TextView tempText = (TextView) serverConnectStatus;
							tempText.setText("Unable to connect to Specified Server");
						}
					});
				} catch (IOException e) {
					//if(BuildConfig.DEBUG)
		        		Log.e(Constants.TAG, "Data socket wasn't created", e);
					disconnectFromServer(view);
					final View serverConnectStatus = (View)findViewById(R.id.serverConnectStatus);
					serverConnectStatus.post(new Runnable(){
						public void run(){
							serverConnectStatus.setVisibility(View.VISIBLE);
							TextView tempText = (TextView) serverConnectStatus;
							tempText.setText("Unable to connect to Specified Server");
							switchClientButtons(false);
							switchToggleVisibility(true);
						}
					});
				}
				
				switchClientButtons(true);
				switchToggleVisibility(false);
			}
		}).start();
	}
	
	public void disconnectFromServer(View view){
		if(client!=null){
			client.close();
			client = null;
		}
		switchClientButtons(false);
		switchToggleVisibility(true);
		final View serverConnectStatus = (View)findViewById(R.id.serverConnectStatus);
		serverConnectStatus.post(new Runnable(){
			public void run(){
				serverConnectStatus.setVisibility(View.GONE);
				TextView tempText = (TextView) serverConnectStatus;
				tempText.setText("Connected to Server");
			}
		});
	}
	
	public void disconnectFromServer(){
		if(client!=null){
			client.close();
			client = null;
		}
		switchClientButtons(false);
		switchToggleVisibility(true);
	}

	protected void onStop(){
		super.onStop();
		if(client!=null){
			client.close();
		}
	}
}
