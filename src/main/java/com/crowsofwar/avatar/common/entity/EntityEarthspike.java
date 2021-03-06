/*
  This file is part of AvatarMod.

  AvatarMod is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  AvatarMod is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with AvatarMod. If not, see <http://www.gnu.org/licenses/>.
*/

package com.crowsofwar.avatar.common.entity;

import com.crowsofwar.avatar.common.AvatarDamageSource;
import com.crowsofwar.avatar.common.bending.BattlePerformanceScore;
import com.crowsofwar.avatar.common.config.ConfigStats;
import com.crowsofwar.avatar.common.data.BendingData;
import com.crowsofwar.gorecore.util.Vector;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

import static com.crowsofwar.avatar.common.config.ConfigSkills.SKILLS_CONFIG;
import static com.crowsofwar.avatar.common.config.ConfigStats.STATS_CONFIG;

/**
 * @author CrowsOfWar
 */
public class EntityEarthspike extends AvatarEntity {

	private static final DataParameter<Float> SYNC_SIZE = EntityDataManager
			.createKey(EntityEarthspike.class, DataSerializers.FLOAT);

	private double damage;
	private float Size;
	private int attacked;
	private double lifetime;

	public EntityEarthspike(World world) {
		super(world);
		this.Size = 1;
		setSize(Size, Size);
		//DO NOT CALL THIS ONUPDATE; THE EARTHSPIKE WILL HAVE SIZE VARIATION DEPENDING ON HOW
		// LONG THE SPAWNER HAS EXISTED.
		this.attacked = 0;
		this.damage = STATS_CONFIG.earthspikeSettings.damage;
		this.noClip = true;
		this.lifetime = 30;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public void setSize(float size) {
		dataManager.set(SYNC_SIZE, size);
	}

	public float getSize() {
		return dataManager.get(SYNC_SIZE);
	}

	public void setLifetime(double lifetime) {
		this.lifetime = lifetime;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(SYNC_SIZE, 1F);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
	}

	@Override
	protected boolean canCollideWith(Entity entity) {
		return true;

	}

	@Override
	public void onEntityUpdate() {

		super.onEntityUpdate();
		this.motionX = 0;
		this.motionY = 0;
		this.motionZ = 0;

		if (ticksExisted >= lifetime) {
			this.setDead();
		}

		BlockPos below = getPosition().offset(EnumFacing.DOWN);
		Block belowBlock = world.getBlockState(below).getBlock();

		if (!world.isRemote && !ConfigStats.STATS_CONFIG.bendableBlocks.contains(belowBlock)) {
			setDead();
		}


		// Push collided entities back
		if (!world.isRemote) {
			AxisAlignedBB box = new AxisAlignedBB(posX - Size, posY - Size, posZ - Size, posX + Size, posY + Size, posZ + Size);
			List<Entity> collided = world.getEntitiesWithinAABB(EntityLivingBase.class, box);
			if (!collided.isEmpty()) {
				for (Entity entity : collided) {
					onCollideWithEntity(entity);
				}
			}
		}
	}

	@Override
	protected void onCollideWithEntity(Entity entity) {
		if (!world.isRemote && entity != getOwner() && !(entity instanceof EntityEarthspike) && !(entity instanceof EntityEarthspikeSpawner)) {
			pushEntity(entity);
			if (attackEntity(entity)) {
				attacked++;
				if (getOwner() != null) {
					BattlePerformanceScore.addMediumScore(getOwner());
				}

			}
			if (getOwner() != null && getAbility() != null) {
				BendingData data = BendingData.get(getOwner());
				if (data != null) {
					data.getAbilityData(getAbility().getName()).addXp(SKILLS_CONFIG.earthspikeHit * attacked);
				}
			}
		}

	}

	private boolean attackEntity(Entity entity) {
		if (!(entity instanceof EntityItem)) {
			DamageSource ds = AvatarDamageSource.causeEarthspikeDamage(entity, getOwner());
			float damage = (float) this.damage;
			return entity.attackEntityFrom(ds, damage);
			//Modify damage based on power rating!
		} else return false;
	}

	private void pushEntity(Entity entity) {
		entity.motionX = this.motionX / 4;
		entity.motionY = STATS_CONFIG.earthspikeSettings.push/1.5 + damage/20;
		entity.motionZ = this.motionZ / 4;
	}
}
