package org.example.github2.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Permission {
    UNVERIFIED("UNVERIFIED"),
    USER("USER");
    private final String permission;
}