var executionId;

$(document).ready(function () {
	populateNavBar();
	$.ajax('/api/reports').done(createTable);
	
	$("#execution_edit_form").submit(function(){
		
		if(confirm('Update Execution #' + executionId + ' description / comment?')) {
			
			var newDesc = $('#execution_edit_form [name="executionDescription"]').val();
			var newComment = $('#execution_edit_form [name="executionComment"]').val();
			var metadataStr = encodeURIComponent("description\\=" + newDesc + "\\;comment\\=" + newComment);
			
			$.ajax({
						url : 'api/executions/' + executionId + '?metadata=' + metadataStr,
						type : 'PUT',		
						contentType: "application/json;charset=utf-8"
					})
					.done(function(text) {
						//alert(text);
						location.reload();
					})
					.fail(function() {
						alert("Error updating execution #" + executionId);
						location.reload();							
					});
			
			return false;
		}
			
		else {
			return false;
		}
	});
});

function createTable(json) {
	var trHead = $("#etable thead tr");
	json.columns.forEach(function (column) {
		trHead.append($('<th>').text(column));
	}, this);
	
	populateTable();
}

function populateTable() {
	// Array for keeping the ids of the selected rows. Will be read after refresh
	var selectedIds = Array();
	
	var table = $('#etable')
		.on('preXhr.dt', function (e, settings, data) {
			// This happens before the Ajax call and it is 
			// used for keeping the ids of the selected rows in array for resoring them later on
			selectedIds = Array();
			var selectedRows = $("tr[role='row'].selected");
			for (var i = 0; i < selectedRows.length; i++) {
				selectedIds.push(selectedRows[i].getAttribute('exeid'));
			}
		})
		.DataTable(
		{
			ajax: '/api/reports',
			dom: 'lBfrtip',
			buttons: [{
				text: 'Reload',
				action: function () {
					table.ajax.reload(null, false);
				},
				text: '<i class="fas fa-sync-alt"></i>',
				titleAttr: 'Reload'
			},
			{
				text: 'Lock',
				action: function (e, dt, node, config) {
					lockUnlockSelected(table, "true");
				},
				text: '<i class="fa fa-lock"></i>',
				titleAttr: 'Lock Execution'

			}, 
			{
				text: 'Unlock',
				action: function (e, dt, node, config) {
					lockUnlockSelected(table, "false");
				},
				text: '<i class="fa fa-unlock"></i>',
				titleAttr: 'Unlock Execution'

			},
			 {
				text: 'Delete',
				action: function (e, dt, node, config) {
					deleteSelected(table);
				},
				text: '<i class="fa fa-times"></i>',
				titleAttr: 'Delete Execution'

			}, {
				text: 'Execute Plugin',
				action: function (e, dt, node, config) {
					executePlugin(table);
				},
				text: '<i class="fa fa-plug"></i>',
				titleAttr: 'Execute Plugin'
			},
			{
				extend: 'copyHtml5',
				text: '<i class="far fa-copy"></i>',
				titleAttr: 'Copy to Clipboard'
			},
			{
				extend: 'excelHtml5',
				text: '<i class="fa fa-file-excel-o"></i>',
				titleAttr: 'Export to Excel'
			},
			{
				extend: 'csvHtml5',
				text: '<i class="fas fa-file-alt"></i>',
				titleAttr: 'Export to CSV'
			},
			{
				extend: 'pdfHtml5',
				text: '<i class="far fa-file-pdf"></i>',
				titleAttr: 'Export to PDF'
			},
			{
				text: 'zipHtml5',
				action: function (e, dt, node, config) {
					downloadSelectedAsZipped(table);
				},
				text: '<i class="far fa-file-archive"></i>',
				titleAttr: 'Download zipped execution'

			}, 
			{
				action: function (e, dt, node, config) {
					var data = table.rows('.selected').data();
					if(data.length == 0) {
						bootbox.alert("There are no selected rows. Please select one row and try again. ");
						return;
					}
					var id = data[0][0];
					executionDetails(id);
				},
				text: '<i class="fas fa-edit"></i>',
				titleAttr: 'Show / Edit Description & Comment'
			}
		],
			aaSorting: [],
			order: [[ 0, "desc" ]],
			deferRender: true,
			sPaginationType: "full_numbers",
			iDisplayLength: 25,
			select: true,
			columnDefs: [
				{
					// Alows missing values. Important for execution properties in which not all of the execution must have all the properties.
					targets: "_all",
					sDefaultContent: ""
				},
				{
					// The description column will be rendered as link using the value in the link column
					targets: 1,
					// We don't want that a click on the link will make the row selectable
					className: "unselectable",
					data: null,
					"render": function (data, type, row) { 
						return '<a target="_blank" href="' + row[2] + '">'+ data[1] + '</a>';
					}
				},
				{
					// The link column is no longer needed since we are using the value in the description column
					targets: 2,
					visible: false
				}],
			"fnCreatedRow": function (nRow, aData, iDataIndex) {
				// Adding attributes to the row. Especially useful for keeping the selected rows after refresh.
				$(nRow).attr('id', 'exe' + aData[0]);
				$(nRow).attr('exeid', aData[0]);
			},
			"drawCallback": function (settings) {
				// After drawing the table, reselecting all the previously selected rows.
				selectedIds.forEach(function (id) {
					$('#exe' + id).toggleClass('selected');
				});
			}
		});

	// Adding the buttons to the buttons container
	table.buttons(0, null).containers().appendTo('#btn-container');

	// Adding the select unselect functionality to the table
	$('#etable tbody').on('click', 'td', function () {
		if ($(this).hasClass("unselectable")) {
			// This is probably the link column and the click should open the link and 
			// not select the row
			return;
		}
		$(this).parent().toggleClass('selected');
	});
	setInterval(function () {
		// user paging is not reset on reload
		table.ajax.reload(null, false);
	}, 30000); // Change this number to change the deload interval

}

