package objects.item;


import java.util.ArrayList;

import objects.character.Player;
import objects.character.Stats;
import objects.spell.SpellEffect;

import common.Constants;
import common.World;
import common.console.Log;
import common.utils.Formulas;

public class Speaking extends Item {
	
	private int _xp = 0;
	private int _appearance = 1;
	private int _state = 0;
	private String _lastEat = "";
	private int _linked_id = -1;
	private Item _linked = null;
	private boolean hasLinked = false;
	private int _lvl = 1;
	private int _type = Constants.ITEM_TYPE_OBJET_VIVANT;
	private int _winXp = 0;

	public Speaking(final int Guid, final int template, final int qua, final int pos, final Stats stats, final ArrayList<SpellEffect> effects) {
		super(Guid, template, qua, pos, stats, effects);

		this._lvl = 1;
		this._xp = 0;
		this._type = this.template.get_obviType();
		this._appearance = 1;

		setState(0);

		this._winXp = 0;
		this._lastEat = "";
		setUnlinkedItem();

		//addTxtStat(805, Formulas.newReceivedDate("#"));

		this.isSpeaking = true;
	}

	public int getType() {
		return this._type;
	}

	public Speaking(final Item obj, final int skin, final int lvl, final int xp, final int state, final int winXp, final String lastEat, final int linked, final int type) {
		super(obj.getGuid(), obj.getTemplate().getID(), obj.getQuantity(), obj.getPosition(), obj.parseToSave());

		this._lvl = lvl;
		this._xp = xp;
		this._type = type;
		this._appearance = skin;

		this._state = state;
		this._winXp = winXp;
		this._lastEat = lastEat;

		this._linked_id = linked;

		this.isSpeaking = true;
	}

	public int getLevel() {
		final int i = 1;
		for (;;) {
			if (_xp > World.getExpLevel(i).obvijevan) {
				return i;
			}
		}
	}

	public int getMealsXp() {
		return this._winXp;
	}

	public void setMealsXp(final int XP) {
		this._winXp = XP;
	}

	public void addMealsXp(final int toAdd) {
		this._winXp += toAdd;
	}

	public void setLvl(final int lvl) {
		this._lvl = lvl;
	}

	public void levelUp(final boolean addXp) {
		if (_lvl > 20)
			return;
		_lvl += 1;
		if (addXp)
			this._xp = (int) World.getExpLevel(_lvl).obvijevan;
	}

	public void addXp(long winxp) {
		if (winxp < 1) 
			winxp = 1;
		if (_lvl >= 20) {
			eatSkinny(winxp);
			return;
		}
		_xp = (int) (_xp + winxp);
		_winXp = (int) (_winXp + winxp);
		if (_winXp > Math.round((World.getExpLevel(_lvl + 1).obvijevan / 10))) {
			_winXp = 0;
			if (_state == 0)
				addState();
		}
		while (_xp >= World.getObviXpMax(_lvl) && _lvl < 20)
			levelUp(false);
	}

	public void eatSkinny(long winxp) {
		if (winxp < 1)
			winxp = 1;
		_winXp = (int) (_winXp + winxp);
		if (_winXp > Math.round((World.getExpLevel(_lvl + 1).obvijevan / 10))) {
			_winXp = 0;
			if (_state == 0)
				addState();
		}
	}

	public void setXp(final int Xp) {
		this._xp = Xp;
	}

	public int getLvl() {
		return this._lvl;
	}

	public boolean hasLinkedItem() {
		return this.hasLinked;
	}

	public static Speaking createSpeakingItem(final int Guid, final int template, final int qua,
	        final int pos, final Stats stats, final ArrayList<SpellEffect> effects) {
		return new Speaking(Guid, template, qua, pos, stats, effects);
	}

	public static Speaking loadSpeakingItem(final Item itm, final int skin, final int lvl,
	        final int xp, final int meals, final int winXp, final String lastEat, final int linked, final int type) {
		return new Speaking(itm, skin, lvl, xp, meals, winXp, lastEat, linked, type);
	}

	public void parseStats(final String Stats) {
		for (final String SSats : Stats.split(",")) {
			final String[] stats = SSats.split("#");
			final int statID = Integer.parseInt(stats[0], 16);
			switch (statID) {
			case 808:
				this._lastEat = Formulas.getDate(stats);
				final int nbr = Formulas.getMissedMeals(_lastEat);
				if (nbr <= 0)
					continue;
				addMealsXp(-nbr);

				break;
			case 972:
				this._appearance = Integer.parseInt(stats[3], 16);
				break;
			case 974:
				this._xp = Integer.parseInt(stats[3], 16);
				break;
			case 970:
				this.template = World.getItemTemplate(Integer.parseInt(stats[3], 16));
			}
		}
	}

