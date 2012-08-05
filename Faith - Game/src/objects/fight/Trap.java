package objects.fight;

import java.util.ArrayList;

import objects.map.DofusCell;
import objects.spell.SpellStat;

import common.Constants;
import common.SocketManager;
import common.utils.Pathfinding;

public class Trap
{
	private final Fighter _caster;
	private final DofusCell _cell;
	private final byte _size;
	private final int _spell;
	private final SpellStat _trapSpell;
	private final Fight _fight;
	private final int _color;
	private boolean _isVisible = true;
	private int _teamVisible;
	
	public Trap(final Fight fight, final Fighter caster, final DofusCell cell, final byte size, final SpellStat trapSpell, final int spell)
	{
		_fight = fight;
		_caster = caster;
		_cell =cell;
		_spell = spell;
		_size = size;
		_trapSpell = trapSpell;
		_color = Constants.getTrapsColor(spell);
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
	
	public void appear(int team)
	{
		team++;
		String str = "GDZ+" + _cell.getId() + ";" + _size + ";" + _color;
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(_fight, team, 999, _caster.getGUID() + "", str);
		str = "GDC" + _cell.getId() + ";Haaaaaaaaz3005;";
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(_fight, team, 999, _caster.getGUID() + "", str);
	}

	public void disappear()
	{
		int team = _caster.getTeam()+1;
		String str = "GDZ-" + _cell.getId() + ";" + _size + ";" + _color;
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(_fight, team, 999, _caster.getGUID() + "", str);
		
		str = "GDC" + _cell.getId();
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(_fight, team, 999, _caster.getGUID() + "", str);
		
		if (_isVisible) {
			int team2 = _caster.getEnemyTeam();
			
			String str2 = "GDZ-" + _cell.getId() + ";" + _size + ";" + _color;
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(_fight, team2, 999, _caster.getGUID() + "", str2);
			
			str2 = "GDC" + _cell.getId();
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(_fight, team2, 999, _caster.getGUID() + "", str2);
		}
	}
	
	public void onTraped(final Fighter target)
	{
		if(target.isDead())return;
		_fight.getTraps().remove(this);
		//On efface le pieges
		disappear();
		//On déclenche ses effets
		final String str = _spell+","+_cell.getId()+",0,1,1,"+_caster.getGUID();
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(_fight, 7, 307, target.getGUID()+"", str);
		
		final ArrayList<DofusCell> cells = new ArrayList<DofusCell>();
		cells.add(_cell);
		//on ajoute les cases
		for(int a = 0; a < _size;a++)
		{
			final char[] dirs = {'b','d','f','h'};
			final ArrayList<DofusCell> cases2 = new ArrayList<DofusCell>();//on évite les modifications concurrentes
			cases2.addAll(cells);
			for(final DofusCell aCell : cases2)
			{
				for(final char d : dirs)
				{
					final DofusCell cell = _fight.getMap().getCell(Pathfinding.getCaseIDFromDirrection(aCell.getId(), d, _fight.getMap(), true));
					if(cell == null)continue;
					if(!cells.contains(cell))
					{
						cells.add(cell);
					}
				}
			}
		}
		Fighter fakeCaster;
		if(_caster.getPlayer() == null)
			fakeCaster = new Fighter(_fight,_caster.getMob());
		else 	
			fakeCaster = new Fighter(_fight,_caster.getPlayer());

		fakeCaster.setFightCell(_cell);
		_trapSpell.applySpellEffectToFight(_fight,fakeCaster,target.getFightCell(),cells,false);
		_fight.verifIfTeamAllDead();
	}
	
	public boolean isVisible()
	{
		return _isVisible;
	}
	
	public void setVisible(int team) {
		_isVisible = true;
		_teamVisible = team;
	}
	
	public int getTeam()
	{
		return _teamVisible;
	}
	
	public int getColor()
	{
		return _color;
	}
}