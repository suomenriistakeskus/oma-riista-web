function DiaryShowSidebar() {
    'use strict';

    this.sidebarElement = element(by.css('.off-canvas-panels.open'));
    this.speciesNameElement = this.sidebarElement.element(by.css('.r-gamediary-species-name'));

    this.getSpeciesName = function () {
        return this.speciesNameElement.getText();
    };
    
    this.getText = function () {
        return this.sidebarElement.getText();
    }
}

module.exports = DiaryShowSidebar;
