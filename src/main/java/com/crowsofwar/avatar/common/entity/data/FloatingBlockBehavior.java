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

package com.crowsofwar.avatar.common.entity.data;

import static com.crowsofwar.avatar.common.config.ConfigStats.STATS_CONFIG;
import static com.crowsofwar.avatar.common.config.ConfigSkills.SKILLS_CONFIG;

import java.util.List;

import com.crowsofwar.avatar.common.AvatarDamageSource;
import com.crowsofwar.avatar.common.bending.BendingAbility;
import com.crowsofwar.avatar.common.bending.BendingManager;
import com.crowsofwar.avatar.common.bending.BendingType;
import com.crowsofwar.avatar.common.bending.earth.FloatingBlockEvent;
import com.crowsofwar.avatar.common.data.AvatarPlayerData;
import com.crowsofwar.avatar.common.entity.EntityFloatingBlock;
import com.crowsofwar.gorecore.util.Vector;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 
 * 
 * @author CrowsOfWar
 */
public abstract class FloatingBlockBehavior extends Behavior<EntityFloatingBlock> {
	
	public static final DataSerializer<FloatingBlockBehavior> DATA_SERIALIZER = new Behavior.BehaviorSerializer<>();
	
	public static int ID_NOTHING, ID_FALL, ID_PICKUP, ID_PLACE, ID_PLAYER_CONTROL, ID_THROWN;
	
	public static void register() {
		DataSerializers.registerSerializer(DATA_SERIALIZER);
		ID_NOTHING = registerBehavior(DoNothing.class);
		ID_FALL = registerBehavior(Fall.class);
		ID_PICKUP = registerBehavior(PickUp.class);
		ID_PLACE = registerBehavior(Place.class);
		ID_PLAYER_CONTROL = registerBehavior(PlayerControlled.class);
		ID_THROWN = registerBehavior(Thrown.class);
	}
	
	public FloatingBlockBehavior() {}
	
	public FloatingBlockBehavior(EntityFloatingBlock entity) {
		super(entity);
	}
	
	protected void applyGravity() {
		entity.velocity().add(0, -9.81 / 20, 0);
	}
	
	public static class DoNothing extends FloatingBlockBehavior {
		
		@Override
		public FloatingBlockBehavior onUpdate() {
			return this;
		}
		
		@Override
		public void fromBytes(PacketBuffer buf) {}
		
		@Override
		public void toBytes(PacketBuffer buf) {}
		
		@Override
		public void load(NBTTagCompound nbt) {}
		
		@Override
		public void save(NBTTagCompound nbt) {}
		
	}
	
	public static class Place extends FloatingBlockBehavior {
		
		private BlockPos placeAt;
		
		public Place() {}
		
		public Place(BlockPos placeAt) {
			this.placeAt = placeAt;
		}
		
		@Override
		public FloatingBlockBehavior onUpdate() {
			Vector placeAtVec = new Vector(placeAt.getX() + 0.5, placeAt.getY() + 0.25, placeAt.getZ() + 0.5);
			Vector thisPos = new Vector(entity);
			Vector force = placeAtVec.minus(thisPos);
			force.normalize();
			force.mul(3);
			entity.velocity().set(force);
			if (!entity.worldObj.isRemote && placeAtVec.sqrDist(thisPos) < 0.01) {
				
				entity.setDead();
				entity.worldObj.setBlockState(new BlockPos(entity), entity.getBlockState());
				
				BendingManager.getBending(BendingType.EARTHBENDING)
						.post(new FloatingBlockEvent.BlockPlacedReached(entity));
				
			}
			
			return this;
		}
		
		@Override
		public void fromBytes(PacketBuffer buf) {
			placeAt = buf.readBlockPos();
		}
		
		@Override
		public void toBytes(PacketBuffer buf) {
			buf.writeBlockPos(placeAt);
		}
		
		@Override
		public void load(NBTTagCompound nbt) {
			placeAt = new BlockPos(nbt.getInteger("PlaceX"), nbt.getInteger("PlaceY"),
					nbt.getInteger("PlaceZ"));
		}
		
		@Override
		public void save(NBTTagCompound nbt) {
			nbt.setInteger("PlaceX", placeAt.getX());
			nbt.setInteger("PlaceY", placeAt.getY());
			nbt.setInteger("PlaceZ", placeAt.getZ());
		}
		
	}
	
	public static class Thrown extends FloatingBlockBehavior {
		
		public Thrown() {}
		
		/**
		 * @param entity
		 */
		public Thrown(EntityFloatingBlock entity) {
			super(entity);
		}
		
