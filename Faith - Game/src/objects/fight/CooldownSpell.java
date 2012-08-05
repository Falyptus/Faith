package objects.fight;

import objects.spell.SpellStat;

public class CooldownSpell
{
	private final Fighter target;
	private final int spellId;
	private int cooldown;
	
	public CooldownSpell(final Fighter target, final SpellStat SS)
	{
		this.target = target;
		this.spellId = SS.getSpellID();
		this.cooldown = SS.getCoolDown();
	}
	
	public Fighter getTarget() {
		return target;
	}

	public int getSpellId() {
		return spellId;
	}

	public int getCooldown() {
		return cooldown;
	}

	public void decrement() {
		this.cooldown--;
	}

	public static int getNumbCastSpellForTarget(final Fighter f, final Fighter target, final int spellId)
	{
		int nb = 0;
		for(final CooldownSpell curSpell : f.getCooldowns())
		{
			if(curSpell.getTarget() == null || target == null)
				continue;
			if(curSpell.getSpellId() == spellId && curSpell.getTarget().getGUID() == target.getGUID())
				nb++;
		}
		return nb;
	}

	public static int getNumbCastSpell(final Fighter f, final int spellId)
	{
		int nb = 0;
		for(final CooldownSpell curSpell : f.getCooldowns())
		{
			if(curSpell.getSpellId() == spellId)
				nb++;
		}
		return nb;
	}

	public static boolean cooldownElasped(final Fighter f, final int spellId)
	{
		for(final CooldownSpell curSpell : f.getCooldowns())
		{
			if(curSpell.getSpellId() == spellId && curSpell.getCooldown() > 0)
			{
				return false;
			}
		}
		return true;
	}

}