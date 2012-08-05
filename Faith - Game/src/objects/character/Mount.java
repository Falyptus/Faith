package objects.character;

import java.util.ArrayList;
import java.util.Map.Entry;

import objects.item.Item;

import common.Constants;
import common.SQLManager;
import common.World;
import common.utils.Formulas;

public class Mount {

	private final int _id;
	private final int _color;
	private final int _sex;
	private int _love;
	private int _stamina;
	private int _level;
	private long _exp;
	private String _name;
	private final int _tired;
	private int _energy;
	private int _reprod;
	private final int _maturity;
	private final int _serenity;
	//private int _montable;
	private int _wild;
	private Stats _stats = new Stats();
	private String _ancestors = ",,,,,,,,,,,,,";
	private final ArrayList<Item> _items = new ArrayList<Item>();
	private String _capacities = "";
	
	public Mount(final int color)
	{
		_id = World.getNextIdForMount();
		_color = color;
		_level = 1;
		_exp = 0;
		_name = "SansNom";
		_tired = 0;
		_energy = getMaxEnergy();
		_reprod = 0;
		_maturity = getMaxMaturity();
		_serenity = 0;
		_stats = Constants.getMountStats(_color, _level);
		_sex = Formulas.getRandomValue(0, 1);
		_ancestors = ",,,,,,,,,,,,,";
		_capacities = "0";
		World.addDragodinde(this);
		SQLManager.CREATE_MOUNT(this);
	}
	
	public Mount(final int id, final int color, final int sex, final int love, final int stamina,
			final int level, final long exp, final String name, final int tired,
			final int energy, final int reprod, final int maturity, final int serenity,final String items,final String anc, final String capacities)
	{
		_id = id;
		_color = color;
		_sex = sex;
		_love = love;
		_stamina = stamina;
		_level = level;
		_exp = exp;
		_name = name;
		_tired = tired;
		_energy = energy;
		_reprod = reprod;
		_maturity = maturity;
		_serenity = serenity;
		_ancestors = anc;
		_stats = Constants.getMountStats(_color,_level);
		for(final String str : items.split(";"))
		{
			try
			{
				final Item obj = World.getObjet(Integer.parseInt(str));
				if(obj != null)_items.add(obj);
			}catch(final Exception e){continue;}
		}
		_capacities = capacities;
	}

	public int getId() {
		return _id;
	}

	public int getColor() {
		return _color;
	}
	
	public String getStrColor() {
		if(isCameleone())
			return ",-1,-1,-1";
		return ""+_color;
	}

	public int getSex() {
		return _sex;
	}

	public int getLove() {
		return _love;
	}

	public String getAncestors() {
		return _ancestors;
	}

	public int getStamina() {
		return _stamina;
	}
	public int getLevel() {
		return _level;
	}

	public long getExp() {
		return _exp;
	}

	public String getName() {
		return _name;
	}

	public int getTired() {
		return _tired;
	}

	public int getEnergy() {
		return _energy;
	}

	public int getReprod() {
		return _reprod;
	}

	public int getMaturity() {
		return _maturity;
	}

	public int getSerenity() {
		return _serenity;
	}

	public Stats getStats() {
		return _stats;
	}

	public ArrayList<Item> getItems() {
		return _items;
	}
	
	public String parseInfos()
	{
		StringBuilder str = new StringBuilder();
		str.append(_id).append(':');
		str.append(_color).append(':');
		str.append(_ancestors).append(':');
		str.append(",,").append(_capacities).append(':');
		str.append(_name).append(':');
		str.append(_sex).append(':');
		str.append(parseXpString()).append(':');
		str.append(_level).append(':');
		str.append(isMountable()?"1":"0").append(':');
		str.append(getTotalPods()).append(':');
		str.append(_wild).append(':');
		str.append(_stamina).append(",10000:");
		str.append(_maturity).append(',').append(getMaxMaturity()).append(':');
		str.append(_energy).append(',').append(getMaxEnergy()).append(':');
		str.append(_serenity).append(",-10000,10000:");
		str.append(_love).append(",10000:");
		str.append("-1").append(':');//FIXME Gestation : Faire le système d'élevage.
		str.append(isReproducible()?'1':'0').append(':');
		str.append(parseStats()).append(':');
		str.append(_tired).append(",240:");
		str.append(_reprod).append(",20:");
		return str.toString();
	}

	private String parseStats()
	{
		final StringBuilder stats = new StringBuilder(15+_stats.getMap().size());
		for(final Entry<Integer,Integer> entry : _stats.getMap().entrySet())
		{
			if(entry.getValue() <= 0)continue;
			if(stats.length() >0)stats.append(',');
			stats.append(Integer.toHexString(entry.getKey())).append('#').append(Integer.toHexString(entry.getValue())).append("#0#0");
		}
		return stats.toString();
	}

	private int getMaxEnergy()
	{
		final int energy = 1000;
		return energy;
	}

