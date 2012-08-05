package objects.character;

import java.util.ArrayList;

import common.SocketManager;

public class Party
{
	private final ArrayList<Player> _members = new ArrayList<Player>();
	private final Player _chief;
	
	public Party(final Player p1,final Player p2)
	{
		_chief = p1;
		_members.add(p1);
		_members.add(p2);
	}
	
	public boolean isChief(final int guid)
	{
		return _chief.getActorId() == guid;
	}
	
	public void addMember(final Player p)
	{
		_members.add(p);
	}
	
	public int getMemberNumber()
	{
		return _members.size();
	}
	
	public int getPartyLevel()
	{
		int lvls = 0;
		for(final Player p : _members)
		{
			lvls += p.getLvl();
		}
		return lvls;
	}
	
	public ArrayList<Player> getMembers()
	{
		return _members;
	}

	public Player getChief()
	{
		return _chief;
	}

	public void leave(final Player p)
	{
		if(!_members.contains(p))return;
		p.setParty(null);
		_members.remove(p);
		if(_members.size() == 1)
		{
			_members.get(0).setParty(null);
			if(_members.get(0).getAccount() == null)return;
			SocketManager.GAME_SEND_PV_PACKET(_members.get(0).getAccount().getGameThread().getOut(),"");
		}
		else
			SocketManager.GAME_SEND_PM_DEL_PACKET_TO_PARTY(this,p.getActorId());
	}
}