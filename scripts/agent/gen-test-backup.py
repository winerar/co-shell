#!/usr/bin/env python3
"""Генератор тестового бэкапа co-shell (формат `co-shell-backup` v1, см. core/data/.../BackupFormat.kt).

Создаёт правдоподобную историю семьи за N месяцев: счета в ARS/USD/USDT/AMD/RUB/EUR/BTC/JPY,
категории с подкатегориями, расходы (в т. ч. оплата USDT-картой в песо), доходы, переводы и обмены,
бюджеты, ручные курсы, импортированные из Wallet операции и пограничные случаи.

    python3 scripts/agent/gen-test-backup.py                     # ~10 000 операций за 24 мес.
    python3 scripts/agent/gen-test-backup.py --months 36 --per-day 20 --seed 7 -o big.json

Детерминирован по --seed. Все записи проходят проверки доменных моделей (суммы > 0, у перевода
есть счёт и сумма назначения, валюты 3–10 [A-Z0-9], importSource/importExternalId парами).
"""
import argparse
import json
import random
from datetime import date, datetime, timedelta, timezone

DIGITS = {"ARS": 2, "USD": 2, "USDT": 2, "AMD": 2, "RUB": 2, "EUR": 2, "BTC": 8, "JPY": 0}
MEMBERS = ["test-member-anna", "test-member-ivan"]
AR_TZ = timezone(timedelta(hours=-3))

ACCOUNTS = [
    # id, name, currency, type, initial (major units), shared, owner, archived, excluded
    ("acc-ars-cash", "Наличные песо", "ARS", "CASH", 150_000, True, None, False, False),
    ("acc-ars-card", "Galicia (песо)", "ARS", "CARD", 420_000, True, None, False, False),
    ("acc-usd-cash", "Доллары дома", "USD", "CASH", 3_000, True, None, False, False),
    ("acc-usdt-card", "USDT-карта Анны", "USDT", "CARD", 1_200, False, MEMBERS[0], False, False),
    ("acc-usdt-card2", "USDT-карта Ивана", "USDT", "CARD", 800, False, MEMBERS[1], False, False),
    ("acc-amd-salary", "Зарплатный AMD", "AMD", "CARD", 250_000, False, MEMBERS[1], False, False),
    ("acc-rub-card", "Карта в рублях", "RUB", "CARD", 85_000, True, None, False, False),
    ("acc-eur-deposit", "Вклад EUR", "EUR", "DEPOSIT", 5_000, True, None, False, True),
    ("acc-btc", "Холодный кошелёк BTC", "BTC", "CRYPTO", 0.0425, False, MEMBERS[1], False, True),
    ("acc-jpy-trip", "Поездка в Японию", "JPY", "CASH", 0, True, None, True, False),
    ("acc-old-rub", "Старая карта (закрыта)", "RUB", "CARD", 0, True, None, True, False),
    ("acc-other", "Долги друзьям и мне — очень длинное название счёта для проверки вёрстки", "USD",
     "OTHER", 0, True, None, False, False),
]

# Курс к USD на старт истории и месячный дрейф (песо инфляционный).
USD_RATE = {"ARS": (1000.0, 0.025), "USD": (1.0, 0), "USDT": (1.0, 0), "AMD": (395.0, -0.001),
            "RUB": (92.0, 0.003), "EUR": (0.93, 0.0), "BTC": (1 / 68000, -0.01), "JPY": (150.0, 0.0)}

