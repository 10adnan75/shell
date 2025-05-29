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
            "c_cc",
            "c_ispeed",
            "c_ospeed",
    })
    class struct_termios extends Structure implements Cloneable {
        public com.sun.jna.NativeLong c_iflag;
        public com.sun.jna.NativeLong c_oflag;
        public com.sun.jna.NativeLong c_cflag;
        public com.sun.jna.NativeLong c_lflag;
        public byte[] c_cc = new byte[NCCS];
        public com.sun.jna.NativeLong c_ispeed;
        public com.sun.jna.NativeLong c_ospeed;

        @Override
        protected struct_termios clone() {
            struct_termios copy = new struct_termios();
            copy.c_iflag = new com.sun.jna.NativeLong(c_iflag.longValue());
            copy.c_oflag = new com.sun.jna.NativeLong(c_oflag.longValue());
            copy.c_cflag = new com.sun.jna.NativeLong(c_cflag.longValue());
            copy.c_lflag = new com.sun.jna.NativeLong(c_lflag.longValue());
            copy.c_cc = c_cc.clone();
            copy.c_ispeed = new com.sun.jna.NativeLong(c_ispeed.longValue());
            copy.c_ospeed = new com.sun.jna.NativeLong(c_ospeed.longValue());
            return copy;
        }
    }
}