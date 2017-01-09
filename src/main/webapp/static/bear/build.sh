#!/bin/sh

handlebars -om bear-table.tmpl.fi.html > bear-templates.js
handlebars -om bear-popup.tmpl.fi.html >> bear-templates.js
handlebars -om bear-table.tmpl.sv.html >> bear-templates.js
handlebars -om bear-popup.tmpl.sv.html >> bear-templates.js