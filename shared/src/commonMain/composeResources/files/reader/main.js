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
    let href = findHref(event.target);
    if (href === undefined || href === "/") {
      href = ""
    }
    //noinspection JSUnresolvedVariable
    window.kmpJsBridge.callNative(
      "linkHandler",
      href,
      {}
    );
  } catch (err) {
    // no-op
  }
}

function processLinks() {
  let links = document.querySelectorAll("a")
  for (let i = 0, max = links.length; i < max; i++) {
    let link = links[i];
    link.addEventListener("click", handleLinkClick);
  }
}

function processIFrames() {
  let iframes = document.querySelectorAll("iframe")
  for (let i = 0, max = iframes.length; i < max; i++) {
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
  const title = document.querySelectorAll("h1")[1];
  if (title) {
    title.style.display = 'none';
  }
}

function updateStyles(colors) {
  const styles = `
  body {
    margin: 0;
    padding: 16px 0 0 0;
    color: ${colors.textColor};
    font-family: 'Golos Text', sans-serif;
    overflow-wrap: break-word;
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
    overflow-x: hidden;
  }
  #content {
    margin: 0;
    padding: 0;
    width: 100%;
    box-sizing: border-box;
  }
  a {
    color: ${colors.linkColor};
  }
  ul li::before {
    content: "\u2022";
    color: ${colors.textColor};
    margin-inline-end: 0.25em;
  }
  ol li::before {
    counter-increment: item;
    content: counters(item, ".") ".";
    color: ${colors.textColor};
    margin-inline-end: 0.25em;
  }
  code, pre {
    font-family: 'Source Code Pro', monospace;
    font-size: 14px;
    -webkit-hyphens: none;
    background: ${colors.codeBackgroundColor};
  }
  .top-divider {
    margin-top: 12px;
    margin-bottom: 12px;
    border: 1px solid ${colors.dividerColor};
  }
  blockquote {
    margin-inline-start: 8px;
    padding-left: 8px;
    border-left: 4px solid ${colors.linkColor}
  }
`

  const styleSheet = document.createElement("style");
  styleSheet.innerText = styles
  document.head.appendChild(styleSheet)
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

  if (!submitterMatch) return "Submitter data not found";

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

async function renderReaderView(link, html, colors) {
  console.log('Preparing reader content for rendering');
  //noinspection JSUnresolvedVariable
  window.kmpJsBridge.callNative(
    "renderProgress",
    "Loading",
    {}
  );

  const sanitizedHtml = `<div>${html}</div>`
  //noinspection JSUnresolvedVariable
  const result = await Mercury.parse(link, { html: sanitizedHtml });
  let content = "";
  if (isRedditUrl(link)) {
    content = formatRedditPost(sanitizedHtml)
  } else {
    content = result.content || html;
  }

  document.getElementById("content").innerHTML += content;
  document.querySelectorAll("pre").forEach((element) =>
    element.dir = "auto"
  );

  updateStyles(colors)
  processLinks();
  processIFrames();
  removeTitle();

  document.body.style.display = "block";

  console.log('Reader content rendered')
  //noinspection JSUnresolvedVariable
  window.kmpJsBridge.callNative(
    "renderProgress",
    "Idle",
    {}
  );
}