function executionDetails(execId) {
	
	executionId = execId;
	
	$('#edit_exec_title').html("Execution #" + execId);
	
	$.ajax({
		url : 'api/executions/' + execId,
		type : 'GET',
	})
	.done(function(metadataObj) {
		$('#execution_edit_form [name="executionDescription"]').val(metadataObj.description);
		$('#execution_edit_form [name="executionComment"]').val(metadataObj.comment);
	});
	
	$('#execution_edit_modal').css("display", "block");
	
	$('.close').click(function() {
		$('#execution_edit_modal').css("display", "none");
	});
	
	$("#execution_edit_modal").click(function(e) {
		if (e.target.getAttribute("id") === "execution_edit_modal" ) {
			$('#execution_edit_modal').css("display", "none");
		}
	});
}

function lockUnlockSelected(table, locked) {
	var numOfSelected = table.rows('.selected').data().length;
	if (numOfSelected == 0) {
		return;
	}
	var numOfChanged = 0;
	var data = table.rows('.selected').data();
	for (var i = 0; i < numOfSelected; i++) {
		var id = data[i][0];
		$.ajax({
			url: 'api/executions/' + id + '?locked=' + locked,
			type: 'PUT',
		}).done(function () {
			if (++numOfChanged != numOfSelected) {
				return;
			}
			// Unselecting all elements
			$(".selected").removeClass("selected");
			// Everything was locked, we can reload the table.
			table.ajax.reload(null, false);
		});
	}
}

function deleteSelected(table) {
	var numOfSelected = table.rows('.selected').data().length;
	if (numOfSelected == 0) {
		return;
	}
	bootbox
		.confirm(
		"Are you sure you want to delete all selected execution reports?",
		function (result) {
			if (!result) {
				return;
			}

			var numOfDeleted = 0;
			var data = table.rows('.selected').data();
			for (var i = 0; i < numOfSelected; i++) {
				var id = data[i][0];
				$.ajax({
					url: 'api/executions/' + id,
					type: 'DELETE',
				}).done(function () {
					if (++numOfDeleted != numOfSelected) {
						return;
					}
					// Unselecting all elements
					$(".selected").removeClass("selected");
					// Everything was deleted, we can reload the table.
					table.ajax.reload(null, false);

				});
			}

		});
}

function downloadSelectedAsZipped(table) {
    var numOfSelected = table.rows('.selected').data().length;
    if (numOfSelected == 0) {
        return;
    }
    var data = table.rows('.selected').data();
    for (var i = 0; i < numOfSelected; i++) {
        var id = data[i][0];
        url= 'api/reports/' + id;
        downloadFileFromUrl(url, i);
        // Unselecting all elements
        // Everything was deleted, we can reload the table.
    }
    setTimeout(function(){window.location.reload(1);}, 2500 *numOfSelected); // Hello, John
   $(".selected").removeClass("selected");
}

function downloadFileFromUrl(url, index ){
  var hiddenIFrameID = 'hiddenDownloader' + index;
  var iframe = document.createElement('iframe');
  iframe.id = hiddenIFrameID;
  iframe.style.display = 'none';
  document.body.appendChild(iframe);
  iframe.src = url;
}

function executePlugin(table) {
	var numOfSelected = table.rows('.selected').data().length;
	if (numOfSelected == 0) {
		console.log("No executions were selected. aborting");
		return;
	}
	var selected = "";
	for (var i = 0; i < numOfSelected; i++) {
		if (i != 0) {
			selected += ","
		}
		selected += table.rows('.selected').data()[i][0];
	}
	$.ajax({
		url: 'api/plugins/',
		type: 'GET',
	}).done(function (plugins) {
		if (plugins.length == 0) {
			console.log("No plugins were defined. aborting");
			return;
		}
		var options = "";
		for (var i = 0; i < plugins.length; i++) {
			options += '<option>' + plugins[i] + '</option>';
		}
		bootbox.dialog({
			title: "Execute Plugin",
			message: '   <form class="form-horizontal">' +
			'       <div class="form-group">' +
			'           <div class="form-group">' +
			'               <label class="col-md-4 control-label" for="name">Plugin Name</label>' +
			'               <div class="col-md-4">' +
			'                   <select id="name" name="name" class="form-control">' + options +
			'                   </select>' +
			'               </div>' +
			'           </div>' +
			'           <div class="form-group">' +
			'               <label class="col-md-4 control-label" for="Parameters">Parameters</label>' +
			'               <div class="col-md-4">' +
			'                   <input id="parameter" name="parameter" type="text" placeholder="Free string parameter" class="form-control input-md">' +
			'               </div>' +
			'           </div>' +
			'           <div class="form-group">' +
			'               <label class="col-md-4 control-label" for="executions">Selected Executions</label>' +
			'               <div class="col-md-4">' +
			'                   <input id="executions" name="executions" type="text" placeholder="Selected Executions" class="form-control input-md" value=' + selected + '' +
			'                       readonly/>' +
			'               </div>' +
			'           </div>' +
			'       </div>' +
			'   </form>',
			buttons: {
				success: {
					label: "Execute",
					className: "btn-success",
					callback: function () {
						var name = $('#name').val();
						var parameter = $('#parameter').val();
						var executions = "";
						for (var i = 0; i < numOfSelected; i++) {
							executions += '&executions=' + table.rows('.selected').data()[i][0];
						}
						$.ajax({
							url: 'api/plugins/' + name + '?params=' + parameter + executions,
							type: 'POST',
						}).done(function (response) {
							bootbox.dialog({
								title:"Plugin Response",
								message: response
							});
						});
					}
				}
			}
		}
		);
	});
}