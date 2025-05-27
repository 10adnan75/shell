package core;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;

public interface LibC extends Library {
    LibC INSTANCE = Native.load("c", LibC.class);

    int STDIN_FILENO = 0;
    int NCCS = 32;
    int ICANON = 2;
    int ECHO = 8;
    int VTIME = 5;
    int VMIN = 6;
    int TCSANOW = 0;

    String strerror(int errnum);

    int tcgetattr(int fd, struct_termios termios_p);

    int tcsetattr(int fd, int optional_actions, struct_termios termios_p);

    @Structure.FieldOrder({
            "c_iflag",
            "c_oflag",
            "c_cflag",
            "c_lflag",
            "c_line",
            "c_cc",
            "c_ispeed",
            "c_ospeed",
    })
    class struct_termios extends Structure implements Cloneable {
        public int c_iflag;
        public int c_oflag;
        public int c_cflag;
        public int c_lflag;
        public byte c_line;
        public byte[] c_cc = new byte[NCCS];
        public int c_ispeed;
        public int c_ospeed;

        @Override
        protected struct_termios clone() {
            struct_termios copy = new struct_termios();
            copy.c_iflag = c_iflag;
            copy.c_oflag = c_oflag;
            copy.c_cflag = c_cflag;
            copy.c_lflag = c_lflag;
            copy.c_line = c_line;
            copy.c_cc = c_cc.clone();
            copy.c_ispeed = c_ispeed;
            copy.c_ospeed = c_ospeed;
            return copy;
        }
    }
}