#!/usr/bin/env python3
"""Synthetic Wallet CSV export for scale checks (#131): realistic mix of accounts, categories,
currencies, transfers (pairs), payees and labels over several years. Deterministic (fixed seed).
Usage: scripts/agent/gen-wallet-export.py [rows=10000] > wallet-10k.csv
Format — docs/samples/wallet-export-sample.csv (ARCHITECTURE §8)."""
import random
import sys
from datetime import datetime, timedelta

HEADER = ("account;category;currency;amount;ref_currency_amount;type;payment_type;payment_type_local;note;date;"
          "gps_latitude;gps_longitude;gps_accuracy_in_meters;warranty_in_month;transfer;payee;labels;envelope_id;custom_category")
ACCOUNTS = [("Наличные ARS", "ARS", 0.24), ("Карта USD", "USD", 380.0), ("USDT", "USD", 380.0),
            ("Карта RUB", "RUB", 4.75), ("Счёт AMD", "AMD", 1.0)]
EXPENSES = ["Продукты", "Рестораны", "Кафе", "Транспорт", "Такси", "Дети", "Образование", "Здоровье",
            "Аптека", "Одежда", "Дом", "Связь", "Подписки", "Подарки", "Развлечения", "Путешествия"]
INCOMES = ["Зарплата", "Проценты с вкладов, дивиденды", "Подарки полученные", "Возврат"]
PAYEES = ["", "", "", "Coto", "Carrefour", "Día", "Farmacity", "Uber", "Cabify", "Netflix", "Spotify"]
LABELS = ["", "", "", "", "Кофе", "Пекарня", "Курсы", "Отпуск"]
NOTES = ["", "", "", "Овощи", "Обед", "Подарок маме", "Коляска: аванс", "Такси домой", "Аренда; ноябрь"]


def money(value: float) -> str:
    return f"{value:.2f}"


def row(account, category, currency, amount, rate, when, transfer=False, note="", payee="", label="", ref=None):
    ref = amount * rate if ref is None else ref
    kind = "Ingresos" if amount > 0 else "Gastos"
    note = f'"{note}"' if ";" in note else note
    return ";".join([account, category, currency, money(amount), money(ref), kind, "CASH", "Efectivo",
                     note, when.strftime("%Y-%m-%d %H:%M:%S"), "", "", "", "0", str(transfer).lower(), payee,
                     label, str(random.randint(1000, 30000)), "false"])


def main():
    total = int(sys.argv[1]) if len(sys.argv) > 1 else 10_000
    random.seed(131)
    when = datetime(2026, 11, 8, 21, 0, 0)
    step = timedelta(days=4 * 365) / total  # about four years of history
    lines = []
    while len(lines) < total:
        when -= step * random.uniform(0.5, 1.5)
        dice = random.random()
        if dice < 0.06 and total - len(lines) >= 2:  # a transfer: two rows with the same time
            (a, ca, ra), (b, cb, rb) = random.sample(ACCOUNTS, 2)
            out = round(random.uniform(10, 2000) / ra * 380, 2)
            ref = round(out * ra, 2)  # Wallet pairs transfer rows by equal |ref_currency_amount|
            lines.append(row(a, "TRANSFER", ca, -out, ra, when, transfer=True, ref=-ref))
            lines.append(row(b, "TRANSFER", cb, round(ref / rb, 2), rb, when, transfer=True, ref=ref))
        elif dice < 0.12:
            acc, cur, rate = random.choice(ACCOUNTS)
            lines.append(row(acc, random.choice(INCOMES), cur, round(random.uniform(50, 3000) * 380 / rate, 2), rate, when))
        else:
            acc, cur, rate = random.choice(ACCOUNTS)
            amount = -round(random.uniform(1, 120) * 380 / rate, 2)
            lines.append(row(acc, random.choice(EXPENSES), cur, amount, rate, when,
                             note=random.choice(NOTES), payee=random.choice(PAYEES), label=random.choice(LABELS)))
    print(HEADER)
    print("\n".join(lines))


if __name__ == "__main__":
    main()
