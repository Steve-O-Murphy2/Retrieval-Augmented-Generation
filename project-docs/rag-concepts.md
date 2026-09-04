# RAG Concepts

## What is RAG?

RAG stands for **Retrieval-Augmented Generation**.

The basic idea is simple:

> Retrieve relevant information from an external knowledge source, add that information to the user's question as context, and give the combined context to an LLM so it can generate an answer.

The **"Augmented"** part is the bridge between retrieval and generation. The retrieved information augments the context available to the LLM.

### Without RAG

Suppose the user asks:

> How do I authenticate with this API?

Without RAG, the application sends the question directly to the LLM:

```text
User question
     │
     ▼
    LLM
     │
     ▼
   Answer
```

The LLM has only whatever knowledge it already possesses.

### With RAG

With RAG, our application first searches a knowledge source, such as our technical documentation:

```text
User question
     │
     ▼
Generate query embedding
     │
     ▼
Retrieve relevant chunks
     │
     ▼
┌───────────────────────────────┐
│ User question                 │
│                               │
│ +                             │
│                               │
│ Retrieved documentation       │
└───────────────┬───────────────┘
                │
                ▼
               LLM
                │
                ▼
              Answer
```

For example, imagine `authentication.md` contains:

> API requests must include an API key in the Authorization header.

The application might construct a prompt along these lines:

```text
Answer the user's question using the following documentation.

Documentation:
API requests must include an API key in the Authorization header.

User question:
How do I authenticate with this API?
```

We've **augmented** the user's question with information retrieved from our documentation. The LLM can now generate an answer grounded in that particular documentation.

---

## The RAG Pipeline

The complete pipeline we are building looks like this:

```text
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
              ┌────────────┴────────────┐
              │                         │
         user question             stored vectors
              │                         │
              ▼                         │
      query embedding                   │
              │                         │
              └──────────┐              │
                         ▼              ▼
                         RETRIEVAL
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

The important stages are:

1. **Load documentation**
2. **Chunk the documentation**
3. **Generate embeddings for the chunks**
4. **Store the embeddings**
5. **Generate an embedding for the user's question**
6. **Retrieve the chunks most relevant to the question**
7. **Add those chunks to the LLM's context**
8. **Generate the answer**

The concepts behind those stages are easier to understand if we start with embeddings.

---

# Embeddings

An **embedding** is a numerical representation of text that captures aspects of its meaning.

Suppose chunking produces this text:

> The API requires an API key for authentication.

The text is passed to an embedding model:

```text
Chunk
 │
 │ "The API requires an API key..."
 ▼
Embedding model
 │
 ▼
Vector
 │
 ├──  0.0123
 ├── -0.0841
 ├──  0.2217
 ├──  ...
 └──  1,536 values
```

The resulting vector is the embedding.

The important thing is not what any individual number means. The useful property is the **relationship between vectors**.

For example, we'd expect these two pieces of text to have relatively similar embeddings:

> The API requires an API key for authentication.

> Clients must authenticate using a valid API key.

Even though they don't use exactly the same words, they express similar ideas.

By contrast:

> Webhooks notify applications when events occur.

should produce an embedding that is farther away from the API-key information.

This is the property RAG exploits.

Instead of searching only for matching words, we can search for **semantically similar text**.

## What are all those numbers?

An embedding isn't:

```text
authentication = 0.87
webhooks = 0.14
```

It's more like a point in a mathematical space with hundreds or thousands of dimensions:

```text
                     dimension 1
                          │
                          │
                     •    │
                 •        │
                          │
                          └──────── dimension 2
```

Except there aren't just two dimensions. There may be hundreds or thousands.

With `text-embedding-3-small`, for example, the standard embedding has **1,536 dimensions**. The model can also produce shorter embeddings by specifying a smaller dimensions value.

We don't need to understand what dimension 847 "means." What matters for retrieval is the relationship between the vectors.

---

# Vectors: magnitude, direction, and components

To understand how we compare embeddings, it helps to review what a vector represents.

A vector has both **magnitude** and **direction**.

For example:

```text
A = [2, 3]
```

The two numbers are the vector's **components**. They tell us how far the vector extends along each dimension.

For a two-dimensional vector, `[2, 3]` means:

- 2 units along the x-axis
- 3 units along the y-axis

Those components determine the vector's magnitude and direction.

The magnitude is:

```text
√(2² + 3²) = √13 ≈ 3.606
```

The direction is approximately:

```text
atan(3 / 2) ≈ 56.3°
```

An embedding works the same basic way, except it has many more components:

```text
[dimension₁, dimension₂, dimension₃, ..., dimension₁₅₃₆]
```

For example, one of our embeddings might look like:

```text
[-0.0129, 0.0406, 0.0222, ..., 0.0037]
```

Each number is a component along one dimension.

In our taster, that's a **1,536-dimensional vector**. We can't draw 1,536 dimensions on a screen, so we use two-dimensional diagrams as a conceptual model:

```text
2 dimensions                 1,536 dimensions

     y                            dimension 1
     │                            dimension 2
     │  •                         dimension 3
     │ /                          dimension 4
     │/                           ...
     └──── x                      dimension 1536
