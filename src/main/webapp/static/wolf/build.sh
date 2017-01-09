#!/bin/sh

handlebars -om wolf-table.tmpl.fi.html > wolf-templates.js
handlebars -om wolf-popup.tmpl.fi.html >> wolf-templates.js
handlebars -om wolf-table.tmpl.sv.html >> wolf-templates.js
handlebars -om wolf-popup.tmpl.sv.html >> wolf-templates.js