#!/usr/bin/env python
# -*- coding: utf-8 -*-

from __future__ import print_function, unicode_literals

import sys

import argparse

from faker import Factory


OPTION_TMPL = '<option value="{value}">{text}</option>'
OPTION_SELCTED_TMPL = '<option selected value="{value}">{text}</option>'
SITE_TMPL = '''
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="css/reselect.css" rel="stylesheet" type="text/css">
    </head>
    <body>
        <div id="app">
            <select id="select" id="" name="foo" multiple>
                {options}
            </select>
        </div>

        <script src="{js}" type="text/javascript"></script>
        <script type="text/javascript">
         reselect.core.create('#app');
        </script>
    </body>
</html>
'''


def text(faker):
    return '{name} &lt;{email}&gt;'.format(name=faker.name(), email=faker.email())


def option(value, faker):
    if value <= 1:
        tmpl = OPTION_SELCTED_TMPL
    else:
        tmpl = OPTION_TMPL
    return tmpl.format(value=value, text=text(faker))


def generate_selection(n, js):
    faker = Factory.create()
    options = {option(value, faker) for value in range(n)}
    return SITE_TMPL.format(
        options='\n                '.join(options),
        js=js
    )


def main():
    parser = argparse.ArgumentParser(description='Site generator.')
    parser.add_argument('-n', type=int, nargs='?', default=1000,
                        help='Number of options')
    parser.add_argument('-js', type=str, nargs='?',
                        default='js/compiled/reselect.js',
                        help='Path for reselect.js')
    args = parser.parse_args()
    n = args.n
    js = args.js
    print(generate_selection(n, js))


if __name__ == '__main__':
    main()
