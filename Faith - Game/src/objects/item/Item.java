package objects.item;


import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import objects.character.Player;
import objects.character.Stats;
import objects.spell.SpellEffect;

import common.Constants;
import common.SQLManager;
import common.SocketManager;
import common.World;
import common.utils.Formulas;

public class Item {

	protected ItemTemplate template;
	protected int quantity = 1;
	protected int position = Constants.ITEM_POS_NO_EQUIPED;
	protected int guid;
	private Stats Stats = new Stats();
	private ArrayList<SpellEffect> Effects = new ArrayList<SpellEffect>();
	private final Map<Integer,String> txtStats = new TreeMap<Integer,String>();
	private final Map<Integer, String> StaticStates = new TreeMap<Integer, String>();
	private boolean isExchangeable = true;
	protected boolean isSpeaking = false;
	protected boolean isPet = false;
	private int linkedItem_id = -1;
	private Speaking linkedItem = null;
	private boolean isLinked = false;
	
	public Item (final int Guid, final int template,final int qua, final int pos, final String strStats)
	{
		this.guid = Guid;
		this.template = World.getItemTemplate(template);
		this.quantity = qua;
		this.position = pos;
		
		Stats = new Stats();
		parseStringToStats(strStats);
	}
	public Item()
	{
		
	}
	
	public void parseStringToStats(final String strStats)
	{
		final String[] split = strStats.split(",");
		for(final String s : split)
		{	
			try
			{
				final String[] stats = s.split("#");
				final int statID = Integer.parseInt(stats[0],16);
				
				if (statID == Constants.EFFECT_RECEIVED_DATE) {
					txtStats.put(statID, (new StringBuilder()).append(stats[1]).append('#').append(stats[2]).append('#').append(stats[3]).toString());
					continue;
				}
				//Stats spécials
				if(statID == 997 || statID == 996)
				{
					txtStats.put(statID, stats[4]);
					continue;
				}
				
				if (isSpecialStat(statID)) {
					continue;
				}

				//Si stats avec Texte (Signature, apartenance, etc)
				if((!stats[3].equals("") && !stats[3].equals("0")))
				{
					txtStats.put(statID, stats[3]);
					continue;
				}
				
				final String jet = stats[4];
				boolean follow = true;
				for(final int a : Constants.ARMES_EFFECT_IDS)
				{
					if(a == statID)
					{
						final int id = statID;
						final String min = stats[1];
						final String max = stats[2];
						final StringBuilder args = new StringBuilder(min.length()+max.length()+jet.length()+7);
						args.append(min);
						args.append(';');
						args.append(max);
						args.append(';').append(-1).append(';').append(-1).append(';').append(0).append(';');
						args.append(jet);
						Effects.add(new SpellEffect(id, args.toString(),0,-1));
						follow = false;
					}
				}
				if(!follow)continue;//Si c'était un effet Actif d'arme ou une signature
				final int value = Integer.parseInt(stats[1],16);
				Stats.addOneStat(statID, value);
			}catch(final Exception e){continue;};
		}
	}

	public void addTxtStat(final int i,final String s)
	{
		txtStats.put(i, s);
	}
	
	public boolean isSpecialStat(final int statID) {
		boolean isSpecial = false;

		if (statID == 970
				|| statID == 971
				|| statID == 972
				|| statID == 973
				|| statID == 974
				|| statID == 983
				|| statID == 808
				|| statID == 805
				|| statID == 800
				|| statID == 997
				|| statID == 996) {
			isSpecial = true;
		}

		return isSpecial;
	}
	
	public Item(final int Guid, final int template, final int qua, final int pos,	final Stats stats,final ArrayList<SpellEffect> effects)
	{
		this.guid = Guid;
		this.template = World.getItemTemplate(template);
		this.quantity = qua;
		this.position = pos;
		this.Stats = stats;
		this.Effects = effects;
	}
	
