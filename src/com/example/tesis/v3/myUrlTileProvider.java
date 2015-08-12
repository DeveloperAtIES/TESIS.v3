package com.example.tesis.v3;

import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.UrlTileProvider;

public class myUrlTileProvider extends UrlTileProvider {

	final String dir = "http://tesis.earth.sinica.edu.tw/testimage/imageAll/";
	
	Context ctx;
	
//	public myUrlTileProvider() {
//		super(256, 256);
//	}
	
	public myUrlTileProvider(Context ctx) {
		super(256, 256);
		this.ctx = ctx;
	}

	@Override
	public URL getTileUrl(int x, int y, int zoom) {
		try {
//			if(ConstantVariables.isConnected(ctx)){
//				int speed = ConstantVariables.getWifiConnectionSpeed(ctx);
//				Log.d("myTag","Connection speed:"+speed);
////				Toast.makeText(ctx,"Connection speed:"+speed,Toast.LENGTH_SHORT).show();
//			}
			URL url = new URL("http://tesis.earth.sinica.edu.tw/testimage/imageAll/"+x+"-"+y+"-"+zoom+".png");
//			Log.d("myTag","URL:"+url);
			return url;
		} catch (MalformedURLException e) {
//			Toast.makeText(ctx,"",Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		return null;
	}

}