```

The mathematics works the same way. We simply can't visualize the full space.

---

# Cosine similarity

Now we can see why vector direction matters.

RAG needs a way to determine how similar two embedding vectors are. One common measure is **cosine similarity**.

If you remember trigonometry, cosine comes from a right triangle:

```text
cos(θ) = adjacent / hypotenuse
```

For vectors, cosine similarity uses the angle between the vectors.

Imagine two vectors starting at the same point:

```text
                 B
                •
              /
            /
          /
        /
      •────────────• A
    origin        Vector A
```

The two vectors have an angle between them.

Cosine tells us something about that angle:

```text
Small angle

     ↗
    /
   /
  ↗

Vectors point in nearly the same direction
                ↓
           cosine ≈ 1
```

Whereas:

```text
Large angle

────────────→
     ↑
     │
     │
     │

Vectors point in substantially different directions
                ↓
           cosine ≈ 0
```

And if they point in exactly opposite directions:

```text
←──────── • ────────→
```

The angle is 180° and:

```text
cos(180°) = -1
```

So cosine similarity gives us a convenient measure of how similarly two vectors are pointing.

For embeddings, that becomes a useful measure of **semantic similarity**.

---

# Using cosine similarity for retrieval

Here's where the pieces come together.

Suppose we have three vectors representing three pieces of documentation:

```text
                     Authentication
                         ↗
                       /
                     /
                   •
                  /
                 /
                •
               /
              ↘
             Webhooks
```

Now suppose we turn the user's question:

> How do I authenticate?

into another embedding vector.

If that query vector points in approximately the same direction as the Authentication vector, their cosine similarity will be high.

If it points in a very different direction from the Webhooks vector, their cosine similarity will be lower.

Conceptually:

```text
Query → Authentication    cosine similarity = high
Query → Webhooks          cosine similarity = low
```

We can calculate the cosine similarity between the query vector and each stored document vector, then rank the results.

The chunks with the highest similarity scores become our **retrieved chunks**.

That is the retrieval mechanism in this RAG system.

## The important part

We're not comparing the individual numbers like this:

```text
query[0] vs document[0]
query[1] vs document[1]
...
```

We're comparing the **vectors as a whole**.

That's why the word *vector* matters.

The embedding gives us a point/direction in a very high-dimensional mathematical space, and cosine similarity lets us ask:

> How closely are these two vectors pointing in the same direction?

For RAG, we turn that mathematical question into:

> How semantically similar are these two pieces of text?

---

# Retrieval, augmentation, and generation

At this point, the three parts of the name **Retrieval-Augmented Generation** should make sense.

### Retrieval

We embed the user's question and compare it with the embeddings of our documentation chunks. We retrieve the chunks with the highest similarity.

### Augmentation

We add those retrieved chunks to the context supplied to the LLM.

### Generation

The LLM uses the user's question and the retrieved documentation to generate an answer.

The overall flow is therefore:

```text
                 DOCUMENTATION
                      │
                      ▼
                   CHUNKS
                      │
                      ▼
                  EMBEDDINGS
                      │
                      ▼
                 VECTOR STORE
                      │
                      │
User question ──► EMBEDDING
                      │
                      ▼
                  RETRIEVAL
                      │
                      ▼
              RELEVANT CHUNKS
                      │
                      ▼
             AUGMENTED CONTEXT
                      │
                      ▼
                     LLM
                      │
                      ▼
                    ANSWER
```

# A real-life example

A very realistic RAG job scenario is an enterprise with a huge internal knowledge base and an engineering team building a system that lets employees ask questions of that knowledge in plain English.

For example, imagine a company like a large software vendor with thousands of engineering and support documents:

```
                 COMPANY KNOWLEDGE
                        │
       ┌────────────────┼────────────────┐
       │                │                │
   Confluence        GitHub          PDFs/DOCX
       │                │                │
       └────────────────┼────────────────┘
                        ▼
                  INGESTION PIPELINE
                        │
                 Parse / normalize
                        │
                     Chunk
                        │
                   Embed text
                        │
                        ▼
                  VECTOR DATABASE
                        │
                        │
