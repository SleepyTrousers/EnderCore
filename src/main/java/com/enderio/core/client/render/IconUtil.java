package com.enderio.core.client.render;

import java.util.ArrayList;

import com.enderio.core.EnderCore;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class IconUtil {

  public static interface IIconProvider {

    public void registerIcons(TextureMap register);
   
  }

  public static IconUtil instance = new IconUtil();
  
  public static void addIconProvider(IIconProvider registrar) {
    instance.iconProviders.add(registrar);
  }
  
  private ArrayList<IIconProvider> iconProviders = new ArrayList<IIconProvider>();
  
  public TextureAtlasSprite whiteTexture;
  public TextureAtlasSprite blankTexture;
  public TextureAtlasSprite errorTexture;
  
  private boolean doneInit = false;
  
  private IconUtil() {    
  }
  
  public void init() {
    if(doneInit) {
      return;
    }
    doneInit = true;
    MinecraftForge.EVENT_BUS.register(this);
    addIconProvider(new IIconProvider() {

      @Override
      public void registerIcons(TextureMap register) {
        whiteTexture = register.registerSprite(new ResourceLocation(EnderCore.MODID, "white"));
        errorTexture = register.registerSprite(new ResourceLocation(EnderCore.MODID, "error"));
        blankTexture = register.registerSprite(new ResourceLocation(EnderCore.MODID, "blank"));
      }

    });
  }
  
  @SubscribeEvent
  public void onIconLoad(TextureStitchEvent.Pre event) {
    for (IIconProvider reg : iconProviders) {
      reg.registerIcons(event.getMap());      
    }
  }

  public static TextureAtlasSprite getIconForItem(int itemId, int meta) {
    Item item = Item.getItemById(itemId);
    if (item == null) {
      return null;
    }
    return Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(item, meta);    
  }

}
