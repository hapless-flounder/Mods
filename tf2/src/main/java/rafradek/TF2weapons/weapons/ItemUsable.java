package rafradek.TF2weapons.weapons;

import java.util.HashMap;
import java.util.UUID;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import rafradek.TF2weapons.ItemFromData;
import rafradek.TF2weapons.TF2Attribute;
import rafradek.TF2weapons.TF2Util;
import rafradek.TF2weapons.WeaponData.PropertyType;
import rafradek.TF2weapons.building.EntityDispenser;
import rafradek.TF2weapons.characters.EntityTF2Character;
import rafradek.TF2weapons.characters.ItemToken;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.WeaponData;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.message.TF2Message.PredictionMessage;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class ItemUsable extends ItemFromData {
	// public ConfigCategory data;
	// public String render;
	public static int sps;
	// public static int tickleft;
	// public static boolean addedIcons;
	// public static ThreadLocalMap<EntityLivingBase, NBTTagCompound>
	// itemProperties=new ThreadLocalMap<EntityLivingBase, NBTTagCompound>();
	public static HashMap<EntityLivingBase, float[]> lastDamage = new HashMap<EntityLivingBase, float[]>();
	
	public ItemUsable() {
		super();
		this.setCreativeTab(TF2weapons.tabweapontf2);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public CreativeTabs getCreativeTab() {
		return TF2weapons.tabweapontf2;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn,
			EnumHand hand) {
		ItemStack itemStackIn=playerIn.getHeldItem(hand);
		return new ActionResult<ItemStack>((this.canAltFire(worldIn, playerIn, itemStackIn)
				&& this.getAltFiringSpeed(itemStackIn, playerIn) != Short.MAX_VALUE) || playerIn.getCapability(TF2weapons.WEAPONS_CAP, null).fire1Cool>0 ? EnumActionResult.SUCCESS
						: EnumActionResult.PASS,
				itemStackIn);
	}

	public abstract boolean use(ItemStack stack, EntityLivingBase living, World world, EnumHand hand,
			PredictionMessage message);

	public boolean startUse(ItemStack stack, EntityLivingBase living, World world, int oldState, int newState) {
		living.getCapability(TF2weapons.WEAPONS_CAP, null).pressedStart = true;
		return false;
	}

	public int getAmmoType(ItemStack stack) {
		return TF2Attribute.getModifier("No Ammo", stack, 0, null) != 0 ? 0:getData(stack).getInt(PropertyType.AMMO_TYPE);
	}

	public int getActualAmmoUse(ItemStack stack, EntityLivingBase living, int amount) {
		if (this.getAmmoType(stack) == 0)
			return 0;

		if (TF2Attribute.getModifier("Metal Ammo", stack, 0, living) != 0) {
			return (int) TF2Attribute.getModifier("Metal Ammo", stack, 0, living);
		}
		stack.getTagCompound().setFloat("UsedAmmo", stack.getTagCompound().getFloat("UsedAmmo")
				+ amount * TF2Attribute.getModifier("Ammo Eff", stack, 1, living));
		amount = 0;
		while (stack.getTagCompound().getFloat("UsedAmmo") >= 1) {
			stack.getTagCompound().setFloat("UsedAmmo", stack.getTagCompound().getFloat("UsedAmmo") - 1);
			amount++;
		}
		return amount;
	}

	public boolean endUse(ItemStack stack, EntityLivingBase living, World world, int oldState, int newState) {
		return false;
	}

	@Override
	public void onUpdate(ItemStack stack, World par2World, Entity par3Entity, int par4, boolean par5) {
		super.onUpdate(stack, par2World, par3Entity, par4, par5);
		if(stack.isEmpty())
			return;
		/*
		 * if(itemProperties.get(par2World.isRemote).get(par3Entity)==null){
		 * itemProperties.get(par2World.isRemote).put((EntityLivingBase)
		 * par3Entity, new NBTTagCompound()); }
		 */
		WeaponsCapability cap = par3Entity.getCapability(TF2weapons.WEAPONS_CAP, null);
		WeaponData.WeaponDataCapability stackcap = stack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null);
		EntityLivingBase living=(EntityLivingBase) par3Entity;
		if (stackcap.active == 0 && par5) {
			stackcap.active = 1;
			// itemProperties.get(par2World.isRemote).get(par3Entity).setShort("reloadd",
			// (short) 800);

			if(!par2World.isRemote && living.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MAX_HEALTH).getModifier(ItemWeapon.HEALTH_MODIFIER)!=null){
				float addHealth=(float) living.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MAX_HEALTH).getModifier(ItemWeapon.HEALTH_MODIFIER).getAmount();
				living.setHealth((living.getMaxHealth())/(living.getMaxHealth()-addHealth)*living.getHealth());
			}
			cap.fire1Cool = this.getDeployTime(stack, living);
			cap.fire2Cool = this.getDeployTime(stack, living);
		} else if (stackcap.active > 0
				&& stack != living.getHeldItemOffhand() && !par5) {
			if (stackcap.active == 2 && (cap.state & 3) > 0)
				this.endUse(stack, living, par2World, cap.state, 0);
			
			stackcap.active = 0;
			this.holster(cap, stack, living, par2World);
			cap.lastWeapon = stack;
		}
		if (par3Entity.ticksExisted % 5 == 0 && stackcap.active == 2
				&& TF2Attribute.getModifier("Mark Death", stack, 0, living) > 0)
			living.addPotionEffect(new PotionEffect(TF2weapons.markDeath,
					(int) TF2Attribute.getModifier("Mark Death", stack, 0,living) * 20));
	}

	public void draw(WeaponsCapability weaponsCapability, ItemStack stack, EntityLivingBase living, World world) {
		if(living instanceof EntityPlayerMP)
			TF2weapons.network.sendTo(new TF2Message.UseMessage(stack.getItemDamage(), 
					false,this.getAmmoAmount(living, stack), EnumHand.MAIN_HAND),(EntityPlayerMP) living);
	}

	public void holster(WeaponsCapability cap, ItemStack stack, EntityLivingBase living, World world) {
		cap.chargeTicks = 0;
		cap.setCharging(false);

	}
	public static double calculateModifiers(IAttributeInstance attribute, UUID except,double initial,double additionToMult){
		double initialO=initial;
		for(AttributeModifier modifier:attribute.getModifiersByOperation(0)){
			if(!modifier.getID().equals(except)){
				initial+=initialO*modifier.getAmount()*additionToMult;
			}
		}
		for(AttributeModifier modifier:attribute.getModifiersByOperation(1)){
			if(!modifier.getID().equals(except)){
				initial+=initialO*modifier.getAmount();
			}
		}
		for(AttributeModifier modifier:attribute.getModifiersByOperation(2)){
			if(!modifier.getID().equals(except)){
				initial*=1+modifier.getAmount();
			}
		}
		return initial;
	}
	@Override
	public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
		/*item.getTagCompound().removeTag("active");
		player.getCapability(TF2weapons.WEAPONS_CAP, null).state = 0;
		if(!player.world.isRemote)
			TF2weapons.network.sendTo(new TF2Message.WeaponDroppedMessage(ItemFromData.getData(item).getName()), (EntityPlayerMP) player);
		this.holster(player.getCapability(TF2weapons.WEAPONS_CAP, null), item, player, player.world);*/
		return true;
	}

	public boolean canFire(World world, EntityLivingBase living, ItemStack stack) {
		return stack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).active > 0 && ItemToken.allowUse(living, this.getUsableClasses(stack))
				&& (living.getActiveItemStack().isEmpty() || this.getDoubleWieldBonus(stack, living) != 1) && this.getFiringSpeed(stack, living) != Integer.MAX_VALUE;
	}

	public String getUsableClasses(ItemStack stack) {
		if (getData(stack).getString(PropertyType.MOB_TYPE).isEmpty() && getData(stack).hasProperty(PropertyType.BASED_ON)) {
			stack = getNewStack(getData(stack).getString(PropertyType.BASED_ON));
		}
		return getData(stack).getString(PropertyType.MOB_TYPE);
	}
	
	public abstract boolean fireTick(ItemStack stack, EntityLivingBase living, World world);

	public abstract boolean altFireTick(ItemStack stack, EntityLivingBase living, World world);

	/*
	 * public void registerIcons(IIconRegister par1IconRegister) { this.itemIcon
	 * = par1IconRegister.registerIcon(this.getIconString()); if(addedIcons)
	 * return; Iterator<String> iterator=MapList.nameToCC.keySet().iterator();
	 * addedIcons=true; while(iterator.hasNext()){ String name=iterator.next();
	 * //System.out.println(MapList.nameToCC.get(name).get("Render").getString()
	 * +" "+name); MapList.nameToIcon.put(name,
	 * par1IconRegister.registerIcon(MapList.nameToCC.get(name).get("Render").
	 * getString())); } //this.itemIcon =
	 * par1IconRegister.registerIcon(getData(stack).get("Render").getString());
	 * }
	 */
	/*
	 * @SideOnly(Side.CLIENT) public boolean requiresMultipleRenderPasses() {
	 * return true; } public int getRenderPasses(int metadata) { return 1; }
	 */

	public int getFiringSpeed(ItemStack stack, EntityLivingBase living) {
		int speed=(int) (TF2Attribute.getModifier("Fire Rate", stack,
				ItemFromData.getData(stack).getInt(PropertyType.FIRE_SPEED), living));
		if(living != null && this.isDoubleWielding(living))
			speed *= this.getDoubleWieldBonus(stack, living);
		if(TF2Attribute.getModifier("Fire Rate Health", stack, 1f, living) != 1f)
			speed *= this.getHealthBasedBonus(stack, living, TF2Attribute.getModifier("Fire Rate Health", stack, 1f, living));
		if(living != null && (WeaponsCapability.get(living).isExpJump() || living.isElytraFlying()) && TF2Attribute.getModifier("Airborne Bonus", stack, 0, living) != 0)
			speed *= 0.35f;
		if(living != null && living.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED)!=null){
			//System.out.println("Pre speed "+speed+" "+living.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue());
			double modifiers=calculateModifiers(living.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED),ATTACK_SPEED_MODIFIER,living.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue(),1.4);
			speed *= (living instanceof EntityPlayer? 4:living.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue())/modifiers;
			//System.out.println("Post speed "+speed);
		}
		if (speed <= 0) {
			return Integer.MAX_VALUE;
		}
		return speed;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
		return true;
	}

	public boolean isDoubleWielding(EntityLivingBase living) {
		return ItemFromData.getData(living.getHeldItemMainhand()) != ItemFromData.BLANK_DATA
				&& ItemFromData.getData(living.getHeldItemOffhand()) == ItemFromData
						.getData(living.getHeldItemMainhand())
				&& this.getDoubleWieldBonus(living.getHeldItemMainhand(), living) != 1;
	}

	public float getDoubleWieldBonus(ItemStack stack, EntityLivingBase living) {
		// System.out.println("Double wield type:
		// "+ItemFromData.getData(stack).hasProperty(PropertyType.DUAL_WIELD_SPEED)+"
		// "+ItemFromData.getData(stack).getFloat(PropertyType.DUAL_WIELD_SPEED));
		return !ItemFromData.getData(stack).hasProperty(PropertyType.DUAL_WIELD_SPEED) ? 1f
				: ItemFromData.getData(stack).getFloat(PropertyType.DUAL_WIELD_SPEED);
	}

	public boolean canAltFire(World worldObj, EntityLivingBase player, ItemStack item) {
		// TODO Auto-generated method stub
		return item.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).active > 0
				&& player.getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks == 0
				&& ItemToken.allowUse(player, this.getUsableClasses(item))
				&& (player.getActiveItemStack().isEmpty() || this.getDoubleWieldBonus(item, player) != 1);
	}

	public void altUse(ItemStack stack, EntityLivingBase living, World world) {

	}

	public float getHealthBasedBonus(ItemStack item, EntityLivingBase living, float maxbonus) {
		if(living != null && living.getHealth()<living.getMaxHealth()*0.8f) {
			float multiplier=1f -((living.getHealth()/(living.getMaxHealth()*0.8f)));
			return TF2Util.lerp(1, maxbonus, multiplier);
		}
		return 1f;
	}
	public short getAltFiringSpeed(ItemStack item, EntityLivingBase player) {
		return Short.MAX_VALUE;
	}
	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		if(newStack.hasCapability(TF2weapons.WEAPONS_DATA_CAP, null) && !slotChanged)
			newStack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).active=oldStack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).active;
		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}
	
	public int getDeployTime(ItemStack stack, EntityLivingBase living) {
		return (int) TF2Attribute.getModifier("Deploy Time", stack, 750, living);
	}
	
	public int getStateOverride(ItemStack stack, EntityLivingBase living, int original) {
		if(TF2Attribute.getModifier("Auto Fire", stack, 0, living) != 0) {
			//System.out.println("Act pre: "+original);
			original = original | 4;
			if((original & 1) == 0)
				original = original | 1;
			else if(WeaponsCapability.get(living).fire1Cool == 0)
				original = original & 6;
			//System.out.println("Act post: "+original);
		}
		return original;
	}
	
	public boolean isAmmoSufficient(ItemStack stack, EntityLivingBase living, boolean all) {
		return true;
	}
	
	public void consumeAmmoGlobal(EntityLivingBase living, ItemStack stack, int amount) {
		if (EntityDispenser.isNearDispenser(living.world, living))
			return;
		if (living instanceof EntityTF2Character)
			((EntityTF2Character)living).useAmmo(((ItemWeapon) stack.getItem()).getActualAmmoUse(stack, living, amount));
		if (!(living instanceof EntityPlayer))
			return;
		if (TF2Attribute.getModifier("Metal Ammo", stack, 0, living) != 0) {
			living.getCapability(TF2weapons.WEAPONS_CAP, null).setMetal(living.getCapability(TF2weapons.WEAPONS_CAP, null).getMetal()-amount);
		}
		if (amount > 0) {
			amount = ((ItemWeapon) stack.getItem()).getActualAmmoUse(stack, living, amount);
			// int
			// type=ItemFromData.getData(stack).getInt(PropertyType.AMMO_TYPE);

			// stack.getCount()-=amount;
			ItemStack stackAmmo;
			while (amount > 0 && !(stackAmmo = searchForAmmo(living, stack)).isEmpty()) {
				int inStack;
				if(stackAmmo.getMaxDamage()!=0)
					inStack = stackAmmo.getMaxDamage()-stackAmmo.getItemDamage();
				else
					inStack = stackAmmo.getCount();
				if (stackAmmo.getItem() instanceof ItemAmmo)
					((ItemAmmo) stackAmmo.getItem()).consumeAmmo(living, stackAmmo, amount);
				else {
					if (stackAmmo.getItem() instanceof ItemArrow)
						stack.getTagCompound().setTag("LastLoaded", stackAmmo.serializeNBT());
					stackAmmo.shrink(amount);
				}
				amount -= inStack;
			}
		}
	}

	public ItemStack searchForAmmo(EntityLivingBase owner, ItemStack stack) {
		if (EntityDispenser.isNearDispenser(owner.world, owner) || (owner instanceof EntityPlayer && ((EntityPlayer)owner).capabilities.isCreativeMode))
			return ItemAmmo.STACK_FILL;

		int type = ((ItemUsable) stack.getItem()).getAmmoType(stack);

		if (type == 0)
			return ItemAmmo.STACK_FILL;
		
		if (owner instanceof EntityTF2Character) {
			return ((EntityTF2Character)owner).getAmmo(ItemFromData.getData(stack).getInt(PropertyType.SLOT)) > 0 ? ItemAmmo.STACK_FILL : ItemStack.EMPTY;
		}
			
		if (owner.world.isRemote && owner.getCapability(TF2weapons.PLAYER_CAP, null).cachedAmmoCount[type] > 0)
			return ItemAmmo.STACK_FILL;
		
		
		int metalammo = (int) TF2Attribute.getModifier("Metal Ammo", stack, 0, owner);
		if (metalammo != 0) {
			return owner.getCapability(TF2weapons.WEAPONS_CAP, null).getMetal() >= metalammo ? ItemAmmo.STACK_FILL : ItemStack.EMPTY;
		}
		
		if (!owner.getCapability(TF2weapons.INVENTORY_CAP, null).getStackInSlot(3).isEmpty()){
			IItemHandler inv=owner.getCapability(TF2weapons.INVENTORY_CAP, null).getStackInSlot(3)
					.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			//System.out.println("Ammo Search: "+inv.getSlots());
			for (int i = 0; i < inv.getSlots(); i++) {
				ItemStack stackCap = inv.getStackInSlot(i);
				//System.out.println("Stack: "+stackCap);
				if (!stackCap.isEmpty() && ((stackCap.getItem() instanceof ItemAmmo
						&& ((ItemAmmo) stackCap.getItem()).getTypeInt(stackCap) == type) || (type == 1000 && stackCap.getItem() instanceof ItemArrow)) ){
					//System.out.println("Found: "+i);
					return stackCap;
				}
			}
		}
		for (int i = 0; i < ((EntityPlayer) owner).inventory.mainInventory.size(); i++) {
			ItemStack stackInv = ((EntityPlayer) owner).inventory.mainInventory.get(i);
			if (stackInv != null && ((stackInv.getItem() instanceof ItemAmmo
					&& ((ItemAmmo) stackInv.getItem()).getTypeInt(stackInv) == type) || (type == 1000 && stackInv.getItem() instanceof ItemArrow)))
				return stackInv;
		}
		return ItemStack.EMPTY;
	}

	public int getAmmoAmount(EntityLivingBase owner, ItemStack stack) {
		if (EntityDispenser.isNearDispenser(owner.world, owner) || (owner instanceof EntityPlayer && ((EntityPlayer)owner).capabilities.isCreativeMode))
			return 999;

		if (owner instanceof EntityTF2Character)
			return (int) (((EntityTF2Character) owner).getAmmo() / TF2Attribute.getModifier("Ammo Eff", stack, 1, owner));

		if(TF2Attribute.getModifier("Ball Release", stack, 0, owner)>0)
			stack=ItemFromData.getNewStack("sandmanball");
		int type = this.getAmmoType(stack);

		if (TF2Attribute.getModifier("Metal Ammo", stack, 0, owner) != 0) {
			return owner.getCapability(TF2weapons.WEAPONS_CAP, null).getMetal();
		}
		
		if (type == 0)
			return 999;

		int ammoCount = 0;

		if (!owner.getCapability(TF2weapons.INVENTORY_CAP, null).getStackInSlot(3).isEmpty()){
			IItemHandler inv=owner.getCapability(TF2weapons.INVENTORY_CAP, null).getStackInSlot(3)
			.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			for (int i = 0; i < inv.getSlots(); i++) {
				ItemStack stackCap = inv.getStackInSlot(i);
				if (!stackCap.isEmpty() && stackCap.getItem() instanceof ItemAmmo
						&& ((ItemAmmo) stackCap.getItem()).getTypeInt(stackCap) == type){
					//System.out.println("Found: "+i);
					if(stackCap.getMaxDamage()!=0)
						ammoCount += stackCap.getMaxDamage()-stackCap.getItemDamage();
					else
						ammoCount += stackCap.getCount();
				}
				else if (type == 1000 && !stackCap.isEmpty() && stackCap.getItem() instanceof ItemArrow)
					ammoCount += stackCap.getCount();
			}
		}
		for (int i = 0; i < ((EntityPlayer) owner).inventory.mainInventory.size(); i++) {
			ItemStack stackInv = ((EntityPlayer) owner).inventory.mainInventory.get(i);
			if (!stackInv.isEmpty() && stackInv.getItem() instanceof ItemAmmo
					&& ((ItemAmmo) stackInv.getItem()).getTypeInt(stackInv) == type)
				if(stackInv.getMaxDamage()!=0)
					ammoCount += stackInv.getMaxDamage()-stackInv.getItemDamage();
				else
					ammoCount += stackInv.getCount();
			else if (type == 1000 && !stackInv.isEmpty() && stackInv.getItem() instanceof ItemArrow)
				ammoCount += stackInv.getCount();
		}
		return (int) (ammoCount / TF2Attribute.getModifier("Ammo Eff", stack, 1, owner));
	}

}
