package com.enderio.core.client.gui.serverlist;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.enderio.core.client.handlers.ClientHandler;
import com.enderio.core.client.render.RenderUtil;
import com.enderio.core.common.Handlers.Handler;
import com.enderio.core.common.Handlers.Handler.HandlerSide;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import gnu.trove.map.hash.TObjectLongHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ServerListEntryNormal;
import net.minecraft.client.gui.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureClock;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.init.Items;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("unchecked")
@Handler(side = HandlerSide.COMMON)
public class ServerListHandler {

  //TODO:1.8
  
//  private static class TextureClockCustom extends TextureClock {
//    private static final Field field_94239_h = ReflectionHelper.findField(TextureClock.class, "field_94239_h");
//    private static final Field field_94240_i = ReflectionHelper.findField(TextureClock.class, "field_94240_i");
//    private static final Field _framesTextureData = ReflectionHelper.findField(TextureAtlasSprite.class, "field_110976_a", "framesTextureData");
//    
//    private static final WorldProvider provider = DimensionManager.createProviderFor(0);
//    
//    private String serverIP;
//
//    public TextureClockCustom(String serverIP) throws IllegalArgumentException, IllegalAccessException {
//      super("clock-custom");
//      this.serverIP = serverIP;
//      this.framesTextureData = (List<?>) _framesTextureData.get(Items.clock.getIconFromDamage(0));
//    }
//
//    @Override
//    public void updateAnimation() {
//      if (!this.framesTextureData.isEmpty()) {
//        try {
//          float f = provider.calculateCelestialAngle(timeCache.get(serverIP), 1.0F);
//          double angle = (double) f;
//
//          if (!provider.isSurfaceWorld()) {
//            angle = Math.random();
//          }
//
//          double d1;
//
//          for (d1 = angle - field_94239_h.getDouble(this); d1 < -0.5D; ++d1) {
//            ;
//          }
//
//          while (d1 >= 0.5D) {
//            --d1;
//          }
//
//          if (d1 < -1.0D) {
//            d1 = -1.0D;
//          }
//
//          if (d1 > 1.0D) {
//            d1 = 1.0D;
//          }
//
//          field_94240_i.setDouble(this, field_94240_i.getDouble(this) + d1 * 0.1D);
//          field_94240_i.setDouble(this, field_94240_i.getDouble(this) * 0.8D);
//          field_94239_h.setDouble(this, field_94239_h.getDouble(this) + field_94240_i.getDouble(this));
//          int i;
//
//          for (i = (int) ((field_94239_h.getDouble(this) + 1.0D) * (double) this.framesTextureData.size()) % this.framesTextureData.size(); i < 0; i = (i + this.framesTextureData
//              .size()) % this.framesTextureData.size()) {
//            ;
//          }
//
//          if (i != this.frameCounter) {
//            this.frameCounter = i;
//            TextureUtil.uploadTextureMipmap((int[][]) this.framesTextureData.get(this.frameCounter), this.width, this.height, this.originX, this.originY,
//                false, false);
//            // For some reason this can spam a GL error, it is harmless from what I can see, so we'll just throw it away
//            GL11.glGetError();
//          }
//        } catch (Exception e) {
//          Throwables.propagate(e);
//        }
//      }
//    }
//  }
//
//  static {
//    EnumConnectionState.STATUS.func_150753_a().put(42, PacketServerTime.class);
//    EnumConnectionState.STATUS.func_150755_b().put(42, PacketServerTime.class);
//    try {
//      ((Map<Class<?>, EnumConnectionState>) ReflectionHelper.findField(EnumConnectionState.class, "field_150761_f", "STATES_BY_CLASS").get(null)).put(
//          PacketServerTime.class, EnumConnectionState.STATUS);
//    } catch (Exception e) {
//      Throwables.propagate(e);
//    }
//  }
//
//  private static Field _serverList;
//  private static Method _getSize;
//  private static Method _drawHoveringText;
//
//  private static TimeServerPinger pinger;
//  private static Set<String> capturedIps = Sets.newHashSet();
//
//  @SideOnly(Side.CLIENT)
//  private static GuiMultiplayer openGui;
//  @SideOnly(Side.CLIENT)
//  private static ServerSelectionList serverList;
//
//  static TObjectLongHashMap<String> timeCache = new TObjectLongHashMap<String>();
//  
//  private static Map<String, TextureClockCustom> textures = Maps.newHashMap();
//
//  @SubscribeEvent
//  @SideOnly(Side.CLIENT)
//  public void onDrawGui(DrawScreenEvent.Post event) {
//    if (openGui != null) {
//      if (serverList != null) {
//        int x = serverList.left + (serverList.width / 2) + (serverList.getListWidth() / 2) - 32;
//        int y = serverList.top + 13 - serverList.getAmountScrolled();
////        System.out.println(timeCache);
//        try {
//          int size = (Integer) _getSize.invoke(serverList);
//          for (int i = 0; i < size; i++) {
//            IGuiListEntry entry = serverList.getListEntry(i);
//            if (entry instanceof ServerListEntryNormal) {
//              ServerData data = ((ServerListEntryNormal) entry).func_148296_a();
//              RenderUtil.bindItemTexture();
//              TextureAtlasSprite sprite = textures.get(data.serverIP);
//
//              if (sprite != null) {
//                Tessellator tess = Tessellator.instance;
//                
//                GL11.glPushMatrix();
//                GL11.glColor4f(1, 1, 1, 1);
//                GL11.glTranslated(0, 0, -500);
//                GL11.glEnable(GL11.GL_DEPTH_TEST);
//                GL11.glDisable(GL11.GL_LIGHTING);
//                GL11.glDepthMask(true);
//                tess.startDrawingQuads();
//                tess.addVertexWithUV(x, y, 0, sprite.getMinU(), sprite.getMinV());
//                tess.addVertexWithUV(x, y + 16, 0, sprite.getMinU(), sprite.getMaxV());
//                tess.addVertexWithUV(x + 16, y + 16, 0, sprite.getMaxU(), sprite.getMaxV());
//                tess.addVertexWithUV(x + 16, y, 0, sprite.getMaxU(), sprite.getMinV());
//                tess.draw();
//                GL11.glPopMatrix();
//                
//                if (event.mouseX >= x && event.mouseX <= x + 16 && event.mouseY >= y && event.mouseY <= y + 16) {
//                  long time = timeCache.get(data.serverIP);
//                  long hour = (time / 1000 + 6) % 24;
//                  long minute = (time % 1000) * 60 / 1000;
//                  String ampm = "AM";
//                  if (hour >= 12) {
//                      hour -= 12;
//                      ampm = "PM";
//                  }
//                  if (hour == 0) {
//                      hour += 12;
//                  }
//                  String tt = String.format(Locale.ENGLISH, "%02d:%02d %s", hour, minute, ampm);
//                  _drawHoveringText.invoke(event.gui, Lists.newArrayList(tt), event.mouseX, event.mouseY, Minecraft.getMinecraft().fontRenderer);
//                }
//              }
//
//              y += serverList.getSlotHeight();
//            }
//          }
//        } catch (Exception e) {
//          Throwables.propagate(e);
//        }
//      }
//      if (ClientHandler.getTicksElapsed() % 100 == 0) {
//        openConnections(openGui);
//      }
//    }
//  }
//
//  @SubscribeEvent
//  @SideOnly(Side.CLIENT)
//  public void onGuiOpen(InitGuiEvent.Post event) {
//    if (_serverList == null) {
//      _serverList = ReflectionHelper.findField(GuiMultiplayer.class, "field_146803_h", "serverListSelector");
//      _getSize = ReflectionHelper.findMethod(ServerSelectionList.class, null, new String[] { "func_148127_b", "getSize" });
//      _drawHoveringText = ReflectionHelper.findMethod(GuiScreen.class, null, new String[] { "func_146283_a", "drawHoveringText" }, List.class, int.class, int.class, FontRenderer.class);
//    }
//    if (pinger != null) {
//      pinger.close();
//    }
//    if (event.gui instanceof GuiMultiplayer) {
//      openGui = (GuiMultiplayer) event.gui;
//      pinger = new TimeServerPinger();
//      capturedIps.clear();
//      openConnections(openGui);
//    }
//  }
//
//  private void openConnections(GuiMultiplayer gui) {
//    try {
//      serverList = (ServerSelectionList) _serverList.get(gui);
//      int size = (Integer) _getSize.invoke(serverList);
//      for (int i = 0; i < size; i++) {
//        IGuiListEntry entry = serverList.getListEntry(i);
//        if (entry instanceof ServerListEntryNormal) {
//          ServerData data = ((ServerListEntryNormal) entry).func_148296_a();
//          if (!capturedIps.contains(data.serverIP)) {
//            capturedIps.add(data.serverIP);
//            pinger.addServer(data);
//            if (!textures.containsKey(data.serverIP)) {
//              TextureClockCustom sprite = new TextureClockCustom(data.serverIP);
//              sprite.copyFrom((TextureAtlasSprite) Items.clock.getIconFromDamage(0));
//              textures.put(data.serverIP, sprite);
//            }
//          }
//        }
//      }
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//  }
//
//  @SubscribeEvent
//  @SideOnly(Side.CLIENT)
//  public void onClientTick(ClientTickEvent event) {
//    if (Minecraft.getMinecraft().currentScreen != openGui) {
//      openGui = null;
//    } else if (openGui != null) {
//      pinger.update(ClientHandler.getTicksElapsed() % 20 == 0);
//      for (TextureClockCustom tex : textures.values()) {
//        tex.updateAnimation();
//      }
//    }
//
//    if (openGui == null && pinger != null) {
//      pinger.close();
//      pinger = null;
//    }
//  }
}
