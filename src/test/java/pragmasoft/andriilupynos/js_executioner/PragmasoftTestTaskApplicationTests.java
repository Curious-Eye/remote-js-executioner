package pragmasoft.andriilupynos.js_executioner;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PragmasoftTestTaskApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    public void fixedThreadPool() {
        final Engine engine = newEngineBuilder().build();
        final ExecutorService pool = Executors.newFixedThreadPool(4);

        try {
            final ThreadLocal<Context> tl = ThreadLocal.withInitial(
                    () -> newContextBuilder().engine(engine).build()
            );

            Set<Callable<Value>> tasks = new HashSet<>();
            for (int i = 0; i < 42; i++) {
                tasks.add(() -> {
                    Context cx = tl.get();
                    cx.enter();
                    try {
                        return cx.eval("js", "42;");
                    } finally {
                        cx.leave();
                    }
                });
            }

            try {
                for (Future<Value> v : pool.invokeAll(tasks)) {
                    assertEquals(v.get().asInt(), 42);
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new AssertionError(e);
            }
        } finally {
            engine.close();
            pool.shutdown();
        }
    }

    private Runnable tmp(Engine engine, String code) {
        final ThreadLocal<Context> tl = ThreadLocal.withInitial(
                () -> newContextBuilder().engine(engine).build()
        );
        return () -> {
            Context cx = tl.get();
            cx.enter();
            try {
                var parsed = cx.parse("js", code);
                parsed.executeVoid();
                cx.eval("js", "42;");
            } finally {
                cx.leave();
            }
        };
    }

    // Duplication from JSTest
    static Context.Builder newContextBuilder() {
        return Context.newBuilder("js").allowExperimentalOptions(true);
    }

    // Duplication from JSTest
    static Engine.Builder newEngineBuilder() {
        return Engine.newBuilder().allowExperimentalOptions(true);
    }

}
