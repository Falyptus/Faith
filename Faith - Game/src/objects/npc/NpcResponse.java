package objects.npc;

import java.util.ArrayList;

import objects.action.Action;
import objects.character.Player;

public class NpcResponse
{
	private final int _id;
	private final ArrayList<Action> _actions = new ArrayList<Action>();
	
	public NpcResponse(final int id)
	{
		_id = id;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public void addAction(final Action act)
	{
		final ArrayList<Action> c = new ArrayList<Action>();
		c.addAll(_actions);
		for(final Action a : c)if(a.getID() == act.getID())_actions.remove(a);
		_actions.add(act);
	}
	
	public void apply(final Player perso)
	{
		for(final Action act : _actions)
		act.apply(perso,-1);
	}
	
	public boolean isAnotherDialog()
	{
		for(final Action curAct : _actions)
		{
			if(curAct.getID() == 1) //1 = Discours NPC
				return true;
		}
		
		return false;
	}
}