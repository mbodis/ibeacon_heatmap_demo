package sk.svb.ibeacon.heatmap.support;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;

public class Logger {

		
	public static final String FILE_IBEACON = "log_ibeacon.txt";
	
	public static final String[] ALL_FILES = { FILE_IBEACON };

	/**
	 * loging into files
	 */
	public static void addLog(Context context, String logFileName, String content) {
				
		
		if (context == null)
			return;
		String timeNow = new SimpleDateFormat("yyyy.MM.dd_HH:mm:ss", Locale.US).format(new Date(System
				.currentTimeMillis()));

		(new File(context.getExternalFilesDir(null) + "/log", "")).mkdirs();
		File file = new File(context.getExternalFilesDir(null) + "/log", logFileName);
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(file, true));
			out.write(timeNow + " " + content);
			out.newLine();
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void removeLogFile(Context context, String fileName) {
		if (context == null)
			return;
		File file = new File(context.getExternalFilesDir(null) + "/log", fileName);
		file.delete();

	}

	public static void removeAllLogs(Context context) {
		if (context == null)
			return;
		for (int i = 0; i < ALL_FILES.length; i++) {
			removeLogFile(context, ALL_FILES[i]);
		}
		(new File(context.getFilesDir() + "/log", "")).delete();

	}
}
