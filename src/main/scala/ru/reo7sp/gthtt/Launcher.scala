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

import java.io.File

object Launcher extends App {
  try {
    args(0) match {
      case "download" =>
        downloader.downloadSubs(new File(args(1)), args.lift(2).getOrElse("1").toInt, args.lift(3).getOrElse("2000").toInt)
      case "filter" =>
        filterer.filterSubs(new File(args(1)).listFiles)
      case "analyze" =>
        analyzer.saveReport(analyzer.pickBestThemes(new File(args(1)).listFiles), new File(args(2)))
    }
  } catch {
    case e: ArrayIndexOutOfBoundsException =>
      System.err.println(e)
      printHelp()
      System.exit(1)
  }

  def printHelp(): Unit = {
    println(
      """
        |Usage: gthtt MODE OPTIONS
        |
        |Modes:
        |    download DESTINATION_SUBS_DIR INDEX_FROM=1 INDEX_TO=2000
        |    filter SUBS_DIR
        |    analyze SUBS_DIR DESTINATION_FILE
      """.stripMargin)
  }
}
