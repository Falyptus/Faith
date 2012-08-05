package objects.character;

public class BreedSpell 
{
	private final int breed;
	private final int lvl;
	private final int spellId;
	private final int pos;
	
	public BreedSpell(final int race, final int lvl, final int spellId, final int pos) 
	{
		this.breed = race;
		this.lvl = lvl;
		this.spellId = spellId;
		this.pos = pos;
	}
	
	public int getBreed() 
	{
		return breed;
	}
	
	public int getLvl() 
	{
		return lvl;
	}
	
	public int getSpellId() 
	{
		return spellId;
	}
	
	public int getPos() 
	{
		return pos;
	}
}