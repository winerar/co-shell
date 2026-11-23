package app.coshell.core.domain.categoryguess

/**
 * More brands of Russia and of the CIS (Armenia, Kazakhstan, Georgia, Belarus, Uzbekistan, Kyrgyzstan) → icon key of the starter
 * category (#637, owner 2026-11-22: «главные регионы — Россия и СНГ, Аргентина»). Names are comma-separated, Cyrillic is folded.
 */
internal val CIS_BRANDS: Map<String, String> = mapOf(
    "food" to "магнит у дома, магнит семейный, перекресток впрок, лента онлайн, окей экспресс, metro c c, метро кэш энд керри, глобус, карусель, монетка, мария ра, " +
        "ярче, командор, полушка, тамерлан, батон, сельпо, магнолия, о кей, жизньмарт, утконос, впрок, вкусвилл экспресс, азбука, гипер, " +
        "фасоль, яндекс еда маркет, купер, kuper, магазин у дома, продуктовый, минимаркет, универсам, фермерский, " +
        "yerevan city, ереван сити, sas supermarket, сас, carrefour armenia, nor zovq, нор зовк, parma, парма, star supermarket, evrika, тсиран, " +
        "magnum, магнум, small, смолл, galmart, гал март, arbuz kz, арбуз, toimart, анвар, " +
        "nikora, никора, carrefour georgia, agrohub, ori nabiji, 2 nabiji, goodwill, " +
        "евроопт, евроторг, санта, корона, гиппо, алми, белмаркет, родная сторона, " +
        "korzinka, корзинка, makro uz, havas, хавас, baraka, frunze, народный кг, globus kg",
    "restaurant" to "шоколадница кафе, му му, мумушка, крошка картошка, теремок блины, грабли, раковарня, тануки суши, суши мастер, сушишоп, sushi shop, ёбидоёби, " +
        "якитория, кофе хауз, coffee house, правда кофе, one price coffee, dabl b, даблби, буханка, хлеб насущный, вареничная, чайхона номер один, " +
        "kfc rostics, бургер, шаурма, шаверма, донер, кебаб, хинкальная, пельменная, чебуречная, блинная, бистро, фудкорт, " +
        "tashir pizza, ташир пицца, jazzve, джазве, lavash, лаваш, karas, карас, kfc armenia, " +
        "kaspi gold food, del papa, chocosalam bro, саламбро, " +
        "machakhela, мачахела, shemoikhede genatsvale, khinkali, " +
        "лидо, lido, васильки, " +
        "evos, эвос, oqtepa lavash, октепа, bellissimo pizza",
    "taxi" to "яндекс go такси, yandex taxi, uber russia, индрайвер, indriver, gg taxi, bolt armenia, ggtaxi, uklon, уклон, онтакси, ontaxi, maxim taxi, " +
        "убер, кабифай, диди, такси максим, везет, везёт, ситимобил такси",
    "transport" to "аэрофлот бонус, ласточка, сапсан, туту, tutu, aviasales, авиасейлс, ozon travel, электричка, автобус, трамвай, троллейбус, маршрутка, " +
        "проезд, проездной, тройка пополнение, стрелка, подорожник, мцк, автомойка, мойка, шиномонтаж, автосервис, техосмотр, осаго, каско, " +
        "газпром, нефтьмагистраль, teboil, тебойл, трасса, платная дорога, автодор, " +
        "flypgs, armenia airways, fly arna, флай арна, gazprom armenia, rompetrol, socar, сокар, wissol, gulf, " +
        "air astana, эйр астана, scat, fly arystan, helios, қазмұнайгаз, kmg, " +
        "беларуснефть, belavia, белавиа, uzbekistan airways, uzbekneftegaz",
    "health" to "вита, аптека вита, максавит, имплозия, мелодия здоровья, будь здоров, аптечка, неофарм, столички, " +
        "сбер здоровье, доктор рядом, см клиника, кдл, ситилаб, анализы, стоматолог, зубной, окулист, терапевт, массаж, " +
        "natali pharm, натали фарм, alfa pharm, альфа фарм, europharma, евро фарма, биосфера, " +
        "psp pharmacy, aversi, аверси, gpc, " +
        "белфармация, адель",
    "utilities" to "мосгаз, петроэлектросбыт, энергосбыт, теплосеть, мусор, вывоз мусора, газ, свет, коммуналка, коммунальные, жку, управляющая компания, ук, тсж, " +
        "ena, ена, veolia, вода ереван, газпром армения, " +
        "казахтелеком, алсеко, astana energy, " +
        "telasi, теласи, gwp, " +
        "ерип",
    "subscriptions" to "мтс премиум, сберпрайм, сбер прайм, т банк про, тинькофф про, вк музыка, vk музыка, вк комбо, литрес, букмейт, amediateka, " +
        "амедиатека, кион, kion, more tv, яндекс 360, chatgpt, openai, claude, anthropic, telegram premium, " +
        "ucom, юком, viva mts, вива мтс, team telecom, beeline armenia, " +
        "kcell, activ, актив, beeline kz, altel, tele2 kz, kaspi подписка, " +
        "magti, магти, silknet, cellfie, " +
        "a1, мтс беларусь, " +
        "ucell, uzmobile, mobiuz",
    "fun" to "кассир ру, яндекс афиша, афиша, кинопоиск билеты, формула кино, киномакс, мираж синема, планета кино, кинотеатр люксор, " +
        "зоопарк, океанариум, аквапарк, бильярд, кальян, кальянная, баня, сауна, развлечения, " +
        "kinopark, кинопарк, chaplin, чаплин, cinema star, кинозал",
    "clothes" to "wildberries одежда, глория джинс, ostin, остин, zolla, золла, love republic, sela, incity, твое, tvoe, finn flare, kari, кари, " +
        "ecco, рандеву, эконика, ralf ringer, пятка, обувь россии, хм, lc waikiki, colin s, defacto, koton, marks spencer, " +
        "детская одежда",
    "home" to "wildberries, вайлдберриз, вб, wb, ozon, озон, яндекс маркет, мегамаркет, авито, avito, aliexpress, алиэкспресс, temu, " +
        "фикс прайс, fix price, галамарт, светофор хоз, хофф, hoff, аскона, много мебели, лазурит, столплит, орматек, " +
        "днс, dns, м видео, мвидео, эльдорадо, ситилинк, technopark, технопарк, re store, бытовая техника, " +
        "всеинструменты, максидом, бауцентр, стройландия, 220 вольт, " +
        "kaspi магазин, kaspi kz, technodom, техно дом, sulpak, сулпак, mechta kz, " +
        "vega, zigzag, зигзаг, " +
        "21vek, 21 век, электросила",
    "gift" to "флорист, флорвей, florist, цветочная лавка, сувениры, открытка",
    "education" to "skyeng, скаенг, яндекс практикум, практикум, нетология, skillbox, скиллбокс, geekbrains, учи ру, фоксфорд, foxford, вебинар, тренинг, " +
        "буквоед, московский дом книги, book24, тетради, канцтовары, комус",
    "kids" to "кораблик, дочки сыночки, бебиклаб, акушерство, подгузники, детское питание, детская площадка, развивашки, " +
        "кружки, садик, няня",
)
