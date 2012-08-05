package objects.entity;

import common.World.ItemSet;

import objects.account.Account;
import objects.account.EnemyList;
import objects.account.FriendList;
import objects.alignment.Alignment;
import objects.alignment.Prism;
import objects.bigstore.BigStore;
import objects.bigstore.BigStoreEntry;
import objects.bigstore.Category;
import objects.bigstore.Line;
import objects.bigstore.Template;
import objects.character.Breed;
import objects.character.BreedSpell;
import objects.character.Player;
import objects.fight.Fight;
import objects.fight.Fighter;
import objects.fight.Glyph;
import objects.fight.Trap;
import objects.guild.Guild;
import objects.guild.GuildMember;
import objects.guild.TaxCollector;
import objects.item.Gift;
import objects.item.Item;
import objects.item.ItemTemplate;
import objects.item.PackObject;
import objects.item.Pet;
import objects.item.SoulStone;
import objects.item.Speaking;
import objects.job.Job;
import objects.job.JobAction;
import objects.job.JobStat;
import objects.map.DofusCell;
import objects.map.DofusMap;
import objects.map.InteractiveObject;
import objects.map.MountPark;
import objects.monster.Monster;
import objects.monster.MonsterGrade;
import objects.monster.MonsterGroup;
import objects.npc.Npc;
import objects.npc.NpcQuestion;
import objects.npc.NpcResponse;
import objects.npc.NpcTemplate;
import objects.spell.Spell;
import objects.spell.SpellEffect;
import objects.spell.SpellStat;

public interface IEntityFactory {

	Account newAccount();
	FriendList newFriendList();
	EnemyList newEnemyList();
	
	Alignment newAlignment();
	
	BigStore newBigStore();
	BigStoreEntry newBigStoreEntry();
	Category newCategory();
	Line newLine();
	Template newTemplate();
	
	DofusMap newMap();
	DofusCell newCell();
	InteractiveObject newInteractiveObject();
	MountPark newMountPark();
	
	Fight newFight();
	Fighter newFighter();
	Glyph newGlyph();
	Trap newTrap();
	
	Guild newGuild();
	GuildMember newGuildMember();
	TaxCollector newTaxCollector();
	
	Item newItem();
	ItemSet newItemSet();
	ItemTemplate newItemTemplate();
	Gift newGift();
	PackObject newPackObject();
	Pet newPet();
	SoulStone newSoulStone();
	Speaking newSpeaking();
	
	Job newJob();
	JobAction newJobAction();
	JobStat newJobStat();
	
	Monster newMonster();
	MonsterGrade newMonsterGrade();
	MonsterGroup newMonsterGroup();
	
	Npc newNpc();
	NpcQuestion newNpcQuestion();
	NpcResponse newNpcResponse();
	NpcTemplate newNpcTemplate();
	
	//To remake
	/*Quest newQuest();
	QuestObjective newQuestObjective();
	QuestStep newQuestStep();*/
	
	Player newPlayer();
	Breed newBreed();
	BreedSpell newBreedSpell();
	
	Prism newPrism();
	
	Spell newSpell();
	SpellEffect newSpellEffect();
	SpellStat newSpellStat();
	
	
	
}
