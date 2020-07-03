package com.crowsofwar.avatar.common.entity;

import com.crowsofwar.avatar.common.bending.water.Waterbending;
import com.crowsofwar.avatar.common.particle.ParticleBuilder;
import com.crowsofwar.avatar.common.util.AvatarUtils;
import com.crowsofwar.avatar.common.util.Raytrace;
import com.crowsofwar.gorecore.util.Vector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;


public class EntityWaterCannon extends EntityArc<EntityWaterCannon.WaterControlPoint> implements IShieldEntity {

    private static final DataParameter<Float> SYNC_SPEED = EntityDataManager
            .createKey(EntityWaterArc.class, DataSerializers.FLOAT);
    private final float velocityMultiplier;
    /**
     * The amount of ticks since last played splash sound. -1 for splashable.
     */
    private int lastPlayedSplash;
    private boolean isSpear;
    private float damageMult;


    public EntityWaterCannon(World world) {
        super(world);
        setSize(0.2F, 0.2F);
        this.lastPlayedSplash = -1;
        this.noClip = false;
        this.damageMult = 1;
        this.putsOutFires = true;
        this.velocityMultiplier = 5;
    }

    public float getDamageMult() {
        return damageMult;
    }

    public void setDamageMult(float mult) {
        this.damageMult = mult;
    }

    public void isSpear(boolean isSpear) {
        this.isSpear = isSpear;
    }


    public float getSpeed() {
        return dataManager.get(SYNC_SPEED);
    }

