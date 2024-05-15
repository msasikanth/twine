/*
 * Copyright 2024 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.sasikanth.rss.reader.reader.ui

import androidx.compose.runtime.Immutable
import twine.shared.generated.resources.Res

internal suspend fun readerHTML(
  title: String,
  feedName: String,
  feedHomePageLink: String,
  publishedAt: String,
  colors: ReaderHTMLColors,
): String {
  val readabilityJS = Res.readBytes("files/readability.js").decodeToString()

  // language=HTML
  return """
    <html lang="en">
    <head>
      <link rel="preconnect" href="https://fonts.googleapis.com">
      <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
      <link rel="preload" as='style' href="https://fonts.googleapis.com/css2?family=Golos+Text:wght@400;500&display=swap">
      <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Golos+Text:wght@400;500&display=swap">
      <link rel="preload" as='style' href="https://fonts.googleapis.com/css2?family=Source+Code+Pro&display=swap">
      <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Source+Code+Pro&display=swap">
      <title>$title</title>
    </head>
    <style>
      ${ReaderCSS.content(colors)}
    </style>
    <body>
      <script>${ReaderJs.content}</script>
      <script>$readabilityJS</script>
      <script>
        async function parse_content(link, html) {
          console.log('Parsing content'); 
          const result = await parse(html, link);
          const content = result.content || html;
    
          document.getElementById("content").innerHTML += content;
          
          processLinks();
          processIFrames();
          removeTitle();
        }
      </script>
      <h1 id='title'>$title</h1>
    
      ${feedSection(
        feedName = feedName,
        feedHomePageLink = feedHomePageLink,
        publishedAt = publishedAt,
        hasTitle = title.isNotBlank()
      )}
      
      <div id='content' />
    </body>
    </html>
        """
    .trimIndent()
}

private fun feedSection(
  feedName: String,
  feedHomePageLink: String,
  publishedAt: String,
  hasTitle: Boolean,
): String {
  return buildString {
    if (hasTitle) {
      appendLine("<hr class=\"top-divider\">")
    }

    appendLine(
      """
      <div class ="feedName"><a href='$feedHomePageLink'>$feedName</a></div>
      <div class="caption">$publishedAt</div>
      <hr class="top-divider">
    """
        .trimIndent()
    )
  }
}

private object ReaderJs {

  // language=JS
  val content =
    """
    function findHref(node, maxDepth = 4) {
        let currentDepth = 0;
        
        while (node && currentDepth < maxDepth) {
            if (node.tagName && node.tagName.toLowerCase() === 'a' && node.hasAttribute('href')) {
              return node.getAttribute("href");
            }
    
            node = node.parentNode;
            currentDepth++;
        }
    
        return null;
    }
    
    function handleLinkClick(event) {
        try {
          event.preventDefault();
          var href = findHref(event.target);
          if (href == undefined || href == "/") {
            href = ""
          }
          window.kmpJsBridge.callNative(
            "linkHandler", 
            href, 
            {}
          );
        } catch(err) {
          // no-op
        }
    }
    
    function processLinks() {
      let links = document.querySelectorAll("a")
      for (let i=0, max=links.length; i<max; i++) {
        let link = links[i];
        link.addEventListener("click", handleLinkClick);
      }
    }

    function processIFrames() {
      let iframes = document.querySelectorAll("iframe")
      for (let i=0, max=iframes.length; i<max; i++) {
        let iframe = iframes[i];
        if (iframe.hasAttribute("data-runner-src")) {
          iframe.src = String(iframe.getAttribute("data-runner-src"));
        }
      }
    }
    
    // We already display title which are usually h1 tags,
    // so no point keeping the duplicate/next title again in the reader view
    // when we load post source.
    function removeTitle() {
      document.querySelectorAll("h1")[1].style.display = 'none';
    }
  """
      .trimIndent()
}

private object ReaderCSS {

  fun content(colors: ReaderHTMLColors) =
    // language=CSS
    """
    h1 {
      font-size: 24px;
    }
    body {
      padding-top: 16px;
      color: ${colors.textColor};
      font-family: 'Golos Text', sans-serif;
    }
    figure {
      margin: 0;
    }
    figcaption {
      margin-top: 8px;
    	font-size: 14px;
    	line-height: 1.6;
    }
    .caption {
      font-size: 12px;
    }
    .feedName {
      margin-bottom: 8px;
    }
    img, figure, video, div, object {
    	max-width: 100%;
    	height: auto !important;
    	margin: 0 auto;
    }
    a {
      color: ${colors.linkColor};
    }
    ul {
      list-style: none;
      padding-left: 8px;
    }
    ul li::before {
      content: "\2022";
      color: ${colors.textColor};
      margin-right: 0.25em;
    }
    ul li p {
      display: inline;
    }
    ol {
      list-style: none;
      padding-left: 8px;
      counter-reset: item;
    }
    ol li::before {
      counter-increment: item;
      content: counters(item, ".") ".";
      color: ${colors.textColor};
      margin-right: 0.25em;
    }
    ol li p {
      display: inline;
    }
    li:not(:last-of-type) { 
      margin-bottom: 1em; 
    } 
    pre {
    	max-width: 100%;
    	margin: 0;
    	overflow: auto;
    	overflow-y: hidden;
    	word-wrap: normal;
    	word-break: normal;
    	border-radius: 4px;
      padding: 8px;
    }
    pre {
    	line-height: 1.4286;
    }
    code, pre {
      font-family: 'Source Code Pro', monospace;
      font-size: 14px;
    	-webkit-hyphens: none;
    	background: ${colors.codeBackgroundColor};
    }
    code {
    	padding: 1px 2px;
    	border-radius: 2px;
    }
    pre code {
    	letter-spacing: -.027em;
    	font-size: 1.15em;
    }
    .top-divider {
      margin-top: 12px;
      margin-bottom: 12px;
      border: 1px solid ${colors.dividerColor};
    }
    iframe {
      width: 100%;
      max-width: 100%;
      height: 250px;
      max-height: 250px;
    }
    blockquote {
      margin-left: 8px;
      padding-left: 8px;
      border-left: 4px solid ${colors.linkColor}
    }
    table {
      display: flex;
      flex-direction: column;
    }

    table img {
      max-width: 100%;
      height: auto;
      margin-bottom: 16px;
    }

    table td {
      display: block;
    }
  """
      .trimIndent()
}

@Immutable
internal data class ReaderHTMLColors(
  val textColor: String,
  val linkColor: String,
  val codeBackgroundColor: String,
  val dividerColor: String
)
