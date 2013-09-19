function DropDown(el) {
	this.dd = el;
	this.initEvents();
}

DropDown.prototype = {
	initEvents : function() {
		jQuery(function($) {
			$('#dd').click(function(event) {
				$('#dd').toggleClass('active');
				event.stopPropagation();
			});
		});
	}
}

jQuery(function($) {

	var dd = new DropDown($('#dd'));

	$(document).click(function() {
		// all dropdowns
		$('.dropdown-menu').removeClass('active');
	});

});

function addRow(fieldKey, fieldName) {
	var table = document.getElementById('optional-field');
	var tbody = table.getElementsByTagName("tbody")[0];
	var rowCount = tbody.rows.length;
	var row = tbody.insertRow(rowCount);
	row.id = fieldKey + '_tr';

	var cell1 = row.insertCell(0);
	var element1 = document.createElement("label");
	var id = fieldName + "_VALUE";
	element1.setAttribute("for", id);
	element1.innerHTML = fieldName;
	cell1.appendChild(element1);

	var cell2 = row.insertCell(1);
	var element2 = document.createElement("input");
	element2.type = "text";
	element2.name = fieldName;
	element2.setAttribute("id", id);
	var element3 = document.createElement("a");
	var onclickAttribute = 'deleteRow("' + fieldKey + '", "' + fieldName + '")';
	element3.setAttribute("onclick", onclickAttribute);
	var classVar = 'ui-icon icon-trash pull-rigth';
	element3.setAttribute("class", classVar);
	var titleVar = '$i18n.getText("icon.remove")';
	element3.setAttribute("title", titleVar);
	cell2.appendChild(element2);
	cell2.appendChild(element3);
	var deleteLiId = '#' + fieldKey;
	jQuery(function($) {
		$(deleteLiId).remove();
	});
	tbody.appendChild(row);
}

function deleteRow(fieldKey, fieldName) {
	if (fieldName != "Custom") {
		var deleteRowId = '#' + fieldKey + '_tr';
		jQuery(function($) {
			$(deleteRowId).remove();
		});
		var ul = document.getElementById('dropdown-list');
		var newLI = document.createElement("li");
		newLI.setAttribute("id", fieldKey);
		var onclickAttr = 'addRow("' + fieldKey + '","' + fieldName + '\")';
		var a = document.createElement("a");
		a.setAttribute("onclick", onclickAttr);
		a.innerHTML = fieldName;
		newLI.appendChild(a);
		ul.appendChild(newLI);
	}
}