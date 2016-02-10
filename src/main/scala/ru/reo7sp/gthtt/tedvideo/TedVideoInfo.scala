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

package ru.reo7sp.gthtt.tedvideo

import org.json4s._
import ru.reo7sp.gthtt.tedcomparser

case class TedVideoInfo(id: Int, name: String, ratings: Iterable[Rating], tags: Iterable[Tag])

object TedVideoInfo {
  def apply(id: Int) = {
    val json = tedcomparser.pullJson(id)
    val talkJson = (tedcomparser.pullJson(id) \ "talks") (0)
    val ratingsJson = tedcomparser.pullJson(id) \ "ratings"

    val JString(name) = talkJson \ "title"
    val ratings = ratingsJson.children.map {
      case JObject(List(JField("name", JString(ratingName)), JField("count", JInt(ratingValue)))) => Rating(ratingName, ratingValue.toInt)
    }
    val tags = (talkJson \ "targeting" \ "tag").extract[String].split(',').map(Tag)

    TedVideoInfo(id, name, ratings, tags)
  }
}
