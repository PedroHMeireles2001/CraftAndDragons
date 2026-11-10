package br.com.pemei.craftanddragons.common.modules.block.extractor;

import br.com.pemei.craftanddragons.core.init.ModBlocks;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.Container;

public class ItemExtractorMenu extends AbstractContainerMenu {

    private final Container container;
    private final ContainerLevelAccess access;

    // Construtor chamado pelo Cliente
    public ItemExtractorMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    // Construtor chamado pelo Servidor
    public ItemExtractorMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModBlocks.ITEM_EXTRACTOR_MENU.get(), containerId); // Referência ao seu registro
        this.access = access;

        // Cria o inventário interno do bloco (2 slots)
        this.container = new SimpleContainer(2) {
            @Override
            public void setChanged() {
                super.setChanged();
                ItemExtractorMenu.this.slotsChanged(this); // Ouve qualquer mudança de itens
            }
        };



        // Adiciona Slot 0: Item para sacrificar (Travado para itens encantados)
        this.addSlot(new Slot(this.container, 0, 56, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                // Só aceita se o item tiver o brilho/NBT de encantamento
                return stack.getItem() instanceof Extractable extractable && extractable.canExtract();
            }
        });

        // Adiciona Slot 1: O Livro (Travado para aceitar APENAS livros comuns)
        this.addSlot(new Slot(this.container, 1, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                // Impede que o jogador coloque pedra, espada ou qualquer outra coisa aqui
                return stack.getItem() == Items.BOOK || stack.getItem() == Items.ENCHANTED_BOOK;
            }
        });

        addPlayerInventory(playerInventory);
    }


    // Valida se o jogador não se afastou demais do bloco
    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.ITEM_EXTRACTOR_TABLE.get());
    }

    // Código boilerplate do Minecraft para renderizar o inventário do jogador
    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    // OBRIGATÓRIO: Lida com o Shift+Click. Se você não implementar isso, o jogo crasha ao usar Shift.
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 2) { // Clicou nos slots da mesa, move pro inventário
                if (!this.moveItemStackTo(itemstack1, 2, this.slots.size(), true)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(itemstack1, 0, 2, false)) { // Clicou no inventário, tenta mover pra mesa
                return ItemStack.EMPTY;
            }
            if (itemstack1.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemstack;
    }

    // Ao fechar a GUI, devolve os itens que sobraram na mesa para o jogador
    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> this.clearContainer(player, this.container));
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        // Verifica se é o nosso botão de "Fundir" (ID 0)
        if (id == 0) {
            ItemStack itemSacrificio = this.container.getItem(0);
            ItemStack livro = this.container.getItem(1);

            if (!itemSacrificio.isEmpty() && livro.getItem() == Items.BOOK) {
                this.access.execute((level, pos) -> {

                    if(itemSacrificio.getItem() instanceof Extractable extractable && extractable.canExtract()){
                        ItemStack livroEncantado = new ItemStack(Items.ENCHANTED_BOOK);
                        extractable.getEnchantments().entrySet().stream()
                                .map((entry)->new EnchantmentInstance(entry.getKey(),entry.getValue()))
                                .forEach((e)->EnchantedBookItem.addEnchantment(livroEncantado,e));
                        this.container.setItem(1, livroEncantado);
                        itemSacrificio.shrink(1);

                        level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.1F + 0.9F);

                    }



                });
                return true; // Retorna true para confirmar que a ação foi processada
            }
        }
        return false;
    }
}