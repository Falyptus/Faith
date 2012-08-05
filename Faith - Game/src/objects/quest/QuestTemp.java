package objects.quest;

import java.util.*;


public class QuestTemp {
	
	private final int id;
	private final String conditions;
	private final List<Integer> steps = new ArrayList<Integer>();
	/*private final int dialogId;
	private final int rewardXp;
	private final int rewardKamas;*/
	
	public QuestTemp(final int id, final String conditions, final String steps)
	{
		this.id = id;
		this.conditions = conditions;
		if(!steps.isEmpty())
		{
			final String[] stepArray = steps.split(";");
			for(final String step : stepArray)
			{
				this.steps.add(Integer.valueOf(step));
			}
		}
	}

	public int getId() {
		return id;
	}

	public String getConditions() {
		return conditions;
	}

	public List<Integer> getSteps() {
		return steps;
	}

}
