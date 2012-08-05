package objects.map;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;


import common.Constants;
import common.SocketManager;
import common.World;
import common.World.IOTemplate;

public class InteractiveObject
{
	private final int _id;
	private int _state;
	private final DofusMap _map;
	private final DofusCell _cell;
	private boolean _interactive = true;
	private final Timer _respawnTimer;
	private final IOTemplate _template;
	
	public InteractiveObject(final DofusMap a_map,final DofusCell a_cell,final int a_id)
	{
		_id = a_id;
		_map = a_map;
		_cell = a_cell;
		_state = Constants.IOBJECT_STATE_FULL;
		int respawnTime = 10000;
		_template = World.getIOTemplate(_id);
		if(_template != null)respawnTime = _template.getRespawnTime();
		//définition du timer
		_respawnTimer = new Timer(respawnTime,
				new ActionListener()
				{
					public void actionPerformed(final ActionEvent e)
					{
						_respawnTimer.stop();
						_state = Constants.IOBJECT_STATE_FULLING;
						_interactive = true;
						SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(_map, _cell);
						_state = Constants.IOBJECT_STATE_FULL;
					}
				}
		);
	}
			
	public int getID()
	{
		return _id;
	}
	
	public boolean isInteractive()
	{
		return _interactive;
	}
	
	public void setInteractive(final boolean b)
	{
		_interactive = b;
	}
	
	public int getState()
	{
		return _state;
	}
	
	public void setState(final int state)
	{
		_state = state;
	}

	public int getUseDuration()
	{
		int duration = 1500;
		if(_template != null)
		{
			duration = _template.getDuration();
		}
		return duration;
	}

	public void startTimer()
	{
		if(_respawnTimer == null)return;
		_state = Constants.IOBJECT_STATE_EMPTY2;
		_respawnTimer.restart();
	}

	public int getUnknowValue()
	{
		int unk = 4;
		if(_template != null)
		{
			unk = _template.getUnk();
		}
		return unk;
	}

	public boolean isWalkable()
	{
		if(_template == null)return false;
		return _template.isWalkable() && _state == Constants.IOBJECT_STATE_FULL;
	}
}