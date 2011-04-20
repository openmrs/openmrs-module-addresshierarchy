	

<%@ include file="/WEB-INF/template/include.jsp"%>

<style>
.hide{

	display:none;

}

</style>



<openmrs:htmlInclude file="/dwr/interface/AddressHierarchy.js"/>



<script type="text/javascript">
var count = 0;
function tableBuilder(data){
	
	if(count==0){ //count to prevent the script running more than one time
	
	count++;
	
	AddressHierarchy.getAddressHierarchyTypeList(0,function(dataa){ //this pulls out all the location types
		var list1 = ["","Address 1","Address 2"];
		
		var list = new Array();
		list = list1.concat(dataa);
		var cnt = 0;
		
		var output = new Array();
		
		output.push("<table id=\"dashboard\">");
		output.push("<tr>");
		for(var j=0;j<list.length;j++){
			output.push("<th>"+list[j]+"</th>");
		}
		
		output.push("</tr>");
		output.push("<tr>");
		for(var i = 0; i<data.length;i++){
			if(data[i]=="true")
				output.push("<td>*</td>");
			else if(data[i]=="false")
				output.push("<td></td>")
			else	
				output.push("<td>"+data[i]+"</td>");
			if(i!=0){
			if(i==13)
				output.push("</tr><tr>");
			else if(i%14 == 0 && i!=14)
				output.push("</tr><tr>");
			}
		}
		output.push("</tr>");
		output.push("</table>");
		
		document.getElementById("tableWrapper").innerHTML=output.join("");//adding the table to the div
		if(document.getElementById("newPatient")!=null)
			document.getElementById("newPatient").parentNode.removeChild(document.getElementById("newPatient"));
		if(document.getElementById("patientForm")!=null)
			document.getElementById("patientForm").parentNode.removeChild(document.getElementById("patientForm"));
			
		
	});
	
		
}
	if(document.getElementById("newPatient")!=null)
		document.getElementById("newPatient").parentNode.removeChild(document.getElementById("newPatient"));
	if(document.getElementById("patientForm")!=null)
		document.getElementById("patientForm").parentNode.removeChild(document.getElementById("patientForm"));

	while(document.getElementById("newPatient")!=null)
		document.getElementById("newPatient").parentNode.removeChild(document.getElementById("newPatient"));
	while(document.getElementById("patientForm")!=null)
		document.getElementById("patientForm").parentNode.removeChild(document.getElementById("patientForm"));
}

function mainTableBuilder(){// fills the location types in the newPatientForm
	
	
		AddressHierarchy.getAddressHierarchyTypeList(0,function(list){ //brings out all the location types
			var cnt = 0;
			var name;
			

			for(var i=0;i<list.length;i++){
				name="th"+i;
				while(document.getElementById(name) == null){
					}
				document.getElementById(name).innerHTML=list[i];
			}
		});
		
}



