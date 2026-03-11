package team8.catan.suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import team8.catan.gameplay.GameFactoryTest;
import team8.catan.gameplay.GameTest;

@Suite
@SelectClasses({
    GameTest.class,
    GameFactoryTest.class
})
public class GameplaySuite {
}
