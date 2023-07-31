package com.mrcrayfish.vehicle.client.particle;

import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class DustParticle extends SimpleAnimatedParticle
{
    public DustParticle(ClientLevel world, SpriteSet spriteSet, double x, double y, double z, double xd, double yd, double zd)
    {
        super(world, x, y, z, spriteSet, (float) yd);
        this.lifetime = 50 + this.random.nextInt(20);
        this.quadSize = 0.3F;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.alpha = 0.45F;
    }

    @Override
    public void tick()
    {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if(this.age++ < this.lifetime)
        {
            this.xd *= 0.98;
            this.yd *= 0.98;
            this.zd *= 0.98;
            this.alpha *= 0.95;
            this.move(this.xd, this.yd, this.zd);
        }
        else
        {
            this.remove();
        }
    }

    @Override
    public @NotNull ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet spriteSet;
        public Factory(SpriteSet spriteSet)
        {
            this.spriteSet = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel world, double x, double y, double z, double xd, double yd, double zd)
        {
            DustParticle particle = new DustParticle(world, spriteSet, x, y, z, xd, yd, zd);
            particle.pickSprite(this.spriteSet);
            return particle;
        }
    }
}
