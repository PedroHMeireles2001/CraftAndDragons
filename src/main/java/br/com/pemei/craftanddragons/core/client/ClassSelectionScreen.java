package br.com.pemei.craftanddragons.core.client;

import br.com.pemei.craftanddragons.network.ModMessages;
import br.com.pemei.craftanddragons.network.packet.OpenClassSelectionPacket.Option;
import br.com.pemei.craftanddragons.network.packet.SelectClassPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

import static br.com.pemei.craftanddragons.Craftanddragons.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public final class ClassSelectionScreen extends Screen {
    private static final String KEY = "screen.craftanddragons.class_selection.";
    private static final int ROW_HEIGHT = 24;
    private static List<Option> pendingOptions;

    private final List<Option> options;
    private int page;

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
        int rows = Math.max(1, (height - 72) / ROW_HEIGHT);
        int lastPage = Math.max(0, (options.size() - 1) / rows);
        page = Math.min(page, lastPage);

        int center = width / 2;
        int buttonWidth = Math.min(260, width - 24);
        int first = page * rows;
        int end = Math.min(first + rows, options.size());
        for (int index = first; index < end; index++) {
            Option option = options.get(index);
            Button button = Button.builder(Component.literal(option.name()), pressed -> confirm(option))
                    .bounds(center - buttonWidth / 2, 40 + (index - first) * ROW_HEIGHT, buttonWidth, 20)
                    .tooltip(Tooltip.create(Component.literal(option.description())))
                    .build();
            addRenderableWidget(button);
        }

        int footer = height - 28;
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, pressed -> onClose())
                .bounds(center - 50, footer, 100, 20).build());
        if (lastPage > 0) {
            addRenderableWidget(Button.builder(Component.literal("<"), pressed -> changePage(-1))
                    .bounds(center - 78, footer, 22, 20).build()).active = page > 0;
            addRenderableWidget(Button.builder(Component.literal(">"), pressed -> changePage(1))
                    .bounds(center + 56, footer, 22, 20).build()).active = page < lastPage;
        }
    }

    private void changePage(int offset) {
        page += offset;
        rebuildWidgets();
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
        graphics.drawCenteredString(font, title, width / 2, 18, 0xFFFFFF);
        if (options.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable(KEY + "empty"), width / 2, 44, 0xFFFFFF);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
