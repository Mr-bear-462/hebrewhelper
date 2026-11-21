package il.helper;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.players.NameAndId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Glue between Minecraft and {@link Brain}: listens to chat, replies in chat, runs commands.
 * Only the world owner (singleplayer / LAN host) or operators can give the helper orders.
 */
public class HelperMod implements ModInitializer {
    public static final String MOD_ID = "hebrewhelper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private final Brain brain = new Brain();

    @Override
    public void onInitialize() {
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, boundChatType) -> {
            String text = message.signedContent();
            MinecraftServer server = sender.level().getServer();
            if (server == null || text == null) return;
            server.execute(() -> handle(server, sender, text));
        });
        LOGGER.info("Hebrew Helper loaded. In chat, say: עוזר עזרה");
    }

    private void handle(MinecraftServer server, ServerPlayer player, String text) {
        try {
            if (text.length() > 300) text = text.substring(0, 300);
            String uuid = player.getStringUUID();
            if (!brain.addressed(uuid, text, System.currentTimeMillis())) return;

            if (!allowed(server, player)) {
                say(server, "רק בעל העולם או מפעיל (OP) יכולים לתת לי פקודות.");
                return;
            }

            Brain.Info info = new Brain.Info();
            info.health = player.getHealth();
            info.food = player.getFoodData().getFoodLevel();
            info.x = player.blockPosition().getX();
            info.y = player.blockPosition().getY();
            info.z = player.blockPosition().getZ();
            for (ServerPlayer p : server.getPlayerList().getPlayers()) info.players.add(p.getGameProfile().name());

            Brain.Reply reply = brain.handle(uuid, text, info);
            for (String line : reply.say) say(server, line);

            CommandSourceStack source = server.createCommandSourceStack();
            for (Brain.Cmd c : reply.cmds) run(server, source, c);
        } catch (Throwable t) {
            LOGGER.error("Hebrew Helper error", t);
            say(server, "אופס, משהו השתבש אצלי (" + t.getClass().getSimpleName() + "). תנסה שוב.");
        }
    }

    private boolean allowed(MinecraftServer server, ServerPlayer player) {
        try {
            if (server.isSingleplayerOwner(new NameAndId(player.getGameProfile()))) return true;
        } catch (Throwable ignored) {
            // fall through to the operator check
        }
        try {
            return player.getPermissionContext().permissionLevel().isEqualOrHigherThan(PermissionLevel.GAMEMASTERS);
        } catch (Throwable t) {
            return false;
        }
    }

    private void run(MinecraftServer server, CommandSourceStack source, Brain.Cmd c) {
        try {
            server.getCommands().getDispatcher().execute(c.run, source);
            return;
        } catch (CommandSyntaxException | RuntimeException e) {
            LOGGER.warn("Command failed: {} ({})", c.run, e.getMessage());
        }
        if (c.fallback != null) {
            try {
                server.getCommands().getDispatcher().execute(c.fallback, source);
                say(server, "(חלק מהקסמים לא עבדו, אז נתתי את הפריט בלי קסמים.)");
                return;
            } catch (CommandSyntaxException | RuntimeException e) {
                LOGGER.warn("Fallback failed: {} ({})", c.fallback, e.getMessage());
            }
        }
        if (c.failMsg != null) say(server, c.failMsg);
    }

    private void say(MinecraftServer server, String line) {
        Component msg = Component.literal("<עוזר> ").withStyle(ChatFormatting.AQUA)
                .append(Component.literal(line).withStyle(ChatFormatting.WHITE));
        for (ServerPlayer p : server.getPlayerList().getPlayers()) p.sendSystemMessage(msg);
    }
}
