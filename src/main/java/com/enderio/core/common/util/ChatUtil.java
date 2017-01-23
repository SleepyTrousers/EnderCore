package com.enderio.core.common.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.Lang;
import com.enderio.core.common.network.EnderPacketHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ChatUtil {

  public static class PacketNoSpamChat implements IMessage {

    ITextComponent[] chatLines;

    public PacketNoSpamChat() {
      chatLines = new ITextComponent[0];
    }

    PacketNoSpamChat(ITextComponent... lines) {
      // this is guaranteed to be >1 length by accessing methods
      this.chatLines = lines;
    }

    @Override
    public void toBytes(ByteBuf buf) {
      buf.writeInt(chatLines.length);
      for (ITextComponent c : chatLines) {
        if (c != null) {
          ByteBufUtils.writeUTF8String(buf, ITextComponent.Serializer.componentToJson(c));
        }
      }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
      chatLines = new ITextComponent[buf.readInt()];
      for (int i = 0; i < chatLines.length; i++) {
        final String readUTF8String = ByteBufUtils.readUTF8String(buf);
        if (readUTF8String == null) {
          chatLines[i] = new TextComponentString("");
        } else {
          chatLines[i] = ITextComponent.Serializer.jsonToComponent(readUTF8String);
        }
      }
    }

    public static class Handler implements IMessageHandler<PacketNoSpamChat, IMessage> {

      @Override
      public IMessage onMessage(PacketNoSpamChat message, MessageContext ctx) {
        sendNoSpamMessages(message.chatLines);
        return null;
      }
    }
  }

  private static final int DELETION_ID = 8675309;
  private static int lastAdded;

  static void sendNoSpamMessages(ITextComponent[] messages) {
    GuiNewChat chat = Minecraft.getMinecraft().ingameGUI.getChatGUI();
    for (int i = DELETION_ID + messages.length - 1; i <= lastAdded; i++) {
      chat.deleteChatLine(i);
    }
    for (int i = 0; i < messages.length; i++) {
      final ITextComponent chatComponent = messages[i];
      if (chatComponent != null) {
        chat.printChatMessageWithOptionalDeletion(chatComponent, DELETION_ID + i);
      }
    }
    lastAdded = DELETION_ID + messages.length - 1;
  }

  /**
   * Returns a standard {@link ITextComponent} for the given {@link String}.
   *
   * @param s
   *          The string to wrap.
   * @return An {@link ITextComponent} containing the string.
   */
  public static @Nonnull ITextComponent wrap(@Nullable String s) {
    return s == null ? new TextComponentString("") : new TextComponentString(s);
  }

  /**
   * @see #wrap(String)
   */
  public static @Nonnull ITextComponent[] wrap(@Nonnull String... s) {
    ITextComponent[] ret = new ITextComponent[s.length];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = wrap(s[i]);
    }
    return ret;
  }

  /**
   * Returns a translatable chat component for the given string and format args.
   *
   * @param s
   *          The string to format
   * @param args
   *          The args to apply to the format
   */
  public static @Nonnull ITextComponent wrapFormatted(@Nonnull String s, @Nonnull Object... args) {
    return new TextComponentTranslation(s, args);
  }

  /**
   * Simply sends the passed lines to the player in a chat message.
   *
   * @param player
   *          The player to send the chat to
   * @param lines
   *          The lines to send
   */
  public static void sendChat(@Nonnull EntityPlayer player, @Nonnull String... lines) {
    sendChat(player, wrap(lines));
  }

  /**
   * Localizes the lines before sending them.
   *
   * @see #sendChat(EntityPlayer, String...)
   */
  public static void sendChatUnloc(@Nonnull EntityPlayer player, @Nonnull Lang lang, @Nonnull String... unlocLines) {
    sendChat(player, lang.localizeAll(lang, unlocLines));
  }

  /**
   * Sends all passed chat components to the player.
   *
   * @param player
   *          The player to send the chat lines to.
   * @param lines
   *          The {@link ITextComponent chat components} to send.
   */
  public static void sendChat(@Nonnull EntityPlayer player, @Nonnull ITextComponent... lines) {
    for (ITextComponent c : lines) {
      if (c != null) {
        player.sendMessage(c);
      }
    }
  }

  /**
   * Localizes the strings before sending them.
   *
   * @see #sendNoSpamClient(String...)
   */
  public static void sendNoSpamClientUnloc(@Nonnull Lang lang, @Nonnull String... unlocLines) {
    sendNoSpamClient(lang.localizeAll(lang, unlocLines));
  }

  /**
   * Same as {@link #sendNoSpamClient(ITextComponent...)}, but wraps the Strings automatically.
   *
   * @param lines
   *          The chat lines to send
   * @see #wrap(String)
   */
  public static void sendNoSpamClient(@Nonnull String... lines) {
    sendNoSpamClient(wrap(lines));
  }

  /**
   * Skips the packet sending, unsafe to call on servers.
   *
   * @see #sendNoSpam(EntityPlayerMP, ITextComponent...)
   */
  public static void sendNoSpamClient(@Nonnull ITextComponent... lines) {
    sendNoSpamMessages(lines);
  }

  /**
   * Localizes the strings before sending them.
   *
   * @see #sendNoSpam(EntityPlayer, String...)
   */
  public static void sendNoSpamUnloc(@Nonnull EntityPlayer player, @Nonnull Lang lang, @Nonnull String... unlocLines) {
    sendNoSpam(player, lang.localizeAll(lang, unlocLines));
  }

  /**
   * @see #wrap(String)
   * @see #sendNoSpam(EntityPlayer, ITextComponent...)
   */
  public static void sendNoSpam(@Nonnull EntityPlayer player, @Nonnull String... lines) {
    sendNoSpam(player, wrap(lines));
  }

  /**
   * First checks if the player is instanceof {@link EntityPlayerMP} before casting.
   *
   * @see #sendNoSpam(EntityPlayerMP, ITextComponent...)
   */
  public static void sendNoSpam(@Nonnull EntityPlayer player, @Nonnull ITextComponent... lines) {
    if (player instanceof EntityPlayerMP) {
      sendNoSpam((EntityPlayerMP) player, lines);
    }
  }

  /**
   * Localizes the strings before sending them.
   *
   * @see #sendNoSpam(EntityPlayerMP, String...)
   */
  public static void sendNoSpamUnloc(@Nonnull EntityPlayerMP player, @Nonnull Lang lang, @Nonnull String... unlocLines) {
    sendNoSpam(player, lang.localizeAll(lang, unlocLines));
  }

  /**
   * @see #wrap(String)
   * @see #sendNoSpam(EntityPlayerMP, ITextComponent...)
   */
  public static void sendNoSpam(@Nonnull EntityPlayerMP player, @Nonnull String... lines) {
    sendNoSpam(player, wrap(lines));
  }

  /**
   * Sends a chat message to the client, deleting past messages also sent via this method.
   * <p>
   * Credit to RWTema for the idea
   *
   * @param player
   *          The player to send the chat message to
   * @param lines
   *          The chat lines to send.
   */
  public static void sendNoSpam(@Nonnull EntityPlayerMP player, @Nonnull ITextComponent... lines) {
    if (lines.length > 0) {
      EnderPacketHandler.INSTANCE.sendTo(new PacketNoSpamChat(lines), player);
    }
  }
}
