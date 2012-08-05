package objects.character;

public class Right {

	private boolean CAN_ASSAULT;
	private boolean CAN_CHALLENGE;
	private boolean CAN_EXCHANGE;
	private boolean CAN_ATTACK;
	private boolean CAN_CHAT_TO_ALL;
	private boolean CAN_BE_MERCHANT;
	private boolean CAN_USE_OBJECT;
	private boolean CANT_INTERACT_WITH_TAX_COLLECTOR;
	private boolean CAN_USE_INTERACTIVE_OBJECTS;
	private boolean CANT_SPEAK_NPC;
	private boolean CAN_ATTACK_DUNGEON_MONSTERS_WHEN_MUTANT;
	private boolean CAN_MOVE_ALL_DIRECTIONS;
	private boolean CAN_ATTACK_MONSTERS_ANYWHERE_WHEN_MUTANT;
	private boolean CANT_INTERACT_WITH_PRISM;
	
	private int rights;

	public Right() {

	}

	public Right(int rights) {
		this.rights = rights;
		CAN_ASSAULT = (rights & 1) != 1;
		CAN_CHALLENGE = (rights & 2) != 2;
		CAN_EXCHANGE = (rights & 4) != 4;
		CAN_ATTACK = (rights & 8) == 8;
		CAN_CHAT_TO_ALL = (rights & 16) != 16;
		CAN_BE_MERCHANT = (rights & 32) != 32;
		CAN_USE_OBJECT = (rights & 64) != 64;
		CANT_INTERACT_WITH_TAX_COLLECTOR = (rights & 128) != 128;
		CAN_USE_INTERACTIVE_OBJECTS = (rights & 256) != 256;
		CANT_SPEAK_NPC = (rights & 512) != 512;
		CAN_ATTACK_DUNGEON_MONSTERS_WHEN_MUTANT = (rights & 4096) == 4096;
		CAN_MOVE_ALL_DIRECTIONS = (rights & 8192) == 8192;
		CAN_ATTACK_MONSTERS_ANYWHERE_WHEN_MUTANT = (rights & 16384) == 16384;
		CANT_INTERACT_WITH_PRISM = (rights & 32768) != 32768;
	}

	public int get() {
		int rights = 0;

		if (!CAN_ASSAULT)
			rights++;
		if (!CAN_CHALLENGE)
			rights += 2;
		if (!CAN_EXCHANGE)
			rights += 4;
		if (CAN_ATTACK)
			rights += 8;
		if (!CAN_CHAT_TO_ALL)
			rights += 16;
		if (!CAN_BE_MERCHANT)
			rights += 32;
		if (!CAN_USE_OBJECT)
			rights += 64;
		if (!CANT_INTERACT_WITH_TAX_COLLECTOR)
			rights += 128;
		if (!CAN_USE_INTERACTIVE_OBJECTS)
			rights += 256;
		if (CANT_SPEAK_NPC)
			rights += 512;
		if (CAN_ATTACK_DUNGEON_MONSTERS_WHEN_MUTANT)
			rights += 4096;
		if (CAN_MOVE_ALL_DIRECTIONS)
			rights += 8192;
		if (CAN_ATTACK_MONSTERS_ANYWHERE_WHEN_MUTANT)
			rights += 16384;
		if (!CANT_INTERACT_WITH_PRISM)
			rights += 32768;

		this.rights = rights;
		return rights;
	}

	public String toBase36() {
		String toReturn = "";
		try {
			toReturn = Integer.toString(get(), 36);
		} catch (final Throwable ex) {
			toReturn = "6bk"; // default value
		}
		return toReturn;
	}

	public void addAll() {
		CAN_ASSAULT = true;
		CAN_CHALLENGE = true;
		CAN_EXCHANGE = true;
		CAN_ATTACK = true;
		CAN_CHAT_TO_ALL = true;
		CAN_BE_MERCHANT = true;
		CAN_USE_OBJECT = true;
		CANT_INTERACT_WITH_TAX_COLLECTOR = true;
		CAN_USE_INTERACTIVE_OBJECTS = true;
		CANT_SPEAK_NPC = true;
		CAN_ATTACK_DUNGEON_MONSTERS_WHEN_MUTANT = true;
		CAN_MOVE_ALL_DIRECTIONS = true;
		CAN_ATTACK_MONSTERS_ANYWHERE_WHEN_MUTANT = true;
		CANT_INTERACT_WITH_PRISM = true;

	}

