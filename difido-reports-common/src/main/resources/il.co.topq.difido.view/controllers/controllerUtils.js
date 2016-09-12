/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function getTestColors(){
    var colors = new Object();
    colors.success = "#81C784";
    colors.error = "#FF8A65";
    colors.failure = "#E57373";
    colors.warning = "#FFD54F";
    return colors;
}

function collectTestsFromScenario(children,tests){
    $(children).each(function(){
       switch (this.type) {
           case "test":
               tests.push(this);
               break;
           case "scenario":
               collectTestsFromScenario(this.children,tests);
               break;
       }
    });
}


function collectAllScenarioProperties() {
    var properties = {};
    $(execution.machines).each(function () {
        for (i = 0; i < this.children.length; i++) {
            for (var key in this.children[i].scenarioProperties) {
                properties[key] = this.children[i].scenarioProperties[key];
            };
        }
    });
    return properties;

}
/**
 * Collects all the tests from the model into array. Each item in the array
 * contains a single test including the scenario and machine name.
 * 
 * @returns {collectAllTests.tests|Array}
 */
function collectAllTests(){
    var tests = new Array();
    $(execution.machines).each(function(){
        var machineName = this.name;
        $(this.children).each(function() {
            var suiteName = this.name;
            var suiteTests = new Array();
            collectTestsFromScenario(this.children,suiteTests);
            $(suiteTests).each(function() {
                this.machineName = machineName;
                this.suiteName = suiteName;
            });
            tests = tests.concat(suiteTests);
        });
        
    });
    return tests;
    
}
