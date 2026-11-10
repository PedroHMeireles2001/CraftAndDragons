package br.com.pemei.craftanddragons.common.modules.skills.capabilities;

import br.com.pemei.craftanddragons.common.modules.effect.base.EffectHolder;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectContext;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectTrigger;
import br.com.pemei.craftanddragons.common.modules.skills.ModSkills;
import br.com.pemei.craftanddragons.common.modules.skills.base.PlayerEffects;
import br.com.pemei.craftanddragons.common.modules.skills.base.Skill;
import br.com.pemei.craftanddragons.network.ModMessages;
import br.com.pemei.craftanddragons.network.packet.UseSkillPacket;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;
import org.slf4j.Logger;

import java.util.*;

@AutoRegisterCapability
public class PlayerSkillsCapability implements INBTSerializable<CompoundTag> {
    private final LivingEntity entity;
    private static final String SKILLS_KEY = "skills";
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Set<Skill> skills = new HashSet<>();
    private final List<PlayerEffects> activeEffects = new ArrayList<>();

    public PlayerSkillsCapability(LivingEntity entity) {
        this.entity = entity;
    }

    public void addSkill(Skill skill) {
        skills.add(skill);
        skill.apply(entity,this);
    }

    public boolean existsSkill(Skill skill) {
        return this.existsSkill(skill.getId());
    }
    public boolean existsSkill(String skillId) {
        Skill skill = ModSkills.REGISTRY.get(skillId);
        if(Objects.isNull(skill)) {
            return false;
        }
        return skills.contains(skill);
    }

    public List<PlayerEffects> getActiveEffects() {
        return List.copyOf(this.activeEffects);
    }

    public void applySkillsEffects(EffectTrigger trigger, LivingEntity caster, LivingEntity target, float powerMultiplier) {

        this.activeEffects.stream()
                .filter(PlayerEffects::enabled)
                .filter(playerEffects -> playerEffects.trigger().equals(trigger))
                .forEach(playerEffects -> playerEffects.executor().apply(new EffectContext(caster.level(), (EffectHolder) playerEffects.holder(),trigger,caster,target,null,null,null,powerMultiplier)));
    }

    public void applySkillsEffectsServer(EffectTrigger trigger, LivingEntity target, float powerMultiplier) {
        this.activeEffects.stream()
                .filter(PlayerEffects::enabled)
                .filter(playerEffects -> playerEffects.trigger().equals(trigger))
                .forEach(playerEffects -> ModMessages.sendToServer(new UseSkillPacket(playerEffects.holder().getId(),playerEffects.trigger(), target.getId(), powerMultiplier)));
    }

    public Set<Skill> getSkills() {
        return Set.copyOf(skills);
    }

    public void copyFrom(PlayerSkillsCapability source) {
        this.skills.clear();
        this.skills.addAll(source.skills);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        ListTag list = new ListTag();

        for (Skill skill : skills) {
            list.add(StringTag.valueOf(skill.toString()));
        }

        nbt.put(SKILLS_KEY, list);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.skills.clear();
        this.activeEffects.clear();
        ListTag list = (ListTag) nbt.get(SKILLS_KEY);

        int listSize = Objects.nonNull(list) ? list.size() : 0;

        for (int i = 0; i < listSize; i++) {
            Skill skill = ModSkills.REGISTRY.get(list.getString(i));
            if(Objects.isNull(skill)) {
                LOGGER.error("Invalid skill id {}",list.getString(i));
                continue;
            }
            skills.add(skill);
            skill.apply(entity,this);
        }


    }

    public void removeSkill(Skill skill) {

        skills.remove(skill);
        skill.remove(entity,this);
    }

    public void clearSkills() {
        this.skills.forEach(skill -> skill.remove(entity,this));
        skills.clear();
    }

    public void addSkillEffect(PlayerEffects playerEffects) {
        this.activeEffects.add(playerEffects);
    }

    public void removeSkillEffect(String id) {
        this.activeEffects.removeIf(playerEffects -> playerEffects.holder().toString().equals(id));
    }

    public boolean hasSkill(Skill skill) {
        return this.hasSkill(skill.getId());
    }

    public boolean hasSkill(String skillId) {
        return this.skills.stream().anyMatch(skill -> skill.getId().equals(skillId));
    }

    public void reloadSkills() {
        this.skills.forEach(skill -> skill.remove(this.entity,this));
        this.skills.forEach(skill -> skill.apply(this.entity,this));
    }
}
