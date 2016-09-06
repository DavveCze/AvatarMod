package com.crowsofwar.avatar.client.particles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AvatarParticleFlames extends Particle {
	
	private static final ResourceLocation VANILLA_PARTICLES = new ResourceLocation(
			"textures/particle/particles.png");
	private static final ResourceLocation AVATAR_PARTICLES = new ResourceLocation("avatar",
			"textures/particles/flame.png");
	
	public AvatarParticleFlames(int particleID, World world, double x, double y, double z, double velX,
			double velY, double velZ, int... parameters) {
		
		this(world, x, y, z, velX, velY, velZ);
		
	}
	
	protected AvatarParticleFlames(World world, double x, double y, double z, double velX, double velY,
			double velZ) {
		
		super(world, x, y, z, velX, velY, velZ);
		this.particleRed = 1.0F;
		this.particleGreen = 1.0F;
		this.particleBlue = 1.0F;
		this.setParticleTextureIndex(4);
		this.setSize(0.02F, 0.02F);
		this.particleScale *= this.rand.nextFloat() * 0.6F + 0.2F;
		this.motionX = velX * 0.20000000298023224D + (Math.random() * 2.0D - 1.0D) * 0.019999999552965164D;
		this.motionY = velY * 0.20000000298023224D + (Math.random() * 2.0D - 1.0D) * 0.019999999552965164D;
		this.motionZ = velZ * 0.20000000298023224D + (Math.random() * 2.0D - 1.0D) * 0.019999999552965164D;
		this.particleMaxAge = (int) (8.0D / (Math.random() * 0.8D + 0.2D));
		
	}
	
	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.motionY += 0.002D;
		this.moveEntity(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.8500000238418579D;
		this.motionY *= 0.8500000238418579D;
		this.motionZ *= 0.8500000238418579D;
		
		if (this.particleMaxAge-- <= 0) {
			this.setExpired();
		}
	}
	
	@Override
	public void renderParticle(VertexBuffer vb, Entity entity, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		
		Tessellator t = Tessellator.getInstance();
		Minecraft mc = Minecraft.getMinecraft();
		
		t.draw();
		mc.getTextureManager().bindTexture(AVATAR_PARTICLES);
		vb.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
		GlStateManager.blendFunc(SourceFactor.ONE, DestFactor.ONE);
		
		super.renderParticle(vb, entity, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY,
				rotationXZ);
		
		t.draw();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		mc.getTextureManager().bindTexture(VANILLA_PARTICLES);
		vb.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
		
	}
	
}