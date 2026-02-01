/*
 * Copyright 2025 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

/** * Combined junk selectors for performance
 */
var JUNK_SELECTORS = [
  ".share", ".social", ".ad", ".promo", ".related", ".newsletter-widget",
  "[id*='share']", "[class*='share']", ".sharedaddy", ".jp-relatedposts"
].join(",");


function processIFrames(doc) {
  var iframes = doc.querySelectorAll("iframe");
  for (var i = 0; i < iframes.length; i++) {
    var iframe = iframes[i];
    var src = iframe.getAttribute("src");
    var lazySrc =
      iframe.getAttribute("data-src") ||
      iframe.getAttribute("data-runner-src") ||
      iframe.getAttribute("data-lazy-src");

    if (!src && lazySrc) {
      iframe.src = lazySrc;
    }
  }
}

/**
 * Extracts images from <noscript> tags. Many sites use this for lazy loading
 * where the actual <img> tag is hidden inside <noscript>.
 */
function processNoScriptImages(doc) {
  var noscripts = doc.querySelectorAll("noscript");
  for (var i = 0; i < noscripts.length; i++) {
    var noscript = noscripts[i];
    var content = noscript.textContent || noscript.innerHTML;
    if (content.includes("<img")) {
      var tempDiv = doc.createElement("div");
      tempDiv.innerHTML = content;
      var imgs = tempDiv.querySelectorAll("img");
      for (var j = 0; j < imgs.length; j++) {
        var img = imgs[j];
        noscript.parentNode.insertBefore(img, noscript);
      }
      noscript.remove();
    }
  }
}

/**
 * Removes common non-content elements that might escape Readability's filtering.
 */
function cleanContent(doc) {
  var junkSelectors = [
    ".share",
    ".social",
    ".ad",
    ".promo",
    ".related",
    ".entry-utility",
    ".post-tags",
    ".post-categories",
    ".subscription-widget-container",
    ".sub-button-container",
    ".newsletter-widget",
    "[id*='share']",
    "[class*='share']",
    "[id*='social']",
    "[class*='social']",
    ".sharedaddy",
    ".jp-relatedposts",
  ];

  for (var i = 0; i < junkSelectors.length; i++) {
    var selector = junkSelectors[i];
    var elements = doc.querySelectorAll(selector);
    for (var j = 0; j < elements.length; j++) {
      elements[j].remove();
    }
  }

  // Remove "Read More" links that are likely internal and redundant
  var links = doc.querySelectorAll("a");
  for (var k = 0; k < links.length; k++) {
    var a = links[k];
    var text = a.textContent.toLowerCase().trim();
    if (text === "read more" || text === "continue reading" || text === "read more...") {
      var p = a.closest("p");
      if (p && p.textContent.trim() === a.textContent.trim()) {
        p.remove();
      } else {
        a.remove();
      }
    }
  }
}

/**
 * Converts custom Reddit "Shreddit" elements (e.g., <shreddit-gallery>) into standard
 * <div> elements. This ensures that Readability and Turndown can correctly process
 * the content, as they often ignore or mishandle custom web components.
 */
function transformShredditElements(doc) {
  var elements = Array.prototype.slice.call(doc.querySelectorAll("*"));
  for (var i = 0; i < elements.length; i++) {
    var el = elements[i];
    if (!el.parentNode) continue;

    var tagName = el.tagName.toLowerCase();
    if (tagName.startsWith("shreddit-")) {
      var div = doc.createElement("div");
      for (var j = 0; j < el.attributes.length; j++) {
        var attr = el.attributes[j];
        try {
          div.setAttribute(attr.name, attr.value);
        } catch (e) {
          // Ignore
        }
      }
      while (el.firstChild) {
        div.appendChild(el.firstChild);
      }
      el.replaceWith(div);
    }
  }
}

/**
 * Specifically targets Reddit pages (both modern Shreddit and legacy UI) to extract
 * the main post content, effectively isolating it from sidebars, comments, and
 * other "noise" that might confuse Readability.
 */
