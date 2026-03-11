package team8.catan.suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import team8.catan.board.BoardTest;
import team8.catan.rules.RuleCheckerTest;
import team8.catan.rules.SettlementRoadConnectionRuleModuleTest;

@Suite
@SelectClasses({
    BoardTest.class,
    RuleCheckerTest.class,
    SettlementRoadConnectionRuleModuleTest.class
})
public class BoardAndRulesSuite {
}
