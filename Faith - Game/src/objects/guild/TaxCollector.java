package objects.guild;

import java.util.ArrayList;
import java.util.List;

import objects.GameActor;
import objects.character.Player;
import objects.fight.Fight;
import objects.item.Item;
import objects.map.DofusMap;

import common.World;
import common.World.Drop;

public class TaxCollector implements GameActor{
	private final int id;
	private short name1;
	private short name2;
	private short size;
	private final Guild guild;
	private ArrayList<Item> items;
	private long kamas;
	private long xp;
	private int mapId;
	private int cellId;
	private byte orientation;
	private boolean inExchange;
	private Fight fight;
	private int timer = 45000;
	private List<Player> attackers;
	private List<Player> defenders;
	
	public TaxCollector(final int id, final short nom1, final short nom2, final int guildId, final int mapId, 
			final int cellId, final byte orientation) {
		super();
		this.id = id;
		this.name1 = nom1;
		this.name2 = nom2;
		this.guild = World.getGuild(guildId);
		this.mapId = mapId;
		this.cellId = cellId;
		this.orientation = orientation;
		this.size = parseSize();
	}
	
	public TaxCollector(final int id, final short nom1, final short nom2, final int guildId, final int mapId, 
			final int cellId, final byte orientation, final String objets, final long kamas, final long xp) {
		super();
		this.id = id;
		this.name1 = nom1;
		this.name2 = nom2;
		this.guild = World.getGuild(guildId);
		this.mapId = mapId;
		this.cellId = cellId;
		this.orientation = orientation;
		this.size = parseSize();
		for(final String curObj : objets.split("\\|"))
		{
			if(curObj.equals(""))continue;
			final String[] infos = curObj.split(":");
			final int itemId = Integer.parseInt(infos[0]);
			final Item obj = World.getObjet(itemId);
			if(obj == null)continue;
			this.items.add(obj);
		}
	}
	
	public String parseGM()
	{
		if(fight != null) return "";
		final StringBuilder sock = new StringBuilder(25).append("GM|");
		sock.append('+');
		sock.append(cellId).append(';');
		sock.append(orientation).append(';');
		sock.append('0').append(';');
		sock.append(id).append(';');
		sock.append(name1).append(',').append(name2).append(';');
		sock.append(getActorType()).append(';');
		sock.append("6000^").append(getSize()).append(';');
		sock.append(guild.getLvl()).append(';');
		sock.append(guild.getName()).append(';').append(guild.getEmblem());
		return sock.toString();
	}
	
	public String parseInfos()
	{
		final StringBuilder builder = new StringBuilder();
		final DofusMap map = World.getMap(mapId);
		
		builder.append(getActorId()).append(';').append(name1).append(',').append(name2).append(';');
		
		builder.append(Integer.toString(map.getId(), 36)).append(',').append(map.getX()).append(',').append(map.getY()).append(';');
		builder.append(fight == null ? 0 : 1).append(';');
		if(fight != null)
		{
			builder.append(timer).append(';');//TimerActuel
			builder.append("45000;");//TimerInit
			builder.append(7).append(';');
			builder.append("?,?,");//?
		}else
		{
			builder.append("0;");
			builder.append("45000;");
			builder.append("7;");
			builder.append("?,?,");
		}
		builder.append("1,2,3,4,5");
		return builder.toString();
		//?,?,callername,startdate(Base 10),lastHarvesterName,lastHarvestDate(Base 10),nextHarvestDate(Base 10)
	}
	private short parseSize() {
        if(guild.getLvl() > 100)
        	return 100;
        return size = (short) (50 + guild.getLvl() / 2);
    }
	public String getItemPercepteurList()
	{
		final StringBuilder builder = new StringBuilder();
		for (final Item obj : items) 
			builder.append('O').append(obj.parseItem()).append(';');
		if (kamas != 0) 
			builder.append('G').append(kamas);
	    return builder.toString();
	}
	public String getItemsStr()
	{
		if(items.isEmpty()) return "";
		final StringBuilder builder = new StringBuilder();
		boolean isFirst = true;
		for(final Item obj : items)
		{
			if(!isFirst) builder.append(';');
			builder.append(obj.getTemplate().getID()).append(',').append(obj.getQuantity());
			isFirst = false;
		}
		return builder.toString();
	}
	public void remove()
	{
		for(final Item obj : items) 
		{
			World.removeItem(obj.getGuid());
		}
		World.getMap(mapId).setPercepteur(null);
	}
	public String parseItemToDB() 
	{
		final StringBuilder str = new StringBuilder();
		boolean isFirst = true;
		for(final Item obj : items) 
		{
			if(isFirst) str.append(',');
			str.append(obj.getGuid());
			isFirst = false;
		}
		return str.toString();
	}
	public short getSize(){
		return size;
	}
	public long getKamas() {
		return kamas;
	}
	public void setKamas(final long kamas) {
		this.kamas = kamas;
	}
	public long getXp() {
		return xp;
	}
	public void setXp(final long xp) {
		this.xp = xp;
	}
	public Guild getGuild() {
		return guild;
	}
	public void addItem(final Item obj) {
        items.add(obj);
    }
	public ArrayList<Item> getItems() {
		return items;
	}
	public ArrayList<Drop> getDrops() {
		final ArrayList<Drop> toReturn = new ArrayList<World.Drop>();
		for(final Item obj : items) 
		{
			toReturn.add(
					new Drop(
							obj.getTemplate().getID(), 0, 100, obj.getQuantity()
					)
			);
		}
		return toReturn;
	}
	public void setInExchange(final boolean inExchange) {
        this.inExchange = inExchange;
    }
	public boolean isInExchange() {
        return inExchange;
    }
	public void setFight(final Fight fight) {
        this.fight = fight;
    }
	public Fight getFight() {
		return fight;
	}
	public void setMapId(final int mapId) {
        this.mapId = mapId;
    }
	public int getMapId() {
        return mapId;
    }
	public void setCellId(final int cellId) {
        this.cellId = cellId;
    }
	public int getCellId() {
        return cellId;
    }
	public void setName(final short name1, final short name2) {
		this.name1 = name1;
		this.name2 = name2;
	}
	public short getName1() {
		return this.name1;
	}
	public short getName2() {
		return this.name2;
	}
	public void setSize(final short size) {
        this.size = size;
    }
	public byte getOrientation() {
        return this.orientation;
    }
	public void setOrientation(final byte orientation) {
		this.orientation = orientation;
    }
	public void decrTimer() {
		this.timer -= 1000;
	}
	public void setEmptyTimer() {
		this.timer = 0;
	}
	public void setFullTimer() {
		this.timer = 45000;
	}
	public int getTimer() {
        return this.timer;
    }
	public List<Player> getAttackers() {
		return attackers;
	}
	public void addAttacker(final Player attacker) {
		if(this.attackers == null)
			attackers = new ArrayList<Player>(8);
		this.attackers.add(attacker);
	}
	public List<Player> getDefenders() {
		return defenders;
	}
	public void addDefender(final Player defender) {
		if(this.defenders == null)
			defenders = new ArrayList<Player>(8);
		this.defenders.add(defender);
	}
	@Override
	public int getActorType() {
		return GameActorTypeEnum.TYPE_TAX_COLLECTOR.getActorType();
	}
	@Override
	public int getActorId() {
		return id;
	}

	public void remDefender(Player defender) {
		if(this.defenders.contains(defender))
		{
			int index = defenders.indexOf(defender);
			defenders.remove(index);
		}
	}
}