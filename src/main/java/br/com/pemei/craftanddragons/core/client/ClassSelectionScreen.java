package br.com.pemei.craftanddragons.core.client;

import br.com.pemei.craftanddragons.network.ModMessages;
import br.com.pemei.craftanddragons.network.packet.OpenClassSelectionPacket.Option;
import br.com.pemei.craftanddragons.network.packet.SelectClassPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static br.com.pemei.craftanddragons.Craftanddragons.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public final class ClassSelectionScreen extends Screen {
    private static final String KEY = "screen.craftanddragons.class_selection.";
    private static final int GOLD = 0xE8BC74;
    private static final int INK = 0xF6E8D5;
    private static final int MUTED = 0xCCBCB5;
    private static final int LINE_HEIGHT = 12;
    private static List<Option> pendingOptions;

    private final List<Option> options;
    private final ItemStack defaultIcon = new ItemStack(Items.KNOWLEDGE_BOOK);
    private final List<Line> lines = new ArrayList<>();
    private int selected;
    private int scroll;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;
    private ResourceLocation portrait;

    private record Line(FormattedCharSequence text, int color) {}

    private ClassSelectionScreen(List<Option> options) {
        super(Component.translatable(KEY + "title"));
        this.options = options;
    }

    public static void queue(List<Option> options) {
        pendingOptions = options;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || pendingOptions == null) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.screen == null) {
            List<Option> options = pendingOptions;
            pendingOptions = null;
            client.setScreen(new ClassSelectionScreen(options));
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        pendingOptions = null;
    }

    @Override
    protected void init() {
        panelWidth = Math.min(360, width - 16);
        panelHeight = Math.min(270, height - 16);
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;

        int footer = top + panelHeight - 29;
        int actionWidth = Math.min(104, (panelWidth - 68) / 2);
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, pressed -> onClose())
                .bounds(width / 2 - actionWidth - 3, footer, actionWidth, 20).build());
        addRenderableWidget(Button.builder(Component.translatable(KEY + "choose"), pressed -> confirm(options.get(selected)))
                .bounds(width / 2 + 3, footer, actionWidth, 20).build()).active = !options.isEmpty();
        if (options.size() > 1) {
            addRenderableWidget(Button.builder(Component.literal("<"), pressed -> changeClass(-1))
                    .bounds(left + 9, footer, 20, 20).build()).active = selected > 0;
            addRenderableWidget(Button.builder(Component.literal(">"), pressed -> changeClass(1))
                    .bounds(left + panelWidth - 29, footer, 20, 20).build()).active = selected < options.size() - 1;
        }
        rebuildDetails();
    }

    private void changeClass(int offset) {
        selected += offset;
        scroll = 0;
        rebuildWidgets();
    }

    private void rebuildDetails() {
        lines.clear();
        if (options.isEmpty()) return;

        Option option = options.get(selected);
        portrait = option.iconPath().isBlank() ? null : ResourceLocation.tryParse(option.iconPath());
        if (portrait != null && minecraft.getResourceManager().getResource(portrait).isEmpty()) {
            portrait = null;
        }

        addLines(option.description(), INK);
        blankLine();
        addLines(Component.translatable(KEY + "skills"), GOLD);
        if (option.levels().isEmpty()) {
            addLines(Component.translatable(KEY + "no_skills"), MUTED);
        }
        for (var level : option.levels()) {
            blankLine();
            addLines(Component.translatable(KEY + "level", level.number()), GOLD);
            for (var skill : level.skills()) {
                addLines(skill.name(), INK);
                addLines(skill.description(), MUTED);
            }
        }
        scroll = Mth.clamp(scroll, 0, maxScroll());
    }

    private void addLines(Component text, int color) {
        for (FormattedCharSequence line : font.split(text, panelWidth - 36)) {
            lines.add(new Line(line, color));
        }
    }

    private void blankLine() {
        lines.add(new Line(FormattedCharSequence.EMPTY, INK));
    }

    private int detailsTop() {
        return top + 87;
    }

    private int detailsBottom() {
        return top + panelHeight - 36;
    }

    private int maxScroll() {
        return Math.max(0, lines.size() * LINE_HEIGHT - (detailsBottom() - detailsTop()));
    }

    private boolean scrollDetails(int offset) {
        int next = Mth.clamp(scroll + offset, 0, maxScroll());
        if (next == scroll) return false;
        scroll = next;
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX >= left && mouseX < left + panelWidth && mouseY >= detailsTop() && mouseY < detailsBottom()
                && scrollDetails((int) Math.round(-delta * 3 * LINE_HEIGHT))) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_PAGE_UP && scrollDetails(-Math.max(LINE_HEIGHT, detailsBottom() - detailsTop()))) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_PAGE_DOWN && scrollDetails(Math.max(LINE_HEIGHT, detailsBottom() - detailsTop()))) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void confirm(Option option) {
        minecraft.setScreen(new ConfirmScreen(confirmed -> {
            if (confirmed) {
                ModMessages.sendToServer(new SelectClassPacket(option.id()));
                onClose();
            } else {
                minecraft.setScreen(this);
            }
        }, Component.translatable(KEY + "confirm", option.name()),
                Component.translatable(KEY + "description", option.description())));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int right = left + panelWidth;
        int bottom = top + panelHeight;
        graphics.fill(left - 2, top - 2, right + 2, bottom + 2, 0xFF100D16);
        graphics.fillGradient(left, top, right, bottom, 0xFF3B2737, 0xFF1C1722);
        graphics.renderOutline(left, top, panelWidth, panelHeight, 0xFF986844);
        graphics.renderOutline(left + 3, top + 3, panelWidth - 6, panelHeight - 6, 0xFF654453);
        graphics.drawCenteredString(font, title, width / 2, top + 10, GOLD);

        if (options.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable(KEY + "empty"), width / 2, top + 45, INK);
        } else {
            renderClass(graphics, right);
            renderDetails(graphics, right);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderClass(GuiGraphics graphics, int right) {
        int x = left + 13;
        int y = top + 23;
        int size = 52;
        graphics.fill(x - 2, y - 2, x + size + 2, y + size + 2, 0xFFAE7B4D);
        graphics.fillGradient(x, y, x + size, y + size, 0xFF4A2B3B, 0xFF211C2D);
        if (portrait != null) {
            graphics.blit(portrait, x, y, size, size, 0, 0, size, size, size, size);
        } else {
            graphics.pose().pushPose();
            graphics.pose().translate(x + 10, y + 10, 0);
            graphics.pose().scale(2, 2, 1);
            graphics.renderItem(defaultIcon, 0, 0);
            graphics.pose().popPose();
        }

        int textX = x + size + 12;
        graphics.enableScissor(textX, y, right - 10, y + 35);
        int nameY = y + 4;
        for (var line : font.split(options.get(selected).name(), right - textX - 12)) {
            graphics.drawString(font, line, textX, nameY, INK, true);
            nameY += LINE_HEIGHT;
        }
        graphics.disableScissor();
        if (options.size() > 1) {
            graphics.drawString(font, Component.translatable(KEY + "position", selected + 1, options.size()),
                    textX, y + 37, MUTED);
        }
    }

    private void renderDetails(GuiGraphics graphics, int right) {
        int start = detailsTop();
        int end = detailsBottom();
        graphics.fill(left + 9, start - 5, right - 9, end + 2, 0xFF241A28);
        graphics.hLine(left + 10, right - 11, start - 5, 0xFF755243);
        graphics.enableScissor(left + 12, start, right - 18, end);
        for (int index = scroll / LINE_HEIGHT, y = start - scroll % LINE_HEIGHT;
             index < lines.size() && y < end; index++, y += LINE_HEIGHT) {
            Line line = lines.get(index);
            graphics.drawString(font, line.text(), left + 15, y, line.color(), false);
        }
        graphics.disableScissor();

        if (maxScroll() > 0) {
            int trackHeight = end - start;
            int thumbHeight = Math.min(trackHeight, Math.max(8, trackHeight * trackHeight / (lines.size() * LINE_HEIGHT)));
            int thumb = start + (trackHeight - thumbHeight) * scroll / maxScroll();
            graphics.fill(right - 15, start, right - 12, end, 0xFF48313C);
            graphics.fill(right - 15, thumb, right - 12, thumb + thumbHeight, 0xFFBF8C57);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