	private int getMaxMaturity()
	{
		final int maturity = 1000;
		return maturity;
	}

	public int getTotalPods()
	{
		int pods = 100;
		int toAdd = 5*_level;
		if (_color < 21) {
			pods = 100;
			toAdd = 5*_level;
		} else if (_color < 52) {
			pods = 250;
			toAdd = 10*_level;
		} else if (_color < 62) {
			pods = 550;
			toAdd = 15*_level;
		}
		if(isCarrier())
			toAdd = 20*_level;
		return pods+toAdd;
		/*int pod = 1000;
		return pod;*/
	}
	
	public int getUsedPods() {
		int pods = 0;
		for(final Item entry : _items) {
			if(entry == null)
				continue;
			pods += entry.getTemplate().getPod() * entry.getQuantity();
		}
		return pods;
	}

	private String parseXpString()
	{
		return _exp+","+World.getExpLevel(_level).dinde+","+World.getExpLevel(_level+1).dinde;
	}

	public boolean isMountable()
	{
		if(_energy <10
		|| _maturity < getMaxMaturity()
		|| _tired == 240)return false;
		return true;
	}
	
	public boolean isReproducible() {
		if(_color == 88)return false;
		if(_maturity == getMaxMaturity() 
		&& _stamina > 7499
		&& _love > 7499
		&& _reprod < 20)return true;
		return false;
	}

	public String getItemsId()
	{
		final StringBuilder str = new StringBuilder(6*_items.size());
		for(final Item obj : _items)
			str.append(str.length()>0?';':"").append(obj.getGuid());
		return str.toString();
	}
	
	public String getItemsPacket() {
		final StringBuilder items = new StringBuilder();
		boolean isFirst = true;
		for(final Item obj : _items)
		{
			if(!isFirst)
				items.append(';');
			items.append('O').append(obj.parseItem());
			isFirst = false;
		}
		return items.toString();
	}

	public void setName(final String packet)
	{
		_name = packet;
		SQLManager.UPDATE_MOUNT_INFOS(this);
	}
	
	public void setCastre() {
		_reprod = -1;
	}
	
	public void setEnergy(final int energy) {
	    _energy = energy;
    }
	
	public void addXp(final long amount)
	{
		_exp += amount;

		while(_exp >= World.getExpLevel(_level+1).dinde && _level<100)
			levelUp();
		
	}
	
	public void levelUp()
	{
		_level++;
		_stats = Constants.getMountStats(_color,_level);
	}
	
	//Inventaire de la dinde
	public void addItem(final Item item) {
		if((item.getQuantity()*item.getTemplate().getPod())+getUsedPods() <= getTotalPods()) {
			for(final Item entry : _items) {
				if(entry.getStats().isSameStats(item.getStats())) {
					entry.setQuantity(entry.getQuantity()+item.getQuantity());
					SQLManager.UPDATE_MOUNT_INFOS(this);
					return;
				}
			}
			_items.add(item);
			SQLManager.UPDATE_MOUNT_INFOS(this);
		}
	}
	public void removeItem(final Item item) {
		_items.remove(item);
	}
	public void removeItemQuantity(final Item item, final int toRem) {
		for(final Item entry : _items) {
			if(entry.getStats().isSameStats(item.getStats())) {
				entry.setQuantity(entry.getQuantity()-toRem);
				if(entry.getQuantity() == 0)
					_items.remove(entry);
				SQLManager.UPDATE_MOUNT_INFOS(this);
				return;
			}
		}
	}
	//Capacités
	public boolean isTireless() {
    	return _capacities.contains("1");
    }
    public boolean isCarrier() {
    	return _capacities.contains("2");
    }
    public boolean isReproductible() {
    	return _capacities.contains("3");
    }
    public boolean isWise() {
    	return _capacities.contains("4");
    }
    public boolean isEnduring() {
    	return _capacities.contains("5");
    }
    public boolean isInLove() {
    	return _capacities.contains("6");
    }
    public boolean isPrecocious() {
    	return _capacities.contains("7");
    }
    public boolean isPredisposed() {
    	return _capacities.contains("8");
    }
	public boolean isCameleone() {
		return _capacities.contains("9");
	}
	
	public void addCapacity(final String capacite) {
		if(!_capacities.isEmpty())
			_capacities += ',';
		_capacities += capacite;
	}
	
	public void setIsInfatiguable() {
		addCapacity("1");
    }
    public void setIsPorteuse() {
    	addCapacity("2");
    }
    public void setIsReproductible() {
    	addCapacity("3");
    }
    public void setIsSage() {
    	addCapacity("4");
    }
    public void setIsEndurante() {
    	addCapacity("5");
    }
    public void setIsAmoureuse() {
    	addCapacity("6");
    }
    public void setIsPrecoce() {
    	addCapacity("7");
    }
    public void setIsPredispose() {
    	addCapacity("8");
    }
	public void setIsCameleone() {
		addCapacity("9");
	}
	
}
