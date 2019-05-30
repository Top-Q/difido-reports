var depth = 0, depthStep = 20;
var levelsStack = [];

Array.prototype.top = function(){
    return this.length > 0 ? this[this.length-1] : undefined;
};

function setFixedProperties(currentTest, element) {
    $(element).find("#name").text(currentTest.name);
    $(element).find("#timestamp").text(currentTest.date + " " + currentTest.timestamp);
    $(element).find("#description").html(currentTest.description);
}

function addPropertiesToTbl(properties, table) {
    for (var key in properties) {
        var tr = $('<tr>');
        tr.append($('<td>').text(key));
        var value = properties[key];
        var lines = value.split(/\\r?\\n/);
        var td = $('<td>');
        lines.forEach(function(line) {
            td.append($('<div>').text(line));
        });
        tr.append(td);
        $(table).append(tr);
    }
}


function setCustomProperties(currentTest,element) {
    addPropertiesToTbl(currentTest.properties, $(element).find("#propTbl > tbody"));
}

function setParameters(currentTest,element) {
    addPropertiesToTbl(currentTest.parameters, $(element).find("#paramTbl > tbody"));
}

function createDetailsTable() {

}

function isPropertyExist(element, property) {
    return (element.hasOwnProperty(property) && element[property] !== null && element[property] !== "");
}

function addStatusAsClass(elementToAppend, elementWithStatus) {
    if (isPropertyExist(elementWithStatus, "status")) {
        elementToAppend.addClass("s_" + elementWithStatus.status +"_text");
    }
}

function setRegularElement($container, element, isHtml) {
    var $div = $("<div>");
    var $timestamp = $("<span>").addClass('timestamp').text(element.time);

    if (isPropertyExist(element, "message")) {
        var $content = $("<span>").addClass('innerToggle').text(element.title);
        indent($content);
        $div.append($timestamp).append($content);

        // add inner div with the message
        var $innerDiv = $("<div>").html(nl2br(element.message));
        $innerDiv.css("margin-left", (depth+depthStep) + "px");
        $div.append($innerDiv);
    }
    else{
        var $content = isHtml ? $("<span>").html(element.title) :  $("<span>").text(element.title);
        indent($content);
        $div.append($timestamp).append($content);
    }

    addStatusAsClass($div, element);
    appendElement($container, $div);
}

function setStartLevelElement($container, element) {
    var $timestamp = $("<span>").addClass('timestamp').text(element.time);
    var $content = $("<span>").addClass('startLevel').addClass("closed").text(element.title);
    var $div = $("<div>").append($timestamp).append($content);
    indent($content);

    addStatusAsClass($div, element);
    appendElement($container, $div);

    // push the div into the level stack
    levelsStack.push($div);

    // increase depth (left margin)
    depth += depthStep;
}

function setStopLevelElement(element) {
    if(!$.isEmptyObject(levelsStack)){
        levelsStack.pop();
    }

    // decrease depth as level closed
    depth -= depthStep;

    //make sure we don't slide left more
    //than needed... Thanks Alik.
    if (depth<0) {
        depth = 0;
    }
}

function setStepElement($container, element) {
    var $timestamp = $("<span>").addClass('timestamp').text(element.time);
    var $content = $("<span>").text(element.title);
    var $div = $("<div>").append($timestamp).append($content);
    indent($content);

    $div.addClass("step");

    addStatusAsClass($div, element);
    appendElement($container, $div);
}

function setImageElement($container, element){
    var $timestamp = $("<span>").addClass('timestamp').text(element.time);
    var $div = $("<div>");

    var $img = $("<img>").attr("src",element.message).addClass("example-image").attr("alt",element.title);
    var $a = $("<a>").attr("href",element.message).attr("data-lightbox","image-1").attr("title",element.title);
    $a.append($img);
    indent($a);
    $div.append($timestamp).append($a);

    addStatusAsClass($div, element);
    appendElement($container, $div);
}

function setLinkElement($container, element) {
    var $timestamp = $("<span>").addClass('timestamp').text(element.time);
    var $div = $("<div>").append($timestamp);
    var $content;

    if (isPropertyExist(element, "message")) {
        $content = $('<a>').text(element.title).attr("href", element.message).attr("target", "_blank");
    }
    else{
        $content = $("<div>").text(element.title);
    }

    indent($content);
    $div.append($content);
    addStatusAsClass($div, element);
    appendElement($container, $div);
}

/**
 * Should $element be appended to the div, or as a sub-item
 * @param $table
 * @param $element
 */
function appendElement($table, $element){
    if(levelsStack.length == 0){
        // stack is empty, append element directly to the main div
        $table.append($element);
    }
    else{
        // append the element to the "top" item in the stack
        levelsStack.top().append($element);
    }
}

function indent($element){
    $element.css("margin-left", depth + "px");
}

function nl2br(str){
    const breakTag = "<br/>";
    return (str + '').replace(/([^>\r\n]?)(\r\n|\n\r|\r|\n)/g, '$1'+ breakTag +'$2');
}

function setReportElements($container, reportElements) {

    $(reportElements).each(function() {
        switch (this.type) {
            case "startLevel":
                setStartLevelElement($container, this);
                break;
            case "stopLevel":
                setStopLevelElement(this);
                break;
            case "lnk":
                setLinkElement($container, this);
                break;
            case "step":
                setStepElement($container, this);
                break;
            case "img":
                setImageElement($container,this);
                break;
            case "html":
                setRegularElement($container, this,true);
                break;
            default:
                setRegularElement($container, this ,false);
                break;
        }
        if(parseInt($(this).attr("levelDepth")) > 1){
            this.hide();
        }
    });

    prepareLevels($container);
}

function testController(element) {
    var currentTest = getTestWithUid(test.uid);
    setFixedProperties(currentTest,element);
    setCustomProperties(currentTest,element);
    setParameters(currentTest,element);
    setReportElements($(element).find("#detailsDiv"), test.reportElements);

}

function prepareLevels($container) {
    // GUI enhancement- find spans with "startLevel" class that have no div siblings (i.e. have no content between 'startLevel' and 'stopLevel'),
    // and replace class with "emptyStartLevel"
    // This way we won't try to click the blue link, and they will always be "expanded"
    $(".startLevel").each(function(i,e){
        if($(e).siblings('div').length == 0){
            $(e).removeClass('startLevel').addClass('emptyStartLevel');
        }
    });

    // register the 'click' on 'startLevel' and 'innerToggle' elements
    $(".startLevel, .innerToggle").click(function(){
        $(this).toggleClass("closed").parent().children('div').toggle('fast');
    });

    // register the 'click' on ExpandAll and CollapseAll
    $("#detailsDivExpandAll").click(function(){
        $(".startLevel, .innerToggle").removeClass('closed').parent().children('div').show('fast');

    });
    $("#detailsDivCollapseAll").click(function(){
        $(".startLevel, .innerToggle").addClass('closed').parent().children('div').hide('fast');
    });

}
