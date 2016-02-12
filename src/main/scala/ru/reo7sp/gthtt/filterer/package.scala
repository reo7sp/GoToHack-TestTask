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

import java.io._

import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.native.JsonMethods
import ru.reo7sp.gthtt.english.WordType
import ru.reo7sp.gthtt.tedvideo.TedVideoInfo

import scala.collection.parallel.ParIterable
import scala.io.Source

package object filterer {
  def filterSubs(files: ParIterable[File]): Unit = files.foreach { f =>
    try {
      filterSubs(f)
    } catch {
      case _: FileNotFoundException => f.delete()
    }
  }

  def filterSubs(srcFile: File): Unit = {
    def normalizeText(text: String) = text.
      replace('\n', ' ').
      replace('\r', ' ').
      replaceAll("""\s{2,}""", " ").
      toLowerCase.
      trim

    def removeSymbols(text: String) = text.
      replaceAll("""\(.+?\)""", "").
      replaceAll("""[\x21-\x40\x5b-\x60\x7b-\x7e]""", "")

    def filterText(text: String) = {
      val filteredText = normalizeText(removeSymbols(text))
      filteredText.split(' ').filter(english.guessWordType(_) == WordType.Noun).mkString(" ")
    }

    def load(file: File) = Source.fromFile(file).mkString

    def save(json: JValue, file: File): Unit = {
      val writer = new PrintWriter(file)
      try {
        writer.write(JsonMethods.pretty(JsonMethods.render(json)).replaceAll("""\\u\d{4}""", ""))
      } finally {
        writer.close()
      }
    }

    val id = srcFile.getName.replace(".txt", "").toInt
    val video = TedVideoInfo(id)

    // @formatter:off
    val json =
      ("id" -> id) ~
      ("name" -> video.name) ~
      ("text" -> filterText(load(srcFile))) ~
      ("tags" -> video.tags) ~
      ("ratings" -> video.ratings.map(_.value))
    // @formatter:on

    save(json, new File(srcFile.getParentFile, s"$id.json"))
  }
}
