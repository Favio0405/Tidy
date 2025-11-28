package commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MakeDirCommand extends Command{
    private final Path dir;
    public MakeDirCommand(Path dir){
        this.dir = dir;
    }
    @Override
    public void execute() {
        try {
            Files.createDirectories(dir);
            wasSuccessful = true;
        } catch (IOException e) {
            wasSuccessful = false;
            exception = e;
        }
    }

    @Override
    public String toString(){
        return  "Create directory " + dir.getFileName();
    }
}

