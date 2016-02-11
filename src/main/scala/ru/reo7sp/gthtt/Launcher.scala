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
        val destDir = new File(args(1))
        val indexFrom = args.lift(2).getOrElse("1").toInt
        val indexTo = args.lift(3).getOrElse("2000").toInt

        println(s"Downloading ${indexTo - indexFrom + 1} subtitles to $destDir")

        downloader.downloadSubs(destDir, indexFrom, indexTo)
      case "filter" =>
        val availableFiles = new File(args(1)).listFiles.toSet.par
        val isRewrite = args.lift(2).contains("-r")
        val files = availableFiles.filter { f =>
          val fName = f.getName
          fName.endsWith(".txt") && (isRewrite || !availableFiles.exists { g =>
            val gName = g.getName
            gName.endsWith(".json") && gName.replace(".json", ".txt") == fName
          })
        }

        println(s"Filtering ${files.size} subtitles")

        filterer.filterSubs(files)
      case "analyze" =>
        val destFile = new File(args(2))
        val files = new File(args(1)).listFiles.par

        println(s"Analyzing ${files.size} videos and saving to $destFile")

        analyzer.saveReport(analyzer.pickBestThemes(files), destFile)
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
        |  download DESTINATION_SUBS_DIR INDEX_FROM=1 INDEX_TO=2000
        |  filter SUBS_DIR OPTIONS
        |    Options:
        |      -r rewrite existing files
        |  analyze SUBS_DIR DESTINATION_FILE
      """.stripMargin)
  }
}
