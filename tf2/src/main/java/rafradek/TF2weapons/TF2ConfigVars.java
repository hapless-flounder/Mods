package rafradek.TF2weapons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;
import rafradek.TF2weapons.characters.EntityDemoman;
import rafradek.TF2weapons.characters.EntityEngineer;
import rafradek.TF2weapons.characters.EntityHeavy;
import rafradek.TF2weapons.characters.EntityPyro;
import rafradek.TF2weapons.characters.EntityScout;
import rafradek.TF2weapons.characters.EntitySniper;
import rafradek.TF2weapons.characters.EntitySoldier;
import rafradek.TF2weapons.characters.EntitySpy;

public class TF2ConfigVars {

	public static Configuration conf;
	public static int destTerrain;
	public static boolean medigunLock;
	public static boolean fastMetalProduction;
	public static boolean shootAttract;
	public static boolean disableSpawn;
	public static boolean disableBossSpawn;
	public static boolean disableInvasion;
	public static boolean disableLoot;
	public static int bossReappear;
	public static boolean disableContracts;
	public static boolean disableGeneration;
	public static boolean randomCrits;
	public static boolean overworldOnly;
	public static String spawnOres;
	public static String naturalCheck;
	public static String biomeCheck;
	public static float damageMultiplier;
	public static float healthScale;
	public static boolean dynamicLights;
	public static boolean dynamicLightsProj;
	public static boolean deadRingerTrigger;
	public static float medicChance;
	public static int sentryTargets;
	public static float dispenserRepair;
	public static boolean dispenserPlayers;
	public static boolean teleporterPlayers;
	public static boolean teleporterEntities;
	public static boolean enableUdp;
	public static boolean targetSentries;
	public static float dropAmmo;
	public static float speedMult;
	public static float armorMult;
	public static float mercenaryVolume;
	public static float bossVolume;
	public static float gunVolume;
	public static boolean enchantedExplosion;
	public static float maxEnergy;
	public static boolean buildingsUseEnergy;
	public static int sentryUseEnergy;
	public static int dispenserUseEnergy;
	public static int teleporterUseEnergy;
	public static Map<Class<? extends EntityLiving>, Integer> spawnRate;

	public static ArrayList<ResourceLocation> repairBlacklist;
	public static ArrayList<ResourceLocation> hostileBlacklist;
	public TF2ConfigVars() {
		// TODO Auto-generated constructor stub
	}

