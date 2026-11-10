package br.com.pemei.craftanddragons.common.modules.archetype.capabilities;

import br.com.pemei.craftanddragons.common.modules.archetype.ModArchetype;
import br.com.pemei.craftanddragons.common.modules.archetype.base.Archetype;
import br.com.pemei.craftanddragons.common.modules.archetype.impl.PrincipalArchetype;
import br.com.pemei.craftanddragons.common.modules.archetype.impl.SubArchetype;
import br.com.pemei.craftanddragons.common.modules.skills.base.Skill;
import br.com.pemei.craftanddragons.common.modules.skills.capabilities.PlayerSkillsCapability;
import br.com.pemei.craftanddragons.common.modules.skills.capabilities.PlayerSkillsCapabilityProvider;
import br.com.pemei.craftanddragons.core.config.Config;
import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import static br.com.pemei.craftanddragons.common.modules.archetype.ModArchetype.xpRequiredForNextLevel;

@AutoRegisterCapability
@Getter
public class PlayerClassCapability implements INBTSerializable<CompoundTag> {
    private final Player player;

    private PrincipalArchetype archetype;
    private SubArchetype subArchetype;
    private int classLevel;
    private double xp;
    @Getter(AccessLevel.PRIVATE)
    private Map<PrincipalArchetype, Integer> multiclassLevelMap = new HashMap<>();
    @Getter(AccessLevel.PRIVATE)
    private Map<PrincipalArchetype, SubArchetype> multiclassSubArchetypeMap = new HashMap<>();


    public PlayerClassCapability(Player player) {
        this.player = player;
    }


    public boolean hasMulticlass(){
        return Objects.nonNull(multiclassLevelMap) && !multiclassLevelMap.isEmpty();
    }

    public boolean hasLevel(int level,PrincipalArchetype archetype){
        if(Objects.isNull(archetype)){
            return false;
        }
        if(this.archetype.equals(archetype) && this.classLevel >= level){
            return true;
        }
        return this.hasMulticlass() && this.multiclassLevelMap.getOrDefault(archetype, 0) >= level;
    }

    public double xpRequiredForLevel(int level){
        return xpRequiredForNextLevel.get(level-1);
    }

    public void convertVanillaXp(){
        this.player.totalExperience = 0;
        double convertedXp = this.player.totalExperience * Config.VANILLA_XP_CONVERT_TAX.get();
        giveXp(convertedXp);
    }

    public void giveXp(double xp){
        this.xp += xp;
        double requiredXp = xpRequiredForLevel(classLevel+1);
        if(this.xp >= requiredXp){
            double xpExcess = this.xp - requiredXp;
            giveLevel();
            this.xp = 0;
            giveXp(xpExcess);
        }

    }

    public void setClass(PrincipalArchetype newArchetype) {
        if (Objects.isNull(newArchetype)) return;

        // 1. Se já tinha classe antiga, limpa as skills pertencentes ao arquétipo ANTIGO
        if (Objects.nonNull(this.archetype)) {
            forEachSkillsInLevel(this.archetype, this.classLevel,
                    (skill, cap) -> cap.removeSkill(skill),
                    (skill, cap) -> cap.hasSkill(skill)
            );
        }

        // 2. Atualiza o arquétipo e reseta a progressão
        this.archetype = newArchetype;
        this.classLevel = 0;

        // 3. Concede o Nível 1 inicial da nova classe
        this.giveLevel();
    }

    public void giveLevel(){
        giveLevel(1);
    }
    public void giveLevel(int levelsGained) {
        if (levelsGained <= 0) return;

        this.classLevel += levelsGained;

        reloadClass();
    }

    private void forEachSkillsInLevel(PrincipalArchetype targetArchetype, int maxLevel, BiConsumer<Skill, PlayerSkillsCapability> consumer, BiPredicate<Skill, PlayerSkillsCapability> condition) {
        if (Objects.isNull(targetArchetype) || targetArchetype.getSkillsPerLevel() == null) return;

        // Garante que opera na Capability OFICIAL atrelada ao jogador (evita instâncias temporárias descartadas)
        player.getCapability(PlayerSkillsCapabilityProvider.PLAYER_SKILLS).ifPresent(playerSkillsCapability -> {
            targetArchetype.getSkillsPerLevel().entrySet().stream()
                    .filter(entry -> entry.getKey() <= maxLevel)
                    .flatMap(entry -> entry.getValue().stream())
                    .filter(skill -> condition.test(skill, playerSkillsCapability))
                    .forEach(skill -> consumer.accept(skill, playerSkillsCapability));
        });
    }

