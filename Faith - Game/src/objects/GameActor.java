package objects;

public interface GameActor {
	
	public int getActorId();
	public int getActorType();
	public byte getOrientation();
	public int getMapId();
	public int getCellId();
	
	public enum GameActorTypeEnum
    {
        TYPE_CHARACTER (0),
        TYPE_MONSTER (-3),
        TYPE_NPC (-4),
        TYPE_MERCHANT (-5),
        TYPE_TAX_COLLECTOR (-6),
        TYPE_MUTANT (-8),
        TYPE_MOUNT_PARK (-9),
        TYPE_PRISM (-10);
        
        private int actorType = 0;
        
        private GameActorTypeEnum(int actorType)
        {
        	this.actorType = actorType;
        }
        
        public int getActorType()
        {
        	return actorType;
        }
    }

}
