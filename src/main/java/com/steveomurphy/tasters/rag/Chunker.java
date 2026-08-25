package com.steveomurphy.tasters.rag;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads a Document {@link Document} object, splits its contents into chunks {@link Chunk} and returns a List of those chunks.
 */
public class Chunker {

    /**
     *
     * @param document The Document object to chunk
     * @return List of chunks of the document
     */
    public List<Chunk> chunk(Document document) {

        List<Chunk> chunks = new ArrayList<>();

        /**
         * Split the document into chunks. Chunks are delimited by blank li nes.
         * Each Chunk is in a String array.
         *
         */
        String[] paragraphs = document.getContent().split("\\n\\s*\\n");

        /**
         * Convert the String array into a list of Chunk objects
         */
        for (String paragraph : paragraphs) {

            String content = paragraph.trim();

            if (!content.isEmpty()) {
                chunks.add(new Chunk(document.getSource(), content));
            }
        }

        return chunks;
    }
}