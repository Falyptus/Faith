package objects.alignment;

import java.util.ArrayList;
import java.util.List;

import objects.character.Player;
import objects.fight.Fight;

public class Prism {
	
	private int id;
	private byte align;
	private int gfxId;
	private int name;
	private final byte grade;
	private final int mapId;
	private final int cellId;
	private Fight fight;
	private ArrayList<Player> attackerPlayers;
	private ArrayList<Player> defenderPlayers;
	private int timer = 45000;
	private String state = "-1";
	
	public Prism(final int id, final byte align, final byte grade, final int mapId, final int cellId)
	{
		this.id = id;
		setAlign(align);
		this.grade = grade;
		this.mapId = mapId;
		this.cellId = cellId;
	}
	
	public String parseGM()
	{
		final StringBuilder packet = new StringBuilder();
		if(fight != null)
			return "";
		packet.append("GM|+").append(cellId).append(";3;0;-13;").append(name).append(";-10;").append(gfxId).append("^100;").append(grade).append(';').append(grade).append(';').append(align);
		return packet.toString();
	}

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public byte getAlign() {
		return align;
	}

	public void setAlign(final byte align) {
		this.align = align;
		setGfxId(7999+this.align);
		setName(1000+this.align);
	}

	public int getGfxId() {
		return gfxId;
	}

	public void setGfxId(final int gfxId) {
		this.gfxId = gfxId;
	}

	public int getName() {
		return name;
	}

	public void setName(final int name) {
		this.name = name;
	}

	public byte getGrade() {
		return grade;
	}

	public int getMapId() {
		return mapId;
	}

	public int getCellId() {
		return cellId;
	}

	public Fight getFight() {
		return fight;
	}

	public void setFight(final Fight fight) {
		this.fight = fight;
		if(fight != null)
			setState("0;"+timer+";45000;7");
		else
			setState("-1");
	}

	public List<Player> getAttackerPlayers() {
		return attackerPlayers;
	}

	public void setAttackerPlayers(final Player attackerPlayer) {
		this.attackerPlayers.add(attackerPlayer);
	}

	public List<Player> getDefenderPlayers() {
		return defenderPlayers;
	}

	public void addDefenderPlayers(final Player defenderPlayer) {
		this.defenderPlayers.add(defenderPlayer);
	}

	public int getTimer() {
		return timer;
	}

	public void setTimer(final int timer) {
		this.timer = timer;
	}

	public String getState() {
		return state;
	}

	public void setState(final String state) {
		this.state = state;
	}
}