package objects.account;

import java.util.HashMap;
import java.util.Map;

import objects.item.Item;

import common.World;

public class Bank {
	
	private int accountId;
	private long kamas;
	private HashMap<Integer, Item> items;
	
	public Bank(int accountId, long kamas, String itemList)
	{
		this.accountId = accountId;
		this.kamas = kamas;
		items = new HashMap<Integer, Item>();
		String[] itemArray = itemList.split(",");
		if(!itemList.isEmpty())
		{
			for(String strUid : itemArray)
			{
				int itemUid = Integer.valueOf(strUid);
				Item item = World.getObjet(itemUid);
				if(item != null)
				{
					items.put(itemUid, item);
				}
			}
		}
	}
	
	public int getAccountId() {
		return accountId;
	}

	public long getKamas() {
		return kamas;
	}

	public void setKamas(long kamas) {
		this.kamas = kamas;
	}

	public Map<Integer, Item> getItems() {
		return items;
	}

	public void put(Item item) {
		this.items.put(item.getGuid(), item);
	}
	
	public String parsePacket()
	{
		StringBuilder packet = new StringBuilder(10+items.size()*15);
		for(Item entry : items.values())
			packet.append('O').append(entry.parseItem()).append(';');
		if(kamas != 0)
			packet.append('G').append(kamas);
		return packet.toString();
	}
	
	public String parseItemToDatabase()
	{
		if(items.isEmpty())
			return "";
		
		StringBuilder builder = new StringBuilder(items.size()*5);
		boolean hasEntry = false;
		for(int uid : items.keySet())
		{
			if(!hasEntry)
				hasEntry = true;
			else
				builder.append(',');
			builder.append(uid);
		}
		return builder.toString();
	}
}
