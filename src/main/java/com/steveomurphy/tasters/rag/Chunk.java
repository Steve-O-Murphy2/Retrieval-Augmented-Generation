package com.steveomurphy.tasters.rag;

/**
 * Represents a document broken into parts (Chunks)
 */
public class Chunk {

    /**
     *
     */
    private final String source;
    private final String content;

    public Chunk(String source, String content) {
        this.source = source;
        this.content = content;
    }

    public String getSource() {
        return source;
    }

    public String getContent() {
        return content;
    }
}