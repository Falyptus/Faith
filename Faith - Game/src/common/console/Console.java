package common.console;

import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.Ansi.Color;
import org.fusesource.jansi.internal.Kernel32;

import common.Config;

public final class Console 
{
	private Console() { 
		super();
	}
	
	public static final Object LOCK = new Object();
	
	public static void setTitle(final String title)
	{
		Kernel32.SetConsoleTitle(title);
		//AnsiConsole.out.append("\033]0;").append(title).append("\007");
	}
	
	private static final String INFO = "[INFO] ";
	private static final String ERROR = "[ERROR] ";
	private static final String SUCCESS = "[SUCCESS] ";
	//private static final String DEBUG = "[DEBUG] ";
	
	public static void printError(final String mess)
	{
		print(ERROR+mess, Color.RED);
	}
	
	public static void printDefault(final String mess)
	{
		print(mess, Color.DEFAULT);
	}
	
	public static void printlnDefault(final String mess)
	{
		println(mess, Color.DEFAULT);
	}
	
	public static void printInfo(final String mess)
	{
		println(INFO+mess, Color.BLUE);
	}
	
	public static void printDebug(final String mess)
	{
		if(Config.CONFIG_DEBUG) {
			print(mess, Color.DEFAULT);
		}
	}
	
	public static void printlnInfo(final String mess)
	{
		println(INFO+mess, Color.BLUE);
	}
	
	public static void printlnSuccess(final String mess)
	{
		println(SUCCESS+mess, Color.GREEN);
	}
	
	public static void printlnError(final String mess)
	{
		println(ERROR+mess, Color.RED);
	}
	
	public static void printlnDebug(final String mess)
	{
		if(Config.CONFIG_DEBUG) {
			println(mess, Color.DEFAULT);
		}
	}
	
	public static void print(final String mess, final Color color)
	{
		synchronized(LOCK) {
			final Color l_color = color;
			AnsiConsole.out.print("\033[" + l_color.fg() + "m" + mess + "\033[0m");
		}
	}
	
	public static void println(final String mess,  final Color color)
	{
		synchronized(LOCK) {
			final Color l_color = color;
			AnsiConsole.out.println("\033[" + l_color.fg() + "m" + mess + "\033[0m");
		}
	}
	
	public static void clear()
	{ 
		synchronized(LOCK) {
			AnsiConsole.out.print("\033[H\033[2J");
		}
	}
		
}