function getCountryList(array){//fills the first dropdown in the newPatientForm
	
	if(count == 0){
	count++;
	
	var dummy = new Array();

	dummy[0] = "Select One";

	var country = dummy.concat(array);

	DWRUtil.removeAllOptions("country");

	DWRUtil.addOptions("country", country);

	setTimeout("mainTableBuilder()", 1000);
	
	document.getElementById("countryparentid").value = "0";
	
	if(document.getElementById("patientForm")!=null)
		document.getElementById("patientForm").parentNode.removeChild(document.getElementById("patientForm"));
	

}
}
function locFill(data){//fills the patient address in the patientForm
	
	if(count==0){

	count++;
	
	AddressHierarchy.getAddressHierarchyTypeList(0,function(dataa){// brings out the location types list
		var j;
		var k=0;
		var list1 = ["Address 1","Address 2"];
		var loclist=new Array();
		loclist=list1.concat(dataa);
		var list = ["address1","address2","country","stateProvince","countyDistrict","subregion","region","townshipDivision","cityVillage","neighborhoodCell","postalCode","longitude","latitude"];
		var name="";
		var locname="";
		var idx = 10;
		var icnt=1;
		var dummcnt=0;
		j=0;
		
		try{
		for(var i=0;i<data.length;i++){//filling the addresses in loops
			if(data[i]=="true"){
				document.getElementsByName("addresse[0].preferred")[dummcnt].checked = true;
				document.getElementsByName("addresse[0].preferred")[0].name = "addresses["+j+"].preferred"; 
				i++;
				icnt++;
			}
			if(data[i]=="false"){
				document.getElementsByName("addresse[0].preferred")[dummcnt].checked = false;
				document.getElementsByName("addresse[0].preferred")[0].name = "addresses["+j+"].preferred";
				i++;
				icnt++;
			}
			locname = "id"+idx;
			name = "addresse[0]."+list[k];
			document.getElementsByName(locname)[j].innerHTML = loclist[k];
			if(document.getElementsByName(name)[0]!=null){
			document.getElementsByName(name)[0].value=data[i];
			document.getElementsByName(name)[0].name="addresses["+j+"]."+list[k];}
			if(document.getElementsByName("_addresses[0].preferred")[dummcnt]!=null)
				document.getElementsByName("_addresses[0].preferred")[dummcnt].name = "_addresses["+j+"].preferred";
			if(idx!=22)
				idx++;
			if(k!=12)
				k++;
				if(icnt==14){
					if(dummcnt==0){
						dummcnt++;
					}
					icnt=0;
					if(locname=="id21"){
						document.getElementsByName("id22")[j].innerHTML = list[list.length-1];
					}
					j++;
					k=0;
					idx=10;
					}
				icnt++;
			}
		}
		catch(err){
			alert(err);
			}
		try{
			document.getElementById("addressData").innerHTML = document.getElementById("address0Data").innerHTML;//adding the innerHTML to addressdata div
		var pfTable = document.getElementById("addressData").childNodes[1].childNodes[7].childNodes[1];
		pfTable.childNodes[0].childNodes[3].childNodes[3].checked=false;
		
		pfTable.childNodes[0].childNodes[3].childNodes[0].name="_preferred";
		pfTable.childNodes[2].childNodes[3].childNodes[0].name="address1";
		pfTable.childNodes[4].childNodes[3].childNodes[0].name="address2";
		var jj=0;
		var listx=["country","stateProvince","countyDistrict","subregion","region","townshipDivision","cityVillage","neighborhoodCell","postalCode","longitude","latitude"];
		for(var ii=6;ii<=26;ii=ii+2){//editing the addressdata div dynamically
			pfTable.childNodes[ii].childNodes[7].childNodes[0].name = listx[jj];
			jj++;
		}
		if(document.getElementById("newPatient")!=null)
			document.getElementById("newPatient").parentNode.removeChild(document.getElementById("newPatient"));
		}
		
		catch(err){
			alert(err);
		}

		while(document.getElementById("newPatient")!=null)
			document.getElementById("newPatient").parentNode.removeChild(document.getElementById("newPatient"));
		
			
		
	});
}
	
}

function init(){//runs on loading the page which decides which functions to run based on the page url

	var loc = window.location.href;
	var locarr = loc.split("/");
	var temp = locarr[locarr.length - 1];
	var fin = temp.split("?");
	
	if(fin[0]=="newPatient.form"){//check for newPatientForm
		
		AddressHierarchy.getCountryList(getCountryList);
	}
	else if(fin[0]=="patientDashboard.form"){//check for patientDashboard
		if(document.getElementById("newPatient")!=null)
			document.getElementById("newPatient").parentNode.removeChild(document.getElementById("newPatient"));
		if(document.getElementById("patientForm")!=null)
			document.getElementById("patientForm").parentNode.removeChild(document.getElementById("patientForm"));
		var dumx = fin[1].split("=");
		var dumy = dumx[1].split("&");
		var patientid = dumy[0];
		
		AddressHierarchy.getLocation(patientid,tableBuilder);
	}
	else{//everything fails then its patient form

		var dumx = fin[1].split("=");
		var patientid = dumx[1];
		AddressHierarchy.getLocation(patientid,locFill);
		if(document.getElementById("newPatient")!=null)
			document.getElementById("newPatient").parentNode.removeChild(document.getElementById("newPatient"));
		if(document.getElementById("newPatient")!=null)
			document.getElementById("newPatient").parentNode.removeChild(document.getElementById("newPatient"));

		
	}
}



