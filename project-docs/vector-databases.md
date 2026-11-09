
# A real vector database adds several layers

A production vector database is more like this:

                 VECTOR DATABASE
```
┌─────────────────────────────────────────────────────┐
│                                                     │
│  Vector data                                        │
│  ┌───────────────────────────────────────────────┐  │
│  │ ID     Vector                                 │  │
│  │ 001    [0.012, -0.034, 0.087, ...]            │  │
│  │ 002    [0.021, -0.041, 0.092, ...]            │  │
│  │ 003    [-0.013, 0.055, 0.071, ...]            │  │
│  └───────────────────────────────────────────────┘  │
│                                                     │
│  Metadata                                           │
│  ┌───────────────────────────────────────────────┐  │
│  │ ID   source             chunk                 │  │
│  │ 001  authentication.md  "The API uses..."     │  │
│  │ 002  authentication.md  "Include your..."     │  │
│  │ 003  rate-limits.md     "The API allows..."   │  │
│  └───────────────────────────────────────────────┘  │
│                                                     │
│  Vector index                                       │
│  ┌───────────────────────────────────────────────┐  │
│  │ Structures optimized for nearest-neighbor     │  │
│  │ searches                                      │  │
│  └───────────────────────────────────────────────┘  │
│                                                     │
│  Query engine                                       │
│  ┌───────────────────────────────────────────────┐  │
│  │ similarity search                             │  │
│  │ top-K retrieval                               │  │
│  │ metadata filtering                            │  │
│  └───────────────────────────────────────────────┘  │
│                                                     │
└─────────────────────────────────────────────────────┘
```

The important thing is that the vector itself isn't the whole database.

There are several pieces working together.

## The vector and its associated data

A useful way to picture one record is:

              VECTOR RECORD
```
┌─────────────────────────────────────────────┐
│ ID: 002                                     │
│                                             │
│ Vector:                                     │
│ [0.021, -0.041, 0.092, ... 1536 values]     │
│                                             │
│ Metadata:                                   │
│   source: authentication.md                 │
│   chunk: 4                                  │
│   section: Authentication                   │
│                                             │
│ Content:                                    │
│   "Include your API key in the              │
│    Authorization header..."                 │
└─────────────────────────────────────────────┘
```


This is why I was previously describing your EmbeddedChunk as a miniature version of this concept.

Your Java class:
```
EmbeddedChunk
├── Chunk
└── List<Float> embedding
```

is basically saying:

"Keep the piece of text and the numerical representation of that piece of text together."

A real vector database does something much more sophisticated, but the conceptual relationship is the same.

4. The really important part: the vector index

Here's where a vector database earns its paycheck.

Imagine you eventually have:

10 documents
↓
1,000 chunks

1,000 documents
↓
100,000 chunks

1,000,000 documents
↓
100,000,000 chunks

You could theoretically compare the query vector against every single vector.

```
Query
│
├── compare with vector 1
├── compare with vector 2
├── compare with vector 3
├── compare with vector 4
├── ...
└── compare with vector 100,000,000
```

That's not exactly a delightful afternoon.

Vector databases therefore use specialized vector indexes designed to make nearest-neighbor searches much more efficient.

Conceptually:
```
                         Query
                           │
                           ▼
                    ┌──────────────┐
                    │ Vector Index │
                    └──────┬───────┘
                           │
              ┌────────────┼────────────┐
              ▼            ▼            ▼
           Region A     Region B     Region C
              │            │
              │            └───── likely candidates
              │
              └───────────────┐
                              ▼
                       similarity scoring
                              │
                              ▼
                         Top 5 chunks

```



The actual indexing algorithms are considerably more complicated than that diagram, but that's the mental model I would use initially.

## Metadata becomes important too

Suppose your vector database contains documentation for several products:
```
                  VECTOR DATABASE
                         │
       ┌─────────────────┼─────────────────┐
       │                 │                 │
       ▼                 ▼                 ▼
    Product A         Product B         Product C
       │                 │                 │
    20,000 chunks     50,000 chunks     30,000 chunks

```




A query might say:

"How do I authenticate?"

But your application may already know:

product = "Product B"
version = "3.2"
language = "Java"

The vector database can potentially use metadata filters:
```
                 Query
                   │
                   ▼
          "How do I authenticate?"
                   │
                   ▼
             Vector search
                   │
                   │
            + metadata filter
            │
            │ product = B
            │ version = 3.2
            ▼
             Candidates
                   │
                   ▼
              Top results
```

So vector databases aren't merely:

"Give me the closest vector."

They can support:

"Give me the most semantically relevant vectors within this particular subset of my data."

That's extremely useful for documentation.

## Put the whole RAG system together

Now we can draw the larger picture:

```
                  DOCUMENT INGESTION
                         │
                         ▼
              ┌─────────────────────┐
              │      Documents      │
              │ authentication.md   │
              │ errors.md           │
              │ rate-limits.md      │
              │ webhooks.md         │
              └──────────┬──────────┘
                         │
                         ▼
                      Chunking
                         │
                         ▼
                  ┌──────────────┐
                  │    Chunks    │
                  └──────┬───────┘
                         │
                         ▼
                 Embedding Model
                         │
                         ▼
                   Vector Embeddings
                         │
                         ▼
              ┌──────────────────────┐
              │   VECTOR DATABASE    │
              │                      │
              │ vectors              │
              │ metadata             │
              │ document/chunk data  │
              │ vector index         │
              │ search engine        │
              └──────────┬───────────┘
                         │
                         │
              ┌──────────┘
              │
              │       USER QUERY
              │           │
              │           ▼
              │    "How do I authenticate?"
              │           │
              │           ▼
              │     Embedding Model
              │           │
              │           ▼
              │      Query Vector
              │           │
              └──────────►│
                          ▼
                   Vector Search
                          │
                          ▼
                   Top-K Chunks
                          │
                          ▼
                   LLM Context
                          │
                          ▼
                     LLM / GPT
                          │
                          ▼
                       Answer
```



That is the RAG architecture you're gradually building