/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function appendTestsToTable(tests,table){
    $(tests).each(function(){
       var tr = $('<tr>');
       tr.append($('<td>').text(this.index));
       tr.append($('<td>').text(this.timestamp));
       tr.append($('<td>').append($('<a>').text(this.name).attr("href","tests/test_"+this.uid+"/test.html")));
       tr.append($('<td>').text(this.suiteName));
       tr.append($('<td>').text(this.machineName));
       tr.append($('<td>').text(this.status).addClass('s_' + this.status + "_back"));
        var durInSec = Math.round(this.duration/1000);
        var durationHour = Math.floor(((durInSec % 31536000) % 86400) / 3600);
        var durationMin = Math.floor((((durInSec % 31536000) % 86400) % 3600) / 60);
        var durationSec = (((durInSec % 31536000) % 86400) % 3600) % 60;

       tr.append($('<td>').text(durationHour + "h" + durationMin + "m" + durationSec + "s"));
       $(table).append(tr);
    });
}

function tableController(element) {
    var tests = collectAllTests();
    appendTestsToTable(tests,element);
}


