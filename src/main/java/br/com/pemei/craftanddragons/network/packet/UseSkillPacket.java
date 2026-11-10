package br.com.pemei.craftanddragons.network.packet;

import br.com.pemei.craftanddragons.common.modules.effect.base.EffectHolder;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectContext;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectTrigger;
import br.com.pemei.craftanddragons.common.modules.skills.ModSkills;
import br.com.pemei.craftanddragons.common.modules.skills.base.Skill;
import br.com.pemei.craftanddragons.common.modules.skills.capabilities.PlayerSkillsCapabilityProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UseSkillPacket {
    // Em vez do objeto, mandamos as propriedades primitivas
    private final String skillId;
    private final String triggerName;
    private final int targetEntityId; // Usamos o ID numérico da entidade
    private final float powerMultiplier;

    public UseSkillPacket(String skillId, EffectTrigger trigger, int targetEntityId, float powerMultiplier) {
        this.skillId = skillId;
        this.triggerName = trigger.name(); // Transforma o Enum em String
        this.targetEntityId = targetEntityId;
        this.powerMultiplier = powerMultiplier;
    }

    // Usado internamente para remontar ao decodificar
    private UseSkillPacket(String skillId, String triggerName, int targetEntityId, float powerMultiplier) {
        this.skillId = skillId;
        this.triggerName = triggerName;
        this.targetEntityId = targetEntityId;
        this.powerMultiplier = powerMultiplier;
    }

    // 1. Escreve os dados no Buffer (Client -> Servidor)
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.skillId);
        buffer.writeUtf(this.triggerName);
        buffer.writeInt(this.targetEntityId);
        buffer.writeFloat(this.powerMultiplier);
    }

    // 2. Lê os dados do Buffer (Servidor recebendo)
    public static UseSkillPacket decode(FriendlyByteBuf buffer) {
        return new UseSkillPacket(
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readInt(),
                buffer.readFloat()
        );
    }

    // 3. Executa a Lógica (Rodando 100% no Servidor)
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer caster = context.getSender();
            if (caster == null) return;

            ServerLevel level = caster.serverLevel();
            EffectTrigger trigger = EffectTrigger.valueOf(this.triggerName);
            Skill holder = ModSkills.REGISTRY.get(this.skillId);

            if (holder == null) return; // Skill não encontrada

            // Tenta achar a entidade alvo pelo ID numérico
            LivingEntity target = null;
            if (this.targetEntityId != -1) {
                Entity foundEntity = level.getEntity(this.targetEntityId);
                if (foundEntity instanceof LivingEntity) {
                    target = (LivingEntity) foundEntity;
                }
            }

            // RECONSTRÓI O EFFECT CONTEXT COM SEGURANÇA NO SERVIDOR!
            EffectContext effectContext = new EffectContext(
                    level,
                    (EffectHolder) holder,
                    trigger,
                    caster,
                    target != null ? target : caster, // Se não tiver alvo, o alvo é o próprio caster
                    null, // BlockPos (mande no pacote se precisar de mira em blocos)
                    null, // BlockState
                    null, // Item
                    this.powerMultiplier
            );

            // Agora aplica a skill pela Capability
            caster.getCapability(PlayerSkillsCapabilityProvider.PLAYER_SKILLS).ifPresent(cap -> {
                if (cap.existsSkill(this.skillId)) {
                    cap.applySkillsEffects(trigger, caster, effectContext.getTargetEntity().orElse(caster), effectContext.powerMultiplier());
                }
            });
        });

        context.setPacketHandled(true);
    }
}