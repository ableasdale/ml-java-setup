import com.marklogic.support.beans.StatsTracker;
import com.marklogic.support.providers.MarkLogicContentSourceProvider;
import com.marklogic.support.util.Util;
import com.marklogic.xcc.Request;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestXCCReporting {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test
    void testGetCurrentStatsFromMarkLogic() throws Exception {
        Session s = MarkLogicContentSourceProvider.getInstance().getContentSource().newSession("PrimaryApplication");
        Request r = s.newAdhocQuery(new String(Files.readAllBytes(Paths.get("src/main/resources/create-stats-tracker-xml.xqy"))));
        ResultSequence rs = s.submitRequest(r);
        // LOG.info(rs.asString());

        StatsTracker st = Util.createStatsObjectFromXml(rs.asString());
        assertNotNull(st.getDateTimeOnServer());
        //LOG.info(st.getDateTimeOnServer());
    }


}
