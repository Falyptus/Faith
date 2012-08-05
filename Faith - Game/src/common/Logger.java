package common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import common.console.Log;

/**
 * 
 * Class de gestion d'�criture de logs dans un fichier
 * @author Mathieu optimized by Keal
 */
public class Logger{
	
	transient private BufferedWriter out;
	
	private ArrayList<String> toWrite;
	transient private int bufferSize;
	
	/**
	 * 
	 * @param filePath Chemin d'acc�s relatif ou absolue du fichier o� �crire les logs.
	 */
	public Logger(final String filePath, final int bufferSize)
	{
		if(!Log.canLog) {
			return;
		}
		
		final File fichier = new File(filePath);
		
		try
		{
			final FileWriter tmpWriter = new FileWriter(fichier,true);
			out = new BufferedWriter(tmpWriter);
		} catch (final IOException e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		toWrite = new ArrayList<String>();
		
		setBufferSize(bufferSize);
	}
	
	/**
	 * Ajoute une String dans le buffer. Elle seras �crite lorsque le buffer seras plein ou � l'appel de la
	 * fonction "write()".
	 * @param toAdd Chaine de caract�re � placer dans le buffer en vue d'une �criture.
	 */
	public void toWrite(final String toAdd)
	{	
		final String l_toAdd = toAdd;
		if(!Log.canLog || out == null) {
			return;
		}

		final String date = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+":"+Calendar.getInstance().get(+Calendar.MINUTE)+":"+Calendar.getInstance().get(Calendar.SECOND);
		toWrite.add(date + ": " + l_toAdd);
		if(toWrite.size() >= bufferSize) {
			write();
		}
	}
	
	/**
	 * Vide le buffer en �crivant tout son contenue dans le fichier de sortie.
	 */
	public void write()
	{
		if(!Log.canLog || out == null) {
			return;
		}
		
		try {
			for(final String curStr : toWrite)
			{
				out.write(curStr);
				out.newLine();
			}
			out.flush();
			toWrite.clear();
		} catch (final IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * �crit le contenue du buffer par un appel � la fonction "write()" et ferme le flux de sortie par la suite.
	 */
	public void close()
	{
		try {
			write();
			if(out != null) {
				out.close();
			}
			out = null;
		} catch (final IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 *Place une chaine de retour � la ligne dans le buffer. 
	 * 
	 */
	public void newLine()
	{	
		if(!Log.canLog || out == null) {
			return;
		}

		toWrite.add("\r\n");
		
		if(toWrite.size() >= bufferSize) {
			write();
		}
	}
	
	/**
	 * D�finit la taille du buffer. Elle influence le temps entre deux phase d'�criture dans le fichier de sortie.
	 * Une taille plus petite r�sulte d'une �criture fr�quente mais plus rapide.
	 * Une taille plus grande r�sulte d'une �criture plus rare mais plus longue.
	 * 
	 * @param newSize La nouvelle taille du buffer. Si c'est une valeur insens� (<= 0), la valeur par d�faut (20) seras appliqu�.
	 */
	public void setBufferSize(final int newSize)
	{
		final int l_newSize = newSize;
		
		if(bufferSize <= 0) {
			bufferSize = 20;
		}
		
		this.bufferSize = l_newSize;
	}
	
}