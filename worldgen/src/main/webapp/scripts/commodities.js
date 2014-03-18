/*
var data = { {'id': '3400''name': 'Military clothing''parent': '3''image': 'textiles''tech': '1''cost': '100''legality': '6''consumption': '4''production': '7''codes': {'In', 'Cl'} }, {'id': '3200''name': 'Civilian clothing''parent': '3''image': 'textiles''tech': '1''cost': '100''legality': '6''consumption': '4''production': '7''codes': {'In', 'Cl'} }, {'id': '3002''name': 'Simple textiles''parent': '3''image': 'textiles''tech': '1''cost': '100''legality': '6''consumption': '4''production': '7''codes': {'In'} }, {'id': '3001''name': 'Silk''parent': '3''image': 'textiles''tech': '3''cost': '100''legality': '6''consumption': '4''production': '7''codes': {'In'} }, {'id': '3000''name': 'Wool''parent': '3''image': 'textiles''tech': '1''cost': '100''legality': '6''consumption': '4''production': '7''codes': {'In'} }, {'id': '3300''name': 'Industrial clothing''parent': '3''image': 'textiles''tech': '1''cost': '100''legality': '6''consumption': '4''production': '7''codes': {'In', 'Cl'} }, {'id': '3100''name': 'Primitive clothing''parent': '3''image': 'textiles''tech': '0''cost': '100''legality': '6''consumption': '4''production': '7''codes': {'In', 'Cl'} } };
*/

var data = {'id': '3400', 'name': 'Military clothing', 'parent': '3', 'image': 'textiles', 'tech': '1', 'cost': '100', 'legality': '6', 'consumption': '4', 'production': '7', 'codes': [ 'yes', 'no', 'maybe' ] };

var children = { 'list': [ {'id': '3400', 'name': 'Military clothing', 'parent': '3', 'image': 'textiles', 'tech': '1', 'cost': '100', 'legality': '6', 'consumption': '4', 'production': '7', 'codes': ['In', 'Cl'] }, {'id': '3200', 'name': 'Civilian clothing', 'parent': '3', 'image': 'textiles', 'tech': '1', 'cost': '100', 'legality': '6', 'consumption': '4', 'production': '7', 'codes': ['In', 'Cl'] }, {'id': '3002', 'name': 'Simple textiles', 'parent': '3', 'image': 'textiles', 'tech': '1', 'cost': '100', 'legality': '6', 'consumption': '4', 'production': '7', 'codes': ['In'] }, {'id': '3001', 'name': 'Silk', 'parent': '3', 'image': 'textiles', 'tech': '3', 'cost': '100', 'legality': '6', 'consumption': '4', 'production': '7', 'codes': ['In'] }, {'id': '3000', 'name': 'Wool', 'parent': '3', 'image': 'textiles', 'tech': '1', 'cost': '100', 'legality': '6', 'consumption': '4', 'production': '7', 'codes': ['In'] }, {'id': '3300', 'name': 'Industrial clothing', 'parent': '3', 'image': 'textiles', 'tech': '1', 'cost': '100', 'legality': '6', 'consumption': '4', 'production': '7', 'codes': ['In', 'Cl'] }, {'id': '3100', 'name': 'Primitive clothing', 'parent': '3', 'image': 'textiles', 'tech': '0', 'cost': '100', 'legality': '6', 'consumption': '4', 'production': '7', 'codes': ['In', 'Cl'] }] };

function getTable() {
    var     table = document.getElementById("commodities");

    return table;
}

/**
 * Add an item to the end of the table.
 */
function addItemToTable(item, index) {
    var     table = getTable();
    
    if (index < 0) index = table.rows.length;
    
    var     row = table.insertRow(index);
    row.id = "c"+item.id;
    row.insertCell(0).innerHTML = "<img style='padding-left:"+item.indent+"em' src='/traveller/images/icons/add.png' width='8' height='8' onclick='openItem("+item.id+")'/> "+item.id;
    row.insertCell(1).innerHTML = "<img src='/traveller/images/trade/"+item.image+".png' width='32'/>";
    row.insertCell(2).innerHTML = "<p onclick='openItem("+item.id+")'>"+item.name+"</p>";
    row.insertCell(3).innerHTML = item.cost;
    row.insertCell(4).innerHTML = item.production;
    row.insertCell(5).innerHTML = item.consumption;
    row.insertCell(6).innerHTML = item.legality;
    row.insertCell(7).innerHTML = item.tech;
    row.insertCell(8).innerHTML = "";
}

var commodityList = new Array();

function sortById(a, b) {
	if (a.id == b.id) return 0;
	if (a.id < b.id) return -1;
	return +1;
}

function sortByName(a, b) {
	if (a.name == b.name) return 0;
	if (a.name < b.name) return -1;
	return +1;
}

function sortByCost(a, b) {
	if (a.cost == b.cost) return 0;
	if (a.cost < b.cost) return -1;
	return +1;
}

function getParents(transport) {
	var	json = transport.responseText;
	commodityList = json.evalJSON().list.sort(sortById);

    for (var i = 0; i < commodityList.length; i++) {
    	commodityList[i].indent = 0;
        addItemToTable(commodityList[i], -1);
    }
}

function getItemById(id) {
	for (var i=0; i < commodityList.length; i++) {
		if (commodityList[i].id == id) return commodityList[i];
	}
	return null;
}

function getChildren(transport) {
	var json = transport.responseText;
	var list = json.evalJSON().list.sort(sortById);
	var table = getTable();
	
	//alert(transport.responseText);

	if (list.length != 0) {
		var	parentId = list[0].parent;

		var parent = getItemById(parentId);
		var	indent = 0;
		if (parent != null) {
			indent = parent.indent + 1;
		}

		var	parentRow = document.getElementById("c"+parentId);
		if (parentRow == null) {
			return;
		}
		var	parentRowNumber = parentRow.rowIndex;
		
		for (var i=0; i < list.length; i++) {
			list[i].indent = indent;
			addItemToTable(list[i], parentRowNumber+i+1);
			commodityList.push(list[i]);
		}
	}
}

/**
 * Make asynchronous call to get children of the given commodity.
 * Calls back to getChildren() when result is obtained.
 */
function openItem(id) {
	var		url = "/traveller/commodity/"+id+".json";
	var		params = { method: 'get', parameters: { children: 'true' }, onSuccess: getChildren };
	new Ajax.Request(url, params);
	
	// Remove the 'plus' sign from this commodity item.
	var row = document.getElementById("c"+id);
	if (row != null) {
		var cell = row.cells[0];
		if (cell != null) {
			var	item = getItemById(id);
			cell.innerHTML = "<span style='padding-left:"+item.indent+"em'>"+id+"</span>";
		}
	}
}

function loaded() {
    new Ajax.Request("/traveller/commodity/0.json", { method: 'get', parameters: { children: 'true' }, onSuccess: getParents, onFailure: function() { alert("Failed")} });
}
