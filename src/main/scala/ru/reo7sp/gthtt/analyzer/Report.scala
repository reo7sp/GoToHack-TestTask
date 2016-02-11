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

case class Report(themes: Iterable[Theme]) {
  def merge(other: Report) = {
    val newThemes = themes.par.map { theme =>
      other.themes.find(_.name == theme.name) match {
        case Some(otherTheme) => theme.copy(ratings = (theme.ratings zip otherTheme.ratings).map {
          case (r1, r2) => r1.copy(value = r1.value + r2.value)
        })
        case None => theme
      }
    }
    Report(newThemes.seq)
  }
}
