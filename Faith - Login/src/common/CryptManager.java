package common;

public class CryptManager {

	public static String cryptPassword(final String Key, final String Password)
    {
        char[] HASH = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
            't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
            'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};

        StringBuilder _Crypted = new StringBuilder();
        _Crypted.append("#1");

        for (int i = 0; i < Password.length(); i++)
        {
            char PPass = Password.charAt(i);
            char PKey = Key.charAt(i);

            int APass = (int)PPass / 16;

            int AKey = (int)PPass % 16;

            int ANB = (APass + (int)PKey) % HASH.length;
            int ANB2 = (AKey + (int)PKey) % HASH.length;

            _Crypted.append(HASH[ANB]);
            _Crypted.append(HASH[ANB2]);
        }
        return _Crypted.toString();
    }
	
	public static String decryptPass(final String pass, final String key)
	{
		String password = pass;
		if(password.startsWith("#1"))
			password = password.substring(2);
		final String Chaine = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_";
		
		char PPass,PKey;
        int APass,AKey,ANB,ANB2,somme1,somme2;
        
        final StringBuilder decrypted = new StringBuilder();

        for (int i = 0; i < password.length(); i+=2)
        {
        	PKey = key.charAt(i/2);
        	ANB = Chaine.indexOf(password.charAt(i));
        	ANB2 = Chaine.indexOf(password.charAt(i+1));

        	somme1 = ANB + Chaine.length();
        	somme2 = ANB2 + Chaine.length();

        	APass = somme1 - (int)PKey;
        	if(APass < 0)APass += 64;
        	APass *= 16;

        	AKey = somme2 - (int)PKey;
        	if(AKey < 0)AKey += 64;

        	PPass = (char)(APass + AKey);

        	decrypted.append(PPass);
        }

		return decrypted.toString();
	}
	
	public static String cryptIP(final String IP)
    {
		final String[] Splitted = IP.split("\\.");
		final StringBuilder Encrypted = new StringBuilder();
        int Count = 0;
        for (int i = 0; i < 50; i++)
        {
            for (int o = 0; o < 50; o++)
            {
                if (((i & 15) << 4 | o & 15) == Integer.parseInt(Splitted[Count]))
                {
                    final Character A = (char)(i+48);
                    final Character B = (char)(o+48);
                    Encrypted.append(A.toString()).append(B.toString());
                    i = 0;
                    o = 0;
                    Count++;
                    if (Count == 4)
                        return Encrypted.toString();
                }
            }
        }
        return "DD";
    }
	
	public static String cryptPort(final int config_game_port)
	{
		final char[] HASH = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
	            't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
	            'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};
		int P = config_game_port;
		final StringBuilder nbr64 = new StringBuilder();
		for(int a = 2;a>=0;a--)
		{
			nbr64.append(HASH[(int)(P/(java.lang.Math.pow(64,a)))]);
			P = (int)(P%(int)(java.lang.Math.pow(64,a)));
		}
		return nbr64.toString();
	}
	
	public static int getIntByHashedValue(final char c)
	{
		final char[] HASH = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
	            't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
	            'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};
		for(int a = 0;a<HASH.length; a++)
		{
			if(HASH[a] == c)
			{
				return a;
			}
		}	
		return -1;
	}
	
	public static char getHashedValueByInt(final int c)
	{
		final char[] HASH = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
	            't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
	            'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};	
		return HASH[c];
	}

	public static String toUtf(final String _in)
	{
		String _out = "";
		try
		{
			_out = new String(_in.getBytes("UTF8"));
		}
		catch (final Exception e)
		{
			System.out.println("Conversion en UTF-8 echoue : " + e.getMessage());
		}

		return _out;
	}
}
