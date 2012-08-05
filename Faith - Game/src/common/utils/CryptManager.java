package common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import common.Main;
import common.console.Console;
import common.console.Log;

import objects.map.DofusCell;
import objects.map.DofusMap;

public class CryptManager {
	
	public static String cryptPassword(final String Key, final String Password)
    {
        final char[] HASH = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
            't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
            'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};

        final StringBuilder _Crypted = new StringBuilder(2+(Password.length()*2)).append("#1");

        for (int i = 0; i < Password.length(); i++)
        {
            final char PPass = Password.charAt(i);
            final char PKey = Key.charAt(i);

            final int APass = (int)PPass / 16;

            final int AKey = (int)PPass % 16;

            final int ANB = (APass + (int)PKey) % HASH.length;
            final int ANB2 = (AKey + (int)PKey) % HASH.length;

            _Crypted.append(HASH[ANB]);
            _Crypted.append(HASH[ANB2]);
        }
        return _Crypted.toString();
    }
	
	/*public static String decryptpass(String pass,String key)
	{

		int l1, l2, l3, l4, l5;
        String l7 = "";
        String Chaine = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_";
        for(l1 = 0; l1<= (pass.length()-1);l1+=2)
        {
        	l3 = (int)key.charAt((l1/2));
            l2 = Chaine.indexOf(pass.charAt(l1));
            l4 = (64 + l2) - l3;
            int l11 = l1+1;
            l2 = Chaine.indexOf(pass.charAt(l11));
            l5 = (64 + l2) - l3;
            if(l5 < 0)l5 = 64 + l5;
            
            l7 = l7 + (char)(16 * l4 + l5);
        }
        return l7;
	}*/
	
	public static String decryptPass(final String pass, final String key)
	{
		String l_pass = pass;
		if(l_pass.startsWith("#1"))
			l_pass = l_pass.substring(2);
		final String Chaine = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_";
		
		char PPass,PKey;
        int APass,AKey,ANB,ANB2,somme1,somme2;
        
        final StringBuilder decrypted = new StringBuilder();

        for (int i = 0; i < l_pass.length(); i+=2)
        {
        	PKey = key.charAt(i/2);
        	ANB = Chaine.indexOf(l_pass.charAt(i));
        	ANB2 = Chaine.indexOf(l_pass.charAt(i+1));

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
		StringBuilder Encrypted = new StringBuilder();
        int Count = 0;
        for (int i = 0; i < 50; i++)
        {
            for (int o = 0; o < 50; o++)
            {
                if (((i & 15) << 4 | o & 15) == Integer.parseInt(Splitted[Count]))
                {
                    final Character A = (char)(i+48);
                    final Character B = (char)(o + 48);
                    Encrypted.append(A.toString() + B.toString());
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
		StringBuilder nbr64 = new StringBuilder();
		for(int a = 2;a>=0;a--)
		{
			nbr64.append(HASH[(int)(P/(java.lang.Math.pow(64,a)))]);
			P = (int)(P%(int)(java.lang.Math.pow(64,a)));
		}
		return nbr64.toString();
	}
	
	public static String cellIDToCode(final int cellID)
	{
		final char[] HASH = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
	            't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
	            'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};
		
		final int char1 = cellID/64,char2 = cellID%64;
		return  HASH[char1]+""+HASH[char2];
	}
	
	public static int cellCodeToID(final String cellCode)
	{
		final char[] HASH = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
	            't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
	            'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};
		final char char1 = cellCode.charAt(0),char2 = cellCode.charAt(1);
		int code1= 0,code2= 0,a = 0;
		while (a < HASH.length)
		{
			if (HASH[a] == char1)
			{
				code1 = a * 64;
			}
			if (HASH[a] == char2)
			{
				code2 = a;
			}
			a++;
		}
		return (code1 + code2);
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
	
	public static ArrayList<DofusCell> parseStartCell(final DofusMap map,final int num)
	{
		ArrayList<DofusCell> list = null;
		String infos = null;
		if(!map.getPlacesStr().equalsIgnoreCase("-1"))
		{
			infos = map.getPlacesStr().split("\\|")[num];
			int a=0;
			list = new ArrayList<DofusCell>();
			while( a < infos.length())
			{
				list.add(map.getCell( (getIntByHashedValue(infos.charAt(a))<<6) + getIntByHashedValue( infos.charAt (a+1) ) ) );
				a = a+2;
			}
		}
		return list;
	}

	public static Map<Integer, DofusCell> decompileMapData(final DofusMap map,final String dData)
	{
		final Map<Integer, DofusCell> cells = new TreeMap<Integer,DofusCell>();
		for (int f = 0; f < dData.length(); f += 10)
	    {
			final String CellData = dData.substring(f, f+10);
			final List<Byte> CellInfo = new ArrayList<Byte>();
		    for (int i = 0; i < CellData.length(); i++)
		    	CellInfo.add((byte)getIntByHashedValue(CellData.charAt(i)));
		    final int Type = (CellInfo.get(2) & 56) >> 3;
		    final boolean IsSightBlocker = (CellInfo.get(0) & 1) != 0;
		    final int layerObject2 = ((CellInfo.get(0) & 2) << 12) + ((CellInfo.get(7) & 1) << 12) + (CellInfo.get(8) << 6) + CellInfo.get(9);
		    final boolean layerObject2Interactive = ((CellInfo.get(7) & 2) >> 1) != 0;
		    final int obj = (layerObject2Interactive?layerObject2:-1);
		    cells.put(f/10,new DofusCell(map, f/10, Type!=0, IsSightBlocker, obj));
	    }
		return cells;
	}

	/*MARTHIEUBEAN*/
	//Fonction qui convertis tout les textes ANSI(Unicode) en UTF-8. Les fichiers doivent être codé en ANSI sinon les phrases seront illisible.
	public static String toUtf(final String _in)
	{
		String _out = "";

		try
		{
			_out = new String(_in.getBytes("UTF8"));
			
		}catch(final Exception e)
		{
			Console.printlnError("Conversion en UTF-8 échoué! : "+e.getMessage());
			e.printStackTrace();
		}
		
		return _out;
	}
	//Utilisé pour convertir les inputs UTF-8 en String normal.
	public static String toUnicode(final String _in)
	{
		String _out = "";

		try
		{
			_out = new String(_in.getBytes(),"UTF8");
			
		}catch(final Exception e)
		{
			Console.printlnError("Conversion en UTF-8 échoué! : "+e.getMessage());
			e.printStackTrace();
		}
		
		return _out;
	}
	/*FIN*/
	
	public static String cryptSHA512(final String message){
        MessageDigest md;
        try {
            md= MessageDigest.getInstance("SHA-512");
 
            md.update(message.getBytes());
            final byte[] mb = md.digest();
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < mb.length; i++) {
                final byte temp = mb[i];
                String s = Integer.toHexString(Byte.valueOf(temp));
                while (s.length() < 2) {
                    s = "0" + s;
                }
                s = s.substring(s.length() - 2);
                out.append(s);
            }
            return out.toString();
 
        } catch (final NoSuchAlgorithmException e) {
            Log.addToErrorLog(e.getMessage());
            e.printStackTrace();
            Main.closeServers();
        }
        return null;
    }
}
