---
title: Hangul-utils, a Clojure Tool for Deconstructing Korean Syllables 
uuid: 8FD045F0-DB70-44BD-A4DD-23B02076E953
date-published: 2017-05-15
tags:
    - Clojure
    - Korean
---

While looking at Peter
Norvig's [spelling corrector](http://norvig.com/spell-correct.html) and
attempting to adapt it for Korean text, I found that the agglutinative (from
Latin, *to glue together*) nature of written Hangul posed some problems. In
English, the alphabet characters which you type appear on screen, unchanged, in
that order. In Korean, one types in the letters "ㄱ", "ㅏ", "ㄴ", and
the combined character "간" is formed. Thus to check the spelling of Korean
words, I needed some way to translate between the list-of-inputs representation
and the agglutinated representation.

I found a Python [library](https://github.com/kaniblu/hangul-utils) written for
this purpose, but it wasn't quite what I needed, so I took this opportunity to
write my first public
Clojure [library](https://github.com/sooheon/hangul-utils).

## How it works
Korean syllables in unicode follow a simple [formula](https://en.wikipedia.org/wiki/Korean_language_and_computers#Hangul_in_Unicode) based on the code points of constuent *jamo* (letters).

Rather than immediately outputting a transformed string (say from "안녕하세요"
to "ㅇㅏㄴㄴㅕㅇㅎㅏㅅㅔㅇㅛ"), the library represents the deconstructed text as
a vector of vectors of jamo first.

```clojure
(deconstruct-str "안녕하세요")
;; => [[\ㅇ \ㅏ \ㄴ] [\ㄴ \ㅕ \ㅇ] [\ㅎ \ㅏ] [\ㅅ \ㅔ] [\ㅇ \ㅛ]]
```

This makes it simple to do operations such as take the initial consonants (초성,
a common way of abbreviating Korean) or medial vowels of each word, and getting
the full string is of course still easy:

```clojure
(str/join (map first (deconstruct-str "안녕하세요")))
;; => "ㅇㄴㅎㅅㅇ"

(str/join (map second (deconstruct-str "안녕하세요")))
;; => "ㅏㅕㅏㅔㅛ"

(str/join (flatten (deconstruct-str "안녕하세요")))
;; => "ㅇㅏㄴㄴㅕㅇㅎㅏㅅㅔㅇㅛ"

;; Convenience fn for the above:
(alphabetize "안녕하세요")
;; => "ㅇㅏㄴㄴㅕㅇㅎㅏㅅㅔㅇㅛ"
```

Reconstructing the syllables from a string of morphemes is a bit trickier.
Hangul consonants can both start and end a syllable ("각" for example begins and
ends with "ㄱ"), so you can't tell whether a given consonant is the start of a
new syllable or the end of the last one without some context. If you kept the
vector of vectors representation, this is simple because the inner vectors
delineate syllables. If you are going from a flat string of morphemes like
"ㅇㅏㅍ ㅈㅣㅂ ㅍㅏㅌㅈㅜㄱㅇㅡㄴ ㅂㅜㄺㅇㅡㄴ ㅍㅏㅌ.", you need to do a bit of
backtracking for context.

I took a look at how it was done in the Python library, under the function
[`join_jamos`](https://github.com/kaniblu/hangul-utils/blob/master/hangul_utils/__init__.py#L144),
but found the logic difficult to follow and translate to Clojure. After some
thought, the following is my stab at a more idiomatic, Clojurian approach.

```clojure
(defn syllabize
  "Takes a string of Korean alphabets, and reconstructs Korean text. The initial
  value for the reduce fn is a vector containing the accumulated result, the
  current syllable under consideration, and the most recent consonant in
  limbo (to be classified as initial or final).

  Each new char read in from the input string is either added to the current
  syllable vector or sent into limbo, and can trigger the syllable to be conj'd
  onto the accumulator once it's fully constructed.

  The cond branches could use more cleanup."
  [s]
  (let [[acc syl limbo]
        (reduce
         (fn [[acc syl limbo] c]
           (cond
             (and (empty? syl) (initial? c))     [acc [c] nil]
             (and (= 1 (count syl)) (not limbo)) (if (medial? c) [acc (conj syl c) nil]
                                                     [(conj acc syl) [c] nil])
             (and (not limbo) (final? c))        [acc syl c]
             (and limbo (initial? c))            [(conj acc (conj syl limbo)) [c] nil]
             (and limbo (medial? c))             [(conj acc syl) [limbo c] nil]
             :else                               [(conj acc (conj syl limbo) [c]) [] nil]))
         [[] [] nil]
         s)]
    (construct-str (conj acc (conj syl limbo)))))
```

The Python code defines a `queue` to hold consonants under consideration and a
`flush` function to clear it once it is determined where it should go. Because
the Clojure version leverages `reduce` to accumulate the result *and* carry
along the unclassified consonant, it results in more concise and readable (to my
eyes) code--if you know `reduce`, you know how this function works.

Unfortunately, I couldn't find a way to add a "post-reduce" step utilizing the
same bindings ("acc" "syl" and "limbo") as within the `reduce` form, so you see
the additional `let` form destructuring the same bindings again--redundant and
inelegant, but it works. If anyone know a better way to approach this kind of
problem, or any obvious improvements to make for reasonability/robustness,
feedback would be much appreciated.

