package app.coshell.core.domain.categoryguess

/**
 * Everyday words of a quick note in Russian, Spanish (Argentina) and English → icon key of the starter category (#637, #631):
 * «кофе 3500», «обед 12к», «nafta 30000», «groceries 40». Common forms are listed; other endings are caught by the fuzzy step
 * of [BrandDictionary] («в аптеке» → «аптека»). Words that mean money in general («перевод», «долг») are left out on purpose.
 */
internal val EVERYDAY_WORDS: Map<String, String> = mapOf(
    "food" to "продуктов, продуктовые, хлеб, молоко, яйца, сыр, колбаса, курица, рыба, овощей, фруктов, крупы, сахар, чай, вода питьевая, " +
        "магазин продуктов, супермаркета, гипермаркет, рынке, бакалея, " +
        "comida, mercado, super, supermercado, verduras, frutas, carne, pollo, leche, pan, huevos, queso, facturas, yerba, " +
        "groceries, grocery, supermarket, food store",
    "restaurant" to "кофе, кофеек, капучино, латте, раф, обед, ужин, завтрак, ланч, бизнес ланч, перекус, пицца, суши, роллы, бургер, шашлык, хинкали, " +
        "столовка, кафешка, ресторане, кафе бар, пиво, вино, коктейль, " +
        "cafe, cafecito, cortado, medialunas, almuerzo, cena, desayuno, merienda, birra, cerveza, vino, helado, lomito, choripan, delivery, " +
        "coffee, lunch, dinner, breakfast, takeaway, takeout",
    "taxi" to "такси, таксист, такса, taxi, remis, remise, cab, ride",
    "transport" to "бензин, топливо, дизель, заправка, заправился, парковка платная, стоянка, метро проезд, автобус проезд, билет на поезд, поезд, " +
        "самолет, авиабилет, авиабилеты, каршеринг поездка, самокат кикшеринг, кикшеринг, whoosh, юрент, " +
        "combustible, gasoil, carga sube, boleto, pasaje, pasajes, tren, subte viaje, peaje autopista, " +
        "gas station, fuel, petrol, parking, bus, train, flight, metro card",
    "health" to "лекарства, лекарство, таблетки, витамины, врачу, доктор, анализ, узи, мрт, стоматологу, зубы, линзы, очки, бахилы, " +
        "remedios, medicamentos, medico, doctor, dentista, odontologo, analisis, obra social, prepaga, turno medico, " +
        "medicine, pills, drugstore pharmacy, doctor visit",
    "utilities" to "электроэнергия, газ за месяц, квартира коммуналка, вода за месяц, интернет домашний, " +
        "luz, agua, expensas, abl, edesur, edenor, metrogas, aysa, " +
        "rent utilities, electric bill, gas bill",
    "subscriptions" to "мобильный, телефон связь, сотовая, симка, роуминг, " +
        "celular, abono, plan celular, suscripcion, " +
        "phone bill, subscription, mobile plan",
    "fun" to "кино, фильм, кинотеатра, концерта, спектакль, выставка, музея, вечеринка, клуб, игра, игрушка для себя, хобби, " +
        "cine, teatro, recital, show, entradas, boliche, juego, museo, " +
        "movie, movies, concert, tickets, game, museum",
    "clothes" to "футболка, джинсы, куртка, кроссовки, ботинки, платье, носки, белье, шапка, " +
        "ropa, remera, zapatillas, zapatos, campera, pantalon, medias, " +
        "clothes, shoes, sneakers, jacket, shirt",
    "home" to "посуда, бытовая химия, хозяйственные, моющее, порошок, лампочка, ремонт, инструменты, мебель для дома, " +
        "limpieza, articulos de limpieza, ferreteria, bazar, " +
        "cleaning, hardware, household",
    "gift" to "подарок на, день рождения, цветочки, " +
        "regalo, regalos, cumple, cumpleanos, flores, " +
        "present, birthday",
    "education" to "учеба, учебники, урок, уроки, занятие, курсы английского, " +
        "curso, clases, libro, libros, utiles, colegio, facultad, " +
        "course, lesson, lessons, books, school",
    "kids" to "детям, ребенку, детское, игрушки детям, " +
        "juguete, juguetes, panales, " +
        "toys, diapers, kids",
)
