package objects;

public class StrLanguage {
	
	/*
	 *  @param first Str = language (fr, en, ...), second Str = string to search in english, third Str = string Wanted
	 /
	private static Map<String, Couple<String, String>> GetStr = new TreeMap<String, Couple<String, String>>();
	
	public StrLanguage()
	{
		
	}
		
	/**
	 * @param strEnglish = underscored str in file.lang
	 * @return The string wanted
	 /
	public static String GetStr(final String strEnglish) {
		for(final Entry<String, Couple<String, String>> curStr : GetStr.entrySet())
		{
			if(curStr.getKey().equalsIgnoreCase(Config.LANG))
			{
				if(curStr.getValue().first.equalsIgnoreCase(strEnglish))
				{
					return curStr.getValue().second;
				}
			}
		}
		return null;
	}

	/**
	 * This function load the strings and stock into GetStr map.
	 /
	public static void loadLangFile() {
		GetStr.put("EN", new Couple<String, String>("LANG_FILE_NO_EXISTANT_OR_UNREADABLE", "Lang file no existant or unreadable."));
		GetStr.put("FR", new Couple<String, String>("LANG_FILE_NO_EXISTANT_OR_UNREADABLE", "Fichier de langue non existant ou illisible."));
		GetStr.put("EN", new Couple<String, String>("CLOSURE_LOGIN_SERVER", "Login server is closing."));
		GetStr.put("FR", new Couple<String, String>("CLOSURE_LOGIN_SERVER", "Fermeture du serveur de connexion."));
		try {
			final BufferedReader lang = new BufferedReader(new FileReader("lang.file"));
			String line = "";
			while ((line = lang.readLine()) != null) 
			{
				if (line.split(" = ").length == 1)continue;
				final String param = line.split(" = ")[0];
				final String value = line.split(" = ")[1];
				if (line.startsWith("#")) continue;
				if (param.startsWith("EN_"))
				{
					GetStr.put("EN", new Couple<String, String>(param.substring(3), value));
				} else if(param.startsWith("FR_"))
				{
					GetStr.put("FR", new Couple<String, String>(param.substring(3), value));
				} else 
				{
					continue;
				}
			}
			lang.close();
		} catch (final Exception e) {
            System.out.println(e.getMessage());
			System.out.println(GetStr("LANG_FILE_NO_EXISTANT_OR_UNREADABLE"));
			System.out.println(GetStr("CLOSURE_LOGIN_SERVER"));
			System.exit(1);
		}
    }*/
	
	
}
