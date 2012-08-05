package common.console;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Calendar;

public class Log {
	
	public static BufferedWriter logGameSock;
	public static BufferedWriter logGame;
	public static BufferedWriter logRealm;
	public static BufferedWriter logMj;
	public static BufferedWriter logRealmSock;
	public static BufferedWriter logShop;
	public static BufferedWriter logRcon;
	public static boolean canLog;
	
	private final static Object LOCK = new Object();
	
	public static void addToLog(final String str)
	{
		synchronized(LOCK) {
			Console.printlnDefault(str);
			if(canLog)
			{
				try {
					final String date = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+":"+Calendar.getInstance().get(+Calendar.MINUTE)+":"+Calendar.getInstance().get(Calendar.SECOND);
					logGame.write(date+": "+str);
					logGame.newLine();
					logGame.flush();
				} catch (final IOException e) {e.printStackTrace();}//ne devrait pas avoir lieu
			}
		}
	}

	public static void addToErrorLog(final String str)
	{
		synchronized(LOCK) {
			Console.printlnError(str);
			if(canLog)
			{
				try {
					final String date = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+":"+Calendar.getInstance().get(+Calendar.MINUTE)+":"+Calendar.getInstance().get(Calendar.SECOND);
					logGame.write(date+": "+str);
					logGame.newLine();
					logGame.flush();
				} catch (final IOException e) {e.printStackTrace();}//ne devrait pas avoir lieu
			}
		}
	}

	public static void addToSockLog(final String str)
	{
		synchronized(LOCK) {
			Console.printlnDefault(str);
			//if(Ancestra.CONFIG_DEBUG)System.out.println(str);
			if(canLog)
			{
				try {
					final String date = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+":"+Calendar.getInstance().get(+Calendar.MINUTE)+":"+Calendar.getInstance().get(Calendar.SECOND);
					logGameSock.write(date+": "+str);
					logGameSock.newLine();
					logGameSock.flush();
				} catch (final IOException e) {}//ne devrait pas avoir lieu
			}
		}
	}
	
	public static void addToMjLog(final String str)
	{
		synchronized(LOCK) {
			if(!Log.canLog)return;
			final String date = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+":"+Calendar.getInstance().get(+Calendar.MINUTE)+":"+Calendar.getInstance().get(Calendar.SECOND);
			try {
				Log.logMj.write("["+date+"]"+str);
				Log.logMj.newLine();
				Log.logMj.flush();
			} catch (final IOException e) {}
		}
	}
	
	public static void addToShopLog(final String str)
	{
		synchronized(LOCK) {
			if(!Log.canLog)return;
			final String date = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+":"+Calendar.getInstance().get(+Calendar.MINUTE)+":"+Calendar.getInstance().get(Calendar.SECOND);
			try {
				Log.logShop.write("["+date+"]"+str);
				Log.logShop.newLine();
				Log.logShop.flush();
			} catch (final IOException e) {}
		}
	}
	
	public static void addToRconLog(final String str)
	{
		synchronized(LOCK) {
			if(!Log.canLog)return;
			final String date = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+":"+Calendar.getInstance().get(+Calendar.MINUTE)+":"+Calendar.getInstance().get(Calendar.SECOND);
			try {
				Log.logRcon.write("["+date+"]"+str);
				Log.logRcon.newLine();
				Log.logRcon.flush();
			} catch (final IOException e) {}
		}
	}	

}
