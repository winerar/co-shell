#!/usr/bin/env python3
"""Tiny QR code encoder for the landing page (#660, #671): byte mode, error correction M, versions 1–10, SVG output.

No dependencies (CI and the landing build stay plain python3). Follows ISO/IEC 18004: Reed–Solomon over GF(256),
all eight masks scored by the four penalty rules, the lowest wins. Test — scripts/test_qr_code.py (golden matrices).
"""

# Version → (data codewords per block groups, EC codewords per block) for level M.
# Each group entry: (number of blocks, data codewords per block).
_M_BLOCKS = {
    1: (10, [(1, 16)]), 2: (16, [(1, 28)]), 3: (26, [(1, 44)]), 4: (18, [(2, 32)]),
    5: (24, [(2, 43)]), 6: (16, [(4, 27)]), 7: (18, [(4, 31)]), 8: (22, [(2, 38), (2, 39)]),
    9: (22, [(3, 36), (2, 37)]), 10: (26, [(4, 43), (1, 44)]),
}
_ALIGN = {1: [], 2: [6, 18], 3: [6, 22], 4: [6, 26], 5: [6, 30], 6: [6, 34], 7: [6, 22, 38], 8: [6, 24, 42],
          9: [6, 26, 46], 10: [6, 28, 50]}

_EXP = [0] * 512
_LOG = [0] * 256
_x = 1
for _i in range(255):
    _EXP[_i] = _x
    _LOG[_x] = _i
    _x <<= 1
    if _x & 0x100:
        _x ^= 0x11D
for _i in range(255, 512):
    _EXP[_i] = _EXP[_i - 255]


def _mul(a, b):
    return 0 if a == 0 or b == 0 else _EXP[_LOG[a] + _LOG[b]]


def _rs_remainder(data, n):
    gen = [1]
    for i in range(n):
        nxt = [0] * (len(gen) + 1)
        for j, g in enumerate(gen):
            nxt[j] ^= g
            nxt[j + 1] ^= _mul(g, _EXP[i])
        gen = nxt
    rem = [0] * n
    for byte in data:
        factor = byte ^ rem[0]
        rem = rem[1:] + [0]
        for j in range(n):
            rem[j] ^= _mul(gen[j + 1], factor)
    return rem


def _capacity(version):
    _, groups = _M_BLOCKS[version]
    return sum(count * size for count, size in groups)


def _codewords(data, version):
    count_bits = 8 if version < 10 else 16
    bits = [0, 1, 0, 0] + [int(b) for b in format(len(data), f"0{count_bits}b")]
    for byte in data:
        bits += [int(b) for b in format(byte, "08b")]
    total = _capacity(version) * 8
    bits += [0] * min(4, total - len(bits))
    bits += [0] * (-len(bits) % 8)
    words = [int("".join(map(str, bits[i:i + 8])), 2) for i in range(0, len(bits), 8)]
    data_len = len(words)
    while len(words) < _capacity(version):
        words.append((0xEC, 0x11)[(len(words) - data_len) % 2])
    ec_len, groups = _M_BLOCKS[version]
    blocks, pos = [], 0
    for count, size in groups:
        for _ in range(count):
            blocks.append(words[pos:pos + size])
            pos += size
    ecs = [_rs_remainder(b, ec_len) for b in blocks]
    out = []
    for i in range(max(len(b) for b in blocks)):
        out += [b[i] for b in blocks if i < len(b)]
    for i in range(ec_len):
        out += [e[i] for e in ecs]
    return out


def _base(version):
    size = version * 4 + 17
    grid = [[None] * size for _ in range(size)]

    def finder(r, c):
        for dr in range(-1, 8):
            for dc in range(-1, 8):
                rr, cc = r + dr, c + dc
                if 0 <= rr < size and 0 <= cc < size:
                    ring = max(abs(dr - 3), abs(dc - 3))
                    grid[rr][cc] = ring != 2 and ring != 4

    finder(0, 0)
    finder(0, size - 7)
    finder(size - 7, 0)
    for i in range(8, size - 8):
        grid[6][i] = grid[i][6] = i % 2 == 0
    centers = _ALIGN[version]
    for r in centers:
        for c in centers:
            if (r, c) in ((6, 6), (6, centers[-1]), (centers[-1], 6)):
                continue  # under a finder pattern
            for dr in range(-2, 3):
                for dc in range(-2, 3):
                    grid[r + dr][c + dc] = max(abs(dr), abs(dc)) != 1
    grid[size - 8][8] = True  # dark module
    reserved = [[cell is not None for cell in row] for row in grid]
    for i in range(9):
        reserved[8][i] = reserved[i][8] = True
    for i in range(8):
        reserved[8][size - 1 - i] = reserved[size - 1 - i][8] = True
    if version >= 7:
        for i in range(6):
            for j in range(3):
                reserved[i][size - 11 + j] = reserved[size - 11 + j][i] = True
    return grid, reserved


