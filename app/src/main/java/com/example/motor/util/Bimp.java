package com.example.motor.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Bimp {

	public static int max = 0;
	public static ArrayList<ImageItem> tempSelectBitmap = new ArrayList<ImageItem>();

	public static Bitmap revitionImageSize(String path) throws IOException {// revision 修订、修改、校对
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(new File(path)));
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(in,null, options);
		in.close();
		int i = 0;
		Bitmap bitmap = null;
		while (true) {
			if ((options.outWidth >> i <= 1000) && (options.outHeight >> i <= 1000)) {
				in = new BufferedInputStream(new FileInputStream(new File(path)));
				options.inSampleSize = (int) Math.pow(2.0D, i);//Math.pow(底数, 几次方)
				options.inJustDecodeBounds = false;
				bitmap = BitmapFactory.decodeStream(in,null, options);
				break;
			}
			i += 1;
		}
		return bitmap;
	}
}
