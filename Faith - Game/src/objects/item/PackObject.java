package objects.item;

import common.World;

public class PackObject
{

	ItemTemplate TempObject;
	private int Quantity;
	boolean isMax;

	public PackObject(final int TempID, final int Qua, final int Max) {
		this.TempObject = World.getItemTemplate(TempID);
		this.Quantity = Qua;
		this.isMax = (Max == 1);
	}

	public PackObject createPack(final int TempID, final int Qua, final int Max) {
		return new PackObject(TempID, Qua, Max);
	}
	//Functions

	/** GET **/
	public ItemTemplate getTemplate() {
		return TempObject;
	}

	public int getQua() {
		return Quantity;
	}

	public boolean isMax() {
		return isMax;
	}

	/** SET **/
	public void setTemplate(final ItemTemplate Template) {
		this.TempObject = Template;
	}

	public void setMax(final boolean max) {
		this.isMax = max;
	}

	public void setQuantity(final int Qua) {
		this.Quantity = Qua;
	}
}