_MASKS = [
    lambda r, c: (r + c) % 2 == 0, lambda r, c: r % 2 == 0, lambda r, c: c % 3 == 0, lambda r, c: (r + c) % 3 == 0,
    lambda r, c: (r // 2 + c // 3) % 2 == 0, lambda r, c: (r * c) % 2 + (r * c) % 3 == 0,
    lambda r, c: ((r * c) % 2 + (r * c) % 3) % 2 == 0, lambda r, c: ((r + c) % 2 + (r * c) % 3) % 2 == 0,
]


def _bch(value, poly, bits):
    v = value << bits
    top = poly.bit_length() - 1
    while v.bit_length() > top:
        v ^= poly << (v.bit_length() - poly.bit_length())
    return (value << bits) | v


def _place(version, codewords, mask):
    grid, reserved = _base(version)
    size = len(grid)
    bits = [(w >> (7 - i)) & 1 for w in codewords for i in range(8)]
    idx, upward, col = 0, True, size - 1
    while col > 0:
        if col == 6:
            col -= 1
        rows = range(size - 1, -1, -1) if upward else range(size)
        for r in rows:
            for c in (col, col - 1):
                if reserved[r][c]:
                    continue
                bit = bits[idx] if idx < len(bits) else 0
                idx += 1
                grid[r][c] = bool(bit) != _MASKS[mask](r, c)
        upward = not upward
        col -= 2
    fmt = _bch((0b00 << 3) | mask, 0b10100110111, 10) ^ 0b101010000010010  # level M = 00
    for i in range(15):
        bit = bool((fmt >> i) & 1)
        if i < 6:
            grid[i][8] = bit
        elif i < 8:
            grid[i + 1][8] = bit
        else:
            grid[size - 15 + i][8] = bit
        if i < 8:
            grid[8][size - 1 - i] = bit
        elif i < 9:
            grid[8][15 - i] = bit
        else:
            grid[8][14 - i] = bit
    if version >= 7:
        ver = _bch(version, 0b1111100100101, 12)
        for i in range(18):
            bit = bool((ver >> i) & 1)
            grid[i // 3][size - 11 + i % 3] = grid[size - 11 + i % 3][i // 3] = bit
    return grid


def _penalty(grid):
    size, score = len(grid), 0
    lines = grid + [list(col) for col in zip(*grid)]
    for line in lines:
        run = 1
        for i in range(1, size + 1):
            if i < size and line[i] == line[i - 1]:
                run += 1
            else:
                if run >= 5:
                    score += run - 2
                run = 1
        text = "".join("1" if x else "0" for x in line)
        for pattern in ("10111010000", "00001011101"):
            start = text.find(pattern)
            while start != -1:
                score += 40
                start = text.find(pattern, start + 1)
    for r in range(size - 1):
        for c in range(size - 1):
            if grid[r][c] == grid[r + 1][c] == grid[r][c + 1] == grid[r + 1][c + 1]:
                score += 3
    dark = sum(sum(row) for row in grid)
    score += abs(dark * 100 // (size * size) - 50) // 5 * 10
    return score


def matrix(text):
    """QR matrix (rows of bools, True = dark) for UTF-8 `text`; raises ValueError if it does not fit version 10-M."""
    data = text.encode("utf-8")
    for version in range(1, 11):
        header = 4 + (8 if version < 10 else 16)
        if header + len(data) * 8 <= _capacity(version) * 8:
            break
    else:
        raise ValueError("text too long for a QR code up to version 10")
    words = _codewords(data, version)
    grids = [_place(version, words, m) for m in range(8)]
    return min(grids, key=_penalty)


def svg(text, module=6, quiet=4):
    """SVG of the QR code: black squares on white, `quiet` modules of margin, one path."""
    grid = matrix(text)
    n = len(grid) + quiet * 2
    path = "".join(f"M{c + quiet} {r + quiet}h1v1h-1z" for r, row in enumerate(grid) for c, dark in enumerate(row) if dark)
    side = n * module
    return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {n} {n}" width="{side}" height="{side}" '
            f'shape-rendering="crispEdges" role="img"><rect width="{n}" height="{n}" fill="#fff"/>'
            f'<path d="{path}" fill="#000"/></svg>')
