package br.com.pemei.craftanddragons.core.init;

import br.com.pemei.craftanddragons.core.config.Config;
import br.com.pemei.craftanddragons.common.entity.EntityCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.stream.Collectors;

public class ModEntities {
    public static Map<EntityType<?>, Set<EntityCategory>> entityTypes = new HashMap<>();

    public static void putDefaultCategories(LivingEntity entity) {
        if(entityTypes.containsKey(entity.getType()) || !Config.ENABLE_DEFAULT_ENTITY_CATEGORY_GETTER.get()) {
            return;
        }


        Set<EntityCategory> categories = new HashSet<>();
        EntityType<?> type = entity.getType();
        String modId = Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(type)).getNamespace();

        if(type.is(Tags.EntityTypes.BOSSES)){
            categories.add(EntityCategory.BOSS);
        }
        if(entity.getMobType().equals(MobType.UNDEAD)){
            categories.add(EntityCategory.UNDEAD);
        }
        if(entity.getMobType().equals(MobType.ARTHROPOD)){
            categories.add(EntityCategory.ARTHROPOD);
            categories.add(EntityCategory.BEAST);
        }
        if(entity.getMobType().equals(MobType.ILLAGER) || type.equals(EntityType.VILLAGER) || entity instanceof Player){
            categories.add(EntityCategory.HUMANOID);
        }
        if(entity instanceof Animal){
            categories.add(EntityCategory.BEAST);
        }
        if(type.fireImmune()){
            categories.add(EntityCategory.FIEND);
        }
        if(modId.equals("aether")){
            categories.add(EntityCategory.HOLY);
        }

        if(modId.equals("twilight")){
            categories.add(EntityCategory.FEY);
        }

        if(categories.isEmpty()){
            System.out.println("Nenhuma categoria encontrada!");
            categories.add(EntityCategory.OTHER);
        }else{
            System.out.println("Registrando %s como %s".formatted(type.toString(), categories.stream().map(Objects::toString).collect(Collectors.joining(","))));
        }



        entityTypes.put(type,categories);
    }
}
