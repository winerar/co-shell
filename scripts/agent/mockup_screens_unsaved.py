"""«Не сохранено» (#323, poll 2026-11-23 «А», poll board `EwDraftA`): the sheet closed with something typed — the next new one opens empty
with a plate on top: «Продолжить» puts the draft back, «✕» forgets it. Board `EntryUnsaved`; imported by mockup_screens_forms.py."""
from mockup_parts import *  # noqa: F401,F403
import mockup_screens_entry as e


def plate():
    return ('<div style="display: flex; align-items: center; gap: 8px; padding: 8px 8px 8px 14px; border-radius: 24px; background: %s; border: 1px solid %s;">'
            '<span style="flex-grow: 1; min-width: 0;"><div style="font-size: 13px; font-weight: 600;">Не сохранено</div>'
            '<div style="font-size: 12px; color: %s;">3 500,00 ARS · Кафе · 5 мин назад</div></span>'
            '<span style="height: 40px; padding: 0 16px; border-radius: 999px; background: %s; color: %s; font-size: 14px; display: flex; align-items: center;">Продолжить</span>'
            '<span style="width: 40px; height: 40px; border-radius: 50%%; display: flex; align-items: center; justify-content: center;">%s</span></div>'
            % (CARD, LINE, MUTED, CHIP, INK, svg('close', MUTED, 20, 1.8)))


def unsaved():
    top = plate() + e.type_bar('exp') + e.ribbon(0)
    return board('Новая операция: «Не сохранено»', e.sheet(top, e.amount_card('0', 'ARS', MUTED2), e.categories(), e.details()))


BOARDS = [('EntryUnsaved', unsaved)]
