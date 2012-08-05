package objects.item;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import objects.action.Action;
import objects.character.Player;
import objects.character.Stats;
import objects.spell.SpellEffect;

import common.Constants;
import common.World;
import common.console.Log;
import common.utils.Formulas;

public class ItemTemplate
{
	private final int ID;
	private final String StrTemplate;
	private final String name;
	private final	int type;
	private final int level;
	private final int pod;
	private final int prix;
	private final int panopID;
	private final String conditions;
	private int PACost,POmin,POmax,TauxCC,TauxEC,BonusCC;
	private boolean isTwoHanded;
	private final ArrayList<Action> onUseActions = new ArrayList<Action>();
	private long sold;
	private int avgPrice;
	private final Map<Integer, String> StaticStates = new TreeMap<Integer, String>();
	
	public ItemTemplate(final int id, final String strTemplate, final String name, final int type,final int level, final int pod, final int prix, final int panopID, final String conditions,final String armesInfos)
	{
		this.ID = id;
		this.StrTemplate = strTemplate;
		this.name = name;
		this.type = type;
		this.level = level;
		this.pod = pod;
		this.prix = prix;
		this.panopID = panopID;
		this.conditions = conditions;
		this.PACost = -1;
		this.POmin = 1;
		this.POmax = 1;
		this.TauxCC = 100;
		this.TauxEC = 2;
		this.BonusCC = 0;
		
		try
		{
			final String[] infos = armesInfos.split(";");
			PACost = Integer.parseInt(infos[0]);
			POmin = Integer.parseInt(infos[1]);
			POmax = Integer.parseInt(infos[2]);
			TauxCC = Integer.parseInt(infos[3]);
			TauxEC = Integer.parseInt(infos[4]);
			BonusCC = Integer.parseInt(infos[5]);
			isTwoHanded = infos[6].equals("1");
		}catch(final Exception e){};

	}
	
	public void addAction(final Action A)
	{
		onUseActions.add(A);
	}
	
	public boolean isTwoHanded()
	{
		return isTwoHanded;
	}
	
	public int getBonusCC()
	{
		return BonusCC;
	}
	
	public int getPOmin() {
		return POmin;
	}
	
	public int getPOmax() {
		return POmax;
	}

	public int getTauxCC() {
		return TauxCC;
	}

	public int getTauxEC() {
		return TauxEC;
	}

	public int getPACost()
	{
		return PACost;
	}
	public int getID() {
		return ID;
	}

	public String getStrTemplate() {
		return StrTemplate;
	}

	public String getName() {
		return name;
	}

	public int getType() {
		return type;
	}

	public int getLevel() {
		return level;
	}

	public int getPod() {
		return pod;
	}

	public int getPrix() {
		return prix;
	}

	public int getPanopID() {
		return panopID;
	}

	public String getConditions() {
		return conditions;
	}
	
	public Item createNewItem(final int qua,final boolean useMax)
	{
		final int nID = World.getNewItemGuid();
		final Stats stats = generateNewStatsFromTemplate(StrTemplate, useMax);
		Item item = null;
		if (type == Constants.ITEM_TYPE_OBJET_VIVANT) {
			item = Speaking.createSpeakingItem(nID, ID, qua, Constants.ITEM_POS_NO_EQUIPED, stats, getEffectTemplate(StrTemplate));
		} else {
			item = new Item(nID, ID, qua, Constants.ITEM_POS_NO_EQUIPED, stats, getEffectTemplate(StrTemplate));
		}
		return item;
	}

	public Stats generateNewStatsFromTemplate(final String statsTemplate,final boolean useMax)
	{
		this.StaticStates.clear();
		final Stats itemStats = new Stats(false, null);
		//Si stats Vides
		if(statsTemplate.equals("") || statsTemplate == null) return itemStats;
		
		final String[] splitted = statsTemplate.split(",");
		for(final String s : splitted)
		{	
			final String[] stats = s.split("#");
			final int statID = Integer.parseInt(stats[0],16);
			boolean follow = true;
			boolean isStatic = false;
			
			for (final int a : Constants.STATIC_EFFECTS) {
				if (a == statID) {
					isStatic = true;
					//this.StaticStates.put(statID, s);
				}
			}
			if (isStatic) {
				isStatic = false;
				continue;
			}
			
			for(final int a : Constants.ARMES_EFFECT_IDS)//Si c'est un Effet Actif
				if(a == statID)
					follow = false;
			if(!follow)continue;//Si c'était un effet Actif d'arme
			
			String jet = "";
			int value  = 1;
			try
			{
				jet = stats[4];
				value = Formulas.getRandomJet(jet);
				if(useMax)
				{
					try
					{
						//on prend le jet max
						final int min = Integer.parseInt(stats[1],16);
						final int max = Integer.parseInt(stats[2],16);
						value = min;
						if(max != 0)value = max;
					}catch(final Exception e){value = Formulas.getRandomJet(jet);};			
				}
			}catch(final Exception e){};
			itemStats.addOneStat(statID, value);
		}
		return itemStats;
	}
	
	private ArrayList<SpellEffect> getEffectTemplate(final String statsTemplate)
	{
		final ArrayList<SpellEffect> Effets = new ArrayList<SpellEffect>();
		if(statsTemplate.equals("") || statsTemplate == null) return Effets;
		
		final String[] splitted = statsTemplate.split(",");
		for(final String s : splitted)
		{	
			final String[] stats = s.split("#");
			final int statID = Integer.parseInt(stats[0],16);
			for(final int a : Constants.ARMES_EFFECT_IDS)
			{
				if(a == statID)
				{
					final int id = statID;
					final String min = stats[1];
					final String max = stats[2];
					final String jet = stats[4];
					final String args = min+";"+max+";-1;-1;0;"+jet;
					Effets.add(new SpellEffect(id, args,0,-1));
				}
			}
		}
		return Effets;
	}
	
	public int get_obviType() {
		try {
			for (final String sts : this.StrTemplate.split(",")) {
				final String[] stats = sts.split("#");
				final int statID = Integer.parseInt(stats[0], 16);
				if (statID == 973)
					return Integer.parseInt(stats[3], 16);
			}
		} catch (final Exception e) {
			Log.addToLog(e.getMessage());
			return 113;
		}
		return 113;
	}
	
	public String parseItemTemplateStats()
	{
		final StringBuilder str = new StringBuilder(6+StrTemplate.length());
		str.append(this.ID).append(';');
		str.append(StrTemplate);
		return str.toString();
	}

	public void applyAction(final Player perso,final int objID)
	{
		for(final Action a : onUseActions)a.apply(perso,objID);
	}
	
	public int getAvgPrice()
	{
		return avgPrice;
	}
	public long getSold()
	{
		return this.sold;
	}
	
	public synchronized void newSold(final int amount, final int price)
	{
		final long oldSold = sold;
		sold += amount;
		avgPrice = (int)((avgPrice * oldSold + price) / sold);
	}
}