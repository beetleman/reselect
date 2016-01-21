#!/usr/bin/env python
# -*- coding: utf-8 -*-

from __future__ import print_function, unicode_literals

import sys

from loremipsum import get_sentences


TMPL = '<option value="{value}">{name}</option>'


def generate_options(n):
    for value, name in zip(range(n), get_sentences(n, True)):
        print(TMPL.format(value=value, name=name))


def main():
    n = 1000
    if len(sys.argv) == 2:
        n = int(sys.argv[1])
    generate_options(n)


if __name__ == '__main__':
    main()
