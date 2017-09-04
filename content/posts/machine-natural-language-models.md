---
title: Machine Natural Language Models pt. 1
uuid: a21b8415-fb79-4d2b-be26-ebd48ddcd8dd
draft: false
date-published: 2017-09-04
tags:
 - Natural Language Processing
 - Korean
---

How do you get a computer to grok a body of text? Is it possible to reimplement
the complex human faculty of language on silicone, when we hardly understand
its reference implementation in our own brains?

Our attempts to tackle this question bit by bit, problem by problem, have
formed the story of Natural Language Processing. This series of blog posts
aspires to be one account of this story with a special interest in Korean NLP.

# Bag of Words

One of the basic approaches is the "Bag-of-words" model, which is exactly
what it sounds like: it models bodies of text ("documents") as collections of
words. Put in a document, and what you get out is a bag of words, with
duplicates--i.e. you receive information about *which* words appear, how many
times. What are the implications of this, and why would one choose such a
model?

First, one loses word order, and therefore *most* grammar. Most, not all, because
some languages assign semantic roles in the word forms themselves.<sup><a
href="#fn1" id="r1">[1]</a></sup>

What you get in return is a simplified, compressed representation of the text
which can still answer one question: "what is this text about?"

Whether the word "baseball" is the subject or the object of a sentence, its
mere presence says something about the topic of that document. If your goal is
not full semantic understanding of the text, but a high-level classification, a
bag-of-words approach is not a terrible tradeoff.

## Bag of Words as a Document Vector

One way to represent such a bag of words is as a hash-map from WORD to COUNT:

```clojure
(frequencies
 (clojure.string/split "this is a world premiere" #" "))

=> {"this" 1, "is" 1, "a" 1, "world" 1, "premiere" 1}
```

To make it even easier for machines to parse, the same information can be
encoded as a vector of numbers, where each index implicitly represents a word.

```txt
"This is a world premiere" => [1 1 1 1 1]
```

```txt
"I got I got I got I got loyalty got royalty inside my DNA"
=> [4 5 1 1 1 1]
```

Finally, for these vectors to be valid inputs to many machine learning
algorithms, they must be of equal length. To represent different documents in
the same vector space (so they can be compared), the length of the vector
should be equal to the number of words in the vocabulary under consideration.
I.e. a bag of words model taking into account the 2,000 most common English
words should use a 2,000 element vector.

```txt
"Four score and seven years ago ..."
=> [0 0 1 0 0 0 3 8 9 0 0 1 2 0 1 0 ... ]
```

This is a way of "embedding" documents into a 2,000 dimension vector space. In
one sense, this embedding is a lossy form of compression of the text, losing
semantics and grammar. The flipside is that because computers are good at
dealing with fixed-length vectors of numbers, these make very good feature
inputs to machine learning algorithms (i.e. Bayesian spam filters).

## "Better" Feature Vectors

The standout limitation in the bag-of-words vectors proposed above is this: one
must decide up front how many words to include in the vocabulary, and this
number is not flexible. Any out-of-vocabulary words one encounters must be
discarded, or all existing vectors must be recalculated.

More words in the vocabulary means more dimensions to the vector. Thus each
typical vector becomes more sparse (more empty indices), with consequences for
training speed and memory usage.

Another less obvious limitation is that there is no way to encode similarities
or relationships among the words in this vector. To humans, the words "Paris"
and "Parisienne" have some semantic and syntactic relation. In a BOW model, the
two words are exactly as orthogonal as any other pair of words.

```txt
"Paris"      => [1 0 0 0 ...]
"Parisienne" => [0 1 0 0 ...]
"Cow"        => [0 0 1 0 ...]
```

Finally, simple word counts do not take into account the different information
values of each word. Common grammatical constructs such as "a", "and", and
"the" are the most frequent words, but contain the least distinguishing
information.

Many techniques exist to address these limitations. TF-IDF weighting takes into
account the frequencies of words across documents to de-value common words, and
pump up less common, potentially more salient words.

Our takeaway is that "embedding" is a simple concept. All it means is to map a
complex, human-friendly piece of information (such as a string representing a
word) to the machine-friendly form of a fixed-length vector.

Next, we will explore the breakthrough of word level embeddings, and how this
is now being extrapolated to character-level and morpheme-level embeddings.

<!-- # The Elegance of Word Embeddings -->

<!-- ## Morpheme based representations -->

<!-- ## Character-level representations -->

<hr>

## Footnotes

<section> <p id="fn1"><a href="#r1">[1]</a>

- Semantic role assignment by word order:
    - "The dog booped the cat on the blanket"
    - "The cat booped the blanket on the dog"

- Semantic role assignment by word forms (agglutinated suffixes):
    - "고양이**는** 마당**에서** 강아지**를** 쫓았다"
    - "마당**에서** 고양이**는** 쫓았다, 강아지**를**"

Both Korean sentences mean "the cat chased the dog in the yard", because the
bolded suffixes, *not* the word orderings, assign the semantic relations.
</p>
</section>
