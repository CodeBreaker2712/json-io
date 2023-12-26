package com.cedarsoftware.util.io;

import java.io.IOException;
import java.io.Reader;

import lombok.Getter;

public class FastReader extends Reader {
    private Reader in;
    private char[] buf;
    private int bufferSize;
    private int pushbackBufferSize;
    private int position; // Current position in the buffer
    private int limit; // Number of characters currently in the buffer
    private char[] pushbackBuffer;
    private int pushbackPosition; // Current position in the pushback buffer
    @Getter
    protected int line = 1;
    @Getter
    protected int col = 0;

    public FastReader(Reader in, int bufferSize, int pushbackBufferSize) {
        super(in);
        if (bufferSize <= 0 || pushbackBufferSize < 0) {
            throw new IllegalArgumentException("Buffer sizes must be positive");
        }
        this.in = in;
        this.bufferSize = bufferSize;
        this.pushbackBufferSize = pushbackBufferSize;
        this.buf = new char[bufferSize];
        this.pushbackBuffer = new char[pushbackBufferSize];
        this.position = 0;
        this.limit = 0;
        this.pushbackPosition = pushbackBufferSize; // Start from the end of pushbackBuffer
    }

    private void fill() throws IOException {
        if (position >= limit) {
            limit = in.read(buf, 0, bufferSize);
            if (limit > 0) {
                position = 0;
            }
        }
    }

    public void pushback(char ch) throws IOException {
        if (pushbackPosition == 0) {
            throw new IOException("Pushback buffer overflow");
        }
        pushbackBuffer[--pushbackPosition] = ch;
        if (ch == 0x0a) {
            line--;
        }
        else {
            col--;
        }
    }

    protected void movePosition(char ch)
    {
        if (ch == 0x0a) {
            line++;
            col = 0;
        }
        else {
            col++;
        }
    }

    public int read() throws IOException {
        if (in == null) {
            throw new IOException("FastReader stream is closed.");
        }
        char ch;
        if (pushbackPosition < pushbackBufferSize) {
            ch = pushbackBuffer[pushbackPosition++];
            movePosition(ch);
            return ch;
        }

        fill();
        if (limit == -1) {
            return -1;
        }

        ch = buf[position++];
        movePosition(ch);
        return ch;
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        if (in == null) {
            throw new IOException("FastReader stream is closed.");
        }
        int bytesRead = 0;

        while (len > 0) {
            int available = pushbackBufferSize - pushbackPosition;
            if (available > 0) {
                int toRead = Math.min(available, len);
                System.arraycopy(pushbackBuffer, pushbackPosition, cbuf, off, toRead);
                pushbackPosition += toRead;
                off += toRead;
                len -= toRead;
                bytesRead += toRead;
            } else {
                fill();
                if (limit == -1) {
                    return bytesRead > 0 ? bytesRead : -1;
                }
                int toRead = Math.min(limit - position, len);
                System.arraycopy(buf, position, cbuf, off, toRead);
                position += toRead;
                off += toRead;
                len -= toRead;
                bytesRead += toRead;
            }
        }

        return bytesRead;
    }

    public void close() throws IOException {
        if (in != null) {
            in.close();
            in = null;
        }
    }

    public String getLastSnippet()
    {
        StringBuilder s = new StringBuilder();
        for (int i=0; i < position; i++)
        {
            s.append(buf[i]);
        }
        return s.toString();
    }
}