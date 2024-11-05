var gridOptions;
var agCenter = { textAlign: 'center' };
var agLeft = { textAlign: 'left' };

var rowData = [];
var filterValue;

$(document).ready(function() {
    $.ajax('/api/reports').done(initGrid);

    // Init buttons and tooltips 
    $('[data-toggle="tooltip"]').tooltip({ trigger: 'hover' });
    $('.to-disable').attr('disabled', true);

    // Create a URLSearchParams object with the current URL search parameters and read the value of the "filter" parameter
    filterValue = new URLSearchParams(window.location.search).get("filter");

    // show hide 'X' button on search input
    $('#searchGroup').hover(
        function() { $(this).find('button').css('display', 'block') },
        function() { $(this).find('button').css('display', 'none') }
    );

});

function initGrid(json) {
    var columnDefs = [];

    $.each(json.columns, (i, col) => {
        switch (col) {
            case "ID":
                columnDefs.push({
                    field: col,
                    cellStyle: agLeft,
                    sort: 'desc',
                    sortIndex: 0,
                    minWidth: 150,
                    pinned: true,
                    comparator: (valueA, valueB, nodeA, nodeB, isDescending) => valueA - valueB,
                    cellRenderer: params => {
                        let id = params.value;
                        let active = "";
                        let locked = "";
                        if (params.data.Active == 'true')
                            active = '<i class="ml-2 fa-solid fa-spinner fa-spin" data-toggle="tooltip" data-placement="top" title="Active"></i>';
                        if (params.data.Locked == 'true')
                            locked = '<i class="ml-2 fa-solid fa-lock" data-toggle="tooltip" data-placement="top" title="Locked"></i>';
                        return id + active + locked;
                    },
                });
                break;
            case "Link":
                columnDefs.push({ field: col, hide: true });
                break;
            case "Description":
                columnDefs.push({
                    field: col,
                    cellRenderer: params => {
                        return `
                            <a target="_blank" href="${params.data.Link}">${params.value}</a>
                            <span class="hover-filter float-right" style="display: none" onclick="hoverFilterClick('${params.value}')">
                                <i class="fa-solid fa-magnifying-glass"></i>
                            </span>`;
                    },
                    cellStyle: agLeft,
                    cellClass: 'cell-with-buttons',
                    editable: true,
                    suppressSizeToFit: true
                });
                break;
            case "Active":
            case "Locked":
                columnDefs.push({ field: col, hide: true });
                break;
            case "Time":
                columnDefs.push({
                    field: col,
                    cellStyle: agCenter,
                    cellRenderer: params => { return params.value.slice(0, -3) }
                });
                break;
            case "Date":
            case "Duration":
                columnDefs.push({ field: col, cellStyle: agCenter });
                break;
            case "User":
            case "Area":
                columnDefs.push({ field: col, cellStyle: agLeft });
                break;
            case "# Machines":
                columnDefs.push({ field: col, hide: true });
                break;
            case "# Successful":
                columnDefs.push({
                    field: col,
                    cellStyle: params => passCellStyle(params.value),
                    headerName: "Pass",
                });
                break;
            case "# Warnings":
                columnDefs.push({
                    field: col,
                    cellStyle: params => warningCellStyle(params.value),
                    headerName: "Warnings",
                });
                break;
            case "# Failed":
                columnDefs.push({
                    field: col,
                    cellStyle: params => failCellStyle(params.value),
                    headerName: "Fail",
                });
                break;
            case "# Tests":
                columnDefs.push({
                    field: col,
                    cellStyle: params => totalCellStyle(),
                    headerName: "Total",
                });
                break;
            default:
                columnDefs.push({ field: col, cellStyle: agCenter });
        }
    });

    const columnHeaders = json.columns;
    const arrayOfArrays = json.data;

    // Convert array of values to row data format
    rowData = arrayOfArrays.map(row => Object.fromEntries(columnHeaders.map((header, index) => [header, row[index]])));

    // Grid Options are properties passed to the grid
    gridOptions = {
        columnDefs: columnDefs,
        rowData: rowData,

        // default col def properties get applied to all columns
        defaultColDef: {
            sortable: true,
            editable: false,
            resizable: true,
            autoHeight: true,
        },
        suppressRowClickSelection: true,
        rowSelection: 'multiple',
        animateRows: true, // have rows animate to new positions when sorted
        pagination: true,
        onRowSelected: onRowSelected,
        onGridReady: function(event) {
            // adjust grid height and width
            autoSizeAllColumns();
            $('#grid').css('height', 'calc(98vh - ' + $('#grid').offset().top + 'px)');

            // apply filter after F5
            if (filterValue) {
                $("#filter-text-box").val(filterValue);
                onFilterTextBoxChanged();
            }

            // clear search when 'ecs' pressed
            $("#filter-text-box").on('keydown', function(event) {
                if (event.keyCode === 27)
                    clearSearchInput();
            });

            // add info about selected rows
            $('.ag-paging-panel').prepend($('<label class="col "id="selected" style="font-weight:500">').text('Selected: 0 row(s)'));

            // load theme
            let theme = localStorage.getItem("theme", name);
            if (theme && theme != null && theme !== 'null')
                changeTheme(theme);
        },

        suppressDragLeaveHidesColumns: true,
        suppressMovableColumns: true,
        paginationPageSize: 50,
        doesExternalFilterPass: doesExternalFilterPass,
        isExternalFilterPresent: isExternalFilterPresent,
        onSelectionChanged: function() {
            let selected = gridOptions.api.getSelectedNodes().length;
            $("#selected").text('Selected: ' + selected + ' row(s)');
        },
        onCellMouseOver: function(event) {
            if (event.column.colId != "ID") {
                gridOptions.suppressRowClickSelection = true;
            } else gridOptions.suppressRowClickSelection = false;

            if (event.column.colId == "Description")
                $(event.eventPath[0]).find('.hover-filter').css('display', 'block');
        },
        onCellMouseOut: function(event) {
            $('.hover-filter').css('display', 'none')
        },
        onCellEditingStopped: function(event) {
            // Edit Description //
            if (event.oldValue != event.newValue) {
                $.confirm({
                    title: 'Update Description',
                    content: 'Are you sure you want to update description?',
                    type: 'orange',
                    typeAnimated: true,
                    buttons: {
                        confirm: function() {
                            var metadataStr = encodeURIComponent("description\\=" + event.newValue + "\\;comment\\=");
                            $.ajax({
                                url: 'api/executions/' + event.data.ID + '?metadata=' + metadataStr,
                                type: 'PUT',
                                contentType: "application/json;charset=utf-8"
                            }).done(function(text) {
                                notifySuccess();
                            }).fail(function() {
                                alert("Error updating execution #" + event.data.ID);
                            });
                        },
                        cancel: function() {
                            event.node.setDataValue(event.column.colId, event.oldValue);
                        }
                    }
                });
            }
        },
    };

    // get div to host the grid
    const gridDiv = document.getElementById("grid");
    // new grid instance, passing in the hosting DIV and Grid Options
    new agGrid.Grid(gridDiv, gridOptions);

    function onRowSelected() {
        if (gridOptions.api.getSelectedRows().length > 0)
            $('.to-disable').removeAttr('disabled');
        else
            $('.to-disable').attr('disabled', true);
    }
}