Employee question ──────┘
        │
        ▼
   Query embedding
        │
        ▼
   Similarity search
        │
        ▼
 Relevant documents
        │
        ▼
   LLM + context
        │
        ▼
      Answer
```

Suppose an employee asks:

> "What's the procedure for rotating production API credentials, and who needs to approve it?"

The company may have information scattered across:

- a security policy
- an internal engineering wiki
- an API operations guide
- an incident-response document
- several versions of procedures
- perhaps even tickets or GitHub documentation

Nobody wants the employee to search six systems manually.

So the company builds an internal RAG assistant.

The employee asks the question. The system turns the question into an embedding, searches the vector database, retrieves the most relevant chunks, and gives those chunks to the LLM.

The LLM then produces something like:

> Production API credentials must be rotated every 90 days. The rotation requires approval from the service owner and Security Operations. See the Production Credential Rotation Procedure for the complete process.

The LLM isn't supposed to know the company's internal procedure from its pretrained knowledge. The RAG system retrieved the company's actual documentation and supplied it as context.

# What does a RAG engineer do?

This is a bit of bonus information to round out the picture.

Companies hire engineers to implement RAG pipelines; this is a specialty job.

The engineer isn't necessarily sitting around writing clever prompts all day. They're responsible for building and maintaining the machinery that makes retrieval work.

They might build a system responsible for the following processes.

## 1. Document ingestion

Connect to systems such as:

```
Confluence
GitHub
SharePoint
Google Drive
S3
Databases
Internal APIs
```
and pull documents into the RAG system.




The rest of this project will implement these pieces one at a time.

## 2. Document processing

Turn those different source formats into something the pipeline can work with:

```
DOCX ──┐
PDF  ──┤
HTML ──┼──► normalized documents
MD   ──┤
Wiki ──┘
```

This is exactly the distinction we were talking about in `documentation-considerations.md`: the source format isn't really the RAG problem. The ingestion pipeline has to turn the source into usable content.

## 3. Chunking

Take a 50-page engineering document and turn it into meaningful pieces.

Bad:
```
chunk 1 = pages 1-5
chunk 2 = pages 6-10
```
Better:

```
chunk 1 = Authentication requirements
chunk 2 = Creating API credentials
chunk 3 = Rotating API credentials
chunk 4 = Revoking API credentials
```
The engineer has to think about chunk size, boundaries, overlap, metadata, and so forth.

## 4. Embedding

Send the chunks through an embedding model:

```
"API credentials must be rotated every 90 days."
                         │
                         ▼
                  embedding model
                         │
                         ▼
              [0.013, -0.042, ...]
```
Those vectors go into the vector database.

## 5. Retrieval

When the employee asks:

> "How often do production API credentials need to be rotated?"

the system creates another embedding:

```
question
   │
   ▼
query embedding
   │
   ▼
vector search
   │
   ├── chunk A  similarity .91
   ├── chunk B  similarity .84
   ├── chunk C  similarity .42
   └── chunk D  similarity .17
```

The system retrieves the highest-ranked chunks.

## 6. Retrieval quality

And this is where the job gets considerably more interesting.

The engineer discovers that users ask:

> "How often do we change API keys?"

but the documentation says:

> "Production credentials must be rotated every 90 days."

Will the system retrieve the right chunk?

Maybe.

If it doesn't, the engineer might investigate:

```
chunking strategy
embedding model
metadata
similarity threshold
top-K retrieval
query transformation
hybrid keyword/vector search
reranking
document quality
```

So _RAG engineering is partly a retrieval-quality engineering problem._

## Keeping the knowledge base updated

Imagine the company changes its credential policy.

The source document gets updated:

> 90 days → 60 days

The RAG system now needs to notice that the source changed, reprocess the document, generate new embeddings, and update the vector store.

So a production RAG pipeline might look more like:

```
              DOCUMENT SOURCES
                     │
                     ▼
              Change detection
                     │
                     ▼
               Ingestion
                     │
                     ▼
             Parse / normalize
                     │
                     ▼
                 Chunking
                     │
                     ▼
                Embeddings
                     │
                     ▼
               Vector store
                     │
                     ▼
             Retrieval service
                     │
          ┌──────────┴──────────┐
          │                     │
     User question         Metadata/filter
          │                     │
          └──────────┬──────────┘
                     ▼
                  Retrieval
                     │
                     ▼
                  Reranking
                     │
                     ▼
              Context assembly
                     │
                     ▼
                    LLM
                     │
                     ▼
                  Answer
```

 