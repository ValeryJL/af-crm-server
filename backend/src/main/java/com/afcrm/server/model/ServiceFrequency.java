package com.afcrm.server.model;

import lombok.Getter;

@Getter
public enum ServiceFrequency {
    WEEKLY("semanal"),
    FORTNIGHTLY("quincenal"),
    MONTHLY("mensual"),
    EVENTUAL("eventual");

    private final String value;

    ServiceFrequency(String value) {
        this.value = value;
    }

    public static ServiceFrequency fromString(String text) {
        for (ServiceFrequency b : ServiceFrequency.values()) {
            if (b.value.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return EVENTUAL;
    }
}
