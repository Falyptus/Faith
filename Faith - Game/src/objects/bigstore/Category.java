package objects.bigstore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Contient les liens associant les templatID au Map de template.
 * C'est une manière plus compréhensible d'écrire : <categID,Map<LigneID,Ligne>>.
 * @author Mathieu
 *
 */
public class Category
{
	Map<Integer,Template> _templates = new HashMap<Integer, Template>();	//Dans le format <templateID,Template>
	int categID;
	
	public Category(final int categID)
	{
		this.categID = categID;
	}
	
	public void addEntry(final BigStoreEntry toAdd)
	{
		final int tempID = toAdd.getObjet().getTemplate().getID();
		if(_templates.get(tempID) == null)
			addTemplate(tempID,toAdd);
		else
			_templates.get(tempID).addEntry(toAdd);
	}
	public void addTemplate(final int templateID, final BigStoreEntry toAdd)
	{
		_templates.put(templateID, new Template(templateID, toAdd));
	}
	
	public boolean delEntry(final BigStoreEntry toDel)
	{
		boolean toReturn = false;
		_templates.get(toDel.getObjet().getTemplate().getID()).delEntry(toDel);
		
		if((toReturn = _templates.get(toDel.getObjet().getTemplate().getID()).isEmpty()))
			delTemplate(toDel.getObjet().getTemplate().getID());
		
		return toReturn;
	}
	
	public Template getTemplate(final int templateID)
	{
		return _templates.get(templateID);
	}
	
	public ArrayList<BigStoreEntry> getAllEntry()
	{
		final ArrayList<BigStoreEntry> toReturn = new ArrayList<BigStoreEntry>();
		
		for(final Template curTemp : _templates.values())
		{
			toReturn.addAll(curTemp.getAllEntry());
		}
		return toReturn;
	}
	
	public String parseTemplate()
	{
		boolean isFirst = true;
		final StringBuilder strTemplate = new StringBuilder();
		
		for(final int curTemp : _templates.keySet())
		{
			if(!isFirst)
				strTemplate.append(';');
			
			strTemplate.append(curTemp);
			
			isFirst = false;
		}
		
		return strTemplate.toString();
	}
	
	public void delTemplate(final int templateID)
	{
		_templates.remove(templateID);
	}
}