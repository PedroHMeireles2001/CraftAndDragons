package br.com.pemei.craftanddragons.common.modules.addon.base;

import br.com.pemei.craftanddragons.common.modules.effect.impl.EffectContext;
import br.com.pemei.craftanddragons.common.entity.EntityCategory;
import br.com.pemei.craftanddragons.core.exceptions.*;
import br.com.pemei.craftanddragons.core.init.ModEntities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static br.com.pemei.craftanddragons.core.init.ModItems.DECAPTATION_MAP;

public enum CustomFilterStrategy {
    HAS_HEAD {
        @Override
        public boolean apply(boolean expectedResult, EffectContext context, Map<String, Object> configParams) throws EffectContextException {
            if(context.getTargetEntity().isEmpty()){
                throw new InvalidTargetException();
            }
            return DECAPTATION_MAP.containsKey(context.getTargetEntity().get().getType()) == expectedResult;
        }
    }, TYPE_FILTER {
        @Override
        public boolean apply(boolean expectedResult, EffectContext context, Map<String, Object> configParams) throws EffectContextException {
            if(context.getTargetEntity().isEmpty()){
                throw new InvalidTargetException();
            }
            EntityCategory category = (EntityCategory) configParams.getOrDefault("expectedCategory", EntityCategory.OTHER);
            Set<EntityCategory> categories = ModEntities.entityTypes.getOrDefault(context.getTargetEntity().get().getType(),Set.of(EntityCategory.OTHER));

            return categories.contains(category);
        }
    }, UNDEAD {
        @Override
        public boolean apply(boolean expectedResult, EffectContext context, Map<String, Object> configParams) throws EffectContextException {
            if(context.getTargetEntity().isEmpty()){
                System.out.println("Lançando InvalidTargetException");
                throw new InvalidTargetException();
            }
            if (context.getTargetEntity().get().getMobType().equals(MobType.UNDEAD)) return expectedResult;
            assert context.targetEntity() != null;
            return ModEntities.entityTypes.getOrDefault(context.targetEntity().getType(),Set.of(EntityCategory.OTHER)).contains(EntityCategory.UNDEAD) == expectedResult;
        }
    }, ARTHROPOD {
        @Override
        public boolean apply(boolean expectedResult, EffectContext context, Map<String, Object> configParams) throws EffectContextException {
            if(context.getTargetEntity().isEmpty()){
                throw new InvalidTargetException();
            }
            return context.getTargetEntity().get().getMobType().equals(MobType.ARTHROPOD);
        }
    }, MOD_FILTER {
        @Override
        public boolean apply(boolean expectedResult, EffectContext context, Map<String, Object> configParams) throws EffectContextException {
            String expectedModid = (String) configParams.getOrDefault("mod","minecraft");

            ResourceLocation resource = null;
            if(context.getTargetEntity().isPresent()){
                EntityType<?> type = context.getTargetEntity().get().getType();
                resource = ForgeRegistries.ENTITY_TYPES.getKey(type);
            }

            if(context.getTargetBlockState().isPresent() || context.getTargetBlockPos().isPresent()){
                BlockState blockState = null;

                if(context.getTargetBlockState().isPresent()){
                    blockState = context.getTargetBlockState().get();
                }else{
                    blockState = context.level().getBlockState(context.getTargetBlockPos().get());
                }

                resource = ForgeRegistries.BLOCKS.getKey(blockState.getBlock());
            }

            if(Objects.nonNull(resource)){
                String modId = resource.getNamespace();
                return modId.equals(expectedModid) == expectedResult;
            }


            return false;
        }
    }, HEALTH_FILTER {
        @Override
        public boolean apply(boolean expectedResult, EffectContext context, Map<String, Object> configParams) throws EffectContextException {
            if(context.getTargetEntity().isEmpty()){
                throw new InvalidTargetException();
            }
            LivingEntity livingEntity = context.getTargetEntity().get();
            CustomComparator comparator = (CustomComparator) configParams.getOrDefault("comparator", CustomComparator.GREATER_OR_EQUAL);
            float health = livingEntity.getHealth();
            float expectedHealth = ((Number) configParams.getOrDefault("expectedValue", 20)).floatValue();

            return comparator.compare(health, expectedHealth) == expectedResult;
        }
    }, PLAYER {
        @Override
        public boolean apply(boolean expectedResult, EffectContext context, Map<String, Object> configParams) throws EffectContextException {
            if(context.getTargetEntity().isEmpty()){
                throw new InvalidTargetException();
            }
            return context.getTargetEntity().get().getType().equals(EntityType.PLAYER) || context.getTargetEntity().get() instanceof Player;
        }
    }, ALLY {
        @Override
        public boolean apply(boolean expectedResult, EffectContext context, Map<String, Object> configParams) throws EffectContextException {
            if(context.getTargetEntity().isEmpty()){
                throw new InvalidTargetException();
            }
            if(Objects.isNull(context.caster())){
                throw new InvalidCasterException();
            }
            return context.getTargetEntity().get().isAlliedTo(context.caster()) == expectedResult;
        }
    }, ANIMAL {
        @Override
        public boolean apply(boolean expectedResult, EffectContext context, Map<String, Object> configParams) throws EffectContextException {
            if(context.getTargetEntity().isEmpty()){
                throw new InvalidTargetException();
            }

            return context.getTargetEntity().get() instanceof Animal;
        }
    }, CAN_REPRODUCE {
        @Override
        public boolean apply(boolean expectedResult, EffectContext context, Map<String, Object> configParams) throws EffectContextException {
            if(context.getTargetEntity().isEmpty()){
                throw new InvalidTargetException();
            }
            if (!(context.getTargetEntity().get() instanceof Animal animal)) {
                throw new InvalidTargetException();
            }
            return !(animal.isBaby() || animal.getAge() != 0) == expectedResult;
        }
    }, HAS_ATTRIBUTE {
        @Override
        public boolean apply(boolean expectedResult, EffectContext context, Map<String, Object> configParams) throws EffectContextException, FilterException {
            return CustomFilterStrategy.getAttributeInstance(context, configParams).isPresent() == expectedResult;
        }
    },ATTRIBUTE{
        @Override
        public boolean apply(boolean expectedResult, EffectContext context, Map<String, Object> configParams) throws EffectContextException, FilterException {
            Optional<AttributeInstance> attributeInstance = CustomFilterStrategy.getAttributeInstance(context, configParams);
            if(attributeInstance.isEmpty()){
                return !expectedResult;
            }
            double attributeValue = attributeInstance.get().getValue();
            double expectedAttributeValue = ((Number) configParams.getOrDefault("expectedValue",0)).doubleValue();
            CustomComparator comparator = (CustomComparator) configParams.getOrDefault("comparator", CustomComparator.GREATER_OR_EQUAL);
            return comparator.compare(attributeValue, expectedAttributeValue) == expectedResult;
        }
    };

    private static Optional<AttributeInstance> getAttributeInstance(EffectContext context, Map<String, Object> configParams) throws InvalidTargetException, MissingFilterConfigException {
        if(context.getTargetEntity().isEmpty()){
            throw new InvalidTargetException();
        }
        LivingEntity livingEntity = context.getTargetEntity().get();
        Object attributeObj = configParams.get("attribute");
        if(Objects.isNull(attributeObj)){
            throw new MissingFilterConfigException();
        }
        Attribute attribute = (Attribute) attributeObj;
        AttributeInstance attributeInstance = livingEntity.getAttribute(attribute);
        return Optional.ofNullable(attributeInstance);
    }

    public abstract boolean apply(boolean expectedResult, EffectContext context, Map<String, Object> configParams) throws EffectContextException, FilterException;
    public final boolean applySafe(boolean expectedResult, EffectContext context, Map<String, Object> configParams) {
        try {
            return this.apply(expectedResult,context,configParams);
        } catch (EffectContextException e) {
            e.printStackTrace();
            return false;
        }catch (FilterException | ClassCastException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
