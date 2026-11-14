package br.com.pemei.craftanddragons.common.modules.skills.base;

import br.com.pemei.craftanddragons.common.modules.skills.capabilities.PlayerSkillsCapability;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

@Getter
@EqualsAndHashCode(of = "id")
public abstract class Skill {
    private final String id;
    private final int level;
    private final Component name;
    private final Component description;

    protected Skill(String id, Component name, Component description) {
        this(id, 1, name, description);
    }

    protected Skill(String id, int level, Component name, Component description) {
        if (level < 1) throw new IllegalArgumentException("Skill level must be at least 1");
        this.id = id;
        this.level = level;
        this.name = name;
        this.description = description;
    }

    public abstract void apply(LivingEntity entity, PlayerSkillsCapability capability);

    public abstract void remove(LivingEntity entity, PlayerSkillsCapability capability);

    @Override
    public String toString() {
        return this.id;
    }

}
