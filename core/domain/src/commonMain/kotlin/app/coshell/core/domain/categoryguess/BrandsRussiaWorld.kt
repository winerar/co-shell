package app.coshell.core.domain.categoryguess

/** Brands of Russia and of the world → icon key of the starter category (see [ARGENTINA_BRANDS]). Cyrillic is folded (ё → е). */
internal val RUSSIA_WORLD_BRANDS: Map<String, String> = mapOf(
    "food" to "пятерочка, магнит, перекресток, лента, ашан, дикси, вкусвилл, азбука вкуса, окей, верный, красное белое, бристоль, самокат, яндекс лавка, сбермаркет, " +
        "чижик, светофор, продукты, супермаркет, гастроном, булочная, пекарня, рынок, овощи, фрукты, мясо, молоко, " +
        "walmart, costco, tesco, aldi, lidl, mercadona, whole foods, trader joes, kroger, seven eleven, 7 eleven, family mart, spar, auchan, edeka, rewe",
    "restaurant" to "вкусно и точка, макдоналдс, бургер кинг, ростикс, додо пицца, dodo pizza, теремок, шоколадница, кофемания, stars coffee, cofix, кофикс, costa coffee, " +
        "coffee like, surf coffee, чайхона, пронто, якитория, тануки, суши вок, кафе, ресторан, кофейня, столовая, пиццерия, бар, пекарня кофе, доставка еды, delivery club, " +
        "uber eats, glovo, deliveroo, doordash, grubhub, just eat, dunkin, dominos, pizza hut, papa johns, five guys, shake shack, chipotle, taco bell, tim hortons, nando s, pret a manger",
    "taxi" to "яндекс go, яндекс такси, yandex go, ситимобил, maxim, максим, wheely, такси, lyft, grab, gett",
    "transport" to "метро, мосметро, тройка, ржд, rzd, аэрофлот, победа, s7, utair, ютэйр, уральские авиалинии, аэроэкспресс, мцд, парковка, делимобиль, ситидрайв, каршеринг, " +
        "лукойл, газпромнефть, роснефть, татнефть, азс, shell, bp, tnk, exxon, chevron, total, repsol, cepsa, texaco, ryanair, easyjet, lufthansa, air france, klm, emirates, " +
        "qatar airways, turkish airlines, delta, united airlines, american airlines, latam, avianca, copa airlines, iberia, wizz air, pegasus",
    "health" to "аптека, 36 6, ригла, аптека ру, еаптека, планета здоровья, горздрав, озерки, здравсити, медси, инвитро, гемотест, helix, клиника, стоматология, поликлиника, " +
        "больница, врач, лаборатория, оптика, cvs, walgreens, boots, pharmacy, dental, hospital",
    "utilities" to "жкх, мосэнергосбыт, мосводоканал, газпром межрегионгаз, капремонт, квартплата, электричество, водоканал, отопление, домофон, intercom, electricity, water bill",
    "subscriptions" to "мтс, билайн, мегафон, теле2, tele2, ростелеком, yota, йота, яндекс плюс, кинопоиск, ivi, окко, okko, wink, apple, itunes, app store, google, netflix, spotify, " +
        "youtube, disney, hbo, max, hulu, peacock, audible, github, dropbox, notion, adobe, microsoft, slack, figma, 1password, nordvpn, patreon, twitch, vpn, подписка, связь, " +
        "интернет, мобильная связь",
    "fun" to "кинотеатр, каро, синема парк, cinema, imax, amc, театр, билеты, концерт, музей, боулинг, аттракционы, игры, steam, playstation, xbox, nintendo, epic games, " +
        "караоке, квест, парк развлечений",
    "clothes" to "zara, h m, mango, bershka, pull bear, stradivarius, uniqlo, nike, adidas, gap, primark, asos, lamoda, спортмастер, sportmaster, decathlon, одежда, обувь, " +
        "ламода, бутик, levis, new balance, reserved, befree, gloria jeans, o stin",
    "home" to "ikea, икеа, леруа мерлен, leroy merlin, hornbach, castorama, home depot, obi, ферротон, петрович, строительный, хозтовары, мебель, сантехник, электрик, химчистка, прачечная",
    "gift" to "цветы, букет, подарок, подарки, цветочный, flowers, gift shop, hallmark",
    "education" to "coursera, udemy, skillshare, duolingo, linkedin learning, школа, университет, курсы, репетитор, книги, читай город, лабиринт, учебник, образование, детский сад",
    "kids" to "детский мир, игрушки, памперсы, chicco, кружок, секция, детская, lego, toys r us",
)
