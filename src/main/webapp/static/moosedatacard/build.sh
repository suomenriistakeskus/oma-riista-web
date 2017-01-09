#!/bin/sh

handlebars -om successful-uploads.tmpl.html > upload-templates.js
handlebars -om failed-uploads.tmpl.html >> upload-templates.js
