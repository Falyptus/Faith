package objects.item;


import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import objects.character.Player;
import objects.character.Stats;
import objects.fight.Fighter;
import objects.spell.SpellEffect;

import common.Constants;
import common.SQLManager;
import common.SocketManager;
import common.World;
import common.World.Couple;
import common.console.Log;
import common.utils.Formulas;

public class Pet extends Item {
	
	/**
	 * PLEASE DO NOT LOOK THIS CODE, I WROTE THIS IN 2011
	 */
	private PetTemplate petTemplate;
	private byte state;
	private byte nbMeal;
	private byte missMeal;
	private int lastFoodId;
	private int life;
	private int type;
	private String lastTimeAte;
	private final Map<Integer, Integer> stats = new TreeMap<Integer, Integer>();
	private ArrayList<SoulFeed> soulsEatten;
	private final boolean hasStatsBoosted;
	private boolean createNow = false;
	
	public Pet(final int Guid, final int template, final int qua, final int pos, final Stats stats, final ArrayList<SpellEffect> effects)
	{
		super(Guid, template, qua, pos, stats, effects);

		this.nbMeal = 0;
		this.missMeal = 0;
		this.lastFoodId = 0;
		this.life = 10;
		this.state = 0;
		this.hasStatsBoosted = false;
		this.createNow = true;
		this.isPet = true;
		this.petTemplate = World.getPetTemplate(template);
		clearStats();
		this.life = this.petTemplate.getStartLife();
		this.lastTimeAte = Formulas.lastEatNewDate("-");
	}
	
	public Pet(final Item obj, final byte nbMeal, final byte missMeal, final int lastFoodID, final byte state, final String lastTimeAte, 
			final String soulsEatten, final int type, final String stats, final int life, final byte hasStatsBoosted) //Loaded Item
	{
		super(obj.getGuid(), obj.getTemplate().getID(), obj.getQuantity(), obj.getPosition(), obj.parseToSave()); //Item stats

		//Pets stats
		this.nbMeal = nbMeal;
		this.missMeal = missMeal;
		this.lastFoodId = lastFoodID;
		this.state = state;
		this.lastTimeAte = lastTimeAte;
		this.isPet = true;
		this.type = type;
		this.life = life;
		this.petTemplate = World.getPetTemplate(template.getID());
		this.hasStatsBoosted = hasStatsBoosted == 1;
		parseStrStatsToStats(stats);
		if(!soulsEatten.isEmpty())
		{
			for(final String curSoul : soulsEatten.split("\\|"))
			{
				final String[] data = curSoul.split("*");
				this.soulsEatten.add(new SoulFeed(Integer.parseInt(data[0]), Integer.parseInt(data[1])));
			}
		}
	}
	
	public static Pet create_PetItem(final int Guid, final int template, final int qua, final int pos, final Stats stats, final ArrayList<SpellEffect> effects)
	{
		return new Pet(Guid, template, qua, pos, stats, effects);
	}

	public static Pet load_PetItem(final Item item, final byte nbMeal, final byte missMeal, final int lastFeedID, 
			final byte state, final String lastEat, final String monstersEat, final int type, final String stats, final int pdv, final byte surBoost) {
		return new Pet(item, nbMeal, missMeal, lastFeedID, state, lastEat, monstersEat, type, stats, pdv, surBoost);
	}
	
