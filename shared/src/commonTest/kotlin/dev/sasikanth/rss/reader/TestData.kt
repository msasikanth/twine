/*
 * Copyright 2023 Sasikanth Miriyampalli
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
package dev.sasikanth.rss.reader

const val feedUrl = "https://example.com"
const val rssXmlContent =
  """<?xml version="1.0" encoding="UTF-8"?>
  <rss version="2.0">
  <channel>
    <title>Feed title</title>
    <link>https://example.com</link>
    <description>Feed description</description>
    <item>
      <title>Post with image</title>
      <link>https://example.com/first-post</link>
      <description>First post description.</description>
      <pubDate>Thu, 25 May 2023 09:00:00 +0000</pubDate>
      <media:content url="https://example.com/first-post-media-url" />
    </item>
    <item>
      <title>Post without image</title>
      <link>https://example.com/second-post</link>
      <description>Second post description.</description>
      <pubDate>Thu, 25 May 2023 07:30:00 +0000</pubDate>
    </item>
    <item>
      <title>Podcast post</title>
      <description>Third post description.</description>
      <pubDate>Wed, 24 May 2023 10:30:00 +0000</pubDate>
      <enclosure url="https://example.com/third-post" />
    </item>
    <item>
      <title>Post with enclosure image</title>
      <description>Fourth post description.</description>
      <pubDate>Wed, 24 May 2023 10:30:00 +0000</pubDate>
      <enclosure url="https://example.com/fourth-post" />
      <enclosure type="image/jpeg" url="https://example.com/enclosure-image" />
    </item>
    <item>
      <title>Post with description and encoded content</title>
      <description>Fourth post description.</description>
      <link>https://example.com/fifth-post</link>
      <content:encoded>
        &lt;p&gt;Fourth post description in HTML syntax.&lt;/p&gt;
        &lt;img src="https://example.com/encoded-image" alt="encoded image" /&gt;
      </content:encoded>
      <pubDate>Wed, 24 May 2023 10:30:00 +0000</pubDate>
    </item>
    <item>
      <title>Post with relative path image</title>
      <link>https://example.com/post-with-relative-image</link>
      <description>Relative image post description.</description>
      <pubDate>Thu, 25 May 2023 09:00:00 +0000</pubDate>
      <media:content url="/relative-media-url" />
    </item>
    <item>
      <title>Post with comments</title>
      <link>https://example.com/post-with-comments</link>
      <description>Really long post with comments.</description>
      <pubDate>Thu, 25 May 2023 09:00:00 +0000</pubDate>
      <comments>https://example/post-with-comments/comments</comments>
    </item>
  </channel>
  </rss>
  """

const val rdfXmlContent =
  """<?xml version="1.0" encoding="UTF-8"?>
  <rdf:RDF>
  <channel>
    <title>Feed title</title>
    <link>https://example.com</link>
    <description>Feed description</description>
  </channel>
    <item>
      <title>Post</title>
      <link>https://example.com/first-post</link>
      <description>First post description.</description>
      <dc:date>Thu, 25 May 2023 09:00:00 +0000</dc:date>
    </item>
    <item>
      <title>Post with encoded description</title>
      <description>
        &lt;p&gt;Second post description in HTML syntax.&lt;/p&gt;
        &lt;img src="https://example.com/encoded-image" alt="encoded image" /&gt;
      </description>
      <link>https://example.com/second-post</link>
      <dc:date>Wed, 24 May 2023 10:30:00 +0000</dc:date>
    </item>
  </rdf:RDF>
  """

const val atomXmlContent =
  """<?xml version="1.0" encoding="UTF-8"?>
  <feed xmlns="http://www.w3.org/2005/Atom">
    <title>Feed title</title>
    <subtitle>Feed description</subtitle>
    <link href="https://example.com" rel="alternate" />
    <entry>
      <title>Post with image</title>
      <link rel="alternate" href="https://example.com/first-post" />
      <published>2023-05-25T10:00:00Z</published>
      <content type="html">
        &lt;img alt="First Image" src="https://example.com/image.jpg" /&gt;
        &lt;p&gt;Post summary with an image.&lt;/p&gt;
      </content>
    </entry>
    <entry>
      <title>Second post</title>
      <link rel="alternate" href="https://example.com/second-post" />
      <published>2023-05-24T08:30:00Z</published>
      <content type="html">
        &lt;p&gt;Post summary of the second post.&lt;/p&gt;
      </content>
    </entry>
    <entry>
      <title>Post without image</title>
      <link rel="alternate" href="https://example.com/third-post" />
      <published>2023-05-24T14:00:00Z</published>
      <content type="html">
        &lt;p&gt;Post summary of the third post. &lt;a href="https://example.com/hyperlink" &gt;click here&lt;/a&gt;.&lt;/p&gt;
      </content>
    </entry>
    <entry>
      <title>Post with relative image</title>
      <link rel="alternate" href="https://example.com/relative-image-post" />
      <published>2023-05-25T10:00:00Z</published>
      <content type="html">
        &lt;img alt="Relative Image" src="/resources/image.jpg" /&gt;
        &lt;p&gt;Post summary with an image.&lt;/p&gt;
      </content>
    </entry>
  </feed>
  """

// language=JSON
const val jsonFeed =
  """

{
  "title": "Tech Insights Blog",
  "description": "The latest insights and news from the tech world",
  "home_page_url": "https://example.com",
  "feed_url": "https://example.com/feed.json",
  "icon": "https://example.com/icon.png",
  "favIcon": "https://example.com/favicon.ico",
  "items": [
    {
      "id": "2025-001",
      "title": "The Future of Quantum Computing in 2025",
      "content_html": "<p>Quantum computing has made significant strides in the past year. Recent breakthroughs at IBM and Google have pushed the boundaries of what we thought possible.</p><p>The new 1000-qubit processor announced last month represents a major milestone in the industry.</p>",
      "content_text": "Quantum computing has made significant strides in the past year. Recent breakthroughs at IBM and Google have pushed the boundaries of what we thought possible.\n\nThe new 1000-qubit processor announced last month represents a major milestone in the industry.",
      "summary": "An overview of recent quantum computing advances and what they mean for the tech industry",
      "image": "https://example.com/images/quantum-2025.jpg",
      "date_published": "2025-02-28T09:15:00Z",
      "url": "https://example.com/posts/quantum-computing-2025"
    },
    {
      "id": "2025-002",
      "title": "AI Ethics Frameworks: A Comparative Analysis",
      "content_html": "<p>As AI becomes more integrated into our daily lives, the need for robust ethical frameworks has never been more important.</p><p>This article examines the approaches taken by major tech companies and governments around the world.</p>",
      "summary": "Comparing different approaches to AI ethics across industry and government",
      "date_published": "2025-02-15T14:30:00Z",
      "url": "https://example.com/posts/ai-ethics-frameworks"
    },
    {
      "id": "2025-003",
      "title": "The Rise of Edge Computing",
      "content_text": "Edge computing continues to grow as IoT devices proliferate. This shift is changing how we think about network architecture and data processing.\n\nIn this article, we explore the implications for businesses and consumers alike.",
      "image": "https://example.com/images/edge-computing.jpg",
      "date_published": "2025-02-01T11:45:00Z",
      "url": "https://example.com/posts/edge-computing-rise"
    },
    {
      "id": "2025-004",
      "title": "Sustainable Tech: Green Innovations in Silicon Valley",
      "content_html": "<p>Silicon Valley companies are leading the charge in sustainable technology development.</p><p>From carbon-neutral data centers to biodegradable electronics, we look at the most promising initiatives.</p>",
      "content_text": "Silicon Valley companies are leading the charge in sustainable technology development.\n\nFrom carbon-neutral data centers to biodegradable electronics, we look at the most promising initiatives.",
      "date_published": "2025-01-20T16:00:00Z",
      "url": "https://example.com/posts/sustainable-tech-innovations"
    }
  ]
}
"""
