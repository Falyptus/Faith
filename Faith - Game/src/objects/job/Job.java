package objects.job;


import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class Job {

	private final int _id;
	private final ArrayList<Integer> _tools = new ArrayList<Integer>();
	private final Map<Integer,ArrayList<Integer>> _crafts = new TreeMap<Integer,ArrayList<Integer>>();
	
	public Job(final int id,final String tools,final String crafts)
	{
		_id= id;
		if(!tools.equals(""))
		{
			for(final String str : tools.split(","))
			{
				try
				{
					final int tool = Integer.parseInt(str);
					_tools.add(tool);
				}catch(final Exception e){continue;};
			}
		}
		
		if(!crafts.equals(""))
		{
			for(final String str : crafts.split("\\|"))
			{
				try
				{
					final int skID = Integer.parseInt(str.split(";")[0]);
					final ArrayList<Integer> list = new ArrayList<Integer>();
					for(final String str2 : str.split(";")[1].split(","))list.add(Integer.parseInt(str2));
					_crafts.put(skID, list);
				}catch(final Exception e){continue;};
			}
		}
	}
	public ArrayList<Integer> getListBySkill(final int skID)
	{
		return _crafts.get(skID);
	}
	public boolean canCraft(final int skill,final int template)
	{
		if(_crafts.get(skill) != null)for(final int a : _crafts.get(skill))if(a == template)return true;
		return false;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public boolean isValidTool(final int t)
	{
		for(final int a : _tools)if(t == a)return true;
		return false;
	}
	
}
