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

import org.json4s._
import org.json4s.native.JsonMethods
import ru.reo7sp.gthtt.tedvideo.{Rating, Tag, TedVideoInfo}

import scala.io.Source

package object tedcomParser {
  val jsonInTedComHtmlPattern = """<script>q\("talkPage.init",(.+?)\)</script></div>""".r

  def fetchHtml(id: Int) = Source.fromURL(s"http://ted.com/talks/$id", "UTF-8").getLines.mkString

  def fetchJsonString(id: Int) = jsonInTedComHtmlPattern.findFirstMatchIn(fetchHtml(id)).get.group(1)

  def fetchJson(id: Int) = JsonMethods.parse(fetchJsonString(id))

  def parseJson(json: JValue) = {
    implicit val formats = DefaultFormats

    val talkJson = (json \ "talks")(0)
    val ratingsJson = json \ "ratings"

    val JInt(bigId) = talkJson \ "id"
    val id = bigId.toInt
    val JString(name) = talkJson \ "title"
    val ratings = ratingsJson.children.map { obj =>
      val JString(ratingName) = obj \ "name"
      val JInt(ratingValue) = obj \ "count"
      Rating(ratingName, ratingValue.toInt)
    }
    val tags = (talkJson \ "targeting" \ "tag").extract[String].split(',').map(Tag)

    TedVideoInfo(id, name, ratings, tags)
  }

  val parseWebpage = fetchJson _ andThen parseJson
}
