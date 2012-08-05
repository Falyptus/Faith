package objects.npc;

import java.util.ArrayList;

import objects.item.ItemTemplate;

import common.World;

public class NpcTemplate {
	private int _id;
	private int _bonusValue;
	private int _gfxID;
	private int _scaleX;
	private int _scaleY;
	private int _sex;
	private int _color1;
	private int _color2;
	private int _color3;
	private String _acces;
	private int _extraClip;
	private int _customArtWork;
	private int _initQuestionID;
	private ArrayList<ItemTemplate> _sales = new ArrayList<ItemTemplate>();
	
	public NpcTemplate(final int _id, final int value, final int _gfxid, final int _scalex, final int _scaley,
			final int _sex, final int _color1, final int _color2, final int _color3, final String _acces,
			final int clip, final int artWork, final int questionID,final String ventes) {
		super();
		this._id = _id;
		_bonusValue = value;
		_gfxID = _gfxid;
		_scaleX = _scalex;
		_scaleY = _scaley;
		this._sex = _sex;
		this._color1 = _color1;
		this._color2 = _color2;
		this._color3 = _color3;
		this._acces = _acces;
		_extraClip = clip;
		_customArtWork = artWork;
		_initQuestionID = questionID;
		if(ventes.equals(""))return;
		for(final String obj : ventes.split("\\,"))
		{
			try
			{
				final int tempID = Integer.parseInt(obj);
				final ItemTemplate temp = World.getItemTemplate(tempID);
				if(temp == null)continue;
				_sales.add(temp);
			}catch(final NumberFormatException e){continue;};
		}
	}

	public int getId() {
		return _id;
	}

	public int getBonusValue() {
		return _bonusValue;
	}

	public int getGfxID() {
		return _gfxID;
	}

	public int getScaleX() {
		return _scaleX;
	}

	public int getScaleY() {
		return _scaleY;
	}

	public int getSex() {
		return _sex;
	}

	public int getColor1() {
		return _color1;
	}

	public int getColor2() {
		return _color2;
	}

	public int getColor3() {
		return _color3;
	}

	public String getAcces() {
		return _acces;
	}

	public int getExtraClip() {
		return _extraClip;
	}

	public int getCustomArtWork() {
		return _customArtWork;
	}

	public int get_initQuestionID() {
		return _initQuestionID;
	}
	
	public String getItemVendorList()
	{
		final StringBuilder items = new StringBuilder();
		if(_sales.size() == 0)return "";
		for(final ItemTemplate obj : _sales)
		{
			items.append(obj.parseItemTemplateStats()).append('|');
		}
		return items.toString();
	}

	public boolean addItemVendor(final ItemTemplate T)
	{
		if(_sales.contains(T))return false;
		_sales.add(T);
		return true;
	}
	public boolean delItemVendor(final int tID)
	{
		final ArrayList<ItemTemplate> newSales = new ArrayList<ItemTemplate>();
		boolean remove = false;
		for(final ItemTemplate T : _sales)
		{
			if(T.getID() == tID)
			{
				remove = true;
				continue;
			}
			newSales.add(T);
		}
		_sales = newSales;
		return remove;
	}

	public void setInitQuestion(final int q)
	{
		_initQuestionID = q;
	}
	
	public boolean haveItem(final int templateID)
	{
		for(final ItemTemplate curTemp : _sales)
		{
			if(curTemp.getID() == templateID)
				return true;
		}
		
		return false;
	}
}
