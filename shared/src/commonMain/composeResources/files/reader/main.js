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
    padding-top: 16px;
    color: ${colors.textColor};
    font-family: 'Golos Text', sans-serif;
    overflow-wrap: break-word;
  }
  body:dir(rtl) {
    padding-inline-start: 24px;
  }
  body:dir(ltr) {
    padding-inline-end: 24px;
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
  const content = result.content || html;

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
