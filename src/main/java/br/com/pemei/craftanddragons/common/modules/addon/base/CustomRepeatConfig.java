package br.com.pemei.craftanddragons.common.modules.addon.base;

import java.util.Objects;

public record CustomRepeatConfig(Integer delay, int times) {
    public int getDelay() {
        return Objects.nonNull(delay) ? delay : 20;
    }
}
