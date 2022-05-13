package pragmasoft.andriilupynos.js_executioner.domain;

import java.io.OutputStream;

/**
 * Anti-corruption layer around GraalJS parsed Value
 */
public abstract class Script implements Runnable, AutoCloseable {

    public final String code;
    protected final OutputStream out;
    protected final OutputStream err;

    protected Script(String code, OutputStream out, OutputStream err) {
        this.code = code;
        this.out = out;
        this.err = err;
    }

}

