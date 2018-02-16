import com.marklogic.support.beans.StatsTracker;
import com.marklogic.support.util.Util;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Display {

    @Test
    void testDateDisplay() {
        String date = "2018-02-16T12:58:29.513456Z";
        assertEquals(10, date.indexOf('T'));
        assertEquals("12:58:29", Util.extractTimeFromDateTime(date));
    }
}
