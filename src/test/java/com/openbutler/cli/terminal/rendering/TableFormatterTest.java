package com.openbutler.cli.terminal.rendering;

import com.openbutler.cli.terminal.model.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TableFormatter.
 * 
 * Tests verify that the table formatter correctly formats session lists
 * and generic tables using the ASCII Table library.
 * 
 * Validates: Requirement 3.8
 */
class TableFormatterTest {
    
    private TableFormatter tableFormatter;
    
    @BeforeEach
    void setUp() {
        tableFormatter = new TableFormatter();
    }
    
    /**
     * Test that formatSessionTable displays all session information.
     * Validates: Requirement 3.8
     */
    @Test
    void testFormatSessionTableDisplaysAllInformation() {
        LocalDateTime now = LocalDateTime.now();
        Session session = Session.builder()
                .id("550e8400-e29b-41d4-a716-446655440000")
                .name("Test Session")
                .messageCount(42)
                .createdAt(now)
                .lastActiveAt(now)
                .active(true)
                .build();
        
        String table = tableFormatter.formatSessionTable(Collections.singletonList(session));
        
        assertNotNull(table, "Table should not be null");
        assertTrue(table.contains("550e8400"), "Table should contain truncated ID");
        assertTrue(table.contains("Test Session"), "Table should contain session name");
        assertTrue(table.contains("42"), "Table should contain message count");
        assertTrue(table.contains("Active"), "Table should contain active status");
    }
    
    /**
     * Test that formatSessionTable handles multiple sessions.
     * Validates: Requirement 3.8
     */
    @Test
    void testFormatSessionTableHandlesMultipleSessions() {
        LocalDateTime now = LocalDateTime.now();
        List<Session> sessions = Arrays.asList(
                Session.builder()
                        .id("session-1")
                        .name("Session 1")
                        .messageCount(10)
                        .createdAt(now)
                        .lastActiveAt(now)
                        .active(true)
                        .build(),
                Session.builder()
                        .id("session-2")
                        .name("Session 2")
                        .messageCount(20)
                        .createdAt(now)
                        .lastActiveAt(now)
                        .active(false)
                        .build()
        );
        
        String table = tableFormatter.formatSessionTable(sessions);
        
        assertNotNull(table, "Table should not be null");
        assertTrue(table.contains("Session 1"), "Table should contain first session");
        assertTrue(table.contains("Session 2"), "Table should contain second session");
        assertTrue(table.contains("10"), "Table should contain first session message count");
        assertTrue(table.contains("20"), "Table should contain second session message count");
        assertTrue(table.contains("Active"), "Table should contain active status");
        assertTrue(table.contains("Inactive"), "Table should contain inactive status");
    }
    
    /**
     * Test that formatSessionTable handles empty list.
     * Validates: Requirement 3.8
     */
    @Test
    void testFormatSessionTableHandlesEmptyList() {
        String table = tableFormatter.formatSessionTable(Collections.emptyList());
        
        assertNotNull(table, "Table should not be null");
        assertEquals("No sessions found.", table, "Should return empty message");
    }
    
    /**
     * Test that formatSessionTable handles null list.
     * Validates: Requirement 3.8
     */
    @Test
    void testFormatSessionTableHandlesNullList() {
        String table = tableFormatter.formatSessionTable(null);
        
        assertNotNull(table, "Table should not be null");
        assertEquals("No sessions found.", table, "Should return empty message");
    }
    
    /**
     * Test that formatSessionTable includes table headers.
     * Validates: Requirement 3.8
     */
    @Test
    void testFormatSessionTableIncludesHeaders() {
        LocalDateTime now = LocalDateTime.now();
        Session session = Session.builder()
                .id("test-id")
                .name("Test")
                .messageCount(0)
                .createdAt(now)
                .lastActiveAt(now)
                .active(false)
                .build();
        
        String table = tableFormatter.formatSessionTable(Collections.singletonList(session));
        
        assertTrue(table.contains("ID"), "Table should contain ID header");
        assertTrue(table.contains("Name"), "Table should contain Name header");
        assertTrue(table.contains("Messages"), "Table should contain Messages header");
        assertTrue(table.contains("Created"), "Table should contain Created header");
        assertTrue(table.contains("Last Active"), "Table should contain Last Active header");
        assertTrue(table.contains("Status"), "Table should contain Status header");
    }
    
    /**
     * Test that formatSessionTable truncates long IDs.
     * Validates: Requirement 3.8
     */
    @Test
    void testFormatSessionTableTruncatesLongIds() {
        LocalDateTime now = LocalDateTime.now();
        Session session = Session.builder()
                .id("very-long-session-id-that-should-be-truncated")
                .name("Test")
                .messageCount(0)
                .createdAt(now)
                .lastActiveAt(now)
                .active(false)
                .build();
        
        String table = tableFormatter.formatSessionTable(Collections.singletonList(session));
        
        assertTrue(table.contains("very-lon"), "Table should contain truncated ID");
        assertFalse(table.contains("very-long-session-id-that-should-be-truncated"), 
                    "Table should not contain full long ID");
    }
    
