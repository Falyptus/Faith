package objects.fight;

import objects.map.DofusCell;
import objects.spell.SpellStat;

import common.Constants;
import common.SocketManager;

public class Glyph
{
	private final Fighter _caster;
	private final DofusCell _cell;
	private final byte _size;
	private final int _spell;
	private final SpellStat _trapSpell;
	private byte _duration;
	private final Fight _fight;
	private final int _color;
	
	public Glyph(final Fight fight, final Fighter caster, final DofusCell cell, final byte size, final SpellStat trapSpell, final byte duration, final int spell)
	{
		_fight = fight;
		_caster = caster;
		_cell =cell;
		_spell = spell;
		_size = size;
		_trapSpell = trapSpell;
		_duration = duration;
		_color = Constants.getGlyphColor(spell);
	}

	public DofusCell getCell() {
		return _cell;
	}

	public byte getSize() {
		return _size;
	}

	public Fighter getCaster() {
		return _caster;
	}
	
	public byte getDuration() {
		return _duration;
	}

	public int decrementDuration()
	{
		_duration--;
		return _duration;
	}
	
	public void onGlyph(final Fighter target)
	{
		final String str = _spell+","+_cell.getId()+",0,1,1,"+_caster.getGUID();
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(_fight, 7, 307, target.getGUID()+"", str);
		_trapSpell.applySpellEffectToFight(_fight,_caster,target.getFightCell(),false);
		_fight.verifIfTeamAllDead();
	}

	public void desapear()
	{
		SocketManager.GAME_SEND_GDZ_PACKET_TO_FIGHT(_fight, 7, "-",_cell.getId(), _size, _color);
		SocketManager.GAME_SEND_GDC_PACKET_TO_FIGHT(_fight, 7, _cell.getId());
	}
	
	public int getColor()
	{
		return _color;
	}
	
	public int getSpellId() {
		return _spell;
	}
}