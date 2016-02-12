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

package ru.reo7sp.gthtt.analyzer

import ru.reo7sp.gthtt.tedvideo.Rating

case class Report(themes: Iterable[Theme]) {
  def merge(other: Report) = {
    val newThemes = (themes ++ other.themes).par.groupBy(_.name).map {
      case (name, similarThemes) =>
        val ratings = similarThemes.head.ratings.map(_.value).toBuffer
        similarThemes.tail.foreach { theme =>
          theme.ratings.foreach(r => ratings(r.id) += r.value)
        }
        Theme(name, ratings.zipWithIndex.map(v => Rating(v._2, v._1)))
    }.seq
    Report(newThemes)
  }
}
