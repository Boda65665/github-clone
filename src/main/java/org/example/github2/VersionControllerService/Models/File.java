package org.example.github2.VersionControllerService.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class File {
    private String pathOriginalFile;
    private Commit commit;
}