function processRedditPost(doc) {
  var post = doc.querySelector("shreddit-post");
  if (post) {
    var contentContainer = doc.createElement("div");

    var mediaContainer = post.querySelector('[slot="post-media-container"]');
    var textBody = post.querySelector('[slot="text-body"]');
    var gallery = post.querySelector("shreddit-gallery");

    if (mediaContainer) contentContainer.appendChild(mediaContainer);
    if (textBody) contentContainer.appendChild(textBody);
    if (!mediaContainer && !textBody && gallery) contentContainer.appendChild(gallery);

    var postType = post.getAttribute("post-type");
    var contentHref = post.getAttribute("content-href");
    if ((postType === "link" || contentContainer.childNodes.length === 0) && contentHref) {
      var p = doc.createElement("p");
      var a = doc.createElement("a");
      a.href = contentHref;
      a.textContent = contentHref;
      p.appendChild(a);
      contentContainer.appendChild(p);
    }

    var author = post.getAttribute("author");
    if (author) {
      var p2 = doc.createElement("p");
      p2.innerHTML = "submitted by <a href=\"https://www.reddit.com/user/" + author + "\">/u/" + author + "</a>";
      contentContainer.appendChild(p2);
    }

    if (contentContainer.childNodes.length > 0) {
      doc.body.innerHTML = "";
      doc.body.appendChild(contentContainer);
    }
    return;
  }

  // Legacy Reddit support (e.g. from RSS snippets)
  var textBodyLegacy = doc.querySelector("div.md");
  if (textBodyLegacy) {
    var contentContainerLegacy = doc.createElement("div");
    contentContainerLegacy.appendChild(textBodyLegacy.cloneNode(true));

    var links = Array.prototype.slice.call(doc.querySelectorAll("a"));
    var authorLink = null;
    for (var i = 0; i < links.length; i++) {
        var a = links[i];
        if (a.href.includes("/user/") || a.textContent.includes("/u/")) {
            authorLink = a;
            break;
        }
    }

    if (authorLink) {
      var authorMatch = authorLink.href.match(/\/user\/([^/]+)/);
      var authorName = authorMatch
        ? authorMatch[1]
        : authorLink.textContent.replace("/u/", "").trim();
      var p3 = doc.createElement("p");
      p3.innerHTML = "submitted by <a href=\"https://www.reddit.com/user/" + authorName + "\">/u/" + authorName + "</a>";
      contentContainerLegacy.appendChild(p3);
    }

    if (contentContainerLegacy.childNodes.length > 0) {
      doc.body.innerHTML = "";
      doc.body.appendChild(contentContainerLegacy);
    }
  }
}

function isRedditUrl(url) {
  var redditDomainPattern = /^https?:\/\/(?:www\.|old\.|new\.|i\.)?reddit\.com|redd\.it/i;
  return redditDomainPattern.test(url);
}

function isXkcdUrl(url) {
  var xkcdDomainPattern = /^https?:\/\/(?:www\.)?xkcd\.com/i;
  return xkcdDomainPattern.test(url);
}

function removeFirstH1(doc) {
  var h1 = doc.querySelector("h1");
  if (h1) h1.remove();
}

function normalizeUrl(url, baseURI) {
  try {
    var u = new URL(url, baseURI);
    u.search = "";
    u.hash = "";
    return u.href;
  } catch (e) {
    return url;
  }
}

function removeFirstImageTagByUrl(doc, imageUrl) {
  if (!imageUrl) return;

  var normalizedBannerUrl = normalizeUrl(imageUrl, doc.baseURI);
  var imgs = Array.prototype.slice.call(doc.querySelectorAll("img"));

  for (var i = 0; i < imgs.length; i++) {
    var img = imgs[i];
    var src = img.getAttribute("src") || img.getAttribute("data-src");
    if (src && normalizeUrl(src, doc.baseURI) === normalizedBannerUrl) {
      var figure = img.closest("figure");
      if (figure) {
        figure.remove();
      } else {
        img.remove();
      }
      break;
    }
  }
}

function getImageCaption(markdown) {
  var captionPattern = /!\[.*\]\(.*\]\(.*\s+"(.*)"\)/;
  var match = markdown.match(captionPattern);
  if (match && match[1]) {
    return match[1];
  }
  return null;
}

// NOTE: This function returns a Promise, so call it with .then()
function parseReaderContent(link, bannerImage, html) {
  return new Promise(function(resolve, reject) {
      try {
        var parser = new DOMParser();
        var doc = parser.parseFromString(
          "<html><head><base href=\"" + link + "\"></head><body>" + html + "</body></html>",
          "text/html"
        );
        var turndownService = new TurndownService({
            headingStyle: 'atx',
            codeBlockStyle: 'fenced'
        });

        turndownService.addRule("iframe", {
          filter: "iframe",
          replacement: function(content, node) {
            var src = node.getAttribute("src") || node.getAttribute("data-src");
            if (!src) return "";
            var label = src.includes("youtube.com") ? "YouTube Video" : "Video";
            return "\n\n[" + label + "](" + src + ")\n\n";
          }
        });

        if (isXkcdUrl(link)) {
          var markdown = turndownService.turndown(doc.body.innerHTML);
          resolve({ content: getImageCaption(markdown), excerpt: null });
          return;
        }

        if (isRedditUrl(link)) {
          processRedditPost(doc);
        }

        removeFirstH1(doc);
        processIFrames(doc);
        processNoScriptImages(doc);
        transformShredditElements(doc);
        removeFirstImageTagByUrl(doc, bannerImage);

        var junkElements = doc.querySelectorAll(JUNK_SELECTORS);
        for(var i=0; i<junkElements.length; i++) {
            junkElements[i].remove();
        }

        var reader = new Readability(doc, {
            charThreshold: 0,
            keepClasses: false
        });
        var article = reader.parse();

        if (!article) {
            resolve({ content: turndownService.turndown(doc.body.innerHTML), excerpt: "" });
            return;
        }

        var markdownArticle = turndownService.turndown(article.content);

        resolve({
          title: article.title,
          content: markdownArticle,
          excerpt: article.excerpt,
          byline: article.byline
        });

      } catch (error) {
        console.error("Reader Error:", error);
        resolve({ content: "Error parsing content: " + error, excerpt: null });
      }
  });
}
