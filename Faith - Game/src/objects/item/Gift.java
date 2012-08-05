package objects.item;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map.Entry;

import objects.account.Account;

import common.SocketManager;

public class Gift 
{

	private final int _ID;
	private final String _title;
	private final String _description;
	private ArrayList<PackObject> _objects = new ArrayList<PackObject>();
	private final String URL;
	private final int cost;

	public Gift(final int aID, final String aTitle, final String aDesc, final String aobjects, final String URL, final int cost) {
		this._ID = aID;
		this._title = aTitle;
		this._description = aDesc;
		this._objects = parseObjects(aobjects);
		this.URL = URL;
		this.cost = cost;
	}

	public Gift createGift(final int aID, final String aTitle, final String aDesc, final String aobjects, final String URL, final int cost) {
		return new Gift(aID, aTitle, aDesc, aobjects, URL, cost);
	}

	/* Fonctions de Gestion des cadeaux */
	public ArrayList<PackObject> parseObjects(final String ObjectList) {
		final ArrayList<PackObject> toReturn = new ArrayList<PackObject>();
		toReturn.clear();
		for (final String Object : ObjectList.split("\\|")) // On charge les packets d'objets
		{
			try {
				final String[] Args = Object.split(",");
				int bool = 0;
				try {
					bool = Integer.parseInt(Args[2]);
				} catch (final Exception e) {
					bool = 0;
				}
				final PackObject Obj = new PackObject(
						Integer.parseInt(Args[0]),
						Integer.parseInt(Args[1]),
						bool);
				toReturn.add(Obj);
			} catch (final Exception e) {}
		}
		return toReturn;
	}

	/* Fonctions de récupération des infos d'un cadeau */
	public String get_title() {
		return _title;
	}

	public String get_desc() {
		return _description;
	}

	public ArrayList<PackObject> get_objects() {
		return _objects;
	}

	public int get_id() {
		return _ID;
	}

	public String get_url() {
		return URL;
	}

	public int get_cost() {
		return cost;
	}

	public String toString()
	{
		final StringBuilder str = new StringBuilder("").append(get_id()).append("|").append(get_TitlePacket()).append("|").append(get_DescPacket()).append("|").append(get_url()).append("|");
		int Incr = 0;
		for (final PackObject Obj : _objects) 
		{
			if (Incr > 5) break;
			Incr++;
			if (Incr > 1) str.append(";");
			str.append(Incr).append("~");
			str.append(toHexa(Obj.getTemplate().getID())).append("~");
			str.append(toHexa(Obj.getQua())).append("~");
			str.append("1~"); //?

			String stats = Obj.getTemplate().getStrTemplate();
			if (Obj.isMax) 	stats = Obj.getTemplate().generateNewStatsFromTemplate(Obj.getTemplate().getStrTemplate(), true).parseToItemSetStats();
			str.append(stats);
		}
		return str.toString();
	}

	public String get_TitlePacket() {
		return _title.replace(' ', '+');	
	}

	public String get_DescPacket() {
		return _description.replace(' ', '+');
	}

	public static String toHexa(final int objectID) {
		return Integer.toHexString(objectID);
	}

	public static void ParseToAgPacket(final PrintWriter _out, final Account _compte)
	{
		try {
			for (final Entry<Integer, Gift> GiftsofList : _compte.getGifts().entrySet()) {
				final Gift ActualGift = GiftsofList.getValue();
				SocketManager.REALM_SEND_GIFT(_out, ActualGift.toString());
				break;
			}
		} catch (final Exception e) {
			SocketManager.REALM_SEND_CLOSE_GIFTS_UI(_out);
			return;
		};
	}
}