	public void eat(final Player player, final Item obj) 
	{
	    if(obj.getTemplate().getID() == 2239 && life < 10)
	    {
	    	life++;
	    	player.removeItem(obj.getGuid(), 1, true, true);
	    	SocketManager.GAME_SEND_Im_PACKET(player, "032");
	    	return;
	    }
	    if(obj.getTemplate().getType() == Constants.ITEM_TYPE_POTION_FAMILIER && !hasStatsBoosted)
	    {
	    	if(obj.getTemplate().getID() == petTemplate.getItemIncreaseStat())
	    	{
		    	for(final Entry<Integer, Integer> curStat : stats.entrySet())
		    	{
		    		curStat.setValue(curStat.getValue()+(int)(curStat.getValue()/10));
		    	}
		    	player.removeItem(obj.getGuid(), 1, true, true);
		    	SocketManager.GAME_SEND_Im_PACKET(player, "032");
		    	return;
	    	}
	    }
	    
	    if(!petTemplate.listFoodContains(obj.getTemplate().getID()))
	    {
	    	SocketManager.GAME_SEND_Im_PACKET(player, "153");
	    	return;
	    }
	    
	    if(createNow)
	    {
	    	obj.decreaseQuantity(player, 1);
	    	addStatItem(obj);
	    	createNow = false;
	    }
	    
	    if(IsLunchTime() && state == 2 && (missMeal > 0 && missMeal <= 5)) 
	    {
	    	obj.decreaseQuantity(player, 1);
			addStatItem(obj);
			SocketManager.GAME_SEND_Im_PACKET(player, "031");
	    }
	    
	    if(IsLunchTime() && state == 2 && missMeal > 5) 
	    {
	    	obj.decreaseQuantity(player, 1);
	    	addStatItem(obj);
			SocketManager.GAME_SEND_Im_PACKET(player, "029");
	    }
	    
	    if (IsLunchTime() && state == 1) 
	    {
			obj.decreaseQuantity(player, 1);
			addStatItem(obj);
			SocketManager.GAME_SEND_Im_PACKET(player, "032");
		}
	    
	    if (!IsLunchTime() && state == 1)
	    {
			obj.decreaseQuantity(player, 1);
			life--;
			addStatItem(obj);
			SocketManager.GAME_SEND_Im_PACKET(player, "026");
			return;
		}

		if (state >= 2)
		{
			life--;
			lastTimeAte = Formulas.lastEatNewDate("-");
			SocketManager.GAME_SEND_Im_PACKET(player, "027");
			return;
		}

		obj.decreaseQuantity(player, 1);
		addStatItem(obj);
		return;
    }
	
	public void eatSouls(final ArrayList<Fighter> listSouls)
	{
		final Map<Integer, SoulFeed> toSoul = new TreeMap<Integer, SoulFeed>();
		for(final Fighter f : listSouls)
		{
			if(f.isMob())
			{
				final int templateId = f.getMob().getTemplate().getID();
				if(toSoul.containsKey(templateId))
				{
					toSoul.get(templateId).setCount(toSoul.get(templateId).getCount() + 1);
				} else
				{
					toSoul.put(templateId, new SoulFeed(f.getMob().getTemplate().getID(), 1));
				}
			}
		}
		soulsEatten.addAll(toSoul.values()); //A test sinon on boucle les souls et on compare
	}
	
	public void addStatSoul(final SoulFeed soul)
	{
		final boolean mayRefreshAS = false;
		
		final Player player = World.getPlayer(World.getPlayerItems().get(guid));
		SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(player, guid);
		SocketManager.GAME_SEND_OAKO_PACKET(player, this);
		if(mayRefreshAS) SocketManager.GAME_SEND_STATS_PACKET(player);
	}
	
	public void addStatItem(final Item food)
	{
		nbMeal++;
		boolean mayRefreshAS = false;
		if(missMeal>0) 
		{
			missMeal--;
			if(missMeal == 0)
			{
				state = 0;
			}
		}
		if(nbMeal == 3)
		{
			if(state != 2) //S'il n'est pas maigrichon on boost les stats
			{
				final int statId = petTemplate.getItemStatId(food.getTemplate().getID());
				if(stats.containsKey(statId)) 
				{
					final int value = stats.get(statId) + 1;
					stats.remove(statId);
					stats.put(statId, value);
				} else 
				{
					stats.put(statId, 1);
				}
				mayRefreshAS = true;
			}
			nbMeal = 0;
		}
		lastFoodId = food.getTemplate().getID();
		lastTimeAte = Formulas.lastEatNewDate("-");
		final Player player = World.getPlayer(World.getPlayerItems().get(guid));
		SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(player, guid);
		SocketManager.GAME_SEND_Ow_PACKET(player);
		SocketManager.GAME_SEND_OAKO_PACKET(player, this);
		if(mayRefreshAS) SocketManager.GAME_SEND_STATS_PACKET(player);
	}
	
