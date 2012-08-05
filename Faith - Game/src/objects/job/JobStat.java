package objects.job;


import java.util.ArrayList;
import java.util.List;

import objects.action.GameAction;
import objects.character.Player;
import objects.map.DofusCell;
import objects.map.InteractiveObject;

import common.Config;
import common.Constants;
import common.SocketManager;
import common.World;

public class JobStat
{
	private final transient int id;
	private final transient Job template;
	private transient int lvl;
	private transient long xp;
	private transient List<JobAction> posActions = new ArrayList<JobAction>();
	private transient boolean isCheap = false;
	private transient boolean freeOnFails = false;
	private transient boolean noRessource = false;
	private transient JobAction curAction;
	
	public JobStat(final int id,final Job tp,final int lvl,final long xp)
	{
		this.id = id;
		this.template = tp;
		this.lvl = lvl;
		this.xp = xp;
		this.posActions = Constants.getPosActionsToJob(tp.getId(),lvl);
	}

	public int getLvl() {
		return lvl;
	}
	public boolean isCheap() {
		return isCheap;
	}

	public void setIsCheap(final boolean isCheap) {
		this.isCheap = isCheap;
	}

	public boolean isFreeOnFails() {
		return freeOnFails;
	}

	public void setFreeOnFails(final boolean freeOnFails) {
		this.freeOnFails = freeOnFails;
	}

	public boolean isNoRessource() {
		return noRessource;
	}

	public void setNoRessource(final boolean noRessource) {
		this.noRessource = noRessource;
	}

	public void levelUp(final Player player,final boolean send)
	{
		lvl++;
		posActions = Constants.getPosActionsToJob(template.getId(),lvl);
		
		if(send)
		{
			//on créer la listes des statsMetier a envoyer (Seulement celle ci)
			final List<JobStat> list = new ArrayList<JobStat>();
			list.add(this);
			SocketManager.GAME_SEND_JS_PACKET(player, list);
			SocketManager.GAME_SEND_STATS_PACKET(player);
			SocketManager.GAME_SEND_Ow_PACKET(player);
			SocketManager.GAME_SEND_JN_PACKET(player,template.getId(),lvl);
			SocketManager.GAME_SEND_JO_PACKET(player, list);
		}
	}
	public String parseJS()
	{
		final StringBuilder str = new StringBuilder(5+25*posActions.size());
		str.append('|').append(template.getId()).append(';');
		boolean first = true;
		for(final JobAction JA : posActions)
		{
			if(!first) {
				str.append(',');
			} else {
				first = false;
			}
			str.append(JA.getSkillID()).append('~').append(JA.getMin()).append('~');
			if(JA.isCraft()) {
				str.append('0').append('~').append('0').append('~').append(JA.getChance());
			} else {
				str.append(JA.getMax()).append('~').append('0').append('~').append(JA.getTime());
			}
		}
		return str.toString();
	}
	public long getXp()
	{
		return xp;
	}
	
	public void startAction(final int id,final Player player,final InteractiveObject interactiveObject,final GameAction gameAction,final DofusCell cell)
	{
		for(final JobAction JA : posActions)
		{
			if(JA.getSkillID() == id)
			{
				curAction = JA;
				JA.startAction(player,interactiveObject,gameAction,cell);
				return;
			}
		}
	}
	
	public void endAction(final int id,final Player P,final InteractiveObject IO,final GameAction GA,final DofusCell cell)
	{
		if(curAction == null) { return; }
		curAction.endAction(P,IO,GA,cell);
		addXp(P,curAction.getXpWin()*Config.XP_METIER);
		//Packet JX
		//on créer la listes des statsMetier a envoyer (Seulement celle ci)
		final ArrayList<JobStat> list = new ArrayList<JobStat>();
		list.add(this);
		SocketManager.GAME_SEND_JX_PACKET(P, list);
	}
	
	public void addXp(final Player P,final long xp)
	{
		if(lvl >99) {
			return;
		}
		final int exLvl = lvl;
		this.xp += xp;
		
		//Si l'xp dépasse le pallier du niveau suivant
		while(this.xp >= World.getExpLevel(lvl+1).metier && lvl <100) {
			levelUp(P,false);
		}
		
		//s'il y a eu Up
		if(lvl > exLvl && P.isOnline())
		{
			//on créer la listes des statsMetier a envoyer (Seulement celle ci)
			final ArrayList<JobStat> list = new ArrayList<JobStat>();
			list.add(this);
			//on envoie le packet
			SocketManager.GAME_SEND_JS_PACKET(P, list);
			SocketManager.GAME_SEND_JN_PACKET(P,template.getId(),lvl);
			SocketManager.GAME_SEND_STATS_PACKET(P);
			SocketManager.GAME_SEND_Ow_PACKET(P);
			SocketManager.GAME_SEND_JO_PACKET(P, list);
		}
	}
	
	public String getXpString(final String s)
	{
		final StringBuilder str = new StringBuilder();
		str.append(World.getExpLevel(lvl).metier).append(s);
		str.append(xp).append(s);
		str.append(World.getExpLevel((lvl<100?lvl+1:lvl)).metier);
		return str.toString();
	}
	public Job getTemplate() {
		
		return template;
	}

	public int getOptBinValue()
	{
		int nbr = 0;
		nbr += (isCheap?1:0);
		nbr += (freeOnFails?2:0);
		nbr += (noRessource?4:0);
		return nbr;
	}
	
	public boolean isValidMapAction(final int id)
	{
		boolean toReturn = false;
		for(final JobAction JA : posActions) {
			if(JA.getSkillID() == id) {
				toReturn = true;
			}
		}
		return toReturn;
	}
	
	public void setOptBinValue(final int bin)
	{
		int l_bin = bin;
		isCheap = false;
		freeOnFails = false;
		noRessource = false;
		
		if(l_bin - 4 >=0)
		{
			l_bin -= 4;
			isCheap = true;
		}
		if(l_bin - 2 >=0)
		{
			l_bin -=2;
			freeOnFails = true;
		}
		if(l_bin - 1 >= 0)
		{
			l_bin -= 1;
			noRessource = true;
		}
	}

	public int getID()
	{
		return id;
	}
}