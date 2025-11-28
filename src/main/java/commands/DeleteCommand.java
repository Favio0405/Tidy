package commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DeleteCommand extends Command{
    private final Path path;
    public DeleteCommand(Path path){
        this.path = path;
    }
    @Override
    public void execute(){
        try {
            wasSuccessful = Files.deleteIfExists(path);
        } catch (IOException e) {
            wasSuccessful = false;
            exception = e;
        }
    }

    @Override
    public String toString(){
        return "delete " + path.getFileName().toString();
    }
}