	public void loseLife()
	{
		life--;
		final Player player = World.getPlayer(World.getPlayerItems().get(guid));
		if(life > 1) 
		{
    		SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(player, getGuid());
			SocketManager.GAME_SEND_Ow_PACKET(player);
			SocketManager.GAME_SEND_OAKO_PACKET(player, this);
			return;
		} else
		{
			life = 0;
			setType(Constants.ITEM_TYPE_FANTOME_FAMILIER);
			setPosition(-1);
			setTemplate(World.getItemTemplate(petTemplate.getGhostId()));
			SocketManager.GAME_SEND_OAKO_PACKET(player, this);
			SQLManager.SAVE_ITEM(this);
			SocketManager.GAME_SEND_STATS_PACKET(player);
			SocketManager.GAME_SEND_Im_PACKET(player, "154");
		}
		SQLManager.UPDATE_PET_ITEM(this);
	}
	
	public void toCertificate()
	{
		final Player player = World.getPlayer(World.getPlayerItems().get(guid));
		setType(Constants.ITEM_TYPE_CERTIFICAT_CHANIL);
		setPosition(-1);
		setTemplate(World.getItemTemplate(petTemplate.getGhostId()));
		SQLManager.SAVE_ITEM(this);
		SQLManager.UPDATE_PET_ITEM(this);
		SocketManager.GAME_SEND_OAKO_PACKET(player, this);
		SocketManager.GAME_SEND_STATS_PACKET(player);
		SocketManager.GAME_SEND_Im_PACKET(player, "154");
	}
	
	public String parseSoulToDB()
	{
		final StringBuilder parse = new StringBuilder();
		final boolean isFirst = true;
		for(final SoulFeed curSoul : soulsEatten)
		{
			if(!isFirst) parse.append('|');
			parse.append(curSoul.getMonsterId()).append('*').append(curSoul.getCount());
		}
		return parse.toString();
	}

	public boolean IsLunchTime() 
	{
		boolean can = false;
		try {
			final String Date = lastTimeAte;
			if (Date.contains("-"))
			{
				if (!Formulas.compareTime(Date, petTemplate.getTimeBetweenMeals().first))
				{
					can = true;
				}
			} 
		} catch (final Exception e) 
		{
			Log.addToErrorLog("Erreur Pet: " + e.getMessage());
			return false;
		}
		return can;
	}
	
	public String lastTimeAteToParse()
	{
		final String[] infos = lastTimeAte.split("-");
		final String split = "#";
		return Integer.toHexString(Integer.parseInt(infos[0])) + split + 
		Integer.toHexString(Integer.parseInt(new StringBuilder(String.valueOf(infos[1])).append(infos[2]).toString())) + split + 
		Integer.toHexString(Integer.parseInt(new StringBuilder(String.valueOf(infos[3])).append(infos[4]).toString()));
	}
	
	public String parseStatsString()
	{
		final StringBuilder stats = new StringBuilder(30+(15*soulsEatten.size()));
		boolean isFirst = true;
		// 800: Life
		if(type == Constants.ITEM_TYPE_FAMILIER)
		{
			stats.append(Integer.toHexString(800)).append("#0#0#").append(Integer.toHexString(life)).append(',');
			isFirst = false;
		}
		if(petTemplate.isSoulEater()) 
		{
			for(final SoulFeed soul : soulsEatten)
			{
				if(!isFirst)
					stats.append(',');
				stats.append(Integer.toHexString(717)).append('#').append(Integer.toHexString(soul.getMonsterId())).append("#0#").append(Integer.toHexString(soul.getCount()));
				isFirst = false;
			}
			if(hasStatsBoosted) stats.append(Integer.toHexString(940)).append("#0#0#1");
			return stats.toString();
		}
		if(!isFirst) stats.append(',');
		stats.append(parseStatsMapString());
		// 808: //A mange le...
		try {
			if (lastTimeAte.contains("-"))	
				stats.append(Integer.toHexString(808)).append('#').append(lastTimeAteToParse()).append(',');
		} catch (final Exception e) {}
		// 806: //State
		stats.append(Integer.toHexString(806)).append("#0#0#").append(state).append(',');
		// 807: //Last item eat
		stats.append(Integer.toHexString(807)).append("#0#0#").append(Integer.toHexString(lastFoodId));
		if(hasStatsBoosted) stats.append(',').append(Integer.toHexString(940)).append("#0#0#1");
		return stats.toString();
	}
	
