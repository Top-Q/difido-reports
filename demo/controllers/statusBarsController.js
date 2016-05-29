function statusBarsController(bars){
    tests = collectAllTests();
    if (tests.length == 0) {
    	return;
    }
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
    	if (percent > 0 && percent <= 2) {
    		percent = 2;
    	}
    	return Math.round(percent) + "%";

    };

    function renderPercentageText(part) {
    	var percent = part / tests.length * 100;
    	if (percent > 0 && percent < 1) {
    		return "<1%";
    	}
   		percent = Math.round(percent);
    	if (percent <= 5){
    		return percent +"%";
    	} else {
    		return percent + "% (" + part + ")";
    	}

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