function onFilterTextBoxChanged() {
    let inputValue = $('#filter-text-box').val();
    gridOptions.api.setQuickFilter(inputValue);

    // Update URL with input value 
    var url = new URL(window.location.href); // Get the current URL
    if (inputValue)
        url.searchParams.set("filter", inputValue);
    else url.searchParams.delete("filter");
    // Update the URL without refreshing the page
    history.pushState(null, "", url.toString());

    adjustColumsWidth();
}

var isActiveOnly = '';
var isLockedOnly = '';
var isFailedOnly = '';

function failedOnly() {
    if ($('#failedOnly').is(":checked")) {
        isFailedOnly = 'true';
        $('#lockedOnly').prop('checked', false).trigger('change');
        $('#activeOnly').prop('checked', false).trigger('change');
        isLockedOnly = '';
        isActiveOnly = '';
    } else isFailedOnly = '';
    externalFilterChanged();
}

function activeOnly() {
    if ($('#activeOnly').is(":checked")) {
        isActiveOnly = 'true';
        $('#lockedOnly').prop('checked', false).trigger('change');
        $('#failedOnly').prop('checked', false).trigger('change');
        isLockedOnly = '';
        isFailedOnly = '';
    } else isActiveOnly = '';
    externalFilterChanged();
}

function lockedOnly() {
    if ($('#lockedOnly').is(":checked")) {
        isLockedOnly = 'true';
        $('#activeOnly').prop('checked', false).trigger('change');
        $('#failedOnly').prop('checked', false).trigger('change');
        isActiveOnly = '';
        isFailedOnly = '';
    } else isLockedOnly = '';
    externalFilterChanged();
}

function externalFilterChanged() {
    gridOptions.api.onFilterChanged();
    adjustColumsWidth();
}

