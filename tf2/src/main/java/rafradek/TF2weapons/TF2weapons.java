package rafradek.TF2weapons;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockOre;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.BehaviorProjectileDispense;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatBasic;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.IFuelHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.StartupQuery;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.Mod.Metadata;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.IForgeRegistryModifiable;
import rafradek.TF2weapons.block.BlockAmmoFurnace;
import rafradek.TF2weapons.block.BlockCabinet;
import rafradek.TF2weapons.block.BlockOverheadDoor;
import rafradek.TF2weapons.block.BlockProp;
import rafradek.TF2weapons.block.BlockRobotDeploy;
import rafradek.TF2weapons.block.BlockUpgradeStation;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.client.gui.inventory.GuiAmmoFurnace;
import rafradek.TF2weapons.client.gui.inventory.GuiDispenser;
import rafradek.TF2weapons.client.gui.inventory.GuiMercenary;
import rafradek.TF2weapons.client.gui.inventory.GuiRobotDeploy;
import rafradek.TF2weapons.client.gui.inventory.GuiSentry;
import rafradek.TF2weapons.client.gui.inventory.GuiTF2Crafting;
import rafradek.TF2weapons.client.gui.inventory.GuiTeleporter;
import rafradek.TF2weapons.client.gui.inventory.GuiUpgradeStation;
import rafradek.TF2weapons.client.gui.inventory.GuiWearables;
import rafradek.TF2weapons.command.CommandChangeConfig;
import rafradek.TF2weapons.command.CommandClientDebug;
import rafradek.TF2weapons.command.CommandForceClass;
import rafradek.TF2weapons.command.CommandGenerateReferences;
import rafradek.TF2weapons.command.CommandGiveWeapon;
import rafradek.TF2weapons.command.CommandInvasion;
import rafradek.TF2weapons.command.CommandResetStat;
import rafradek.TF2weapons.command.CommandResetWeapons;
import rafradek.TF2weapons.common.CommonProxy;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.common.TF2Achievements;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.EntityDummy;
import rafradek.TF2weapons.entity.EntityLightDynamic;
import rafradek.TF2weapons.entity.EntityStatue;
import rafradek.TF2weapons.entity.EntityTarget;
import rafradek.TF2weapons.entity.boss.EntityHHH;
import rafradek.TF2weapons.entity.boss.EntityMerasmus;
import rafradek.TF2weapons.entity.boss.EntityMonoculus;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.building.EntityDispenser;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.entity.building.EntityTeleporter;
import rafradek.TF2weapons.entity.building.EntityTeleporter.TeleporterData;
import rafradek.TF2weapons.entity.mercenary.EntityDemoman;
import rafradek.TF2weapons.entity.mercenary.EntityEngineer;
import rafradek.TF2weapons.entity.mercenary.EntityHeavy;
import rafradek.TF2weapons.entity.mercenary.EntityMedic;
import rafradek.TF2weapons.entity.mercenary.EntityPyro;
import rafradek.TF2weapons.entity.mercenary.EntitySaxtonHale;
import rafradek.TF2weapons.entity.mercenary.EntityScout;
import rafradek.TF2weapons.entity.mercenary.EntitySniper;
import rafradek.TF2weapons.entity.mercenary.EntitySoldier;
import rafradek.TF2weapons.entity.mercenary.EntitySpy;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.entity.mercenary.InvasionEvent;
import rafradek.TF2weapons.entity.mercenary.Squad;
import rafradek.TF2weapons.entity.projectile.EntityBall;
import rafradek.TF2weapons.entity.projectile.EntityCleaver;
import rafradek.TF2weapons.entity.projectile.EntityFlame;
import rafradek.TF2weapons.entity.projectile.EntityFlare;
import rafradek.TF2weapons.entity.projectile.EntityFuryFireball;
import rafradek.TF2weapons.entity.projectile.EntityGrenade;
import rafradek.TF2weapons.entity.projectile.EntityJar;
import rafradek.TF2weapons.entity.projectile.EntityOnyx;
import rafradek.TF2weapons.entity.projectile.EntityProjectileEnergy;
import rafradek.TF2weapons.entity.projectile.EntityProjectileSimple;
import rafradek.TF2weapons.entity.projectile.EntityRocket;
import rafradek.TF2weapons.entity.projectile.EntityStickProjectile;
import rafradek.TF2weapons.entity.projectile.EntityStickybomb;
import rafradek.TF2weapons.inventory.ContainerAmmoFurnace;
import rafradek.TF2weapons.inventory.ContainerDispenser;
import rafradek.TF2weapons.inventory.ContainerEnergy;
import rafradek.TF2weapons.inventory.ContainerMercenary;
import rafradek.TF2weapons.inventory.ContainerRobotDeploy;
import rafradek.TF2weapons.inventory.ContainerTF2Workbench;
import rafradek.TF2weapons.inventory.ContainerUpgrades;
import rafradek.TF2weapons.inventory.ContainerWearables;
import rafradek.TF2weapons.inventory.InventoryAmmoBelt;
import rafradek.TF2weapons.inventory.InventoryWearables;
import rafradek.TF2weapons.item.ItemAmmo;
import rafradek.TF2weapons.item.ItemAmmoBelt;
import rafradek.TF2weapons.item.ItemAmmoPackage;
import rafradek.TF2weapons.item.ItemArmorTF2;
import rafradek.TF2weapons.item.ItemBossSpawner;
import rafradek.TF2weapons.item.ItemBuildingBox;
import rafradek.TF2weapons.item.ItemCrate;
import rafradek.TF2weapons.item.ItemDisguiseKit;
import rafradek.TF2weapons.item.ItemDoorController;
import rafradek.TF2weapons.item.ItemEventMaker;
import rafradek.TF2weapons.item.ItemFireAmmo;
import rafradek.TF2weapons.item.ItemFoodThrowable;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemHorn;
import rafradek.TF2weapons.item.ItemKillstreakFabricator;
import rafradek.TF2weapons.item.ItemKillstreakKit;
import rafradek.TF2weapons.item.ItemMonsterPlacerPlus;
import rafradek.TF2weapons.item.ItemPDA;
import rafradek.TF2weapons.item.ItemRobotPart;
import rafradek.TF2weapons.item.ItemStatue;
import rafradek.TF2weapons.item.ItemStrangifier;
import rafradek.TF2weapons.item.ItemTF2;
import rafradek.TF2weapons.item.ItemTarget;
import rafradek.TF2weapons.item.ItemToken;
import rafradek.TF2weapons.item.ItemUsable;
import rafradek.TF2weapons.item.crafting.OpenCrateRecipe;
import rafradek.TF2weapons.item.crafting.TF2CraftingManager;
import rafradek.TF2weapons.loot.EntityBuildingFunction;
import rafradek.TF2weapons.loot.EntityNBTCondition;
import rafradek.TF2weapons.loot.EntityOfClassFunction;
import rafradek.TF2weapons.loot.EntityOrCondition;
import rafradek.TF2weapons.loot.KilledByTeam;
import rafradek.TF2weapons.loot.RandomWeaponFunction;
import rafradek.TF2weapons.message.TF2ActionHandler;
import rafradek.TF2weapons.message.TF2AttackSyncHandler;
import rafradek.TF2weapons.message.TF2BulletHandler;
import rafradek.TF2weapons.message.TF2CapabilityHandler;
import rafradek.TF2weapons.message.TF2ContractHandler;
import rafradek.TF2weapons.message.TF2DisguiseHandler;
import rafradek.TF2weapons.message.TF2EffectCooldownHandler;
import rafradek.TF2weapons.message.TF2GuiConfigHandler;
import rafradek.TF2weapons.message.TF2InitClientHandler;
import rafradek.TF2weapons.message.TF2InitHandler;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.message.TF2NetworkWrapper;
import rafradek.TF2weapons.message.TF2ProjectileHandler;
import rafradek.TF2weapons.message.TF2ParticleSpawnHandler;
import rafradek.TF2weapons.message.TF2PlayerCapabilityHandler;
import rafradek.TF2weapons.message.TF2PropertyHandler;
import rafradek.TF2weapons.message.TF2ShowGuiHandler;
import rafradek.TF2weapons.message.TF2UseHandler;
import rafradek.TF2weapons.message.TF2VelocityAddHandler;
import rafradek.TF2weapons.message.TF2WeaponDataHandler;
import rafradek.TF2weapons.message.TF2WeaponDropHandler;
import rafradek.TF2weapons.message.TF2WearableChangeHandler;
import rafradek.TF2weapons.message.udp.TF2UdpClient;
import rafradek.TF2weapons.message.udp.TF2UdpServer;
import rafradek.TF2weapons.potion.PotionTF2;
import rafradek.TF2weapons.potion.PotionTF2Item;
import rafradek.TF2weapons.tileentity.TileEntityAmmoFurnace;
import rafradek.TF2weapons.tileentity.TileEntityOverheadDoor;
import rafradek.TF2weapons.tileentity.TileEntityRobotDeploy;
import rafradek.TF2weapons.tileentity.TileEntityUpgrades;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Util;
import rafradek.TF2weapons.util.WeaponData;
import rafradek.TF2weapons.world.gen.structure.MannCoBuilding;
import rafradek.TF2weapons.world.gen.structure.ScatteredFeatureTF2Base;

@Mod(modid = TF2weapons.MOD_ID, name = "TF2 Stuff", version = "1.5.10", guiFactory = "rafradek.TF2weapons.client.gui.TF2GuiFactory", acceptedMinecraftVersions = "[1.12, 1.13)", 
dependencies = "after:dynamiclights;after:thermalexpansion", updateJSON="https://rafradek.github.io/tf2stuffmod.json")
public class TF2weapons {

	public static final Logger LOGGER = LogManager.getLogger("TF2 Stuff Mod");
	public static final String MOD_ID = "rafradek_tf2_weapons";
	@Metadata(MOD_ID)
	public static ModMetadata metadata;

	@CapabilityInject(WeaponsCapability.class)
	public static final Capability<WeaponsCapability> WEAPONS_CAP = null;

	@CapabilityInject(InventoryWearables.class)
	public static final Capability<InventoryWearables> INVENTORY_CAP = null;

	@CapabilityInject(InventoryAmmoBelt.class)
	public static final Capability<InventoryAmmoBelt> INVENTORY_BELT_CAP = null;
	
	@CapabilityInject(TF2EventsCommon.TF2WorldStorage.class)
	public static final Capability<TF2EventsCommon.TF2WorldStorage> WORLD_CAP = null;

