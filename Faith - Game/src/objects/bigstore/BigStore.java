package objects.bigstore;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import objects.character.Player;

import common.SQLManager;
import common.SocketManager;
import common.World;
import common.World.Couple;

public class BigStore {
	private final int _hdvID;
	private final float _taxe;
	private short _sellTime;
	private final short _maxCompteItem;
	private final String _strCategories;
	private final short _lvlMax;
	
	private final Map<Integer,Category> _categories = new HashMap<Integer, Category>();
	private final Map<Integer,Couple<Integer, Integer>> _path = new HashMap<Integer, Couple<Integer,Integer>>();	//<LigneID,<CategID,TemplateID>>
	
	private final DecimalFormat pattern = new DecimalFormat("0.0"); 
	
	public BigStore(final int hdvID, final float taxe, final short sellTime, final short maxItemCompte,final short lvlMax, final String categories)
	{
		this._hdvID = hdvID;
		this._taxe = taxe;
		this._maxCompteItem = maxItemCompte;
		this._strCategories = categories;
		this._lvlMax = lvlMax;
		int categID;
		for(final String strCategID : categories.split(","))
		{
			categID = Integer.parseInt(strCategID);
			_categories.put(categID, new Category(categID));
		}
	}
	
	public int getHdvID()
	{
		return this._hdvID;
	}
	public float getTaxe()
	{
		return this._taxe;
	}
	public short getSellTime()
	{
		return this._sellTime;
	}
	public short getMaxItemCompte()
	{
		return this._maxCompteItem;
	}
	public String getStrCategories()
	{
		return this._strCategories;
	}
	public short getLvlMax()
	{
		return this._lvlMax;
	}
	
	public String parseToEHl(final int templateID)
	{
		final int type = World.getItemTemplate(templateID).getType();
		
		return _categories.get(type).getTemplate(templateID).parseToEHl();
	}
	public String parseTemplate(final int categID)
	{
		return _categories.get(categID).parseTemplate();
	}
	public String parseTaxe()
	{
		return pattern.format(_taxe).replace(",", ".");
	}
	
	public Line getLigne(final int ligneID)
	{
		try
		{
			final int categ = _path.get(ligneID).first;
			final int template = _path.get(ligneID).second;
			
			return _categories.get(categ).getTemplate(template).getLigne(ligneID);
		}
		catch(final NullPointerException e)
		{
			return null;
		}
	}
	
	public ArrayList<BigStoreEntry> getAllEntry()
	{
		final ArrayList<BigStoreEntry> toReturn = new ArrayList<BigStoreEntry>();
		for(final Category curCat : _categories.values())
		{
			toReturn.addAll(curCat.getAllEntry());
		}
		
		return toReturn;
	}
	
	public void addEntry(final BigStoreEntry toAdd)
	{
		toAdd.setHdvID(this._hdvID);
		final int categ = toAdd.getObjet().getTemplate().getType();
		final int template = toAdd.getObjet().getTemplate().getID();
		_categories.get(categ).addEntry(toAdd);
		_path.put(toAdd.getLigneID(), new Couple<Integer, Integer>(categ, template));
		
		World.addHdvItem(toAdd.getOwner(), _hdvID, toAdd);		
	}
	public boolean delEntry(final BigStoreEntry toDel)
	{
		final boolean toReturn =  _categories.get(toDel.getObjet().getTemplate().getType()).delEntry(toDel);
		if(toReturn)
		{
			_path.remove(toDel.getLigneID());
			World.removeHdvItem(toDel.getOwner(), toDel.getHdvID(), toDel);
		}
		
		return toReturn;
	}
	
	public synchronized boolean buyItem(final int ligneID,final byte amount, final int price, final Player newOwner)
	{
		boolean toReturn = true;
		
		try
		{
			if(newOwner.getKamas() < price)
				return false;
			
			final Line ligne = getLigne(ligneID);
			
			BigStoreEntry toBuy = ligne.doYouHave(amount, price);
			
			newOwner.addKamas(price * -1);	//Retire l'argent à l'acheteur (prix et taxe de vente)
			newOwner.kamasLog(-price+"", "Achat d'un objet dans un HDV");
			
			World.addKamasToAcc(toBuy.getOwner(),toBuy.getPrice());	//Ajoute l'argent au vendeur
			SocketManager.GAME_SEND_STATS_PACKET(newOwner);	//Met a jour les kamas de l'acheteur
			
			newOwner.addItem(toBuy.getObjet(), true);	//Ajoute l'objet au nouveau propriétaire
			newOwner.itemLog(toBuy.getObjet().getTemplate().getID(), toBuy.getAmount(true), "Acheté dans un HDV");
			toBuy.getObjet().getTemplate().newSold(toBuy.getAmount(true),price);	//Ajoute la ventes au statistiques
			
			delEntry(toBuy); //Retire l'item de l'HDV ainsi que de la liste du vendeur
			
			if(World.getAccount(toBuy.getOwner()) != null && World.getAccount(toBuy.getOwner()).getCurPerso() != null)
				SocketManager.GAME_SEND_Im_PACKET(World.getAccount(toBuy.getOwner()).getCurPerso(),
						"065;"+price+"~"+
						toBuy.getObjet().getTemplate().getID()+"~"+
						toBuy.getObjet().getTemplate().getID()+"~1");	//Si le vendeur est connecter, envoie du packet qui lui annonce la vente de son objet
			
			if(toBuy.getOwner() == -1)
			{
				SQLManager.SAVE_ITEM(toBuy.getObjet());
			}
			toBuy = null;
		}
		catch(final NullPointerException e)
		{
			toReturn = false;
		}
		
		return toReturn;
	}
}
