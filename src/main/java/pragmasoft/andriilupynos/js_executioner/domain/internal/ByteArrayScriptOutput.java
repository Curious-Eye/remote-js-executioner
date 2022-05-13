package pragmasoft.andriilupynos.js_executioner.domain.internal;

import pragmasoft.andriilupynos.js_executioner.domain.ScriptInfo;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class ByteArrayScriptOutput implements ScriptInfo.ScriptOutput {

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    @Override
    public OutputStream asStream() {
        return this.outputStream;
    }

    @Override
    public String toString() {
        return this.outputStream.toString();
    }
}
