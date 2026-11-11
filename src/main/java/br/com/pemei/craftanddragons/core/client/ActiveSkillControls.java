package br.com.pemei.craftanddragons.core.client;

import br.com.pemei.craftanddragons.network.ModMessages;
import br.com.pemei.craftanddragons.network.packet.ActivateSkillPacket;
import br.com.pemei.craftanddragons.network.packet.ActiveSkillsStatePacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.stream.IntStream;

import static br.com.pemei.craftanddragons.Craftanddragons.MODID;
import static br.com.pemei.craftanddragons.common.modules.skills.capabilities.PlayerSkillsCapability.SKILL_SLOT_COUNT;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public final class ActiveSkillControls {
    private static final String HUD_KEY = "hud.craftanddragons.active_skill.";
    private static final KeyMapping OPEN_SKILLS = new KeyMapping("key.craftanddragons.open_skills",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_K),
            "key.categories.craftanddragons");
    private static final List<KeyMapping> KEYS = IntStream.range(0, SKILL_SLOT_COUNT)
            .mapToObj(slot -> new KeyMapping("key.craftanddragons.skill_slot." + (slot + 1),
                    KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM.getOrCreate(defaultKey(slot)),
                    "key.categories.craftanddragons"))
            .toList();
    private static ActiveSkillsStatePacket state;

    private static int defaultKey(int slot) {
        if (slot == 0) return GLFW.GLFW_KEY_R;
        if (slot == SKILL_SLOT_COUNT - 1) return GLFW.GLFW_KEY_KP_0;
        return GLFW.GLFW_KEY_KP_1 + slot;
    }

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_SKILLS);
        KEYS.forEach(event::register);
    }

    public static Component slotKey(int slot) {
        return KEYS.get(slot).getTranslatedKeyMessage();
    }

    public static void setState(ActiveSkillsStatePacket packet) {
        state = packet;
        if (Minecraft.getInstance().screen instanceof SkillSlotsScreen screen) {
            screen.updateState(packet);
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        state = null;
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var client = Minecraft.getInstance();

        while (OPEN_SKILLS.consumeClick()) {
            if (client.screen == null && client.player != null && client.player.isAlive()) {
                client.setScreen(new SkillSlotsScreen(state));
            }
        }

        for (int slot = 0; slot < KEYS.size(); slot++) {
            KeyMapping key = KEYS.get(slot);
            boolean pressed = key.consumeClick();
            while (key.consumeClick()) {
                // Envia no máximo um pedido por habilidade a cada tick.
            }
            if (pressed && client.screen == null && client.player != null && client.player.isAlive()
                    && !client.player.isSpectator() && state != null && state.skillInSlot(slot) != null) {
                ModMessages.sendToServer(new ActivateSkillPacket(slot));
            }
        }
    }

    @SubscribeEvent
    public static void onRender(RenderGuiOverlayEvent.Post event) {
        if (state == null || state.maxMana() <= 0 || !event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) return;
        var client = Minecraft.getInstance();
        if (client.options.hideGui || client.player == null || !client.player.isAlive() || client.player.isSpectator()) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int x = 8;
        int y = 8;
        int width = 100;
        graphics.fill(x, y + 13, x + width, y + 18, 0xDD151D2D);
        graphics.fill(x, y + 13, x + Math.round((float) state.mana() / state.maxMana() * width), y + 18, 0xFF527DDF);
        graphics.drawString(client.font, Component.translatable(HUD_KEY + "mana", state.mana(), state.maxMana()),
                x, y, 0xAFCCFF, true);

        y += 23;
        for (int slot = 0; slot < KEYS.size(); slot++) {
            var entry = state.skillInSlot(slot);
            if (entry == null) continue;

            Component text = entry.secondsRemaining() > 0
                    ? Component.translatable(HUD_KEY + "running", entry.name(), entry.secondsRemaining())
                    : Component.translatable(HUD_KEY + "ready", slotKey(slot), entry.name(), entry.manaCost());
            int color = entry.secondsRemaining() > 0 ? 0xFFC06C : state.mana() >= entry.manaCost() ? 0xFFFFFF : 0xAAAAAA;
            graphics.drawString(client.font, text, x, y, color, true);
            y += 12;
        }
        if (y == 31 && !state.skills().isEmpty()) {
            graphics.drawString(client.font, Component.translatable(HUD_KEY + "unassigned",
                    OPEN_SKILLS.getTranslatedKeyMessage()), x, y, 0xAAAAAA, true);
        }
    }
}