		@Override
		public FloatingBlockBehavior onUpdate() {
			
			if (entity.isCollided) {
				if (!entity.worldObj.isRemote) entity.setDead();
				entity.onCollision();
				BendingManager.getBending(BendingType.EARTHBENDING)
						.post(new FloatingBlockEvent.BlockThrownReached(entity));
				
			}
			
			applyGravity();
			
			World world = entity.worldObj;
			if (!entity.isDead) {
				List<Entity> collidedList = world.getEntitiesWithinAABBExcludingEntity(entity,
						entity.getExpandedHitbox());
				if (!collidedList.isEmpty()) {
					Entity collided = collidedList.get(0);
					if (collided instanceof EntityLivingBase && collided != entity.getOwner()) {
						collision((EntityLivingBase) collided);
					} else if (collided != entity.getOwner()) {
						Vector motion = new Vector(collided).minus(new Vector(entity));
						motion.mul(0.3);
						motion.setY(0.08);
						collided.addVelocity(motion.x(), motion.y(), motion.z());
					}
					
				}
			}
			
			return this;
			
		}
		
		private void collision(EntityLivingBase collided) {
			// Add damage
			double speed = entity.velocity().magnitude();
			collided.attackEntityFrom(
					AvatarDamageSource.causeFloatingBlockDamage(collided, entity.getOwner()),
					(float) (speed * STATS_CONFIG.floatingBlockSettings.damage * entity.getDamageMult()));
			
			// Push entity
			Vector motion = new Vector(collided).minus(new Vector(entity));
			motion.mul(STATS_CONFIG.floatingBlockSettings.push);
			motion.setY(0.08);
			collided.addVelocity(motion.x(), motion.y(), motion.z());
			
			// Add XP
			System.out.println("Adding XP; health now " + collided.getHealth());
			AvatarPlayerData data = AvatarPlayerData.fetcher().fetch(entity.getOwner());
			if (!collided.worldObj.isRemote && data != null) {
				float xp = SKILLS_CONFIG.blockThrowHit;
				if (collided.getHealth() <= 0) {
					System.out.println("That's a confirmed kill");
					xp = SKILLS_CONFIG.blockKill;
				}
				data.getAbilityData(BendingAbility.ABILITY_PICK_UP_BLOCK).addXp(xp);
			}
			
			// Remove the floating block & spawn particles
			if (!entity.worldObj.isRemote) entity.setDead();
			entity.onCollision();
		}
		
		@Override
		public void fromBytes(PacketBuffer buf) {}
		
		@Override
		public void toBytes(PacketBuffer buf) {}
		
		@Override
		public void load(NBTTagCompound nbt) {}
		
		@Override
		public void save(NBTTagCompound nbt) {}
		
	}
	
	public static class PickUp extends FloatingBlockBehavior {
		
		public PickUp() {}
		
		/**
		 * @param entity
		 */
		public PickUp(EntityFloatingBlock entity) {
			super(entity);
		}
		
		@Override
		public FloatingBlockBehavior onUpdate() {
			applyGravity();
			
			Vector velocity = entity.velocity();
			if (velocity.y() <= 0) {
				velocity.setY(0);
				entity.velocity().set(velocity);
				return new PlayerControlled(entity, entity.getOwner());
			}
			
			return this;
		}
		
		@Override
		public void fromBytes(PacketBuffer buf) {}
		
		@Override
		public void toBytes(PacketBuffer buf) {}
		
		@Override
		public void load(NBTTagCompound nbt) {}
		
		@Override
		public void save(NBTTagCompound nbt) {}
		
	}
	
	public static class PlayerControlled extends FloatingBlockBehavior {
		
		public PlayerControlled() {}
		
		public PlayerControlled(EntityFloatingBlock entity, EntityPlayer player) {
			super(entity);
		}
		
		private EntityPlayer getControllingPlayer() {
			return entity.getOwner();
		}
		
		@Override
		public FloatingBlockBehavior onUpdate() {
			EntityPlayer controller = getControllingPlayer();
			
			if (controller == null) return this;
			
			AvatarPlayerData data = AvatarPlayerData.fetcher().fetch(controller,
					"Could not get player data to update PlayerControlled entity Block");
			
			double yaw = Math.toRadians(controller.rotationYaw);
			double pitch = Math.toRadians(controller.rotationPitch);
			Vector forward = Vector.fromYawPitch(yaw, pitch);
			Vector eye = Vector.getEyePos(controller);
			Vector target = forward.times(2).plus(eye);
			Vector motion = target.minus(new Vector(entity));
			motion.mul(5);
			entity.velocity().set(motion);
			
			return this;
		}
		
		@Override
		public void fromBytes(PacketBuffer buf) {}
		
		@Override
		public void toBytes(PacketBuffer buf) {}
		
		@Override
		public void load(NBTTagCompound nbt) {}
		
		@Override
		public void save(NBTTagCompound nbt) {}
		
	}
	
	public static class Fall extends FloatingBlockBehavior {
		
		@Override
		public FloatingBlockBehavior onUpdate() {
			applyGravity();
			return this;
		}
		
		@Override
		public void fromBytes(PacketBuffer buf) {}
		
		@Override
		public void toBytes(PacketBuffer buf) {}
		
		@Override
		public void load(NBTTagCompound nbt) {}
		
		@Override
		public void save(NBTTagCompound nbt) {}
		
	}
	
}
