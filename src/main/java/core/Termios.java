package core;

import com.sun.jna.Native;
import com.sun.jna.Platform;

public class Termios implements AutoCloseable {
    private final LibC.struct_termios previous = new LibC.struct_termios();
    private int ttyFd;
    private final boolean rawModeEnabled;

    public interface CLibrary extends com.sun.jna.Library {
        int open(String path, int flags);

        int close(int fd);
    }

    private static final CLibrary C = Native.load(Platform.C_LIBRARY_NAME, CLibrary.class);
    private static final int O_RDWR = 2;

    public Termios() {
        int fd = -1;
        boolean rawModeEnabled = false;
        try {
            fd = C.open("/dev/tty", O_RDWR);
            if (fd < 0)
                throw new Exception("open /dev/tty failed");
            this.ttyFd = fd;
            if (LibC.INSTANCE.tcgetattr(ttyFd, previous) == -1)
                throw new Exception("tcgetattr failed");
            LibC.struct_termios termios = previous.clone();
            long lflag = termios.c_lflag.longValue();
            lflag &= ~(LibC.ECHO | LibC.ICANON);
            termios.c_lflag.setValue(lflag);
            termios.c_cc[LibC.VMIN] = 1;
            termios.c_cc[LibC.VTIME] = 0;
            int res1 = LibC.INSTANCE.tcsetattr(ttyFd, LibC.TCSANOW, termios);
            if (res1 == -1) {
                System.err.println("tcsetattr(ttyFd) failed: " + LibC.INSTANCE.strerror(Native.getLastError()));
                throw new Exception("tcsetattr(ttyFd) failed");
            }
            int res2 = LibC.INSTANCE.tcsetattr(LibC.STDIN_FILENO, LibC.TCSANOW, termios);
            if (res2 == -1) {
                System.err.println("tcsetattr(STDIN_FILENO) failed: " + LibC.INSTANCE.strerror(Native.getLastError()));
                throw new Exception("tcsetattr(STDIN_FILENO) failed");
            }
            // long lflagCheck = termios.c_lflag.longValue();
            // if ((lflagCheck & LibC.ECHO) == 0) {
            //     System.err.println("DEBUG: ECHO is disabled");
            // } else {
            //     System.err.println("DEBUG: ECHO is still enabled");
            // }
            rawModeEnabled = true;
        } catch (Exception e) {
            System.err.println("WARNING: Raw mode not enabled (not a tty): " + e.getMessage());
            this.ttyFd = -1;
        }
        this.rawModeEnabled = rawModeEnabled;
    }

    @Override
    public void close() {
        if (rawModeEnabled && ttyFd >= 0) {
            if (LibC.INSTANCE.tcsetattr(ttyFd, LibC.TCSANOW, previous) == -1) {
                System.err.println(
                        "WARNING: Failed to restore ttyFd termios: " + LibC.INSTANCE.strerror(Native.getLastError()));
            }
            if (LibC.INSTANCE.tcsetattr(LibC.STDIN_FILENO, LibC.TCSANOW, previous) == -1) {
                System.err.println("WARNING: Failed to restore STDIN_FILENO termios: "
                        + LibC.INSTANCE.strerror(Native.getLastError()));
            }
            C.close(ttyFd);
        }
    }

    public static Termios enableRawMode() {
        return new Termios();
    }
}