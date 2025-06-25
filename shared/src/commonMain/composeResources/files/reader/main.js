function processIFrames(doc) {
  const iframes = doc.querySelectorAll("iframe");
  iframes.forEach((iframe) => {
    if (iframe.hasAttribute("data-runner-src")) {
      iframe.src = iframe.getAttribute("data-runner-src");
    }
  });
}

function isRedditUrl(url) {
  const redditDomainPattern = /^https?:\/\/(?:www\.|old\.|new\.|i\.)?reddit\.com|redd\.it/i;
  return redditDomainPattern.test(url);
}

function formatRedditPost(html) {
  const imagePattern = /<a href="([^"]+)">\s*<img src="([^"]+)"\s*alt="([^"]+)" title="([^"]+)"\s*\/>\s*<\/a>/;
  const submitterPattern = /(?:submitted by\s*<a href="([^"]+)">|user\/([^"]+)[^<]*<\/a>)/;
  const divMdPattern = /(?:<!-- SC_OFF -->)?\s*<div class="md">([\s\S]*?)<\/div>\s*(?:<!-- SC_ON -->)?/;

  const imageMatch = html.match(imagePattern);
  let submitterMatch = html.match(submitterPattern);
  let divMdMatch = html.match(divMdPattern);

  if (!submitterMatch) return null;

  const userUrl = submitterMatch[1];
  const username = submitterMatch[2] ? submitterMatch[2].trim() : '';
  const divContent = divMdMatch ? divMdMatch[1].trim() : null;

  let result = "<table>\n";

  if (imageMatch) {
    const [_, postUrl, imageUrl, imageAlt, imageTitle] = imageMatch;
    result += `
      <tr>
        <td>
          <a href="${postUrl}">
            <img style="max-width: 100%; height: auto !important; display: block; margin-bottom: 8px;"
                 src="${imageUrl}" alt="${imageAlt}" title="${imageTitle}" />
          </a>
        </td>
      </tr>`;
  }

  if (divContent) {
    result += `
      <tr>
        <td class="content">
          ${divContent}
        </td>
      </tr>`;
  }

  result += `
    <tr>
      <td>
        <div style="margin-top: 8px;"><a href="${userUrl}">/u/${username}</a></div>
      </td>
    </tr>
  </table>`;

  return result;
}

function removeHeadTag(html) {
  return html.replace(/<head[^>]*>[\s\S]*?<\/head>/i, '');
}

function removeFirstH1(doc) {
  doc.querySelector('h1')?.remove();
}

function removeFirstImageTagByUrl(doc, imageUrl) {
  if (!imageUrl) return;

  const img = doc.querySelector(`img[src="${imageUrl}"]`);
  if (img) {
    img.closest('figure')?.remove() || img.remove();
  }
}

async function parseReaderContent(link, html, bannerImage, fetchFullArticle) {
  let processedHtml = html;

  if (fetchFullArticle) {
    try {
      const response = await fetch(link);
      if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
      processedHtml = await response.text();
    } catch (error) {
      console.error("Error fetching full article:", error);
    }
  }

  const cleanedHtml = removeHeadTag(processedHtml);
  const sanitizedHtml = isRedditUrl(link)
    ? formatRedditPost(cleanedHtml)
    : `<body><div>${cleanedHtml}</div></body>`;

  let doc;
  try {
    const parser = new DOMParser();
    const htmlWithBase = `<head><base href="${link}"></head>${sanitizedHtml}`;
    doc = parser.parseFromString(htmlWithBase, 'text/html');
  } catch (error) {
    console.error("Error parsing HTML:", error);
    return null;
  }

  removeFirstH1(doc);
  processIFrames(doc);
  removeFirstImageTagByUrl(doc, bannerImage);

//  const opts = { html: doc.body.innerHTML, contentType: 'markdown' };

  const article = new Readability(doc).parse();
  const turndownService = new TurndownService()
  const markdown = turndownService.turndown(article.content)
  return JSON.stringify({ content: markdown, excerpt: article.excerpt });
}
