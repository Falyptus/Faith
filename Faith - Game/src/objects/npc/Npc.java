package objects.npc;

import objects.GameActor;

public class Npc implements GameActor
{
	private final NpcTemplate _template;
	private int _cellID;
	private final int _guid;
	private byte _orientation;
	//private Carte _map; May Be useless
	
	public Npc (final NpcTemplate temp,final int guid,final int cell, final byte o/*, Carte map*/)
	{
		_template = temp;
		_guid = guid;
		_cellID = cell;
		_orientation = o;
		//_map = map;
	}

	public String parseGM()
	{
		final StringBuilder sock = new StringBuilder(30).append('+');
		sock.append(_cellID).append(';');
		sock.append(_orientation).append(';');
		sock.append('0').append(';');
		sock.append(_guid).append(';');
		sock.append(_template.getId()).append(';');
		sock.append(getActorType()).append(';');
		String taille = "";
		if(_template.getScaleX() == _template.getScaleY())
		{
			taille= ""+_template.getScaleY();
		}else
		{
			taille= _template.getScaleX()+"x"+_template.getScaleY();
		}
		sock.append(_template.getGfxID()).append('^').append(taille).append(';');
		sock.append(_template.getSex()).append(';');
		sock.append((_template.getColor1() != -1?Integer.toHexString(_template.getColor1()):"-1")).append(';');
		sock.append((_template.getColor2() != -1?Integer.toHexString(_template.getColor2()):"-1")).append(';');
		sock.append((_template.getColor3() != -1?Integer.toHexString(_template.getColor3()):"-1")).append(';');
		
		sock.append(_template.getAcces()).append(';');
		sock.append((_template.getExtraClip()!=-1?(_template.getExtraClip()):(""))).append(';');
		sock.append(_template.getCustomArtWork());
		return sock.toString();
	}
	
	public NpcTemplate getTemplate() 
	{
		return _template;
	}

	public void setCellID(final int id)
	{
		_cellID = id;
	}

	public void setOrientation(final byte o)
	{
		_orientation = o;
	}

	@Override
	public int getActorType() {
		return GameActorTypeEnum.TYPE_NPC.getActorType();
	}

	@Override
	public byte getOrientation() {
		return _orientation;
	}

	@Override
	public int getMapId() {
		return -1;
	}

	@Override
	public int getCellId() {
		return _cellID;
	}

	@Override
	public int getActorId() {
		return _guid;
	}
	
}