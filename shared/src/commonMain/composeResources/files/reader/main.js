function processIFrames(doc) {
  let iframes = doc.querySelectorAll("iframe")
  for (let i = 0, max = iframes.length; i < max; i++) {
    let iframe = iframes[i];
    if (iframe.hasAttribute("data-runner-src")) {
      iframe.src = String(iframe.getAttribute("data-runner-src"));
    }
  }
}

function isRedditUrl(url) {
  const redditDomainPattern = /^https?:\/\/(?:www\.|old\.|new\.|i\.)?reddit\.com|redd\.it/i;

  try {
    const urlPattern = /^https?:\/\/([^/]+)/i;
    const match = url.match(urlPattern);

    if (match) {
      const domain = match[1];
      return redditDomainPattern.test(`https://${domain}`);
    }
    return false;
  } catch (e) {
    return false;
  }
}

function formatRedditPost(html) {
  const imagePattern = /<a href="([^"]+)">\s*<img src="([^"]+)"\s*alt="([^"]+)" title="([^"]+)"\s*\/>\s*<\/a>/;
  const submitterPattern = /submitted by\s*<a href="([^"]+)">\s*\/u\/([^<\s]+)/;
  const divMdPattern = /(?:<!-- SC_OFF -->)?\s*<div class="md">([\s\S]*?)<\/div>\s*(?:<!-- SC_ON -->)?/;

  const imageMatch = html.match(imagePattern);
  let submitterMatch = html.match(submitterPattern);
  const divMdMatch = html.match(divMdPattern);

  if (!submitterMatch) {
    const altSubmitterPattern = /user\/([^"]+)[^<]*<\/a>/;
    const altMatch = html.match(altSubmitterPattern);

    if (altMatch) {
      const username = altMatch[1].trim();
      const userUrl = `https://www.reddit.com/user/${username}`;
      submitterMatch = [null, userUrl, username];
    }
  }

  if (!submitterMatch) return null;

  const userUrl = submitterMatch[1];
  const username = submitterMatch[2].trim();
  const divContent = divMdMatch ? divMdMatch[1].trim() : null;

  let result = "<table>\n";

  if (imageMatch) {
    const postUrl = imageMatch[1];
    const imageUrl = imageMatch[2];
    const imageAlt = imageMatch[3];
    const imageTitle = imageMatch[4];

    result += `
  <tr>
    <td>
      <a href="${postUrl}">
        <img style="max-width: 100%; height: auto !important; display: block; margin-bottom: 8px;" src="${imageUrl}" alt="${imageAlt}" title="${imageTitle}" />
      </a>
    </td>
  </tr>
`;
  }

  if (divContent) {
    result += `
  <tr>
    <td class="content">
      ${divContent}
    </td>
  </tr>
`;
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
  const headRegex = /<head[^>]*>[\s\S]*?<\/head>/i;
  return html.replace(headRegex, '');
}

function removeFirstH1(doc) {
  const firstH1 = doc.querySelector('h1');
  if (firstH1) {
    firstH1.parentNode.removeChild(firstH1);
  }
}

function removeFirstImageTagByUrl(doc, imageUrl) {
  if (!imageUrl) {
    return;
  }

  const img = doc.querySelector(`img[src="${imageUrl}"]`);
  if (img) {
    img.parentNode.removeChild(img);
    return;
  }

  const figure = doc.querySelector(`figure img[src="${imageUrl}"]`);
  if (figure) {
    figure.parentNode.parentNode.removeChild(figure.parentNode);
  }
}

async function parseReaderContent(link, html, bannerImage, fetchFullArticle) {
  let processedHtml;

  if (fetchFullArticle) {
    try {
      const response = await fetch(link);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      processedHtml = await response.text();
    } catch (error) {
      console.error("Error fetching full article:", error);
    }
  } else {
    processedHtml = html;
  }

  const cleanedHtml = removeHeadTag(processedHtml);
  let sanitizedHtml;
  if (isRedditUrl(link)) {
    sanitizedHtml = formatRedditPost(cleanedHtml);
  } else {
    sanitizedHtml = `<body><div>${cleanedHtml}</div></body>`;
  }

  const parser = new DOMParser();
  const doc = parser.parseFromString(sanitizedHtml, 'text/html');

  removeFirstH1(doc);
  processIFrames(doc);
  removeFirstImageTagByUrl(doc, bannerImage);

  const opts = { html: doc.body.innerHTML, contentType: 'markdown' };

  // noinspection JSUnresolvedReference
  const result = await Mercury.parse(link, opts);
  return JSON.stringify({ content: result.content, excerpt: result.excerpt })
}
