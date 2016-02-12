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

import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods
import ru.reo7sp.gthtt.tedvideo.Rating

import scala.collection.mutable
import scala.io.Source

package object csvExporter {
  def export(reportFile: File, destFile: File, indexFrom: Int, indexTo: Int): Unit = {
    type Column = IndexedSeq[String]

    implicit val formats = DefaultFormats

    def load(file: File) = JsonMethods.parse(Source.fromFile(file).mkString)

    def save(table: IndexedSeq[Column], file: File) = {
      val writer = new PrintWriter(destFile)
      try {
        for (y <- table(0).indices) {
          for (x <- table.indices) {
            writer.print(table(x)(y))
            writer.print(",")
          }
          writer.println()
        }
      } finally {
        writer.close()
      }
    }

    var table = new mutable.ArrayBuffer[Column]

    table += IndexedSeq("name", "popularity") ++ Rating.names

    val json = load(reportFile)
    json.children.slice(indexFrom - 1, indexTo).foreach { themeJson =>
      table += IndexedSeq((themeJson \ "name").extract[String], (themeJson \ "popularity").extract[String]) ++ (themeJson \ "ratings").children.map(_.extract[String])
    }

    save(table, destFile)
  }
}
