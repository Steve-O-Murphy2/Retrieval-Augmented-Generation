package com.steveomurphy.tasters.rag;

public class ScoredChunk {

    private final Chunk chunk;
    private final double similarityScore;

    public ScoredChunk(Chunk chunk, double similarityScore) {
        this.chunk = chunk;
        this.similarityScore = similarityScore;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }
}