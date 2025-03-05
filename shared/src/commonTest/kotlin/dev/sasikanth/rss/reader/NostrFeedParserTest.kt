/*
 * Copyright 2025 Sasikanth Miriyampalli
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

import dev.sasikanth.rss.reader.core.model.remote.PostPayload
import dev.sasikanth.rss.reader.core.network.parser.nostr.NostrFeedParser
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NostrFeedParserTest {

    @Test
    fun parsingNostrArticlesShouldWorkCorrectly() = runTest {
        val expectedArticles = listOf(
            PostPayload(
                title = "Mea culpa: things I did wrong in Nostr",
                link = "nostr:naddr1qqyk6etp943h2mrsvyqs6amnwvaz7tmwdaejumr0dsq3qamnwvaz7tmwdaehgu3wwa5kuegzyqalp33lewf5vdq847t6te0wvnags0gs0mu72kz8938tn24wlfze6qcyqqq823cx48sea",
                description = "",
                rawContent = "Things that we mostly can't fix, but that maybe could have been done better if they were here from the beginning -- or something like that.\n\n- The `NOTICE` message would have been better if it had structure like `OK` messages have. That would have allowed a more direct communication between relays and users, instead of relays being inside a client black box.\n- Choosing secp256k1 felt cool and maybe it still feels cool because it is the Bitcoin curve, but since many people have pointed out that other curves and algorithms are much faster maybe picking those would have been better.\n- Writing a spec for direct messages and implementing them was bad. In my defense, it was an attempt to [please the public](https://t.me/nostr_protocol/307), but still I should have not have done that, or thought more about it before doing it.\n- Thinking that kind 1 should be used for all the things \"text\" just restricted the ability of clients to do different interfaces. If we had different kinds for replies, quotes, comments and \"root\" posts from the beginning that would have been better.\n- For a long time I didn't realize Nostr wasn't useful just for \"social networking\" things. I wonder what else could have been better designed in the relay-client interface if the needs of non-social-networking apps were kept in mind.\n- The querying system is sometimes too generic, it could have been better if it was more restrictive, but more complete. For example: allowing generic querying over tags is weird, can lead to O(n²) issues in some cases and relays are left to fend for themselves -- on the other hand we can't query for the absence of some tags. But I don't know how any of these things could have been better even today, so maybe it wasn't so bad.\n- Making the events be JSON: sometimes I think this was a bad idea and a binary format would have been better, but most of the times I think Nostr wouldn't have become any popular at all if this was the case -- also binary is slower than JSON in JavaScript, so I guess this wasn't a completely bad choice. Perhaps if something like [NSON](https://github.com/nostr-protocol/nips/pull/515) had been adopted from the start, though, that would have been better for everybody.\n\nWhen I decided to write this I had one item in mind, but when I started I forgot what that was. I'll add it here back when I remember.",
                imageUrl = null,
                date = 1691689936000,
                commentsLink = null,
                isDateParsedCorrectly = true
            ),
            PostPayload(
                title = "Setting up a handler for `nostr:` links on your Desktop, even if you don't use a native client",
                link = "nostr:naddr1qqyrxvm9vvcrjwf4qy8hwumn8ghj7mn0wd68ytnddaksygpm7rrrljungc6q0tuh5hj7ue863q73qlheu4vywtzwhx42a7j9n5psgqqqw4rsxlhq4k",
                description = "",
                rawContent = "\n# Setting up a handler for `nostr:` links on your Desktop, even if you don't use a native client\n\nThis is the most barebones possible, it will just open a web browser at `https://nostr.guru/` with the contents of the `nostr:` link.\n\nCreate this file at `~/.local/share/applications/nostr-opener.desktop`:\n\n```\n[Desktop Entry]\nExec=/home/youruser/nostr-opener %u\nName=Nostr Browser\nType=Application\nStartupNotify=false\nMimeType=x-scheme-handler/nostr;\n```\n\n(Replace \"youruser\" with your username above.)\n\nThis will create a default handler for `nostr:` links. It will be called with the link as its first argument.\n\nNow you can create the actual program at `~/nostr-opener`. For example:\n\n```python\n#!/usr/bin/env python\n\nimport sys\nimport webbrowser\n\nnip19 = sys.argv[1][len('nostr:'):]\nwebbrowser.open(f'https://nostr.guru/{nip19}')\n```\n\nRemember to make it executable with `chmod +x ~/nostr-opener`.\n",
                imageUrl = null,
                date = 1675175880,
                commentsLink = null,
                isDateParsedCorrectly = true
            )
        )

        val parsedArticles = listOf(firstNostrArticle, secondNostrArticle).map { json ->
            NostrFeedParser.parsePost(json)
        }

        expectedArticles.forEachIndexed { index, article ->
            assertEquals(article.title, parsedArticles[index].title)
            assertEquals(article.rawContent, parsedArticles[index].rawContent)
        }
    }

}