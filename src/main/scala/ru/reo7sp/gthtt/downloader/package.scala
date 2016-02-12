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

import scala.io.Source
import scala.util.control.NonFatal

package object downloader {
  def downloadSubs(destDir: File, indexFrom: Int, indexTo: Int): Unit = {
    def download(i: Int) = {
      Source.fromURL(s"http://www.ted.com/talks/subtitles/id/$i/lang/en/format/srt", "UTF-8").getLines().
        filterNot(s => s.isEmpty || s(0).isDigit)
    }

    def save(lines: TraversableOnce[String], file: File): Unit = {
      val writer = new PrintWriter(file)
      try {
        lines.foreach(writer.println)
      } finally {
        writer.close()
      }
    }

    (indexFrom to indexTo).par.foreach { i =>
      try {
        save(download(i), new File(destDir, s"$i.txt"))
      } catch {
        case NonFatal(e) => System.err.println(s"Error while downloading $i. $e")
      }
    }
  }
}
