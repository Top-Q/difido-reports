/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function appendTestsToBar(tests, element) {

    var success = 0;
    var error = 0;
    var failure = 0;
    var warning = 0;
    var suiteName = null;
	//Since there is a bug in the bar chart that labels over 80 characters are causing the browser to stuck, we are slicing the suite names.
	var maxLengthOfSuiteName = 40;    
	var suites = new Array();
    $(tests).each(function() {
        if (suiteName !== null && suiteName !== this.suiteName) {
            //TODO : Handle same scenario name in different machines            
			suites.push({"name": suiteName.slice(0,maxLengthOfSuiteName), "success": success, "error": error, "failure": failure, "warning": warning});
            success = error = failure = warning = 0;
        }
        suiteName = this.suiteName;
        switch (this.status) {
            case "success":
                success++;
                break;
            case "error":
                error++;
                break;
            case "failure":
                failure++;
                break;
            case "warning":
                warning++;
                break;
        }

    });

    suites = suites.slice(-5);
    //The last scenario
    suites.push({"name": suiteName.slice(0,maxLengthOfSuiteName), "success": success, "error": error, "failure": failure, "warning": warning});
    
    var successPerSuite = new Array();
    var errorPerSuite = new Array();
    var failurePerSuite = new Array();
    var warningPerSuite = new Array();
    var barData = {"labels" : [],"datasets" : [] };
    $(suites).each(function() {
       barData.labels.push(this.name);
       successPerSuite.push(this.success);
       errorPerSuite.push(this.error);
       failurePerSuite.push(this.failure);
       warningPerSuite.push(this.warning);
    });
    var colors = getTestColors();
    barData.datasets.push({"fillColor" : colors.success,"strokeColor" : colors.success , "data":successPerSuite});
    barData.datasets.push({"fillColor" : colors.error,"strokeColor" : colors.error , "data":errorPerSuite});
    barData.datasets.push({"fillColor" : colors.failure,"strokeColor" : colors.failure , "data":failurePerSuite});
    barData.datasets.push({"fillColor" : colors.warning,"strokeColor" : colors.warning , "data":warningPerSuite});
    new Chart(element.getContext("2d")).Bar(barData);
    
}



function sumBarChartController(element) {
    var tests = collectAllTests();
    appendTestsToBar(tests, element);

}
/**

{
                labels: ["default", "default (2)", "default (3)", "myScenario", "another"],
                datasets: [
                    {
                        fillColor: "rgba(136,238,136,0.5)",
                        strokeColor: "rgba(136,238,136,1)",
                        data: [65, 59, 90, 81, 56]
                    },
                    {
                        fillColor: "rgba(255,136,136,0.5)",
                        strokeColor: "rgba(255,136,136,1)",
                        data: [28, 48, 40, 19, 96]
                    },
                    {
                        fillColor: "rgba(70,130,180,0.5)",
                        strokeColor: "rgba(70,130,180,1)",
                        data: [65, 59, 90, 81, 56]
                    },
                    {
                        fillColor: "rgba(255,255,119,0.5)",
                        strokeColor: "rgba(255,255,119,1)",
                        data: [65, 59, 90, 81, 56]
                    }
                ]

            }
            
             * 
 */