package vn.truongngo.apartcom.one.lib.abac.servlet;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Wrapper around a cached request body byte array, allowing re-reading the input stream.
 * @author Truong Ngo
 */
public class CacheBodyServletInputStream extends ServletInputStream {

    private static final Logger log = LoggerFactory.getLogger(CacheBodyServletInputStream.class);

    private final InputStream cachedBodyInputStream;

    public CacheBodyServletInputStream(byte[] cacheBody) {
        this.cachedBodyInputStream = new ByteArrayInputStream(cacheBody);
    }

    @Override
    public boolean isFinished() {
        try {
            return cachedBodyInputStream.available() == 0;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int read() throws IOException {
        return cachedBodyInputStream.read();
    }
}
