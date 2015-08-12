package com.example.tesis.v3;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.android.gms.maps.model.UrlTileProvider;

public class mySeisImgUrlTileProvider extends UrlTileProvider {

	public mySeisImgUrlTileProvider() {
		super(256, 256);
	}

	@Override
	public URL getTileUrl(int x, int y, int zoom) {
		try {
//			if(ConstantVariables.isConnected(ctx)){
//				int speed = ConstantVariables.getWifiConnectionSpeed(ctx);
//				Log.d("myTag","Connection speed:"+speed);
////				Toast.makeText(ctx,"Connection speed:"+speed,Toast.LENGTH_SHORT).show();
//			}
			URL url = new URL("http://tesis.earth.sinica.edu.tw/testimage/seisMapTiles35_0_50/"+x+"-"+y+"-"+zoom+".png");
//			Log.d("myTag","URL:"+url);
			return url;
		} catch (MalformedURLException e) {
//			Toast.makeText(ctx,"",Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		return null;
	}

}
