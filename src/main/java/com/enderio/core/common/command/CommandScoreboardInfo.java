package com.enderio.core.common.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;

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
    public boolean canCommandSenderUseCommand(ICommandSender p_71519_1_) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processCommand(ICommandSender player, String[] args) {
        if (args.length < 2) {
            throw new WrongUsageException("This command requires 2 args: <board> <name>");
        }

        Scoreboard board = player.getEntityWorld().getScoreboard();

        ScoreObjective obj = board.getObjective(args[0]);

        if (obj == null) {
            player.addChatMessage(new ChatComponentText("No such board " + args[0]));
        }

        Collection<Score> collection = board.func_96534_i(obj);

        for (Score score : collection) {
            if (score.getPlayerName().equals(args[1])) {
                player.addChatMessage(
                        new ChatComponentText(
                                args[1] + "'s score on board \"" + args[0] + "\": " + score.getScorePoints()));
                return;
            }
        }

        player.addChatMessage(new ChatComponentText("No score for " + args[1] + " on board \"" + args[0] + "\""));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List addTabCompletionOptions(ICommandSender player, String[] args) {
        if (args.length == 1) {
            List<String> boards = new ArrayList<String>();
            for (ScoreObjective obj : (Collection<ScoreObjective>) player.getEntityWorld().getScoreboard()
                    .getScoreObjectives()) {
                boards.add(obj.getName());
            }

            return getListOfStringsFromIterableMatchingLastWord(args, boards);
        }

        if (args.length == 2) {
            List<String> players = new ArrayList<String>();
            for (EntityPlayer p : (List<EntityPlayer>) player.getEntityWorld().playerEntities) {
                players.add(p.getCommandSenderName());
            }

            return getListOfStringsFromIterableMatchingLastWord(args, players);
        }

        return null;
    }
}
