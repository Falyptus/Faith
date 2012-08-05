package objects.quest;

import java.util.ArrayList;


import common.World;
import common.World.Couple;

public class QuestStep {
	
	private final int id;
	private final int dialogId;
	private final int rewardXp;
	private final int rewardKamas;
	private final ArrayList<Couple<Integer, Integer>> rewardItems = new ArrayList<Couple<Integer, Integer>>();//itemId/itemQuantity
	private final ArrayList<QuestObjective> objectives = new ArrayList<QuestObjective>();
	private int order;
	
	public QuestStep (final int id, final int dialogId, final int rewardXp, final int rewardKamas, final String rewardItems, final String objectives)
	{
		this.id = id;
		this.dialogId = dialogId;
		this.rewardXp = rewardXp;
		this.rewardKamas = rewardKamas;
		for(final String curItem : rewardItems.split("\\|"))
		{
			try
			{
				final String[] infoItem = curItem.split(";");
				this.rewardItems.add(new Couple<Integer, Integer>(Integer.parseInt(infoItem[0]), Integer.parseInt(infoItem[1])));
			} catch(final Exception e)
			{
				continue;
			}
		}
		for(final String curObjective : objectives.split("\\|"))
		{
			try
			{
				this.objectives.add(World.getQuestObjectives().get(Integer.parseInt(curObjective)));
			} catch(final Exception e)
			{
				continue;
			}
		}
	}

	public int getId() {
        return id;
    }

	public int getDialogId() {
        return dialogId;
    }

	public int getRewardXp() {
        return rewardXp;
    }

	public int getRewardKamas() {
        return rewardKamas;
    }

	public ArrayList<Couple<Integer, Integer>> getRewardItems() {
        return rewardItems;
    }
	
	public ArrayList<QuestObjective> getObjectives() {
        return objectives;
    }
	
	public void setOrder(final int order) {
	    this.order = order;
    }

	public int getOrder() {
	    return order;
    }
	
}