    /**
     * Test that formatSessionTable handles null timestamps.
     * Validates: Requirement 3.8
     */
    @Test
    void testFormatSessionTableHandlesNullTimestamps() {
        Session session = Session.builder()
                .id("test-id")
                .name("Test")
                .messageCount(0)
                .createdAt(null)
                .lastActiveAt(null)
                .active(false)
                .build();
        
        String table = tableFormatter.formatSessionTable(Collections.singletonList(session));
        
        assertNotNull(table, "Table should not be null");
        assertTrue(table.contains("N/A"), "Table should contain N/A for null timestamps");
    }
    
    /**
     * Test that formatSessionTable formats timestamps correctly.
     * Validates: Requirement 3.8
     */
    @Test
    void testFormatSessionTableFormatsTimestamps() {
        LocalDateTime timestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
        Session session = Session.builder()
                .id("test-id")
                .name("Test")
                .messageCount(0)
                .createdAt(timestamp)
                .lastActiveAt(timestamp)
                .active(false)
                .build();
        
        String table = tableFormatter.formatSessionTable(Collections.singletonList(session));
        
        assertTrue(table.contains("2024-01-15"), "Table should contain formatted date");
        assertTrue(table.contains("10:30:45"), "Table should contain formatted time");
    }
    
    /**
     * Test that formatTable displays generic table data.
     * Validates: Requirement 3.8
     */
    @Test
    void testFormatTableDisplaysGenericData() {
        List<String> headers = Arrays.asList("Column1", "Column2", "Column3");
        List<List<String>> rows = Arrays.asList(
                Arrays.asList("A", "B", "C"),
                Arrays.asList("D", "E", "F")
        );
        
        String table = tableFormatter.formatTable(headers, rows);
        
        assertNotNull(table, "Table should not be null");
        assertTrue(table.contains("Column1"), "Table should contain first header");
        assertTrue(table.contains("Column2"), "Table should contain second header");
        assertTrue(table.contains("Column3"), "Table should contain third header");
        assertTrue(table.contains("A"), "Table should contain first row data");
        assertTrue(table.contains("D"), "Table should contain second row data");
    }
    
    /**
     * Test that formatTable handles empty rows.
     * Validates: Requirement 3.8
     */
    @Test
    void testFormatTableHandlesEmptyRows() {
        List<String> headers = Arrays.asList("Column1", "Column2");
        List<List<String>> rows = Collections.emptyList();
        
        String table = tableFormatter.formatTable(headers, rows);
        
        assertNotNull(table, "Table should not be null");
        assertTrue(table.contains("Column1"), "Table should contain headers even with empty rows");
        assertTrue(table.contains("Column2"), "Table should contain headers even with empty rows");
    }
    
    /**
     * Test that formatTable handles null rows.
     * Validates: Requirement 3.8
     */
    @Test
    void testFormatTableHandlesNullRows() {
        List<String> headers = Arrays.asList("Column1", "Column2");
        
        String table = tableFormatter.formatTable(headers, null);
        
        assertNotNull(table, "Table should not be null");
        assertTrue(table.contains("Column1"), "Table should contain headers even with null rows");
    }
    
    /**
     * Test that formatTable handles null headers.
     * Validates: Requirement 3.8
     */
    @Test
    void testFormatTableHandlesNullHeaders() {
        String table = tableFormatter.formatTable(null, null);
        
        assertNotNull(table, "Table should not be null");
        assertEquals("No data to display.", table, "Should return no data message");
    }
    
    /**
     * Test that formatTable handles empty headers.
     * Validates: Requirement 3.8
     */
    @Test
    void testFormatTableHandlesEmptyHeaders() {
        String table = tableFormatter.formatTable(Collections.emptyList(), null);
        
        assertNotNull(table, "Table should not be null");
        assertEquals("No data to display.", table, "Should return no data message");
    }
    
    /**
     * Test that formatTable includes table borders.
     * Validates: Requirement 3.8
     */
    @Test
    void testFormatTableIncludesBorders() {
        List<String> headers = Arrays.asList("Col1", "Col2");
        List<List<String>> rows = Arrays.asList(
                Arrays.asList("A", "B")
        );
        
        String table = tableFormatter.formatTable(headers, rows);
        
        // ASCII Table uses various border characters, check for common ones
        boolean hasBorders = table.contains("+") || table.contains("─") || 
                           table.contains("│") || table.contains("|") ||
                           table.contains("═") || table.contains("║");
        assertTrue(hasBorders, "Table should contain border characters");
        assertFalse(table.trim().isEmpty(), "Table should not be empty");
    }
}