function setComponent(locList){//called from newPatientForm to fill the next dropdown when a location is selected

	try{
		
	if(locList.length!=0){

	var paramList = ["","country","state","sublocation1","sublocation2","sublocation3","sublocation4","sublocation5","sublocation6","postalcode","longitude","latitude"];

	var locationName = locList[0];

	var id = parseInt(locList[1]);
		
	var parentLocationId = locList[2];

	var paramName = paramList[locList[3]];

	var parentId = paramList[locList[3]]+"parentid";

	

	document.getElementById(parentId).value = parentLocationId;

    var arr = locList.slice(4,locList.length);

	var dummy = new Array();

	dummy[0] = "Select One";

	var subArray = dummy.concat(arr);

	document.getElementById(paramName).disabled=false;

	DWRUtil.removeAllOptions(paramName);

    DWRUtil.addOptions(paramName,subArray);

	}
	}
	catch(err){
		alert(err);
	}        

}



function doIt(){//called from locationSelected to fill the next dropdown boxex

	var arr = [this.typeId,this.parId,this.locationName];

	AddressHierarchy.getNextComponent(arr,setComponent);

}

function doItPForm(selected,arrList){//called from locationSelectedPform to fill the drop down boxes in patientForm
	AddressHierarchy.getNextComponent(arrList,function(locList){
		var element = selected;
		var paramList = ["","country","state","sublocation1","sublocation2","sublocation3","sublocation4","sublocation5","sublocation6","postalcode","longitude","latitude"];

		var locationName = locList[0];

		var id = parseInt(locList[1]);

		var parentLocationId = locList[2];

		var paramName = paramList[locList[3]];

		var parentId = paramList[locList[3]]+"parentid";
		
		try{
		
		var nextElement = element.parentNode.parentNode.nextSibling.nextSibling.childNodes[3].childNodes[0];
		
		while(nextElement.id!=paramName){
			
			nextElement.parentNode.parentNode.childNodes[7].childNodes[0].value="";
			nextElement = nextElement.parentNode.parentNode.nextSibling.nextSibling.childNodes[3].childNodes[0];
			
		}
		nextElement.disabled=false;
		}
		catch(err){
			alert(err);
		}
		nextElement.parentNode.parentNode.childNodes[5].childNodes[0].value = parentLocationId;

	    var arr = locList.slice(4,locList.length);
		
		var dummy = new Array();

		dummy[0] = "Select One";

		var subArray = dummy.concat(arr);
		
				nextElement.length = 0;

		for(var i=0;i<subArray.length;i++){
			var optn = document.createElement("OPTION");
			optn.text = subArray[i];
			optn.value = subArray[i];
			nextElement.options.add(optn);
		}

		});
}

