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

import java.util.concurrent.Semaphore

import org.json4s._
import org.json4s.native.JsonMethods
import ru.reo7sp.gthtt.tedvideo.{Rating, TedVideoInfo}

import scala.io.Source

package object tedcomParser {
  val jsonInTedComHtmlPattern = """<script>q\("talkPage.init",(.+?)\)</script></div>""".r
  val tedcomDownloadSemaphore = new Semaphore(2)

  def fetchHtml(id: Int) = Source.fromURL(s"http://ted.com/talks/$id", "UTF-8").getLines().mkString

  def pickJsonString(html: String) = jsonInTedComHtmlPattern.findFirstMatchIn(html).get.group(1)

  def pickJson(jsonString: String) = JsonMethods.parse(jsonString)

  def fetchJson(id: Int) = pickJson(pickJsonString(fetchHtml(id)))

  def parseJson(json: JValue) = {
    implicit val formats = DefaultFormats

    val talkJson = (json \ "talks")(0)
    val ratingsJson = json \ "ratings"

    val id = (talkJson \ "id").extract[Int]
    val name = (talkJson \ "title").extract[String]
    val ratings = ratingsJson.children.map { obj =>
      Rating((obj \ "name").extract[String], (obj \ "count").extract[Int])
    }.sortBy(_.id)
    val tags = (talkJson \ "targeting" \ "tag").extract[String].split(',')

    TedVideoInfo(id, name, ratings, tags)
  }

  def parseWebpage(id: Int) = {
    tedcomDownloadSemaphore.acquire()
    val html = try {
      fetchHtml(id)
    } finally {
      tedcomDownloadSemaphore.release()
    }
    parseJson(pickJson(pickJsonString(html)))
  }
}
