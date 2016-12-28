package com.enderio.core.common.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class CommandScoreboardInfo extends CommandBase {
  @Override
  public @Nonnull String getName() {
    return "scoreboardinfo";
  }

  @Override
  public @Nonnull String getUsage(@Nonnull ICommandSender p_71518_1_) {
    return "/scoreboardinfo <board> <name>";
  }

  @Override
  public int getRequiredPermissionLevel() {
    return 0;
  }

  @Override
  public boolean checkPermission(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender) {
    return true;
  }

  @Override
  public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender player, @Nonnull String[] args) throws CommandException {
  
    if (args.length < 2) {
      throw new WrongUsageException("This command requires 2 args: <board> <name>");
    }

    Scoreboard board = player.getEntityWorld().getScoreboard();

    final String arg0 = args[0];
    if (arg0 == null) {
      player.sendMessage(new TextComponentString("No such board ''"));
      return;
    }

    ScoreObjective obj = board.getObjective(arg0);

    if (obj == null) {
      player.sendMessage(new TextComponentString("No such board " + arg0));
      return;
    }

    Collection<Score> collection = board.getSortedScores(obj);

    for (Score score : collection) {
      if (score.getPlayerName().equals(args[1])) {
        player.sendMessage(new TextComponentString(args[1] + "'s score on board \"" + arg0 + "\": " + score.getScorePoints()));
        return;
      }
    }

    player.sendMessage(new TextComponentString("No score for " + args[1] + " on board \"" + arg0 + "\""));
  }

  
  
  @Override
  public @Nonnull List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args,
      @Nullable BlockPos pos) {
    if (args.length == 1) {
      List<String> boards = new ArrayList<String>();
      for (ScoreObjective obj : sender.getEntityWorld().getScoreboard().getScoreObjectives()) {
        boards.add(obj.getName());
      }

      return getListOfStringsMatchingLastWord(args, boards);
    }

    if (args.length == 2) {
      List<String> players = new ArrayList<String>();
      for (EntityPlayer p : sender.getEntityWorld().playerEntities) {
        players.add(p.getDisplayNameString());
      }

      return getListOfStringsMatchingLastWord(args, players);
    }

    return super.getTabCompletions(server, sender, args, pos);
  }
  
  
}