	@CapabilityInject(WeaponData.WeaponDataCapability.class)
	public static final Capability<WeaponData.WeaponDataCapability> WEAPONS_DATA_CAP = null;
	
	@CapabilityInject(TF2PlayerCapability.class)
	public static final Capability<TF2PlayerCapability> PLAYER_CAP = null;
	
	public static TF2UdpServer udpServer;

	public int[] itemid = new int[9];
	public static Configuration conf;

	public static CreativeTabs tabutilitytf2;
	public static CreativeTabs tabweapontf2;
	public static CreativeTabs tabsurvivaltf2;
	public static CreativeTabs tabspawnertf2;
	// public static final ArmorMaterial OPARMOR =
	// EnumHelper.addArmorMaterial("OPARMOR", "", 1000, new int[] {24,0,0,0},
	// 100);
	public static TF2NetworkWrapper network;
	public static Item itemPlacer;
	public static Item mobHeldItem;

	static int weaponVersion;

	@Instance(value = MOD_ID)
	public static TF2weapons instance;

	public File weaponDir;

	public static Block blockCabinet;
	public static Block blockCopperOre;
	public static Block blockProp;
	public static Block blockLeadOre;
	public static Block blockAustraliumOre;
	public static Block blockAustralium;
	public static Block blockUpgradeStation;
	public static Block blockAmmoFurnace;
	public static Block blockOverheadDoor;
	public static Block blockRobotDeploy;
	
	public static boolean generateCopper;
	public static boolean generateLead;
	public static boolean generateAustralium;

	public static Potion bonk;
	public static Potion stun;
	public static Potion crit;
	public static Potion buffbanner;
	public static Potion backup;
	public static Potion conch;
	public static Potion markDeath;
	public static Potion jarate;
	public static Potion madmilk;
	public static Potion critBoost;
	public static Potion charging;
	public static Potion uber;
	public static Potion it;
	public static Potion bombmrs;
	public static Potion bleeding;
	public static Potion noKnockback;
	public static Potion sapped;
	
	public static Item itemDisguiseKit;
	public static Item itemBuildingBox;
	public static Item itemSandvich;
	public static Item itemChocolate;
	public static Item itemAmmo;
	public static Item itemAmmoFire;
	public static Item itemAmmoPistol;
	public static Item itemAmmoMinigun;
	public static Item itemAmmoSMG;
	public static Item itemAmmoSyringe;
	public static Item itemAmmoPackage;
	public static Item itemAmmoMedigun;
	public static Item itemAmmoBelt;
	public static Item itemScoutBoots;
	public static Item itemMantreads;
	public static Item itemTF2;
	public static Item itemHorn;
	public static Item itemStatue;
	public static Item itemToken;
	public static Item itemTarget;
	public static Item itemPDA;
	public static Item itemKillstreak;
	public static Item itemGunboats;
	public static Item itemStrangifier;
	public static Item itemKillstreakFabricator;
	public static Item itemEventMaker;
	public static Item itemRobotPart;
	public static Item itemBossSpawn;
	public static Item itemDoorController;
	
	public static ResourceLocation lootTF2Character;
	public static ResourceLocation lootScout;
	public static ResourceLocation lootSpy;
	public static ResourceLocation lootHeavy;
	public static ResourceLocation lootEngineer;
	public static ResourceLocation lootMedic;
	public static ResourceLocation lootPyro;
	public static ResourceLocation lootSoldier;
	public static ResourceLocation lootDemoman;
	public static ResourceLocation lootSniper;
	public static ResourceLocation lootHale;
	public static ResourceLocation lootTF2Base;

	public static byte[] itemDataCompressed;
	public static GZIPOutputStream out;
	
	public static StatBase cratesOpened;
	public static StatBase robotsKilled;
	public static MinecraftServer server;
	public static EntityDummy dummyEnt;
	
	public static BannerPattern redPattern;
	public static BannerPattern bluPattern;
	public static BannerPattern neutralPattern;
	public static BannerPattern fastSpawn;
	public static ArrayList<ResourceLocation> animals;
	
	public static Fluid refinedFuel;
	public static boolean squakeLoaded;
	public static boolean corrupted;
	
	@SidedProxy(clientSide = "rafradek.TF2weapons.client.ClientProxy", serverSide = "rafradek.TF2weapons.common.CommonProxy")
	public static CommonProxy proxy;
	
	public static int getCurrentWeaponVersion() {
		return 47;
	}

