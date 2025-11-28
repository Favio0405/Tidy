package commands;

import java.io.IOException;
import java.nio.file.*;

public class MoveCommand extends Command{
    private final Path file;
    private final Path dir;

    public MoveCommand(Path file, Path dir) {
        this.file = file;
        this.dir = dir;
    }

    @Override
    public void execute() {
        String name = file.getFileName().toString();
        String base = name;
        String ext = "";
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            base = name.substring(0, dot);
            ext = name.substring(dot); // includes the dot
        }

        Path target = dir.resolve(name);
        int i = 1;
        while (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
            String newName = String.format("%s(%d)%s", base, i, ext);
            target = dir.resolve(newName);
            i++;
        }

        try {
            // try atomic move first, fallback if not supported
            try {
                Files.move(file, target, StandardCopyOption.ATOMIC_MOVE);
                wasSuccessful = true;
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(file, target);
                wasSuccessful = true;
            }
        } catch (IOException ex) {
            wasSuccessful = false;
            exception = ex;
        }
    }

    @Override
    public String toString(){
        return "Move file from " + file + " to " + dir;
    }
}
