package br.com.pemei.craftanddragons.common.modules.block.extractor;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ItemExtractorScreen extends AbstractContainerScreen<ItemExtractorMenu> {

    // Caminho da textura PNG do seu menu (tamanho padrão vanilla: 176x166)
    private static final ResourceLocation TEXTURE = ResourceLocation.tryParse("craftanddragons:textures/gui/item_sacrifice_table.png");

    public ItemExtractorScreen(ItemExtractorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();

        // Coordenadas na tela (leftPos e topPos representam a ponta superior esquerda do menu)
        int x = this.leftPos + 75; // Fica entre os dois slots (Slot 0 no 56, Slot 1 no 116)
        int y = this.topPos + 32;

        // Cria o botão com a palavra "Fundir" usando o Button.builder (padrão 1.20+)
        this.addRenderableWidget(Button.builder(Component.literal("Fundir"), button -> {

            // Quando clicado, envia a requisição para o servidor (ID = 0)
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);

        }).bounds(x, y, 50, 20).build()); // X, Y, Largura, Altura
    }

    // Desenha o fundo da interface
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    // Responsável por desenhar as dicas (tooltips) e escurecer o fundo do jogo
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}