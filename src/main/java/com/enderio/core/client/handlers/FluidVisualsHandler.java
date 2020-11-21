package com.enderio.core.client.handlers;

import javax.annotation.Nonnull;

import com.enderio.core.EnderCore;
import com.enderio.core.common.fluid.EnderFluidBlock;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class FluidVisualsHandler {
  @SubscribeEvent
  public static void onFOVModifier(@Nonnull EntityViewRenderEvent.FOVModifier event) {
    if (event.getInfo().getBlockAtCamera().getBlock() instanceof EnderFluidBlock) {
      event.setFOV(event.getFOV() * 60.0F / 70.0F);
    }
  }

  private static final @Nonnull ResourceLocation RES_UNDERFLUID_OVERLAY = new ResourceLocation(EnderCore.DOMAIN, "textures/misc/underfluid.png");

  @SubscribeEvent
  public static void onRenderBlockOverlay(RenderBlockOverlayEvent event) {
    if (event.getOverlayType() == OverlayType.WATER) {
      final PlayerEntity player = event.getPlayer();
      // the event has the wrong BlockPos (entity center instead of eyes)
      final BlockPos blockpos = new BlockPos(player.getPosX(), player.getPosY() + player.getEyeHeight(), player.getPosZ());
      final Block block = player.world.getBlockState(blockpos).getBlock();

      if (block instanceof EnderFluidBlock) {
        float fogColorRed = ((EnderFluidBlock) block).getFogColorRed();
        float fogColorGreen = ((EnderFluidBlock) block).getFogColorGreen();
        float fogColorBlue = ((EnderFluidBlock) block).getFogColorBlue();

        Minecraft.getInstance().getTextureManager().bindTexture(RES_UNDERFLUID_OVERLAY);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        float f = player.getBrightness();
        RenderSystem.color4f(f * fogColorRed, f * fogColorGreen, f * fogColorBlue, 0.5F);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.pushMatrix();
        float f7 = -player.rotationYaw / 64.0F;
        float f8 = player.rotationPitch / 64.0F;
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.pos(-1.0D, -1.0D, -0.5D).tex(4.0F + f7, 4.0F + f8).endVertex();
        vertexbuffer.pos(1.0D, -1.0D, -0.5D).tex(0.0F + f7, 4.0F + f8).endVertex();
        vertexbuffer.pos(1.0D, 1.0D, -0.5D).tex(0.0F + f7, 0.0F + f8).endVertex();
        vertexbuffer.pos(-1.0D, 1.0D, -0.5D).tex(4.0F + f7, 0.0F + f8).endVertex();
        tessellator.draw();
        RenderSystem.popMatrix();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

        event.setCanceled(true);
      }
    }
  }

  @SubscribeEvent
  public static void onFogDensity(@Nonnull EntityViewRenderEvent.FogDensity event) throws IllegalArgumentException {
    BlockState blockState = event.getInfo().getBlockAtCamera();
    if (blockState.getBlock() instanceof EnderFluidBlock) {
      final GameRenderer renderer = event.getRenderer();
      final Entity entity = event.getInfo().getRenderViewEntity();
      final boolean cloudFog = event.getType() == FogRenderer.FogType.FOG_SKY;

      // again the event is fired at a bad location...
      if (entity instanceof LivingEntity && ((LivingEntity) entity).isPotionActive(Effects.BLINDNESS)) {
        return;
      } else if (cloudFog) {
        return;
      }

      RenderSystem.fogMode(GlStateManager.FogMode.EXP);

      if (entity instanceof LivingEntity) {
        if (((LivingEntity) entity).isPotionActive(Effects.WATER_BREATHING)) {
          event.setDensity(0.01F);
        } else {
          event.setDensity(0.1F - EnchantmentHelper.getRespirationModifier((LivingEntity) entity) * 0.03F);
        }
      } else {
        event.setDensity(0.1F);
      }
    }
  }

  @SubscribeEvent
  public static void onFogColor(EntityViewRenderEvent.FogColors event) throws IllegalArgumentException {
    BlockState blockState = event.getInfo().getBlockAtCamera();
    if (blockState.getBlock() instanceof EnderFluidBlock) {

      float fogColorRed = ((EnderFluidBlock) blockState.getBlock()).getFogColorRed();
      float fogColorGreen = ((EnderFluidBlock) blockState.getBlock()).getFogColorGreen();
      float fogColorBlue = ((EnderFluidBlock) blockState.getBlock()).getFogColorBlue();

      // Fields for this hateful mess
      ActiveRenderInfo activeRenderInfoIn = event.getInfo();
      ClientWorld worldIn = ((ClientWorld) event.getInfo().getRenderViewEntity().getEntityWorld());
      FluidState fluidstate = event.getInfo().getFluidState();

      // Copied with hate
      double d0 = activeRenderInfoIn.getProjectedView().y * worldIn.getWorldInfo().getFogDistance();
      if (activeRenderInfoIn.getRenderViewEntity() instanceof LivingEntity && ((LivingEntity)activeRenderInfoIn.getRenderViewEntity()).isPotionActive(Effects.BLINDNESS)) {
        int i2 = ((LivingEntity)activeRenderInfoIn.getRenderViewEntity()).getActivePotionEffect(Effects.BLINDNESS).getDuration();
        if (i2 < 20) {
          d0 *= (double)(1.0F - (float)i2 / 20.0F);
        } else {
          d0 = 0.0D;
        }
      }

      if (d0 < 1.0D && !fluidstate.isTagged(FluidTags.LAVA)) {
        if (d0 < 0.0D) {
          d0 = 0.0D;
        }

        d0 = d0 * d0;
        fogColorRed = (float)((double)fogColorRed * d0);
        fogColorGreen = (float)((double)fogColorGreen * d0);
        fogColorBlue = (float)((double)fogColorBlue * d0);
      }

      GameRenderer gameRendererIn = event.getRenderer();
      float bossColorModifier = gameRendererIn.getBossColorModifier((float) event.getRenderPartialTicks());
      if (bossColorModifier > 0.0F) {
        fogColorRed = fogColorRed * (1.0F - bossColorModifier) + fogColorRed * 0.7F * bossColorModifier;
        fogColorGreen = fogColorGreen * (1.0F - bossColorModifier) + fogColorGreen * 0.6F * bossColorModifier;
        fogColorBlue = fogColorBlue * (1.0F - bossColorModifier) + fogColorBlue * 0.6F * bossColorModifier;
      }

      if (fluidstate.isTagged(FluidTags.WATER)) {
        float f6 = 0.0F;
        if (activeRenderInfoIn.getRenderViewEntity() instanceof ClientPlayerEntity) {
          ClientPlayerEntity clientplayerentity = (ClientPlayerEntity)activeRenderInfoIn.getRenderViewEntity();
          f6 = clientplayerentity.getWaterBrightness();
        }

        float f9 = Math.min(1.0F / fogColorRed, Math.min(1.0F / fogColorGreen, 1.0F / fogColorBlue));
        // Forge: fix MC-4647 and MC-10480
        if (Float.isInfinite(f9)) f9 = Math.nextAfter(f9, 0.0);
        fogColorRed = fogColorRed * (1.0F - f6) + fogColorRed * f9 * f6;
        fogColorGreen = fogColorGreen * (1.0F - f6) + fogColorGreen * f9 * f6;
        fogColorBlue = fogColorBlue * (1.0F - f6) + fogColorBlue * f9 * f6;
      } else if (activeRenderInfoIn.getRenderViewEntity() instanceof LivingEntity && ((LivingEntity)activeRenderInfoIn.getRenderViewEntity()).isPotionActive(Effects.NIGHT_VISION)) {
        float f7 = GameRenderer.getNightVisionBrightness((LivingEntity)activeRenderInfoIn.getRenderViewEntity(), (float) event.getRenderPartialTicks());
        float f10 = Math.min(1.0F / fogColorRed, Math.min(1.0F / fogColorGreen, 1.0F / fogColorBlue));
        // Forge: fix MC-4647 and MC-10480
        if (Float.isInfinite(f10)) f10 = Math.nextAfter(f10, 0.0);
        fogColorRed = fogColorRed * (1.0F - f7) + fogColorRed * f10 * f7;
        fogColorGreen = fogColorGreen * (1.0F - f7) + fogColorGreen * f10 * f7;
        fogColorBlue = fogColorBlue * (1.0F - f7) + fogColorBlue * f10 * f7;
      }

      event.setRed(fogColorRed);
      event.setGreen(fogColorGreen);
      event.setBlue(fogColorBlue);
    }
  }
}
