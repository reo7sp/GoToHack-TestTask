/*
 * The MIT License (MIT)
 * Copyright (c) 2016 Oleg Morozenkov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ru.reo7sp.gthtt

import scala.io.Source

package object english {
  val prefixes = Seq("contra", "circum", "trans", "therm", "super", "micro", "macro", "intra", "inter", "infra", "hyper", "extra", "semi", "post", "para", "para", "omni", "mono", "magn", "homo", "homo", "fore", "anti", "ante", "uni", "tri", "tri", "sub", "sub", "pre", "non", "mis", "mid", "epi", "dis", "con", "com", "un", "re", "ir", "in", "in", "im", "il", "ex", "ex", "en", "em", "de", "co", "an", "a")

  val adverbSuffixes = Set("ly", "wards", "wise")
  val verbSuffixes = Set("ing", "ed", "en", "ate", "fy", "ize", "ise")
  val adjectiveSuffixes = Set("able", "ible", "al", "ant", "ary", "ent", "esque", "est", "ful", "ic", "ish", "ive", "less", "ous", "y", "er", "est")
  val nounSuffixes = Set("s", "ance", "ancy", "acy", "ence", "ency", "ion", "sion", "tion", "ate", "fy", "ism", "ity", "logy", "ment", "ness", "ship", "dom", "er", "or", "ty")

  lazy val adverbs = Source.fromURL(getClass.getResource("/adverbs.txt")).getLines.toSet
  lazy val verbs = Source.fromURL(getClass.getResource("/verbs.txt")).getLines.toSet
  lazy val adjectives = Source.fromURL(getClass.getResource("/adjectives.txt")).getLines.toSet

  def removePrefix(word: String) = word.replaceFirst(prefixes.find(word.startsWith) getOrElse "", "")

  def guessWordType(word: String) = {
    def guessBySuffix(word: String) = {
      def checkIfSuffixesAreInSet(word: String, set: Set[String]) = {
        (1 to 4).
          filter(_ < word.length).
          map(i => word.substring(word.length() - i)).
          exists(set.contains)
      }

      if (checkIfSuffixesAreInSet(word, verbSuffixes)) {
        Some(WordType.Verb)
      } else if (checkIfSuffixesAreInSet(word, adverbSuffixes)) {
        Some(WordType.Adverb)
      } else if (checkIfSuffixesAreInSet(word, adjectiveSuffixes)) {
        Some(WordType.Adjective)
      } else if (checkIfSuffixesAreInSet(word, nounSuffixes)) {
        Some(WordType.Noun)
      } else {
        None
      }
    }

    def guessByName(word: String) = {
      if (adverbs contains word) {
        WordType.Adverb
      } else if (verbs contains word) {
        WordType.Verb
      } else if (adjectives contains word) {
        WordType.Adjective
      } else {
        WordType.Noun
      }
    }

    def removeSymbols(word: String) = word.replaceAll("""[\x21-\x40\x5b-\x60\x7b-\x7e]""", "")

    val guessByFilteredName = removeSymbols _ andThen removePrefix andThen guessByName


    guessBySuffix(word) getOrElse guessByFilteredName(word)
  }
}