EXPENSE_CATS = [
    # id, name, icon, color, children [(id, name, icon)], weight, typical ARS (major)
    ("cat-food", "Продукты", "food", 0xFF4DFF88,
     [("cat-food-super", "Супермаркет", "food"), ("cat-food-market", "Рынок и овощи", "shopping"),
      ("cat-food-meat", "Мясная лавка", "food")], 30, 25_000),
    ("cat-rest", "Рестораны и кафе", "restaurant", 0xFFFFA53D,
     [("cat-rest-cafe", "Кофейни", "cafe"), ("cat-rest-delivery", "Доставка", "restaurant")], 12, 18_000),
    ("cat-home", "Дом", "home", 0xFFFFB38A,
     [("cat-home-rent", "Аренда", "home"), ("cat-home-furniture", "Мебель", "furniture"),
      ("cat-home-repair", "Ремонт", "repair"), ("cat-home-clean", "Уборка", "cleaning")], 4, 40_000),
    ("cat-util", "Коммунальные услуги", "utilities", 0xFF8FB3CC, [], 3, 30_000),
    ("cat-transport", "Транспорт", "transport", 0xFF38B6FF,
     [("cat-transport-sube", "SUBE", "transport"), ("cat-transport-fuel", "Бензин", "fuel"),
      ("cat-transport-car", "Машина", "car")], 8, 6_000),
    ("cat-taxi", "Такси", "taxi", 0xFFFFD43B, [], 6, 9_000),
    ("cat-health", "Здоровье", "health", 0xFFFF4FD8,
     [("cat-health-pharmacy", "Аптека", "pharmacy"), ("cat-health-sport", "Спорт", "sport")], 4, 22_000),
    ("cat-kids", "Дети", "kids", 0xFFD36BFF, [], 5, 20_000),
    ("cat-clothes", "Одежда и обувь", "clothes", 0xFF6E8BFF, [], 3, 60_000),
    ("cat-fun", "Развлечения", "fun", 0xFFFF7A3D,
     [("cat-fun-movies", "Кино", "movies"), ("cat-fun-books", "Книги", "books"),
      ("cat-fun-travel", "Путешествия", "travel")], 4, 30_000),
    ("cat-edu", "Образование", "education", 0xFF00E5C0, [], 2, 50_000),
    ("cat-subs", "Связь и подписки", "subscriptions", 0xFF2BE4FF,
     [("cat-subs-phone", "Мобильная связь", "phone"), ("cat-subs-net", "Интернет", "internet")], 3, 12_000),
    ("cat-gift", "Подарки", "gift", 0xFFFF5470, [], 2, 35_000),
    ("cat-docs", "Документы и налоги", "documents", 0xFFB0B8C4, [], 1, 45_000),
    ("cat-pets", "Питомцы", "pets", 0xFFA8FF3E, [], 2, 15_000),
    ("cat-beauty", "Красота", "beauty", 0xFFFF9EC7, [("cat-beauty-hair", "Стрижка", "haircut")], 2, 25_000),
    ("cat-other", "Другое", "other", 0xFF7A8494, [], 2, 10_000),
]
INCOME_CATS = [("cat-in-salary", "Зарплата", "salary", 0xFF4DFF88),
               ("cat-in-freelance", "Подработка", "work", 0xFF00E5C0),
               ("cat-in-interest", "Проценты и дивиденды", "interest", 0xFFA8FF3E),
               ("cat-in-gift", "Подарки", "gift", 0xFFFF5470),
               ("cat-in-other", "Другое", "other", 0xFF7A8494)]

PAYEES = {"cat-food": ["Coto", "Carrefour", "Día", "Jumbo", "Disco", "Verdulería Don José"],
          "cat-rest": ["Café Tortoni", "La Cabrera", "Starbucks", "PedidosYa", "Rappi", "Güerrin"],
          "cat-taxi": ["Uber", "Cabify", "DiDi"], "cat-transport": ["YPF", "Shell", "SUBE", "Axion"],
          "cat-health": ["Farmacity", "OSDE", "SportClub"], "cat-subs": ["Personal", "Movistar", "Netflix",
          "Spotify", "Fibertel"], "cat-clothes": ["Zara", "Adidas", "Falabella"],
          "cat-fun": ["Cinemark", "El Ateneo", "Aerolíneas Argentinas"]}
NOTES = ["", "", "", "", "на неделю", "с друзьями", "день рождения", "срочно", "по акции -20%",
         "🍕 пицца", "вернуть половину Ивану", "Чек потерян", "оплата за два месяца",
         "Очень длинная заметка, чтобы проверить перенос строк в списке операций и на экране "
         "редактирования: купили всё по списку, плюс немного сверху, потому что были скидки."]
LABELS = ["отпуск", "работа", "подарок", "возврат", "кешбэк", "дача", "ремонт-2025"]


def minor(major: float, cur: str) -> int:
    return max(1, round(major * 10 ** DIGITS[cur]))


