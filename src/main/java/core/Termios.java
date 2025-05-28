package core;

import com.sun.jna.Native;
import com.sun.jna.Platform;

public class Termios implements AutoCloseable {
    private final LibC.struct_termios previous = new LibC.struct_termios();
    private final int ttyFd;

    public interface CLibrary extends com.sun.jna.Library {
        int open(String path, int flags);

        int close(int fd);
    }

    private static final CLibrary C = Native.load(Platform.C_LIBRARY_NAME, CLibrary.class);
    private static final int O_RDWR = 2;

    public Termios() {
        int fd = C.open("/dev/tty", O_RDWR);
        if (fd < 0) {
            throw new IllegalStateException("Failed to open /dev/tty");
        }
        this.ttyFd = fd;
        checkErrno(LibC.INSTANCE.tcgetattr(ttyFd, previous));
        LibC.struct_termios termios = previous.clone();
        termios.c_lflag &= ~(LibC.ECHO | LibC.ICANON);
        termios.c_cc[LibC.VMIN] = 1;
        termios.c_cc[LibC.VTIME] = 0;
        checkErrno(LibC.INSTANCE.tcsetattr(ttyFd, LibC.TCSANOW, termios));
    }

    @Override
    public void close() {
        checkErrno(LibC.INSTANCE.tcsetattr(ttyFd, LibC.TCSANOW, previous));
        C.close(ttyFd);
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