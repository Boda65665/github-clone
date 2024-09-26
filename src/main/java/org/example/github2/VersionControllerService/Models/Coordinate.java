package org.example.github2.VersionControllerService.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Coordinate {
    private int line;
    private int startNumberSymbol;
    private int endNumberSymbol;
}
