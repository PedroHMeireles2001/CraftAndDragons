package br.com.pemei.craftanddragons.common.modules.block.extractor;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ItemExtractorTableBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);


    public ItemExtractorTableBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.ENCHANTING_TABLE));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }


    // Aqui você define a "Função Diferente" ao clicar com o botão direito
    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand p_60507_, BlockHitResult p_60508_) {
        // Abertura de GUI deve sempre acontecer exclusivamente no Servidor
        if (!level.isClientSide()) {
            MenuProvider menuProvider = this.getMenuProvider(state, level, pos);

            if (menuProvider != null && player instanceof ServerPlayer serverPlayer) {
                // Abre o menu para o jogador
                serverPlayer.openMenu(menuProvider);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @SuppressWarnings("deprecation")
    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        // SimpleMenuProvider cria a ponte entre o Bloco e a sua classe de Menu
        return new SimpleMenuProvider(
                (containerId, playerInventory, playerEntity) -> {
                    // Aqui você instanciará a sua classe de Menu (ex: MagicMenu)
                    // return new MagicMenu(containerId, playerInventory);
                    return new ItemExtractorMenu(containerId, playerInventory, ContainerLevelAccess.create(level, pos)); // Substitua null pela instância do seu menu quando criá-lo
                },
                Component.translatable("container.craftanddragons.item_sacrifice_table") // Título que aparecerá no topo do Menu
        );
    }


}
