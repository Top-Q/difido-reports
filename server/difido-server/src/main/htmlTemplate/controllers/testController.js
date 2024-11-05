var depth = 0,
    depthStep = 20;
var levelsStack = [];

Array.prototype.top = function() {
    return this.length > 0 ? this[this.length - 1] : undefined;
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


function setCustomProperties(currentTest, element) {
    addPropertiesToTbl(currentTest.properties, $(element).find("#propTbl > tbody"));
}

function setParameters(currentTest, element) {
    addPropertiesToTbl(currentTest.parameters, $(element).find("#paramTbl > tbody"));
}

function createDetailsTable() {

}

function isPropertyExist(element, property) {
    return (element.hasOwnProperty(property) && element[property] !== null && element[property] !== "");
}

function addStatusAsClass(elementToAppend, elementWithStatus) {
    if (isPropertyExist(elementWithStatus, "status")) {
        elementToAppend.addClass("s_" + elementWithStatus.status + "_text");
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
        $innerDiv.css("margin-left", (depth + depthStep) + "px");
        $div.append($innerDiv);
    } else {
        var $content = isHtml ? $("<span>").html(element.title) : $("<span>").text(element.title);
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
    if (!$.isEmptyObject(levelsStack)) {
        levelsStack.pop();
    }

    // decrease depth as level closed
    depth -= depthStep;

    //make sure we don't slide left more
    //than needed... Thanks Alik.
    if (depth < 0) {
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

function setImageElement($container, element) {
    var $timestamp = $("<span>").addClass('timestamp').text(element.time);
    var $div = $("<div>");

    var $img = $("<img>").attr("src", element.message).addClass("example-image").attr("alt", element.title);
    var $a = $("<a>").attr("href", element.message).attr("data-lightbox", "image-1").attr("title", element.title);
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
    } else {
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
function appendElement($table, $element) {
    if (levelsStack.length == 0) {
        // stack is empty, append element directly to the main div
        $table.append($element);
    } else {
        // append the element to the "top" item in the stack
        levelsStack.top().append($element);
    }
}

function indent($element) {
    $element.css("margin-left", depth + "px");
}

function nl2br(str) {
    const breakTag = "<br/>";
    return (str + '').replace(/([^>\r\n]?)(\r\n|\n\r|\r|\n)/g, '$1' + breakTag + '$2');
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
                setImageElement($container, this);
                break;
            case "html":
                setRegularElement($container, this, true);
                break;
            default:
                setRegularElement($container, this, false);
                break;
        }
        if (parseInt($(this).attr("levelDepth")) > 1) {
            this.hide();
        }
    });

    prepareLevels($container);
}

function testController(element) {
    var currentTest = getTestWithUid(test.uid);
    setFixedProperties(currentTest, element);
    setCustomProperties(currentTest, element);
    setParameters(currentTest, element);
    setReportElements($(element).find("#detailsDiv"), test.reportElements);
}

function prepareLevels($container) {
    // GUI enhancement- find spans with "startLevel" class that have no div siblings (i.e. have no content between 'startLevel' and 'stopLevel'),
    // and replace class with "emptyStartLevel"
    // This way we won't try to click the blue link, and they will always be "expanded"
    $(".startLevel").each(function(i, e) {
        if ($(e).siblings('div').length == 0) {
            $(e).removeClass('startLevel').addClass('emptyStartLevel');
        }
    });

    // register the 'click' on 'startLevel' and 'innerToggle' elements
    $(".startLevel, .innerToggle").click(function() {
        $(this).toggleClass("closed").parent().children('div').toggle('fast');
    });

    // register the 'click' on ExpandAll and CollapseAll
    $("#detailsDivExpandAll").click(function() {
        $(".startLevel, .innerToggle").removeClass('closed').parent().children('div').show('fast');
    });

    $("#detailsDivCollapseAll").click(function() {
        $(".startLevel, .innerToggle").addClass('closed').parent().children('div').hide('fast');
    });
}

// general //

function toggleProp() {
    $('#propTbl').toggle('fast');
}

function toggleParam() {
    $('#paramTbl').toggle('fast');
}

function populateTestDetails() {
    testController($("#container"));
}

function hideUneededHeaders() {
    if ($("#paramTbl tbody").children().length === 0) {
        $("#paramTblH").hide();
    }
    if ($("#propTbl tbody").children().length === 0) {
        $("#propTblH").hide();
    }
}

// -------- search ---------- //

var totalFound = 0;
var currentFound = 0;
var lazySearch = true;
var searchVal;

$("#searchInput").on("keydown", function search(e) {
    if (e.key === 'Enter' || e.keyCode === 13) {
        let val = $("#searchInput").val();

        if (!val) {
            loadClearInput();
        } else if (val === searchVal)
            searchDown();
        else {
            loader().then(() => {
                clearInput();
                searchVal = val;
                searchInput();
            }).then(() => {
                loader();
            });
        }
    }
});

function loadClearInput() {
    loader().then(() => {
        clearInput();
    }).then(() => {
        loader();
    });
}

function searchUp() {
    if (totalFound != 0) {
        if (currentFound == 1)
            currentFound = totalFound;
        else
            currentFound--;
        findNext();
    }
}

function searchDown() {
    if (totalFound != 0) {
        if (currentFound == totalFound)
            currentFound = 1;
        else
            currentFound++;
        findNext();
    }
}

function findNext() {
    $('.was-found-current').removeClass('was-found-current');
    let x = $(".was-found-text").get(currentFound - 1);
    $(x).addClass("was-found-current");

    $('#searchInfo').text(currentFound + "/" + totalFound)
    openSearchResult(x);
    scroll();
}


function hasChildNodesExceptBR(element) {
    // Get all child nodes of the element
    const childNodes = element.childNodes;

    // Loop through each child node
    for (let i = 0; i < childNodes.length; i++) {
        const node = childNodes[i];

        // Check if the node is an element node and not a <br> tag
        if (node.nodeType === Node.ELEMENT_NODE && node.tagName !== 'BR') {
            return true; // Found a child node that is not <br>, return true
        }
    }
    return false; // No child nodes found, or only <br> tags found
}

function loader() {
    return new Promise(function(resolve) {
        $('#spinnerContainer').toggle();
        setTimeout(function() {
            resolve();
        }, 200);
    });
}

function searchInput() {

    currentFound = 0;
    if (searchVal.length == 0)
        return
    $('#searchInput').val(searchVal);
    lazySearch = $('#lazySearch').prop('checked');


    // create a regular expression to match the text
    var regex = new RegExp(searchVal, "ig");
    $('#detailsDiv *').filter(function() {
        return $(this).html().toLowerCase().includes(searchVal.toLowerCase()) && (!hasChildNodesExceptBR(this) || $(this).children().hasClass('jira'));
    }).each(function(i, element) {

        // check if the element has Jira child and handle search inside Jira span --> relevant for badges only //
        let jiraSpan;
        let jiraSpanHtml;
        let jiraSpanText;

        if ($(element).children().hasClass('jira')) {
            jiraSpan = $(element).children()[0];
            jiraSpanHtml = jiraSpan.outerHTML;
            jiraSpanText = jiraSpan.innerHTML;

            $(element).children().remove();
            if (jiraSpanText.match(regex)) {
                let originalValues = jiraSpanText.match(regex).filter(function(value, index, self) {
                    return self.indexOf(value) === index;
                });

                $.each(originalValues, (i, value) => {
                    jiraSpanText = jiraSpanText.replaceAll(value, "|~|" + value + "|^|");
                });
                jiraSpanText = jiraSpanText.replaceAll("|~|", "<span class='was-found-text'>");
                jiraSpanText = jiraSpanText.replaceAll("|^|", "</span>");
                $(jiraSpan).html(jiraSpanText);
            }
        }

        // common search  //
        if ($(element).html()) {
            let originalValues2 = $(element).html().match(regex).filter(function(value, index, self) {
                return self.indexOf(value) === index;
            });

            let text = $(element).text();
            $.each(originalValues2, (i, value) => {
                text = text.replaceAll(value, "|~|" + value + "|^|");
            });
            text = text.replaceAll("|~|", "<span class='was-found-text'>");
            text = text.replaceAll("|^|", "</span>");
            text = text.replaceAll("\n", "<br>\n");
            $(element).html(text);
        }

        if (jiraSpan)
            $(element).append($(jiraSpan));
    });

    let $wasFoundText = $(".was-found-text");
    totalFound = $wasFoundText.length;


    if (totalFound > 0) {
        // set current found to first found //
        currentFound = 1;
        $($wasFoundText.get(0)).addClass("was-found-current");

        // enable navigation
        $('#searchUp').removeAttr('disabled');
        $('#searchDown').removeAttr('disabled');

        $.each($wasFoundText, (i, span) => {
            // open report levels //
            if (lazySearch && i == 0)
                openSearchResult(span); // open first only
            else if (!lazySearch)
                openSearchResult(span); // open all       
        });

        scroll();
    }

    $('#searchInfo').text(currentFound + "/" + totalFound);
}


let scrollDelay = 500;
let scrollOffset = 300;

function scroll() {
    setTimeout(function() {
        let scrollTo = $('.was-found-current').offset().top - scrollOffset;
        $('html, body').animate({ scrollTop: scrollTo }, 300);
    }, scrollDelay);
}

function openSearchResult(span) {
    scrollDelay = 0;
    let parents = $(span).parents('div.s_success_text, div.s_warning_text, div.s_failure_text'); // all levels for span
    $.each(parents, (j, parent) => {
        let startLevel = $(parent).find('.startLevel, .innerToggle').first();
        if (startLevel.hasClass('closed')) {
            startLevel.click();
            scrollDelay = 500; // if new level found - add delay to scroll
        }
    });
}

function clearInput() {
    backToOriginalHtmlValues()
    searchVal = '';
    $('#searchInput').val('');
    $('#detailsDivCollapseAll').click();

    $('#searchInfo').text('0/0')
    $('#searchUp').attr('disabled', true);
    $('#searchDown').attr('disabled', true);
    $('html, body').animate({ scrollTop: 0 }, 300);
}

function badgeSearchListener() {
    $(".qs").click(function() {
        let val = $(this).text();
        if (searchVal === val)
            searchDown();
        else {
            searchVal = '';
            $('#searchInput').val('');
            $('#searchInput').val(val).trigger($.Event("keydown", { keyCode: 13 }));
        }
    });
}

function backToOriginalHtmlValues() {
    $.each($('.was-found-text'), (i, element) => {
        let val = $(element).text();
        $(element).replaceWith(val);
    });
}

// Badges // 
var badges = [
    "badge-dark",
    "badge-secondary",
    "badge-primary",
    "badge-info",
    "badge-danger",
    "badge-warning",
];


function handleBadges() {
    $.each(badges, (i, badge) => {
        $.each($('#detailsDiv span:contains("' + badge + '")'), (i, span) => {
            // parse text //
            let data = $(span).text().match(new RegExp(badge + '<text>(.*)<text>'));
            let badgePlaceholder = data[0];
            let badgeText = data[1];

            // remove placeholder from reporter //
            $(span).html($(span).text().replace(badgePlaceholder, ""));
            // add badge to reporter //
            if (badge === 'badge-primary')
                $(span).after('<span class="badge ' + badge + ' ml-3 pb-1">' + '<span class="jira" onclick="openJira(\'' + badgeText + '\')">' + badgeText + '</span></span>');
            else
                $(span).after('<span class="badge ' + badge + ' ml-3 pb-1">' + badgeText + '</span>');
        });
    });
}

function openJira(ticket) {
    let url = "https://jira.com/browse/" + ticket; // TODO -> get base link as parameter from the server ??
    window.open(url, '_blank').focus();
}

function toggleBadges() {
    let offset = $("#collapseBadge").is(":visible") ? -20 : 20;
    let currentMarginTop = parseInt($("#testDetailsDiv").css("margin-top"));
    let newMarginTop = currentMarginTop + offset + "px";

    $("#testDetailsDiv").css("margin-top", newMarginTop);
    $("#collapseBadge").toggle();
}

function badgesSummary() {
    let groups = {}; // k - badge class, v - eleements
    $.each($('#detailsDiv .badge'), (i, el) => {
        let type = $(el).attr('class').match(/badge-([a-z]+)/)[0];
        if (groups.hasOwnProperty(type)) {
            groups[type].push(el);
        } else {
            groups[type] = [el];
        }
    });


    $.each(groups, (type, badges) => {
        let $tabBody = $('#tagsModalContent #' + type + ' div');
        $tabBody.empty();

        // add total occurrences to title //
        let title = $('#tabsHeader .' + type).text().replace('(0)', '(' + badges.length + ')');
        $('#tabsHeader .' + type).text(title);

        let $table = $('<table class="table table-sm table-bordered table-hover">');
        let $thead = $('<thead>');
        let $tr = $('<tr>');
        $tr.append($('<th>').html('Time'))
        $tr.append($('<th>').html('Text'))
        $tr.append($('<th>').html('Badge'))
        $tr.append($('<th>').html('Type'))
        $tr.append($('<th>').html('Status'))
        $tr.append($('<th>').html('Duration'));

        $thead.append($tr);
        $table.append($thead);

        let $tbody = $('<tbody>');
        let totalTime = 0;

        $.each(badges, (i, badge) => {
            let $badge = $(badge);
            let text = $badge.prev().text();
            let isLevel = $badge.prev().hasClass('startLevel');

            // read status //
            let status = isLevel ? "PASS" : "N/A";
            if ($badge.parent().hasClass('s_failure_text'))
                status = "FAIL";
            else if ($badge.parent().hasClass('s_warning_text'))
                status = "WARNING";
            let spanStatus = $('<span>').addClass('ml-3 tag-' + status).html(status);

            // clone badge //
            let id = type + "_" + i;
            let badgeClone = $badge.attr('id', id).clone(); // assign ID for search
            badgeClone.addClass("mb-1").attr('onclick', 'goToBadge("' + id + '")').css('cursor', 'pointer');
            badgeClone.find('span').removeClass('was-found-current was-found-text jira').removeAttr('onclick'); // remove jira link and search results from the clone

            // read time //
            let time = $badge.prevAll('.timestamp').text()
            if (time.endsWith(":"))
                time = time.slice(0, -1);
            let duration = "N/A";
            if (isLevel) {
                let time2;
                $.each($badge.parents(), (i, parent) => {
                    if ($(parent).next().length > 0) {
                        time2 = $(parent).next().find('.timestamp').first().text();
                        return false; // exit from loop
                    }
                });

                if (time2.endsWith(":"))
                    time2 = time2.slice(0, -1);
                duration = calcDuration(time, time2);
            }

            // add row //
            let $tr = $('<tr>');
            $tr.append($('<td>').html(time)); // time
            $tr.append($('<td class="text-left">').html(text)); // text
            $tr.append($('<td>').html(badgeClone)); // badge
            $tr.append($('<td>').html(isLevel ? "level" : "report")); //type
            $tr.append($('<td>').html(spanStatus)); //status
            $tr.append($('<td>').html(duration)); // duration
            $tbody.append($tr);

            // inc total time
            let x = parseInt(duration);
            if ($.isNumeric(x))
                totalTime += x;
        });

        // add body and table //
        $table.append($tbody);
        $tabBody.append($('<h6 class="mb-2">').text("Total spend time: " + secondsToTime(totalTime)));
        $tabBody.append($table);

    });
}

function goToBadge(id) {
    $('#tagsModal').modal('hide');
    var targetElement = $('#' + id).parent();
    openSearchResult(targetElement)
    let scrollTo = targetElement.offset().top - scrollOffset;

    $('html, body').animate({ scrollTop: scrollTo }, 300);
    targetElement.addClass('blink-animation');
    setTimeout(function() {
        targetElement.removeClass('blink-animation');
    }, 3000);
}

function calcDuration(time1, time2) {
    // Split the time strings into hours, minutes, and seconds
    var time1Parts = time1.split(":");
    var time2Parts = time2.split(":");

    // Create Date objects using the current date and the time values
    var date1 = new Date();
    date1.setHours(time1Parts[0], time1Parts[1], time1Parts[2], 0);
    var date2 = new Date();
    date2.setHours(time2Parts[0], time2Parts[1], time2Parts[2], 0);

    // If time2 is smaller than time1, it means time2 is of the next day
    // Adjust date2 by adding 1 day in milliseconds
    if (date2 < date1)
        date2.setDate(date2.getDate() + 1);

    // Calculate the time difference in milliseconds
    var diffMs = date2 - date1;

    // Convert milliseconds to seconds
    var diffSec = Math.floor(diffMs / 1000);
    return diffSec + " sec.";
}

function secondsToTime(seconds) {
    var hours = Math.floor(seconds / 3600); // Calculate the hours
    var minutes = Math.floor((seconds % 3600) / 60); // Calculate the minutes
    var remainingSeconds = seconds % 60; // Calculate the remaining seconds
    // Format the time string
    return ("0" + hours).slice(-2) + ":" + ("0" + minutes).slice(-2) + ":" + ("0" + remainingSeconds).slice(-2);
}