package objects.bigstore;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Contient des HdvEntry de même template et de même statistiques.
 * Les lignes sont des : ArrayList<HdvEntry>
 * @author Mathieu
 *
 */
public class Line
{
	private final int ligneID;
	private final ArrayList<ArrayList<BigStoreEntry>> _entries = new ArrayList<ArrayList<BigStoreEntry>>(3);	//La première ArrayList est un tableau de 3 (0=1 1=10 2=100 de quantité)
	private final String _strStats;
	private final int templateID;
	
	public Line(final int ligneID, final BigStoreEntry toAdd)
	{
		this.ligneID = ligneID;
		this._strStats = toAdd.getObjet().parseStatsString();
		this.templateID = toAdd.getObjet().getTemplate().getID();
		
		for (int i = 0; i < 3; i++)
		{
			_entries.add(new ArrayList<BigStoreEntry>());	//Boucle 3 fois pour ajouter 3 List vide dans la SuperList
		}
		addEntry(toAdd);
	}
	
	public String getStrStats()
	{
		return this._strStats;
	}
	
	/**
	 * Méthode pour ajouter un HdvEntry à la ligne.
	 * @param toAdd L'objet HdvEntry à ajouter à la ligne
	 * @return Cette fonction retourne false dans le cas où l'objet à ajouter n'a pas les mêmes stats que la ligne. Dans tout les autres cas, elle retourne true.
	 */
	public boolean addEntry(final BigStoreEntry toAdd)
	{
		if(!haveSameStats(toAdd) && !isEmpty())
			return false;
		
		toAdd.setLigneID(this.ligneID);
		final byte index = (byte) (toAdd.getAmount(false) - 1);
		
		_entries.get(index).add(toAdd);
		trier(index);
		
		return true;	//Anonce que l'objet à été accepté
	}
	public boolean haveSameStats(final BigStoreEntry toAdd)
	{
		return _strStats.equalsIgnoreCase(toAdd.getObjet().parseToSave())
				&& toAdd.getObjet().getTemplate().getType() != 85;	//Récupère les stats de l'objet et compare avec ceux de la ligne
	}
	
	public BigStoreEntry doYouHave(final int amount, final int price)
	{
		final int index = amount-1;
		for (int i = 0; i < _entries.get(index).size(); i++) 
		{
			if(_entries.get(index).get(i).getPrice() == price)
				return _entries.get(index).get(i);
		}
		
		
		return null;
	}
	
	public int[] getFirsts()
	{
		final int[] toReturn = new int[3];
		
		for (int i = 0; i < _entries.size(); i++) 
		{
			try{
				toReturn[i] = _entries.get(i).get(0).getPrice();	//Récupère le premier objet de chaque liste
			}catch(final IndexOutOfBoundsException e){toReturn[i] = 0;}
		}
		
		return toReturn;
	}
	public ArrayList<BigStoreEntry> getAll()
	{
		//Additionne le nombre d'objet de chaque quantité
		final int totalSize = _entries.get(0).size() + _entries.get(1).size() + _entries.get(2).size();
		final ArrayList<BigStoreEntry> toReturn = new ArrayList<BigStoreEntry>(totalSize);
		
		for (int qte = 0; qte < _entries.size(); qte++) //Boucler dans les quantité
		{
			toReturn.addAll(_entries.get(qte));
		}
		
		return toReturn;
	}
	public boolean delEntry(final BigStoreEntry toDel)
	{
		final byte index = (byte) (toDel.getAmount(false) - 1);
		
		final boolean toReturn = _entries.get(index).remove(toDel);
		
		trier(index);
		
		return toReturn;
	}
	public BigStoreEntry delEntry(final byte amount)
	{
		final byte index = (byte) (amount -1);
		final BigStoreEntry toReturn = _entries.get(index).remove(0);
		trier(index);
		return toReturn;
	}
	
	public String parseToEHl() {
		final StringBuilder toReturn = new StringBuilder();

		final int[] price = getFirsts();
		toReturn.append(this.ligneID).append(';').append(this._strStats).append(';').append(price[0] == 0 ? "" : Integer.valueOf(price[0])).append(';').append(price[1] == 0 ? "" : Integer.valueOf(price[1])).append(';').append(price[2] == 0 ? "" : Integer.valueOf(price[2]));

		return toReturn.toString();
	}		
	
	public String parseToEHm() {
		final int[] prix = getFirsts();
		final StringBuilder toReturn = new StringBuilder();
		toReturn.append(this.ligneID).append(this._strStats).append('|').append(this.templateID).append('|').append(this._strStats).append('|').append(prix[0] == 0 ? "" : Integer.valueOf(prix[0])).append('|').append(prix[1] == 0 ? "" : Integer.valueOf(prix[1])).append('|').append(prix[2] == 0 ? "" : Integer.valueOf(prix[2]));

		return toReturn.toString();
	}
	
	public void trier(final byte index)
	{
		Collections.sort(_entries.get(index));
	}
	
	public boolean isEmpty()
	{
		for (int i = 0; i < _entries.size(); i++) 
		{
			try
			{
				if(_entries.get(i).get(0) != null)	//Vérifie s'il existe un objet dans chacune des 3 quantité
					return false;
			}catch(final IndexOutOfBoundsException e){}
		}
		
		return true;
	}
}