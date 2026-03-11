package team8.catan.suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import team8.catan.configuration.GameConfigTest;
import team8.catan.configuration.JsonLoaderTest;
import team8.catan.players.HumanCommandParserTest;
import team8.catan.players.ResourceHandTest;

@Suite
@SelectClasses({
    GameConfigTest.class,
    JsonLoaderTest.class,
    HumanCommandParserTest.class,
    ResourceHandTest.class
})
public class ConfigurationAndResourcesSuite {
}
