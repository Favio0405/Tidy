package execution;

import commands.Command;

import java.util.*;
import java.util.stream.Collectors;

class CommandHistory {
    private final List<Command> history = new ArrayList<>();

    public void add(Command command){
        history.add(command);
    }

    public void printSummary(){
        System.out.println("\n====== SUMMARY ======\n");

        Map<Class<?>, List<Command>> classMap = history.stream().collect(Collectors.groupingBy(Object::getClass));

        classMap.forEach((k, v) -> {
            Map<Boolean, List<Command>> successMap = v.stream()
                    .collect(Collectors.partitioningBy(Command::wasSuccessful));
            String name = String.join(" ", k.getSimpleName().split("(?=[A-Z])"));
            System.out.println("Successful " + name + "s:");
            successMap.get(true).forEach(System.out::println);
            if(successMap.get(false).isEmpty()) System.out.println("No failed " + name + "s:");
            else{
                System.out.println("Failed " + name + "s:");
                successMap.get(false).forEach(e -> System.out.println(e + ": " + e.getException()));
            }
        });
    }
}
