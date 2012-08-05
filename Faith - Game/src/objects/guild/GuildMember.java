package objects.guild;

import java.util.Map;
import java.util.TreeMap;

import objects.character.Player;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import common.Constants;
import common.SQLManager;
import common.World;

public class GuildMember
{
	private final int _guid;
	private final Guild _guild;
	private final String _name;
	private int _level;
	private final int _gfx;
	private final byte _align;
	private int _rank = 0;
	private byte _pXpGive = 0;
	private long _xpGave = 0;
	private int _rights = 0;
	private String _lastCo;
	
	//Droit
	private final Map<Integer,Boolean> haveRight = new TreeMap<Integer,Boolean>();

	public GuildMember(final int gu,final Guild g,final String name,final int lvl,final int gfx,final int r,final long x,final byte pXp,final int ri,final byte a,final String lastCo)
	{
		_guid = gu;
		_guild = g;
		_name = name;
		_level = lvl;
		_gfx = gfx;
		_rank = r;
		_xpGave = x;
		_pXpGive = pXp;
		_rights = ri;
		_align = a;
		_lastCo = lastCo;
		parseIntToRight(_rights);
	}
	
	public int getAlign()
	{
		return _align;
	}
	
	public int getGfx()
	{
		return _gfx;
	}
	
	public int getLvl()
	{
		return _level;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public int getGuid()
	{
		return _guid;
	}
	public int getRank()
	{
		return _rank;
	}
	
	public Guild getGuild()
	{
		return _guild;
	}

	public String parseRights()
	{
		return Integer.toString(_rights,36);
	}

	public int getRights()
	{
		return _rights;
	}

	public long getXpGave() {
		return _xpGave;
	}

	public int getPXpGive()
	{
		return _pXpGive;
	}
	
	public String getLastCo()
	{
		return _lastCo;
	}
	
	public int getHoursFromLastCo()
	{
		final String[] strDate = _lastCo.toString().split("~");
		
		final LocalDate lastCo = new LocalDate(Integer.parseInt(strDate[0]),Integer.parseInt(strDate[1]),Integer.parseInt(strDate[2]));
		final LocalDate now = new LocalDate();
		
		return Days.daysBetween(lastCo,now).getDays()*24;
	}

	public Player getPerso()
	{
		return World.getPlayer(_guid);
	}

	public boolean canDo(final int rightValue)
	{
		if(this._rights == 1)
			return true;
		
		return haveRight.get(rightValue);
	}

	public void setRank(final int i)
	{
		_rank = i;
	}
	
	public void setAllRights(int rank,byte xp,int right)
	{
		if(rank == -1)
			rank = this._rank;
		
		if(xp < 0)
			xp = this._pXpGive;
		if(xp > 90)
			xp = 90;
		
		if(right == -1)
			right = this._rights;
		
		this._rank = rank;
		this._pXpGive = xp;
		
		if(right != this._rights && right != 1)	//Vérifie si les droits sont pareille ou si des droits de meneur; pour ne pas faire la conversion pour rien
			parseIntToRight(right);
		this._rights = right;
		
		SQLManager.UPDATE_GUILDMEMBER(this);
	}

	
	public void setLevel(final int lvl)
	{
		this._level = lvl;
	}
	
	public void giveXpToGuild(final long xp)
	{
		this._xpGave+=xp;
		this._guild.addXp(xp);
	}
	
	public void initRight()
	{
		haveRight.put(Constants.G_BOOST,false);
		haveRight.put(Constants.G_RIGHT,false);
		haveRight.put(Constants.G_INVITE,false);
		haveRight.put(Constants.G_BAN,false);
		haveRight.put(Constants.G_ALLXP,false);
		haveRight.put(Constants.G_HISXP,false);
		haveRight.put(Constants.G_RANK,false);
		haveRight.put(Constants.G_POSPERCO,false);
		haveRight.put(Constants.G_COLLPERCO,false);
		haveRight.put(Constants.G_USEENCLOS,false);
		haveRight.put(Constants.G_AMENCLOS,false);
		haveRight.put(Constants.G_OTHDINDE,false);
	}
	
	public void parseIntToRight(int total)
	{
		if(haveRight.size() == 0)
		{
			initRight();
		}
		if(total == 1)
			return;
		
		if(haveRight.size() > 0)	//Si les droits contiennent quelque chose -> Vidage (Même si le TreeMap supprimerais les entrées doublon lors de l'ajout)
			haveRight.clear();
			
		initRight();	//Remplissage des droits
		
		final Integer[] mapKey = haveRight.keySet().toArray(new Integer[haveRight.size()]);	//Récupère les clef de map dans un tableau d'Integer
		
		while(total > 0)
		{
			for (int i = haveRight.size()-1; i < haveRight.size(); i--)
			{
				if(mapKey[i].intValue() <= total)
				{
					total ^= mapKey[i].intValue();
					haveRight.put(mapKey[i],true);
					break;
				}
			}
		}
	}
	
	public void setLastCo(final String lastCo)
	{
		_lastCo = lastCo;
	}
}