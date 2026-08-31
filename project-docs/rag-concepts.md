# RAG


This is the pipeline we are aiming for:

<p>
<img src="/images/pipeline.png" alt="Table example" width="300">
</p>


# About

We have a collection of technical documentation. Rather than asking an LLM to answer questions from its general knowledge, we'll retrieve relevant passages from our documentation and provide those passages as context to the model.




# Embeddings

```
An embedding is a numerical representation of text that captures aspects of its meaning.

```

Let's say that chunking produced this chunk "The API requires and API key..."

That chunk is passed off to the embedding model that has a vector:

<p>
<img src="/images/embedding-pipeline.png" alt="Table example" width="600">
</p>

The vector is the **embedding**. The important thing is that the vector captures semantic information about the text.

For example, we'd expect these two chunks to have relatively similar embeddings:

`The API requires an API key for authentication.`

`Clients must authenticate using a valid API key.`

Even though they don't use exactly the same words.

But this:

`Webhooks notify applications when events occur.`

should be semantically farther away than the API key information.

Later, we'll exploit those distances to retrieve relevant chunks.


# First: what an embedding actually is

An embedding is a numerical representation of text that captures aspects of its meaning.

Take these two chunks:

`The API requires an API key for authentication.`

and:

`Clients must authenticate requests using a valid API key.`

The wording is different, but semantically they're very close.

An embedding model turns each into a vector:

"The API requires an API key..."
↓
[0.018, -0.043, 0.217, ...]


"Clients must authenticate..."
↓
[0.021, -0.039, 0.225, ...]

Those vectors occupy nearby positions in a high-dimensional mathematical space.

Conversely:

"The API supports webhook subscriptions."

would produce a vector that should be farther away.

That's the trick RAG exploits.

Instead of searching for matching words, we can search for *semantically similar text*.

## What are all those numbers?

This is worth understanding.

An embedding isn't:

`authentication = 0.87`
`webhooks = 0.14`

It's more like a point in a space with hundreds or thousands of dimensions:

                    dimension 1
                        │
                        │
              •         │
          •             │
                        │
                        └──────── dimension 2

Except there aren't two dimensions. There may be hundreds or thousands.

With text-embedding-3-small, for example, the standard embedding has 1,536 dimensions. The model can also produce shorter embeddings by specifying a smaller dimensions value.

We don't need to understand what dimension 847 "means." The useful property is the relationship between vectors.

## And here's where RAG gets interesting

Suppose our documentation contains:

The API allows 100 requests per minute per API key.

We generate its embedding and store it.

Later, the user asks:

`How many calls can I make?`

We generate an embedding for the question.

```
Question
   ↓
embedding
   ↓
[0.12, -0.07, 0.31, ...]
```

Then compare that vector against the vectors we've stored.

The rate-limit chunk should rank highly even though the question doesn't contain:

"100 requests per minute"

That's semantic retrieval.