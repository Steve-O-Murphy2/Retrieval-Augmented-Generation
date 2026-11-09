package com.steveomurphy.tasters.rag;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Driver program
 * <p>Maintains a list of embedded chunks that acts as a simplified in-memory vector store</p>
 * <p>Collects embeddings for all document chunks.</p>
 */
public class Main {

    /**
     * @param args Standard command line args. Not used
     *  <p>Using <code>java.nio.file.Path</code>, <code>Paths</code>, and <code>Files</code> functionality, does the following for each source document:</p>
     *  <ol>
     *  <li>Reads the document contents.</li>
     *  <li>Creates a <code>Document</code> object from the file name and contents.</li>
     *  <li>Using a <code>Chunker</code> object, breaks the document contents into Chunks.</li>
     *  <li>Creates an embedding for each chunk.</li>
     *  <li>Associates each chunk with its embedding.</li>
     *  <li>Adds the EmbeddedChunk to the in-memory vector store.</li>
     *  <li>Prints information about the chunk and its embeddings.</li>
     *  </ol>
     */

    public static void main(String[] args) {

        Path docsPath = Paths.get("src/main/resources/docs");

        Chunker chunker = new Chunker();
        EmbeddingService embeddingService = new EmbeddingService();
        SimilarityService similarityService = new SimilarityService();

        // List to collect all chunks and their embeddings.
        // This acts as a simplified in-memory vector store.
        List<EmbeddedChunk> embeddedChunks = new ArrayList<>();

        // Examine each source file, chunk it, and get its embeddings.
        try (Stream<Path> paths = Files.list(docsPath)) {

            paths
                    .filter(Files::isRegularFile)
                    .forEach(path -> {

                        try {
                            String content = Files.readString(path);

                            Document document =
                                    new Document(
                                            path.getFileName().toString(),
                                            content
                                    );

                            List<Chunk> chunks = chunker.chunk(document);

                            System.out.println(
                                    "===== " + document.getSource() + " ====="
                            );

                            // Create the embedding for the chunk.
                            for (Chunk chunk : chunks) {

                                List<Float> embedding =
                                        embeddingService.createEmbedding(
                                                chunk.getContent()
                                        );

                                // Associate the chunk with its embedding.
                                EmbeddedChunk embeddedChunk =
                                        new EmbeddedChunk(chunk, embedding);

                                // Add the embedded chunk to the in-memory vector store.
                                embeddedChunks.add(embeddedChunk);

                                System.out.println("--- CHUNK ---");
                                System.out.println(chunk.getContent());
                                System.out.println(
                                        "Embedding dimensions: " + embedding.size()
                                );
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

            System.out.println();
            System.out.println(
                    "Total embedded chunks: " + embeddedChunks.size()
            );

            // Create the question and generate its embedding.
            String question = "How do I authenticate with the API?";

            System.out.println();
            System.out.println("Question: " + question);

            List<Float> questionEmbedding =
                    embeddingService.createEmbedding(question);

            System.out.println();
            System.out.println(
                    "Question embedding dimensions: "
                            + questionEmbedding.size()
            );

            // Create a list to hold chunks and their similarity scores.
            List<ScoredChunk> scoredChunks = new ArrayList<>();

            // Calculate the similarity between the question and each chunk.
            for (EmbeddedChunk embeddedChunk : embeddedChunks) {

                double chunkSimilarity =
                        similarityService.cosineSimilarity(
                                questionEmbedding,
                                embeddedChunk.getEmbedding()
                        );

                ScoredChunk scoredChunk =
                        new ScoredChunk(
                                embeddedChunk.getChunk(),
                                chunkSimilarity
                        );

                scoredChunks.add(scoredChunk);
            }

            // Print the scored chunks in their original processing order.
            System.out.println();
            System.out.println(
                    "Total scored chunks: " + scoredChunks.size()
            );

            System.out.println();
            System.out.println(
                    "===== SCORED CHUNKS (ORIGINAL ORDER) ====="
            );

            for (ScoredChunk scoredChunk : scoredChunks) {

                System.out.println(
                        "Similarity: " + scoredChunk.getSimilarityScore()
                );

                System.out.println(
                        "Chunk: " + scoredChunk.getChunk().getContent()
                );

                System.out.println();
            }

            // Sort chunks by similarity score, highest first. This gives the "ranking"
            scoredChunks.sort(
                    (a, b) -> Double.compare(
                            b.getSimilarityScore(),
                            a.getSimilarityScore()
                    )
            );

            // Print the ranked chunks.
            System.out.println();
            System.out.println("===== RANKED CHUNKS =====");

            int rank = 1;

            for (ScoredChunk scoredChunk : scoredChunks) {

                System.out.println("Rank: " + rank++);

                System.out.println(
                        "Similarity: " + scoredChunk.getSimilarityScore()
                );

                System.out.println(
                        "Chunk: " + scoredChunk.getChunk().getContent()
                );

                System.out.println();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}