package execution;

import commands.Command;

import java.util.LinkedList;
import java.util.Queue;

class CommandExecutor {
    private final Queue<Command> commands = new LinkedList<>();
    private final CommandHistory history = new CommandHistory();

    public void add(Command command){
        commands.add(command);
    }

    public void run(){
        while (!commands.isEmpty()){
            Command command = commands.poll();
            command.execute();
            history.add(command);
        }
    }

    public void dryRun(){
        while (!commands.isEmpty()){
            System.out.println(commands.poll());
        }
    }

    public void printSummary(){
        history.printSummary();
    }
}
