var executionId;

$(document).ready(function() {			
	populateNavBar();
	$.ajax('/api/reports').done(maketable);
	
	$("#execution_edit_form").submit(function(){
		
		if(confirm('Update execution #' + executionId + ' description/comment?')) {
			
			var executionEditRequestJson = {
				"executionId": executionId,
				"executionDescription": $('#execution_edit_form [name="executionDescription"]').val(),
				"executionComment": $('#execution_edit_form [name="executionComment"]').val()
			};
		
			$.ajax({
						url : 'api/executions/update',
						type : 'POST',
						data: JSON.stringify(executionEditRequestJson),
						contentType: "application/json;charset=utf-8"
					})
					.done(function(text) {
						//alert(text);
						location.reload();							
					})
					.fail(function() {
						alert("Error updating execution #" + executionEditRequestJson.executionId);
						location.reload();							
					});
			
			return false;
		}
	});
});

function maketable(json) {

	// adding "Edit" column
	json.headers.splice(3, 0, "Execution Details");
	
	for (var i=0; i<json.data.length; i++) {
		json.data[i].More = "Show/Edit descirption & comment";
	}

	var data = json.data;
	var column_names = json.headers;
	var columns = []
		
	for (var i = 0; i < column_names.length; i++) {
		columns[i] = {
			'title' : column_names[i],
			'data' : column_names[i]
		}
	}

	var table = $('#etable')
			.DataTable(
					{
						dom : 'lBfrtip',
						buttons : [ {
							text : 'Delete',
							action : function(e, dt, node, config) {
								deleteSelected(table);
							}
						}, {
							text : 'Lock',
							action : function(e, dt, node, config) {
								lockUnlockSelected(table,"true");
							}
						}, {
							text : 'Unlock',
							action : function(e, dt, node, config) {
								lockUnlockSelected(table,"false");
							}
						}, {
							text : 'Execute Plugin',
							action : function(e, dt, node, config) {
								executePlugin(table);
							}
						},
						'copyHtml5', 'csvHtml5', 'pdfHtml5' ],
						columns : columns,
						data : data,
						aaSorting : [],
						deferRender : true,
						sPaginationType : "full_numbers",
						iDisplayLength : 25,
						columnDefs : [
								{
									// Alows missing values. Important for execution properties in which not all of the execution must have all the properties.
									targets : "_all",
									sDefaultContent : ""
								},
								{
									// The description column will be rendered as link using the value in the link column
									targets : 1,
									// We don't want that a click on the link will make the row selectable
									className : "unselectable",
									data : null,
									"render" : function(data, type, row) {
										return '<a target="_blank" href="'+row.Link+'">'
												+ data + '</a>';
									}
								},
								{
									targets : 3,
									className : "unselectable",
									data : null,
									"render" : function(data, type, row) {
										return '<button type="button" onClick="executionDetails(' + row.ID + ',\'' + row.Description + '\')">Description & Comment</button>';
									}
								},
								{
									// The link column is no longer needed since we are using the value in the description column
									targets : 2,
									visible : false
								}
							]
					})
	$('#etable tbody').on('click', 'td', function() {
		if ($(this).hasClass("unselectable")) {
			// This is probably the link column.
			return;
		}
		$(this).parent().toggleClass('selected');
	});

}

function executionDetails(execId, execDescription) {
	
	executionId = execId;
	
	$('#edit_exec_title').html("Execution #" + execId);
	$('#execution_edit_form [name="executionDescription"]').val(execDescription);
	
	// get the current comment
	$.ajax({
		url : 'api/executions/' + execId + '/comment',
		type : 'GET',
	})
	.done(function(text) {
		$('#execution_edit_form [name="executionComment"]').val(text);
	});
	
	$('#execution_edit_container').css("display", "block");
	
	$('.close').click(function() {
		$('#execution_edit_container').css("display", "none");
	});
}

function lockUnlockSelected(table,locked){
	var numOfSelected = table.rows('.selected').data().length;
	if (numOfSelected == 0) {
		return;
	}
	var numOfChanged = 0;
	for (var i = 0; i < numOfSelected; i++) {
		var id = table.rows('.selected').data()[i].Id;
		$.ajax({
			url : 'api/executions/' + id + '?locked=' + locked,
			type : 'PUT',
		}).done(function() {
			if (++numOfChanged != numOfSelected) {
				return;
			}
			// Everything was locked, we can refresh the table.
			location.reload();
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
				function(result) {
					if (!result) {
						return;
					}

					var numOfDeleted = 0;
					for (var i = 0; i < numOfSelected; i++) {
						var id = table.rows('.selected').data()[i].Id;
						$.ajax({
							url : 'api/executions/' + id,
							type : 'DELETE',
						}).done(function() {
							if (++numOfDeleted != numOfSelected) {
								return;
							}
							// Everything was deleted, we can refresh the table.
							location.reload();
						});
					}

				});
}

function executePlugin(table) {
	var numOfSelected = table.rows('.selected').data().length;
	if (numOfSelected == 0) {
		console.log("No executions were selected. aborting");
		return;
	}
	var selected = "";
	for (var i = 0; i < numOfSelected; i++) {
		if (i != 0){
			selected += ","
		}
		selected += table.rows('.selected').data()[i].Id;
	}
	$.ajax({
		url : 'api/plugins/',
		type : 'GET',
	}).done(function(plugins) {
		if (plugins.length == 0) {
			console.log("No plugins were defined. aborting");
			return;
		}
		var options = "";
		for (var i = 0; i < plugins.length ; i++){
			options += '<option>' + plugins[i] +'</option>';
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
				'                   <input id="executions" name="executions" type="text" placeholder="Selected Executions" class="form-control input-md" value='+selected+'' +
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
							executions += '&executions=' + table.rows('.selected').data()[i].Id;
						}
						$.ajax({
							url : 'api/plugins/' + name + '?params=' + parameter + executions,
							type : 'POST',
						}).done(function() {
							
						});
					}
				}
			}
		}
	);
});
}