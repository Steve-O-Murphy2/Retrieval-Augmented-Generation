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

        ////////////////////////////////////////////
        // START Temporary test of SimilarityService
        ////////////////////////////////////////////
        SimilarityService similarityService = new SimilarityService();

        List<Float> vectorA = List.of(2.0f, 3.0f);
        List<Float> vectorB = List.of(2.0f, 3.0f);

        double similarity =
                similarityService.cosineSimilarity(vectorA, vectorB);

        System.out.println();
        System.out.println("Cosine similarity: " + similarity);
        System.exit(0);
        ////////////////////////////////////////////
        // END Temporary test of SimilarityService
        ////////////////////////////////////////////

        Path docsPath = Paths.get("src/main/resources/docs");

        Chunker chunker = new Chunker();
        EmbeddingService embeddingService = new EmbeddingService();


        // List to collect all chunks and their embeddings. An in-memory vector store
        List<EmbeddedChunk> embeddedChunks = new ArrayList<>();

        try (Stream<Path> paths = Files.list(docsPath)) {

            paths
                    .filter(Files::isRegularFile)
                    .forEach(path -> {

                        try {
                            String content = Files.readString(path);

                            Document document =
                                    new Document(path.getFileName().toString(), content);

                            List<Chunk> chunks = chunker.chunk(document);

                            System.out.println(
                                    "===== " + document.getSource() + " ====="
                            );

                            for (Chunk chunk : chunks) {

                                List<Float> embedding =
                                        embeddingService.createEmbedding(chunk.getContent());

                                // Associate the chunk with its embedding
                                EmbeddedChunk embeddedChunk =
                                        new EmbeddedChunk(chunk, embedding);

                                // Add the embedded chunk to the in-memory vector store
                                embeddedChunks.add(embeddedChunk);

                                System.out.println("--- CHUNK ---");
                                System.out.println(chunk.getContent());
                                System.out.println("Embedding dimensions: " + embedding.size());
                            }


                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            System.out.println();
            System.out.println(
                    "Total embedded chunks: " + embeddedChunks.size()
            );

            String question = "How do I authenticate with the API?";

            System.out.println();
            System.out.println("Question: " + question);

            // Create question embedding and print information about it.
            List<Float> questionEmbedding =
                    embeddingService.createEmbedding(question);

            System.out.println();
            System.out.println("Question embedding dimensions: "
                    + questionEmbedding.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}