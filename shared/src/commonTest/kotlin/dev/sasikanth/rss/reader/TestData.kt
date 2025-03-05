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

// language=JSON
const val firstNostrArticle =
  """
{
  "kind":30023,
  "id":"bbbce8d0da989753eb5bdbe1fd60d8728ddd1d6f9244a9ba101a4b978ec85b8a",
  "pubkey":"3bf0c63fcb93463407af97a5e5ee64fa883d107ef9e558472c4eb9aaaefa459d",
  "created_at":1691690551,
  "tags":[["d","mea-culpa"],["title","Mea culpa: things I did wrong in Nostr"],["summary",""],["published_at","1691689936"],["t","nostr"]],
  "content":"Things that we mostly can't fix, but that maybe could have been done better if they were here from the beginning -- or something like that.\n\n- The `NOTICE` message would have been better if it had structure like `OK` messages have. That would have allowed a more direct communication between relays and users, instead of relays being inside a client black box.\n- Choosing secp256k1 felt cool and maybe it still feels cool because it is the Bitcoin curve, but since many people have pointed out that other curves and algorithms are much faster maybe picking those would have been better.\n- Writing a spec for direct messages and implementing them was bad. In my defense, it was an attempt to [please the public](https://t.me/nostr_protocol/307), but still I should have not have done that, or thought more about it before doing it.\n- Thinking that kind 1 should be used for all the things \"text\" just restricted the ability of clients to do different interfaces. If we had different kinds for replies, quotes, comments and \"root\" posts from the beginning that would have been better.\n- For a long time I didn't realize Nostr wasn't useful just for \"social networking\" things. I wonder what else could have been better designed in the relay-client interface if the needs of non-social-networking apps were kept in mind.\n- The querying system is sometimes too generic, it could have been better if it was more restrictive, but more complete. For example: allowing generic querying over tags is weird, can lead to O(n²) issues in some cases and relays are left to fend for themselves -- on the other hand we can't query for the absence of some tags. But I don't know how any of these things could have been better even today, so maybe it wasn't so bad.\n- Making the events be JSON: sometimes I think this was a bad idea and a binary format would have been better, but most of the times I think Nostr wouldn't have become any popular at all if this was the case -- also binary is slower than JSON in JavaScript, so I guess this wasn't a completely bad choice. Perhaps if something like [NSON](https://github.com/nostr-protocol/nips/pull/515) had been adopted from the start, though, that would have been better for everybody.\n\nWhen I decided to write this I had one item in mind, but when I started I forgot what that was. I'll add it here back when I remember.",
  "sig":"f8bf9e508e00e1cf24498549dc6955d0fb6b13678efe177a909ff7e7fab5f28ce7dc05324a45bc5ca6acb4b5071df4da3e82c4c63296d3fc857f06c79d4d105a"
}
"""

// language=JSON
const val secondNostrArticle =
  """
{
  "kind":30023,
  "id":"2e950ded426e86b6b358444e207e1eaea46e4fc1e175933b8b3a3fb17ae0ba66",
  "pubkey":"3bf0c63fcb93463407af97a5e5ee64fa883d107ef9e558472c4eb9aaaefa459d",
  "created_at":1705240528,
  "tags":[["d","33ec0995"],["title","Setting up a handler for `nostr:` links on your Desktop, even if you don't use a native client"],["published_at","1675175880"],["t","nostr"]],
  "content":"\n# Setting up a handler for `nostr:` links on your Desktop, even if you don't use a native client\n\nThis is the most barebones possible, it will just open a web browser at `https://nostr.guru/` with the contents of the `nostr:` link.\n\nCreate this file at `~/.local/share/applications/nostr-opener.desktop`:\n\n```\n[Desktop Entry]\nExec=/home/youruser/nostr-opener %u\nName=Nostr Browser\nType=Application\nStartupNotify=false\nMimeType=x-scheme-handler/nostr;\n```\n\n(Replace \"youruser\" with your username above.)\n\nThis will create a default handler for `nostr:` links. It will be called with the link as its first argument.\n\nNow you can create the actual program at `~/nostr-opener`. For example:\n\n```python\n#!/usr/bin/env python\n\nimport sys\nimport webbrowser\n\nnip19 = sys.argv[1][len('nostr:'):]\nwebbrowser.open(f'https://nostr.guru/{nip19}')\n```\n\nRemember to make it executable with `chmod +x ~/nostr-opener`.\n",
  "sig":"59131fd3371427ab49a23f5057d6580ea361abf74d965eb47d249177924e3553693932fe7b7c106a2d99bae73481c42841ce6e86eb6be4a6dac68f3ec150a5b7"
}
"""