function locationSelectedPform(selected,id){//called from patient form when a location is selected from the drop down box

	if(selected.value == "Load"){
		AddressHierarchy.getCountryList(function(array){
			var dummy = new Array();

			dummy[0] = "Select One";

			var country = dummy.concat(array);

			selected.length = 0;

			for(var i=0;i<country.length;i++){
				var optn = document.createElement("OPTION");
				optn.text = country[i];
				optn.value = country[i];
				selected.options.add(optn);
			}
				
			selected.parentNode.parentNode.childNodes[5].childNodes[0].value = "0";
					
			});
	}
	else{
	this.typeId = id+"";
	
	var list = ["","country","state","sublocation1","sublocation2","sublocation3","sublocation4","sublocation5","sublocation6","postalcode","longitude","latitude"];

	var arr=new Array("","country","stateProvince","countyDistrict","subregion","region","townshipDivision","cityVillage","neighborhoodCell","postalCode","longitude","latitude");

	
	this.paramName = list[id];

	this.locationName = selected.value;

	var dumm = "address."+arr[id];

	selected.parentNode.parentNode.childNodes[7].childNodes[0].value = selected.value;
	var par = selected.parentNode.parentNode.nextSibling.nextSibling.childNodes[3].childNodes[0];
	while(par!=null){
		par.disabled=true;
		par.length=0;
		par.parentNode.parentNode.childNodes[7].childNodes[0].value="";
		
		if(par.parentNode.parentNode.nextSibling.nextSibling!=null)
			par = par.parentNode.parentNode.nextSibling.nextSibling.childNodes[3].childNodes[0];
		else
			par=null;
		}
	
	parentId=list[id]+"parentid";

	this.parId = selected.parentNode.parentNode.childNodes[5].childNodes[0].value; 

	this.arrList = [typeId,parId,locationName];

	doItPForm(selected,arrList);
	
	
}
}

function locationSelected(id){//called from newPatient form when a location is selected

	this.typeId = id+"";

	var list = ["","country","state","sublocation1","sublocation2","sublocation3","sublocation4","sublocation5","sublocation6","postalcode","longitude","latitude"];

	var arr=new Array("","country","stateProvince","countyDistrict","subregion","region","townshipDivision","cityVillage","neighborhoodCell","postalCode","longitude","latitude");

	
	this.paramName = list[id];

	this.locationName = DWRUtil.getValue(paramName);

	

	for(var i=id+1;i<list.length;i++){

		document.getElementById(list[i]).disabled=true;

		DWRUtil.removeAllOptions(list[i]);

	}
	

	var dumm = "address."+arr[id];
	try{
		
	var par = document.getElementById(dumm).parentNode.parentNode.nextSibling.nextSibling.childNodes[3].childNodes[1];
	while(par!=null){
		par.parentNode.parentNode.childNodes[7].childNodes[0].value="";
		
		if(par.parentNode.parentNode.nextSibling.nextSibling!=null)
			par = par.parentNode.parentNode.nextSibling.nextSibling.childNodes[3].childNodes[0];
		else
			par=null;
	
	}}
	catch(err){
		alert(err);
	}
	document.getElementById(dumm).value = DWRUtil.getValue(paramName);

	parentId=list[id]+"parentid";

	this.parId = document.getElementById(parentId).value; 

	this.arrList = [typeId,parId,locationName];


	doIt();

}


function savefinval(selected){//called when the location from last drop down to fill the data

	selected.parentNode.parentNode.childNodes[7].childNodes[0].value = selected.value;
}


window.onload=init;

