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
       tr.append($('<td>').append($('<a>').text(this.name).attr("href","tests/test_"+this.index+"/test.html")));
       tr.append($('<td>').text(this.suiteName));
       tr.append($('<td>').text(this.machineName));
       tr.append($('<td>').text(this.status).addClass(this.status));
       tr.append($('<td>').text(this.duration));
       $(table).append(tr);
    });
}

function tableController(element) {
    var tests = collectAllTests();
    appendTestsToTable(tests,element);
}


