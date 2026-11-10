package br.com.pemei.craftanddragons.common.modules.addon;

import br.com.pemei.craftanddragons.common.modules.effect.annotations.OptionalParam;
import br.com.pemei.craftanddragons.common.modules.addon.base.CustomEffectChain;
import br.com.pemei.craftanddragons.common.modules.addon.base.CustomSkill;
import br.com.pemei.craftanddragons.core.config.Config;
import br.com.pemei.craftanddragons.common.modules.effect.ModEffects;
import br.com.pemei.craftanddragons.common.modules.skills.ModSkills;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class JsonAddonLoader {

    private final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final static Map<String, String> ADDON_REGISTRY = new HashMap<>();

    public static Map<String, Object> extractParams(Object obj) {
        Map<String, Object> params = new HashMap<>();
        if (obj == null) return params;

        Class<?> aClass = obj.getClass();
        for (Field field : aClass.getDeclaredFields()) {
            if (!field.isAnnotationPresent(OptionalParam.class)) {
                continue;
            }
            try {
                field.setAccessible(true); // Garante acesso mesmo a atributos privados sem depender de getters
                Object objValue = field.get(obj);
                if (objValue != null) {
                    params.put(field.getName(), objValue);
                }
            } catch (IllegalAccessException e) {
                // Ignora campos inacessíveis
            }
        }

        return params;
    }

    public static void loadAddons() {
        Path homebrewDir = Config.getHomebrewDir();

        // Garante que o diretório exista antes de listar os arquivos
        if (!Files.exists(homebrewDir)) {
            try {
                Files.createDirectories(homebrewDir);
            } catch (IOException e) {
                System.err.println("Não foi possível criar o diretório homebrew: " + e.getMessage());
                return;
            }
        }

        try (Stream<Path> fileList = Files.list(homebrewDir)) {
            fileList.filter(Files::isDirectory)
                    .forEach(JsonAddonLoader::loadAddon);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void loadAddon(Path path) {
        String addonName = path.getFileName().toString();
        //create effects chain
        Path effectsDir = path.resolve("effects");
        if (Files.exists(effectsDir) && Files.isDirectory(effectsDir)) {
            try(Stream<Path> effectPathList = Files.list(path.resolve("effects"))){
                effectPathList
                        .filter(p -> p.getFileName().toString().endsWith(".json"))
                        .map(JsonAddonLoader::readStringJson)
                        .filter(Optional::isPresent)
                        .map(effectString -> GSON.fromJson(effectString.get(), CustomEffectChain.class))
                        .collect(Collectors.toMap(
                                CustomEffectChain::id,
                                CustomEffectChain::getExecutor,
                                (existing, replacement) -> replacement
                        ))
                        .forEach((name, effectExecutor) -> ModEffects.registerEffect(name,()->effectExecutor));
            } catch (IOException e) {
                System.out.println("No effects found");
            }

        }


        Path skillsDir = path.resolve("skills");
        if (Files.exists(skillsDir) && Files.isDirectory(skillsDir)) {
            try(Stream<Path> skillPathList = Files.list(path.resolve("skills"))) {
                skillPathList
                        .filter(p -> p.getFileName().toString().endsWith(".json"))
                        .map(JsonAddonLoader::readStringJson)
                        .filter(Optional::isPresent)
                        .map(effectString -> GSON.fromJson(effectString.get(), CustomSkill.class))
                        .collect(Collectors.toMap(
                                CustomSkill::getId,
                                CustomSkill::toSkill,
                                (existing, replacement) -> replacement
                        ))
                        .forEach((name,skill) -> {
                            ADDON_REGISTRY.put(addonName, skill.getId());
                            ModSkills.REGISTRY.put(name, skill);
                        });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }




    }

    private static Optional<String> readStringJson(Path path) {
        try {
            String content = Files.readString(path);
            if(content.isEmpty()) {
                return Optional.empty();
            }
            if(!(content.startsWith("[") || content.startsWith("{"))) {
                return Optional.empty();
            }
            return Optional.of(content);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

}
