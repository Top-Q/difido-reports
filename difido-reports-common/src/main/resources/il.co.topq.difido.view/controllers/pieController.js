/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


function pieController(element) {
    var success = 0;
    var failure = 0;
    var warning = 0;
    var error = 0;
    $(collectAllTests()).each(function() {
        switch (this.status) {
            case "success":
                success++;
                break;
            case "failure":
                failure++;
                break;
            case "warning":
                warning++;
                break;
            case "error":
                error++;
                break;
            default:
                success++;
        }
    });
    var colors = getTestColors();
    var pieData = new Array();
    pieData.push({"value": success, "color": colors.success});
    pieData.push({"value": failure, "color": colors.failure});
    pieData.push({"value": warning, "color": colors.warning});
    pieData.push({"value": error, "color": colors.error});
    new Chart(element.getContext("2d")).Pie(pieData);
}

