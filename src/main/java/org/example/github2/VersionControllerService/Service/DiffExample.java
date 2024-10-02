package org.example.github2.VersionControllerService.Service;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class DiffExample {
    public static void main(String[] args) {
        try {
            // Чтение содержимого файлов
            List<String> original = Files.readAllLines(Paths.get("P:\\test\\s.txt"));
            List<String> revised = Files.readAllLines(Paths.get("P:\\test\\n.txt"));

            // Получение патча с изменениями
            Patch<String> patch = DiffUtils.diff(original, revised);

            for (AbstractDelta<String> delta : patch.getDeltas()) {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}