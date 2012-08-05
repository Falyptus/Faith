package objects.fight;

import java.util.HashMap;
import java.util.Map;

public class Challenge {
	
	private final int id;
	private final int baseXp;
	private final int baseDrop;
	private final int groupXp;
	private final int groupDrop;
	private Map<Integer, ChallengeAppendConditionEnum> conditions;
	
	public Challenge(final int id, final int baseXp, final int baseDrop, final int groupXp, final int groupDrop, final String conditions)
	{
		this.id = id;
		this.baseXp = baseXp;
		this.baseDrop = baseDrop;
		this.groupXp = groupXp;
		this.groupDrop = groupDrop;
		if(!conditions.isEmpty())
		{
			this.conditions = new HashMap<Integer, ChallengeAppendConditionEnum>();
			for(final String condition : conditions.split(","))
			{
				final int ordinal = Integer.valueOf(condition);
				this.conditions.put(ordinal, ChallengeAppendConditionEnum.valueOf(ordinal));
			}
		}
	}
	
	public int getId() {
		return id;
	}

	public int getBaseXp() {
		return baseXp;
	}

	public int getBaseDrop() {
		return baseDrop;
	}

	public int getGroupXp() {
		return groupXp;
	}

	public int getGroupDrop() {
		return groupDrop;
	}
	
	public String parseChallengeAppendPacket()
	{
		final StringBuilder packet = new StringBuilder();
		
		packet.append("Gd");
		packet.append(id).append(';');
		packet.append("%d").append(';');
		packet.append("%d").append(';');
		packet.append(baseXp).append(';');
		packet.append(groupXp).append(';');
		packet.append(baseDrop).append(';');
		packet.append(groupDrop);
		
		return packet.toString();
	}
	
	public String parseChallengeSuccess()
	{
		return "GdOK"+id;
	}
	
	public String parseChallengeFail()
	{
		return "GdKO"+id;
	}
	
	public enum ChallengeAppendConditionEnum
	{
		ENNEMIES_HAS_BOSS,
		NEED_FEW_ALLIES,
		NEED_FEW_ENNEMIES,
		NEED_PAIR_COUNT_ALLIES,
		NEED_PAIR_COUNT_ENNEMIES,
		NEED_TWO_SEX,
		PLAYER_HAS_JARDINIER,
		PLAYER_HAS_ROULETTE,
		PLAYER_HAS_INVOCATION_DARAKNE,
		PLAYER_HAS_INVOCATION_DE_CHAFER,
		USE_ONLY_SPELL,
		USE_ONLY_MELEE,
		USE_ONLY_SAME_ACTION_FOR_FIGHT,
		USE_ONLY_SAME_ELEMENT, 
		USE_NOT_SAME_ACTION_FOR_TURN,
		USE_NOT_SAME_ACTION_FOR_FIGHT;
		//Missing conditions ??

		public static ChallengeAppendConditionEnum valueOf(final int ordinal) {
			for (final ChallengeAppendConditionEnum condition : values()) {
				if (condition.ordinal() == ordinal) return condition;
			}
			return null;
	    }
	}
}
