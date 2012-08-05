package common;

import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.Ansi.Color;

public class Console {
	
	private Console() { 
		super();
	}
	
	public static final Object SINGLETON_LOCK = new Object();
	
	public static void setTitle(final String title)
	{
		synchronized(SINGLETON_LOCK) {
			AnsiConsole.out.append("\033]0;").append(title).append("\007");
		}
	}
	
	public static void print(final String mess, final Color color)
	{
		synchronized(SINGLETON_LOCK) {
			final Color l_color = color;
			AnsiConsole.out.print("\033[" + l_color.fg() + "m" + mess + "\033[0m");
		}
	}
	
	public static void println(final String mess,  final Color color)
	{
		synchronized(SINGLETON_LOCK) {
			final Color l_color = color;
			AnsiConsole.out.println("\033[" + l_color.fg() + "m" + mess + "\033[0m");
		}
	}
	
	public static void clear()
	{ 
		synchronized(SINGLETON_LOCK) {
			AnsiConsole.out.print("\033[H\033[2J");
		}
	}

}
