package com.steveomurphy.tasters.rag;

import java.util.List;

/**
 * Stores embeddings as a mapping from a Chunk to its embeddings.
 */
public class EmbeddedChunk {

    /**
     * the chunk object
     */
    private final Chunk chunk;
    /**
     * the embeddings
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
     * retrieve the chunk
     * @return the chunk
     */
    public Chunk getChunk() {
        return chunk;
    }

    /**
     * retrieves the chunk's embeddings
     * @return the chunk's embeddings
     */
    public List<Float> getEmbedding() {
        return embedding;
    }
}
