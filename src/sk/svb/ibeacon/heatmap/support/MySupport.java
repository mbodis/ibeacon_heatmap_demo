package sk.svb.ibeacon.heatmap.support;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * some static methods
 * @author mbodis
 *
 */
public class MySupport {

	/**
	 * get contetent form file in resourecs-raw
	 */
	public static String loadFile(Resources res, int id) {
		String result = "";
		try {
			String UTF = "utf8";
			// int BUFFER_SIZE = 8192;
			String str;
			StringBuffer sb = new StringBuffer();

			InputStream is = res.openRawResource(id);
			InputStreamReader in = new InputStreamReader(is, UTF);
			BufferedReader reader = new BufferedReader(in);

			while ((str = reader.readLine()) != null) {
				sb.append(str);
			}

			result = sb.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * http://stackoverflow.com/questions/4837715/how-to-resize-a-bitmap-in-
	 * android
	 * 
	 * @param bm
	 * @param newHeight
	 * @param newWidth
	 * @return
	 */
	public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// CREATE A MATRIX FOR THE MANIPULATION
		Matrix matrix = new Matrix();
		// RESIZE THE BIT MAP
		matrix.postScale(scaleWidth, scaleHeight);

		// "RECREATE" THE NEW BITMAP
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
				matrix, false);
		return resizedBitmap;
	}

}