    public void setSpeed(float speed) {
        dataManager.set(SYNC_SPEED, speed);
    }


    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(SYNC_SPEED, 30F);
    }


    @Override
    public void spawnExplosionParticles(World world, Vec3d pos) {

    }

    @Override
    public void spawnDissipateParticles(World world, Vec3d pos) {

    }

    @Override
    public void spawnPiercingParticles(World world, Vec3d pos) {

    }

    @Nullable
    @Override
    public SoundEvent[] getSounds() {
        SoundEvent[] events = new SoundEvent[1];
        events[0] = SoundEvents.ENTITY_GENERIC_SPLASH;
        return events;
    }

    @Override
    public boolean onCollideWithSolid() {
        if (getOwner() != null) {
            for (int i = 0; i < getAmountOfControlPoints(); i++) {
                Vec3d dir = i == 0 ? Vector.getLookRectangular(getOwner()).times(Math.min(ticksExisted / 20F, 1) * getSpeed() / 5F).toMinecraft()
                        : getControlPoint(i - 1).position().minus(getControlPoint(i).position()).normalize().toMinecraft();
                RayTraceResult re = Raytrace.rayTrace(world,
                        getControlPoint(i).position().toMinecraft(), dir,
                        getAvgSize(), true, true, false,
                        Entity.class, entity -> !canCollideWith(entity));
                if (re != null && re.hitVec != null) {
                   // setVelocity(re.hitVec.subtract(position().toMinecraft()).scale(getVelocityMultiplier() / 2));
                   // setPosition(re.hitVec);
                }
            }
        }
        return false;
    }

    @Override
    public void applyElementalContact(AvatarEntity entity) {
        super.applyElementalContact(entity);
        if (entity.getTier() <= getTier())
            entity.onMajorWaterContact();
        else entity.onMinorWaterContact();
    }

    @Override
    public void setDead() {
        super.setDead();
    }

    @Override
    protected void updateCpBehavior() {
        getLeader().setPosition(position().plusY(height / 2));
        getLeader().setVelocity(velocity());

        // Move control points to follow leader

        if (getOwner() != null) {
        for (int i = 1; i < points.size() - 1; i++) {

            ControlPoint leader = points.get(i - 1);
            ControlPoint p = points.get(i);
            Vector leadPos = leader.position();
            double sqrDist = p.position().sqrDist(leadPos);

            if (sqrDist > getControlPointTeleportDistanceSq() && getControlPointTeleportDistanceSq() != -1) {

                Vector toFollowerDir = p.position().minus(leader.position()).normalize();

                double idealDist = Math.sqrt(getControlPointTeleportDistanceSq());
                if (idealDist > 1) idealDist -= 1; // Make sure there is some room

                Vector revisedOffset = leader.position().plus(toFollowerDir.times(idealDist));
                p.setPosition(revisedOffset);
                leader.setPosition(revisedOffset);
                p.setVelocity(Vector.ZERO);

            } else if (sqrDist > getControlPointMaxDistanceSq() && getControlPointMaxDistanceSq() != -1) {
                Vector lookPos = Vector.getLookRectangular(getOwner())
                        .times(getSpeed() / 5F * ((float) i / getAmountOfControlPoints())).plus(Vector.getEntityPos(getOwner()))
                        .plusY(getOwner().getEyeHeight() - 0.5);
                Vector diff = lookPos.minus(p.position());
                diff = diff.times(getVelocityMultiplier() / 2);
                p.setVelocity(diff);

            }
        }

            Vector lookPos = Vector.getLookRectangular(getOwner())
                    .times(0.5).plus(Vector.getEntityPos(getOwner()))
                    .plusY(getOwner().getEyeHeight() - 0.5);
            WaterControlPoint point = getControlPoint(points.size() - 1);
            point.setVelocity(lookPos.minus(point.position()).times(getVelocityMultiplier()));
        }
    }

    @Override
    public void onUpdate() {

        super.onUpdate();
        if (lastPlayedSplash > -1) {
            lastPlayedSplash++;
            if (lastPlayedSplash > 20) lastPlayedSplash = -1;
        }


        if (getOwner() == null) {
            this.setDead();
        }
        setEntitySize(getAvgSize());

        if (world.isRemote && getOwner() != null) {
            Vec3d[] points = new Vec3d[getAmountOfControlPoints()];
            for (int i = 0; i < points.length; i++)
                points[i] = getControlPoint(i).position().toMinecraft();
            //Particles! Let's do this.
            //First, we need a bezier curve. Joy.
            //Iterate through all of the control points.
            //0 is the leader/front one
            for (int i = 0; i < getAmountOfControlPoints(); i++) {
                if (i < getAmountOfControlPoints() - 1) {
                    //for (int j = 0; j < 4; j++) {
                    Vec3d pos = getControlPoint(points.length - i - 1).position().toMinecraft();
                    if (pos.distanceTo(getOwner().getPositionVector()) <= Math.min(ticksExisted / 20F, 1) * getSpeed() * 7.5F) {
                        Vec3d pos2 = i < points.length - 1 ? getControlPoint(Math.max(points.length - i - 2, 0)).position().toMinecraft() : Vec3d.ZERO;

                        for (int h = 0; h < 6; h++) {
                            pos = pos.add(AvatarUtils.bezierCurve(((points.length - i - 1D / (h + 1)) / points.length), points));

                            //Flow animation
                            pos2 = pos2.add(AvatarUtils.bezierCurve(Math.min(((points.length - i - 2D / (h + 1D)) / points.length), 1), points));
                            Vec3d circlePos = Vector.getOrthogonalVector(getLookVec(), (ticksExisted % 360) * 20 + h * 60, getAvgSize() / (4 - (i + 1D) / getAmountOfControlPoints())).toMinecraft().add(pos);
                            Vec3d targetPos = i < points.length - 1 ? Vector.getOrthogonalVector(getLookVec(),
                                    (ticksExisted % 360) * 20 + h * 60 + 20, getAvgSize() / (2 - (i + 1D) / getAmountOfControlPoints())).toMinecraft().add(pos2)
                                    : Vec3d.ZERO;
                            Vec3d vel = new Vec3d(world.rand.nextGaussian() / 240, world.rand.nextGaussian() / 240, world.rand.nextGaussian() / 240);

                            if (targetPos != circlePos)
                                vel = targetPos == Vec3d.ZERO ? vel : targetPos.subtract(circlePos).normalize().scale(0.075).add(vel);
                            ParticleBuilder.create(ParticleBuilder.Type.CUBE).pos(circlePos).spawnEntity(this).vel(vel)
                                    .clr(0, 102, 255, 145).scale(getAvgSize() / 2).target(targetPos == Vec3d.ZERO ? pos : targetPos)
                                    .time(14 + AvatarUtils.getRandomNumberInRange(0, 9)).collide(true).element(new Waterbending()).spawn(world);
                        }

                        //Particles along the line
                        for (int h = 0; h < 6; h++) {
                            pos = pos.add(AvatarUtils.bezierCurve(((points.length - i - 1D / (h + 1)) / points.length), points));
                            ParticleBuilder.create(ParticleBuilder.Type.CUBE).pos(pos).spawnEntity(this).vel(world.rand.nextGaussian() / 120,
                                    world.rand.nextGaussian() / 120, world.rand.nextGaussian() / 120).clr(0, 102, 255, 185)
                                    .time(12 + AvatarUtils.getRandomNumberInRange(0, 5)).scale(getAvgSize()).target(pos).collide(true).element(new Waterbending()).spawn(world);

                        }
                        //Dripping water particles
                        for (int h = 0; h < 2; h++) {
                            pos = pos.add(AvatarUtils.bezierCurve(((points.length - i - 1D / (h + 1)) / points.length), points));
                            ParticleBuilder.create(ParticleBuilder.Type.CUBE).pos(pos).spawnEntity(this).vel(world.rand.nextGaussian() / 20,
                                    world.rand.nextDouble() / 12, world.rand.nextGaussian() / 20).clr(0, 102, 255, 185)
                                    .time(6 + AvatarUtils.getRandomNumberInRange(0, 3)).scale(getAvgSize()).gravity(true).collide(true).element(new Waterbending()).spawn(world);
                        }
                    }
                    //}
                }
            }
        }
        if (getOwner() != null)
            setVelocity((Vector.getEntityPos(getOwner())
                    .plus(0, getOwner().getEyeHeight() - 0.5, 0).plus(Vector.getLookRectangular(getOwner()).times(getSpeed() / 5F))).minus(position())
                    .times(getVelocityMultiplier() / 4));

    }


    @Override
    public int getFireTime() {
        return 0;
    }

    @Override
    protected EntityWaterCannon.WaterControlPoint createControlPoint(float size, int index) {
        return new EntityWaterCannon.WaterControlPoint(this, size, posX, posY, posZ);
    }

    @Override
    public boolean canBePushed() {
        return true;
    }

    public boolean canPlaySplash() {
        return lastPlayedSplash == -1;
    }

    public void playSplash() {
        world.playSound(posX, posY, posZ, SoundEvents.ENTITY_GENERIC_SWIM, SoundCategory.PLAYERS, 0.3f,
                1.5f, false);
        lastPlayedSplash = 0;
    }


    @Override
    public EntityLivingBase getController() {
        return getOwner();
    }

    @Override
    protected double getVelocityMultiplier() {
        return velocityMultiplier;
    }

    @Override
    public boolean isProjectile() {
        return true;
    }

    @Override
    public boolean shouldDissipate() {
        return true;
    }

    @Override
    public boolean isPiercing() {
        return true;
    }

    @Override
    public float getHealth() {
        return 0;
    }

    @Override
    public void setHealth(float health) {

    }

    @Override
    public float getMaxHealth() {
        return 0;
    }

    @Override
    public void setMaxHealth(float maxHealth) {

    }

    @Override
    protected double getControlPointTeleportDistanceSq() {
        return getControlPointMaxDistanceSq() * getAmountOfControlPoints();
    }

    @Override
    protected double getControlPointMaxDistanceSq() {
        return 0.25F;
    }

    @Override
    public int getAmountOfControlPoints() {
        return 10;
    }

    @Override
    public boolean shouldExplode() {
        return false;
    }

    static class WaterControlPoint extends ControlPoint {

        private WaterControlPoint(EntityWaterCannon arc, float size, double x, double y, double z) {
            super(arc, size, x, y, z);
        }

    }
}
