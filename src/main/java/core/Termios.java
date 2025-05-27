package core;

public class Termios implements AutoCloseable {
    private final LibC.struct_termios previous = new LibC.struct_termios();

    public Termios() {
        checkErrno(LibC.INSTANCE.tcgetattr(LibC.STDIN_FILENO, previous));
        LibC.struct_termios termios = previous.clone();
        termios.c_lflag &= ~(LibC.ECHO | LibC.ICANON);
        termios.c_cc[LibC.VMIN] = 1;
        termios.c_cc[LibC.VTIME] = 0;
        checkErrno(LibC.INSTANCE.tcsetattr(LibC.STDIN_FILENO, LibC.TCSANOW, termios));
    }

    @Override
    public void close() {
        checkErrno(LibC.INSTANCE.tcsetattr(LibC.STDIN_FILENO, LibC.TCSANOW, previous));
    }

    private static void checkErrno(int ret) {
        if (ret != -1)
            return;
        throw new IllegalStateException("errno: " + ret);
    }

    public static Termios enableRawMode() {
        return new Termios();
    }
}