function doesExternalFilterPass(node) {
    if (isActiveOnly !== '')
        return node.data.Active == isActiveOnly
    else if (isLockedOnly !== '')
        return node.data.Locked == isLockedOnly
    else if (isFailedOnly !== '')
        return node.data['# Failed'] > 0
}

function isExternalFilterPresent() {
    return isActiveOnly !== '' || isLockedOnly !== '' || isFailedOnly !== '';
}

function goToOldDifido() {
    var currentURL = window.location.href;
    var indexURL = currentURL.substring(0, currentURL.lastIndexOf("/") + 1) + "index.html";
    window.open(indexURL);
}

function failCellStyle(value) {
    if (parseInt(value) > 0)
        return { color: 'red', textAlign: 'center', fontWeight: 500 };
    else return { textAlign: 'center' }
}

function warningCellStyle(value) {
    if (parseInt(value) > 0) return { color: 'orange', textAlign: 'center', fontWeight: 500 };
    else return { textAlign: 'center' }
}

function passCellStyle(value) {
    if (parseInt(value) > 0) return { color: 'green', textAlign: 'center', fontWeight: 500 };
    else return { textAlign: 'center' }
}

function totalCellStyle(value) {
    return { color: 'darkBlue', textAlign: 'center', fontWeight: 500 };
}

// buttons //

function reload() {
    $.ajax('/api/reports').done((json) => {
        const columnHeaders = json.columns;
        const arrayOfArrays = json.data;
        // Convert array of values to row data format
        rowData = arrayOfArrays.map(row => Object.fromEntries(columnHeaders.map((header, index) => [header, row[index]])));
        gridOptions.api.setRowData(rowData);
        notifySuccess();
    });
}

function lock() {
    lockUnlockSelected("true").then(() => {
        gridOptions.api.applyTransaction({ update: rowData });
        gridOptions.api.redrawRows();
        notifySuccess();

    });
}

function unlock() {
    lockUnlockSelected("false").then(() => {
        gridOptions.api.applyTransaction({ update: rowData });
        gridOptions.api.redrawRows();
        notifySuccess();

    });
}

function remove() {
    $.confirm({
        title: 'Delete Execution',
        content: 'Are you sure you want to delete all selected execution reports?',
        type: 'red',
        typeAnimated: true,
        buttons: {
            confirm: function() {
                deleteRows(gridOptions.api.getSelectedRows()).then((data) => {
                    gridOptions.api.applyTransaction({ remove: data });
                    gridOptions.api.redrawRows();
                    notifySuccess();
                });
            },
            cancel: function() {}
        }
    });
}

function download() {
    $.each(gridOptions.api.getSelectedRows(), (i, row) => {
        downloadFileFromUrl('api/reports/' + row.ID, i);
    });
    notifySuccess();
}

function downloadFileFromUrl(url, index) {
    var hiddenIFrameID = 'hiddenDownloader' + index;
    var iframe = document.createElement('iframe');
    iframe.id = hiddenIFrameID;
    iframe.style.display = 'none';
    document.body.appendChild(iframe);
    iframe.src = url;
}

function lockUnlockSelected(locked) {
    var promises = [];
    $.each(gridOptions.api.getSelectedRows(), (i, row) => {
        var request = $.ajax({
            url: 'api/executions/' + row.ID + '?locked=' + locked,
            type: 'PUT',
        }).done(function() {
            $.grep(rowData, function(obj) {
                return obj.ID === row.ID;
            })[0].Locked = locked;
        });
        promises.push(request);
    });
    return $.when.apply($, promises);
}

function deleteRows(rows) {
    var promises = [];
    var removed = [];
    return new Promise(function(resolve, reject) {
        $.each(rows, function(i, row) {
            var request = $.ajax({
                url: 'api/executions/' + row.ID,
                type: 'DELETE'
            }).done(function() {
                removed.push(row);
            });

            promises.push(request);
        });

        $.when.apply($, promises)
            .done(function() {
                resolve(removed);
            })
            .fail(function() {
                reject(new Error('Failed to delete selected rows.'));
            });
    });
}

function notifySuccess() {
    $.notify({
        message: 'Operation completed successfully!'
    }, {
        type: 'success',
        placement: {
            from: "bottom",
            align: "center"
        },
        delay: 1500,
        z_index: '9999',
        animate: {
            enter: 'animated fadeInDown',
            exit: 'animated fadeOutUp'
        },
        newest_on_top: true,
    });
};

// cleanup //
var cleanupRows = [];
var xDays;

$('#cleanupModal').on('show.bs.modal', function(event) {
    $('#cleanupResults').empty();
    $('#cleanupBtn').attr('disabled', true);
});


