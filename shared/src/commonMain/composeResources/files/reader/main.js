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
  }
  a {
    color: ${colors.linkColor};
  }
  ul li::before {
    content: "\u2022";
    color: ${colors.textColor};
    margin-right: 0.25em;
  }
  ol li::before {
    counter-increment: item;
    content: counters(item, ".") ".";
    color: ${colors.textColor};
    margin-right: 0.25em;
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
    margin-left: 8px;
    padding-left: 8px;
    border-left: 4px solid ${colors.linkColor}
  }
`

  const styleSheet = document.createElement("style");
  styleSheet.innerText = styles
  document.head.appendChild(styleSheet)
}

async function renderReaderView(link, html, colors) {
  console.log('Parsing content');

  const fontPromise = new Promise(resolve => {
    if (document.fonts) {
      Promise.all([
        document.fonts.load('1em Golos Text'),
        document.fonts.load('1em "Source Code Pro"')
      ]).then(() => resolve());
    } else {
      // Fallback timeout
      setTimeout(() => resolve(), 3000);
    }
  });

  //noinspection JSUnresolvedVariable
  const parsePromise = parse(html, link)
    .then(result => result.content || html)
    .catch(error => {
      console.error('Error parsing content:', error);
      return html;
    });

  const [content] = await Promise.all([parsePromise, fontPromise]);

  document.getElementById("content").innerHTML += content;

  updateStyles(colors)
  processLinks();
  processIFrames();
  removeTitle();

  document.body.style.display = "block";
}