    private void reloadClass(){
        forEachSkillsInLevel(this.archetype, this.classLevel,
                (skill, cap) -> cap.addSkill(skill),
                (skill, cap) -> !cap.hasSkill(skill)
        );
    }


    @Override
    public CompoundTag serializeNBT() {
        CompoundTag archetypeCompoundTag = new CompoundTag();

        if(Objects.isNull(this.archetype)){
            return archetypeCompoundTag; // if player not have a class, there is no reason to store the NBT tag.
        }

        StringTag archetypeTag = StringTag.valueOf(this.archetype.getId());


        IntTag classLevelTag = IntTag.valueOf(this.classLevel); //primitive values don´t have null
        DoubleTag xpTag = DoubleTag.valueOf(this.xp); //primitive values don´t have null

        archetypeCompoundTag.put("main_archetype", archetypeTag);
        archetypeCompoundTag.put("class_level", classLevelTag);
        archetypeCompoundTag.put("class_xp", xpTag);

        if(Objects.nonNull(this.subArchetype)){
            StringTag subArchetypeTag = StringTag.valueOf(this.subArchetype.getId());
            archetypeCompoundTag.put("main_sub_archetype", subArchetypeTag);
        }

        if(Config.ENABLE_MULTICLASS.get()){
            ListTag multiClassList = new ListTag();
            for(PrincipalArchetype multiArchetype : multiclassLevelMap.keySet()){
                int multiLevel = multiclassLevelMap.get(multiArchetype);
                SubArchetype multiSubArchetype = multiclassSubArchetypeMap.get(multiArchetype);
                StringTag multiClassTag;
                if(Objects.nonNull(multiSubArchetype)){
                    multiClassTag = StringTag.valueOf("%s;%s;%s".formatted(multiArchetype.getName(), multiSubArchetype.getName(),multiLevel));
                }else{
                    multiClassTag = StringTag.valueOf("%s;%s".formatted(multiArchetype.getName(), multiLevel));
                }
                multiClassList.add(multiClassTag);
            }
            archetypeCompoundTag.put("multi_class", multiClassList);
        }
        return archetypeCompoundTag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.multiclassLevelMap.clear();
        this.multiclassSubArchetypeMap.clear();

        this.archetype = ModArchetype.getArchetype(nbt.getString("main_archetype"));
        this.classLevel = nbt.getInt("class_level");
        this.xp = nbt.getDouble("class_xp");

        if (nbt.contains("main_sub_archetype")) {
            this.subArchetype = ModArchetype.getSubArchetype(nbt.getString("main_sub_archetype"));
        }

        if(Config.ENABLE_MULTICLASS.get()){
            if(!nbt.contains("multi_class")) {
                return;
            }
            ListTag listTag = (ListTag) nbt.get("multi_class");
            int listSize = Objects.nonNull(listTag) ? listTag.size() : 0;
            for (int i = 0; i < listSize; i++) {
                String[] chunks = listTag.getString(i).split(";");
                PrincipalArchetype multiArchetype = ModArchetype.getArchetype(chunks[0]);
                int level = Integer.parseInt(chunks[1]);
                multiclassLevelMap.put(multiArchetype, level);
                if(chunks.length >= 3){
                    SubArchetype subArchetype = ModArchetype.getSubArchetype(chunks[2]);
                    multiclassSubArchetypeMap.put(multiArchetype, subArchetype);
                }
            }
        }

        reloadClass();
    }

    public void copyFrom(PlayerClassCapability oldStore) {
        this.archetype = oldStore.archetype;
        this.subArchetype = oldStore.subArchetype;
        this.classLevel = oldStore.classLevel;
        this.xp = oldStore.xp;
        if(Config.ENABLE_MULTICLASS.get()){
            this.multiclassLevelMap.clear();
            this.multiclassSubArchetypeMap.clear();
            this.multiclassSubArchetypeMap.putAll(oldStore.multiclassSubArchetypeMap);
            this.multiclassLevelMap.putAll(oldStore.multiclassLevelMap);
        }
    }
}
