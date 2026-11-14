package br.com.pemei.craftanddragons.common.modules.addon.base;

import br.com.pemei.craftanddragons.common.modules.skills.base.Skill;

import java.util.Map;
import java.util.Set;

public record CustomClass(String id, String name,String iconPath, String description, Map<Integer, Set<Skill>> skills, Float baseMana, Float baseHealth, Boolean isMagic, Integer subclassUnlockLevel, Set<CustomSubClass> subClasses) {
}
