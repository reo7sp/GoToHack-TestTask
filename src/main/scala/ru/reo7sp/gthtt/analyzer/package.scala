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
import ru.reo7sp.gthtt.tedvideo.{Rating, Tag}

import scala.collection.parallel.ParIterable
import scala.io.Source

package object analyzer {
  val tagImportance = 8

  def pickBestThemes(file: File): Report = {
    def load(file: File) = JsonMethods.parse(Source.fromFile(file).mkString)

    implicit val formats = DefaultFormats

    val json = load(file)

    val ratings = (json \ "ratings").children.map(_.extract[Rating]).sortBy(_.name)

    val words = (json \ "text").extract[String].split(' ').par
    val wordStats = words.groupBy(identity).mapValues(_.size)
    val wordRatings = wordStats.mapValues(count => ratings.map(r => r.copy(value = r.value / tagImportance * count)))

    val tags = (json \ "tags").children.par.map(_.extract[Tag]).map(_.name)
    val tagStats = tags.groupBy(identity).mapValues(_.size)
    val tagRatings = tagStats.mapValues(count => ratings.map(r => r.copy(value = r.value * count)))

    val stats = wordRatings.map { case (name, ratings) => Theme(name, ratings) } ++ tagRatings.map { case (name, ratings) => Theme(name, ratings) }

    Report(stats.seq)
  }

  def pickBestThemes(files: ParIterable[File]): Report = files.map(pickBestThemes).reduce(_ merge _)

  def saveReport(report: Report, destFile: File): Unit = {
    def save(json: JValue, file: File) = {
      val writer = new PrintWriter(destFile)
      try {
        writer.write(JsonMethods.pretty(JsonMethods.render(json)))
      } finally {
        writer.close()
      }
    }

    // @formatter:off
    val json = report.themes.toSeq.sortBy(_.ratings.map(_.value).sum).reverse.map { theme =>
      ("name" -> theme.name) ~
      ("ratings" -> theme.ratings.map(rating => rating.name -> rating.value))
    }
    // @formatter:on

    save(json, destFile)
  }
}
