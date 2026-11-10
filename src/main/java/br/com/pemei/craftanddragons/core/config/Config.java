package br.com.pemei.craftanddragons.core.config;

import br.com.pemei.craftanddragons.Craftanddragons;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = Craftanddragons.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    public static final Path MOD_CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve(Craftanddragons.MODID);

    private static final Path MAIN_CONFIG_FILE = MOD_CONFIG_DIR.resolve("main.toml");


    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // A variável que vai guardar a lista crua do arquivo TOML
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HEAD_DROPS;

    public static final ForgeConfigSpec.ConfigValue<String> HOMEBREW_DIR_NAME = BUILDER
            .comment("Nome do diretório para Addons de Homebrew")
            .define("homebrewDirectory", "homebrew");

    public static final ForgeConfigSpec.BooleanValue ENABLE_DEFAULT_ENTITY_CATEGORY_GETTER;
    public static final ForgeConfigSpec.BooleanValue DISABLE_VANILLA_CRIT;
    public static final ForgeConfigSpec.BooleanValue ENABLE_MODDED_CRIT;

    public static final ForgeConfigSpec.BooleanValue ENABLE_MULTICLASS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_CLASS_MODULE;
    public static final ForgeConfigSpec.DoubleValue VANILLA_XP_CONVERT_TAX;



    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ENTITY_TYPES;

    static {
        BUILDER.push("Modules");
        BUILDER.push("Combat");

        DISABLE_VANILLA_CRIT = BUILDER
                .comment("Disable vanilla crit system")
                .define("disableVanillaCrit", true);

        ENABLE_MODDED_CRIT  = BUILDER
                .comment("Enable CnD crit system")
                .define("enableModdedCrit", true);


        BUILDER.pop();
        BUILDER.push("class");

        ENABLE_CLASS_MODULE = BUILDER
                .comment("enable class module")
                .define("enable", true);

        ENABLE_MULTICLASS = BUILDER
                .comment("enable multiclass")
                        .define("enableMultiClass", true);

        VANILLA_XP_CONVERT_TAX = BUILDER
                .comment("vanilla xp to CnD xp convert multiplier")
                        .defineInRange("vanillaXpConversionMultiplier", 1.0, 0.0, 2.0);

        BUILDER.pop();
        BUILDER.pop();

        BUILDER.push("EntityTypeSettings");

        ENABLE_DEFAULT_ENTITY_CATEGORY_GETTER = BUILDER
                .comment("Enable Default Entity Type List")
                .define("enableDefaultEntityCategoryGetter", true);

        ENTITY_TYPES = BUILDER
                .comment("Custom entity type list. Format: 'entity=class1;class2'")
                .defineList("entityTypes",
                        List.of(
                                // Valores padrão de exemplo
                                "minecraft:ender_dragon=boss;enderian"
                        ),
                        // Validador: Garante que é uma String e que contém o caractere '='
                        obj -> obj instanceof String && ((String) obj).contains("=")
                );

        BUILDER.pop();

        BUILDER.push("DecapitationSettings");

        HEAD_DROPS = BUILDER
                .comment("Decapitation to Item dict. Format: 'entidade=item'")
                .defineList("decapitationItems",
                        // Valores padrão
                        List.of(
                                "minecraft:zombie=minecraft:zombie_head",
                                "minecraft:skeleton=minecraft:skeleton_skull"
                        ),
                        // Validador: Garante que é uma String e que contém o caractere '='
                        obj -> obj instanceof String && ((String) obj).contains("=")
                );

        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static void register(ModLoadingContext modLoadingContext) {
        try {
            // Garante que a pasta config/craftanddragons e a subpasta homebrew existam
            if (Files.notExists(MOD_CONFIG_DIR)) {
                Files.createDirectories(MOD_CONFIG_DIR);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Registra o arquivo de config customizado no Forge vinculando ao caminho exato
        modLoadingContext.registerConfig(ModConfig.Type.COMMON, SPEC, MAIN_CONFIG_FILE.toString());
    }

    public static Path getHomebrewDir() {
        return FMLPaths.GAMEDIR.get().resolve(HOMEBREW_DIR_NAME.get());
    }
}
