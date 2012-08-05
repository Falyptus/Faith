package objects.alignment;

public class Alignment {

	private final int id;
	private int subAreasOwned;
	private int areasOwned;
	
	public Alignment(final int id)
	{
		this.id = id;
	}
	
	public int getId()
	{
		return id;
	}
	
	public int getSubAreasOwned()
	{
		return subAreasOwned;
	}
	
	public int getAreasOwned()
	{
		return areasOwned;
	}
	
	public void addSubAreasOwned()
	{
		subAreasOwned++;
	}
	
	public void addAreasOwned()
	{
		areasOwned++;
	}
	
}
