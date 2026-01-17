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

function processIFrames(doc) {
  const iframes = doc.querySelectorAll("iframe");
  iframes.forEach((iframe) => {
    if (iframe.hasAttribute("data-runner-src")) {
      iframe.src = iframe.getAttribute("data-runner-src");
    }
  });
}

/**
 * Converts custom Reddit "Shreddit" elements (e.g., <shreddit-gallery>) into standard
 * <div> elements. This ensures that Readability and Turndown can correctly process
 * the content, as they often ignore or mishandle custom web components.
 */
function transformShredditElements(doc) {
  const elements = Array.from(doc.querySelectorAll("*"));
  elements.forEach((el) => {
    if (!el.parentNode) return;

    const tagName = el.tagName.toLowerCase();
    if (tagName.startsWith("shreddit-")) {
      const div = doc.createElement("div");
      for (const attr of el.attributes) {
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
  });
}

/**
 * Specifically targets Reddit pages (both modern Shreddit and legacy UI) to extract
 * the main post content, effectively isolating it from sidebars, comments, and
 * other "noise" that might confuse Readability.
 */
function processRedditPost(doc) {
  const post = doc.querySelector("shreddit-post");
  if (post) {
    const contentContainer = doc.createElement("div");

    const mediaContainer = post.querySelector('[slot="post-media-container"]');
    const textBody = post.querySelector('[slot="text-body"]');
    const gallery = post.querySelector("shreddit-gallery");

    if (mediaContainer) contentContainer.appendChild(mediaContainer);
    if (textBody) contentContainer.appendChild(textBody);
    if (!mediaContainer && !textBody && gallery) contentContainer.appendChild(gallery);

    const postType = post.getAttribute("post-type");
    const contentHref = post.getAttribute("content-href");
    if ((postType === "link" || contentContainer.childNodes.length === 0) && contentHref) {
      const p = doc.createElement("p");
      const a = doc.createElement("a");
      a.href = contentHref;
      a.textContent = contentHref;
      p.appendChild(a);
      contentContainer.appendChild(p);
    }

    const author = post.getAttribute("author");
    if (author) {
      const p = doc.createElement("p");
      p.innerHTML = `submitted by <a href="https://www.reddit.com/user/${author}">/u/${author}</a>`;
      contentContainer.appendChild(p);
    }

    if (contentContainer.childNodes.length > 0) {
      doc.body.innerHTML = "";
      doc.body.appendChild(contentContainer);
    }
    return;
  }

  // Legacy Reddit support (e.g., from RSS snippets)
  const textBody = doc.querySelector("div.md");
  if (textBody) {
    const contentContainer = doc.createElement("div");
    contentContainer.appendChild(textBody.cloneNode(true));

    const links = Array.from(doc.querySelectorAll("a"));
    const authorLink = links.find(
      (a) => a.href.includes("/user/") || a.textContent.includes("/u/"),
    );

    if (authorLink) {
      const authorMatch = authorLink.href.match(/\/user\/([^/]+)/);
      const authorName = authorMatch
        ? authorMatch[1]
        : authorLink.textContent.replace("/u/", "").trim();
      const p = doc.createElement("p");
      p.innerHTML = `submitted by <a href="https://www.reddit.com/user/${authorName}">/u/${authorName}</a>`;
      contentContainer.appendChild(p);
    }

    if (contentContainer.childNodes.length > 0) {
      doc.body.innerHTML = "";
      doc.body.appendChild(contentContainer);
    }
  }
}

function isRedditUrl(url) {
  const redditDomainPattern = /^https?:\/\/(?:www\.|old\.|new\.|i\.)?reddit\.com|redd\.it/i;
  return redditDomainPattern.test(url);
}

function isXkcdUrl(url) {
  const xkcdDomainPattern = /^https?:\/\/(?:www\.)?xkcd\.com/i;
  return xkcdDomainPattern.test(url);
}

function removeHeadTag(html) {
  return html.replace(/<head[^>]*>[\s\S]*?<\/head>/i, "");
}

function removeFirstH1(doc) {
  doc.querySelector("h1")?.remove();
}

function removeFirstImageTagByUrl(doc, imageUrl) {
  if (!imageUrl) return;

  const img = doc.querySelector(`img[src="${imageUrl}"]`);
  if (img) {
    img.closest("figure")?.remove() || img.remove();
  }
}

function getImageCaption(markdown) {
  const captionPattern = /!\[.*\]\(.*\s+"(.*)"\)/;
  const match = markdown.match(captionPattern);
  if (match && match[1]) {
    return match[1];
  }
  return null;
}

async function parseReaderContent(link, bannerImage, html) {
  const cleanedHtml = removeHeadTag(html);
  const sanitizedHtml = `<body><div>${cleanedHtml}</div></body>`;

  let doc;
  try {
    const parser = new DOMParser();
    const htmlWithBase = `<head><base href="${link}"></head>${sanitizedHtml}`;
    doc = parser.parseFromString(htmlWithBase, "text/html");

    const turndownService = new TurndownService();

    if (isXkcdUrl(link)) {
      const markdown = turndownService.turndown(doc.documentElement.outerHTML);
      const imageCaption = getImageCaption(markdown);

      return JSON.stringify({ content: imageCaption, excerpt: null });
    } else {
      if (isRedditUrl(link)) {
        processRedditPost(doc);
      }

      removeFirstH1(doc);
      processIFrames(doc);
      transformShredditElements(doc);
      removeFirstImageTagByUrl(doc, bannerImage);

      const article = new Readability(doc).parse();
      const markdown = turndownService.turndown(article.content);
      return JSON.stringify({ content: markdown, excerpt: article.excerpt });
    }
  } catch (error) {
    console.error("Error parsing HTML:", error);
    return null;
  }
}
