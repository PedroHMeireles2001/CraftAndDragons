package br.com.pemei.craftanddragons.common.modules.skills.capabilities;

import br.com.pemei.craftanddragons.common.modules.effect.base.EffectHolder;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectContext;
import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectTrigger;
import br.com.pemei.craftanddragons.common.modules.archetype.capabilities.PlayerClassCapabilityProvider;
import br.com.pemei.craftanddragons.common.modules.skills.ModSkills;
import br.com.pemei.craftanddragons.common.modules.skills.base.PlayerEffects;
import br.com.pemei.craftanddragons.common.modules.skills.base.Skill;
import br.com.pemei.craftanddragons.common.modules.skills.impl.SkillEffect;
import br.com.pemei.craftanddragons.network.ModMessages;
import br.com.pemei.craftanddragons.network.packet.ActiveSkillsStatePacket;
import br.com.pemei.craftanddragons.network.packet.UseSkillPacket;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;
import org.slf4j.Logger;

import java.util.*;

@AutoRegisterCapability
public class PlayerSkillsCapability implements INBTSerializable<CompoundTag> {
    public static final int SKILL_SLOT_COUNT = 10;
    private final LivingEntity entity;
    private static final String SKILLS_KEY = "skills";
    private static final String SKILL_SLOTS_KEY = "skill_slots";
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Set<Skill> skills = new HashSet<>();
    private final List<String> skillSlots = new ArrayList<>(Collections.nCopies(SKILL_SLOT_COUNT, ""));
    private final List<PlayerEffects> activeEffects = new ArrayList<>();
    private ActiveSkillsStatePacket lastActiveSkillsState;

    public PlayerSkillsCapability(LivingEntity entity) {
        this.entity = entity;
    }

    public void addSkill(Skill skill) {
        Skill previous = skills.stream().filter(learned -> learned.getId().equals(skill.getId())).findFirst().orElse(null);
        if (previous != null) {
            if (previous.getLevel() >= skill.getLevel()) return;
            previous.remove(entity, this);
            skills.remove(previous);
        }
        skills.add(skill);
        skill.apply(entity,this);
        if (!(skill instanceof SkillEffect effect) || !effect.hasUseEffects()) {
            Collections.replaceAll(skillSlots, skill.getId(), "");
        }
    }

    public boolean existsSkill(Skill skill) {
        return hasSkill(skill);
    }
    public boolean existsSkill(String skillId) {
        return hasSkill(skillId);
    }

    public List<PlayerEffects> getActiveEffects() {
        return List.copyOf(this.activeEffects);
    }

