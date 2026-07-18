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
  ".sharedaddy", ".jp-relatedposts", ".share-links", ".share-tools",
  ".social-share", ".share-container", ".entry-utility", ".post-tags",
  ".post-categories", ".subscription-widget-container", ".sub-button-container"
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
 * Removes "Read More" links that are likely internal and redundant.
 */
function removeReadMoreLinks(doc) {
  var links = doc.querySelectorAll("a");
  for (var i = 0; i < links.length; i++) {
    var a = links[i];
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

function getBestSrcFromSrcset(srcset) {
  if (!srcset) return null;
  // Split on ", " rather than any comma: some CDNs (e.g. Wired's image
  // transforms) put literal commas inside the URL itself
  // (".../w_640,c_limit/photo.jpg"), and a bare comma-split shreds those
  // URLs apart. Candidates are conventionally comma+whitespace separated,
  // while in-URL commas are never followed by whitespace.
  var parts = srcset.split(/,\s+/);
  var bestSrc = null;
  var maxVal = -1;

  for (var i = 0; i < parts.length; i++) {
    var part = parts[i].trim();
    if (!part) continue;

    var entry = part.split(/\s+/);
    var url = entry[0];
    if (entry.length === 1) {
      if (!bestSrc) bestSrc = url;
      continue;
    }

    var descriptor = entry[1].toLowerCase();
    var val = parseFloat(descriptor);
    if (isNaN(val)) {
      if (!bestSrc) bestSrc = url;
      continue;
    }

    if (val > maxVal) {
      maxVal = val;
      bestSrc = url;
    }
  }
  return bestSrc;
}

function getBestSrc(node) {
  var srcset = node.getAttribute("srcset") || node.getAttribute("data-srcset");
  if (srcset) {
    var best = getBestSrcFromSrcset(srcset);
    if (best) return best;
  }

  return (
    node.getAttribute("src") ||
    node.getAttribute("data-src") ||
    node.getAttribute("data-runner-src") ||
    node.getAttribute("data-lazy-src") ||
    ""
  );
}

function urlPath(url, baseURI) {
  try {
    return new URL(url, baseURI).pathname;
  } catch (e) {
    return url;
  }
}

function removeFirstImageTagByUrl(doc, imageUrl) {
  if (!imageUrl) return;

  var bannerPath = urlPath(imageUrl, doc.baseURI);
  var imgs = Array.prototype.slice.call(doc.querySelectorAll("img"));

  for (var i = 0; i < imgs.length; i++) {
    var img = imgs[i];
    var src = getBestSrc(img);
    if (src && urlPath(src, doc.baseURI) === bannerPath) {
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

/**
 * Removes duplicate images that are essentially the same (e.g., responsive variants).
 * It uses normalizeUrl to strip query parameters and hashes for comparison.
 */
function deduplicateImages(doc) {
  var imgs = Array.prototype.slice.call(doc.querySelectorAll("img"));
  var seenUrls = [];

  for (var i = 0; i < imgs.length; i++) {
    var img = imgs[i];
    var src = getBestSrc(img);

    if (!src) continue;

    var normalizedUrl = normalizeUrl(src, doc.baseURI);
    if (seenUrls.indexOf(normalizedUrl) !== -1) {
      var nodeToRemove = img;
      var figure = img.closest("figure");
      var picture = img.closest("picture");
      var parentA = img.closest("a");

      if (figure) {
        nodeToRemove = figure;
      } else if (picture) {
        nodeToRemove = picture;
      } else if (parentA && parentA.querySelectorAll("img").length === 1) {
        // Only remove the anchor if it contains no other images
        nodeToRemove = parentA;
      }

      if (nodeToRemove && nodeToRemove.parentNode) {
        nodeToRemove.remove();
      }
    } else {
      seenUrls.push(normalizedUrl);
    }
  }
}

/**
 * Readability treats any aria-hidden="true" node as invisible and deletes
 * it outright (see its _isProbablyVisible check), but aria-hidden only
 * affects the accessibility tree, never visual rendering. Sites commonly
 * mark a duplicate icon-only link (e.g. an image wrapped in a link) as
 * aria-hidden next to a more descriptive link, for screen readers -
 * content that is fully visible to sighted users. Stripping the attribute
 * before Readability runs avoids losing that visible content, while
 * Readability's separate display/visibility/hidden-attribute checks still
 * catch content that is actually hidden.
 */
function stripAriaHidden(doc) {
  var hiddenNodes = doc.querySelectorAll("[aria-hidden]");
  for (var i = 0; i < hiddenNodes.length; i++) {
    hiddenNodes[i].removeAttribute("aria-hidden");
  }
}

function normalizeHeadingText(text) {
  return (text || "").replace(/\s+/g, " ").trim().toLowerCase();
}

/**
 * True when every node between two sibling headings is empty/whitespace
 * text or a non-media element with no text content (e.g. an empty anchor
 * permalink icon). Used to distinguish a stray duplicate heading from a
 * legitimately repeated section title that has real content in between.
 */
function siblingContentIsInsubstantial(previous, current) {
  if (previous.parentNode !== current.parentNode) return false;

  var node = previous.nextSibling;
  while (node && node !== current) {
    if (node.nodeType === 1) {
      var tagName = node.tagName;
      if (
        tagName === "IMG" || tagName === "IFRAME" || tagName === "VIDEO" ||
        tagName === "AUDIO" || tagName === "TABLE" || tagName === "PICTURE"
      ) {
        return false;
      }
      if (node.textContent && node.textContent.replace(/\s+/g, "") !== "") {
        return false;
      }
    } else if (node.nodeType === 3 && node.textContent.replace(/\s+/g, "") !== "") {
      return false;
    }
    node = node.nextSibling;
  }

  return node === current;
}

/**
 * Collapses a heading that is an immediate sibling duplicate of the
 * previously kept heading (same normalized text, nothing substantial in
 * between). Deliberately scoped to direct siblings only, so legitimately
 * repeated section titles elsewhere in the article (FAQ/digest-style
 * posts) are left untouched.
 */
function dedupeAdjacentHeadings(doc) {
  var headings = Array.prototype.slice.call(
    doc.querySelectorAll("h1, h2, h3, h4, h5, h6")
  );

  var lastKept = null;
  for (var i = 0; i < headings.length; i++) {
    var heading = headings[i];
    if (!heading.parentNode) continue;

    if (lastKept) {
      var headingText = normalizeHeadingText(heading.textContent);
      var lastKeptText = normalizeHeadingText(lastKept.textContent);

      if (
        headingText &&
        headingText === lastKeptText &&
        siblingContentIsInsubstantial(lastKept, heading)
      ) {
        heading.remove();
        continue;
      }
    }

    lastKept = heading;
  }
}


function getImageCaption(markdown) {
  var captionPattern = /!\[[^\]]*\]\([^\s)]+\s+"([^"]*)"\)/;
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

        turndownService.addRule("image", {
          filter: "img",
          replacement: function(content, node) {
            var alt = node.alt || "";
            var src = getBestSrc(node);
            if (!src) return "";
            var title = node.title || "";
            var titlePart = title ? ' "' + title + '"' : "";
            var markdown = "![" + alt + "](" + src + titlePart + ")";

            // Check the whole ancestor chain, not just the direct parent:
            // sites commonly wrap the img in <picture> or a similar tag
            // before the enclosing <a>. Forcing blank lines around the
            // image here would break the "linked-image" rule's
            // surrounding [...](href) brackets.
            if (node.closest("a")) {
              return markdown;
            }
            return "\n\n" + markdown + "\n\n";
          }
        });

        turndownService.addRule("linked-image", {
          filter: function(node) {
            return node.nodeName === "A" && node.querySelector("img");
          },
          replacement: function(content, node) {
            var href = node.getAttribute("href");
            if (!href) return content;
            return "\n\n[" + content + "](" + href + ")\n\n";
          }
        });

        if (isXkcdUrl(link)) {
          var markdown = turndownService.turndown(doc.body.innerHTML);
          // The comic itself is shown as the post's banner image, so the
          // hover-text caption is all we need; fall back to the full
          // markdown instead of showing an empty reader page.
          resolve({ content: getImageCaption(markdown) || markdown, excerpt: null });
          return;
        }

        if (isRedditUrl(link)) {
          processRedditPost(doc);
        }

        removeFirstH1(doc);
        stripAriaHidden(doc);
        processIFrames(doc);
        processNoScriptImages(doc);
        transformShredditElements(doc);
        removeFirstImageTagByUrl(doc, bannerImage);

        var junkElements = doc.querySelectorAll(JUNK_SELECTORS);
        for(var i=0; i<junkElements.length; i++) {
            junkElements[i].remove();
        }

        removeReadMoreLinks(doc);
        deduplicateImages(doc);
        dedupeAdjacentHeadings(doc);

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
        // Never surface error text as article content; fall back to a plain
        // conversion of the original HTML, or nothing if even that fails.
        try {
          resolve({ content: turndownService.turndown(html), excerpt: null });
        } catch (fallbackError) {
          resolve({ content: null, excerpt: null });
        }
      }
  });
}
