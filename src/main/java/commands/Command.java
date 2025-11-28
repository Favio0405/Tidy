package commands;

public abstract class Command {
    protected boolean wasSuccessful = false;
    protected Exception exception;
    public abstract void execute();

    public boolean wasSuccessful() {
        return wasSuccessful;
    }

    public Exception getException() {
        return exception;
    }
}
