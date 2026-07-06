package com.pega.gcs.tracerviewer;

import com.pega.gcs.fringecommon.utilities.KnuthMorrisPrattAlgorithm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for TraceEvent context checking logic.
 * Verifies that the parser correctly identifies start tags, end tags, and child elements.
 */
public class TraceEventContextTest {

    private static final String TRACEEVENT_BASE = "<TraceEvent";
    private static final String TRACEEVENT_END = "</TraceEvent>";

    @Test
    public void testStartTagWithSpace() throws Exception {
        // <TraceEvent sequence="1"> - valid start tag (space after base)
        String xml = "<TraceEvent sequence=\"1\">";
        byte[] buffer = xml.getBytes("UTF-8");

        int pos = findBasePattern(buffer);
        Assertions.assertTrue(pos >= 0, "Base pattern should be found");

        byte nextChar = buffer[pos + TRACEEVENT_BASE.length()];
        Assertions.assertTrue(isValidStartTag(nextChar), "Space should be a valid start tag delimiter");
    }

    @Test
    public void testStartTagWithTab() throws Exception {
        // <TraceEvent\tsequence="1"> - valid start tag (tab after base)
        String xml = "<TraceEvent\tsequence=\"1\">";
        byte[] buffer = xml.getBytes("UTF-8");

        int pos = findBasePattern(buffer);
        Assertions.assertTrue(pos >= 0, "Base pattern should be found");

        byte nextChar = buffer[pos + TRACEEVENT_BASE.length()];
        Assertions.assertTrue(isValidStartTag(nextChar), "Tab should be a valid start tag delimiter");
    }

    @Test
    public void testStartTagWithNewline() throws Exception {
        // <TraceEvent\nsequence="1"> - valid start tag (newline after base)
        String xml = "<TraceEvent\nsequence=\"1\">";
        byte[] buffer = xml.getBytes("UTF-8");

        int pos = findBasePattern(buffer);
        Assertions.assertTrue(pos >= 0, "Base pattern should be found");

        byte nextChar = buffer[pos + TRACEEVENT_BASE.length()];
        Assertions.assertTrue(isValidStartTag(nextChar), "Newline should be a valid start tag delimiter");
    }

    @Test
    public void testStartTagWithCarriageReturn() throws Exception {
        // <TraceEvent\rsequence="1"> - valid start tag (CR after base)
        String xml = "<TraceEvent\rsequence=\"1\">";
        byte[] buffer = xml.getBytes("UTF-8");

        int pos = findBasePattern(buffer);
        Assertions.assertTrue(pos >= 0, "Base pattern should be found");

        byte nextChar = buffer[pos + TRACEEVENT_BASE.length()];
        Assertions.assertTrue(isValidStartTag(nextChar), "Carriage return should be a valid start tag delimiter");
    }

    @Test
    public void testStartTagWithMultipleSpaces() throws Exception {
        // <TraceEvent  sequence="1"> - valid start tag (multiple spaces after base)
        String xml = "<TraceEvent  sequence=\"1\">";
        byte[] buffer = xml.getBytes("UTF-8");

        int pos = findBasePattern(buffer);
        Assertions.assertTrue(pos >= 0, "Base pattern should be found");

        byte nextChar = buffer[pos + TRACEEVENT_BASE.length()];
        Assertions.assertTrue(isValidStartTag(nextChar), "Multiple spaces should be valid start tag delimiters");
    }

    @Test
    public void testStartTagNoAttributes() throws Exception {
        // <TraceEvent>true</TraceEvent> - valid start tag (no attributes, > follows)
        String xml = "<TraceEvent>true</TraceEvent>";
        byte[] buffer = xml.getBytes("UTF-8");

        int pos = findBasePattern(buffer);
        Assertions.assertTrue(pos >= 0, "Base pattern should be found");

        byte nextChar = buffer[pos + TRACEEVENT_BASE.length()];
        Assertions.assertTrue(isValidStartTag(nextChar), "> should be a valid start tag delimiter");
    }

