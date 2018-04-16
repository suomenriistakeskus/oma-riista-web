/*
 * App that searches Suomen riistakeskus event information from oma.riista.fi portal.
 */
(function() {
    window.EventsApp = function(options) {
        var self = this;
        this.supportedAreas = ["850", "000", "050", "100", "150", "200", "250", "300", "350", "400", "450", "500", "550", "600", "650", "700"];
        this.dateUIFormat = "DD.MM.YYYY";
        this.dateBEFormat = "YYYY-MM-DD";
        this.options = options;
        this.$areaSelection = jQuery(options.areaSelector);
        this.$rhySelection = jQuery(options.rhySelector);
        this.$eventSelection = jQuery(options.eventSelector);
        this.$eventStartDate = jQuery(options.eventStartDateSelector);
        this.$eventEndDate = jQuery(options.eventEndDateSelector);
        this.$mapContainer = jQuery(options.mapContainerSelector);
        this.$searchForm = jQuery(options.formSelector);
        this.$searchResults = jQuery(options.searchResultsSelector);

        this.getJSONP = function (urlSuffix, requestData, successCallback) {
            requestData = requestData || {};
            if (self.options.lang) requestData.lang = self.options.lang;

            return jQuery.ajax({
                type: "GET",
                data: requestData,
                url: self.options.backendUrl + urlSuffix,
                accepts: "application/json; charset=UTF-8",
                dataType: "jsonp",
                timeout: 2000,
                cache: false,
                crossDomain: true,
                success: successCallback
            });
        };
        this.installEventHandlers = function() {
            window.mapClickHandler = function(selectedArea) {
                if (!!selectedArea) {
                    self.handleMapClickEvent(selectedArea);
                }
                return false;
            };
            self.$searchForm.submit(function(event) {
                event.preventDefault();
                self.handleContactInformationSearch();
                return false;
            });
            self.$areaSelection.change(function(event) {
                self.handleAreaSelection();
                return false;
            });
        };
        this.start = function() {
            if (self.getParameterFromFragment("lang") === "sv") {
                this.options.lang = 'sv';
            } else if (self.getParameterFromFragment("lang") === "fi") {
                this.options.lang = 'fi';
            }

            if (maps) {
                self.$mapContainer.append(maps);
                self.$mapContainer.append("<div id='r-map'></div>");
            } else {
                return false;
            }

            self.updateAreaMap("");
            self.setDatesInitialStateFromUrl();
            self.updateRHYList();
            self.updateEventTypeList(self.setEventStateFromUrl);
            self.updateAreaList(self.setAreaStateFromUrl);

            window.setTimeout(function() {
                self.installEventHandlers();
            }, 250);

            window.setTimeout(function() {
                if (self.getParameterFromFragment("onlyresults") === "true") {
                    self.$searchForm.hide();
                    self.$mapContainer.hide();
                }

                self.doEventSearchFromUrlParams();
            }, 500);
        };
        this.getParameterFromFragment = function(name) {
            name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
            var regex = new RegExp("[\\?&#]" + name + "=([^&#]*)");
            var results = regex.exec(location.hash);
            return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
        };

        this.setDatesInitialStateFromUrl = function() {
            if (self.getParameterFromFragment("begin")) {
                self.$eventStartDate.val(self.getParameterFromFragment("begin"));
            } else {
                self.$eventStartDate.val(moment().format(self.dateUIFormat));
            }
            if (self.getParameterFromFragment("end")) {
                self.$eventEndDate.val(self.getParameterFromFragment("end"));
            } else {
                self.$eventEndDate.val(moment().date(1).add('months', 3).subtract('days', 1).format(self.dateUIFormat));
            }
        };
        this.setAreaStateFromUrl = function() {
            if (!self.getParameterFromFragment("area")) return;
            self.updateAreaSelection(self.getParameterFromFragment("area"));
            self.handleAreaSelection(self.setRHYStateFromUrl);
        };
        this.setRHYStateFromUrl = function() {
            if (!self.getParameterFromFragment("rhy")) return;
            self.updateRHYSelection(self.getParameterFromFragment("rhy"));
        };
        this.setEventStateFromUrl = function() {
            self.$eventSelection.val(self.getParameterFromFragment("event"));
        };
        this.updateStateToUrl = function() {
            var params = {};
            if (self.$areaSelection.val()) params["area"] = self.$areaSelection.val();
            if (self.$rhySelection.val()) params["rhy"] = self.$rhySelection.val();
            if (self.$eventSelection.val()) params["event"] = self.$eventSelection.val();
            if (self.$eventStartDate.val()) params["begin"] = self.$eventStartDate.val();
            if (self.$eventEndDate.val()) params["end"] = self.$eventEndDate.val();
            var hash = "#";
            for (var key in params) {
                if (hash.length > 1) hash += "&";
                hash += key + "=" + params[key];
            }
            location.hash = hash;
        };
        this.doEventSearchFromUrlParams = function() {
            var areaId = self.getParameterFromFragment("area");
            var rhyId = self.getParameterFromFragment("rhy");
            var eventType = self.getParameterFromFragment("event");
            var startDate = self.getParameterFromFragment("begin");
            var endDate = self.getParameterFromFragment("end");
            if (!areaId && !rhyId && !eventType && !startDate && !endDate) return;
            self.searchEvents(areaId, rhyId, eventType, startDate, endDate);
        };
        this.handleContactInformationSearch = function() {
            self.updateStateToUrl();
            var areaId = self.$areaSelection.val();
            var rhyId = self.$rhySelection.val();
            var eventType = self.$eventSelection.val();
            var startDate = self.$eventStartDate.val();
            var endDate = self.$eventEndDate.val();
            self.searchEvents(areaId, rhyId, eventType, startDate, endDate);
        };
        this.searchEvents = function(areaId, rhyId, eventType, startDate, endDate) {
            var startDateParsed = moment(startDate, self.dateUIFormat);
            var endDateParsed = moment(endDate, self.dateUIFormat);

            if (!startDateParsed.isValid() || !endDateParsed.isValid()) {
                self.$searchResults.html(self.options.translations.datesAreRequired);
                return;
            }

            var requestData = {
                begin: startDateParsed.format(self.dateBEFormat),
                end: endDateParsed.format(self.dateBEFormat),
                areaId: areaId,
                rhyId: rhyId,
                calendarEventType: eventType
            };

            self.getJSONP("/tapahtumat.jsonp", requestData, function(data) {
                var events = data.events || [];
                if (data.tooManyResults) {
                    self.$searchResults.html(self.options.translations.tooManyResults);
                    return;
                }
                if (events.length === 0) {
                    self.$searchResults.html(self.options.translations.noResults);
                    return;
                }
                var tableRowDataString = "";
                var oddColor = self.options.oddColor;
                var evenColor = self.options.evenColor;
                for (var i = 0; i < events.length; i++) {
                    var bgColor = i % 2 == 0 ? evenColor : oddColor;
                    tableRowDataString += "<tr><td rowspan='6' style='vertical-align: top; background-color: "+bgColor+";'>" + moment(events[i].date, self.dateBEFormat).format(self.dateUIFormat) + "</td></tr>";
                    tableRowDataString = self.addTimeRow(events[i], tableRowDataString, bgColor);
                    tableRowDataString = self.addTableRow(events[i].organisation.name, tableRowDataString, bgColor);
                    tableRowDataString = self.addEventTypeAndNameRow(events[i], tableRowDataString, bgColor);
                    tableRowDataString = self.addVenueRow(events[i].venue, tableRowDataString, bgColor);
                    var description = events[i].description ? events[i].description.replace(/(?:\r\n|\r|\n)/g, '<br />') : '';
                    tableRowDataString = self.addTableRow(description, tableRowDataString, bgColor);
                }
                self.$searchResults.html("<table style='border-spacing: 0; width: 100%;'>" + tableRowDataString + "</table>");
            });
        };
        this.addEventTypeAndNameRow = function(data, tableString, bgColor) {
            tableString += "<tr style='background-color: "+bgColor+";'><td>";
            tableString += data.calendarEventType.name;
            if (data.name) tableString += " " + data.name;
            tableString += "</td></tr>";
            return tableString;
        };
        this.addTimeRow = function(data, tableString, bgColor) {
            tableString += "<tr style='background-color: "+bgColor+";'><td>";
            if (data.beginTime) tableString += data.beginTime;
            if (data.endTime) tableString += " - " + data.endTime;
            tableString += "</td></tr>";
            return tableString;
        };
        this.addVenueRow = function(data, tableString, bgColor) {
            tableString += "<tr style='background-color: "+bgColor+";'><td>";
            if (data.name) tableString += data.name + "<br/>";
            if (data.streetAddress) tableString += data.streetAddress + " ";
            if (data.postalCode) tableString += data.postalCode + " ";
            if (data.city) tableString += data.city;
            tableString += "</td></tr>";
            return tableString;
        };
        this.addTableRow = function(data, tableString, bgColor) {
            tableString += "<tr style='background-color: "+bgColor+";'><td>";
            if (data) tableString += data;
            tableString += "</td></tr>";
            return tableString;
        };
        this.clearResults = function() {
            self.$searchResults.html("");
        };
        var sortOptionData = function (a, b) {
            var left = jQuery(a).eq(0).text();
            var right = jQuery(b).eq(0).text();

            return left.localeCompare(right);
        };
        this.updateAreaList = function(callback) {
            self.getJSONP("/rk.jsonp", {}, function(data) {
                var options = [];
                self.addAreasAsOptionsToArray(data, options);
                options.sort(sortOptionData);
                options.unshift(jQuery("<option value='' selected=''>("+self.options.translations.all+")</option>"));
                self.$areaSelection.append(options);
                self.areaListDone = true;
                if (callback && typeof callback === "function")
                    callback();
            });
        };
        this.updateRHYList = function(selectedArea, callback) {
            if (selectedArea) {
                self.getJSONP("/RKA/" + selectedArea + ".jsonp", {}, function(data) {
                    var options = [];
                    self.addRHYsAsOptionsToArray(data, options);
                    options.sort(sortOptionData);
                    options.unshift(jQuery("<option value='' selected=''>("+self.options.translations.all+")</option>"));
                    self.$rhySelection.html("");
                    self.$rhySelection.append(options);
                    self.rhyListDone = true;
                    if (callback && typeof callback === "function")
                        callback();
                });
            } else {
                self.$rhySelection.html("<option value='' selected=''>("+self.options.translations.all+")</option>");
                self.rhyListDone = true;
                if (callback && typeof callback === "function")
                    callback();
            }
        };
        this.updateEventTypeList = function(callback) {
            self.getJSONP("/tapahtumatyypit.jsonp", {}, function(data) {
                var options = ["<option value='' selected=''>("+self.options.translations.all+")</option>"];
                for (var i = 0; i < data.length; i++) {
                    self.addEventTypesAsOptionsToArray(data[i], options);
                }
                self.$eventSelection.append(options);
                self.eventTypeListDone = true;
                if (callback)
                    callback();
            });
        };
        this.addAreasAsOptionsToArray = function(data, array) {
            if (data.organisationType === 'RKA' && jQuery.inArray(data.officialCode, self.supportedAreas) !== -1) {
                var option = jQuery('<option/>');
                option.attr({ 'value': data.officialCode }).text(data.name);
                array.push(option);
            }
            if (data.subOrganisations) {
                for (var i = 0; i < data.subOrganisations.length; i++) {
                    self.addAreasAsOptionsToArray(data.subOrganisations[i], array);
                }
            }
        };
        this.addRHYsAsOptionsToArray = function(data, array) {
            if (data.subOrganisations) {
                for (var i = 0; i < data.subOrganisations.length; i++) {
                    if (data.subOrganisations[i].organisationType === 'RHY') {
                        var option = jQuery('<option/>');
                        option.attr({ 'value': data.subOrganisations[i].officialCode }).text(data.subOrganisations[i].name);
                        array.push(option);
                    }
                }
            }
        };
        this.addEventTypesAsOptionsToArray = function(data, array) {
            var option = jQuery('<option/>');
            option.attr({ 'value': data.calendarEventType }).text(data.name);
            array.push(option);
        };
        this.updateAreaMap = function(selectedArea) {
            jQuery("#r-map").html("<img usemap='#map-"+selectedArea+"' src='" + self.options.backendUrl + "../../../../static/maps/images/kartta"+selectedArea+".jpg' border='0'>");
        };
        this.updateAreaSelection = function(selectedArea) {
            self.$areaSelection.val(selectedArea);
        };
        this.updateRHYSelection = function(selectedRHY) {
            self.$rhySelection.val(selectedRHY);
        };
        this.handleAreaSelection = function(callback) {
            var selectedArea = self.$areaSelection.val();

            if (selectedArea) {
                self.updateAreaMap(selectedArea);
                self.updateRHYList(selectedArea, callback);
                self.clearResults();
            }
        };
        this.handleMapClickEvent = function(selectedArea) {
            if (jQuery.inArray(selectedArea, self.supportedAreas) !== -1) {
                self.updateAreaMap(selectedArea);
                self.updateAreaSelection(selectedArea);
                self.updateRHYList(selectedArea);
            } else {
                self.updateRHYSelection(selectedArea);
            }
            self.clearResults();
        }
    };
}).call(this);