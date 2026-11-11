package br.com.pemei.craftanddragons.core.client;

import br.com.pemei.craftanddragons.network.ModMessages;
import br.com.pemei.craftanddragons.network.packet.ActiveSkillsStatePacket;
import br.com.pemei.craftanddragons.network.packet.SetSkillSlotPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;

import static br.com.pemei.craftanddragons.common.modules.skills.capabilities.PlayerSkillsCapability.SKILL_SLOT_COUNT;

public final class SkillSlotsScreen extends Screen {
    private static final String KEY = "screen.craftanddragons.skill_slots.";
    private static final int ROW_HEIGHT = 23;
    private ActiveSkillsStatePacket state;
    private int selectedSlot;
    private int page;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;

    public SkillSlotsScreen(ActiveSkillsStatePacket state) {
        super(Component.translatable(KEY + "title"));
        this.state = state;
    }

    public void updateState(ActiveSkillsStatePacket updated) {
        var previous = state;
        state = updated;
        if (previous == null || !updated.slots().equals(previous.slots())
                || !skillIds(updated).equals(skillIds(previous))) {
            rebuildWidgets();
        }
    }

    private static List<String> skillIds(ActiveSkillsStatePacket packet) {
        return packet.skills().stream().map(ActiveSkillsStatePacket.Entry::id).toList();
    }

    @Override
    protected void init() {
        panelWidth = Math.min(width - 12, 348);
        panelHeight = Math.min(height - 12, 268);
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;

        int slotWidth = Math.min(30, (panelWidth - 20) / SKILL_SLOT_COUNT);
        int slotLeft = (width - slotWidth * SKILL_SLOT_COUNT) / 2;
        for (int slot = 0; slot < SKILL_SLOT_COUNT; slot++) {
            int index = slot;
            var skill = state == null ? null : state.skillInSlot(slot);
            Component name = skill == null ? Component.translatable(KEY + "empty") : skill.name();
            Component label = Component.literal(slot == selectedSlot ? "[" + (slot + 1) + "]" : Integer.toString(slot + 1))
                    .withStyle(style -> style.withColor(skill == null ? 0xCCCCCC : 0xF6CD87));
            addRenderableWidget(Button.builder(label, pressed -> {
                        selectedSlot = index;
                        rebuildWidgets();
                    })
                    .tooltip(Tooltip.create(Component.translatable(KEY + "slot", slot + 1, ActiveSkillControls.slotKey(slot), name)))
                    .bounds(slotLeft + slot * slotWidth, top + 28, slotWidth - 2, 20)
                    .build());
        }

        int footer = top + panelHeight - 27;
        int actionWidth = Math.min(104, (panelWidth - 68) / 2);
        addRenderableWidget(Button.builder(Component.translatable(KEY + "clear"), pressed -> assign(""))
                .bounds(width / 2 - actionWidth - 3, footer, actionWidth, 20).build()).active =
                state != null && state.skillInSlot(selectedSlot) != null;
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, pressed -> onClose())
                .bounds(width / 2 + 3, footer, actionWidth, 20).build());

        if (state == null) return;

        int rows = Math.max(1, (panelHeight - 108) / ROW_HEIGHT);
        int lastPage = Math.max(0, (state.skills().size() - 1) / rows);
        page = Math.min(page, lastPage);
        int first = page * rows;
        int end = Math.min(first + rows, state.skills().size());
        for (int index = first; index < end; index++) {
            var skill = state.skills().get(index);
            Component description = Component.translatable(KEY + "skill", skill.name(), skill.manaCost());
            String text = description.getString();
            if (font.width(text) > panelWidth - 32) {
                text = font.plainSubstrByWidth(text, panelWidth - 42) + "…";
            }
            addRenderableWidget(Button.builder(Component.literal(text), pressed -> assign(skill.id()))
                    .tooltip(Tooltip.create(description))
                    .bounds(left + 12, top + 76 + (index - first) * ROW_HEIGHT, panelWidth - 24, 20)
                    .build()).active = !state.slots().get(selectedSlot).equals(skill.id());
        }

        if (lastPage > 0) {
            addRenderableWidget(Button.builder(Component.literal("<"), pressed -> changePage(-1))
                    .bounds(left + 8, footer, 20, 20).build()).active = page > 0;
            addRenderableWidget(Button.builder(Component.literal(">"), pressed -> changePage(1))
                    .bounds(left + panelWidth - 28, footer, 20, 20).build()).active = page < lastPage;
        }
    }

    private void changePage(int offset) {
        page += offset;
        rebuildWidgets();
    }

    private void assign(String skillId) {
        ModMessages.sendToServer(new SetSkillSlotPacket(selectedSlot, skillId));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.fillGradient(left, top, left + panelWidth, top + panelHeight, 0xFF3B2737, 0xFF1C1722);
        graphics.renderOutline(left, top, panelWidth, panelHeight, 0xFF986844);
        graphics.drawCenteredString(font, title, width / 2, top + 10, 0xE8BC74);

        var skill = state == null ? null : state.skillInSlot(selectedSlot);
        Component name = skill == null ? Component.translatable(KEY + "empty") : skill.name();
        Component selected = Component.translatable(KEY + "slot", selectedSlot + 1, ActiveSkillControls.slotKey(selectedSlot), name);
        graphics.enableScissor(left + 10, top + 54, left + panelWidth - 10, top + 68);
        graphics.drawCenteredString(font, selected, width / 2, top + 55, 0xF6E8D5);
        graphics.disableScissor();
        if (state == null || state.skills().isEmpty()) {
            Component message = Component.translatable(KEY + (state == null ? "loading" : "no_skills"));
            graphics.drawCenteredString(font, message, width / 2, top + 84, 0xCCBCB5);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

}
