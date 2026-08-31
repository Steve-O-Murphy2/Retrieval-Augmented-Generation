# Project setup

Create this directory structure:

``` 
src /
  main /
    java /
      com.steveomurphy.tasters.rag /
        Chunk.java
        Chunker.java
        Document.java
        EmbeddingSereevice.java
        Main.java
    resources /
      docs /
        authentication.md
        errors.md
        rate-limits.md
        webhooks.md            
```

`src/main/java/com.steveomurphy.tasters.rag` Is where our Java source files are. 
You are welcome to use your own package name, just be sure to replace it in the 
sample content I provide in this taster.

The `resources/docs` directory is where our RAG corpus is.

## Pipeline end game

We are aiming for the following flow:

```
                 OUR DOCUMENTATION
                       │
                       ▼
                    CHUNKING
                       │
                       ▼
                   EMBEDDINGS
                       │
                       ▼
                  VECTOR STORE
                       │
             ┌─────────┴─────────┐
             │                   │
        user question       user question
             │                   │
             ▼                   ▼
          embedding          retrieval
                                 │
                                 ▼
                           relevant chunks
                                 │
                                 ▼
                                LLM
                                 │
                                 ▼
                              answer
```
You will learn all about the various components and stages as you progress through this taster. 


## Maven

This is a Maven project, so add `pom.xml` at the project root. Add the following content to it:

```html 
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>rag-taster</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.release>21</maven.compiler.release>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.openai</groupId>
            <artifactId>openai-java</artifactId>
            <version>4.52.0</version>
        </dependency>
    </dependencies>

</project>
```

## Get a tiny Java program running

Open `Main.java` and add this content:

```java
public class Main {
    public static void main(String[] args) {
        System.out.println("RAG taster is alive!");
    }
}
```
Run `Main` from IntelliJ. You should see

`RAG taster is alive!`


# Simplified content retrieval

You will implement code that reads documents and writes the content to the console.

Open `Main.java` and completely replace its content with:

```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {

        Path docsPath = Paths.get("src/main/resources/docs");

        try (Stream<Path> paths = Files.list(docsPath)) {

            paths
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        String content = Files.readString(path);

                        System.out.println("===== " + path.getFileName() + " =====");
                        System.out.println(content);
                        System.out.println();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

Run `Main` from IntelliJ. You should see

```
===== authentication.md =====
# Authentication

The API uses API keys for authentication.

Include your API key in the Authorization header...

===== errors.md =====
# Errors

The API returns standard HTTP status codes...

===== rate-limits.md =====
# Rate Limits

The API allows 100 requests per minute...

===== webhooks.md =====
# Webhooks

The API can send events to your application...
```
# Recap
Let's pause for a moment. We have created `.md` files with the content, and updated `Main.java` to read the documents and split 
their contents. Currently, our pipeline is:

```
             Markdown files
                   │
                   ▼
             Files.list()
                   │
                   ▼
             Path objects
                   │
                   ▼
           Files.readString()
                   │
                   ▼
             String content
```

That's deliberately simple. We're not doing RAG yet.

Being good engineers, we can start to add some classes that we will use in the next iteration of the application.

# Documents

Start with a Document class that represents a source document. 

Open `Document.java` and add this:

```java

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
```
The class has instance variables `source` and `content` that represent the document name and content.

# Chunking

Remember that the application parsed document contents in one big main method. You will now create two classes involved
in chunking.

First create a class that represents a document chunk. Open `Chunk.java` and add this content:

```java
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
```

You will notice two instance variables: `source` is the name of a document and chunk is one of the document's chunks.

Now create the class that carries out the chunking operation. Open `Chunker.java` and add this content:

```java
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
         * Split the document into chunks. Chunks are delimited by blank lines.
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
```

The class has one public method that receives a `Document` object, splits its contents into pieces based on newlines.
The `split` method returns pieces in an array, which is not very portable, so the method 
converts the array into a `List` and returns it.

Finally, update `Main.java` to use the new classes. Completely replace `Main.java` with this content:

```java
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
     *  <li>Prints Document name and chunks</li>
     *  </ol>
     */
    public static void main(String[] args) {

        Path docsPath = Paths.get("src/main/resources/docs");

        Chunker chunker = new Chunker();

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
                                System.out.println("--- CHUNK ---");
                                System.out.println(chunk.getContent());
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

```
Run Main.java in your IDE. You should see output similar to this in the console:

```
===== authentication.md =====
--- CHUNK ---
# Authentication
--- CHUNK ---
The API uses API keys for authentication.
--- CHUNK ---
Include your API key in the `Authorization` header:
--- CHUNK ---
Authorization: Bearer YOUR_API_KEY
--- CHUNK ---
API keys can be regenerated from the developer portal.
--- CHUNK ---
## OAuth
--- CHUNK ---
The API also supports OAuth 2.0.
===== errors.md =====
--- CHUNK ---
# Errors
--- CHUNK ---
Error handling is robust.
===== rate-limits.md =====
--- CHUNK ---
# Rate Limits
--- CHUNK ---
The API allows 100 requests per minute.
--- CHUNK ---
When the rate limit is exceeded, the API returns HTTP status 429.
===== webhooks.md =====
--- CHUNK ---
# Webhooks
--- CHUNK ---
The API has extensive webhooks for payments and messaging.
```



# Recap

So far we have run a simple no-class application that chunks, followed by an application and some new classes that encapsulate
file reading and chunk creation. Notice also that we have not used the `com.openai` in the pom file.

We are ready to move onto the next major RAG functionality--**embeddings**.

# Embeddings

An embedding is a numerical representation of text that captures aspects of its meaning. Embeddings are captured in an array also called a **Vector**.
(Vector is a term from mathematics, it is called that because we can perform mathematical operations on it.)

In RAG speak an embedding is an array of floating-point numbers.
```java
float[] embedding = {
    0.12f,
    -0.43f,
    0.87f,
    0.21f
};
```
In mathematics, an embedding is a vector:

`v=[0.12, −0.43, 0.87, 0.21`

You can find a lot more details in ![rag concepts](C:\Users\Steve\dev\tasters\rag-taster\docs\rag-concepts.md)

For now, let's get down to the business of creating embedding functionality.

## Create an Embedding Service

Open `EmbeddingService.java` and add this content:

```java
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import com.openai.models.embeddings.EmbeddingCreateParams;
import com.openai.models.embeddings.EmbeddingModel;

import java.util.List;

public class EmbeddingService {

    private final OpenAIClient client;

    public EmbeddingService() {
        client = OpenAIOkHttpClient.fromEnv();
    }

    public List<Float> createEmbedding(String text) {

        EmbeddingCreateParams params = EmbeddingCreateParams.builder()
                .input(text)
                .model(EmbeddingModel.TEXT_EMBEDDING_3_SMALL)
                .build();

        CreateEmbeddingResponse response =
                client.embeddings().create(params);

        return response.data()
                .get(0)
                .embedding();
    }
}
```
The current Java SDK uses EmbeddingCreateParams, EmbeddingModel.TEXT_EMBEDDING_3_SMALL, and client.embeddings().create(...) for this operation.

# Retrieval


# Generation