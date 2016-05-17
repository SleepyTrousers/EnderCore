package com.enderio.core.common.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
  public String getCommandName() {
    return "scoreboardinfo";
  }

  @Override
  public String getCommandUsage(ICommandSender p_71518_1_) {
    return "/scoreboardinfo <board> <name>";
  }

  @Override
  public int getRequiredPermissionLevel() {
    return 0;
  }

  @Override
  public boolean checkPermission(MinecraftServer server, ICommandSender sender) { 
    return true;
  }

  @Override
  public void execute(MinecraftServer server, ICommandSender player, String[] args) throws CommandException {
  
    if (args.length < 2) {
      throw new WrongUsageException("This command requires 2 args: <board> <name>");
    }

    Scoreboard board = player.getEntityWorld().getScoreboard();

    ScoreObjective obj = board.getObjective(args[0]);

    if (obj == null) {
      player.addChatMessage(new TextComponentString("No such board " + args[0]));
    }

    Collection<Score> collection = board.getSortedScores(obj);

    for (Score score : collection) {
      if (score.getPlayerName().equals(args[1])) {
        player.addChatMessage(new TextComponentString(args[1] + "'s score on board \"" + args[0] + "\": " + score.getScorePoints()));
        return;
      }
    }

    player.addChatMessage(new TextComponentString("No score for " + args[1] + " on board \"" + args[0] + "\""));
  }

  
  
  @Override
  public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {  
    if (args.length == 1) {
      List<String> boards = new ArrayList<String>();
      for (ScoreObjective obj : (Collection<ScoreObjective>) sender.getEntityWorld().getScoreboard().getScoreObjectives()) {
        boards.add(obj.getName());
      }

      return getListOfStringsMatchingLastWord(args, boards);
    }

    if (args.length == 2) {
      List<String> players = new ArrayList<String>();
      for (EntityPlayer p : (List<EntityPlayer>) sender.getEntityWorld().playerEntities) {
        players.add(p.getDisplayNameString());
      }

      return getListOfStringsMatchingLastWord(args, players);
    }

    return null;
  }
  
  
}
