function Pager(tableName, itemsPerPage) {
	this.tableName = tableName;
	this.itemsPerPage = itemsPerPage;
	this.currentPage = 1;
	this.pages = 0;
	this.inited = false;

	this.showRecords = function(from, to) {
		var rows = document.getElementById(tableName).rows;
		// i starts from 1 to skip table header row
		for ( var i = 1; i < rows.length; i++) {
			if (i < from || i > to)
				rows[i].style.display = 'none';
			else
				rows[i].style.display = '';
		}
	}

	this.showPage = function(pagerName, pageNumber) {
		if (!this.inited) {
			alert("not inited");
			return;
		}
		var oldPageAnchor = document.getElementById(pagerName + '_pg'
				+ this.currentPage);
		if (oldPageAnchor != null) {
			oldPageAnchor.className = 'pg-normal';
		}

		this.currentPage = pageNumber;
		var newPageAnchor = document.getElementById(pagerName + '_pg'
				+ this.currentPage);
		if (newPageAnchor != null) {
			newPageAnchor.className = 'pg-selected';
		}

		var from = (pageNumber - 1) * itemsPerPage + 1;
		var to = from + itemsPerPage - 1;
		this.showRecords(from, to);

		var pgNext = document.getElementById(pagerName + '_pgNext');
		var pgPrev = document.getElementById(pagerName + '_pgPrev');
		if (pgNext != null) {
			if (this.currentPage == this.pages)
				pgNext.style.display = 'none';
			else
				pgNext.style.display = '';
		}
		if (pgPrev != null) {
			if (this.currentPage == 1)
				pgPrev.style.display = 'none';
			else
				pgPrev.style.display = '';
		}
	}

	this.prev = function(pagerName) {
		if (this.currentPage > 1)
			this.showPage(pagerName, this.currentPage - 1);
	}

	this.next = function(pagerName) {
		if (this.currentPage < this.pages) {
			this.showPage(pagerName, this.currentPage + 1);
		}
	}

	this.init = function() {
		var rows = document.getElementById(tableName).rows;
		var records = (rows.length - 1);
		this.pages = Math.ceil(records / itemsPerPage);
		this.inited = true;
	}

	this.showPageNav = function(pagerName, positionId) {
		if (!this.inited) {
			alert("not inited");
			return;
		}
		var element = document.getElementById(positionId);
		if (this.pages > 0) {
			var pagerHtml = '<span id="'
					+ pagerName
					+ '_pgPrev" onclick="'
					+ pagerName
					+ '.prev(\''
					+ pagerName
					+ '\');" class="pg-normal"> &#171  $i18n.getText("table.prev") </span> | ';
			for ( var page = 1; page <= this.pages; page++)
				pagerHtml += '<span id="' + pagerName + '_pg' + page
						+ '" class="pg-normal" onclick="' + pagerName
						+ '.showPage(\'' + pagerName + '\',' + page + ');">'
						+ page + '</span> | ';
			pagerHtml += '<span id="'
					+ pagerName
					+ '_pgNext" onclick="'
					+ pagerName
					+ '.next(\''
					+ pagerName
					+ '\');" class="pg-normal"> $i18n.getText("table.next")  &#187;</span>';

			element.innerHTML = pagerHtml;
		}
	}
}
