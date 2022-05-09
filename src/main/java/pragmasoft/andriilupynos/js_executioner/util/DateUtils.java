package pragmasoft.andriilupynos.js_executioner.util;

import java.util.Date;

public class DateUtils {

    private DateUtils() { }

    /**
     * Returns copy of the date or null if src is null
     */
    public static Date copyOrNull(Date src) {
        if (src == null) return null;

        return new Date(src.getTime());
    }

}