class Gen:
    def __init__(self, seed: int, months: int, per_day: float):
        self.r = random.Random(seed)
        self.end = date(2026, 11, 9)
        self.start = self.end - timedelta(days=round(months * 30.44))
        self.months, self.per_day = months, per_day
        self.n = 0
        self.tx = []

    def rate(self, cur: str, d: date) -> float:
        base, drift = USD_RATE[cur]
        m = (d - self.start).days / 30.44
        noise = 1 + self.r.uniform(-0.01, 0.01)
        return base * (1 + drift) ** m * noise

    def convert(self, major: float, frm: str, to: str, d: date) -> float:
        return major / self.rate(frm, d) * self.rate(to, d)

    def when(self, d: date, hour_from=8, hour_to=23) -> str:
        t = datetime(d.year, d.month, d.day, self.r.randint(hour_from, hour_to), self.r.randint(0, 59),
                     self.r.randint(0, 59), tzinfo=AR_TZ)
        return t.astimezone(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")

    def add(self, **kw):
        self.n += 1
        t = {"id": f"tx-{self.n:06d}", "authorMemberId": self.r.choice(MEMBERS)}
        t.update({k: v for k, v in kw.items() if v not in (None, [], "")})
        self.tx.append(t)

    def money(self, major, cur):
        return {"minor": minor(major, cur), "currency": cur}

    # --- генерация ------------------------------------------------------------------------
    def expenses(self, d: date):
        leaves = []
        for cid, _, _, _, kids, w, typical in EXPENSE_CATS:
            targets = [cid] + [k[0] for k in kids]
            leaves += [(self.r.choice(targets) if kids else cid, cid, typical)] * w
        count = max(0, round(self.r.gauss(self.per_day, self.per_day / 3)))
        for _ in range(count):
            cat, root, typical = self.r.choice(leaves)
            ars = typical * self.rate("ARS", d) / 1000 * self.r.lognormvariate(0, 0.6)
            roll = self.r.random()
            payee = self.r.choice(PAYEES[root]) if root in PAYEES and self.r.random() < 0.7 else None
            common = dict(type="EXPENSE", date=self.when(d), categoryId=cat, payee=payee,
                          note=self.r.choice(NOTES), labels=self.r.sample(LABELS, 1) if self.r.random() < 0.05 else [])
            if roll < 0.35:  # USDT-карта платит в песо: чек в ARS, списано USDT
                acc = self.r.choice(["acc-usdt-card", "acc-usdt-card2"])
                usdt = self.convert(ars, "ARS", "USDT", d) * 1.01
                self.add(accountId=acc, amount=self.money(usdt, "USDT"), originalAmount=self.money(ars, "ARS"), **common)
            elif roll < 0.65:
                self.add(accountId="acc-ars-card", amount=self.money(ars, "ARS"), **common)
            elif roll < 0.88:
                self.add(accountId="acc-ars-cash", amount=self.money(round(ars, -2) or 100, "ARS"), **common)
            elif roll < 0.95:
                self.add(accountId="acc-rub-card", amount=self.money(self.convert(ars, "ARS", "RUB", d), "RUB"), **common)
            else:
                self.add(accountId="acc-usd-cash", amount=self.money(round(self.convert(ars, "ARS", "USD", d)) or 1, "USD"), **common)

    def monthly(self, d: date):
        r = self.r
        self.add(type="INCOME", accountId="acc-amd-salary", date=self.when(d, 10, 12), categoryId="cat-in-salary",
                 amount=self.money(round(r.uniform(1_150_000, 1_250_000), -3), "AMD"), payee="ООО Арм-Софт")
        self.add(type="EXPENSE", accountId="acc-ars-card", date=self.when(d + timedelta(days=4)),
                 categoryId="cat-home-rent", amount=self.money(round(650_000 * self.rate("ARS", d) / 1000, -3), "ARS"),
                 payee="Inmobiliaria Palermo", note="аренда квартиры")
        for sub, usd in (("Netflix", 9.99), ("Spotify", 4.99), ("iCloud", 2.99)):
            self.add(type="EXPENSE", accountId="acc-usdt-card", date=self.when(d + timedelta(days=r.randint(1, 20))),
                     categoryId="cat-subs", amount=self.money(usd, "USDT"), payee=sub)
        # Обмен AMD → USDT (разные валюты: курс = toAmount / amount)
        amd = round(r.uniform(700_000, 900_000), -3)
        self.add(type="TRANSFER", accountId="acc-amd-salary", toAccountId=r.choice(["acc-usdt-card", "acc-usdt-card2"]),
                 date=self.when(d + timedelta(days=1)), amount=self.money(amd, "AMD"),
                 toAmount=self.money(self.convert(amd, "AMD", "USDT", d) * 0.99, "USDT"), note="обмен")
        # Продажа долларов на песо и пополнение наличных
        usd = r.choice([100, 200, 300, 500])
        self.add(type="TRANSFER", accountId="acc-usd-cash", toAccountId="acc-ars-cash",
                 date=self.when(d + timedelta(days=r.randint(2, 10))), amount=self.money(usd, "USD"),
                 toAmount=self.money(round(self.convert(usd, "USD", "ARS", d), -2), "ARS"), note="cambio")
        ars = round(r.uniform(150_000, 400_000) * self.rate("ARS", d) / 1000, -3)
        self.add(type="TRANSFER", accountId="acc-ars-card", toAccountId="acc-ars-cash",
                 date=self.when(d + timedelta(days=r.randint(1, 25))), amount=self.money(ars, "ARS"),
                 toAmount=self.money(ars, "ARS"), note="снятие в банкомате")
        self.add(type="INCOME", accountId="acc-eur-deposit", date=self.when(d + timedelta(days=27), 9, 9),
                 categoryId="cat-in-interest", amount=self.money(r.uniform(12, 18), "EUR"), payee="Банк")
        if r.random() < 0.5:
            self.add(type="INCOME", accountId="acc-usdt-card2", date=self.when(d + timedelta(days=r.randint(0, 27))),
                     categoryId="cat-in-freelance", amount=self.money(r.uniform(150, 900), "USDT"),
                     labels=["работа"], note="фриланс")
        if r.random() < 0.3:
            btc = r.uniform(0.001, 0.005)
            self.add(type="TRANSFER", accountId="acc-usdt-card2", toAccountId="acc-btc",
                     date=self.when(d + timedelta(days=r.randint(0, 27))),
                     amount=self.money(self.convert(btc, "BTC", "USDT", d), "USDT"), toAmount=self.money(btc, "BTC"))

    def trip_to_japan(self):
        d0 = self.start + timedelta(days=200)
        jpy = 300_000
        self.add(type="TRANSFER", accountId="acc-usd-cash", toAccountId="acc-jpy-trip", date=self.when(d0),
                 amount=self.money(2000, "USD"), toAmount=self.money(jpy, "JPY"), labels=["отпуск"])
        for i in range(30):
            d = d0 + timedelta(days=1 + i // 3)
            self.add(type="EXPENSE", accountId="acc-jpy-trip", date=self.when(d), labels=["отпуск"],
                     categoryId=self.r.choice(["cat-rest", "cat-fun-travel", "cat-transport", "cat-gift"]),
                     amount=self.money(round(self.r.uniform(800, 18000), -1), "JPY"))

    def edge_cases(self):
        d = self.end - timedelta(days=1)
        self.add(type="EXPENSE", accountId="acc-ars-cash", date=self.when(d), categoryId="cat-other",
                 amount=self.money(0.01, "ARS"), note="минимальная сумма")
        self.add(type="INCOME", accountId="acc-ars-card", date=self.when(d), categoryId="cat-in-other",
                 amount=self.money(987_654_321.99, "ARS"), note="огромная сумма — проверка вёрстки")
        self.add(type="EXPENSE", accountId="acc-ars-card", date=self.when(d), amount=self.money(5000, "ARS"),
                 note="без категории")
        self.add(type="EXPENSE", accountId="acc-btc", date=self.when(d), categoryId="cat-other",
                 amount={"minor": 1, "currency": "BTC"}, note="1 сатоши комиссии")
        self.add(type="INCOME", accountId="acc-other", date=self.when(d), categoryId="cat-in-gift",
                 amount=self.money(50, "USD"), note="Эмодзи 🎉👨‍👩‍👧 и \"кавычки\", <теги>, \\ слеши")
        self.add(type="EXPENSE", accountId="acc-ars-card", date=self.when(d), categoryId="cat-archived-old",
                 amount=self.money(1234, "ARS"), note="операция в архивной категории")
        self.add(type="EXPENSE", accountId="acc-ars-card", date=self.when(self.end, 8, 9),
                 categoryId="cat-food-super", amount=self.money(15_000, "ARS"), note="сегодня утром")
        self.add(type="TRANSFER", accountId="acc-rub-card", toAccountId="acc-old-rub", date=self.when(self.start),
                 amount=self.money(1000, "RUB"), toAmount=self.money(1000, "RUB"), note="перевод на архивный счёт")
        # Импорт из Wallet
        for i in range(200):
            day = self.start - timedelta(days=200 - i)
            self.add(type="EXPENSE", accountId="acc-old-rub", date=self.when(day), categoryId="cat-food",
                     amount=self.money(self.r.uniform(200, 4000), "RUB"), payee="Пятёрочка",
                     importSource="wallet", importExternalId=f"wallet-{100000 + i}")

    def run(self):
        d = self.start
        month = None
        while d <= self.end:
            if (d.year, d.month) != month:
                month = (d.year, d.month)
                self.monthly(d)
            self.expenses(d)
            d += timedelta(days=1)
        self.trip_to_japan()
        self.edge_cases()
        return self.tx

    def manual_rates(self):
        rates, d = [], self.start
        while d <= self.end:
            for cur in ("ARS", "AMD"):
                rates.append({"date": d.isoformat(), "from": "USD", "to": cur, "rate": f"{self.rate(cur, d):.2f}"})
            if d.weekday() == 0:
                rates.append({"date": d.isoformat(), "from": "USDT", "to": "ARS",
                              "rate": f"{self.rate('ARS', d) * 1.02:.2f}"})
            d += timedelta(days=self.r.choice([1, 1, 2, 3]))
        return rates


def build(args):
    g = Gen(args.seed, args.months, args.per_day)
    accounts = [{"id": i, "name": n, "currency": c, "type": t, "initialBalanceMinor": minor(b, c) if b else 0,
                 "isShared": s, "ownerMemberId": o, "archived": a, "sortOrder": k, "excludedFromTotal": x}
                for k, (i, n, c, t, b, s, o, a, x) in enumerate(ACCOUNTS)]
    categories = []
    for k, (cid, name, icon, color, kids, *_rest) in enumerate(EXPENSE_CATS):
        categories.append({"id": cid, "name": name, "type": "EXPENSE", "icon": icon, "color": color, "sortOrder": k})
        for j, (kid, kname, kicon) in enumerate(kids):
            categories.append({"id": kid, "name": kname, "type": "EXPENSE", "icon": kicon, "color": color,
                               "parentId": cid, "sortOrder": j})
    categories.append({"id": "cat-archived-old", "name": "Сигареты (бросили)", "type": "EXPENSE", "icon": "other",
                       "color": 0xFF7A8494, "archived": True, "sortOrder": 99})
    for k, (cid, name, icon, color) in enumerate(INCOME_CATS):
        categories.append({"id": cid, "name": name, "type": "INCOME", "icon": icon, "color": color, "sortOrder": k})

    transactions = sorted(g.run(), key=lambda t: t["date"])
    budgets = []
    y, m = g.end.year, g.end.month
    for back in range(12):
        mm, yy = (m - back - 1) % 12 + 1, y + (m - back - 1) // 12
        infl = (1.025) ** (12 - back)
        for cid, ars in (("cat-food", 900_000), ("cat-rest", 300_000), ("cat-taxi", 120_000), ("cat-fun", 200_000)):
            budgets.append({"id": f"budget-{yy}-{mm:02d}-{cid}", "categoryId": cid, "month": f"{yy}-{mm:02d}",
                            "limit": {"minor": int(round(ars * infl, -3)) * 100, "currency": "ARS"}})
        budgets.append({"id": f"budget-{yy}-{mm:02d}-cat-subs", "categoryId": "cat-subs", "month": f"{yy}-{mm:02d}",
                        "limit": {"minor": 3000, "currency": "USDT"}})

    return {"format": "co-shell-backup", "version": 1, "exportedAt": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
            "accounts": [{k: v for k, v in a.items() if v is not None} for a in accounts],
            "categories": categories, "transactions": transactions, "budgets": budgets,
            "manualRates": g.manual_rates()}


def main():
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("--months", type=int, default=24)
    p.add_argument("--per-day", type=float, default=12, help="среднее число обычных расходов в день")
    p.add_argument("--seed", type=int, default=42)
    p.add_argument("-o", "--out", default="co-shell-test-backup.json")
    args = p.parse_args()
    data = build(args)
    with open(args.out, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=1)
    print(f"{args.out}: {len(data['accounts'])} счетов, {len(data['categories'])} категорий, "
          f"{len(data['transactions'])} операций, {len(data['budgets'])} бюджетов, {len(data['manualRates'])} курсов")


if __name__ == "__main__":
    main()
