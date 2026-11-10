package br.com.pemei.craftanddragons.common.modules.skills.impl;

import br.com.pemei.craftanddragons.common.modules.effect.base.Effect;
import br.com.pemei.craftanddragons.common.modules.effect.base.EffectExecutor;
import br.com.pemei.craftanddragons.common.modules.effect.base.EffectHolder;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectContext;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectTrigger;
import br.com.pemei.craftanddragons.common.modules.skills.base.PlayerEffects;
import br.com.pemei.craftanddragons.common.modules.skills.base.Skill;
import br.com.pemei.craftanddragons.common.modules.skills.capabilities.PlayerSkillsCapability;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Map;

@Getter
public class SkillEffect extends Skill implements EffectHolder {


    private final Map<EffectTrigger, List<EffectExecutor>> effectsExecutor;

    public SkillEffect(String id, Component name, Component description, Map<EffectTrigger, List<EffectExecutor>> effectsExecutor) {
        super(id, name, description);
        this.effectsExecutor = effectsExecutor;
    }

    public SkillEffect(String id, String  name, String description, EffectExecutor effect, EffectTrigger effectTrigger) {
        super(id, Component.literal(name),Component.literal(description));
        this.effectsExecutor = Map.of(effectTrigger,List.of(effect));
    }



    @Override
    public void apply(LivingEntity entity, PlayerSkillsCapability capability) {
        if (this.effectsExecutor == null || this.effectsExecutor.isEmpty()) {
            return;
        }

        for (Map.Entry<EffectTrigger, List<EffectExecutor>> entry : this.effectsExecutor.entrySet()) {
            EffectTrigger trigger = entry.getKey();
            List<EffectExecutor> executors = entry.getValue();

            if (executors != null) {
                for (EffectExecutor executor : executors) {
                    capability.addSkillEffect(new PlayerEffects(trigger, this, executor, true));
                }
            }
        }

        EffectContext context = new EffectContext(entity.level(),this,EffectTrigger.ON_SKILL_APPLY,entity,null,null,null,null,1f);
        this.effectsExecutor.get(EffectTrigger.ON_SKILL_APPLY).forEach(executor -> { executor.apply(context); });

    }

    @Override
    public void remove(LivingEntity entity, PlayerSkillsCapability capability) {
        capability.removeSkillEffect(this.getId());
        this.stopAllTimers(entity);
    }
}
