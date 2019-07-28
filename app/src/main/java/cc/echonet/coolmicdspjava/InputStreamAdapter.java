package cc.echonet.coolmicdspjava;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamAdapter {
    private InputStream is;
    private boolean isEOF;

    public InputStreamAdapter(InputStream is) {
        this.is = is;
        this.isEOF = false;
    }

    private int callback(String task, byte[] buffer) {
        try {
            switch (task) {
                case "close":
                    is.close();
                    is = null;
                    break;
                case "read":
                    try {
                        int ret = is.read(buffer);

                        if (ret < buffer.length)
                            isEOF = true;

                        return ret;
                    } catch (EOFException e) {
                        isEOF = true;
                        return -1;
                    }
                case "eof":
                    return isEOF ? 1 : 0;
            }
        } catch (IOException e) {
            return -1;
        }

        return -1;
    }
}