	@Mod.EventHandler
	public void preinit(FMLPreInitializationEvent event) {
		LOGGER.info("Initializing config files");
		this.weaponDir = new File(event.getModConfigurationDirectory(), "TF2WeaponsLists");
		if (!this.weaponDir.exists())
			this.weaponDir.mkdirs();
		metadata.autogenerated = false;

		conf = new Configuration(event.getSuggestedConfigurationFile());
		TF2ConfigVars.conf = conf;
		boolean shouldCopy = false;
		if (!conf.hasKey("internal", "Weapon Config Version"))
			shouldCopy = true;
		TF2ConfigVars.createConfig(true);
		File file = event.getSourceFile();
		File squadFile = new File(event.getModConfigurationDirectory(), "TF2RobotSquad.json");
		// System.out.println("LOLOLOLOLOLOL "+file.getAbsolutePath());
		// System.out.println("LOLOLOLOLOLOL2
		// "+event.getModConfigurationDirectory());
		// System.out.println("Istnieje? "+outputFile.exists());
		
		
		if (weaponVersion < getCurrentWeaponVersion() || !file.isFile())
			shouldCopy = true;
		if (!new File(this.weaponDir, "Weapons.json").exists() || shouldCopy ) {
			conf.get("internal", "Weapon Config Version", getCurrentWeaponVersion()).set(getCurrentWeaponVersion());
			conf.save();
			TF2Util.extractData("Weapons.json", new File(this.weaponDir, "Weapons.json"), file);
			TF2Util.extractData("Cosmetics.json", new File(this.weaponDir, "Cosmetics.json"), file);
			TF2Util.extractData("Crates.json", new File(this.weaponDir, "Crates.json"), file);
			TF2Util.extractData("TF2RobotSquad.json", squadFile, file);
		}

		
		bluPattern=EnumHelper.addEnum(BannerPattern.class, "BLU_PATTERN", new Class<?>[]{String.class,String.class}, "blu_base","bb");
		redPattern=EnumHelper.addEnum(BannerPattern.class, "RED_PATTERN", new Class<?>[]{String.class,String.class}, "red_base","rb");
		neutralPattern=EnumHelper.addEnum(BannerPattern.class, "NEUTRAL_PATTERN", new Class<?>[]{String.class,String.class}, "neutral_base","nb");
		fastSpawn=EnumHelper.addEnum(BannerPattern.class, "FAST_SPAWN", new Class<?>[]{String.class,String.class}, "fast_spawn","fs");
		
		tabweapontf2 = new CreativeTabs("tf2weapons") {
			@Override
			public ItemStack getTabIconItem() {
				return ItemFromData.getNewStack("minigun");
			}
			
		};
		tabspawnertf2 = new CreativeTabs("tf2spawner") {
			@Override
			public ItemStack getTabIconItem() {
				return new ItemStack(itemBuildingBox, 1, 18);
			}
			
		};
		tabutilitytf2 = new CreativeTabs("tf2util") {
			@Override
			public ItemStack getTabIconItem() {
				return new ItemStack(itemDisguiseKit);
			}
			
			@SideOnly(Side.CLIENT)
		    public void displayAllRelevantItems(NonNullList<ItemStack> list) {
				super.displayAllRelevantItems(list);
				for(int i=0;i<3;i++){
					for(int j=0;j<2;j++){
						if (i == 2 && j == 1)
							continue;
						ItemStack banner=new ItemStack(Items.BANNER,1,(i==0?EnumDyeColor.RED:(i == 1 ? EnumDyeColor.BLUE : EnumDyeColor.GRAY)).getDyeDamage());
						NBTTagList patterns=new NBTTagList();
						banner.getOrCreateSubCompound("BlockEntityTag").setTag("Patterns", patterns);
						NBTTagCompound pattern=new NBTTagCompound();
						pattern.setInteger("Color", 15);
						if(i==0){
							pattern.setString("Pattern", "rb");
						}
						else if (i == 1) {
							pattern.setString("Pattern", "bb");
						}
						else if (i == 2) {
							pattern.setString("Pattern", "nb");
						}
						
						patterns.appendTag(pattern);
						if(j==1){
							NBTTagCompound patternfast=new NBTTagCompound();
							patternfast.setString("Pattern", "fs");
							patternfast.setInteger("Color", 15);
							patterns.appendTag(patternfast);
						}
						
						list.add(banner);
					}
				}
		    }
		};
		tabsurvivaltf2 = new CreativeTabs("tf2misc") {
			@Override
			public ItemStack getTabIconItem() {
				return new ItemStack(Item.getItemFromBlock(blockCabinet));
			}
		};
		
		LOGGER.info("Initializing attributes");
		MapList.initMaps();
		TF2Attribute.initAttributes();
		
		/*File refFile=new File(event.getModConfigurationDirectory(), "TF2References");
		if(!refFile.exists())
			refFile.mkdirs();
		try {
			BufferedWriter attributes=new BufferedWriter(new FileWriter(new File(refFile,"attributes.txt")));
			attributes.write("ID - Name - Effect - State - Type - Default\n");
			for(int i=0;i<TF2Attribute.attributes.length;i++){
				TF2Attribute attr=TF2Attribute.attributes[i];
				if(attr != null){
					attributes.write(attr.id+" - "+attr.name+" - "+attr.effect+" - "+attr.state.toString()+" - "+attr.typeOfValue.toString()+" - "+attr.defaultValue+"\n");
				}
			}
			attributes.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		LOGGER.info("Registering");
		
		// EntityRegistry.registerModEntity(EntityBullet.class, "bullet", 1,
		// this, 256, 100, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"heavy"),EntityHeavy.class, "heavy", 2, this, 80, TF2ConfigVars.updateDelay, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"scout"),EntityScout.class, "scout", 3, this, 80, TF2ConfigVars.updateDelay, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"sniper"),EntitySniper.class, "sniper", 4, this, 80, TF2ConfigVars.updateDelay, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"soldier"),EntitySoldier.class, "soldier", 5, this, 80, TF2ConfigVars.updateDelay, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"pyro"),EntityPyro.class, "pyro", 6, this, 80, TF2ConfigVars.updateDelay, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"demoman"),EntityDemoman.class, "demoman", 7, this, 80, TF2ConfigVars.updateDelay, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"medic"),EntityMedic.class, "medic", 8, this, 80, TF2ConfigVars.updateDelay, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"spy"),EntitySpy.class, "spy", 9, this, 80, TF2ConfigVars.updateDelay, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"engineer"),EntityEngineer.class, "engineer", 10, this, 80, TF2ConfigVars.updateDelay, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"rocket"),EntityRocket.class, "rocket", 11, this, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"flame"),EntityFlame.class, "flame", 12, this, 0, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"grenade"),EntityGrenade.class, "grenade", 13, this, 64, 5, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"sticky"),EntityStickybomb.class, "sticky", 14, this, 64, 5, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"simple"),EntityProjectileSimple.class, "simple", 26, this, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"syringe"),EntityStickProjectile.class, "syringe", 15, this, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"flare"),EntityFlare.class, "flare", 20, this, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"jar"),EntityJar.class, "jar", 21, this, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"ball"),EntityBall.class, "ball", 22, this, 64, 10, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"sentry"),EntitySentry.class, "sentry", 16, this, 80, 2, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"dispenser"),EntityDispenser.class, "dispenser", 17, this, 80, 40, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"teleporter"),EntityTeleporter.class, "teleporter", 18, this, 80, 40, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"hale"),EntitySaxtonHale.class, "hale", 19, this, 80, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"monoculus"),EntityMonoculus.class, "monoculus", 23, this, 80, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"hhh"),EntityHHH.class, "hhh", 24, this, 80, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"merasmus"),EntityMerasmus.class, "merasmus", 25, this, 80, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"cleaver"),EntityCleaver.class, "cleaver", 27, this, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"target"),EntityTarget.class, "target", 28, this, 80, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"statue"),EntityStatue.class, "statue", 29, this, 80, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"projenergy"),EntityProjectileEnergy.class, "projenergy", 30, this, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"onyx"),EntityOnyx.class, "onyx", 31, this, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"fireball"),EntityFuryFireball.class, "fireball", 32, this, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"light"),EntityLightDynamic.class, "light", 33, this, 256, 20, false);
		// GameRegistry.registerItem(new ItemArmor(TF2weapons.OPARMOR, 3,
		// 0).setUnlocalizedName("oparmor").setTextureName("diamond_helmet").setCreativeTab(tabtf2),"oparmor");
		ForgeRegistries.ITEMS.register(itemPlacer = new ItemMonsterPlacerPlus().setUnlocalizedName("monsterPlacer").setRegistryName(TF2weapons.MOD_ID + ":placer"));
		ForgeRegistries.ITEMS.register(itemDisguiseKit = new ItemDisguiseKit().setUnlocalizedName("disguiseKit").setRegistryName(TF2weapons.MOD_ID + ":disguise_kit"));
		ForgeRegistries.ITEMS.register(itemBuildingBox = new ItemBuildingBox().setUnlocalizedName("buildingBox").setRegistryName(TF2weapons.MOD_ID + ":building_box"));
		ForgeRegistries.ITEMS.register(itemSandvich = new ItemFoodThrowable(14, 2F, false, 600).setPotionEffect(new PotionEffect(MobEffects.REGENERATION, 120, 2), 1f).setUnlocalizedName("sandvich")
				.setCreativeTab(tabutilitytf2).setRegistryName(TF2weapons.MOD_ID + ":sandvich"));
		ForgeRegistries.ITEMS.register(itemChocolate = new ItemFoodThrowable(7, 1.5F, false, 200).setPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 600, 0), 1f).setUnlocalizedName("chocolate")
				.setCreativeTab(tabutilitytf2).setRegistryName(TF2weapons.MOD_ID + ":chocolate"));
		ForgeRegistries.ITEMS.register(itemHorn = new ItemHorn().setUnlocalizedName("horn").setRegistryName(TF2weapons.MOD_ID + ":horn"));
		ForgeRegistries.ITEMS.register(itemAmmo = new ItemAmmo().setUnlocalizedName("tf2ammo").setRegistryName(TF2weapons.MOD_ID + ":ammo"));
		ForgeRegistries.ITEMS.register(itemAmmoPackage = new ItemAmmoPackage().setUnlocalizedName("tf2ammobox").setRegistryName(TF2weapons.MOD_ID + ":ammo_box"));
		ForgeRegistries.ITEMS.register(itemAmmoFire = new ItemFireAmmo(10, 350, 1).setUnlocalizedName("tf2ammo").setRegistryName(TF2weapons.MOD_ID + ":ammo_fire"));
		ForgeRegistries.ITEMS.register(itemAmmoMedigun = new ItemFireAmmo(12, 1400, 1).setUnlocalizedName("tf2ammo").setRegistryName(TF2weapons.MOD_ID + ":ammo_medigun"));
		ForgeRegistries.ITEMS.register(itemAmmoPistol = new ItemFireAmmo(3, 12, 12).setUnlocalizedName("tf2ammo").setRegistryName(TF2weapons.MOD_ID + ":ammo_pistol"));
		ForgeRegistries.ITEMS.register(itemAmmoMinigun = new ItemFireAmmo(2, 100, 2).setUnlocalizedName("tf2ammo").setRegistryName(TF2weapons.MOD_ID + ":ammo_minigun"));
		ForgeRegistries.ITEMS.register(itemAmmoSMG = new ItemFireAmmo(5, 25, 8).setUnlocalizedName("tf2ammo").setRegistryName(TF2weapons.MOD_ID + ":ammo_smg"));
		ForgeRegistries.ITEMS.register(itemAmmoSyringe = new ItemFireAmmo(9, 40, 6).setUnlocalizedName("tf2ammo").setRegistryName(TF2weapons.MOD_ID + ":ammo_syringe"));
		ForgeRegistries.ITEMS.register(itemAmmoBelt = new ItemAmmoBelt().setUnlocalizedName("ammoBelt").setRegistryName(TF2weapons.MOD_ID + ":ammo_belt").setCreativeTab(tabsurvivaltf2));
		ForgeRegistries.ITEMS.register(itemScoutBoots = new ItemArmorTF2(ArmorMaterial.LEATHER, 0, EntityEquipmentSlot.FEET,"Allows double jumping",0f)
				.setUnlocalizedName("scoutBoots").setRegistryName(TF2weapons.MOD_ID + ":scout_shoes").setCreativeTab(tabutilitytf2));
		ForgeRegistries.ITEMS.register(itemMantreads = new ItemArmorTF2(ArmorMaterial.IRON, 0, EntityEquipmentSlot.FEET,"Deals 1.8x falling damage to the player you land on",0.75f)
				.setUnlocalizedName("mantreads").setRegistryName(TF2weapons.MOD_ID + ":mantreads").setCreativeTab(tabutilitytf2));
		ForgeRegistries.ITEMS.register(itemGunboats = new ItemArmorTF2(ArmorMaterial.IRON, 0, EntityEquipmentSlot.FEET,"Reduces blast jumping damage by 60%",0f)
				.setUnlocalizedName("gunboats").setRegistryName(TF2weapons.MOD_ID + ":gunboats").setCreativeTab(tabutilitytf2));
		ForgeRegistries.ITEMS.register(itemStatue = new ItemStatue().setRegistryName(TF2weapons.MOD_ID +":statue"));
		ForgeRegistries.ITEMS.register(itemToken = new ItemToken().setRegistryName(TF2weapons.MOD_ID, "token"));
		// GameRegistry.register(itemCopperIngot=new
		// Item().setUnlocalizedName("ingotCopper").setCreativeTab(tabtf2).setRegistryName(TF2weapons.MOD_ID+":ingotCopper"));
		// GameRegistry.register(itemLeadIngot=new
		// Item().setUnlocalizedName("ingotLead").setCreativeTab(tabtf2).setRegistryName(TF2weapons.MOD_ID+":ingotLead"));
		// GameRegistry.register(itemAustraliumIngot=new
		// Item().setUnlocalizedName("ingotAustralium").setCreativeTab(tabtf2).setRegistryName(TF2weapons.MOD_ID+":ingotAustralium"));
		ForgeRegistries.ITEMS.register(itemTF2 = new ItemTF2().setRegistryName(TF2weapons.MOD_ID + ":itemTF2"));
		ForgeRegistries.ITEMS.register(itemRobotPart = new ItemRobotPart().setRegistryName(TF2weapons.MOD_ID + ":robotPart"));
		//ForgeRegistries.ITEMS.register(itemPDA = new ItemPDA().setRegistryName(TF2weapons.MOD_ID + ":pda").setUnlocalizedName("pda").setCreativeTab(tabutilitytf2));
		ForgeRegistries.ITEMS.register(itemTarget = new ItemTarget().setRegistryName(TF2weapons.MOD_ID + ":target").setUnlocalizedName("attackTarget"));
		ForgeRegistries.ITEMS.register(itemKillstreak = new ItemKillstreakKit().setRegistryName(TF2weapons.MOD_ID + ":killstreakkit"));
		ForgeRegistries.ITEMS.register(itemStrangifier = new ItemStrangifier().setRegistryName(TF2weapons.MOD_ID + ":strangifier")
				.setUnlocalizedName("strangifier").setCreativeTab(TF2weapons.tabutilitytf2));
		ForgeRegistries.ITEMS.register(itemKillstreakFabricator = new ItemKillstreakFabricator().setRegistryName(TF2weapons.MOD_ID + ":killstreakfabricator"));
		ForgeRegistries.ITEMS.register(itemEventMaker = new ItemEventMaker().setCreativeTab(tabutilitytf2).setUnlocalizedName("eventmaker").setRegistryName(TF2weapons.MOD_ID + ":eventmaker"));
		ForgeRegistries.ITEMS.register(itemBossSpawn = new ItemBossSpawner().setCreativeTab(tabspawnertf2).setUnlocalizedName("boss").setRegistryName(TF2weapons.MOD_ID + ":boss_spawner"));
		ForgeRegistries.ITEMS.register(itemDoorController = new ItemDoorController().setCreativeTab(tabutilitytf2).setUnlocalizedName("doorcontroller")
				.setRegistryName(TF2weapons.MOD_ID + ":door_controller"));
		Iterator<String> iterator = MapList.weaponClasses.keySet().iterator();
		while (iterator.hasNext()) {
			String name = iterator.next();
			ForgeRegistries.ITEMS.register(MapList.weaponClasses.get(name).setRegistryName(new ResourceLocation(MOD_ID, "" + name.toLowerCase())));
		}

		GameRegistry.registerTileEntity(TileEntityUpgrades.class, "UpgradeStation");
		GameRegistry.registerTileEntity(TileEntityAmmoFurnace.class, "AmmoFurnace");
		GameRegistry.registerTileEntity(TileEntityOverheadDoor.class, new ResourceLocation(TF2weapons.MOD_ID, "overhead_door"));
		GameRegistry.registerTileEntity(TileEntityRobotDeploy.class, new ResourceLocation(TF2weapons.MOD_ID, "robot_deploy"));

		registerBlock(blockCabinet = new BlockCabinet().setHardness(5.0F).setResistance(10.0F).setUnlocalizedName("cabinet"), TF2weapons.MOD_ID + ":tf2workbench");
		registerBlock(blockAmmoFurnace = new BlockAmmoFurnace().setHardness(5.0F).setResistance(10.0F).setUnlocalizedName("ammoFurnace"), TF2weapons.MOD_ID + ":ammo_furnace");
		registerBlock(blockUpgradeStation = new BlockUpgradeStation().setHardness(5f).setResistance(10.0F).setUnlocalizedName("upgradeStation"),
				TF2weapons.MOD_ID + ":upgrade_station");
		registerBlock(blockCopperOre = new BlockOre().setCreativeTab(tabsurvivaltf2).setHardness(3.0F).setResistance(5.0F).setUnlocalizedName("oreCopper"),
				TF2weapons.MOD_ID + ":copper_ore");
		registerBlock(blockLeadOre = new BlockOre().setCreativeTab(tabsurvivaltf2).setHardness(3.0F).setResistance(5.0F).setUnlocalizedName("oreLead"),
				TF2weapons.MOD_ID + ":lead_ore");
		registerBlock(blockAustraliumOre = new BlockOre().setCreativeTab(tabsurvivaltf2).setHardness(7.0F).setResistance(10.0F).setUnlocalizedName("oreAustralium"),
				TF2weapons.MOD_ID + ":australium_ore");
		registerBlock(blockAustralium = new Block(Material.IRON, MapColor.GOLD).setCreativeTab(tabsurvivaltf2).setHardness(9.0F).setResistance(20.0F)
				.setUnlocalizedName("blockAustralium"), TF2weapons.MOD_ID + ":australium_block");
		registerBlock(blockOverheadDoor = new BlockOverheadDoor().setCreativeTab(tabsurvivaltf2).setHardness(3.0F).setResistance(5.0F)
				.setUnlocalizedName("blockOverhead"), TF2weapons.MOD_ID + ":overhead_door");
		registerBlock(blockRobotDeploy= new BlockRobotDeploy().setCreativeTab(tabsurvivaltf2).setHardness(3.0F).setResistance(5.0F)
				.setUnlocalizedName("blockRobotDeploy"), TF2weapons.MOD_ID + ":robot_deploy");
		ForgeRegistries.BLOCKS.register(blockProp= new BlockProp(Material.WOOD, MapColor.GOLD).setHardness(0.75F).setResistance(2.0F)
				.setUnlocalizedName("blockProp").setRegistryName(TF2weapons.MOD_ID + ":prop_block"));

		OreDictionary.registerOre("oreCopper", blockCopperOre);
		OreDictionary.registerOre("oreLead", blockLeadOre);
		OreDictionary.registerOre("oreAustralium", blockAustraliumOre);
		OreDictionary.registerOre("blockAustralium", blockAustralium);
		OreDictionary.registerOre("ingotCopper", new ItemStack(itemTF2, 1, 0));
		OreDictionary.registerOre("ingotLead", new ItemStack(itemTF2, 1, 1));
		OreDictionary.registerOre("ingotAustralium", new ItemStack(itemTF2, 1, 2));
		OreDictionary.registerOre("nuggetAustralium", new ItemStack(itemTF2, 1, 9));

		blockCopperOre.setHarvestLevel("pickaxe", 1);
		blockLeadOre.setHarvestLevel("pickaxe", 1);
		blockAustraliumOre.setHarvestLevel("pickaxe", 2);

		ItemAmmo.STACK_FILL = new ItemStack(itemAmmo);
		
		CapabilityManager.INSTANCE.register(TF2PlayerCapability.class, new NullStorage<TF2PlayerCapability>(), () -> {return null;});
		CapabilityManager.INSTANCE.register(WeaponsCapability.class, new NullStorage<WeaponsCapability>(), () -> {return null;});
		CapabilityManager.INSTANCE.register(InventoryWearables.class, new NullStorage<InventoryWearables>(), () -> {return null;});
		CapabilityManager.INSTANCE.register(WeaponData.WeaponDataCapability.class, new NullStorage<WeaponData.WeaponDataCapability>(), () -> {return null;});
		CapabilityManager.INSTANCE.register(TF2EventsCommon.TF2WorldStorage.class, new NullStorage<TF2EventsCommon.TF2WorldStorage>(), () -> {return null;});
		CapabilityManager.INSTANCE.register(ItemCrate.CrateContent.class, new NullStorage<ItemCrate.CrateContent>(), () -> {return null;});
		CapabilityManager.INSTANCE.register(ItemFromData.AttributeProvider.class, new NullStorage<ItemFromData.AttributeProvider>(), () -> {return null;});
		
		ForgeRegistries.POTIONS.register(bonk = new PotionTF2Item(false, 0x696969, new ResourceLocation(TF2weapons.MOD_ID, "textures/items/bonk.png")).setPotionName("effect.bonk")
				.setRegistryName(TF2weapons.MOD_ID + ":bonkEff"));
		ForgeRegistries.POTIONS.register(stun = new PotionTF2(true, 0, 3, 1).setPotionName("effect.stun").setRegistryName(TF2weapons.MOD_ID + ":stunEff")
				.registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-14B354F0D8BA", -0.5D, 2));
		ForgeRegistries.POTIONS.register(crit = new PotionTF2Item(false, 0, new ResourceLocation(TF2weapons.MOD_ID, "textures/items/critacola.png")).setPotionName("effect.crit")
				.setRegistryName(TF2weapons.MOD_ID + ":critEff")
				.registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-14B354E56B59", 0.25D, 2));
		ForgeRegistries.POTIONS.register(buffbanner = new PotionTF2Item(false, 0, new ResourceLocation(TF2weapons.MOD_ID, "textures/items/buffbanner.png")).setPotionName("effect.banner")
				.setRegistryName(TF2weapons.MOD_ID + ":bannerEff").setBeneficial());
		ForgeRegistries.POTIONS.register(backup = new PotionTF2Item(false, 0, new ResourceLocation(TF2weapons.MOD_ID, "textures/items/backup.png")).setPotionName("effect.backup")
				.setRegistryName(TF2weapons.MOD_ID + ":backupEff").setBeneficial());
		ForgeRegistries.POTIONS.register(conch = new PotionTF2Item(false, 0, new ResourceLocation(TF2weapons.MOD_ID, "textures/items/conch.png")).setPotionName("effect.conch")
				.setRegistryName(TF2weapons.MOD_ID + ":conchEff").setBeneficial()
				.registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-14B35B5565E2", 0.25D, 2));
		ForgeRegistries.POTIONS.register(markDeath = new PotionTF2(true, 0, 1, 2).setPotionName("effect.markDeath").setRegistryName(TF2weapons.MOD_ID + ":markDeathEff"));
		ForgeRegistries.POTIONS.register(critBoost = new PotionTF2(false, 0, 4, 0).setPotionName("effect.critBoost").setRegistryName(TF2weapons.MOD_ID + ":critBoostEff").setBeneficial());
		ForgeRegistries.POTIONS.register(jarate = new PotionTF2Item(true, 0xFFD500, new ResourceLocation(TF2weapons.MOD_ID, "textures/items/jarate.png")).setPotionName("effect.jarate")
				.setRegistryName(TF2weapons.MOD_ID + ":jarateEff"));
		ForgeRegistries.POTIONS.register(madmilk = new PotionTF2Item(true, 0xF1F1F1, new ResourceLocation(TF2weapons.MOD_ID, "textures/items/madmilk.png")).setPotionName("effect.madmilk")
				.setRegistryName(TF2weapons.MOD_ID + ":madmilkEff"));
		ForgeRegistries.POTIONS.register(charging = new PotionTF2Item(false, 0, new ResourceLocation(TF2weapons.MOD_ID, "textures/items/charging_targe.png")) {
			
			public void applyAttributesModifiersToEntity(EntityLivingBase entityLivingBaseIn, AbstractAttributeMap attributeMapIn, int amplifier)
		    {
				super.applyAttributesModifiersToEntity(entityLivingBaseIn, attributeMapIn, amplifier);
				entityLivingBaseIn.stepHeight+=amplifier;
				if (entityLivingBaseIn instanceof EntityTF2Character) {
					((EntityTF2Character)entityLivingBaseIn).rotation*=0.1f;
				}
		    }
			
			public void removeAttributesModifiersFromEntity(EntityLivingBase entityLivingBaseIn, AbstractAttributeMap attributeMapIn, int amplifier)
		    {
				super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
				entityLivingBaseIn.stepHeight-=amplifier;
				if (entityLivingBaseIn.hasCapability(TF2weapons.WEAPONS_CAP, null)) {
					WeaponsCapability.get(entityLivingBaseIn).setExpJump(true);
				}
				if (entityLivingBaseIn instanceof EntityTF2Character) {
					((EntityTF2Character)entityLivingBaseIn).rotation*=10f;
				}
		    }
			
		}.setPotionName("effect.charging")
				.setRegistryName(TF2weapons.MOD_ID + ":chargingEff")
				.registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-14B35B5565E6", 1.5D, 2).setBeneficial());
		ForgeRegistries.POTIONS.register(uber = new PotionTF2Item(false, 0, new ResourceLocation(TF2weapons.MOD_ID, "textures/items/medigun_im.png")).setPotionName("effect.uber")
				.setRegistryName(TF2weapons.MOD_ID + ":uberEff").setBeneficial());
		ForgeRegistries.POTIONS.register(it = new PotionTF2(true, 0xFFFFFFFF, 1, 2).setPotionName("effect.it").setRegistryName(TF2weapons.MOD_ID + ":itEff"));
		ForgeRegistries.POTIONS.register(bombmrs = new PotionTF2(false, 0xFFFFFFFF, 1, 2).setPotionName("effect.bombmrs").setRegistryName(TF2weapons.MOD_ID + ":bombEff")
				.registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "7107DE6F-7CE8-4030-940E-14B354F0D8BA", 1.25D, 2));
		ForgeRegistries.POTIONS.register(bleeding = new PotionTF2(true, 0, 4, 0).setPotionName("effect.bleeding").setRegistryName(TF2weapons.MOD_ID + ":bleedingEff"));
		ForgeRegistries.POTIONS.register(noKnockback = new PotionTF2(true, 0, 0, 0).setPotionName("effect.noKnockback").setRegistryName(TF2weapons.MOD_ID + ":noKnockbackEff")
				.registerPotionAttributeModifier(SharedMonsterAttributes.KNOCKBACK_RESISTANCE, "0d09b7c3-ac5e-4f56-9b65-fe2d09b99dc3", 10, 0));
		ForgeRegistries.POTIONS.register(sapped = new PotionTF2(true, 0, 0, 0) {
			public void applyAttributesModifiersToEntity(EntityLivingBase entityLivingBaseIn, AbstractAttributeMap attributeMapIn, int amplifier)
		    {
				super.applyAttributesModifiersToEntity(entityLivingBaseIn, attributeMapIn, amplifier);
				if (entityLivingBaseIn instanceof EntityTF2Character) {
					if (((EntityTF2Character)entityLivingBaseIn).isGiant())
						((EntityTF2Character)entityLivingBaseIn).rotation*=0.4f;
					else
						((EntityTF2Character)entityLivingBaseIn).rotation*=0.1f;
				}
		    }
			public void removeAttributesModifiersFromEntity(EntityLivingBase entityLivingBaseIn, AbstractAttributeMap attributeMapIn, int amplifier)
		    {
				super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
				if (entityLivingBaseIn instanceof EntityTF2Character) {
					if (((EntityTF2Character)entityLivingBaseIn).isGiant())
						((EntityTF2Character)entityLivingBaseIn).rotation*=2.5f;
					else
						((EntityTF2Character)entityLivingBaseIn).rotation*=10f;
				}
		    }
		}.setPotionName("effect.sapped")
				.setRegistryName(TF2weapons.MOD_ID + ":sapped")
				.registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "7107DE6F-7CE8-4030-940E-14B354F0D4AB", -0.5D, 2));
		// conf.save();
		ItemKillstreakFabricator.initKillstreaks();
		PropertyType.init();
		if(!TF2ConfigVars.disableGeneration){
			MapGenStructureIO.registerStructureComponent(MannCoBuilding.class, "ViMC");
			MapGenStructureIO.registerStructure(MannCoBuilding.Start.class, "MCBuild");
			MapGenStructureIO.registerStructureComponent(ScatteredFeatureTF2Base.class, "TF2B");
			MapGenStructureIO.registerStructure(ScatteredFeatureTF2Base.Start.class, "TF2Base");
			//GameRegistry.registerWorldGenerator(generator, modGenerationWeight);
		}
		
		if (TF2ConfigVars.thermalExpansion) {
			ThermalExpansionModule.init();
		}
		
		LOGGER.info("Parsing weapon files");
		loadWeapons();
		InvasionEvent.squads = Squad.parseFile(squadFile);
			//System.out.println(MapList.nameToData.get("rocketlauncher"));
		cratesOpened = (new StatBasic("stat.cratesOpened", new TextComponentTranslation("stat.cratesOpened", new Object[0]))).registerStat();
		robotsKilled = (new StatBasic("stat.robotsKilled", new TextComponentTranslation("stat.cratesKilled", new Object[0]))).registerStat();
		LOGGER.info("Registering models");
		proxy.preInit();
		MinecraftForge.EVENT_BUS.register(new TF2EventsCommon());
		MinecraftForge.ORE_GEN_BUS.register(new TF2EventsCommon());
		LOGGER.info("Pre init done");
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		/*if (event.getSide() == Side.CLIENT) {
			AchievementPage.registerAchievementPage(new TF2Achievements());
		}*/
		
		TF2Achievements.init();
		GameRegistry.addSmelting(new ItemStack(blockCopperOre), new ItemStack(itemTF2, 1, 0), 0.5f);
		GameRegistry.addSmelting(new ItemStack(blockLeadOre), new ItemStack(itemTF2, 1, 1), 0.55f);
		GameRegistry.addSmelting(new ItemStack(blockAustraliumOre), new ItemStack(itemTF2, 1, 2), 2f);
		GameRegistry.addSmelting(new ItemStack(itemTF2, 1, 3), new ItemStack(Items.IRON_INGOT, 2), 0.35f);
		
		GameRegistry.addSmelting(new ItemStack(itemAmmoMinigun), new ItemStack(itemTF2, 2, 1), 0.1f);
		GameRegistry.addSmelting(new ItemStack(itemAmmo, 13, 1), new ItemStack(itemTF2, 1, 1), 0.1f);
		GameRegistry.addSmelting(new ItemStack(itemAmmoPistol, 4, 0), new ItemStack(itemTF2, 1, 0), 0.1f);
		GameRegistry.addSmelting(new ItemStack(itemAmmoSMG, 3, 0), new ItemStack(itemTF2, 1, 0), 0.1f);
		GameRegistry.addSmelting(new ItemStack(itemAmmo, 16, 4), new ItemStack(itemTF2, 1, 1), 0.1f);
		GameRegistry.addSmelting(new ItemStack(itemAmmo, 5, 6), new ItemStack(itemTF2, 1, 0), 0.1f);
		GameRegistry.addSmelting(new ItemStack(itemAmmo, 10, 7), new ItemStack(Items.IRON_INGOT), 0.1f);
		GameRegistry.addSmelting(new ItemStack(itemAmmo, 10, 8), new ItemStack(Items.IRON_INGOT), 0.1f);
		GameRegistry.addSmelting(new ItemStack(itemAmmo, 8, 11), new ItemStack(Items.IRON_INGOT), 0.1f);
		GameRegistry.addSmelting(new ItemStack(itemAmmo, 10, 13), new ItemStack(Items.IRON_INGOT), 0.1f);
		GameRegistry.addSmelting(new ItemStack(itemAmmoFire), new ItemStack(Items.IRON_INGOT,2), 0.1f);
		
		
		/*GameRegistry.(new IFuelHandler() {

			@Override
			public int getBurnTime(ItemStack fuel) {
				// TODO Auto-generated method stub
				return fuel.getItem() instanceof ItemCrate ? 300 : 0;
			}

		});*/
		ForgeRegistries.RECIPES.register(new ShapedOreRecipe(null, new ItemStack(blockCabinet), "SCS", "SIS", 'S', new ItemStack(itemTF2, 1, 3), 'C', "workbench", 'I', "blockIron")
				.setRegistryName(TF2weapons.MOD_ID,"recipe1"));
		ForgeRegistries.RECIPES.register(new ShapedOreRecipe(null, new ItemStack(itemTF2, 1, 2), "AAA", "AAA", "AAA", 'A', new ItemStack(itemTF2, 1, 6))
				.setRegistryName(TF2weapons.MOD_ID,"recipe2"));
		ForgeRegistries.RECIPES.register(new ShapelessOreRecipe(null,new ItemStack(itemTF2, 9, 6), "ingotAustralium").setRegistryName(TF2weapons.MOD_ID,"recipe3"));
		ForgeRegistries.RECIPES.register(new ShapedOreRecipe(null, new ItemStack(blockAustralium), "AAA", "AAA", "AAA", 'A', new ItemStack(itemTF2, 1, 2))
				.setRegistryName(TF2weapons.MOD_ID,"recipe4"));
		ForgeRegistries.RECIPES.register(new ShapelessOreRecipe(null,new ItemStack(itemTF2, 9, 2), "blockAustralium").setRegistryName(TF2weapons.MOD_ID,"recipe5"));
		ForgeRegistries.RECIPES.register(new OpenCrateRecipe().setRegistryName(TF2weapons.MOD_ID,"opencrate"));
		
		ForgeRegistries.RECIPES.register(new ShapedOreRecipe(null,new ItemStack(TF2weapons.blockOverheadDoor), new Object[] { "BI", "B ", "B ", 'B', Blocks.IRON_BARS, 'I',"ingotIron" })
				.setRegistryName(TF2weapons.MOD_ID,"blockoverhead"));
		ForgeRegistries.RECIPES.register(new ShapedOreRecipe(null,new ItemStack(TF2weapons.itemDoorController,1,0), new Object[] { "RIR", "IOI", "RIR", 'R', "dustRedstone", 'I',"ingotIron", 'O', Blocks.OBSERVER })
				.setRegistryName(TF2weapons.MOD_ID,"doorcontroller1"));
		ForgeRegistries.RECIPES.register(new ShapedOreRecipe(null,new ItemStack(TF2weapons.itemDoorController,1,1), new Object[] { "SIS", "IOI", "SIS", 'S', "gunpowder", 'I',"ingotIron", 'O', Blocks.OBSERVER })
		.setRegistryName(TF2weapons.MOD_ID,"doorcontroller2"));
		ForgeRegistries.RECIPES.register(new ShapedOreRecipe(null,new ItemStack(TF2weapons.itemDoorController,1,2), new Object[] { "RIR", "IOI", "RIR", 'R', "dyeRed", 'I',"ingotIron", 'O', Blocks.OBSERVER })
		.setRegistryName(TF2weapons.MOD_ID,"doorcontroller3"));
		ForgeRegistries.RECIPES.register(new ShapedOreRecipe(null,new ItemStack(TF2weapons.itemDoorController,1,3), new Object[] { "RIR", "IOI", "RIR", 'R', "dyeBlue", 'I',"ingotIron", 'O', Blocks.OBSERVER })
		.setRegistryName(TF2weapons.MOD_ID,"doorcontroller4"));

		LootFunctionManager.registerFunction(new EntityBuildingFunction.Serializer());
		LootFunctionManager.registerFunction(new EntityOfClassFunction.Serializer());
		LootFunctionManager.registerFunction(new RandomWeaponFunction.Serializer());
		LootConditionManager.registerCondition(new KilledByTeam.Serializer());
		LootConditionManager.registerCondition(new EntityNBTCondition.Serializer());
		LootConditionManager.registerCondition(new EntityOrCondition.Serializer());
		
		LootTableList.register(new ResourceLocation(MOD_ID, "chests/simple_dungeon"));
		LootTableList.register(new ResourceLocation(MOD_ID, "chests/nether_bridge"));
		LootTableList.register(new ResourceLocation(MOD_ID, "chests/stronghold_corridor"));
		LootTableList.register(new ResourceLocation(MOD_ID, "chests/end_city_treasure"));
		LootTableList.register(new ResourceLocation(MOD_ID, "chests/abandoned_mineshaft"));
		
		lootTF2Character = LootTableList.register(new ResourceLocation(MOD_ID, "entities/tf2character"));
		lootScout = LootTableList.register(new ResourceLocation(MOD_ID, "entities/scout"));
		lootHeavy = LootTableList.register(new ResourceLocation(MOD_ID, "entities/heavy"));
		lootSniper = LootTableList.register(new ResourceLocation(MOD_ID, "entities/sniper"));
		lootSpy = LootTableList.register(new ResourceLocation(MOD_ID, "entities/spy"));
		lootDemoman = LootTableList.register(new ResourceLocation(MOD_ID, "entities/demoman"));
		lootEngineer = LootTableList.register(new ResourceLocation(MOD_ID, "entities/engineer"));
		lootSoldier = LootTableList.register(new ResourceLocation(MOD_ID, "entities/soldier"));
		lootMedic = LootTableList.register(new ResourceLocation(MOD_ID, "entities/medic"));
		lootPyro = LootTableList.register(new ResourceLocation(MOD_ID, "entities/pyro"));
		lootHale = LootTableList.register(new ResourceLocation(MOD_ID, "entities/hale"));
		lootTF2Base = LootTableList.register(new ResourceLocation(MOD_ID, "chests/tf2_base"));
		

		// new
		// Item(2498).setUnlocalizedName("FakeItem").setTextureName(TF2weapons.MOD_ID+":saw").setCreativeTab(CreativeTabs.tabBlock);
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(itemPlacer, new BehaviorDefaultDispenseItem() {
			@Override
			public ItemStack dispenseStack(IBlockSource p_82487_1_, ItemStack p_82487_2_) {
				EnumFacing enumfacing = p_82487_1_.getBlockState().getValue(BlockDispenser.FACING);
				double d0 = p_82487_1_.getX() + enumfacing.getFrontOffsetX();
				double d1 = p_82487_1_.getY() + 0.2F;
				double d2 = p_82487_1_.getZ() + enumfacing.getFrontOffsetZ();
				Entity entity = ItemMonsterPlacerPlus.spawnCreature(null,p_82487_1_.getWorld(), p_82487_2_.getItemDamage(), d0, d1, d2,
						p_82487_2_.getTagCompound() != null && p_82487_2_.getTagCompound().hasKey("SavedEntity") ? p_82487_2_.getTagCompound().getCompoundTag("SavedEntity")
								: null);

				if (entity instanceof EntityLivingBase && p_82487_2_.hasDisplayName())
					((EntityLiving) entity).setCustomNameTag(p_82487_2_.getDisplayName());

				p_82487_2_.splitStack(1);
				return p_82487_2_;
			}
		});
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(itemBuildingBox, new BehaviorDefaultDispenseItem() {
			@Override
			public ItemStack dispenseStack(IBlockSource p_82487_1_, ItemStack p_82487_2_) {
				EnumFacing enumfacing = p_82487_1_.getBlockState().getValue(BlockDispenser.FACING);
				double d0 = p_82487_1_.getX() + enumfacing.getFrontOffsetX();
				double d1 = p_82487_1_.getY() + 0.2F;
				double d2 = p_82487_1_.getZ() + enumfacing.getFrontOffsetZ();
				Entity entity = ItemMonsterPlacerPlus.spawnCreature(null,p_82487_1_.getWorld(), p_82487_2_.getItemDamage(), d0, d1, d2,
						p_82487_2_.getTagCompound() != null && p_82487_2_.getTagCompound().hasKey("SavedEntity") ? p_82487_2_.getTagCompound().getCompoundTag("SavedEntity")
								: null);

				if (entity instanceof EntityLivingBase && p_82487_2_.hasDisplayName())
					((EntityLiving) entity).setCustomNameTag(p_82487_2_.getDisplayName());

				p_82487_2_.splitStack(1);
				return p_82487_2_;
			}
		});
		class BehaviorDispenseAmmo extends BehaviorDefaultDispenseItem {
			public ItemStack dispenseStack(IBlockSource source, ItemStack stack)
	        {
				ItemStack weapon;
				switch (((ItemAmmo)stack.getItem()).getTypeInt(stack)) {
	            case 1: weapon = ItemFromData.getNewStack("shotgun"); break;
	            case 2: weapon = ItemFromData.getNewStack("minigun"); break;
	            case 3: weapon = ItemFromData.getNewStack("pistol"); break;
	            case 4: weapon = ItemFromData.getNewStack("revolver"); break;
	            case 5: weapon = ItemFromData.getNewStack("smg"); break;
	            case 6: weapon = ItemFromData.getNewStack("sniperrifle"); TF2Attribute.setAttribute(weapon, TF2Attribute.attributes[60], 2); break;
	            case 7: weapon = ItemFromData.getNewStack("rocketlauncher"); break;
	            case 8: weapon = ItemFromData.getNewStack("grenadelauncher"); break;
	            case 9: weapon = ItemFromData.getNewStack("syringegun"); break;
	            case 13: weapon = ItemFromData.getNewStack("flaregun"); break;
	            case 14: weapon = ItemFromData.getNewStack("sandmanball"); break;
	            default: return stack;
	            }
				if (weapon.isEmpty())
					return stack;
	            World world = source.getWorld();
	            IPosition iposition = BlockDispenser.getDispensePosition(source);
	            EnumFacing enumfacing = (EnumFacing)source.getBlockState().getValue(BlockDispenser.FACING);
	            dummyEnt.setPosition(iposition.getX(), iposition.getY(), iposition.getZ());
	            Vec2f rot = TF2Util.getAngleFromFacing(enumfacing);
	            dummyEnt.rotationYawHead = rot.x;
	            dummyEnt.rotationPitch = rot.y;
	            dummyEnt.world = world;
	            dummyEnt.setHeldItem(EnumHand.MAIN_HAND, weapon);
	            ((ItemUsable)weapon.getItem()).use(weapon, dummyEnt, world, EnumHand.MAIN_HAND, null);
	            //IProjectile iprojectile = this.getProjectileEntity(world, iposition, stack);
	           // iprojectile.shoot((double)enumfacing.getFrontOffsetX(), (double)((float)enumfacing.getFrontOffsetY() + 0.1F), (double)enumfacing.getFrontOffsetZ(), this.getProjectileVelocity(), this.getProjectileInaccuracy());
	            //world.spawnEntity((Entity)iprojectile);
	            ((ItemAmmo)stack.getItem()).consumeAmmo(dummyEnt, stack, 1);
	            return stack;
	        }

	        /**
	         * Play the dispense sound from the specified block.
	         */
	        protected void playDispenseSound(IBlockSource source)
	        {
	            source.getWorld().playEvent(1002, source.getBlockPos(), 0);
	        }
		};
		class BehaviorDispenseSelf extends BehaviorDefaultDispenseItem {
			public ItemStack dispenseStack(IBlockSource source, ItemStack stack)
	        {
	            World world = source.getWorld();
	            IPosition iposition = BlockDispenser.getDispensePosition(source);
	            EnumFacing enumfacing = (EnumFacing)source.getBlockState().getValue(BlockDispenser.FACING);
	            dummyEnt.setPosition(iposition.getX(), iposition.getY(), iposition.getZ());
	            Vec2f rot = TF2Util.getAngleFromFacing(enumfacing);
	            dummyEnt.rotationYawHead = rot.x;
	            dummyEnt.rotationPitch = rot.y;
	            dummyEnt.world = world;
	            dummyEnt.setHeldItem(EnumHand.MAIN_HAND, stack);
	            ((ItemUsable)stack.getItem()).use(stack, dummyEnt, world, EnumHand.MAIN_HAND, null);
	            //IProjectile iprojectile = this.getProjectileEntity(world, iposition, stack);
	           // iprojectile.shoot((double)enumfacing.getFrontOffsetX(), (double)((float)enumfacing.getFrontOffsetY() + 0.1F), (double)enumfacing.getFrontOffsetZ(), this.getProjectileVelocity(), this.getProjectileInaccuracy());
	            //world.spawnEntity((Entity)iprojectile);
	            stack.shrink(1);
	            return stack;
	        }

	        /**
	         * Play the dispense sound from the specified block.
	         */
	        protected void playDispenseSound(IBlockSource source)
	        {
	            source.getWorld().playEvent(1002, source.getBlockPos(), 0);
	        }
		};
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(itemAmmo, new BehaviorDispenseAmmo());
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(itemAmmoMinigun, new BehaviorDispenseAmmo());
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(itemAmmoPistol, new BehaviorDispenseAmmo());
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(itemAmmoSMG, new BehaviorDispenseAmmo());
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(itemAmmoSyringe, new BehaviorDispenseAmmo());
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(MapList.weaponClasses.get("jar"), new BehaviorDispenseSelf());
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(MapList.weaponClasses.get("cleaver"), new BehaviorDispenseSelf());
		network = new TF2NetworkWrapper(MOD_ID);
		network.registerMessage(TF2ActionHandler.class, TF2Message.ActionMessage.class, 0, Side.SERVER, true);
		network.registerMessage(TF2PropertyHandler.class, TF2Message.PropertyMessage.class, 2, Side.SERVER, false);
		network.registerMessage(TF2BulletHandler.class, TF2Message.BulletMessage.class, 3, Side.SERVER, true);
		network.registerMessage(TF2ProjectileHandler.class, TF2Message.PredictionMessage.class, 4, Side.SERVER, true);
		network.registerMessage(TF2GuiConfigHandler.class, TF2Message.GuiConfigMessage.class, 5, Side.SERVER, false);
		network.registerMessage(TF2CapabilityHandler.class, TF2Message.CapabilityMessage.class, 7, Side.SERVER, false);
		network.registerMessage(TF2ShowGuiHandler.class, TF2Message.ShowGuiMessage.class, 9, Side.SERVER, false);
		network.registerMessage(TF2DisguiseHandler.class, TF2Message.DisguiseMessage.class, 11, Side.SERVER, false);
		network.registerMessage(TF2ActionHandler.class, TF2Message.ActionMessage.class, 18, Side.CLIENT, true);
		network.registerMessage(TF2UseHandler.class, TF2Message.UseMessage.class, 1, Side.CLIENT, true);
		network.registerMessage(TF2PropertyHandler.class, TF2Message.PropertyMessage.class, 19, Side.CLIENT, false);
		network.registerMessage(TF2CapabilityHandler.class, TF2Message.CapabilityMessage.class, 6, Side.CLIENT, false);
		network.registerMessage(TF2WeaponDataHandler.class, TF2Message.WeaponDataMessage.class, 8, Side.CLIENT, false);
		network.registerMessage(TF2WearableChangeHandler.class, TF2Message.WearableChangeMessage.class, 10, Side.CLIENT, false);
		network.registerMessage(TF2WeaponDropHandler.class, TF2Message.WeaponDroppedMessage.class, 12, Side.CLIENT, true);
		network.registerMessage(TF2ContractHandler.class, TF2Message.ContractMessage.class, 13, Side.CLIENT, false);
		network.registerMessage(TF2VelocityAddHandler.class, TF2Message.VelocityAddMessage.class, 14, Side.CLIENT, true);
		network.registerMessage(TF2AttackSyncHandler.class, TF2Message.AttackSyncMessage.class, 15, Side.SERVER, true);
		network.registerMessage(TF2InitHandler.class, TF2Message.InitMessage.class, 16, Side.CLIENT, false);
		network.registerMessage(TF2InitClientHandler.class, TF2Message.InitClientMessage.class, 17, Side.SERVER, false);
		network.registerMessage(TF2EffectCooldownHandler.class, TF2Message.EffectCooldownMessage.class, 20, Side.CLIENT, false);
		network.registerMessage(TF2ParticleSpawnHandler.class, TF2Message.ParticleSpawnMessage.class, 21, Side.CLIENT, true);
		network.registerMessage(TF2PlayerCapabilityHandler.class, TF2Message.PlayerCapabilityMessage.class, 22, Side.CLIENT, false);
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new IGuiHandler() {

			@Override
			public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
				// TODO Auto-generated method stub
				BlockPos pos = new BlockPos(x, y, z);
				if (ID == 0){
					ContainerWearables container=new ContainerWearables(player.inventory, player.getCapability(INVENTORY_CAP, null), false, player);
					container.addListener(new TF2EventsCommon.TF2ContainerListener((EntityPlayerMP) player));
					return container;
				}
				else if (ID == 1 && world.getBlockState(pos).getBlock() instanceof BlockCabinet)
					return new ContainerTF2Workbench(player, player.inventory,
							/* (TileEntityCabinet) world.getTileEntity(pos), */ world, pos);
				else if (ID == 2 && world.getTileEntity(pos) != null && world.getTileEntity(pos) instanceof TileEntityUpgrades){
					if(((TileEntityUpgrades) world.getTileEntity(pos)).attributes.size()==0){
						((TileEntityUpgrades) world.getTileEntity(pos)).generateUpgrades(player.getRNG());
					}
					if(player instanceof EntityPlayerMP)
						((EntityPlayerMP)player).connection.sendPacket(world.getTileEntity(pos).getUpdatePacket());
					return new ContainerUpgrades(player, player.inventory, (TileEntityUpgrades) world.getTileEntity(pos), world, pos);
				}
				else if (ID == 3 && world.getTileEntity(pos) != null && world.getTileEntity(pos) instanceof TileEntityAmmoFurnace)
					return new ContainerAmmoFurnace(player.inventory, (TileEntityAmmoFurnace) world.getTileEntity(pos));
				else if (ID == 4 && world.getEntityByID(x) != null && world.getEntityByID(x) instanceof EntityTF2Character)
					return new ContainerMercenary(player, (EntityTF2Character) world.getEntityByID(x), world);
				else if (ID == 5 && world.getEntityByID(x) != null && world.getEntityByID(x) instanceof EntityBuilding) {
					if(world.getEntityByID(x) instanceof EntityDispenser)
						return new ContainerDispenser((EntityDispenser) world.getEntityByID(x), player.inventory);
					else
						return new ContainerEnergy((EntityBuilding) world.getEntityByID(x), player.inventory);
				}
				else if (ID == 6 && world.getTileEntity(pos) != null && world.getTileEntity(pos) instanceof TileEntityRobotDeploy)
					return new ContainerRobotDeploy(player.inventory, (TileEntityRobotDeploy) world.getTileEntity(pos));
				return null;
			}

			@Override
			public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
				// TODO Auto-generated method stub
				BlockPos pos = new BlockPos(x, y, z);
				if (ID == 0)
					return new GuiWearables(new ContainerWearables(player.inventory, player.getCapability(INVENTORY_CAP, null), true, player));
				else if (ID == 1 && world.getBlockState(pos).getBlock() instanceof BlockCabinet)
					return new GuiTF2Crafting(player.inventory,
							/* (TileEntityCabinet) world.getTileEntity(pos), */world, pos);
				else if (ID == 2 && world.getTileEntity(pos) != null && world.getTileEntity(pos) instanceof TileEntityUpgrades){
					return new GuiUpgradeStation(player.inventory, (TileEntityUpgrades) world.getTileEntity(pos), world, pos);
				}
				else if (ID == 3 && world.getTileEntity(pos) != null && world.getTileEntity(pos) instanceof TileEntityAmmoFurnace)
					return new GuiAmmoFurnace(player.inventory, (TileEntityAmmoFurnace) world.getTileEntity(pos));
				else if (ID == 4 && world.getEntityByID(x) != null && world.getEntityByID(x) instanceof EntityTF2Character)
					return new GuiMercenary(player.inventory,(EntityTF2Character) world.getEntityByID(x),world);
				else if (ID == 5 && world.getEntityByID(x) != null && world.getEntityByID(x) instanceof EntityBuilding) {
					if (world.getEntityByID(x) instanceof EntitySentry)
						return new GuiSentry((EntitySentry) world.getEntityByID(x));
					else if (world.getEntityByID(x) instanceof EntityDispenser)
						return new GuiDispenser((EntityDispenser) world.getEntityByID(x));
					else if (world.getEntityByID(x) instanceof EntityTeleporter)
						return new GuiTeleporter((EntityTeleporter) world.getEntityByID(x));
				}
				else if (ID == 6 && world.getTileEntity(pos) != null && world.getTileEntity(pos) instanceof TileEntityRobotDeploy)
					return new GuiRobotDeploy(player.inventory, (TileEntityRobotDeploy) world.getTileEntity(pos));
				return null;
			}

		});
		
		refinedFuel = FluidRegistry.getFluid("refined_fuel");
		// TickRegistry.registerTickHandler(new CommonTickHandler(),
		// Side.SERVER);

		// FMLCommonHandler.instance().bus().register(new
		// TF2EventBusListener());
		proxy.registerRenderInformation();

	}

	@Mod.EventHandler
	public void postinit(FMLPostInitializationEvent event) {

		updateMobSpawning();
		updateOreGenStatus();
		
		squakeLoaded = Loader.isModLoaded("squake");
		
		animals = new ArrayList<>();
		for(ResourceLocation entry:ForgeRegistries.ENTITIES.getKeys()) {
			if(EntityAnimal.class.isAssignableFrom(EntityList.getClass(entry))) {
				animals.add(entry);
			}
		}
	}

	@Mod.EventHandler
	public void modMessage(IMCEvent event) {
		for (IMCMessage message : event.getMessages()) {
			if ("addcraftingrecipe".equals(message.key)) {
				TF2CraftingManager.INSTANCE.addRecipe(ForgeRegistries.RECIPES.getValue(message.getResourceLocationValue()));
				((IForgeRegistryModifiable<IRecipe>)ForgeRegistries.RECIPES).remove(message.getResourceLocationValue());
			}
			else if ("removecraftingrecipeoutput".equals(message.key)) {
				for (Iterator<IRecipe> it = TF2CraftingManager.INSTANCE.getRecipeList().iterator(); it.hasNext();) {
					IRecipe recipe = it.next();
					if (recipe.getRecipeOutput().isItemEqual(message.getItemStackValue())) {
						it.remove();
					}
				}
			}
			else if ("removecraftingrecipeid".equals(message.key)) {
				TF2CraftingManager.INSTANCE.getRecipeList().remove(Integer.parseInt(message.getStringValue()));
			}
			else if ("addsaxtonhalerecipe".equals(message.key)) {
				MerchantRecipe recipe = new MerchantRecipe(message.getNBTValue());
				EntitySaxtonHale.addRecipes.add(recipe);
			}
			else if ("removesaxtonhalerecipe".equals(message.key)) {
				MerchantRecipe recipe = new MerchantRecipe(message.getNBTValue());
				EntitySaxtonHale.removeRecipes.add(recipe);
			}
		}
	}
	
	public static void updateOreGenStatus() {

		//System.out.println("Generowane: " + OreDictionary.getOres("oreCopper").size());
		String copper =conf.get("world gen", "Generate copper", "Default").setValidValues(new String[] { "Always", "Default", "Never" }).getString();
		String lead =conf.get("world gen", "Generate lead", "Default").setValidValues(new String[] { "Always", "Default", "Never" }).getString();
		String australium =conf.get("world gen", "Generate australium", "Always").setValidValues(new String[] { "Always", "Never" }).getString();
		generateCopper = copper.equals("Always");
		generateLead = lead.equals("Always");
		generateAustralium = australium.equals("Always");
		
		if (copper.equals("Default") && OreDictionary.getOres("oreCopper").size() == 1)
			generateCopper = true;
		if (lead.equals("Default") && OreDictionary.getOres("oreLead").size() == 1)
			generateLead = true;
	}

	public static void registerBlock(Block block, String name) {
		ForgeRegistries.BLOCKS.register(block.setRegistryName(name));
		ItemBlock item = new ItemBlock(block);
		item.setRegistryName(block.getRegistryName());
		ForgeRegistries.ITEMS.register(item);
		proxy.registerItemBlock(item);
	}

	public static void loadWeapons() {

		MapList.nameToData.clear();
		MapList.buildInAttributes.clear();
		try {
			ByteArrayOutputStream output=new ByteArrayOutputStream();
			DataOutputStream stream=new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(output)));
		
			loadConfig(new File(instance.weaponDir, "Weapons.json"),stream);
			File[] files = instance.weaponDir.listFiles(new FilenameFilter() {
	
				@Override
				public boolean accept(File arg0, String arg1) {
					return arg1.endsWith(".json") && !arg1.equalsIgnoreCase("Weapons.json");
				}
	
			});
			for (File file : files)
				loadConfig(file,stream);
			stream.flush();
			stream.close();
			itemDataCompressed=output.toByteArray();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void loadConfig(File file,DataOutput output) {
		/*
		 * Configuration weaponsFile= new Configuration(file);
		 * weaponsFile.load();
		 */
		ArrayList<WeaponData> list = WeaponData.parseFile(file);
		for (WeaponData data : list) {
			String weaponEntry = data.getName();
			// Class<?> weaponClass =
			// MapList.weaponClasses.get(weaponData.get("Class").getString());
			try {
				// System.out.println("attach "+weaponEntry);
				if (PropertyType.BASED_ON.hasKey(data) && MapList.nameToData.containsKey(data.getString(PropertyType.BASED_ON)))
					data = attach(MapList.nameToData.get(data.getString(PropertyType.BASED_ON)), data);
				loadWeapon(weaponEntry, data);
				
				output.writeUTF(data.getName());
				output.writeByte(data.properties.size());
				for (PropertyType type : data.properties.keySet()){
					output.writeByte(type.id);
					type.serialize(output, data, data.get(type));
				}

			} catch (Exception var4) {
				var4.printStackTrace();
			}
		}
	}
	public static void loadWeapon(String name, WeaponData weapon) {
		
		
		/*
		 * else{ weaponList[Integer.parseInt(weaponEntry)] =(ItemUsable)
		 * weaponClass.getConstructor(new Class[] {ConfigCategory.class,
		 * ConfigCategory.class}).newInstance(new Object[] {weaponData, null});
		 * }
		 */
		// GameRegistry.registerItem(weaponList[Integer.parseInt(weaponEntry)],
		// "weapon"+Integer.parseInt(weaponEntry));
		MapList.nameToData.put(name, weapon);
		// System.out.println("Weapon read: "+name);
		/*
		 * for(Entry<PropertyType, WeaponData.Property>
		 * entry:weapon.properties.entrySet()){
		 * System.out.println("Property: "+entry.getKey().name+" Value: "+entry.
		 * getValue().intValue+" "+entry.getValue().stringValue); }
		 */
		// LanguageRegistry.instance().addStringLocalization(weaponData.get("Name").getString()+".name",
		// weaponData.get("Name").getString());
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("Type", name);
		NBTTagCompound tag2 = new NBTTagCompound();
		if (weapon.hasProperty(PropertyType.ATTRIBUTES))
			for (Entry<TF2Attribute, Float> entry : weapon.get(PropertyType.ATTRIBUTES).attributes.entrySet())
				tag2.setFloat(String.valueOf(entry.getKey().id), entry.getValue());
		MapList.buildInAttributes.put(name, tag2);
	}

	public static WeaponData attach(WeaponData base, WeaponData additional) {
		for (PropertyType prop : base.properties.keySet())
			if (!additional.properties.containsKey(prop))
				additional.properties.put(prop, base.properties.get(prop));
		MapList.buildInAttributes.put(additional.getName(), MapList.buildInAttributes.get(base.getName()));
		// System.out.println("merged: "+additional.getName()+" "+key);

		// new ConfigCategory(null, additional);
		return additional;
	}

	@Mod.EventHandler
	public void serverPreInit(FMLServerAboutToStartEvent event) {
		// System.out.println("Starting server");
		
		if (!event.getServer().isDedicatedServer())
			for (WeaponData weapon : MapList.nameToData.values())
				ClientProxy.RegisterWeaponData(weapon);
		/*if(event.getSide()==Side.SERVER)
			AchievementPage.registerAchievementPage(new TF2Achievements());*/
	}

	@Mod.EventHandler
	public void serverInit(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandGiveWeapon());
		event.registerServerCommand(new CommandResetWeapons());
		event.registerServerCommand(new CommandResetStat());
		event.registerServerCommand(new CommandForceClass());
		event.registerServerCommand(new CommandGenerateReferences());
		event.registerServerCommand(new CommandChangeConfig());
		event.registerServerCommand(new CommandInvasion());
		if(event.getServer().isSinglePlayer())
			TF2UdpClient.addressToUse = "127.0.0.1";
		try {
			server = event.getServer();
			File input = new File(((AnvilSaveConverter) server.getActiveAnvilConverter()).savesDirectory, server.getFolderName() + "/teleports.dat");
			NBTTagCompound tagRoot = CompressedStreamTools.readCompressed(new FileInputStream(input));
			NBTTagCompound tag = tagRoot.getCompoundTag("Teleporters");
			for (String keys : tag.getKeySet()) {
				TeleporterData[] blockArray = new TeleporterData[EntityTeleporter.TP_PER_PLAYER];
				EntityTeleporter.teleporters.put(UUID.fromString(keys), blockArray);
				NBTTagCompound exitTag = tag.getCompoundTag(keys);
				for (int i = 0; i < EntityTeleporter.TP_PER_PLAYER; i++)
					if (exitTag.hasKey(Integer.toString(i))) {
						int[] array = exitTag.getIntArray(Integer.toString(i));
						blockArray[i] = new TeleporterData(new BlockPos(array[0], array[1], array[2]), array[3], array[4]);
					}
			}
			EntityTeleporter.tpCount = tagRoot.getInteger("TPCount");

		} catch (IOException e) {
			System.err.println("Reading teleporter data skipped");
		}
		
		if(TF2ConfigVars.enableUdp) {
			try {
				udpServer = new TF2UdpServer(event.getServer().isSinglePlayer()? 12454 : event.getServer().getServerPort() );
				udpServer.start();
				TF2weapons.network.useUdp=true;
				//new TF2UdpClient(SocketUtils.socketAddress("127.0.0.1", 12454));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Mod.EventHandler
	public void serverStop(FMLServerStoppingEvent event) {
		//MapList.nameToData.clear();
		//MapList.buildInAttributes.clear();

		File output = new File(((AnvilSaveConverter) server.getActiveAnvilConverter()).savesDirectory, server.getFolderName() + "/teleports.dat");
		NBTTagCompound tagRoot = new NBTTagCompound();
		NBTTagCompound tag = new NBTTagCompound();
		tagRoot.setTag("Teleporters", tag);

		for (Entry<UUID, TeleporterData[]> entry : EntityTeleporter.teleporters.entrySet()) {
			NBTTagCompound exitTag = new NBTTagCompound();
			for (int i = 0; i < EntityTeleporter.TP_PER_PLAYER; i++) {
				TeleporterData blockPos = entry.getValue()[i];
				if (blockPos != null)
					exitTag.setIntArray(Integer.toString(i), new int[] { blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.id, blockPos.dimension});
			}
			tag.setTag(entry.getKey().toString(), exitTag);
		}
		tagRoot.setInteger("TPCount", EntityTeleporter.tpCount);

		try {
			CompressedStreamTools.writeCompressed(tagRoot, new FileOutputStream(output));
		} catch (IOException e) {
			e.printStackTrace();
		}

		EntityTeleporter.teleporters.clear();
		EntityTeleporter.tpCount = 0;
		if(udpServer != null) {
			udpServer.stopServer();
			udpServer = null;
		}
	}

	/*
	 * public static void openWearableGUI(EntityPlayerMP player){ InventoryBasic
	 * inventory=new InventoryBasic("Wearables", false, 3); NBTTagList
	 * nbttaglist = player.getEntityData().getTagList("Wearables", 10); for (int
	 * i = 0; i < nbttaglist.tagCount(); ++i) { NBTTagCompound nbttagcompound =
	 * nbttaglist.getCompoundTagAt(i); int j = nbttagcompound.getByte("Slot") &
	 * 255; inventory.setInventorySlotContents(j,
	 * ItemStack.loadItemStackFromNBT(nbttagcompound)); } if
	 * (player.openContainer != player.inventoryContainer) {
	 * player.closeScreen(); }
	 * 
	 * player.getNextWindowId(); player.connection.sendPacket(new
	 * SPacketOpenWindow(player.currentWindowId, "rafradek_wearables",
	 * inventory.getDisplayName(), inventory.getSizeInventory(),
	 * player.getEntityId())); player.openContainer = new
	 * ContainerHorseInventory(player.inventory, inventory, horse, this);
	 * player.openContainer.windowId = player.currentWindowId;
	 * player.openContainer.addListener(this); }
	 */

	public static void updateMobSpawning() {
		ArrayList<Biome> biomesList = new ArrayList<Biome>();
		for (Biome biome : GameRegistry.findRegistry(Biome.class)){
			biome.getSpawnableList(EnumCreatureType.MONSTER).removeIf(entry -> TF2ConfigVars.spawnRate.containsKey(entry.entityClass));
			if (TF2ConfigVars.biomeCheck.equalsIgnoreCase("Default")) {
				boolean zombie = false;
				boolean spider = false;
				boolean creeper = false;
				for (SpawnListEntry entry : biome.getSpawnableList(EnumCreatureType.MONSTER)) {
					if (entry.entityClass == EntityZombie.class)
						zombie = true;
					else if (entry.entityClass == EntitySpider.class)
						spider = true;
					else if (entry.entityClass == EntityCreeper.class)
						creeper = true;
					if (zombie && spider && creeper) {
						biomesList.add(biome);
						break;
					}
				}
			}
			else if (TF2ConfigVars.biomeCheck.equalsIgnoreCase("Vanilla") && biome.delegate.name().getResourceDomain().equals("minecraft") && biome!=Biomes.HELL && biome!=Biomes.SKY) {
				biomesList.add(biome);
			}
			else if (TF2ConfigVars.biomeCheck.equalsIgnoreCase("All"))
				biomesList.add(biome);
		}
		if (!TF2ConfigVars.disableSpawn) {
			Biome[] biomes = biomesList.toArray(new Biome[biomesList.size()]);
			for(Entry<Class<? extends EntityLiving>, Integer> entry:TF2ConfigVars.spawnRate.entrySet()) {
				EntityRegistry.addSpawn(entry.getKey(), entry.getValue().intValue(), 1, 3, EnumCreatureType.MONSTER, biomes);
			}
		}
	}
	public static class NullStorage<T> implements IStorage<T> {

		@Override
		public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt) {
			// TODO Auto-generated method stub

		}

	}
	
	public static class NullCallable<T> implements Callable<T> {

		@Override
		public T call() throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

	}
}
