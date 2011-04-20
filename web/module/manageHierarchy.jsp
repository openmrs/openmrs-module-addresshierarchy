<%@ include file="/WEB-INF/template/include.jsp"%>
<openmrs:require privilege="Manage Hierarchy" otherwise="/login.htm" redirect="/module/addresshierarchy/addresshierarchyTree.htm" />
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/dwr/interface/AddressHierarchy.js"/>
<openmrs:htmlInclude file="/moduleResources/addresshierarchy/jquery.js"/>
<openmrs:htmlInclude file="/moduleResources/addresshierarchy/tablednd.js"/>
<style type="text/css">
table {width:50%}
.tableWrapper {text-align:center}
.trh {background-color:#ffffcc;
	  font-weight:bold}
.tr0 {background-color:#ffffcc}
.tr1 {background-color:#ccffcc}
.col0 {width:3%}
.col1 {width:20%}
.col2 {width:5%}
.hid {display:none}
</style>
<script>
 var typelist;

function builder(list){//builds the table and feeds into the div with the proper location types
		var output = new Array();
		typelist = list;
		output.push("<table id=\"edittable\">");
		
		for(var j=0;j<list.length;j++){
			output.push("<tr>");
			output.push("<th>"+list[j]+"</th>");
			output.push("<td><input type=\"text\" value=\""+list[j]+"\" id=\""+list[j]+"\" /></td>")
			output.push("</tr>");
		}
		output.push("</table>");
		
		document.getElementById("tableWrapper").innerHTML=output.join("");
		
	}


function init(){//runs when window loads
	AddressHierarchy.getAddressHierarchyTypeList(0,builder);	
}

function tableReset(){//resets the page
	document.getElementById("edittable").parentNode.removeChild(document.getElementById("edittable"));
	init();
}

function updateTable(){//updates the table when the page is submitted
	AddressHierarchy.getAddressHierarchyTypeList(0,updater);
}

function updater(data){//called from updateTable
	var x =new Array();
	
	for(var i=0;i<data.length;i++){
		if(document.getElementById(data[i]).value!="")
			x.push(document.getElementById(data[i]).value);
		else
			x.push("Not Applicable");
	}
	AddressHierarchy.updateLocationTypeTable(x,location.reload());
	
}

window.onload=init;
</script>
<body>
<div class="tableWrapper" id="tableWrapper">
</div>
<br>
<br>
<input type="submit" id="update" value="Update" onclick="updateTable()" />
<input type="submit" id="reset" value="Reset" onclick="tableReset()"/>
</body>
