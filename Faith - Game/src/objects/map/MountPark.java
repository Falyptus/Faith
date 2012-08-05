package objects.map;

import java.util.ArrayList;

import objects.guild.Guild;

import common.World;

public class MountPark
{
	private final int _owner;
	private final InteractiveObject _door;
	private final int _size;
	private final ArrayList<DofusCell> _cases = new ArrayList<DofusCell>();
	private final Guild _guild;
	private final DofusMap _map;
	private final int _price;
	
	public MountPark(final int owner, final DofusMap map, final int size,final String data, final int guild,final int price)
	{
		_owner = owner;
		_door = map.getMountParkDoor();
		_size = size;
		_guild = World.getGuild(guild);
		_map = map;
		_price = price;
		if(_map != null)_map.setMountPark(this);
	}

	public int getOwner() {
		return _owner;
	}

	public InteractiveObject getDoor() {
		return _door;
	}

	public int getSize() {
		return _size;
	}

	public Guild getGuild() {
		return _guild;
	}

	public DofusMap getMap() {
		return _map;
	}

	public int getPrice() {
		return _price;
	}

	public int getObjectNumb()
	{
		int n = 0;
		for(final DofusCell C : _cases)if(C.getObject() != null)n++;
		return n;
	}

	public String parseData()
	{
		final String str ="";
		return str;
	}
	
}