    public void applySkillsEffects(EffectTrigger trigger, LivingEntity caster, LivingEntity target, float powerMultiplier) {
        if (trigger == EffectTrigger.ON_SKILL_USE) return;
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

    public boolean setSkillSlot(int slot, String skillId) {
        if (slot < 0 || slot >= SKILL_SLOT_COUNT || skillSlots.get(slot).equals(skillId)) return false;
        if (!skillId.isEmpty()) {
            if (findUsableSkill(skillId).isEmpty()) return false;
            Collections.replaceAll(skillSlots, skillId, "");
        }
        skillSlots.set(slot, skillId);
        return true;
    }

    public void useSkillSlot(int slot) {
        if (slot >= 0 && slot < SKILL_SLOT_COUNT && !skillSlots.get(slot).isEmpty()) {
            useSkill(skillSlots.get(slot));
        }
    }

    private Optional<SkillEffect> findUsableSkill(String skillId) {
        return skills.stream()
                .filter(skill -> skill.getId().equals(skillId))
                .filter(SkillEffect.class::isInstance)
                .map(SkillEffect.class::cast)
                .filter(SkillEffect::hasUseEffects)
                .findFirst();
    }

    public void useSkill(String skillId) {
        if (!(entity instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator()) return;

        findUsableSkill(skillId)
                .filter(skill -> skill.getUseRemainingTicks(player) == 0)
                .ifPresent(skill -> player.getCapability(PlayerClassCapabilityProvider.PLAYER_CLASS).ifPresent(cap -> {
                    int cost = skill.getUseManaCost();
                    if (!cap.spendMana(cost)) {
                        player.displayClientMessage(Component.translatable("skill.craftanddragons.mana.insufficient", cost), true);
                        return;
                    }

                    var context = new EffectContext(player.level(), skill, EffectTrigger.ON_SKILL_USE,
                            player, player, null, null, null, 1f);
                    skill.getEffectsExecutor(EffectTrigger.ON_SKILL_USE).forEach(executor -> executor.apply(context));
                    syncActiveSkills(player, cap.getMana(), cap.getMaxMana());
                }));
    }

    public void syncActiveSkills(ServerPlayer player, int mana, int maxMana) {
        var entries = skills.stream()
                .filter(SkillEffect.class::isInstance)
                .map(SkillEffect.class::cast)
                .filter(SkillEffect::hasUseEffects)
                .sorted(Comparator.comparing(Skill::getId))
                .map(skill -> new ActiveSkillsStatePacket.Entry(skill.getId(), skill.getName(), skill.getUseManaCost(),
                        (skill.getUseRemainingTicks(player) + 19) / 20))
                .toList();
        var state = new ActiveSkillsStatePacket(mana, maxMana, entries, List.copyOf(skillSlots));
        if (!state.equals(lastActiveSkillsState)) {
            ModMessages.sendToPlayer(player, state);
            lastActiveSkillsState = state;
        }
    }

    public void copyFrom(PlayerSkillsCapability source) {
        this.skills.clear();
        this.skills.addAll(source.skills);
        Collections.copy(skillSlots, source.skillSlots);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        ListTag list = new ListTag();

        for (Skill skill : skills) {
            CompoundTag learned = new CompoundTag();
            learned.putString("id", skill.getId());
            learned.putInt("level", skill.getLevel());
            list.add(learned);
        }

        nbt.put(SKILLS_KEY, list);
        ListTag slots = new ListTag();
        skillSlots.forEach(id -> slots.add(StringTag.valueOf(id)));
        nbt.put(SKILL_SLOTS_KEY, slots);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.skills.clear();
        this.activeEffects.clear();
        Collections.fill(skillSlots, "");
        ListTag list = (ListTag) nbt.get(SKILLS_KEY);

        int listSize = Objects.nonNull(list) ? list.size() : 0;

        for (int i = 0; i < listSize; i++) {
            boolean legacy = list.getElementType() == Tag.TAG_STRING;
            String id = legacy ? list.getString(i) : list.getCompound(i).getString("id");
            Skill skill = legacy ? ModSkills.REGISTRY.get(id)
                    : ModSkills.getSkill(id, list.getCompound(i).getInt("level"));
            if(Objects.isNull(skill)) {
                LOGGER.error("Invalid saved skill {}", id);
                continue;
            }
            addSkill(skill);
        }
        ListTag slots = nbt.getList(SKILL_SLOTS_KEY, Tag.TAG_STRING);
        for (int slot = 0; slot < Math.min(slots.size(), SKILL_SLOT_COUNT); slot++) {
            setSkillSlot(slot, slots.getString(slot));
        }
    }

    public void removeSkill(Skill skill) {
        Skill learned = skills.stream().filter(current -> current.getId().equals(skill.getId())).findFirst().orElse(null);
        if (learned == null) return;
        learned.remove(entity, this);
        skills.remove(learned);
        Collections.replaceAll(skillSlots, learned.getId(), "");
    }

    public void clearSkills() {
        this.skills.forEach(skill -> skill.remove(entity,this));
        skills.clear();
        Collections.fill(skillSlots, "");
    }

    public void addSkillEffect(PlayerEffects playerEffects) {
        this.activeEffects.add(playerEffects);
    }

    public void removeSkillEffect(String id) {
        this.activeEffects.removeIf(playerEffects -> playerEffects.holder().toString().equals(id));
    }

    public boolean hasSkill(Skill skill) {
        return skills.stream().anyMatch(learned -> learned.getId().equals(skill.getId())
                && learned.getLevel() >= skill.getLevel());
    }

    public boolean hasSkill(String skillId) {
        return this.skills.stream().anyMatch(skill -> skill.getId().equals(skillId));
    }

    public void reloadSkills() {
        this.skills.forEach(skill -> skill.remove(this.entity, this));
        this.skills.forEach(skill -> skill.apply(this.entity,this));
    }
}
