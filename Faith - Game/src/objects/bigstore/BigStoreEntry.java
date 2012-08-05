package objects.bigstore;

import objects.item.Item;

/**
 * Contient toutes les informations necessaire sur la vente d'un objet.
 * -Son prix
 * -Sa quantité
 * -Le nombres d'heures depuis la mise en vente
 * -Le propriétaire
 * -Une référence vers l'objet à vendre.
 * @author Mathieu
 *
 */
public class BigStoreEntry implements Comparable<BigStoreEntry>
{
	private int _hdvID;
	private final int _price;
	private final byte _amount;	//Dans le format : 1=1 2=10 3=100
	private final Item _obj;
	private int _ligneID;
	private final int _owner;
	
	public BigStoreEntry(final int price, final byte amount, final int owner, final Item obj)
	{
		this._price = price;
		this._amount = amount;
		this._obj = obj;
		this._owner = owner;
		//TODO : Ajouter le nouvel objet dans la bonne categorie et le bon template
	}

	public void setHdvID(final int id)
	{
		this._hdvID = id;
	}
	public int getHdvID()
	{
		return this._hdvID;
	}
	public int getPrice()
	{
		return this._price;
	}
	public byte getAmount(final boolean parseToRealNumber)
	{
		if(parseToRealNumber)
			return (byte)(Math.pow(10,(double)_amount) / 10);
		else
			return this._amount;
	}
	public Item getObjet()
	{
		return this._obj;
	}
	public int getLigneID()
	{
		return this._ligneID;
	}
	public void setLigneID(final int ID)
	{
		this._ligneID = ID;
	}
	public int getOwner()
	{
		return this._owner;
	}

	public String parseToEL()
	{
		final int count = getAmount(true); //Transfère dans le format (1,10,100) le montant qui etait dans le format (1,2,3)
		return new StringBuilder().append(_ligneID).append(';').append(count).append(';').append(_obj.getTemplate().getID())
		.append(';').append(_obj.parseStatsString()).append(';').append(_price).append(";350").toString();//350 = temps restant
	}
	public String parseToEmK()
	{
		final int count = getAmount(true); //Transfère dans le format (1,10,100) le montant qui etait dans le format (1,2,3)
		return new StringBuilder().append(_ligneID).append('|').append(count).append('|').append(_obj.getTemplate().getID())
		.append('|').append(_obj.parseStatsString()).append('|').append(_price).append("|350").toString();//350 = temps restant
	}
	
	public String parseItem(final char divider)
	{
		final int count = getAmount(true); //Transfère dans le format (1,10,100) le montant qui etait dans le format (1,2,3)
		return new StringBuilder().append(_ligneID).append(divider)
		.append(count).append(divider)
		.append(_obj.getTemplate().getID()).append(divider)
		.append(_obj.parseStatsString()).append(divider)
		.append(_price).append(divider)
		.append("350").toString();//350 = temps restant
	}
	
	public int compareTo(final BigStoreEntry o)
	{
		final BigStoreEntry e = (BigStoreEntry)o;
		final int celuiCi = this.getPrice();
		final int autre = e.getPrice();
		if(autre > celuiCi)
			return -1;
		if(autre == celuiCi)
			return 0;
		if(autre < celuiCi )
			return 1;
		return 0;
	}
}