</script>


	<table id="newPatient" name="newPatient">

		<tr>
			<th>Address 1</th>
			<td><input type="text" value="" name="address.address1" id="address.address1"></td>
		</tr>

		<tr>
			<th>Address 2</th>
			<td><input type="text" value="" name="address.address2" id="address.address2"></td>
		</tr>


		<tr>

		

			<th id="th0"></th>

			<td>

				<select name="country" id="country" onChange="locationSelected(1)">

				<option>Wait...</option>

				</select>

				

			</td>

			<td><input name=countryparentid id="countryparentid" class="hide"></td>
			<td><input type="text" value="" name="address.country" id="address.country"></td>

			</tr>

			

		<tr>

			<th id="th1"></th>

			<td>

			<select name="state" id="state" onchange="locationSelected(2)"></select>

			

			</td>

			<td><input name="stateparentid" id="stateparentid" class="hide"></td>
			<td><input type="text" value="" name="address.stateProvince" id="address.stateProvince"></td>
			</tr>

			

		<tr>

			<th id="th2"></th>

			<td>

			<select name="sublocation1" id="sublocation1" onchange="locationSelected(3)"></select>

			

			</td>

			<td><input name="sublocation1parentid" id="sublocation1parentid" class="hide"></td>
			<td><input type="text" value="" name="address.countyDistrict" id="address.countyDistrict"></td>
			
			</tr>

			

		<tr>

			<th id="th3"></th>

			<td>

			<select name="sublocation2" id="sublocation2" onchange="locationSelected(4)"></select>

			

			</td>

			<td><input name="sublocation2parentid" id="sublocation2parentid" class="hide"></td>
			<td><input type="text" value="" name="address.subregion" id="address.subregion"></td>
			
			</tr>

			

		<tr>	

			<th id="th4"></th>

			<td>

			<select name="sublocation3" id="sublocation3" onchange="locationSelected(5)"></select>

			

			</td>

			<td><input name="sublocation3parentid" id="sublocation3parentid" class="hide"></td>
			<td><input type="text" value="" name="address.region" id="address.region"></td>
			</tr>

			

		<tr>

			<th id="th5"></th>

			<td>

			<select name="sublocation4" id="sublocation4" onchange="locationSelected(6)"></select>

			

			</td>

			<td><input name="sublocation4parentid" id="sublocation4parentid" class="hide"></td>
			<td><input type="text" value="" name="address.townshipDivision" id="address.townshipDivision"></td>
			</tr>

			

		<tr>

			<th id="th6"></th>

			<td>

			<select name="sublocation5" id="sublocation5" onchange="locationSelected(7)"></select>

			

			</td>

			<td><input name="sublocation5parentid" id="sublocation5parentid" class="hide"></td>
			<td><input type="text" value="" name="address.cityVillage" id="address.cityVillage"></td>
			
			</tr>

					

		<tr>	

			<th id="th7"></th>

			<td>

			<select name="sublocation6" id="sublocation6" onchange="locationSelected(8)"></select>

			

			</td>

			<td><input name="sublocation6parentid" id="sublocation6parentid" class="hide"></td>
			<td><input type="text" value="" name="address.neighborhoodCell" id="address.neighborhoodCell"></td>
			</tr>

			

		<tr>

			<th id="th8"></th>

			<td>

			<select name="postalcode" id="postalcode" onchange="locationSelected(9)"></select>

			</td>

			<td><input name=postalcodeparentid id="postalcodeparentid" class="hide"></td>
			<td><input type="text" value="" name="address.postalCode" id="address.postalCode"></td>
			</tr>

			

		<tr>	

			<th id="th9"></th>

			<td>

			<select name="longitude" id="longitude" onchange="locationSelected(10)"></select>

			</td>

			<td><input name="longitudeparentid" id="longitudeparentid" class="hide"></td>
			<td><input type="text" value="" name="address.longitude" id="address.longitude"></td>
			</tr>

			

		<tr>	

			<th id="th10"></th>

			<td>

			<select name="latitude" id="latitude" onchange="savefinval(this)"></select>

			</td>

			<td><input name="latitudeparentid" id="latitudeparentid" class="hide"></td>
			<td><input type="text" value="" name="address.latitude" id="address.latitude"></td>
			</tr>

	</table>

		<table id="patientForm" name="patientForm">

		<tr>
  		<td>Preferred</td>
		<td>
		<input name="_addresses[0].preferred" type="hidden">
		<input name="addresse[0].preferred" onclick="if (preferredBoxClick) preferredBoxClick(this)" alt="personAddress" type="checkbox">
		</td>
		</tr>

		<tr>
			<th name="id10"></th>
			<td><input type="text" value="" name="addresse[0].address1" id="addresse[0].address1"></td>
		</tr>

		<tr>
			<th name="id11"></th>
			<td><input type="text" value="" name="addresse[0].address2" id="addresse[0].address2"></td>
		</tr>


		<tr>

		

			<th name="id12"></th>
			<td><select name="pfcountry" id="country" onChange="locationSelectedPform(this,1)">
				<option>Select</option>
				<option>Load</option>
				</select>
			</td>
			<td><input name=countryparentid id="countryparentid" class="hide"></td>
			<td><input type="text" value="" name="addresse[0].country" id="addresse[0].country"></td>

			</tr>

			

		<tr>

			<th name="id13"></th>
			<td><select name="pfstate" id="state" onChange="locationSelectedPform(this,2)">
				</select>
			</td>
			<td><input name="stateparentid" id="stateparentid" class="hide"></td>

			<td><input type="text" value="" name="addresse[0].stateProvince" id="addresse[0].stateProvince"></td>
			</tr>

			

		<tr>

			<th name="id14"></th>
			<td><select name="pfsublocation1" id="sublocation1" onChange="locationSelectedPform(this,3)">
				</select>
			</td>
			<td><input name="sublocation1parentid" id="sublocation1parentid" class="hide"></td>
			
			<td><input type="text" value="" name="addresse[0].countyDistrict" id="addresse[0].countyDistrict"></td>
			
			</tr>

			

		<tr>

			<th name="id15"></th>
			<td><select name="pfsublocation2" id="sublocation2" onChange="locationSelectedPform(this,4)">
				</select>
			</td>
			<td><input name="sublocation2parentid" id="sublocation2parentid" class="hide"></td>
			<td><input type="text" value="" name="addresse[0].region" id="addresse[0].region"></td>
			
			</tr>

			

		<tr>	

			<th name="id16"></th>
			<td><select name="pfsublocation3" id="sublocation3" onChange="locationSelectedPform(this,5)">
				</select>
			</td>
			<td><input name="sublocation3parentid" id="sublocation3parentid" class="hide"></td>
			<td><input type="text" value="" name="addresse[0].subregion" id="addresse[0].subregion"></td>
			</tr>

			

		<tr>

			<th name="id17"></th>
			<td><select name="pfsublocation4" id="sublocation4" onChange="locationSelectedPform(this,6)">
				</select>
			</td>
			<td><input name="sublocation4parentid" id="sublocation4parentid" class="hide"></td>
			<td><input type="text" value="" name="addresse[0].townshipDivision" id="addresse[0].townshipDivision"></td>
			</tr>

			

		<tr>

			<th name="id18"></th>
			<td><select name="pfsublocation5" id="sublocation5" onChange="locationSelectedPform(this,7)">
				</select>
			</td>
			<td><input name="sublocation5parentid" id="sublocation5parentid" class="hide"></td>
			<td><input type="text" value="" name="addresse[0].cityVillage" id="addresse[0].cityVillage"></td>
			
			</tr>

					

		<tr>	

			<th name="id19"></th>
			<td><select name="pfsublocation6" id="sublocation6" onChange="locationSelectedPform(this,8)">
				</select>
			</td>
			<td><input name="sublocation6parentid" id="sublocation6parentid" class="hide"></td>
			<td><input type="text" value="" name="addresse[0].neighborhoodCell" id="addresse[0].neighborhoodCell"></td>
			</tr>

			

		<tr>

			<th name="id20"></th>
			<td><select name="pfpostalcode" id="postalcode" onChange="locationSelectedPform(this,9)">
				</select>
			</td>
			<td><input name="postalcodeparentid" id="postalcodeparentid" class="hide"></td>
			<td><input type="text" value="" name="addresse[0].postalCode" id="addresse[0].postalCode"></td>
			</tr>

			

		<tr>	

			<th name="id21"></th>
			<td><select name="pflongitude" id="longitude" onChange="locationSelectedPform(this,10)">
				</select>
			</td>
			<td><input name="longitudeparentid" id="longitudeparentid" class="hide"></td>
			<td><input type="text" value="" name="addresse[0].longitude" id="addresse[0].longitude"></td>
			</tr>

			

		<tr>	

			<th name="id22"></th>
			<td><select name="pflatitude" id="latitude" onchange="savefinval(this)">
				</select>
			</td>
			<td><input name="latitudeparentid" id="latitudeparentid" class="hide"></td>
			<td><input type="text" value="" name="addresse[0].latitude" id="addresse[0].latitude"></td>
			</tr>

	</table>

<div class="tableWrapper" id="tableWrapper">
</div>




</body>



