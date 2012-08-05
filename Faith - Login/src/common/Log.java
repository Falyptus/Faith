package common;

import java.io.BufferedWriter;
import java.util.Calendar;

public class Log {

	public static boolean LOGIN_LOG = true;
	public static BufferedWriter logLogin;
	public static BufferedWriter logLoginSock;
	public static BufferedWriter logErrors;
	
	public static Object SINGLETON_LOCK = new Object();
	
	public static void addToErrorLog(final String str)
	{
		synchronized(SINGLETON_LOCK) {
			try {
				final String date = Calendar.HOUR_OF_DAY+":"+Calendar.MINUTE+":"+Calendar.SECOND;
				logErrors.write(date+": "+str);
				logErrors.newLine();
				logErrors.flush();
			} catch (final Exception e) {}
		}
	}
	public static void addToLoginLog(final String str) 
	{
		synchronized(SINGLETON_LOCK) {
			try {
				final String date = Calendar.HOUR_OF_DAY + ":" + Calendar.MINUTE+ ":" + Calendar.SECOND;
				logLogin.write(date + ": " + str);
				logLogin.newLine();
				logLogin.flush();
			} catch (final Exception e) {}
		}
	}
	public static void addToLoginSockLog(final String str) 
	{
		//System.out.println(str);
		synchronized(SINGLETON_LOCK) {
			try {
				final String date = Calendar.HOUR_OF_DAY+":"+Calendar.MINUTE+":"+Calendar.SECOND;
				logLoginSock.write(date + ": " + str);
				logLoginSock.newLine();
				logLoginSock.flush();
			} catch (final Exception e) {}
		}
	}
	public static void addToLinkLog(String str) {
		System.out.println(str);
		/*
		 * 
		 */
	}

}