	public void parseStrStatsToStats(final String Stats)
	{
		if(Stats.isEmpty()) return;
		final String[] split = Stats.split(",");
		for(final String s : split)
		{	
			final String[] stats = s.split("#");
			final int statID = Integer.parseInt(stats[0],16);				
			final int value = Integer.parseInt(stats[1],16);
			this.stats.put(statID, value);
		}
	}
	
	public String parseStatsMapString()
	{
		final StringBuilder stats = new StringBuilder();
		final boolean isFirst = true;
		for(final Entry<Integer, Integer> curStat : this.stats.entrySet())
		{
			if(!isFirst)
    			stats.append(',');
    		stats.append(Integer.toHexString(curStat.getKey())).append('#').append(Integer.toHexString(curStat.getValue())).append("#0#0#0d0+").append(curStat.getValue());
		}
		return stats.toString();
	}

	public static class PetTemplate {
		
		private final int templateId;
		private final int ghostId;
		private final int certificateId;
		private final int startLife;
		private final boolean soulEater;
		private Couple<Long, Long> timeBetweenMeals;
		private final ArrayList<ItemFeed> listFood;
		private final Map<ArrayList<SoulFeed>, Integer> listSoul; //Key: liste d'ames value: statId
		private final Map<Integer, Integer> statsMax; //Key: StatId, Value: CapitalMax
		private final int itemIncreaseStat; //Key: ItemTemplateId, Value: 10
		
		public PetTemplate(final int templateId, final int ghostId, final int certificateId, final int startLife, final boolean soulEater, 
				final String timeData, final String bonusFoodData, final String bonusSoulData, final String statsMaxData, final int itemIncreaseStat)
		{
			this.templateId = templateId;
			this.ghostId = ghostId;
			this.certificateId = certificateId;
			this.startLife = startLife;
			this.soulEater = soulEater;
			if(!timeData.isEmpty() && timeData.contains(","))
			{
				this.timeBetweenMeals = new Couple<Long, Long>(Long.parseLong(timeData.split(",")[0]), Long.parseLong(timeData.split(",")[0]));
			}else
			{
				this.timeBetweenMeals = new Couple<Long, Long>(-1L, -1L);
			}
			this.listFood = new ArrayList<ItemFeed>();
			if(!bonusFoodData.isEmpty())
			{
				for(final String feedStats : bonusFoodData.split(";"))
				{
					final int id = Integer.parseInt(feedStats.split(":")[0]);
					final int statId = Integer.parseInt(feedStats.split(":")[1]);
					if(id < 110)
					{
						for(final ItemTemplate obj : World.getObjTemplates().values()) 
						{
							if(obj.getType() == id) 
							{
								listFood.add(new ItemFeed(obj.getID(), statId));
							}
						}
						continue;
					}
					listFood.add(new ItemFeed(id, statId));
				}
			}
			this.listSoul = new TreeMap<ArrayList<SoulFeed>, Integer>();
			if(!bonusSoulData.isEmpty())
			{
				for(final String soulData : bonusFoodData.split("\\|"))
				{
					final String[] data = soulData.split("=>");
					final ArrayList<SoulFeed> soulsDATA = new ArrayList<SoulFeed>();
					for(final String datas : data[0].split("\\+")) 
					{
						soulsDATA.add(new SoulFeed(Integer.parseInt(datas.split("\\*")[0]), Integer.parseInt(datas.split("\\*")[1])));
						//TEMPLATE : monsterId*count+monsterId*count...=>statId|monsterId*count+monsterId*count...=>statId
					}
					listSoul.put(soulsDATA, Integer.parseInt(data[1]));
				}
			}
			this.statsMax = new TreeMap<Integer, Integer>();
			if(!statsMax.isEmpty())
			{
				for (final String curStatMax : statsMaxData.split(";"))
				{
					final String[] infos = curStatMax.split(":");
					this.statsMax.put(Integer.parseInt(infos[0]), Integer.parseInt(infos[1]));
				}
			}
			this.itemIncreaseStat = itemIncreaseStat;
		}
		
		public boolean listFoodContains(final int itemTemplateId) 
		{
			for(int i = 0; i < listFood.size(); i++)
			{
				if(listFood.get(i).getItemId() == itemTemplateId)
				{
					return true;
				}
			}
			return false;
		}

