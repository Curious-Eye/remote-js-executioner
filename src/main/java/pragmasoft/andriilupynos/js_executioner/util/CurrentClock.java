package pragmasoft.andriilupynos.js_executioner.util;

import java.time.Clock;

public class CurrentClock {

    private static Clock clock = Clock.systemUTC();

    private CurrentClock() {}

    public static Clock get() {
        return clock;
    }

    public static void set(Clock clock) {
        CurrentClock.clock = clock;
    }

}
