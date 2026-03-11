package team8.catan.suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import team8.catan.actions.ActionExecutorsTest;

@Suite
@SelectClasses(ActionExecutorsTest.class)
public class ActionExecutorsSuite {
}
