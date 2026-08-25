
package com.steveomurphy.tasters.rag;

/**
 * Represents a file that is read. Contains a file name and content of that file.
 */
public class Document {
    /**
     * The file that the document is based on
     */
    private final String source;

    /**
     * The contents of the document.
     */
    private final String content;

    /**
     *
     * @param source File name
     * @param content File contents
     */

    public Document(String source, String content) {
        this.source = source;
        this.content = content;
    }

    /**
     *
     * @return File name
     */
    public String getSource() {
        return source;
    }

    /**
     *
     * @return File contents
     */
    public String getContent() {
        return content;
    }
}