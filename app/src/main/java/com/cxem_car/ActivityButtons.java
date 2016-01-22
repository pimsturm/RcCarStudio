package com.cxem_car;

import java.lang.ref.WeakReference;

import com.cxem_car.cBluetooth;
import com.cxem_car.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ActivityButtons extends Activity {
	
	private cBluetooth bl = null;
	private ToggleButton LightButton;
	
	private Button btn_forward, btn_backward, btn_left, btn_right;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_buttons);

		Log.d("Buttons", "OnCreate");
        ArduinoCommunicator.SetupChannel();
		
		btn_forward = (Button) findViewById(R.id.forward);
		btn_backward = (Button) findViewById(R.id.backward);
		btn_left = (Button) findViewById(R.id.left);
		btn_right = (Button) findViewById(R.id.right);
		       
		btn_forward.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_MOVE) {
                    ArduinoCommunicator.MotorForward();
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ArduinoCommunicator.MotorStop();
		        }
				return false;
		    }
		});
		
		btn_left.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_MOVE) {
                    ArduinoCommunicator.MotorToLeft();
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ArduinoCommunicator.MotorStop();
		        }
				return false;
		    }
		});
		
		btn_right.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_MOVE) {
                    ArduinoCommunicator.MotorToRight();
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ArduinoCommunicator.MotorStop();
		        }
				return false;
		    }
		});
		
		btn_backward.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_MOVE) {
                    ArduinoCommunicator.MotorBackward();
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ArduinoCommunicator.MotorStop();
		        }
				return false;
		    }
		});
		
		LightButton = (ToggleButton) findViewById(R.id.LightButton);   
		LightButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(LightButton.isChecked()){
                    ArduinoCommunicator.SwitchLightOn();
	    		}else{
                    ArduinoCommunicator.SwitchLightOff();
	    		}
	    	}
	    });
		

	}
		
    private void loadPref(){
    	SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);  
	}
    
    @Override
    protected void onResume() {
    	super.onResume();
    }

    @Override
    protected void onPause() {
    	super.onPause();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	loadPref();
    }
}
