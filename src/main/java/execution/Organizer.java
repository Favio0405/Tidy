package execution;

import commands.DeleteCommand;
import commands.MakeDirCommand;
import commands.MoveCommand;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Organizer {
    private final Path sourceDir;
    private final int numberOfDays;
    private final CommandExecutor executor;
    private final Map<String, List<Path>> organizedFiles;
    private final HashSet<Path> oldFiles;

    public Organizer(String sourceDir,  int numberOfDays) throws IOException {
        this.sourceDir = Paths.get(sourceDir);
        this.numberOfDays = numberOfDays;
        this.oldFiles = new HashSet<>();
        this.executor = new CommandExecutor();

        findOldFiles();
        this.organizedFiles = categorizeFiles();
    }

    public Organizer(String sourceDir) throws IOException {
        this.sourceDir = Paths.get(sourceDir);
        this.numberOfDays = -1;
        this.oldFiles = new HashSet<>();
        this.executor = new CommandExecutor();

        this.organizedFiles = categorizeFiles();
    }

    private Map<String, List<Path>> categorizeFiles() throws IOException {
        Map<String, List<Path>> map;

        try(Stream<Path> stream = Files.list(sourceDir)){
            map = stream.filter(this::toCategorize)
                    .collect(Collectors.groupingBy(Organizer::getFileExtension));
        }

        return map;
    }
    private void findOldFiles() throws IOException{
        if(numberOfDays < 0) return;
        try(Stream<Path> stream = Files.list(sourceDir)){
            stream.filter(this::toDelete).forEach(this::updateOldFiles);
        }
    }

    public void organizeFiles() {
        organizedFiles.keySet().forEach(this::addMoveAndDirCommands);
    }
    

    public void deleteEmptyDirs(){
        try(Stream<Path> stream = Files.list(sourceDir)){
            stream.filter(Organizer::isEmptyDir).map(DeleteCommand::new).forEach(executor::add);
        } catch (IOException e) {
            System.err.println("Could not remove empty directories: " + e);
            e.printStackTrace();
        }
    }

    public void writeChanges(){
        executor.run();
    }

    public void dryRun(){
        executor.dryRun();
    }

    public void printSummary(){
        executor.printSummary();
    }

    //UTILITY

    private void addMoveAndDirCommands(String str){
        Path newDir = sourceDir.resolve(str);
        executor.add(new MakeDirCommand(newDir));
        organizedFiles.get(str).forEach(f -> executor.add(new MoveCommand(f, newDir)));
    }
    private boolean toDelete(Path path) {
        if (Files.isDirectory(path)) return false;
        try {
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
            Instant lastAccess;
            try {
                lastAccess = attributes.lastAccessTime().toInstant();
            } catch (Exception ignored) {
                lastAccess = attributes.lastModifiedTime().toInstant();
            }
            return Duration.between(lastAccess, Instant.now()).toDays() > numberOfDays;
        } catch (IOException e) {
            System.err.println("Could not read attributes: " + path + "\n" + e);
            e.printStackTrace();
            return false;
        }
    }

    private static String getFileExtension(Path file) {
        String name = file.getFileName().toString();
        int idx = name.lastIndexOf('.');
        return (idx > 0 && idx < name.length() - 1) ? name.substring(idx + 1).toLowerCase() : "other";
    }

    private static boolean isEmptyDir(Path dir){
        if(!Files.isDirectory(dir)) return false;
        boolean isEmpty = false;
        try(Stream<Path> stream = Files.list(dir)) {
            isEmpty = stream.findAny().isEmpty();
        } catch (IOException e) {
            System.err.println("Could not check for empty directories " + e);
            e.printStackTrace();
        }
        return isEmpty;
    }

    private void updateOldFiles(Path file){
        oldFiles.add(file);
        executor.add(new DeleteCommand(file));
    }

    private boolean toCategorize(Path file){
        return !Files.isDirectory(file) && !Organizer.isHidden(file) && !oldFiles.contains(file);
    }

    private static boolean isHidden(Path file){
        try {
            return Files.isHidden(file);
        } catch (IOException e) {
            System.err.println("File access error, could not categorize file" + file + "file will be skipped " + e);
            e.printStackTrace();
        }
        return true;
    }
}
