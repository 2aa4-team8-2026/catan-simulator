package team8.catan.io;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonParser {
    private final String text;
    private int index;

    private JsonParser(String text) {
        this.text = text;
    }

    public static Map<String, Object> parseObject(String text) {
        Object value = new JsonParser(text).parseValue();
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("Expected top-level JSON object");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> out = (Map<String, Object>) map;
        return out;
    }

    private Object parseValue() {
        skipWhitespace();
        if (index >= text.length()) {
            throw error("Unexpected end of input");
        }

        char current = text.charAt(index);
        return switch (current) {
            case '{' -> parseObjectValue();
            case '[' -> parseArrayValue();
            case '"' -> parseString();
            case 't' -> parseLiteral("true", Boolean.TRUE);
            case 'f' -> parseLiteral("false", Boolean.FALSE);
            case 'n' -> parseLiteral("null", null);
            default -> {
                if (current == '-' || Character.isDigit(current)) {
                    yield parseNumber();
                }
                throw error("Unexpected character '" + current + "'");
            }
        };
    }

    private Map<String, Object> parseObjectValue() {
        expect('{');
        Map<String, Object> out = new LinkedHashMap<>();
        skipWhitespace();
        if (consumeIf('}')) {
            return out;
        }

        while (true) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            expect(':');
            Object value = parseValue();
            out.put(key, value);
            skipWhitespace();
            if (consumeIf('}')) {
                return out;
            }
            expect(',');
        }
    }

    private List<Object> parseArrayValue() {
        expect('[');
        List<Object> out = new ArrayList<>();
        skipWhitespace();
        if (consumeIf(']')) {
            return out;
        }

        while (true) {
            out.add(parseValue());
            skipWhitespace();
            if (consumeIf(']')) {
                return out;
            }
            expect(',');
        }
    }

    private String parseString() {
        expect('"');
        StringBuilder out = new StringBuilder();
        while (index < text.length()) {
            char current = text.charAt(index++);
            if (current == '"') {
                return out.toString();
            }
            if (current != '\\') {
                out.append(current);
                continue;
            }
            if (index >= text.length()) {
                throw error("Unterminated escape sequence");
            }
            char escaped = text.charAt(index++);
            switch (escaped) {
                case '"', '\\', '/' -> out.append(escaped);
                case 'b' -> out.append('\b');
                case 'f' -> out.append('\f');
                case 'n' -> out.append('\n');
                case 'r' -> out.append('\r');
                case 't' -> out.append('\t');
                case 'u' -> out.append(parseUnicodeEscape());
                default -> throw error("Unsupported escape sequence \\" + escaped);
            }
        }
        throw error("Unterminated string");
    }

    private char parseUnicodeEscape() {
        if (index + 4 > text.length()) {
            throw error("Incomplete unicode escape");
        }
        String hex = text.substring(index, index + 4);
        index += 4;
        try {
            return (char) Integer.parseInt(hex, 16);
        } catch (NumberFormatException ex) {
            throw error("Invalid unicode escape: " + hex);
        }
    }

    private Object parseNumber() {
        int start = index;
        if (text.charAt(index) == '-') {
            index++;
        }
        consumeDigits();
        if (index < text.length() && text.charAt(index) == '.') {
            index++;
            consumeDigits();
        }
        if (index < text.length()) {
            char current = text.charAt(index);
            if (current == 'e' || current == 'E') {
                index++;
                if (index < text.length()) {
                    char sign = text.charAt(index);
                    if (sign == '+' || sign == '-') {
                        index++;
                    }
                }
                consumeDigits();
            }
        }

        String token = text.substring(start, index);
        try {
            if (token.contains(".") || token.contains("e") || token.contains("E")) {
                return Double.parseDouble(token);
            }
            return Long.parseLong(token);
        } catch (NumberFormatException ex) {
            throw error("Invalid number: " + token);
        }
    }

    private void consumeDigits() {
        int start = index;
        while (index < text.length() && Character.isDigit(text.charAt(index))) {
            index++;
        }
        if (start == index) {
            throw error("Expected digit");
        }
    }

    private Object parseLiteral(String literal, Object value) {
        if (!text.startsWith(literal, index)) {
            throw error("Expected '" + literal + "'");
        }
        index += literal.length();
        return value;
    }

    private void skipWhitespace() {
        while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
            index++;
        }
    }

    private boolean consumeIf(char expected) {
        if (index < text.length() && text.charAt(index) == expected) {
            index++;
            return true;
        }
        return false;
    }

    private void expect(char expected) {
        skipWhitespace();
        if (index >= text.length() || text.charAt(index) != expected) {
            throw error("Expected '" + expected + "'");
        }
        index++;
    }

    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException(message + " at index " + index);
    }
}
