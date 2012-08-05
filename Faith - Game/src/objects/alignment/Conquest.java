package objects.alignment;

import java.util.HashMap;
import java.util.Map;

import objects.character.Player;

import common.Constants;
import common.SocketManager;
import common.World;
import common.World.SubArea;

public class Conquest {
	
	private static short numberSubAreaBrakmarian;
	private static short numberSubAreaBontarian;
	private static short numberAreaBrakmarian;
	private static short numberAreaBontarian;
	private static Map<Short, Short> numberPlayerBrakmarianBySArea;
	private static Map<Short, Short> numberPlayerBontarianBySArea;
	private static Map<Short, Short> numberPlayerBrakmarianByArea;
	private static Map<Short, Short> numberPlayerBontarianByArea;
	
	public static void Initalize()
	{
		numberSubAreaBrakmarian = 0;
		numberSubAreaBontarian = 0;
		numberAreaBrakmarian = 0;
		numberAreaBontarian = 0;
		numberPlayerBrakmarianBySArea = new HashMap<Short, Short>();
		numberPlayerBontarianBySArea = new HashMap<Short, Short>();
		numberPlayerBrakmarianByArea = new HashMap<Short, Short>();
		numberPlayerBontarianByArea = new HashMap<Short, Short>();
		final int numbSubAreas = World.getSubAreas().size();
		for(int i = 0; i < numbSubAreas; i++)
		{
			final short subAreaId = (short)World.getSubArea(i).getId();
			numberPlayerBrakmarianBySArea.put(subAreaId, (short)0);
			numberPlayerBontarianBySArea.put(subAreaId, (short)0);
		}
	}
	
	public static float[] GetBalanceWorld()
	{
		float percentBrak = 0, percentBonta = 0;
		for(final SubArea subArea : World.getSubAreas())
		{
			final float[] balanceArea = GetBalanceArea(subArea);
			percentBonta+= balanceArea[0];
			percentBrak += balanceArea[1];
		}
		percentBonta /= World.getSubAreas().size();
		percentBrak /= World.getSubAreas().size();
		return new float[]{percentBonta, percentBrak};
	}
	
	public static float[] GetBalanceArea(final SubArea subArea)
	{
		if(subArea.getPlayers().size() == 0) return new float[]{};
		int playersBrakmarian = 0, playersBontarian = 0, playersTotal = 0;
		for(final Player player : subArea.getPlayers())
		{
			if(player.isShowingWings() && player.getFight().getType() != 4)
			{
				if(player.getAlign() == Constants.ALIGNEMENT_BONTARIEN)
					playersBontarian++;
				else if(player.getAlign() == Constants.ALIGNEMENT_BRAKMARIEN)
					playersBrakmarian++;
				playersTotal++;
			}
		}
		final float percentBontarians = (float) playersTotal*playersBontarian/100;
		final float percentBrakmarians = (float) playersTotal*playersBrakmarian/100;
		return new float[]{percentBontarians, percentBrakmarians};
	}
	
	/*public static float GetBalanceArea(byte align)
	{
		if(align == 3) return 0;
		int playersBontarian = 0;
		int playersBrakmarian = 0;
		int playersTotal = 0;
		for(SubArea subArea : World.getSubAreas())
		{
			if(subArea.getAlignement() == align)
			{
				for(Player player : subArea.getPlayers())
				{
					if(player.isShowingWings() && player.get_fight().getType() != 4)
					{
						if(align == 1) playersBontarian++;
						else playersBrakmarian++;
						playersTotal++;
					}
				}
			}
		}
		for(SubArea subArea : World.getSubAreas())
		{
			if(subArea.getAlignement() != align)
			{
				for(Player player : subArea.getPlayers())
				{
					if(player.isShowingWings() && player.get_fight().getType() != 4)
					{
						if(align == 1) playersBontarian++;
						else playersBrakmarian++;
						playersTotal++;
					}
				}
			}
		}
		float percentAlignBontarian, percentAlignBrakmarian;
		percentAlignBontarian = playersTotal*playersBontarian/100;
	}*/
	public static int GetCountSAreaAlign(final int align)
	{
		int count = 0;
		for(final SubArea subArea : World.getSubAreas())
		{
			if(subArea.getAlignement() == align)
			{
				count++;
			}
		}
		return count;
	}
	
	public static short GetNumbSAreaBrakmarian()
	{
		return numberSubAreaBrakmarian;
	}
	public static short GetNumbAreaBrakmarian()
	{
		return numberAreaBrakmarian;
	}
	public static short GetNumbSAreaBontarian()
	{
		return numberSubAreaBontarian;
	}
	public static short GetNumbAreaBontarian()
	{
		return numberAreaBontarian;
	}
	public static short GetNumbPlayerBrakmarianBySArea(final int sareaId)
	{
		return numberPlayerBrakmarianBySArea.get(sareaId);
	}
	public static short GetNumbPlayerBrakmarianByArea(final int areaId)
	{
		return numberPlayerBrakmarianByArea.get(areaId);
	}
	public static short GetNumbPlayerBontarianBySArea(final int sareaId)
	{
		return numberPlayerBontarianBySArea.get(sareaId);
	}
	public static short GetNumbPlayerBontarianByArea(final int areaId)
	{
		return numberPlayerBontarianByArea.get(areaId);
	}
	
