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

import java.io.{File, PrintWriter}

import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.native.JsonMethods
import ru.reo7sp.gthtt.tedvideo.TedVideoInfo

import scala.io.Source
import scala.util.control.NonFatal

package object filterer {
  def filterSubs(files: TraversableOnce[File]): Unit = files.foreach {
    f =>
      try {
        filterSubs(f)
      } catch {
        case NonFatal(e) => System.err.println(s"Error while filtering $f. $e")
      }
  }

  def filterSubs(srcFile: File): Unit = {
    def removeSymbols(text: String) = text.
      replaceAll("""\(.+?\)""", "").
      replaceAll("""[\x21-\x40\x5b-\x60\x7b-\x7e]""", "")

    def removeNonNouns(text: String) = text.
      replaceAll(???, "")

    def removeRedundantWhitespace(text: String) = text.
      replace('\n', ' ').
      replaceAll(" {2,}", " ").
      trim

    val filterText = removeSymbols _ andThen removeNonNouns andThen removeRedundantWhitespace

    def load(file: File) = Source.fromFile(file).mkString

    def save(json: JValue, file: File): Unit = {
      val writer = new PrintWriter(file)
      try {
        writer.write(JsonMethods.pretty(JsonMethods.render(json)))
      } finally {
        writer.close()
      }
    }

    val id = srcFile.toPath.getFileName.toString.toInt
    val video = TedVideoInfo(id)

    val json =
      ("id" -> id) ~
        ("name" -> video.name) ~
        ("text" -> filterText(load(srcFile))) ~
        ("ratings" -> video.ratings.map { rating =>
          ("name" -> rating.name) ~
            ("value" -> rating.value)
        }) ~
        ("tags" -> video.tags.map { tag =>
          "name" -> tag.name
        })

    save(json, new File(srcFile.getParentFile, s"$id.json"))
  }
}