$("#xDays").on("keydown", function search(e) {
    if (e.key === 'Enter' || e.keyCode === 13) {
        findOlderThanXDays();
    }
});

function isDateMoreThanXDaysAgo(dateString) {
    var parts = dateString.split('/'); // Split the date string into day, month, and year
    var day = parseInt(parts[0], 10);
    var month = parseInt(parts[1], 10) - 1; // Month is zero-based (0-11)
    var year = parseInt(parts[2], 10);

    var providedDate = new Date(year, month, day);
    var currentDate = new Date();
    currentDate.setDate(currentDate.getDate() - xDays); // Subtract X days from the current date

    return providedDate < currentDate;
}

function findOlderThanXDays() {
    loader().then(() => {
        // collect ids of records more than xDays //
        cleanupRows = [];
        xDays = $('#xDays').val();
        $.each(rowData, (i, row) => {
            if (isDateMoreThanXDaysAgo(row.Date)) {
                cleanupRows.push(row);
            }
        });

        var output = $('#cleanupResults');
        output.empty();

        if (cleanupRows.length > 0) {
            output.append($('<h5>').text(cleanupRows.length + " records found"));
            let table = $('<table class="table mt-3">')
            let thead = $('<thead>');
            let tr = $('<tr>');
            tr.append($('<th>').text('id'));
            tr.append($('<th>').text('date'));
            tr.append($('<th>').text('is locked?'));
            thead.append(tr);
            table.append(thead);

            for (var i = 0; i < cleanupRows.length; i++) {
                let tr = $('<tr>');
                tr.append($('<td>').text(cleanupRows[i].ID));
                tr.append($('<td>').text(cleanupRows[i].Date));
                tr.append($('<td>').text(cleanupRows[i].Locked));
                table.append(tr);
            }
            output.append(table);
            $('#cleanupBtn').removeAttr('disabled');
        } else {
            output.append($('<h5>').text("No records found!"));
            $('#cleanupBtn').attr('disabled', true);
        }
    }).then(() => {
        loader();
    });
}

function cleanup() {
    $.confirm({
        title: 'Delete Execution',
        content: 'Are you sure you want to delete all found execution reports?',
        type: 'red',
        typeAnimated: true,
        buttons: {
            confirm: function() {
                deleteRows(cleanupRows).then((data) => {
                    gridOptions.api.applyTransaction({ remove: data });
                    gridOptions.api.redrawRows();
                    $('#cleanupModal').modal('hide');
                    notifySuccess();
                });
            },
            cancel: function() {}
        }
    });
}

function loader() {
    return new Promise(function(resolve) {
        $('#spinnerContainer').toggle();
        setTimeout(function() {
            resolve();
        }, 200);
    });
}

function onPageSizeChanged() {
    gridOptions.api.paginationSetPageSize(Number($('#page-size').val()));
}

// Attach the event listener
window.addEventListener('resize', handleZoomEvent);
var autoSize = true;

function autoSizeAllColumns() {
    autoSize = true;
    gridOptions.columnApi.autoSizeAllColumns();

    // Adjust description column with free space 
    let freeSpace = $('.ag-header-viewport').width() - $('.ag-header-container').width();
    if (freeSpace > 0) {
        let actualWidth = gridOptions.columnApi.getColumn("Description").actualWidth;
        gridOptions.columnApi.setColumnWidth('Description', actualWidth + freeSpace);
    }
}

function sizeColumnsToFit() {
    autoSize = false;
    gridOptions.api.sizeColumnsToFit();
}

function handleZoomEvent() {
    adjustColumsWidth();
}

function adjustColumsWidth() {
    if (autoSize)
        autoSizeAllColumns();
    else sizeColumnsToFit();
}

function hoverFilterClick(value) {
    $('#filter-text-box').val(value);
    onFilterTextBoxChanged();
}

function clearSearchInput() {
    $("#filter-text-box").val('');
    onFilterTextBoxChanged();
    var url = new URL(window.location.href); // Get the current URL
    url.searchParams.delete("filter");
    history.pushState(null, "", url.toString());
}

function changeTheme(name) {
    $('#grid').removeClass();
    $('#grid').addClass(name + ' mt-2');
    localStorage.setItem("theme", name);

    $(".theme-item").each((i, item) => {
        if ($(item).attr('data-theme') === name)
            $(item).addClass('theme-check').removeClass('theme-uncheck');
        else
            $(item).removeClass('theme-check').addClass('theme-uncheck');
    });

    adjustColumsWidth();
}