	public Stats getStats() {
		return Stats;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(final int quantity) {
		this.quantity = quantity;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(final int position) {
		this.position = position;
	}

	public ItemTemplate getTemplate() {
		return template;
	}
	
	public void setTemplate(final ItemTemplate template) {
	    this.template = template;
    }

	public int getGuid() {
		return guid;
	}
	
	public String parseItem()
	{	
		if (isSpeaking() && Speaking.toSpeaking(this).hasLinkedItem())
			return "";
		final String posi = position==Constants.ITEM_POS_NO_EQUIPED?"":Integer.toHexString(position);
		return (new StringBuilder(40).append(Integer.toHexString(guid)).append('~').append(Integer.toHexString(template.getID())).append('~').append(Integer.toHexString(quantity)).append('~').append(posi).append('~').append(parseStatsString()).append(';')).toString();
	}

	public String parseStatsString()
	{
		if(getTemplate().getType() == 83)	//Si c'est une pierre d'âme vide
			return getTemplate().getStrTemplate();
		
		final StringBuilder stats = new StringBuilder();
		boolean isFirst = true;
		for(final SpellEffect SE : Effects)
		{
			if(!isFirst)
				stats.append(',');
			
			final String[] infos = SE.getArgs().split(";");
			try
			{
				stats.append(Integer.toHexString(SE.getEffectID())).append('#').append(infos[0]).append('#').append(infos[1]).append("#0#").append(infos[5]);
			}catch(final Exception e)
			{
				e.printStackTrace();
				continue;
			}
			
			isFirst = false;
		}
		
		for(final Entry<Integer,Integer> entry : Stats.getMap().entrySet())
		{
			if(!isFirst)stats.append(',');
			final String jet = "0d0+"+entry.getValue();
			stats.append(Integer.toHexString(entry.getKey())).append('#').append(Integer.toHexString(entry.getValue())).append("#0#0#").append(jet);
			isFirst = false;
		}
		
		for(final Entry<Integer,String> entry : txtStats.entrySet())
		{
			if(!isFirst)stats.append(',');
			if(entry.getKey() == Constants.CAPTURE_MONSTRE)
				stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(entry.getValue());
			else if (entry.getKey() == Constants.EFFECT_RECEIVED_DATE)
				stats.append(Integer.toHexString(entry.getKey())).append('#').append(entry.getValue());
			else
				stats.append(Integer.toHexString(entry.getKey())).append("#0#0#0#").append(entry.getValue());
			isFirst = false;
		}
		
		if (isSpeaking()) {
			if (!isFirst)
				stats.append(',');
			stats.append(Speaking.toSpeaking(this).toString());
			isFirst = false;
		}
		if (is_linked()) {
			if (!isFirst)
				stats.append(',');
			stats.append(linkedItem.toString());
			isFirst = false;
			for (final Entry<Integer, String> entry : linkedItem.getTxtStat().entrySet()) {
				if (!isFirst) 
					stats.append(',');
				if (entry.getKey() == Constants.CAPTURE_MONSTRE)
					stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(entry.getValue());
				else if (entry.getKey() == Constants.EFFECT_RECEIVED_DATE)
					stats.append(Integer.toHexString(entry.getKey())).append('#').append(entry.getValue());
				else
					stats.append(Integer.toHexString(entry.getKey())).append("#0#0#0#").append(entry.getValue());
				isFirst = false;
			}
		}
		return stats.toString();
	}
	
	public String parseStatsStringToSave() {
		if(getTemplate().getType() == 83)	//Si c'est une pierre d'âme vide
			return getTemplate().getStrTemplate();
		
		final StringBuilder stats = new StringBuilder();
		boolean isFirst = true;
		for(final SpellEffect SE : Effects)
		{
			if(!isFirst)
				stats.append(",");
			
			final String[] infos = SE.getArgs().split(";");
			try
			{
				stats.append(Integer.toHexString(SE.getEffectID())).append("#").append(infos[0]).append("#").append(infos[1]).append("#0#").append(infos[5]);
			}catch(final Exception e)
			{
				e.printStackTrace();
				continue;
			};
			
			isFirst = false;
		}
		
		for(final Entry<Integer,Integer> entry : Stats.getMap().entrySet())
		{
			if(!isFirst)stats.append(',');
			final String jet = "0d0+"+entry.getValue();
			stats.append(Integer.toHexString(entry.getKey())).append('#').append(Integer.toHexString(entry.getValue())).append("#0#0#").append(jet);
			isFirst = false;
		}
		
		for(final Entry<Integer,String> entry : txtStats.entrySet())
		{
			if(!isFirst)stats.append(",");
			if(entry.getKey() == Constants.CAPTURE_MONSTRE)
				stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(entry.getValue());
			else if (entry.getKey() == Constants.EFFECT_RECEIVED_DATE)
				stats.append(Integer.toHexString(entry.getKey())).append('#').append(entry.getValue());
			else
				stats.append(Integer.toHexString(entry.getKey())).append("#0#0#0#").append(entry.getValue());
			isFirst = false;
		}
		return stats.toString();
	}

	public String parseToSave()
	{
		return parseStatsStringToSave();
	}
	
	public ArrayList<SpellEffect> getEffects()
	{
		return Effects;
	}

	public ArrayList<SpellEffect> getCritEffects()
	{
		final ArrayList<SpellEffect> effets = new ArrayList<SpellEffect>();
		for(final SpellEffect SE : Effects)
		{
			try
			{
				boolean boost = true;
				for(final int i : Constants.NO_BOOST_CC_IDS)if(i == SE.getEffectID())boost = false;
				final String[] infos = SE.getArgs().split(";");
				if(!boost)
				{
					effets.add(SE);
					continue;
				}
				final int min = Integer.parseInt(infos[0],16)+ (boost?template.getBonusCC():0);
				final int max = Integer.parseInt(infos[1],16)+ (boost?template.getBonusCC():0);
				final String jet = "1d"+(max-min+1)+"+"+(min-1);
				//exCode: String newArgs = Integer.toHexString(min)+";"+Integer.toHexString(max)+";-1;-1;0;"+jet;
				//osef du minMax, vu qu'on se sert du jet pour calculer les dégats
				final String newArgs = "0;0;0;-1;0;"+jet;
				effets.add(new SpellEffect(SE.getEffectID(),newArgs,0,-1));
			}catch(final Exception e){continue;};
		}
		return effets;
	}

	public static Item getCloneObjet(final Item obj,final int qua)
	{
		Item ob = null;		
		final int nID = World.getNewItemGuid();
		final int type = obj.getTemplate().getType();
		if (type == Constants.ITEM_TYPE_OBJET_VIVANT) {
			ob = Speaking.createSpeakingItem(nID, obj.getTemplate().getID(), qua, Constants.ITEM_POS_NO_EQUIPED, obj.getStats(), obj.getEffects());
			//World.addObjet(ob, true);
		} else {
			ob = new Item(nID, obj.getTemplate().getID(), qua, Constants.ITEM_POS_NO_EQUIPED, obj.getStats(), obj.getEffects());
		}
		return ob;
	}
	
	public boolean is_linked() {
		return this.isLinked;
	}

	public Speaking get_linkedItem() {
		return this.linkedItem;
	}

	public void set_linkedItem_id(final int ID) {
		this.linkedItem_id = ID;
	}

	public int get_linkedItem_id() {
		return this.linkedItem_id;
	}

	public void setLinkedItem(final Speaking obj) {
		this.linkedItem_id = obj.getGuid();
		this.linkedItem = obj;
		this.isLinked = true;
	}

	public void setUnlinkedItem() {
		this.linkedItem_id = -1;
		this.isLinked = false;
		this.linkedItem = null;
	}

	public void clearStats()
	{
		//On vide l'item de tous ces effets
		Stats = new Stats();
		Effects.clear();
		txtStats.clear();
		StaticStates.clear();
	}
	
	public boolean decreaseQuantity(final Player p, final int qua) {
		if (this.quantity - qua < 0)
			return false;
		if (this.quantity - qua == 0) {
			p.removeItem(this.guid);
			World.removeItem(this.guid);
			SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(p, this.guid);
		} else {
			this.quantity -= qua;
			SQLManager.SAVE_ITEM(this);
			SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(p, this);
		}
		return true;
	}
	
	public Map<Integer, String> getTxtStat()
	{
		return txtStats;
	}

	public void setExchangeable(final boolean isExchangeable) {
		this.isExchangeable = isExchangeable;
	}

	public boolean isExchangeable() {
		return this.isExchangeable;
	}

	public boolean isSpeaking() {
		return this.isSpeaking;
	}

	public boolean isPet() {
		return this.isPet;
	}
	public static int getPoidsOfActualItem(final String statsTemplate) {
		int poid = 0;
		int somme = 0;
		final String[] splitted = statsTemplate.split(",");
		for (final String s : splitted) {
			final String[] stats = s.split("#");
			final int statID = Integer.parseInt(stats[0], 16);
			boolean follow = true;

			for (final int a : Constants.ARMES_EFFECT_IDS)//Si c'est un Effet Actif
			{
				if (a == statID) {
					follow = false;
				}
			}
			if (!follow) {
				continue;//Si c'était un effet Actif d'arme
			}
			String jet = "";
			int value = 1;
			try {
				jet = stats[4];
				value = Formulas.getRandomJet(jet);
				try {
					//on prend le jet max
					final int min = Integer.parseInt(stats[1], 16);
					final int max = Integer.parseInt(stats[2], 16);
					value = min;
					if (max != 0) {
						value = max;
					}
				} catch (final Exception e) {
					value = Formulas.getRandomJet(jet);
				};
			} catch (final Exception e) {
			};

			int multi = 1;
			if (statID == 118 || statID == 126 || statID == 125 || statID == 119 || statID == 123 || statID == 158 || statID == 174)//Force,Intel,Vita,Agi,Chance,Pod,Initiative
			{
				multi = 1;
			} else if (statID == 138 || statID == 666 || statID == 226 || statID == 220)//Domages %,Domage renvoyï¿½,Piï¿½ge %
			{
				multi = 2;
			} else if (statID == 124 || statID == 176)//Sagesse,Prospec
			{
				multi = 3;
			} else if (statID == 240 || statID == 241 || statID == 242 || statID == 243 || statID == 244)//Rï¿½ Feu, Air, Eau, Terre, Neutre
			{
				multi = 4;
			} else if (statID == 210 || statID == 211 || statID == 212 || statID == 213 || statID == 214)//Rï¿½ % Feu, Air, Eau, Terre, Neutre
			{
				multi = 5;
			} else if (statID == 225)//Piï¿½ge
			{
				multi = 15;
			} else if (statID == 178 || statID == 112)//Soins,Dommage
			{
				multi = 20;
			} else if (statID == 115 || statID == 182)//Cri,Invoc
			{
				multi = 30;
			} else if (statID == 117)//PO
			{
				multi = 50;
			} else if (statID == 128)//PM
			{
				multi = 90;
			} else if (statID == 111)//PA
			{
				multi = 100;
			}
			poid = value * multi; //poid de la carac
			somme += poid;
		}
		return somme;
    }
	
	/*public Map<Integer,Integer> getMinMaxStats(boolean max)
	{
		Stats itemStats = new Stats(false, null);
		String statsTemplate = this.template.getStrTemplate();
		//Si stats Vides
		if(statsTemplate.equals("") || statsTemplate == null) return null;
		
		String[] splitted = statsTemplate.split(",");
		for(String s : splitted)
		{	
			String[] stats = s.split("#");
			int statID = Integer.parseInt(stats[0],16);
			boolean follow = true;
			
			for(int a : Constants.ARMES_EFFECT_IDS)//Si c'est un Effet Actif
				if(a == statID)
					follow = false;
			if(!follow)continue;//Si c'était un effet Actif d'arme
			
			String jet = "";
			int value  = 1;
			try
			{
				value = Integer.parseInt(stats[(max?2:1)],16);	//1min, 2max
			}catch(Exception e){System.out.println(e.getMessage()); e.printStackTrace();};
			itemStats.addOneStat(statID, value);
		}
		return itemStats.getMap();
	}*/
	
	
}
