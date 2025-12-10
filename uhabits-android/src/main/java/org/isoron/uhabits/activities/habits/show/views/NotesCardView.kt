/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits.activities.habits.show.views

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.text.util.Linkify
import android.util.AttributeSet
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.net.toUri
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import org.isoron.uhabits.core.ui.screens.habits.show.views.NotesCardState
import org.isoron.uhabits.databinding.ShowHabitNotesBinding
import java.util.regex.Pattern

class NotesCardView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    data class LinkSpec(
        val url: String,
        val start: Int,
        val end: Int
    )

    data class LinkPattern(
        val regex: Pattern,
        val defaultScheme: String = "",
        val matchFilter: ((CharSequence, Int, Int) -> Boolean)? = null,
        val transformFilter: ((String) -> String)? = null
    )

    companion object {
        fun addLinks(
            spannable: Spannable,
            patterns: List<LinkPattern>,
            context: Context
        ): Boolean {
            // Remove existing URLSpans to start fresh
            spannable.getSpans(0, spannable.length, URLSpan::class.java).forEach {
                spannable.removeSpan(it)
            }

            val links = ArrayList<LinkSpec>()

            // Gather links from all patterns
            for (pattern in patterns) {
                gatherLinks(links, spannable, pattern)
            }

            // Resolve overlaps (longer/earlier links win)
            pruneOverlaps(links)

            if (links.isEmpty()) return false

            // Apply the spans
            for (link in links) {
                spannable.setSpan(
                    URLSpan(link.url),
                    link.start,
                    link.end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            return true
        }

        private fun gatherLinks(
            links: ArrayList<LinkSpec>,
            text: Spannable,
            pattern: LinkPattern
        ) {
            val matcher = pattern.regex.matcher(text)

            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()
                val matchText = matcher.group(0) ?: continue

                // Apply match filter if provided
                if (pattern.matchFilter?.invoke(text, start, end) == false) {
                    continue
                }

                // Transform URL if needed
                var url = pattern.transformFilter?.invoke(matchText) ?: matchText

                // Add scheme prefix if needed
                if (!hasScheme(url) && pattern.defaultScheme.isNotEmpty()) {
                    url = pattern.defaultScheme + url
                }

                links.add(LinkSpec(url, start, end))
            }
        }

        private fun hasScheme(url: String): Boolean {
            return url.contains("://") || url.startsWith("mailto:") || url.startsWith("tel:")
        }

        private fun pruneOverlaps(links: ArrayList<LinkSpec>) {
            // Sort by start position, then by length (longer first)
            links.sortWith(compareBy<LinkSpec> { it.start }.thenByDescending { it.end - it.start })

            var i = 0
            while (i < links.size - 1) {
                val a = links[i]
                val b = links[i + 1]

                // Check if they overlap
                if (a.start <= b.start && a.end > b.start) {
                    // Remove the link that is completely contained or shorter
                    val remove = when {
                        b.end <= a.end -> i + 1  // b is contained in a
                        (a.end - a.start) >= (b.end - b.start) -> i + 1  // a is longer/equal
                        else -> i  // b is longer
                    }
                    links.removeAt(remove)
                    continue  // Don't increment i, check again
                }
                i++
            }
        }

        const val UCS_CHAR: String = "[" +
                "\u00A0-\uD7FF" +
                "\uF900-\uFDCF" +
                "\uFDF0-\uFFEF" +
                "\uD800\uDC00-\uD83F\uDFFD" +
                "\uD840\uDC00-\uD87F\uDFFD" +
                "\uD880\uDC00-\uD8BF\uDFFD" +
                "\uD8C0\uDC00-\uD8FF\uDFFD" +
                "\uD900\uDC00-\uD93F\uDFFD" +
                "\uD940\uDC00-\uD97F\uDFFD" +
                "\uD980\uDC00-\uD9BF\uDFFD" +
                "\uD9C0\uDC00-\uD9FF\uDFFD" +
                "\uDA00\uDC00-\uDA3F\uDFFD" +
                "\uDA40\uDC00-\uDA7F\uDFFD" +
                "\uDA80\uDC00-\uDABF\uDFFD" +
                "\uDAC0\uDC00-\uDAFF\uDFFD" +
                "\uDB00\uDC00-\uDB3F\uDFFD" +
                "\uDB44\uDC00-\uDB7F\uDFFD" +
                "&&[^\u00A0[\u2000-\u200A]\u2028\u2029\u202F\u3000]]"

        const val LABEL_CHAR: String = "a-zA-Z0-9" + UCS_CHAR
        const val TLD_CHAR: String = "a-zA-Z" + UCS_CHAR
        const val IRI_LABEL: String =
            "[" + LABEL_CHAR + "](?:[" + LABEL_CHAR + "_\\-]{0,61}[" + LABEL_CHAR + "]){0,1}"
        const val PUNYCODE_TLD: String = "xn\\-\\-[\\w\\-]{0,58}\\w"
        const val IANA_TOP_LEVEL_DOMAINS: String = ("(?:"
                + "(?:aaa|aarp|abb|abbott|abbvie|abc|able|abogado|abudhabi|academy|accenture|accountant"
                + "|accountants|aco|actor|ads|adult|aeg|aero|aetna|afl|africa|agakhan|agency|aig|airbus"
                + "|airforce|airtel|akdn|alibaba|alipay|allfinanz|allstate|ally|alsace|alstom|amazon|americanexpress"
                + "|americanfamily|amex|amfam|amica|amsterdam|analytics|android|anquan|anz|aol|apartments"
                + "|app|apple|aquarelle|arab|aramco|archi|army|arpa|art|arte|asda|asia|associates|athleta"
                + "|attorney|auction|audi|audible|audio|auspost|author|auto|autos|avianca|aws|axa|azure"
                + "|a[cdefgilmoqrstuwxz])"
                + "|(?:baby|baidu|banamex|bananarepublic|band|bank|bar|barcelona|barclaycard|barclays"
                + "|barefoot|bargains|baseball|basketball|bauhaus|bayern|bbc|bbt|bbva|bcg|bcn|beats|beauty"
                + "|beer|bentley|berlin|best|bestbuy|bet|bharti|bible|bid|bike|bing|bingo|bio|biz|black"
                + "|blackfriday|blockbuster|blog|bloomberg|blue|bms|bmw|bnpparibas|boats|boehringer|bofa"
                + "|bom|bond|boo|book|booking|bosch|bostik|boston|bot|boutique|box|bradesco|bridgestone"
                + "|broadway|broker|brother|brussels|build|builders|business|buy|buzz|bzh|b[abdefghijmnorstvwyz])"
                + "|(?:cab|cafe|cal|call|calvinklein|cam|camera|camp|canon|capetown|capital|capitalone"
                + "|car|caravan|cards|care|career|careers|cars|casa|case|cash|casino|cat|catering|catholic"
                + "|cba|cbn|cbre|cbs|center|ceo|cern|cfa|cfd|chanel|channel|charity|chase|chat|cheap|chintai"
                + "|christmas|chrome|church|cipriani|circle|cisco|citadel|citi|citic|city|cityeats|claims"
                + "|cleaning|click|clinic|clinique|clothing|cloud|club|clubmed|coach|codes|coffee|college"
                + "|cologne|com|comcast|commbank|community|company|compare|computer|comsec|condos|construction"
                + "|consulting|contact|contractors|cooking|cool|coop|corsica|country|coupon|coupons|courses"
                + "|cpa|credit|creditcard|creditunion|cricket|crown|crs|cruise|cruises|cuisinella|cymru"
                + "|cyou|c[acdfghiklmnoruvwxyz])"
                + "|(?:dabur|dad|dance|data|date|dating|datsun|day|dclk|dds|deal|dealer|deals|degree"
                + "|delivery|dell|deloitte|delta|democrat|dental|dentist|desi|design|dev|dhl|diamonds|diet"
                + "|digital|direct|directory|discount|discover|dish|diy|dnp|docs|doctor|dog|domains|dot"
                + "|download|drive|dtv|dubai|dunlop|dupont|durban|dvag|dvr|d[ejkmoz])"
                + "|(?:earth|eat|eco|edeka|edu|education|email|emerck|energy|engineer|engineering|enterprises"
                + "|epson|equipment|ericsson|erni|esq|estate|etisalat|eurovision|eus|events|exchange|expert"
                + "|exposed|express|extraspace|e[cegrstu])"
                + "|(?:fage|fail|fairwinds|faith|family|fan|fans|farm|farmers|fashion|fast|fedex|feedback"
                + "|ferrari|ferrero|fidelity|fido|film|final|finance|financial|fire|firestone|firmdale"
                + "|fish|fishing|fit|fitness|flickr|flights|flir|florist|flowers|fly|foo|food|football"
                + "|ford|forex|forsale|forum|foundation|fox|free|fresenius|frl|frogans|frontdoor|frontier"
                + "|ftr|fujitsu|fun|fund|furniture|futbol|fyi|f[ijkmor])"
                + "|(?:gal|gallery|gallo|gallup|game|games|gap|garden|gay|gbiz|gdn|gea|gent|genting"
                + "|george|ggee|gift|gifts|gives|giving|glass|gle|global|globo|gmail|gmbh|gmo|gmx|godaddy"
                + "|gold|goldpoint|golf|goo|goodyear|goog|google|gop|got|gov|grainger|graphics|gratis|green"
                + "|gripe|grocery|group|guardian|gucci|guge|guide|guitars|guru|g[abdefghilmnpqrstuwy])"
                + "|(?:hair|hamburg|hangout|haus|hbo|hdfc|hdfcbank|health|healthcare|help|helsinki|here"
                + "|hermes|hiphop|hisamitsu|hitachi|hiv|hkt|hockey|holdings|holiday|homedepot|homegoods"
                + "|homes|homesense|honda|horse|hospital|host|hosting|hot|hotels|hotmail|house|how|hsbc"
                + "|hughes|hyatt|hyundai|h[kmnrtu])"
                + "|(?:ibm|icbc|ice|icu|ieee|ifm|ikano|imamat|imdb|immo|immobilien|inc|industries|infiniti"
                + "|info|ing|ink|institute|insurance|insure|int|international|intuit|investments|ipiranga"
                + "|irish|ismaili|ist|istanbul|itau|itv|i[delmnoqrst])"
                + "|(?:jaguar|java|jcb|jeep|jetzt|jewelry|jio|jll|jmp|jnj|jobs|joburg|jot|joy|jpmorgan"
                + "|jprs|juegos|juniper|j[emop])"
                + "|(?:kaufen|kddi|kerryhotels|kerrylogistics|kerryproperties|kfh|kia|kids|kim|kinder"
                + "|kindle|kitchen|kiwi|koeln|komatsu|kosher|kpmg|kpn|krd|kred|kuokgroup|kyoto|k[eghimnprwyz])"
                + "|(?:lacaixa|lamborghini|lamer|lancaster|land|landrover|lanxess|lasalle|lat|latino"
                + "|latrobe|law|lawyer|lds|lease|leclerc|lefrak|legal|lego|lexus|lgbt|lidl|life|lifeinsurance"
                + "|lifestyle|lighting|like|lilly|limited|limo|lincoln|link|lipsy|live|living|llc|llp|loan"
                + "|loans|locker|locus|lol|london|lotte|lotto|love|lpl|lplfinancial|ltd|ltda|lundbeck|luxe"
                + "|luxury|l[abcikrstuvy])"
                + "|(?:madrid|maif|maison|makeup|man|management|mango|map|market|marketing|markets|marriott"
                + "|marshalls|mattel|mba|mckinsey|med|media|meet|melbourne|meme|memorial|men|menu|merckmsd"
                + "|miami|microsoft|mil|mini|mint|mit|mitsubishi|mlb|mls|mma|mobi|mobile|moda|moe|moi|mom"
                + "|monash|money|monster|mormon|mortgage|moscow|moto|motorcycles|mov|movie|msd|mtn|mtr"
                + "|museum|music|m[acdeghklmnopqrstuvwxyz])"
                + "|(?:nab|nagoya|name|natura|navy|nba|nec|net|netbank|netflix|network|neustar|new|news"
                + "|next|nextdirect|nexus|nfl|ngo|nhk|nico|nike|nikon|ninja|nissan|nissay|nokia|norton"
                + "|now|nowruz|nowtv|nra|nrw|ntt|nyc|n[acefgilopruz])"
                + "|(?:obi|observer|office|okinawa|olayan|olayangroup|oldnavy|ollo|omega|one|ong|onl"
                + "|online|ooo|open|oracle|orange|org|organic|origins|osaka|otsuka|ott|ovh|om)"
                + "|(?:page|panasonic|paris|pars|partners|parts|party|pay|pccw|pet|pfizer|pharmacy|phd"
                + "|philips|phone|photo|photography|photos|physio|pics|pictet|pictures|pid|pin|ping|pink"
                + "|pioneer|pizza|place|play|playstation|plumbing|plus|pnc|pohl|poker|politie|porn|post"
                + "|pramerica|praxi|press|prime|pro|prod|productions|prof|progressive|promo|properties"
                + "|property|protection|pru|prudential|pub|pwc|p[aefghklmnrstwy])"
                + "|(?:qpon|quebec|quest|qa)"
                + "|(?:racing|radio|read|realestate|realtor|realty|recipes|red|redstone|redumbrella"
                + "|rehab|reise|reisen|reit|reliance|ren|rent|rentals|repair|report|republican|rest|restaurant"
                + "|review|reviews|rexroth|rich|richardli|ricoh|ril|rio|rip|rocher|rocks|rodeo|rogers|room"
                + "|rsvp|rugby|ruhr|run|rwe|ryukyu|r[eosuw])"
                + "|(?:saarland|safe|safety|sakura|sale|salon|samsclub|samsung|sandvik|sandvikcoromant"
                + "|sanofi|sap|sarl|sas|save|saxo|sbi|sbs|sca|scb|schaeffler|schmidt|scholarships|school"
                + "|schule|schwarz|science|scot|search|seat|secure|security|seek|select|sener|services"
                + "|seven|sew|sex|sexy|sfr|shangrila|sharp|shaw|shell|shia|shiksha|shoes|shop|shopping"
                + "|shouji|show|showtime|silk|sina|singles|site|ski|skin|sky|skype|sling|smart|smile|sncf"
                + "|soccer|social|softbank|software|sohu|solar|solutions|song|sony|soy|spa|space|sport"
                + "|spot|srl|stada|staples|star|statebank|statefarm|stc|stcgroup|stockholm|storage|store"
                + "|stream|studio|study|style|sucks|supplies|supply|support|surf|surgery|suzuki|swatch"
                + "|swiss|sydney|systems|s[abcdeghijklmnorstuvxyz])"
                + "|(?:tab|taipei|talk|taobao|target|tatamotors|tatar|tattoo|tax|taxi|tci|tdk|team|tech"
                + "|technology|tel|temasek|tennis|teva|thd|theater|theatre|tiaa|tickets|tienda|tips|tires"
                + "|tirol|tjmaxx|tjx|tkmaxx|tmall|today|tokyo|tools|top|toray|toshiba|total|tours|town"
                + "|toyota|toys|trade|trading|training|travel|travelers|travelersinsurance|trust|trv|tube"
                + "|tui|tunes|tushu|tvs|t[cdfghjklmnortvwz])"
                + "|(?:ubank|ubs|unicom|university|uno|uol|ups|u[agksyz])"
                + "|(?:vacations|vana|vanguard|vegas|ventures|verisign|versicherung|vet|viajes|video"
                + "|vig|viking|villas|vin|vip|virgin|visa|vision|viva|vivo|vlaanderen|vodka|volkswagen"
                + "|volvo|vote|voting|voto|voyage|v[aceginu])"
                + "|(?:wales|walmart|walter|wang|wanggou|watch|watches|weather|weatherchannel|webcam"
                + "|weber|website|wed|wedding|weibo|weir|whoswho|wien|wiki|williamhill|win|windows|wine"
                + "|winners|wme|wolterskluwer|woodside|work|works|world|wow|wtc|wtf|w[fs])"
                + "|(?:\u03b5\u03bb|\u03b5\u03c5|\u0431\u0433|\u0431\u0435\u043b|\u0434\u0435\u0442\u0438"
                + "|\u0435\u044e|\u043a\u0430\u0442\u043e\u043b\u0438\u043a|\u043a\u043e\u043c|\u043c\u043a\u0434"
                + "|\u043c\u043e\u043d|\u043c\u043e\u0441\u043a\u0432\u0430|\u043e\u043d\u043b\u0430\u0439\u043d"
                + "|\u043e\u0440\u0433|\u0440\u0443\u0441|\u0440\u0444|\u0441\u0430\u0439\u0442|\u0441\u0440\u0431"
                + "|\u0443\u043a\u0440|\u049b\u0430\u0437|\u0570\u0561\u0575|\u05d9\u05e9\u05e8\u05d0\u05dc"
                + "|\u05e7\u05d5\u05dd|\u0627\u0628\u0648\u0638\u0628\u064a|\u0627\u062a\u0635\u0627\u0644\u0627\u062a"
                + "|\u0627\u0631\u0627\u0645\u0643\u0648|\u0627\u0644\u0627\u0631\u062f\u0646|\u0627\u0644\u0628\u062d\u0631\u064a\u0646"
                + "|\u0627\u0644\u062c\u0632\u0627\u0626\u0631|\u0627\u0644\u0633\u0639\u0648\u062f\u064a\u0629"
                + "|\u0627\u0644\u0639\u0644\u064a\u0627\u0646|\u0627\u0644\u0645\u063a\u0631\u0628|\u0627\u0645\u0627\u0631\u0627\u062a"
                + "|\u0627\u06cc\u0631\u0627\u0646|\u0628\u0627\u0631\u062a|\u0628\u0627\u0632\u0627\u0631"
                + "|\u0628\u064a\u062a\u0643|\u0628\u06be\u0627\u0631\u062a|\u062a\u0648\u0646\u0633|\u0633\u0648\u062f\u0627\u0646"
                + "|\u0633\u0648\u0631\u064a\u0629|\u0634\u0628\u0643\u0629|\u0639\u0631\u0627\u0642|\u0639\u0631\u0628"
                + "|\u0639\u0645\u0627\u0646|\u0641\u0644\u0633\u0637\u064a\u0646|\u0642\u0637\u0631|\u0643\u0627\u062b\u0648\u0644\u064a\u0643"
                + "|\u0643\u0648\u0645|\u0645\u0635\u0631|\u0645\u0644\u064a\u0633\u064a\u0627|\u0645\u0648\u0631\u064a\u062a\u0627\u0646\u064a\u0627"
                + "|\u0645\u0648\u0642\u0639|\u0647\u0645\u0631\u0627\u0647|\u067e\u0627\u06a9\u0633\u062a\u0627\u0646"
                + "|\u0680\u0627\u0631\u062a|\u0915\u0949\u092e|\u0928\u0947\u091f|\u092d\u093e\u0930\u0924"
                + "|\u092d\u093e\u0930\u0924\u092e\u094d|\u092d\u093e\u0930\u094b\u0924|\u0938\u0902\u0917\u0920\u0928"
                + "|\u09ac\u09be\u0982\u09b2\u09be|\u09ad\u09be\u09b0\u09a4|\u09ad\u09be\u09f0\u09a4|\u0a2d\u0a3e\u0a30\u0a24"
                + "|\u0aad\u0abe\u0ab0\u0aa4|\u0b2d\u0b3e\u0b30\u0b24|\u0b87\u0ba8\u0bcd\u0ba4\u0bbf\u0baf\u0bbe"
                + "|\u0b87\u0bb2\u0b99\u0bcd\u0b95\u0bc8|\u0b9a\u0bbf\u0b99\u0bcd\u0b95\u0baa\u0bcd\u0baa\u0bc2\u0bb0\u0bcd"
                + "|\u0c2d\u0c3e\u0c30\u0c24\u0c4d|\u0cad\u0cbe\u0cb0\u0ca4|\u0d2d\u0d3e\u0d30\u0d24\u0d02"
                + "|\u0dbd\u0d82\u0d9a\u0dcf|\u0e04\u0e2d\u0e21|\u0e44\u0e17\u0e22|\u0ea5\u0eb2\u0ea7|\u10d2\u10d4"
                + "|\u307f\u3093\u306a|\u30a2\u30de\u30be\u30f3|\u30af\u30e9\u30a6\u30c9|\u30b0\u30fc\u30b0\u30eb"
                + "|\u30b3\u30e0|\u30b9\u30c8\u30a2|\u30bb\u30fc\u30eb|\u30d5\u30a1\u30c3\u30b7\u30e7\u30f3"
                + "|\u30dd\u30a4\u30f3\u30c8|\u4e16\u754c|\u4e2d\u4fe1|\u4e2d\u56fd|\u4e2d\u570b|\u4e2d\u6587\u7f51"
                + "|\u4e9a\u9a6c\u900a|\u4f01\u4e1a|\u4f5b\u5c71|\u4fe1\u606f|\u5065\u5eb7|\u516b\u5366"
                + "|\u516c\u53f8|\u516c\u76ca|\u53f0\u6e7e|\u53f0\u7063|\u5546\u57ce|\u5546\u5e97|\u5546\u6807"
                + "|\u5609\u91cc|\u5609\u91cc\u5927\u9152\u5e97|\u5728\u7ebf|\u5927\u62ff|\u5929\u4e3b\u6559"
                + "|\u5a31\u4e50|\u5bb6\u96fb|\u5e7f\u4e1c|\u5fae\u535a|\u6148\u5584|\u6211\u7231\u4f60"
                + "|\u624b\u673a|\u62db\u8058|\u653f\u52a1|\u653f\u5e9c|\u65b0\u52a0\u5761|\u65b0\u95fb"
                + "|\u65f6\u5c1a|\u66f8\u7c4d|\u673a\u6784|\u6de1\u9a6c\u9521|\u6e38\u620f|\u6fb3\u9580"
                + "|\u70b9\u770b|\u79fb\u52a8|\u7ec4\u7ec7\u673a\u6784|\u7f51\u5740|\u7f51\u5e97|\u7f51\u7ad9"
                + "|\u7f51\u7edc|\u8054\u901a|\u8c37\u6b4c|\u8d2d\u7269|\u901a\u8ca9|\u96c6\u56e2|\u96fb\u8a0a\u76c8\u79d1"
                + "|\u98de\u5229\u6d66|\u98df\u54c1|\u9910\u5385|\u9999\u683c\u91cc\u62c9|\u9999\u6e2f"
                + "|\ub2f7\ub137|\ub2f7\ucef4|\uc0bc\uc131|\ud55c\uad6d"
                + "|xbox|xerox|xfinity|xihuan|xin|xn\\-\\-11b4c3d|xn\\-\\-1ck2e1b|xn\\-\\-1qqw23a|xn\\-\\-2scrj9c"
                + "|xn\\-\\-30rr7y|xn\\-\\-3bst00m|xn\\-\\-3ds443g|xn\\-\\-3e0b707e|xn\\-\\-3hcrj9c|xn\\-\\-3pxu8k"
                + "|xn\\-\\-42c2d9a|xn\\-\\-45br5cyl|xn\\-\\-45brj9c|xn\\-\\-45q11c|xn\\-\\-4dbrk0ce|xn\\-\\-4gbrim"
                + "|xn\\-\\-54b7fta0cc|xn\\-\\-55qw42g|xn\\-\\-55qx5d|xn\\-\\-5su34j936bgsg|xn\\-\\-5tzm5g"
                + "|xn\\-\\-6frz82g|xn\\-\\-6qq986b3xl|xn\\-\\-80adxhks|xn\\-\\-80ao21a|xn\\-\\-80aqecdr1a"
                + "|xn\\-\\-80asehdb|xn\\-\\-80aswg|xn\\-\\-8y0a063a|xn\\-\\-90a3ac|xn\\-\\-90ae|xn\\-\\-90ais"
                + "|xn\\-\\-9dbq2a|xn\\-\\-9et52u|xn\\-\\-9krt00a|xn\\-\\-b4w605ferd|xn\\-\\-bck1b9a5dre4c"
                + "|xn\\-\\-c1avg|xn\\-\\-c2br7g|xn\\-\\-cck2b3b|xn\\-\\-cckwcxetd|xn\\-\\-cg4bki|xn\\-\\-clchc0ea0b2g2a9gcd"
                + "|xn\\-\\-czr694b|xn\\-\\-czrs0t|xn\\-\\-czru2d|xn\\-\\-d1acj3b|xn\\-\\-d1alf|xn\\-\\-e1a4c"
                + "|xn\\-\\-eckvdtc9d|xn\\-\\-efvy88h|xn\\-\\-fct429k|xn\\-\\-fhbei|xn\\-\\-fiq228c5hs"
                + "|xn\\-\\-fiq64b|xn\\-\\-fiqs8s|xn\\-\\-fiqz9s|xn\\-\\-fjq720a|xn\\-\\-flw351e|xn\\-\\-fpcrj9c3d"
                + "|xn\\-\\-fzc2c9e2c|xn\\-\\-fzys8d69uvgm|xn\\-\\-g2xx48c|xn\\-\\-gckr3f0f|xn\\-\\-gecrj9c"
                + "|xn\\-\\-gk3at1e|xn\\-\\-h2breg3eve|xn\\-\\-h2brj9c|xn\\-\\-h2brj9c8c|xn\\-\\-hxt814e"
                + "|xn\\-\\-i1b6b1a6a2e|xn\\-\\-imr513n|xn\\-\\-io0a7i|xn\\-\\-j1aef|xn\\-\\-j1amh|xn\\-\\-j6w193g"
                + "|xn\\-\\-jlq480n2rg|xn\\-\\-jvr189m|xn\\-\\-kcrx77d1x4a|xn\\-\\-kprw13d|xn\\-\\-kpry57d"
                + "|xn\\-\\-kput3i|xn\\-\\-l1acc|xn\\-\\-lgbbat1ad8j|xn\\-\\-mgb9awbf|xn\\-\\-mgba3a3ejt"
                + "|xn\\-\\-mgba3a4f16a|xn\\-\\-mgba7c0bbn0a|xn\\-\\-mgbaakc7dvf|xn\\-\\-mgbaam7a8h|xn\\-\\-mgbab2bd"
                + "|xn\\-\\-mgbah1a3hjkrd|xn\\-\\-mgbai9azgqp6j|xn\\-\\-mgbayh7gpa|xn\\-\\-mgbbh1a|xn\\-\\-mgbbh1a71e"
                + "|xn\\-\\-mgbc0a9azcg|xn\\-\\-mgbca7dzdo|xn\\-\\-mgbcpq6gpa1a|xn\\-\\-mgberp4a5d4ar|xn\\-\\-mgbgu82a"
                + "|xn\\-\\-mgbi4ecexp|xn\\-\\-mgbpl2fh|xn\\-\\-mgbt3dhd|xn\\-\\-mgbtx2b|xn\\-\\-mgbx4cd0ab"
                + "|xn\\-\\-mix891f|xn\\-\\-mk1bu44c|xn\\-\\-mxtq1m|xn\\-\\-ngbc5azd|xn\\-\\-ngbe9e0a|xn\\-\\-ngbrx"
                + "|xn\\-\\-node|xn\\-\\-nqv7f|xn\\-\\-nqv7fs00ema|xn\\-\\-nyqy26a|xn\\-\\-o3cw4h|xn\\-\\-ogbpf8fl"
                + "|xn\\-\\-otu796d|xn\\-\\-p1acf|xn\\-\\-p1ai|xn\\-\\-pgbs0dh|xn\\-\\-pssy2u|xn\\-\\-q7ce6a"
                + "|xn\\-\\-q9jyb4c|xn\\-\\-qcka1pmc|xn\\-\\-qxa6a|xn\\-\\-qxam|xn\\-\\-rhqv96g|xn\\-\\-rovu88b"
                + "|xn\\-\\-rvc1e0am3e|xn\\-\\-s9brj9c|xn\\-\\-ses554g|xn\\-\\-t60b56a|xn\\-\\-tckwe|xn\\-\\-tiq49xqyj"
                + "|xn\\-\\-unup4y|xn\\-\\-vermgensberater\\-ctb|xn\\-\\-vermgensberatung\\-pwb|xn\\-\\-vhquv"
                + "|xn\\-\\-vuq861b|xn\\-\\-w4r85el8fhu5dnra|xn\\-\\-w4rs40l|xn\\-\\-wgbh1c|xn\\-\\-wgbl6a"
                + "|xn\\-\\-xhq521b|xn\\-\\-xkc2al3hye2a|xn\\-\\-xkc2dl3a5ee0h|xn\\-\\-y9a3aq|xn\\-\\-yfro4i67o"
                + "|xn\\-\\-ygbi2ammx|xn\\-\\-zfr164b|xxx|xyz)"
                + "|(?:yachts|yahoo|yamaxun|yandex|yodobashi|yoga|yokohama|you|youtube|yun|y[et])"
                + "|(?:zappos|zara|zero|zip|zone|zuerich|z[amw]))")
        const val STRICT_TLD: String = "(?:" +
                IANA_TOP_LEVEL_DOMAINS + "|" + PUNYCODE_TLD + ")"
        const val STRICT_HOST_NAME: String = ("(?:(?:" + IRI_LABEL + "\\.)+"
                + STRICT_TLD + ")")
        const val IP_ADDRESS_STRING: String =
            ("((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                    + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                    + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                    + "|[1-9][0-9]|[0-9]))")
        const val STRICT_DOMAIN_NAME: String = ("(?:" + STRICT_HOST_NAME + "|"
                + IP_ADDRESS_STRING + ")")
        const val PORT_NUMBER: String = "\\:\\d{1,5}"
        const val PATH_AND_QUERY: String = ("[/\\?](?:(?:[" + LABEL_CHAR
                + ";/\\?:@&=#~" // plus optional query params
                + "\\-\\.\\+!\\*'\\(\\),_\\$])|(?:%[a-fA-F0-9]{2}))*")
        const val WORD_BOUNDARY: String = "(?:\\b|$|^)"
        const val WEB_URL_WITHOUT_PROTOCOL: String = ("("
            + WORD_BOUNDARY
            + "(?<!:\\/\\/)"
            + "("
            + "(?:" + STRICT_DOMAIN_NAME + ")"
            + "(?:" + PORT_NUMBER + ")?"
            + ")"
            + "(?:" + PATH_AND_QUERY + ")?"
            + WORD_BOUNDARY
            + ")")
        val WEB_URL_WITHOUT_PROTOCOL_COMPILED = Pattern.compile(WEB_URL_WITHOUT_PROTOCOL, Pattern.CASE_INSENSITIVE)

        const val EMAIL_CHAR: String = LABEL_CHAR + "\\+\\-_%'"

        const val EMAIL_ADDRESS_LOCAL_PART: String =
            "[" + EMAIL_CHAR + "]" + "(?:[" + EMAIL_CHAR + "\\.]{0,62}[" + EMAIL_CHAR + "])?"
        const val TLD: String =
            "(" + PUNYCODE_TLD + "|" + "[" + TLD_CHAR + "]{2,63}" + ")"
        const val HOST_NAME: String = "(" + IRI_LABEL + "\\.)+" + TLD
        const val EMAIL_ADDRESS_DOMAIN: String = "(?=.{1,255}(?:\\s|$|^))" + HOST_NAME

        /**
         * Regular expression pattern to match email addresses. It excludes double quoted local parts
         * and the special characters #&~!^`{}/=$*?| that are included in RFC5321.
         * @hide
         */
        val AUTOLINK_EMAIL_ADDRESS: Pattern = Pattern.compile(
            "(" + WORD_BOUNDARY +
                    "(?:" + EMAIL_ADDRESS_LOCAL_PART + "@" + EMAIL_ADDRESS_DOMAIN + ")" +
                    WORD_BOUNDARY + ")"
        )

    }


    private val binding = ShowHabitNotesBinding.inflate(LayoutInflater.from(context), this)
    fun setState(state: NotesCardState) {
        if (state.description.isEmpty()) {
            visibility = GONE
            return
        } 
        
        visibility = VISIBLE
        
        // 1. Set text immediately (AutoLink in XML handles web/phone/email instantly)
        binding.habitNotes.text = state.description

        // 2. Launch background work to find "weird" links
        findViewTreeLifecycleOwner()?.lifecycleScope?.launch(Dispatchers.Default) {
            val description = state.description
            
            // Create a Spannable to work on
            val spannable = SpannableString(description)

            val patterns = listOf(
                // 1. Phone numbers (always valid)
                LinkPattern(
                    regex = Patterns.PHONE,
                    defaultScheme = "tel:"
                ),

                // 2. Email addresses (always valid)
                LinkPattern(
                    regex = AUTOLINK_EMAIL_ADDRESS,
                    defaultScheme = "mailto:"
                ),

                // 3. Standard web URLs (use standard filter)
                LinkPattern(
                    regex = WEB_URL_WITHOUT_PROTOCOL_COMPILED,
                    defaultScheme = "http://",
                    matchFilter = { s, start, end -> Linkify.sUrlMatchFilter.acceptMatch(s, start, end) },
                    transformFilter = { url ->
                        // Ensure proper scheme for www. links
                        if (url.startsWith("www.")) "http://$url" else url
                    }
                ),

                // 4. Arbitrary URI schemes (validated via Intent)
                LinkPattern(
                    regex = Pattern.compile(
                        "\\b[a-z][a-z0-9+.-]*://[^\\s]+",
                        Pattern.CASE_INSENSITIVE
                    ),
                    matchFilter = { text, start, end ->
                        val url = text.substring(start, end)

                        // Skip web URLs (assume valid)
                        if (url.startsWith("http://") || url.startsWith("https://")) {
                            return@LinkPattern true
                        }

                        // Validate via PackageManager
                        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                        context.packageManager.queryIntentActivities(
                            intent,
                            PackageManager.MATCH_DEFAULT_ONLY
                        ).isNotEmpty()
                    }
                )
            )

            val foundLinks = addLinks(spannable, patterns, context)

            // 3. Post back to UI only if we actually found something worth updating
            if (foundLinks) {
                withContext(Dispatchers.Main) {
                    binding.habitNotes.movementMethod = LinkMovementMethod.getInstance()
                    binding.habitNotes.setText(spannable, TextView.BufferType.SPANNABLE)
                }
            }
        }
    }
}
