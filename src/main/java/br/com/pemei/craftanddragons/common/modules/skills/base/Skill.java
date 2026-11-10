package br.com.pemei.craftanddragons.common.modules.skills.base;

import br.com.pemei.craftanddragons.common.modules.skills.capabilities.PlayerSkillsCapability;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public abstract class Skill {
    private final String id;
    private final Component name;
    private final Component description;

    public abstract void apply(LivingEntity entity, PlayerSkillsCapability capability);

    public abstract void remove(LivingEntity entity, PlayerSkillsCapability capability);

    @Override
    public String toString() {
        return this.id;
    }

}