	public static void AddNumbSAreaBrakmarian()
	{
		numberSubAreaBrakmarian++;
	}
	public static void AddNumbAreaBrakmarian()
	{
		numberAreaBrakmarian++;
	}
	public static void AddNumbSAreaBontarian()
	{
		numberSubAreaBontarian++;
	}
	public static void AddNumbAreaBontarian()
	{
		numberAreaBontarian++;
	}
	public static void AddNumbPlayerBrakmarianBySArea(final short sareaId)
	{
		final short numb = (short) (numberPlayerBrakmarianBySArea.get(sareaId) + 1);
		if(numberPlayerBrakmarianBySArea.containsKey(sareaId))
			numberPlayerBrakmarianBySArea.remove(sareaId);
		numberPlayerBrakmarianBySArea.put(sareaId, numb);
	}
	public static void AddNumbPlayerBrakmarianByArea(final short areaId)
	{
		final short numb = (short) (numberPlayerBrakmarianBySArea.get(areaId) + 1);
		if(numberPlayerBrakmarianBySArea.containsKey(areaId))
			numberPlayerBrakmarianBySArea.remove(areaId);
		numberPlayerBrakmarianBySArea.put(areaId, numb);
	}
	public static void AddNumbPlayerBontarianBySArea(final short sareaId)
	{
		final short numb = (short) (numberPlayerBontarianBySArea.get(sareaId) + 1);
		if(numberPlayerBontarianBySArea.containsKey(sareaId))
			numberPlayerBontarianBySArea.remove(sareaId);
		numberPlayerBontarianBySArea.put(sareaId, numb);
	}
	public static void AddNumbPlayerBontarianByArea(final short areaId)
	{
		final short numb = (short) (numberPlayerBontarianBySArea.get(areaId) + 1);
		if(numberPlayerBontarianBySArea.containsKey(areaId))
			numberPlayerBontarianBySArea.remove(areaId);
		numberPlayerBontarianBySArea.put(areaId, numb);
	}
	
	public static void DecrNumbSAreaBrakmarian()
	{
		numberSubAreaBrakmarian--;
	}
	public static void DecrNumbAreaBrakmarian()
	{
		numberAreaBrakmarian--;
	}
	public static void DecrNumbSAreaBontarian()
	{
		numberSubAreaBontarian--;
	}
	public static void DecrNumbAreaBontarian()
	{
		numberAreaBontarian--;
	}
	public static void DecrNumbPlayerBrakmarianBySArea(final short sareaId)
	{
		final short numb = (short) (numberPlayerBrakmarianBySArea.get(sareaId) - 1);
		if(numberPlayerBrakmarianBySArea.containsKey(sareaId))
			numberPlayerBrakmarianBySArea.remove(sareaId);
		numberPlayerBrakmarianBySArea.put(sareaId, numb);
	}
	public static void DecrNumbPlayerBrakmarianByArea(final short areaId)
	{
		final short numb = (short) (numberPlayerBrakmarianBySArea.get(areaId) - 1);
		if(numberPlayerBrakmarianBySArea.containsKey(areaId))
			numberPlayerBrakmarianBySArea.remove(areaId);
		numberPlayerBrakmarianBySArea.put(areaId, numb);
	}
	public static void DecrNumbPlayerBontarianBySArea(final short sareaId)
	{
		final short numb = (short) (numberPlayerBontarianBySArea.get(sareaId) - 1);
		if(numberPlayerBontarianBySArea.containsKey(sareaId))
			numberPlayerBontarianBySArea.remove(sareaId);
		numberPlayerBontarianBySArea.put(sareaId, numb);
	}
	public static void DecrNumbPlayerBontarianByArea(final short areaId)
	{
		final short numb = (short) (numberPlayerBontarianBySArea.get(areaId) - 1);
		if(numberPlayerBontarianBySArea.containsKey(areaId))
			numberPlayerBontarianBySArea.remove(areaId);
		numberPlayerBontarianBySArea.put(areaId, numb);
	}

	public static boolean CanDepositPrism(final int playerId, final int id) {
		final SubArea subArea = World.getSubArea(id);
		final Player out = World.getPlayer(playerId);
		boolean toReturn = true;
		if(out.getLvl() < 10 || out.getALvl() < 3)
		{
			SocketManager.GAME_SEND_Im_PACKET(out, "1155");
			toReturn = false;
		}
		if(toReturn && !out.isShowingWings() || out.getLastTimeShowWings() - 300000 < 0)
		{
			SocketManager.GAME_SEND_Im_PACKET(out, "1148");
			toReturn = false;
		}
		if(toReturn && subArea.getPrism() != null)
		{
			SocketManager.GAME_SEND_Im_PACKET(out, "1149");
			toReturn = false;
		}
		if(toReturn && out.getCurMap().getPlacesStr().length() < 5)
		{
			SocketManager.GAME_SEND_Im_PACKET(out, "1145");
			toReturn = false;
		}
		final Alignment alignment = World.getAlignment(subArea.getAlignement());
		if(toReturn && alignment.getSubAreasOwned()-alignment.getAreasOwned()+1 
				< 17*alignment.getAreasOwned())
		{
			SocketManager.GAME_SEND_Im_PACKET(out, "1152");
			toReturn = false;
		}
		if(toReturn)
		{
			int numbEnemies = 0;
			final int otherAlign = out.getAlign() == 1 ? 2 : 1;
			for(final Player player : subArea.getPlayers())
			{
				if(player.getAlign() == otherAlign)
				{
					numbEnemies++;
					if(numbEnemies == 20)
						break;
				}
			}
			if(numbEnemies == 20)
			{
				SocketManager.GAME_SEND_Im_PACKET(out, "1153");
				toReturn = false;
			}
		}
		//TODO: Check des territoires adjacents.
		return toReturn;
	}
}