package objects.fight;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import common.SocketManager;
import objects.character.Player;

public class FightChallenge {
	
	private Fight fight;
	private List<Fighter> fighters;
	private String lastAction;
	private Challenge challenge;
	private ChallengeState state;
	private ChallengeObjective objective;
	
	public FightChallenge(Fight fight, Challenge challenge)
	{
		this.fight = fight;
		this.fighters = fight.getListFighter();
		Collections.copy(this.fighters, fight.getListFighter());
		this.challenge = challenge;
		this.state = ChallengeState.ACTIVE;
		analyzeObjective();
	}
	
	private void analyzeObjective() {
		switch(challenge.getId())
		{
		
		}
	}
	
	private void analyzeAction(Object obj) {
		if(state == ChallengeState.ACTIVE) {
			if(obj instanceof String) {
				String action = (String) obj;
				
			}
		}
	}
	
	public void analyzeBeginTurn(Fighter fighter) {
		if(state == ChallengeState.ACTIVE && 
				fighter.getPlayer() instanceof Player) {
			
		}
	}
	
	public void analyzeEndTurn() {
		if(state == ChallengeState.ACTIVE) {
			
		}
	}
	
	public void challengeFilled() {
		state = ChallengeState.SUCCESS;
		SocketManager.GAME_SEND_CHALLENGE_FILLED(fighters, challenge.getId());
	}
	
	public void challengeFailed(String failer) {
		state = ChallengeState.FAIL;
		SocketManager.GAME_SEND_CHALLENGE_FAILED(fighters, challenge.getId(), failer);
	}

	public enum ChallengeObjective
	{
		USE_ONLY_SPELL,
		USE_ONLY_MELEE,
		USE_ONLY_SAME_ACTION_FOR_FIGHT,
		USE_ONLY_SAME_ELEMENT,
		USE_ONLY_ONE_MP,
		
		USE_ALL_MP,
		USE_ALL_AP,
		
		DO_NOT_USE_SAME_ACTION_FOR_TURN,
		DO_NOT_USE_SAME_ACTION_FOR_FIGHT,
		
		DO_NOT_USE_MP,
		
		DO_NOT_MINUS_MP,
		DO_NOT_MINUS_AP,
		DO_NOT_MINUS_RANGE,
		
		FOCUS_MONSTER_TO_DEATH,
		
		KILL_FIRST,
		KILL_LAST,
		
		ONLY_GIRL_ALLOW_KILL,
		ONLY_BOY_ALLOW_KILL;
		
		public static ChallengeObjective valueOf(int ordinal) {
			for (ChallengeObjective condition : values()) {
				if (condition.ordinal() == ordinal) return condition;
			}
			return null;
	    }
	}
	
	public enum ChallengeState
	{
		ACTIVE,
		SUCCESS,
		FAIL;
	}

}