		public int getTemplateId() {
	        return templateId;
        }

		public int getGhostId() {
	        return ghostId;
        }

		public int getCertificateId() {
	        return certificateId;
        }

		public int getStartLife() {
	        return startLife;
        }
		
		public boolean isSoulEater() {
	        return soulEater;
        }

		public Couple<Long, Long> getTimeBetweenMeals() {
	        return timeBetweenMeals;
        }
		
		public ArrayList<ItemFeed> getListFood() {
	        return listFood;
        }
		
		public int getItemStatId(final int itemTemplateId)
		{
			for(int i = 0; i < listFood.size(); i++)
			{
				if(listFood.get(i).getItemId() == itemTemplateId)
				{
					return listFood.get(i).getStatId();
				}
			}
			return -1;
		}

		public Map<ArrayList<SoulFeed>, Integer> getListSoul() {
	        return listSoul;
        }

		public int getSoulStatId(final int monsterId)
		{
			for(final Entry<ArrayList<SoulFeed>, Integer> data : listSoul.entrySet())
			{
				for(final SoulFeed curSoul : data.getKey())
				{
					if(curSoul.getMonsterId() == monsterId)
					{
						return data.getValue();
					}
				}
			}
			return -1;
		}
		
		public int getCountSoulForBoost(final int monsterId)
		{
			for(final Entry<ArrayList<SoulFeed>, Integer> data : listSoul.entrySet())
			{
				for(final SoulFeed curSoul : data.getKey())
				{
					if(curSoul.getMonsterId() == monsterId)
					{
						return curSoul.getCount();
					}
				}
			}
			return -1;
		}
		
		public Map<Integer, Integer> getStatsMax() {
	        return statsMax;
        }

		public int getItemIncreaseStat() {
	        return itemIncreaseStat;
        }	
	}
	
	public static class ItemFeed {
		
		private final int itemId;
		private final int statId;
		
		public ItemFeed(final int itemId, final int statId)
		{
			this.itemId = itemId;
			this.statId = statId;
		}

		public int getItemId() {
			return itemId;
		}

		public int getStatId() {
			return statId;
		}
		
	}
	
	public static class SoulFeed {
		
		private final int monsterId;
		private int count;
		
		public SoulFeed(final int monsterId, final int count)
		{
			this.monsterId = monsterId;
			this.count = count;
		}
		
		public int getMonsterId()
		{
			return this.monsterId;
		}
		
		public int getCount()
		{
			return this.count;
		}
		
		public void setCount(final int count)
		{
			this.count = count;
		}
		
		public String parseToStats()
		{
			return new StringBuilder(15).append("717").append('#').append(Integer.toHexString(monsterId)).append("#0#").append(Integer.toHexString(count)).toString();
		}
		
		public String parseToSave()
		{
			return new StringBuilder(10).append(monsterId).append('*').append(count).toString();
		}
	}

	public void setPetTemplate(final PetTemplate petTemplate) {
	    this.petTemplate = petTemplate;
    }

	public PetTemplate getPetTemplate() {
	    return petTemplate;
    }

	public void setState(final byte state) {
	    this.state = state;
    }

	public byte getState() {
	    return state;
    }

	public void setNbMeal(final byte nbMeal) {
	    this.nbMeal = nbMeal;
    }

	public byte getNbMeal() {
	    return nbMeal;
    }

	public void setMissMeal(final byte missMeal) {
	    this.missMeal = missMeal;
    }

	public byte getMissMeal() {
	    return missMeal;
    }

	public int getLastFoodId() {
	    return lastFoodId;
    }

	public int getLife() {
	    return life;
    }

	public void setType(final int type) {
	    this.type = type;
    }

	public int getType() {
	    return type;
    }

	public String getLastTimeAte() {
	    return lastTimeAte;
    }

	public ArrayList<SoulFeed> getSoulsEatten() {
	    return soulsEatten;
    }

	public boolean isHasStatsBoosted() {
	    return hasStatsBoosted;
    }

	public boolean isCreateNow() {
	    return createNow;
    }

	public Map<Integer, Integer> getMapStats() {
		return stats;
	}

}
