package com.steveomurphy.tasters.rag;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

/**
 * Driver program
 */
public class Main {

    /**
     * @param args Standard command line args. Not used
     *  <p>Using <code>java.nio.file.Path</code>, <code>Paths</code>, and <code>Files</code> functionality, does the following for each source document:</p>
     *  <ol>
     *  <li>Reads the document contents</li>
     *  <li>Creates a <code>Document</code> object from the file name and contents.</li>
     *  <li>Using a <code>Chunker</code> object, breaks the document contents into Chunks</li>
     *  <li>Reads the first chunk in the document</li>
     *  <li>Using the EmbeddingService, creates the chunk's embeddings</li>
     *   <li>Prints the first five embeddings.</li>
     *  </ol>
     */
    public static void main(String[] args) {

        Path docsPath = Paths.get("src/main/resources/docs");

        Chunker chunker = new Chunker();
        EmbeddingService embeddingService = new EmbeddingService();

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

                                System.out.println("--- CHUNK ---");
                                System.out.println(chunk.getContent());

                                System.out.println("Embedding dimensions: " + embedding.size());

                                System.out.println("First five values:");

                                for (int i = 0; i < 5; i++) {
                                    System.out.println(embedding.get(i));
                                }

                                break;
                            }


                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}