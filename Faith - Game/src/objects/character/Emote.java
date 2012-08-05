package objects.character;

public class Emote
{
	private int emotes;
	
	public Emote(final int emotes)
	{
		this.emotes = emotes;
	}
	
	public void addEmote(final EmoteType emote)
	{
		emotes += emote.getEmote();
	}
	
	public void removeEmote(final EmoteType emote)
	{
		emotes -= emote.getEmote();
	}

	public int get() 
	{
		return emotes;
	}

	public enum EmoteType
	{
		
		SIT	(1),
		BYE	(2),
		APPLAUSE (4),
		ANGRY (8),
		FEAR (16),
		WEAPON (32),
		FLUTE (64),
		PET	(128),
		HELLO (256),
		KISS (512),
		STONE (1024),
		SHEET (2048),
		SCISSORS (4096),
		CROSSARM (8192),
		POINT (16384),
		CROW (32768),
		REST (262144),
		CHAMP (1048576),
		POWERAURA (2097152),
		VAMPYRAURA (4194304);
		
		private int emote;
		
		private EmoteType(final int emote)
		{
			this.emote = emote;
		}
		
		public int getEmote()
		{
			return emote;
		}
	}
}