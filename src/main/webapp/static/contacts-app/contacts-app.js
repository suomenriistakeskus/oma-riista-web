/*
 * App that searches Suomen riistakeskus contact information from oma.riista.fi portal.
 */
(function() {
    window.ContactsApp = function(options) {
        var self = this;
        this.supportedAreas = ["850", "000", "050", "100", "150", "200", "250", "300", "350", "400", "450", "500", "550", "600", "650", "700"];
        this.options = options;
        this.$areaSelection = jQuery(options.areaSelector);
        this.$rhySelection = jQuery(options.rhySelector);
        this.$occupationSelection = jQuery(options.occupationSelector);
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
                event.preventDefault();
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
            self.updateRHYList();
            self.updateOccupationList(self.setOrgStateFromUrl);
            self.updateAreaList(self.setAreaStateFromUrl);

            window.setTimeout(function() {
                self.installEventHandlers();
            }, 250);

            window.setTimeout(function() {
                if (self.getParameterFromFragment("onlyresults") === "true") {
                    self.$searchForm.hide();
                    self.$mapContainer.hide();
                }

                self.doContactInformationSearchFromUrlParams();
            }, 500);
        };
        this.getParameterFromFragment = function(name) {
            name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
            var regex = new RegExp("[\\?&#]" + name + "=([^&#]*)");
            var results = regex.exec(location.hash);
            return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
        };

        this.setOrgStateFromUrl = function() {
            if (self.getParameterFromFragment("org") && self.getParameterFromFragment("type")) {
                self.$occupationSelection.val(self.getParameterFromFragment("org") + ":" + self.getParameterFromFragment("type"));
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
        this.updateStateToUrl = function() {
            var params = {};
            var occupation = self.$occupationSelection.val();
            if (self.$areaSelection.val()) params["area"] = self.$areaSelection.val();
            if (self.$rhySelection.val()) params["rhy"] = self.$rhySelection.val();
            if (occupation && occupation.indexOf(":") !== -1) {
                params["org"] = occupation.substring(0, occupation.indexOf(":"));
                params["type"] = occupation.substring(occupation.indexOf(":") + 1);
            }
            var hash = "#";
            for (var key in params) {
                if (hash.length > 1) hash += "&";
                hash += key + "=" + params[key];
            }
            location.hash = hash;
        };
        this.doContactInformationSearchFromUrlParams = function(event) {
            var areaId = self.getParameterFromFragment("area");
            var rhyId = self.getParameterFromFragment("rhy");
            var organisation = self.getParameterFromFragment("org");
            var occupation = self.getParameterFromFragment("type");
            if (!areaId && !rhyId && !organisation && !occupation) return;
            self.searchContactInformation(areaId, rhyId, organisation, occupation);
        };
        this.handleContactInformationSearch = function() {
            self.updateStateToUrl();
            var areaId = self.$areaSelection.val();
            var rhyId = self.$rhySelection.val();
            var occupation = self.$occupationSelection.val();
            var occupationType = null;
            var organisationType = null;
            if (occupation) {
                organisationType = occupation.substring(0, occupation.indexOf(":"));
                occupationType = occupation.substring(occupation.indexOf(":") + 1);
            }
            self.searchContactInformation(areaId, rhyId, organisationType, occupationType);
        };
        var eachObjectKeySorted = function (obj, func) {
            var keys = [];
            for (var key in obj) {
                keys.push(key);
            }
            keys.sort(function (a, b) {
                return a.localeCompare(b);
            });
            for(var i = 0; i < keys.length; i++){
                func(keys[i]);
            }
        };
        this.searchContactInformation = function(areaId, rhyId, organisationType, occupationType) {
            var requestData = {
                areaId: areaId,
                rhyId: rhyId,
                organisationType: organisationType,
                occupationType: occupationType
            };

            self.getJSONP("/tehtavat.jsonp", requestData, function(data) {
                var occupationData = {};
                var organisationData = {};
                var allOccupations = data.occupations;
                var organisations = data.organisations;

                if (allOccupations.length > self.options.searchLimit) {
                    self.$searchResults.html(self.options.translations.tooManyResults);
                    return;
                }
                if (allOccupations.length === 0) {
                    self.$searchResults.html(self.options.translations.noResults);
                    return;
                }

                for (var i = 0; i < allOccupations.length; i++) {
                    var orgId = allOccupations[i].orgId;
                    if (!occupationData[orgId]) {
                        occupationData[orgId] = [];
                    }
                    occupationData[orgId].push(allOccupations[i]);
                }

                for (var i = 0; i < organisations.length; i++) {
                    var orgName = organisations[i].name;
                    organisationData[orgName] = organisations[i];
                }

                var tableRowDataString = "";
                var headerColor = self.options.headerColor;
                var oddColor = self.options.oddColor;
                var evenColor = self.options.evenColor;
                eachObjectKeySorted(organisationData, function (key) {
                    tableRowDataString += "<tr style='background-color: " + headerColor + ";'><td style='vertical-align: top;'><h4>" + key + "</h4></td><td/>";
                    tableRowDataString += "<td>";

                    var organisation = organisationData[key];

                    if (organisation.address) {
                        var address = organisation.address;
                        if (address.streetAddress) {
                        	tableRowDataString += address.streetAddress + ", ";
                        }
                        if (address.postalCode) {
                        	tableRowDataString += address.postalCode + " ";
                        }
                        if (address.city) {
                        	tableRowDataString += address.city;
                        }
                        tableRowDataString += "<br/>"
                    }
                    if (organisation.email) {
                        tableRowDataString += organisation.email + "<br/>"
                    }
                    if (organisation.phoneNumber) {
                        tableRowDataString += organisation.phoneNumber + "<br/>"
                    }
                    if (organisation.rhyNumberString) {
                    	tableRowDataString += organisation.rhyNumberString + "<br/>";
                    }
                	tableRowDataString += "</td></tr>";

                    var occupations = occupationData[organisation.id];
                    for (var i = 0; i < occupations.length; i++) {
                        var bgColor = i % 2 == 0 ? evenColor : oddColor;
                        tableRowDataString += "<tr style='background-color: " + bgColor + ";'>";
                        tableRowDataString = self.addTableCell(occupations[i].occupationType.name, tableRowDataString);
                        tableRowDataString = self.addTableCell(occupations[i].personName, tableRowDataString);
                        tableRowDataString = self.addContactInfoTableCell(occupations[i], tableRowDataString);
                        tableRowDataString += "</tr>";
                    }
                });
                self.$searchResults.html("<table style='border-spacing: 0; width: 100%;'>" + tableRowDataString + "</table>");
            });
        };
        this.addContactInfoTableCell = function(data, tableString) {
            tableString += "<td>";
            if (data.additionalInfo) tableString += data.additionalInfo + "<br/>";
            if (data.streetAddress) tableString += data.streetAddress + "<br/>";
            if (data.postalCode) tableString += data.postalCode + " ";
            if (data.city) tableString += data.city + "<br/>";
            if (data.email) tableString += "<a href='mailto:" + data.email + "'>" + data.email + "</a><br/>";
            if (data.phoneNumber) tableString += data.phoneNumber + "<br/>";
            tableString += "</td>";
            return tableString;
        };
        this.addTableCell = function(data, tableString) {
            tableString += "<td style='vertical-align: top;'>";
            if (data) tableString += data;
            tableString += "</td>";
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
                self.$areaSelection.html("");
                self.$areaSelection.append(options);
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
                    options.unshift("<option value='' selected=''>("+self.options.translations.all+")</option>");
                    self.$rhySelection.html("");
                    self.$rhySelection.append(options);
                    if (callback && typeof callback === "function")
                        callback();
                });
            } else {
                self.$rhySelection.html("<option value='' selected=''>("+self.options.translations.all+")</option>");
            }
        };
        this.updateOccupationList = function(callback) {
            self.getJSONP("/tehtavatyypit.jsonp", {}, function(data) {
                var options = ["<option value='' selected=''>("+self.options.translations.all+")</option>"];
                for (var i = 0; i < data.length; i++) {
                    self.addOccupationsAsOptionsToArray(data[i], options);
                }
                self.$occupationSelection.html("");
                self.$occupationSelection.append(options);
                if (callback && typeof callback === "function")
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
        this.addOccupationsAsOptionsToArray = function(data, array) {
            var option = jQuery('<option/>');
            option.attr({ 'value': data.organisationType + ":" + data.occupationType }).text(data.name);
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