	public void setDefault() {
		CAN_ASSAULT = false;
		CAN_CHALLENGE = false;
		CAN_EXCHANGE = false;
		CAN_ATTACK = false;
		CAN_CHAT_TO_ALL = false;
		CAN_BE_MERCHANT = false;
		CAN_USE_OBJECT = false;
		CANT_INTERACT_WITH_TAX_COLLECTOR = false;
		CAN_USE_INTERACTIVE_OBJECTS = false;
		CANT_SPEAK_NPC = false;
		CAN_ATTACK_DUNGEON_MONSTERS_WHEN_MUTANT = false;
		CAN_MOVE_ALL_DIRECTIONS = true;
		CAN_ATTACK_MONSTERS_ANYWHERE_WHEN_MUTANT = false;
		CANT_INTERACT_WITH_PRISM = false;

	}

	public void setAssault(final boolean bool) {
		CAN_ASSAULT = bool;
	}

	public boolean canAssault() {
		return CAN_ASSAULT;
	}

	public void setCanChallenge(final boolean bool) {
		CAN_CHALLENGE = bool;
	}

	public boolean canChallenge() {
		return CAN_CHALLENGE;
	}

	public void setCanExchange(final boolean bool) {
		CAN_EXCHANGE = bool;
	}

	public boolean canExchange() {
		return CAN_EXCHANGE;
	}

	public void setCanAttack(final boolean bool) {
		CAN_ATTACK = bool;
	}

	public boolean canAttack() {
		return CAN_ATTACK;
	}

	public boolean canChatWithAll() {
		return CAN_CHAT_TO_ALL;
	}

	public void setCanChatWithAll(final boolean bool) {
		CAN_CHAT_TO_ALL = bool;
	}

	public boolean canBeMerchant() {
		return CAN_BE_MERCHANT;
	}

	public void setCanBeMerchant(final boolean bool) {
		CAN_BE_MERCHANT = bool;
	}

	public boolean canUseObject() {
		return CAN_USE_OBJECT;
	}

	public void setCanUseObject(final boolean bool) {
		CAN_USE_OBJECT = bool;
	}

	public boolean cantInteractWithTaxCollector() {
		return CANT_INTERACT_WITH_TAX_COLLECTOR;
	}

	public void setCantInteractWithTaxCollector(final boolean bool) {
		CANT_INTERACT_WITH_TAX_COLLECTOR = bool;
	}

	public boolean canUseInteractiveObject() {
		return CAN_USE_INTERACTIVE_OBJECTS;
	}

	public void setCanUseInteractiveObject(final boolean bool) {
		CAN_USE_INTERACTIVE_OBJECTS = bool;
	}

	public boolean cantSpeakWithNPC() {
		return CANT_SPEAK_NPC;
	}

	public void setCantSpeakWithNPC(final boolean bool) {
		CANT_SPEAK_NPC = bool;
	}

	public boolean canAttackDungeonMonstersWhenMutant() {
		return CAN_ATTACK_DUNGEON_MONSTERS_WHEN_MUTANT;
	}

	public void setCanAttackDungeonMobsWhenMutant(final boolean bool) {
		CAN_ATTACK_DUNGEON_MONSTERS_WHEN_MUTANT = bool;
	}

	public boolean canMoveAllDirections() {
		return CAN_MOVE_ALL_DIRECTIONS;
	}

	public void setCanMoveAllDirections(final boolean bool) {
		CAN_MOVE_ALL_DIRECTIONS = bool;
	}

	public boolean canAttackMonstersAnyWhereWhenMutant() {
		return CAN_ATTACK_MONSTERS_ANYWHERE_WHEN_MUTANT;
	}

	public void setCanAttackMonstersAnyWhereWhenMutant(final boolean bool) {
		CAN_ATTACK_MONSTERS_ANYWHERE_WHEN_MUTANT = bool;
	}

	public boolean cantInteractWithPrism() {
		return CANT_INTERACT_WITH_PRISM;
	}

	public void setCantInteractWithPrism(final boolean bool) {
		CANT_INTERACT_WITH_PRISM = bool;
	}
}