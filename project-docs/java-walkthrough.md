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

## Get an OpenAPI API key

API access: This project uses the OpenAI API to generate embeddings. An OpenAI API key with available API credits is required to run the embedding example.

A ChatGPT subscription and API access are billed separately.

Get an OpenAPI API key.

1. In your OpenAPI API account, go to the [API keys page](https://platform.openai.com/api-keys?utm_source=chatgpt.com).
2. Click `+ Create new secret key`
3. Give your key a name and copy it to the clipboard.
4. Save it as an environment variable.
 
_Windows_

Create a new user environment variable called `OPENAI_API_KEY`
 
_MacOS_

1. Open a terminal and edit `zshrc`: `nano ~/.zshrc`
2. Add this line: `export OPENAI_API_KEY="sk-your-actual-key-here"`
3. Save and exit nano:
   1. Control+O + Enter to save
   2. Control+X to exit
   3. Reload your shell configuration: run `source ~/.zshrc`
   4. Verify the value by pasting the following in a terminal:

```shell
if [ -n "$OPENAI_API_KEY" ]; then
    echo "OPENAI_API_KEY is set"
else
    echo "OPENAI_API_KEY is NOT set"
fi
```

This command checks whether `OPENAI_API_KEY` has a value without displaying the API key itself.   
    
Finally, restart IntelliJ.

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

That one and only dependency downloads the OpenAI Java SDK library from Maven and make its classes available to our application.
It is *client-side software* that runs inside the application.

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

You can find a lot more details in ![rag concepts](C:\Users\Steve\dev\tasters\rag-taster\project-docs\rag-concepts.md)



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

The `OpenAIOkHttpClient.fromEnv()` call constructs the client using a configuration from the OPEN_API_KEY environment variable you created at the beginning of this taster.

`OpenAIOkHttpClient` is an implementation of the OpenAI Java client's HTTP transport. OkHttp is the HTTP client library underneath the dependency in `pom.xml`.

The current Java SDK uses EmbeddingCreateParams, EmbeddingModel.TEXT_EMBEDDING_3_SMALL, and client.embeddings().create(...) for this operation.

Modify `Main.java`

Instantiate the embedding service by adding the following line after `Chunker chunker = new Chunker();`:
`EmbeddingService embeddingService = new EmbeddingService();`

The beginning of `Main.java` (not including imports) should look like this:

```java
Path docsPath = Paths.get("src/main/resources/docs");

Chunker chunker = new Chunker();
EmbeddingService embeddingService = new EmbeddingService();
```

Modify your chunk loop. It currently looks like this:
```java
for (Chunk chunk : chunks) {
    System.out.println("--- CHUNK ---");
    System.out.println(chunk.getContent());
}
```
Change it to this:

```java
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
```

The key line is `embeddingService.createEmbedding(chunk.getContent());`

The line takes the actual text of the chunk and sending it to the embedding model. 

The result `List<Float> embedding` is the vector.

And `embedding.size()` tells us the number of dimensions in the vector.

This is a baby step--instead of creating embeddings for all chunks, we limit ourselves to **one** using the `break;` statement.

The flow will be:

```
Our Main.java
      │
      ▼
EmbeddingService
      │
      ▼
OpenAI Java SDK
      │
      ▼
OpenAIOkHttpClient
      │
      ▼
HTTPS request
      │
      ▼
OpenAI API
      │
      ▼
Embedding response
```

## Run the updated application

When you run the application you should see output similar to this:
```
===== authentication.md =====
--- CHUNK ---
# Authentication
Embedding dimensions: 1536
First five values:
-0.012931824
0.040618896
0.022216797
-0.026626587
0.0037193298
===== errors.md =====
--- CHUNK ---
# Errors
Embedding dimensions: 1536
First five values:
-0.0082473755
0.049835205
0.053009033
0.0137786865
-0.022567749
===== rate-limits.md =====
--- CHUNK ---
# Rate Limits
Embedding dimensions: 1536
First five values:
0.0029144287
0.031051636
0.029251099
-0.029586792
-0.022338867
===== webhooks.md =====
--- CHUNK ---
# Webhooks
Embedding dimensions: 1536
First five values:
-0.053344727
0.0104904175
-0.013137817
-0.026046753
0.029129028
```
## Let's unpack that

The outer loop reads each file in the `resources/docs` directory and splits each file into chunks.

The inner loop reads one chunk per file, generates a list of embedding vectors, then prints the embedding size along with first five embeddings in the first vector.

What does this tell us? Several things.

1. The API call succeeded.

We got actual numerical data back for every chunk.

2. Every embedding has 1,536 dimensions.

3. Different text produces different vectors.

Here is an important point based on our chunk limitation.
We should not look at those first five numbers and conclude that, say, Authentication and Errors are more similar because their first numbers are closer. 

That's not how we're going to determine semantic similarity.

The entire 1,536-dimensional vectors matter.

## Recap & where we're going next

Right now we're doing:

```
                    ┌── vector 1
authentication.md ──┤
                    └── 1536 numbers

                    ┌── vector 2
errors.md ──────────┤
                    └── 1536 numbers

                    ┌── vector 3
rate-limits.md ────┤
                    └── 1536 numbers

                    ┌── vector 4
webhooks.md ────────┤
                    └── 1536 numbers
```

We've created the vectors.

Now we need to do something with them.

The next experiment I'd recommend is particularly illuminating:

We'll create a query such as:

How do I authenticate with the API?

We'll generate an embedding for that query.

Then we'll compare the query vector against each document vector using cosine similarity.
See [RAG concepts](rag-concepts.md) for details about cosine similarity.


Conceptually:

```
                 "How do I authenticate?"
                           │
                           ▼
                    Query embedding
                           │
                           ▼
                     1536 numbers
                           │
             ┌─────────────┼─────────────┐
             ▼             ▼             ▼
       Authentication    Errors     Webhooks
          vector          vector       vector
             │             │             │
             └─────────────┼─────────────┘
                           ▼
                 cosine similarity
                           │
                           ▼
                 ranked results
```

And hopefully we'll see something like:

```
Authentication    0.87
Errors            0.31
Rate Limits       0.24
Webhooks          0.18
```
That's the moment where the numbers stop being abstract.

We'll actually use the vectors to answer the question:

"Which piece of my documentation is most relevant to this query?"

And that is the "retrieval" in Retrieval-Augmented Generation.


## But first

Let's modify the application to generate embedding vectors for all chunks in all documents.


First, create the `EmbeddedChunk` class that maps a `Chunk` object to its embeddings.
This is in lieu of using an actual vector database.

```java
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
```

Next, modify `Main.java`:
1. Remove the `break` that stopped processing a file after reading the first chunk
2. Remove the embeddings limitation that printed the first five embeddings.
3. Add functionality to collect a chunk and its embeddings. You do this by using the new EmbeddingChunk class:

3a. Create structure that will be a list of all embeddings of all documents. Add it before the loop 
through all documents.

```java
List<EmbeddedChunk> embeddedChunks = new ArrayList<EmbeddedChunk>();
```

3b. Then within the loop through a document's chunks, create the `EmbeddedChunk` instance and  add the previously created embedding.  


```java
EmbeddedChunk embeddedChunk = new EmbeddedChunk(chunk, embedding);
embeddedChunks.add(embeddedChunk);
```
3c. Print the chunk and details about its embeddings:

```java
    System.out.println("--- CHUNK ---");
    System.out.println(chunk.getContent());
    System.out.println("Embedding dimensions: " + embedding.size());
```

4.When the chunk loop is done, add the embeddings to the `embeddedChunks` list.

The modified `Main.java` should look like this:

```java
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
 * <p>Maintains a list of embeddings that acts as a substitute vector database.</p>
 * <p>Collects embeddings for all document chunks.</p>
 *
 */
public class Main {

    /**
     * @param args Standard command line args. Not used
     *  <p>Using <code>java.nio.file.Path</code>, <code>Paths</code>, and <code>Files</code> functionality, does the following for each source document:</p>
     *  <ol>
     *  <li>Reads the document contents</li>
     *  <li>Creates a <code>Document</code> object from the file name and contents.</li>
     *  <li>Using a <code>Chunker</code> object, breaks the document contents into Chunks</li>
     *  <li>Using the EmbeddingService, creates the chunk's embeddings</li>
     *  <li>Associates each chunk with the chunk's embeddings.</li>
     *  <li>Adds the chunk map to the global list of embeddings.</li>
     *  <li>Prints information about the chunk and its embeddings.</li>
     *  </ol>
     */
    public static void main(String[] args) {

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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```




# Retrieval


# Generation