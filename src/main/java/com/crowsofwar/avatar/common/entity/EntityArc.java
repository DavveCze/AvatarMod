package com.crowsofwar.avatar.common.entity;

import com.crowsofwar.avatar.common.entityproperty.EntityPropertyVector;
import com.crowsofwar.avatar.common.util.VectorUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public abstract class EntityArc extends Entity implements IPhysics {
	
	private static final int DATAWATCHER_ID = 3, DATAWATCHER_VELOCITY = 4, // 4,5,6
			DATAWATCHER_GRAVITY = 7;
	
	private static int nextId = 1;
	private EntityControlPoint[] points;
	
	private Vec3 internalPos;
	private EntityPropertyVector velocity;
	
	protected EntityPlayer owner;
	
	public EntityArc(World world) {
		super(world);
		float size = .2f;
		setSize(size, size);
		
		this.points = new EntityControlPoint[getAmountOfControlPoints()];
		for (int i = 0; i < points.length; i++) {
			points[i] = createControlPoint(size);
		}
		
		this.internalPos = Vec3.createVectorHelper(0, 0, 0);
		this.velocity = new EntityPropertyVector(this, dataWatcher, DATAWATCHER_VELOCITY);
		if (!worldObj.isRemote) {
			setId(nextId++);
		}
	}
	
	/**
	 * Called from the EntityArc constructor to create a new control point entity.
	 * 
	 * @param size
	 * @return
	 */
	protected EntityControlPoint createControlPoint(float size) {
		return new EntityControlPoint(this, size, 0, 0, 0);
	}
	
	@Override
	protected void entityInit() {
		dataWatcher.addObject(DATAWATCHER_ID, 0);
		dataWatcher.addObject(DATAWATCHER_GRAVITY, (byte) 0);
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		if (this.ticksExisted == 1) {
			for (int i = 0; i < points.length; i++) {
				points[i].setPosition(getPosition());
				worldObj.spawnEntityInWorld(points[i]);
			}
		}
		
		ignoreFrustumCheck = true;
		
		Vec3 vel = getVelocity();
		if (ticksExisted % 5 == 0) {
			velocity.sync();
		}
		
		if (isGravityEnabled()) {
			addVelocity(getGravityVector());
		}
		
		moveEntity(vel.xCoord / 20, vel.yCoord / 20, vel.zCoord / 20);
		getLeader().setPosition(posX, posY, posZ);
		getLeader().setVelocity(getVelocity());
		
		if (isCollided) {
			setDead();
			onCollideWithBlock();
		}
		
		for (int i = 1; i < points.length; i++) {
			
			EntityControlPoint leader = points[i - 1];
			EntityControlPoint p = points[i];
			Vec3 leadPos = i == 0 ? getPosition() : getLeader(i).getPosition();
			double sqrDist = p.getPosition().squareDistanceTo(leadPos);
			
			if (sqrDist > getControlPointTeleportDistanceSq()) {
				
				p.setPosition(leader.getXPos(), leader.getYPos(), leader.getZPos());
				
			} else if (sqrDist > getControlPointMaxDistanceSq()) {
				
				Vec3 diff = VectorUtils.minus(leader.getPosition(), p.getPosition());
				diff.normalize();
				VectorUtils.mult(diff, 0.15);
				p.addVelocity(diff);
				
			}
			
		}
		
	}
	
	protected abstract void onCollideWithBlock();
	
	protected abstract Vec3 getGravityVector();
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		setDead();
	}
	
	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		setDead();// IEntityMultiPart
	}
	
	@Override
	public void setPosition(double x, double y, double z) {
		super.setPosition(x, y, z);
		// Set position - called from entity constructor, so points might be null
		if (points != null) {
			points[0].setPosition(x, y, z);
		}
	}
	
	@Override
	public void setDead() {
		super.setDead();
		for (EntityControlPoint point : points)
			point.setDead();
	}
	
	public EntityControlPoint[] getControlPoints() {
		return points;
	}
	
	public EntityControlPoint getControlPoint(int index) {
		return points[index];
	}
	
	/**
	 * Get the first control point in this arc.
	 */
	public EntityControlPoint getLeader() {
		return points[0];// EntityDragon
	}
	
	/**
	 * Get the leader of the specified control point.
	 */
	public EntityControlPoint getLeader(int index) {
		return points[index == 0 ? index : index - 1];
	}
	
	public int getId() {
		return dataWatcher.getWatchableObjectInt(DATAWATCHER_ID);
	}
	
	public void setId(int id) {
		dataWatcher.updateObject(DATAWATCHER_ID, id);
	}
	
	public static EntityArc findFromId(World world, int id) {
		for (Object obj : world.loadedEntityList) {
			if (obj instanceof EntityArc && ((EntityArc) obj).getId() == id) return (EntityArc) obj;
		}
		return null;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double d) {
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender(float p_70070_1_) {
		return 15728880;
	}
	
	@Override
	public Vec3 getPosition() {
		internalPos.xCoord = posX;
		internalPos.yCoord = posY;
		internalPos.zCoord = posZ;
		return internalPos;
	}
	
	@Override
	public Vec3 getVelocity() {
		return velocity.getValue();
	}
	
	@Override
	public void setVelocity(Vec3 vel) {
		velocity.setValue(vel);
	}
	
	@Override
	public void addVelocity(Vec3 vel) {
		setVelocity(VectorUtils.plus(getVelocity(), vel));
	}
	
	public boolean isGravityEnabled() {
		return dataWatcher.getWatchableObjectByte(DATAWATCHER_GRAVITY) == 1;
	}
	
	public void setGravityEnabled(boolean enabled) {
		dataWatcher.updateObject(DATAWATCHER_GRAVITY, (byte) (enabled ? 1 : 0));
	}
	
	public EntityPlayer getOwner() {
		return owner;
	}
	
	public void setOwner(EntityPlayer owner) {
		this.owner = owner;
		for (EntityControlPoint cp : points)
			cp.setOwner(owner);
	}
	
	@Override
	public boolean shouldRenderInPass(int pass) {
		return pass == 1;
	}
	
	/**
	 * Returns the amount of control points which will be created.
	 */
	public int getAmountOfControlPoints() {
		return 5;
	}
	
	/**
	 * Returns the maximum distance between control points, squared. Any control points beyond this
	 * distance will follow their leader to get closer.
	 */
	protected double getControlPointMaxDistanceSq() {
		return 1;
	}
	
	/**
	 * Returns the distance between control points to be teleported to their leader, squared. If any
	 * control point is more than this distance from its leader, then it is teleported to the
	 * leader.
	 */
	protected double getControlPointTeleportDistanceSq() {
		return 36;
	}
	
}
