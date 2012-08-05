package objects.character;

public class Restriction {

	private boolean CAN_BE_ASSAULT;
	private boolean CAN_BE_CHALLENGE;
	private boolean CAN_EXCHANGE;
	private boolean CAN_BE_ATTACK;
	private boolean FORCE_WALK;
	private boolean IS_SLOW;
	private boolean CAN_SWITCH_TO_CREATURE;
	private boolean IS_TOMBE;

	public Restriction() {

	}

	public Restriction(int restrictions) {
		CAN_BE_ASSAULT = (restrictions & 1) != 1;
		CAN_BE_CHALLENGE = (restrictions & 2) != 2;
		CAN_EXCHANGE = (restrictions & 4) != 4;
		CAN_BE_ATTACK = (restrictions & 8) != 8;
		FORCE_WALK = (restrictions & 16) == 16;
		IS_SLOW = (restrictions & 32) == 32;
		CAN_SWITCH_TO_CREATURE = (restrictions & 64) != 64;
		IS_TOMBE = (restrictions & 128) == 128;
	}

	public int get() {
		int restrictions = 0;

		if (!CAN_BE_ASSAULT)
			restrictions ++;
		if (!CAN_BE_CHALLENGE)
			restrictions += 2;
		if (!CAN_EXCHANGE)
			restrictions += 4;
		if (!CAN_BE_ATTACK)
			restrictions += 8;
		if (FORCE_WALK)
			restrictions += 16;
		if (IS_SLOW)
			restrictions += 32;
		if (!CAN_SWITCH_TO_CREATURE)
			restrictions += 64;
		if (IS_TOMBE)
			restrictions += 128;

		return restrictions;
	}

	public String toBase36() {
		String toReturn = "";
		try {
			toReturn = Integer.toString(get(), 36);
		} catch (final Throwable ex) {
			toReturn = "0";
		}
		return toReturn;
	}

	public void setAll() {
		CAN_BE_ASSAULT = true;
		CAN_BE_CHALLENGE = true;
		CAN_EXCHANGE = true;
		CAN_BE_ATTACK = true;
		FORCE_WALK = true;
		IS_SLOW = true;
		CAN_SWITCH_TO_CREATURE = true;
		IS_TOMBE = true;
	}

	public void setDefault() {
		CAN_BE_ASSAULT = false;
		CAN_BE_CHALLENGE = false;
		CAN_EXCHANGE = false;
		CAN_BE_ATTACK = false;
		FORCE_WALK = false;
		IS_SLOW = false;
		CAN_SWITCH_TO_CREATURE = false;
		IS_TOMBE = false;
	}

	public void setCanBeAssault(final boolean bool) {
		CAN_BE_ASSAULT = bool;
	}

	public boolean canBeAssault() {
		return CAN_BE_ASSAULT;
	}

	public void setCanBeChallenge(final boolean bool) {
		CAN_BE_CHALLENGE = bool;
	}

	public boolean canBeChallenge() {
		return CAN_BE_CHALLENGE;
	}

	public void setCanExchange(final boolean bool) {
		CAN_EXCHANGE = bool;
	}

	public boolean canExchange() {
		return CAN_EXCHANGE;
	}

	public void setCanBeAttack(final boolean bool) {
		CAN_BE_ATTACK = bool;
	}

	public boolean canBeAttack() {
		return CAN_BE_ATTACK;
	}

	public void setForceWalk(final boolean bool) {
		FORCE_WALK = bool;
	}

	public boolean forceWalk() {
		return FORCE_WALK;
	}

	public void setSlow(final boolean bool) {
		IS_SLOW = bool;
	}

	public boolean isSlow() {
		return IS_SLOW;
	}

	public void setCanSwitchToCreature(final boolean bool) {
		CAN_SWITCH_TO_CREATURE = bool;
	}

	public boolean canSwitchToCreature() {
		return CAN_SWITCH_TO_CREATURE;
	}

	public void setIsTombe(final boolean bool) {
		IS_TOMBE = bool;
	}

	public boolean isTombe() {
		return IS_TOMBE;
	}
}