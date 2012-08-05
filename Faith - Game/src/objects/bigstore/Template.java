package objects.bigstore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import common.World;

/**
 * Contient les liens associant les ID des Lignes à des objets "Ligne".
 * C'est une manière plus compréhensible d'écrire : <LigneID,Ligne>.
 * @author Mathieu
 *
 */
public class Template
{
	int templateID;
	Map<Integer, Line> _lignes = new HashMap<Integer, Line>();
	
	public Template(final int templateID, final BigStoreEntry toAdd)
	{
		this.templateID = templateID;
		
		addEntry(toAdd);
	}
	
	public void addEntry(final BigStoreEntry toAdd)
	{
		//TODO : Peut-être catché un nullPointerException à cause du for
		for(final Line curLine : _lignes.values())	//Boucle dans toutes les lignes pour essayer de trouver des objets de mêmes stats
		{
			if(curLine.addEntry(toAdd))	//Si une ligne l'accepte, arrête la méthode.
				return;
		}

		//Si aucune ligne ne l'a accepté, crée une nouvelle ligne.
		final int ligneID = World.getNextLigneID();
		_lignes.put(ligneID, new Line(ligneID, toAdd));
	}
	public Line getLigne(final int ligneID)
	{
		return _lignes.get(ligneID);
	}
	
	public boolean delEntry(final BigStoreEntry toDel)
	{
		final boolean toReturn =  _lignes.get(toDel.getLigneID()).delEntry(toDel);
		if(_lignes.get(toDel.getLigneID()).isEmpty())	//Si la ligne est devenue vide
		{
			_lignes.remove(toDel.getLigneID());
		}
		
		return toReturn;
	}
	
	public ArrayList<BigStoreEntry> getAllEntry()
	{
		final ArrayList<BigStoreEntry> toReturn = new ArrayList<BigStoreEntry>();
		
		for(final Line curLine : _lignes.values())
		{
			toReturn.addAll(curLine.getAll());
		}
		return toReturn;
	}
	
	public String parseToEHl()
	{
		final StringBuilder toReturn = new StringBuilder();
		toReturn.append(templateID).append('|');
		
		boolean isFirst = true;
		for (final Line curLine : _lignes.values())
		{
			if(!isFirst)
				toReturn.append('|');
				
			toReturn.append(curLine.parseToEHl());
			
			isFirst = false;
		}
		return toReturn.toString();
	}
	
	public boolean isEmpty()
	{
		if(_lignes.size() == 0)
			return true;
		
		return false;
	}
}