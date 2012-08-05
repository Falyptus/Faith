package objects.quest;

import java.util.ArrayList;
import java.util.Map.Entry;

import objects.character.Player;
import objects.fight.Fighter;
import objects.item.Item;

import common.Constants;
import common.World.MonsterFollower;

public class QuestObjective {
	
	private final int id;
	private final int type;
	private final String args;
	private boolean isFinished;
	
	public QuestObjective (final int id, final int type, final String args)
	{
		this.id = id;
		this.type = type;
		this.args = args;
	}
	
	@SuppressWarnings("unchecked")
    public void fill(final Player player, final Object object)
	{
		final String[] args = this.args.split(";");
		switch(type)
		{
		case 1://Aller voir
			if(player.getIsTalkingWith() == Integer.parseInt(args[0]))
			{
				isFinished = true;
			}
			break;
		case 2://Montrer à
			if(player.hasItemTemplate(Integer.parseInt(args[0]), Integer.parseInt(args[1])))
			{
				isFinished = true;
			}
			break;
		case 3://Ramener à
			if(player.hasItemTemplate(Integer.parseInt(args[0]), Integer.parseInt(args[1])))
			{
				player.removeByTemplateID(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
				isFinished = true;
			}
			break;
		case 4://Découvrir la carte
			if(player.getCurMap().getId() == Integer.parseInt(args[0]))
			{
				isFinished = true;
			}
			break;
		case 5://Découvrir la zone
			if(player.getCurMap().getSubArea().getId() == Integer.parseInt(args[0]))
			{
				isFinished = true;
			}
			break;
		case 6://Vaincre en un seul combat
			int i = 0;

			for(final String mobs : args)
			{
				final String[] infos = mobs.split(",");
				for(final Fighter fighter : (ArrayList<Fighter>)object)
				{
					if(fighter.getMob() != null && fighter.getMob().getTemplate().getID() == Integer.parseInt(infos[0]))
					{
						i++;
					}
				}
				if(i >= Integer.parseInt(infos[1]))
				{
					isFinished = true;
				}
			}
			break;
		case 7://Monstre à vaincre FIXME: maybe more than 1 mobs to kill
			for(final String mob : args)
			{
				for(final Fighter fighter : ((ArrayList<Fighter>)object))
				{
					if(fighter.isMob() && fighter.getMob().getTemplate().getID() == Integer.parseInt(mob))
					{
						isFinished = true;
					}
				}
			}
			break;
		case 8://Utiliser
			if(player.getLastItemUsed() == Integer.parseInt(args[0]))
			{
				isFinished = true;
			}
			break;
		case 9://Retourner voir
			//TODO: Faire un checker pour voir s'il a déjà parler au pnj ??
			if(player.getIsTalkingWith() == Integer.parseInt(args[0]))
			{
				isFinished = true;
			}
			break;
		case 10://Escorter
			for(final MonsterFollower monsterFollower : player.getMonstersFollower().values())
			{
				if(monsterFollower.getMobId() == Integer.parseInt(args[0]))
				{
					isFinished = true;
				}
			}
			break;
		case 11://Vaincre un joueur en défi
			if(player.getFight().getType() == Constants.FIGHT_TYPE_CHALLENGE)
			{
				final Player target = ((ArrayList<Fighter>) object).get(0).getPlayer(); //Pas sûr du fonctionnement
				if(target != null)
				{
					final int diff = Math.round(target.getLvl()-player.getLvl());
					if(diff >= 0 && diff <= 25)
					{
						isFinished = true;
					}
				}
			}
			break;
		case 12://Ramener âmes
			final ArrayList<Item> toRem = new ArrayList<Item>();
			final String[] infosSoul = args[1].split(":");
			int soulQua = Integer.parseInt(args[1]);
			if(player.getIsTalkingWith() == Integer.parseInt(args[0]))
			{
				for(final Item item : player.getItems().values())
				{
					for(final Entry<Integer, Integer> curStat : item.getStats().getMap().entrySet())
					{
						if(curStat.getKey() == 623 && curStat.getValue() == Integer.parseInt(infosSoul[0]))
						{
							if(!toRem.contains(item))
								toRem.add(item);
							soulQua--;
						}
					}
				}
				if(soulQua <= 0)
				{
					isFinished = true;
				}
				for(final Item toRemove : toRem)
				{
					player.removeItem(toRemove.getGuid());
				}
			}
			break;
		case 13://Eliminer
			for(final String mob : args)
			{
				for(final Fighter fighter : (ArrayList<Fighter>)object)
				{
					if(fighter.getMob() != null && fighter.getMob().getTemplate().getID() == Integer.parseInt(mob))
					{
						isFinished = true;
					}
				}
			}
			break;
		case 14://Avis de recherche
			for(final String mob : args)
			{
				for(final Fighter fighter : (ArrayList<Fighter>)object)
				{
					if(fighter.getMob() != null && fighter.getMob().getTemplate().getID() == Integer.parseInt(mob))
					{
						isFinished = true;
					}
				}
			}
			break;
		
		default:
			isFinished = true;
			break;
		}
		player.checkQuests();
	}

	public int getId() {
        return id;
    }

	public int getType() {
        return type;
    }

	public String getArgs() {
        return args;
    }

	public boolean isFinished() {
        return isFinished;
    }
}