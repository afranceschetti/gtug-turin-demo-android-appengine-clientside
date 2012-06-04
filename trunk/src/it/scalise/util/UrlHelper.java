package it.scalise.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.util.Log;

public class UrlHelper {
	
	private static final String TAG = "CAMERADEMO";
	
	public static String getContents(String url) {
		String contents ="";

		try {
			URLConnection conn = new URL(url).openConnection();

			InputStream in = conn.getInputStream();
			contents = convertStreamToString(in);
		} catch (MalformedURLException e) {
			Log.d(TAG,"MALFORMED URL EXCEPTION");
		} catch (IOException e) {
			Log.d(TAG,e.getMessage());
		}

		return contents;
	}
	
	private static String convertStreamToString(InputStream is) throws UnsupportedEncodingException {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}
