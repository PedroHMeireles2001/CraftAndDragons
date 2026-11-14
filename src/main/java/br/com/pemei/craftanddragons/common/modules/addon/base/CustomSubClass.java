package br.com.pemei.craftanddragons.common.modules.addon.base;

import br.com.pemei.craftanddragons.common.modules.skills.base.Skill;

import java.util.Map;
import java.util.Set;

public record CustomSubClass(String id, String name,String iconPath, String description, Map<Integer, Set<Skill>> skills) {
}