	public int getXp() {
		return _xp;
	}

	public void addXp(final int xp) {
		_xp += xp;
	}

	public int getSelectedLevel() {
		return _appearance;
	}

	public void setSelectedLevel(final int lvl) {
		/*if (lvl > get_lvl())
			return;
		else*/
			_appearance = lvl;
	}

	public boolean eatItem(final Player p, final Item item) {
		if (!canEat() && _state == 1) {
			addState();
			item.decreaseQuantity(p, 1);
			int XP = Formulas.getXpItem(item, 40);
			if (XP < 1)
				XP = 1;
			addXp(XP);
			_lastEat = Formulas.lastEatNewDate("-");
			return true;
		}
		if (canEat() && _state == 1) {
			item.decreaseQuantity(p, 1);
			int XP = Formulas.getXpItem(item, 10);
			if (XP < 1)
				XP = 1;
			addXp(XP);
			_lastEat = Formulas.lastEatNewDate("-");
			return true;
		}

		if (_state >= 2)
			return false;

		item.decreaseQuantity(p, 1);
		int XP = Formulas.getXpItem(item, 10);
		if (XP < 1)
			XP = 1;
		addXp(XP);
		_lastEat = Formulas.lastEatNewDate("-");
		return true;
	}

	public boolean canEat() {
		boolean can = false;
		try {
			String Date = getLastEat();
			if (Date.contains("-")) {
				if (!Formulas.compareTime(Date, Constants.ITEM_TIME_FEED_MIN)) {
					can = true;
				}
			} else {
				Date = Formulas.getDate(("325#" + getTxtStat().get(Constants.EFFECT_RECEIVED_DATE)).split("#"));
				if (!Formulas.compareTime(Date, Constants.ITEM_TIME_FEED_MIN))
					can = true;
			}
		} catch (final Exception e) {
			Log.addToErrorLog("Erreur Speaking: " + e.getMessage());
			return false;
		}
		return can;
	}

	public void addState() {
		if (this._state >= 2)
			this._state = 2;
		this._state += 1;
	}

	public void decrState() {
		if (this._state <= 0)
			this._state = 0;
		this._state -= 1;
	}

	public void setState(final int _state) {
		this._state = _state;
	}

	public int getState() {
		return this._state;
	}

	public void setLastEat(final String _lastEat) {
		this._lastEat = _lastEat;
	}

	public String getLastEat() {
		return this._lastEat;
	}

	public void setHasLinked(final Item obj) {
		this._linked_id = obj.getGuid();
		this._linked = obj;
		this.hasLinked = true;
	}

	public void setLinkedItem(final Item linkedItem) {
		this._linked = linkedItem;
	}

	@Override
	public void setUnlinkedItem() {
		this._linked_id = -1;
		this.hasLinked = false;
		this._linked = null;
	}

	public int getLinkedID() {
		return this._linked_id;
	}

	public Item getLinked() {
		return this._linked;
	}

	public String lastEatToPacket() {
		final String[] infos = this._lastEat.split("-");
		final String split = "#";
		return Integer.toHexString(Integer.parseInt(infos[0])) + split + 
		Integer.toHexString(Integer.parseInt(new StringBuilder(String.valueOf(infos[1])).append(infos[2]).toString())) + split + 
		Integer.toHexString(Integer.parseInt(new StringBuilder(String.valueOf(infos[3])).append(infos[4]).toString()));
	}

	public String toString() {
		final StringBuilder states = new StringBuilder();
		try {
			if (this._lastEat.contains("-"))
				states.append(Integer.toHexString(Constants.EFFECT_OBVI_LAST_EAT)).append('#').append(lastEatToPacket()).append(',');
		} catch (final Exception localException) {}
		states.append(Integer.toHexString(Constants.EFFECT_OBVI_SKIN)).append("#0#0#").append(Integer.toHexString(_appearance)).append(',');
		states.append(Integer.toHexString(Constants.EFFECT_OBVI_XP)).append("#0#0#").append(Integer.toHexString(_xp)).append(',');
		states.append(Integer.toHexString(Constants.EFFECT_OBVI_ITEMID)).append("#0#0#").append(Integer.toHexString(template.getID())).append(',');
		states.append(Integer.toHexString(Constants.EFFECT_OBVI_STATE)).append("#0#0#").append(_state).append(',');
		states.append(Integer.toHexString(Constants.EFFECT_OBVI_TYPE)).append("#0#0#").append(Integer.toHexString(_type));

		return states.toString();
	}

	public boolean isSimmilar(final Speaking obj) {
		return false;
	}

	public static Speaking toSpeaking(final Item item) {
		return (Speaking) item;
	}

	public static Item toItem(final Speaking item) {
		return item;
	}
}