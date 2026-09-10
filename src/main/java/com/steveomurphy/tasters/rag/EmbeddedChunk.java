package com.steveomurphy.tasters.rag;

import java.util.List;

/**
 * Associates a Chunk with its embedding vector.
 */
public class EmbeddedChunk {

    /**
     * The document chunk
     */
    private final Chunk chunk;
    /**
     * The chunk's embedding vector
     */
    private final List<Float> embedding;

    /**
     * Constructor. Creates the mapping structure
     * @param chunk
     * @param embedding
     */
    public EmbeddedChunk(Chunk chunk, List<Float> embedding) {
        this.chunk = chunk;
        this.embedding = embedding;
    }

    /**
     * Returns the document chunk
     * @return the document chunk
     */
    public Chunk getChunk() {
        return chunk;
    }

    /**
     * Returns the chunk's embedding vector
     * @return the chunk's embedding vector
     */
    public List<Float> getEmbedding() {
        return embedding;
    }
}