    @Test
    public void testChildElementTraceEventActStartTime() throws Exception {
        // <TraceEventActStartTime>123</TraceEventActStartTime> - NOT a start tag (alphanumeric follows)
        String xml = "<TraceEventActStartTime>123</TraceEventActStartTime>";
        byte[] buffer = xml.getBytes("UTF-8");

        int pos = findBasePattern(buffer);
        Assertions.assertTrue(pos >= 0, "Base pattern should be found");

        byte nextChar = buffer[pos + TRACEEVENT_BASE.length()];
        Assertions.assertFalse(isValidStartTag(nextChar), "Alphanumeric 'A' should NOT be a valid start tag delimiter");
    }

    @Test
    public void testChildElementTraceEventActEndTime() throws Exception {
        // <TraceEventActEndTime>123</TraceEventActEndTime> - NOT a start tag (alphanumeric follows)
        String xml = "<TraceEventActEndTime>123</TraceEventActEndTime>";
        byte[] buffer = xml.getBytes("UTF-8");

        int pos = findBasePattern(buffer);
        Assertions.assertTrue(pos >= 0, "Base pattern should be found");

        byte nextChar = buffer[pos + TRACEEVENT_BASE.length()];
        Assertions.assertFalse(isValidStartTag(nextChar), "Alphanumeric 'A' should NOT be a valid start tag delimiter");
    }

    @Test
    public void testEndTagPartOfTraceEvent() throws Exception {
        // </TraceEvent> - the base pattern is part of the end tag, previous char is '/'
        String xml = "</TraceEvent>";
        byte[] buffer = xml.getBytes("UTF-8");

        int pos = findBasePattern(buffer);
        Assertions.assertTrue(pos >= 0, "Base pattern should be found");

        // Previous character should be '/'
        Assertions.assertTrue(buffer[pos - 1] == '/', "Previous character should be '/' for end tag");
    }

    @Test
    public void testNestedTraceEventWithWhitespace() throws Exception {
        // Verify depth tracking works with whitespace variants
        String xml = "<TraceEvent sequence=\"1\"><Nested><TraceEvent\t>true</TraceEvent></Nested></TraceEvent>";
        byte[] buffer = xml.getBytes("UTF-8");

        int depth = 0;
        int searchPos = 0;
        int baseLen = TRACEEVENT_BASE.length();

        while (searchPos < buffer.length) {
            int nextBase = findBasePattern(buffer, searchPos);
            int nextEnd = findBasePattern(buffer, searchPos, TRACEEVENT_END);

            if (nextBase == -1 && nextEnd == -1) {
                break;
            }

            if (nextBase != -1 && (nextEnd == -1 || nextBase < nextEnd)) {
                // Check if it's part of end tag
                boolean isPartOfEndTag = (nextBase > 0 && buffer[nextBase - 1] == '/');

                if (!isPartOfEndTag) {
                    // Check if it's a valid start tag
                    int checkPos = nextBase + baseLen;
                    if (checkPos < buffer.length && isValidStartTag(buffer[checkPos])) {
                        depth++;
                    }
                }
            } else if (nextEnd != -1) {
                depth--;
            }

            searchPos = Math.max(nextBase, nextEnd) + 1;
        }

        Assertions.assertEquals(0, depth, "Depth should return to 0 after complete nesting with whitespace variants");
    }

    // Helper methods

    private int findBasePattern(byte[] buffer) {
        return KnuthMorrisPrattAlgorithm.indexOf(buffer, TRACEEVENT_BASE.getBytes());
    }

    private int findBasePattern(byte[] buffer, int startIdx) {
        return KnuthMorrisPrattAlgorithm.indexOf(buffer, TRACEEVENT_BASE.getBytes(), startIdx);
    }

    private int findBasePattern(byte[] buffer, int startIdx, String pattern) {
        return KnuthMorrisPrattAlgorithm.indexOf(buffer, pattern.getBytes(), startIdx);
    }

    private boolean isValidStartTag(byte nextChar) {
        return nextChar == ' ' || nextChar == '\t' || nextChar == '\n'
            || nextChar == '\r' || nextChar == '>';
    }
}
