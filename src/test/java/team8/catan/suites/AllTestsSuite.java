package team8.catan.suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
    ConfigurationAndResourcesSuite.class,
    BoardAndRulesSuite.class,
    ActionExecutorsSuite.class,
    GameplaySuite.class
})
public class AllTestsSuite {
}
