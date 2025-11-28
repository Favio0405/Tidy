import execution.Organizer;
import org.apache.commons.cli.*;

import java.io.IOException;

public class Tidy {
    public static void main(String[] args){
        Options options = new Options();

        Option dir =  new Option("f", "directory", true, "Directory to tidy up");
        dir.setRequired(true);
        options.addOption(dir);

        Option timeToDelete = new Option("d", "time",
                true, "Time in days after last accessing to delete");

        timeToDelete.setRequired(false);
        options.addOption(timeToDelete);

        Option testRun = new Option("t", "test",
                false, "Test run, does not actually change filesystem");
        testRun.setRequired(false);
        options.addOption(testRun);

        Option emptyDirs = new Option("e", "empty-directories",
                false, "Delete empty directories after organizing");
        emptyDirs.setRequired(false);
        options.addOption(emptyDirs);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("Command line error: " + e);
            e.printStackTrace();
            System.exit(1);
        }

        String sourceDir = cmd.getOptionValue("f");
        boolean deleteOldFiles = cmd.hasOption("d");
        boolean deleteEmptyDirs = cmd.hasOption("e");
        boolean dryRun = cmd.hasOption("t");

        Organizer organizer;
        try {
            if (deleteOldFiles) {
                String daysString = cmd.getOptionValue("d");
                int days = Integer.parseInt(daysString);
                organizer = new Organizer(sourceDir, days);
            } else {
                organizer = new Organizer(sourceDir);
            }
        } catch (NumberFormatException | NullPointerException e) {
            System.err.println("Please enter a valid integer: " + e);
            System.exit(2);
            return;
        } catch (IOException e) {
            System.err.println("Could not categorize files: " + e);
            System.exit(3);
            return;
        }

        organizer.organizeFiles();

        if(dryRun) organizer.dryRun();
        else {
            organizer.writeChanges();
            organizer.printSummary();
        }
        if(deleteEmptyDirs) {
            organizer.deleteEmptyDirs();
            if(dryRun) organizer.dryRun();
            else organizer.writeChanges();
        }
    }
}
