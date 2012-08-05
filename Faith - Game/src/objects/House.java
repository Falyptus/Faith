package objects;

public class House {

	private int id;
	private int doorMapId;
	private int doorCellId;
	private int houseMapId;
	private int houseCellId;
	private String key;
	private int price;
	private int localOwner;
	private String localNameOwner;
	private int guildId;
	private String guildName;
	private String guildEmblem;
	private int guildRights;
	private int sale;
	private boolean isLocked;
	private boolean isShared;
	private int skills;
	
	
	public int getId() {
		return id;
	}

	public int getDoorMapId() {
		return doorMapId;
	}

	public int getDoorCellId() {
		return doorCellId;
	}

	public int getHouseMapId() {
		return houseMapId;
	}

	public int getHouseCellId() {
		return houseCellId;
	}

	public String getKey() {
		return key;
	}

	public int getPrice() {
		return price;
	}
	public void setPrice(final int price) {
		this.price = price;
	}
	public int getLocalOwner() {
		return localOwner;
	}
	public void setLocalOwner(final int localOwner) {
		this.localOwner = localOwner;
	}
	public String getLocalNameOwner() {
		return localNameOwner;
	}
	public void setLocalNameOwner(final String localNameOwner) {
		this.localNameOwner = localNameOwner;
	}
	public int getGuildId() {
		return guildId;
	}
	public void setGuildId(final int guildId) {
		this.guildId = guildId;
	}
	public String getGuildName() {
		return guildName;
	}
	public void setGuildName(final String guildName) {
		this.guildName = guildName;
	}
	public String getGuildEmblem() {
		return guildEmblem;
	}
	public void setGuildEmblem(final String guildEmblem) {
		this.guildEmblem = guildEmblem;
	}
	public int getGuildRights() {
		return guildRights;
	}
	public void setGuildRights(final int guildRights) {
		this.guildRights = guildRights;
	}
	public int getSale() {
		return sale;
	}
	public void setSale(final int sale) {
		this.sale = sale;
	}
	public boolean isLocked() {
		return isLocked;
	}
	public void setLocked(final boolean isLocked) {
		this.isLocked = isLocked;
	}
	public boolean isShared() {
		return isShared;
	}
	public void setShared(final boolean isShared) {
		this.isShared = isShared;
	}
	public int getSkills() {
		return skills;
	}
	public void setSkills(final int skills) {
		this.skills = skills;
	}
	
}
