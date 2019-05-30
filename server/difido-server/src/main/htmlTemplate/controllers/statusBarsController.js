function statusBarsController(bars){
    tests = collectAllTests();
    if (tests.length == 0) {
    	return;
    }
    var totalExecuted = tests.length;
    var success = 0;
    var failure = 0;
    var warning = 0;
    $(tests).each(function() {
        switch (this.status) {
            case "success":
                success++;
                break;
            case "error":
                failure++;
                break;
            case "failure":
                failure++;
                break;
            case "warning":
                warning++;
                break;
        }

        
    });

    function calculatePercent(part) {
    	var percent = part / tests.length * 100;
    	return percent + "%";
    };

    function renderPercentageText(part) {
    	var percent = part / tests.length * 100;
    	if (percent > 0 && percent < 1) {
    		return ".";
    	}
   		percent = Math.round(percent);
        if (percent <= 2) {
            return percent;
        }
        else if (percent <= 5){
            return percent +"%";
        } else {
            return percent + "% (" + part + ")";
        }
    }

    function getAllPlannedTests(){
        var total = 0;
        try{
            $(execution.machines).each(function() {
                total+= this.plannedTests;
            });
            
            return total;
        }
        catch(err){
            return totalExecuted;
        }
    }

    var totalPlanned = getAllPlannedTests();    
    
    if (totalPlanned != 0 && totalExecuted <= totalPlanned) {
        $(".totalExecuted").animate({
            width: (totalExecuted / totalPlanned) * 100 + "%"
        },100).text(totalExecuted + " of " + totalPlanned);
        
    } else {
    // There is a problem evaluating the number of planned tests
        $(".totalExecuted").text(totalExecuted);
    }
    
    $(".success").animate({
        width: calculatePercent(success)
    },100).text(renderPercentageText(success));
    $(".failure").animate({
        width: calculatePercent(failure)
    }, 100).text(renderPercentageText(failure));
    $(".warning").animate({
        width: calculatePercent(warning)
    }, 100).text(renderPercentageText(warning));
}



