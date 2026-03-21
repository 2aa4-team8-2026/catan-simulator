package team8.catan.suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import team8.catan.gameplay.GameFactoryTest;
import team8.catan.gameplay.GameTest;
import team8.catan.gameplay.commands.BuildCityCommandTest;
import team8.catan.gameplay.commands.BuildRoadCommandTest;
import team8.catan.gameplay.commands.BuildSettlementCommandTest;
import team8.catan.gameplay.commands.CompositeCommandTest;
import team8.catan.gameplay.commands.CommandHistoryTest;
import team8.catan.gameplay.commands.ResourceDistributionCommandTest;
import team8.catan.gameplay.commands.TurnCommandTest;
import team8.catan.gameplay.commands.TurnResolutionCommandTest;
import team8.catan.logging.ConsoleActionLoggerTest;
import team8.catan.players.HumanPlayerTest;
import team8.catan.players.RandomAgentTest;
import team8.catan.players.strategy.ImmediateValueSelectionPolicyTest;

@Suite
@SelectClasses({
    BuildCityCommandTest.class,
    BuildRoadCommandTest.class,
    BuildSettlementCommandTest.class,
    CompositeCommandTest.class,
    CommandHistoryTest.class,
    ResourceDistributionCommandTest.class,
    TurnCommandTest.class,
    TurnResolutionCommandTest.class,
    HumanPlayerTest.class,
    ImmediateValueSelectionPolicyTest.class,
    RandomAgentTest.class,
    ConsoleActionLoggerTest.class,
    GameTest.class,
    GameFactoryTest.class
})
public class GameplaySuite {
}
