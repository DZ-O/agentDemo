package com.openbutler.cli.terminal.rendering;

import com.openbutler.cli.terminal.model.Session;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * TableFormatter provides table formatting functionality using ASCII Table library.
 * 
 * This class formats session lists and other tabular data for display in the terminal.
 * 
 * Validates: Requirement 3.8
 */
public class TableFormatter {
    
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Formats a list of sessions as an ASCII table.
     * 
     * @param sessions the list of sessions to format
     * @return formatted table as a string
     * 
     * Validates: Requirement 3.8
     */
    public String formatSessionTable(List<Session> sessions) {
        if (sessions == null || sessions.isEmpty()) {
            return "No sessions found.";
        }
        
        AsciiTable table = new AsciiTable();
        
        // Add header row
        table.addRule();
        table.addRow("ID", "Name", "Messages", "Created", "Last Active", "Status");
        table.addRule();
        
        // Add session rows
        for (Session session : sessions) {
            table.addRow(
                truncateId(session.getId()),
                session.getName(),
                String.valueOf(session.getMessageCount()),
                formatTimestamp(session.getCreatedAt()),
                formatTimestamp(session.getLastActiveAt()),
                session.isActive() ? "Active" : "Inactive"
            );
            table.addRule();
        }
        
        // Set column alignment
        table.setTextAlignment(TextAlignment.LEFT);
        
        return table.render();
    }
    
    /**
     * Formats a generic table with headers and rows.
     * 
     * @param headers the column headers
     * @param rows the data rows
     * @return formatted table as a string
     * 
     * Validates: Requirement 3.8
     */
    public String formatTable(List<String> headers, List<List<String>> rows) {
        if (headers == null || headers.isEmpty()) {
            return "No data to display.";
        }
        
        AsciiTable table = new AsciiTable();
        
        // Add header row
        table.addRule();
        table.addRow(headers.toArray());
        table.addRule();
        
        // Add data rows
        if (rows != null) {
            for (List<String> row : rows) {
                table.addRow(row.toArray());
                table.addRule();
            }
        }
        
        // Set column alignment
        table.setTextAlignment(TextAlignment.LEFT);
        
        return table.render();
    }
    
    /**
     * Truncates a session ID to the first 8 characters for display.
     * 
     * @param id the full session ID
     * @return truncated ID
     */
    private String truncateId(String id) {
        if (id == null) {
            return "N/A";
        }
        return id.length() > 8 ? id.substring(0, 8) : id;
    }
    
    /**
     * Formats a LocalDateTime as a readable timestamp.
     * 
     * @param timestamp the timestamp to format
     * @return formatted timestamp string
     */
    private String formatTimestamp(LocalDateTime timestamp) {
        if (timestamp == null) {
            return "N/A";
        }
        return timestamp.format(TIMESTAMP_FORMATTER);
    }
}
