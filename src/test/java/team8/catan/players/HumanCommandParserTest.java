package team8.catan.players;

import org.junit.jupiter.api.Test;
import team8.catan.players.HumanCommand.HumanCommandType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class HumanCommandParserTest {
    private final HumanCommandParser parser = new HumanCommandParser();

    @Test
    void parse_fullSettlementCommand_allowsMixedCaseAndWhitespace() {
        HumanCommand command = parser.parse("  BuIlD   settlement   12  ");

        assertCommand(command, HumanCommandType.BUILD_SETTLEMENT, 12, null, null, null);
    }

    @Test
    void parse_fullCityCommand_returnsBuildCityCommand() {
        HumanCommand command = parser.parse("build city 7");

        assertCommand(command, HumanCommandType.BUILD_CITY, 7, null, null, null);
    }

    @Test
    void parse_fullRoadCommand_withCommaSeparatedEndpoints_returnsBuildRoadCommand() {
        HumanCommand command = parser.parse("build road 3, 9");

        assertCommand(command, HumanCommandType.BUILD_ROAD, null, 3, 9, null);
    }

    @Test
    void parse_fullRoadCommand_withSpaceSeparatedEndpoints_returnsBuildRoadCommand() {
        HumanCommand command = parser.parse("build road 3 9");

        assertCommand(command, HumanCommandType.BUILD_ROAD, null, 3, 9, null);
    }

    @Test
    void parse_shortSettlementCommand_withSingleLetterTarget_returnsBuildSettlementCommand() {
        HumanCommand command = parser.parse("b s 5");

        assertCommand(command, HumanCommandType.BUILD_SETTLEMENT, 5, null, null, null);
    }

    @Test
    void parse_shortSettlementCommand_withLongKeyword_returnsBuildSettlementCommand() {
        HumanCommand command = parser.parse("b settlement 5");

        assertCommand(command, HumanCommandType.BUILD_SETTLEMENT, 5, null, null, null);
    }

    @Test
    void parse_shortCityCommand_withSingleLetterTarget_returnsBuildCityCommand() {
        HumanCommand command = parser.parse("b c 8");

        assertCommand(command, HumanCommandType.BUILD_CITY, 8, null, null, null);
    }

    @Test
    void parse_shortRoadCommand_withCommaSeparatedEndpoints_returnsBuildRoadCommand() {
        HumanCommand command = parser.parse("b r 4,11");

        assertCommand(command, HumanCommandType.BUILD_ROAD, null, 4, 11, null);
    }

    @Test
    void parse_shortRoadCommand_withSpaceSeparatedEndpoints_returnsBuildRoadCommand() {
        HumanCommand command = parser.parse("b road 4 11");

        assertCommand(command, HumanCommandType.BUILD_ROAD, null, 4, 11, null);
    }

    @Test
    void parse_malformedBuildCommand_returnsInvalidCommand() {
        HumanCommand command = parser.parse("build road 4");

        assertCommand(
            command,
            HumanCommandType.INVALID,
            null,
            null,
            null,
            "Unknown command. Use b, ls, undo, redo, or Enter(go)."
        );
    }

    @Test
    void parse_undoCommand_returnsUndoCommand() {
        HumanCommand command = parser.parse("undo");

        assertCommand(command, HumanCommandType.UNDO, null, null, null, null);
    }

    @Test
    void parse_redoCommand_returnsRedoCommand() {
        HumanCommand command = parser.parse("redo");

        assertCommand(command, HumanCommandType.REDO, null, null, null, null);
    }

    private static void assertCommand(
        HumanCommand command,
        HumanCommandType expectedType,
        Integer expectedNodeId,
        Integer expectedFromNodeId,
        Integer expectedToNodeId,
        String expectedError
    ) {
        assertEquals(expectedType, command.getType());
        assertEquals(expectedNodeId, command.getNodeId());
        assertEquals(expectedFromNodeId, command.getFromNodeId());
        assertEquals(expectedToNodeId, command.getToNodeId());
        if (expectedError == null) {
            assertNull(command.getError());
            return;
        }
        assertEquals(expectedError, command.getError());
    }
}
