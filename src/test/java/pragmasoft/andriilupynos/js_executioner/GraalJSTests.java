package pragmasoft.andriilupynos.js_executioner;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class GraalJSTests {

    /**
     * We could potentially reuse same engine for multiple contexts,
     * if we could prove that setting streams on one context,
     * does not affect streams of the other
     */
    @Test
    void multipleContextsShouldBeAbleToShareTheSameEngineThreadsafe() {
        // GIVEN
        var engine = Engine.newBuilder().build();
        var out1 = new ByteArrayOutputStream();
        var out2 = new ByteArrayOutputStream();
        var ctx1 = Context.newBuilder("js")
                .engine(engine)
                .out(out1)
                .build();
        var ctx2 = Context.newBuilder("js")
                .engine(engine)
                .out(out2)
                .build();

        // WHEN
        // First eval demonstrating that different streams were used
        ctx1.eval("js", "print('First context')");
        ctx2.eval("js", "print('Second context')");
        // THEN
        assertEquals("First context\n", out1.toString());
        assertEquals("Second context\n", out2.toString());

        // WHEN
        // Second eval in case there was some lazy setting of properties
        ctx1.eval("js", "print('First context')");
        ctx2.eval("js", "print('Second context')");
        // THEN
        assertEquals("First context\nFirst context\n", out1.toString());
        assertEquals("Second context\nSecond context\n", out2.toString());
    }

}