	public static void createConfig() {
		if(conf.get("gameplay", "Destructible terrain", "Upgrade only", "Explosions can destroy blocks").getType()==Property.Type.BOOLEAN)
			conf.getCategory("gameplay").remove("Destructible terrain");
		String destr=conf.get("gameplay", "Destructible terrain", "Upgrade only", "Explosions can destroy blocks").setValidValues(new String[] { "Always", "Upgrade only", "Never" }).getString();
		if(destr.equalsIgnoreCase("Always"))
			destTerrain=2;
		else if(destr.equalsIgnoreCase("Upgrade only"))
			destTerrain=1;
		else
			destTerrain=0;
		medigunLock = conf.getBoolean("Medigun lock target", "gameplay", false, "Left Click selects healing target");
		fastMetalProduction = conf.getBoolean("Fast metal production", "gameplay", false, "Dispensers produce metal every 5 seconds");
		
		disableSpawn = conf.getBoolean("Disable mob spawning", "spawn rate", false, "Disable mod-specific mobs spawning");
		overworldOnly = conf.getBoolean("Spawn only in overworld", "spawn rate", false, "Disable spawning in custom dimensions");
		biomeCheck = conf.get("spawn rate", "Spawn in biomes", "All","Default - biomes that spawn vanilla monsters").setValidValues(new String[] { "All", "Default", "Vanilla only" }).getString();
		disableBossSpawn = conf.getBoolean("Disable boss spawn", "spawn rate", false, "Disable random tf2 boss spawn");
		disableInvasion =conf.getBoolean("Disable invasion event", "spawn rate", false, "Disable invasion event");
		disableContracts =conf.getBoolean("Disable contracts", "gameplay", false, "Stop new contracts from appearing");
		disableGeneration = conf.getBoolean("Disable structures", "world gen", false, "Disable structures generation, such as Mann Co. building");
		disableLoot = conf.getBoolean("Disable chest loot", "world gen", false, "Disable chest loot generated by this mod");
		
		TF2weapons.weaponVersion = TF2weapons.conf.getInt("Weapon Config Version", "internal", TF2weapons.getCurrentWeaponVersion(), 0, 1000, "");
		conf.get("gameplay", "Disable structures", false).setRequiresMcRestart(true);
		
		boolean old = conf.hasKey("gameplay", "Natural mob detection");
		if (old) {
			conf.moveProperty("gameplay", "Natural mob detection", "mercenary");
		}
		
		naturalCheck = conf.get("mercenary", "Natural mob detection", "Always").setValidValues(new String[] { "Always", "Fast", "Never" }).getString();
		shootAttract = conf.getBoolean("Shooting attracts mobs", "gameplay", true, "Gunfire made by players attracts mobs");
		randomCrits = conf.getBoolean("Random critical hits", "gameplay", true, "Enables randomly appearing critical hits that deal 3x more damage");
		
		healthScale = conf.getInt("TF2 - Minecraft health translation", "gameplay", 200,-10000,10000, "How much 10 minecraft hearts are worth in TF2 health");
		damageMultiplier = 200f/healthScale;
		
		deadRingerTrigger = conf.getBoolean("Feign death events", "gameplay", true, "Does feign death trigger death events, set to false in case of mod conflicts");
		dynamicLights = conf.getBoolean("Dynamic Lights", "modcompatibility", true, "Enables custom light sources for AtomicStryker's Dynamic Lights mod")
				&& Loader.isModLoaded("dynamiclights");
		dynamicLightsProj = conf.getBoolean("Dynamic Lights - Projectiles", "modcompatibility", true, "Should projectiles emit light");
		bossReappear = conf.getInt("Boss respawn cooldown", "gameplay", 360000, 0, Integer.MAX_VALUE, "Maximum boss reappear time in ticks. Bosses always spawn in full moon");
		
		dispenserRepair = conf.getFloat("Dispenser repair rate", "gameplay", 3, 0, 10000, "Repair per 1 metal. Reduced by enchants");
		String[] blacklist = conf.getStringList("Repair blacklist", "gameplay", new String[0], "Item IDs that should not be repairable by dispensers");
		String[] hostileblacklist = conf.getStringList("Hostile entity blacklist", "gameplay", new String[] {
				"minecraft:enderman", "minecraft:zombie_pigman"
		}, "Entity IDs that should not be considered hostile");
		
		enableUdp = conf.getBoolean("Enable UDP (experimental)", "gameplay", false, "");
		targetSentries = conf.getBoolean("Mobs target sentries", "gameplay", true, "Mobs will attack sentries that dont shoot");
		bossVolume = conf.getFloat("Boss volume (radius)", "sound volume", 4f, 0, 10, "Values above 1 increase sound radius only");
		mercenaryVolume = conf.getFloat("Mercenary volume (radius)", "sound volume", 0.6f, 0, 10, "Values above 1 increase sound radius only");
		gunVolume = conf.getFloat("Gun volume (radius)", "sound volume", 2f, 0, 10, "Applies only to players, values above 1 increase sound radius only");
		enchantedExplosion = conf.getBoolean("Enchanted blast jumping", "gameplay", true, "Strafing, no air resistance and reduced gravity when blast jumping");
		dropAmmo = conf.getFloat("Ammo drop chance", "gameplay", 0.15f, 0f, 1f, "Chance of dropping ammo from non-TF2 hostile creature");
		speedMult = conf.getFloat("Mercenary speed multiplier", "mercenary", 0.8f, 0f, 2f, "Speed multiplier of mercenaries. Does not apply to owned mercenaries");
		armorMult = conf.getFloat("Armored mercenary chance", "mercenary", 0.06f, 0f, 10f, "Base chance of armored mercenaries. Altered by difficulty level");
		
		buildingsUseEnergy = conf.getBoolean("Buildings use energy", "gameplay", false, "");
		sentryUseEnergy = conf.getInt("Sentry energy use", "gameplay", 100, 0, 40000, "Energy use on attack");
		dispenserUseEnergy = conf.getInt("Dispenser energy use", "gameplay", 15, 0, 40000, "Energy use on repairs and heals");
		teleporterUseEnergy = conf.getInt("Teleport energy use", "gameplay", 20000, 0, 40000, "Energy use on teleport");
		
		if(!buildingsUseEnergy) {
			sentryUseEnergy = 0;
			dispenserUseEnergy = 0;
			teleporterUseEnergy = 0;
		}
		
		conf.getBoolean("Attack on hurt", "default building targets", true, "");
		conf.getBoolean("Attack other players", "default building targets", false, "");
		conf.getBoolean("Attack hostile mobs", "default building targets", true, "");
		conf.getBoolean("Attack friendly creatures", "default building targets", false, "");
		conf.getBoolean("Dispensers heal neutral players", "default building targets", true, "");
		conf.getBoolean("Neutral players can teleport", "default building targets", true, "");
		conf.getBoolean("Entities can teleport", "default building targets", true, "");
		
		spawnRate = new HashMap<>();
		spawnRate.put(EntityScout.class, conf.getInt("Scout", "spawn rate", 12, 0, 1000, ""));
		spawnRate.put(EntityPyro.class, conf.getInt("Pyro", "spawn rate", 12, 0, 1000, ""));
		spawnRate.put(EntitySoldier.class, conf.getInt("Soldier", "spawn rate", 12, 0, 1000, ""));
		spawnRate.put(EntityHeavy.class, conf.getInt("Heavy", "spawn rate", 12, 0, 1000, ""));
		spawnRate.put(EntityDemoman.class, conf.getInt("Demoman", "spawn rate", 12, 0, 1000, ""));
		spawnRate.put(EntitySpy.class, conf.getInt("Spy", "spawn rate", 9, 0, 1000, ""));
		spawnRate.put(EntityEngineer.class, conf.getInt("Engineer", "spawn rate", 9, 0, 1000, ""));
		spawnRate.put(EntitySniper.class, conf.getInt("Sniper", "spawn rate", 9, 0, 1000, ""));
		TF2weapons.updateOreGenStatus();
	
		TF2weapons.updateMobSpawning();
		medicChance = conf.getFloat("Medic spawn chance", "spawn rate", 1, 0, 1000, "Medic spawn chance multiplier");
		
		repairBlacklist = new ArrayList<>();
		for(String id : blacklist) {
			repairBlacklist.add(new ResourceLocation(id));
		}
		hostileBlacklist = new ArrayList<>();
		for(String id : hostileblacklist) {
			hostileBlacklist.add(new ResourceLocation(id));
		}
		
		if (conf.hasChanged())
			conf.save();
	}

}
