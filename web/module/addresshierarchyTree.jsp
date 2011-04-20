<%@ include file="/WEB-INF/template/include.jsp"%>
<openmrs:require privilege="Manage Hierarchy" otherwise="/login.htm" redirect="/module/addresshierarchy/addresshierarchyTree.htm" />
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/dwr/interface/AddressHierarchy.js"/>
<openmrs:htmlInclude file="/moduleResources/addresshierarchy/jquery.js" />
<openmrs:htmlInclude file="/moduleResources/addresshierarchy/thickbox.js" />
<openmrs:htmlInclude file="/moduleResources/addresshierarchy/loadingAnimation.gif" />
<openmrs:htmlInclude file="/moduleResources/addresshierarchy/thickbox.css" />

<style>

.cusform {
	display:none;

}

.hid{
	display:none;
}

.dojoTree {

	-x-system-font:caption;

	font-family:-moz-use-system-font;

	font-size:14px;

	font-size-adjust:-moz-use-system-font;

	font-stretch:-moz-use-system-font;

	font-style:-moz-use-system-font;

	font-variant:-moz-use-system-font;

	font-weight:normal;

	line-height:-moz-use-system-font;

	overflow:auto;

}
</style>



<script  type="text/javascript">

	var djConfig = { isDebug: true };

</script>



<openmrs:htmlInclude file="/scripts/dojo/dojo.js"/>

  <script type="text/javascript">

	var arr;
	var comnode;

	function pullOptions(){//get the location types and fills the address type in the thick box
		AddressHierarchy.getLocationType(function (data){
			for (var i=0; i < data.length;++i){

				addOption(document.getElementById("addaddresstype"), data[i], i);
				}
			});
		}

	function addOption(selectbox,text,value )//adding options to the select box
	{
	var optn = document.createElement("OPTION");
	optn.text = text;
	optn.value = value;
	selectbox.options.add(optn);
	}

	 function added(){//function to add the elements to the tree and table
		   
		  var val = document.getElementById("ind").value;
		  for(var j=0;j<arr.length;j++){
			  if(val==arr[j])
				  var ind = j-1;
		  }  
		  var txt = document.getElementById("txt").value;
		  this.controller = dojo.widget.manager.getWidgetById("myTreeController");
		  if (!comnode.isFolder) {

		       comnode.setFolder();

		     }
		  var locationarr = txt.split("\n");
		  
		  var rad = this.controller;
		  for(var i=0;i<locationarr.length;i++){
			  if(locationarr[i]!=""){
			     AddressHierarchy.createLocation(locationarr[i],ind,comnode.locationId,function(data){

			 		var dumm = parseInt(data[2]);

			      	var titl = data[0]+"( "+arr[dumm]+" )";

			      	var res = rad.createChild(comnode, 0, { title: titl,locationName:data[0], locationId : data[1],typeId : data[2], parentId : data[3] });

			      });	  
		  }}
	 }

	 
	  function fireEvent(node,obj,evt){//dynamically making a link to click

		  var x = "Under "+node.locationName;
		  document.getElementById("desc").innerHTML=x;
		  var fireOnThis = obj;
		  if( document.createEvent ) {
		  var evObj = document.createEvent('MouseEvents');
		  evObj.initEvent( evt, true, false );
		  fireOnThis.dispatchEvent(evObj);
		  } else if( document.createEventObject ) {
		  fireOnThis.fireEvent(node,'on'+evt);
		  }
		  }   

 var TreeBuilder = {//main function which builds the tree

   buildTreeNodes:function (dataObjs, treeParentNode,arr){//building tree with the json data
	 for(var i=0; i<dataObjs.length;i++){

       	var typ = dataObjs[i].typeId;

     	var dumm = parseInt(typ);
     	
     	var titl = dataObjs[i].title+"( "+arr[dumm]+" )";	
     	
       	var node = dojo.widget.createWidget("TreeNode",{

        	 title:titl ,locationName:dataObjs[i].title, locationId:dataObjs[i].locationId , typeId:dataObjs[i].typeId , parentId:dataObjs[i].parentId 

       	});


    	treeParentNode.addChild(node);

        treeParentNode.registerChild(node,i);

        if(dataObjs[i].children){

         this.buildTreeNodes(dataObjs[i].children, node,arr);

       }

     }

   
     },

   buildTree:function (treeDat){

     myTreeWidget = dojo.widget.createWidget("Tree",{

       widgetId:"myNewTreeWidget"

//       DNDMode:"both",
       
//       DNDAcceptTypes:["myNewTreeWidget"]

     });

         TreeBuilder.buildTreeNodes(treeDat.treeNodes,myTreeWidget,arr);
     		
    

     var treeContainer = document.getElementById("myWidgetContainer");

     var placeHolder = document.getElementById("treePlaceHolder");

     treeContainer.replaceChild(myTreeWidget.domNode,placeHolder);

     DemoTreeManager.init();

     

   }

   

 };

 

 var TreeActions = {//context menu functions controllers

   addNewNode: function(parent,controllerId){//for adding nodes

     this.controller = dojo.widget.manager.getWidgetById(controllerId);

     if (!parent.isFolder) {

       parent.setFolder();

     }

     var arr=new Array("Country - 1","State - 2","Sub Location1 - 3","Sub Location2 - 4","Sub Location3 - 5","Sub Location4 - 6","Sub Location5 - 7","Sub Location6 - 8","Postal Code - 9","Longitude - 10","Latitude - 11");

	 var typeid = parent.typeId;

	 var str="Enter corresponding code for the location type \n";

	 for(var i=typeid;i<arr.length;i++){

	 	str = str + arr[i] + "\n";

	 }

	 var dummy = prompt(str,"");

     

     

	 if(dummy>typeid){

     if(dummy<12){

     var titl = prompt("Enter the location name","");

     if(titl==""){

     	alert("Enter valid name");

     }

     else{

     var rad = this.controller;

     AddressHierarchy.createLocation(titl,parseInt(dummy)-1,parent.locationId,function(data){

		var dumm = parseInt(data[2]);

     	var titl = data[0]+"( "+arr[dumm]+" )";

     	var res = rad.createChild(parent, 0, { title: titl,locationName:data[0], locationId : data[1],typeId : data[2], parentId : data[3] });

     })

     

     }}

     else{

     	alert("Invalid location type");

     }

  	 }

  	 else{

  	 	alert("Invalid location type");

  	 }

     

   },

   removeNode: function(node,controllerId){//for removing nodes

   	if(node.locationName!="Start"){

     if (!node) {

       alert("Nothing selected to delete");

       return false;

     }

     else{

     var name = node.locationName;

     var parid = node.parentId;

     this.controller = dojo.widget.manager.getWidgetById(controllerId);

     var rad = this.controller;

     if(!confirm("Are you sure you want to delete "+name)){

     	return false;

     }

     AddressHierarchy.deleteLocation(parid,name,function() {

     	var res = rad.removeNode(node, dojo.lang.hitch(this));

     })

     

     }

   }

	   else
		   alert("Cannot remove");



   },

   editNode: function(node,controllerId){//for editing the nodes  

   	if(node.locationName!="Start"){

   	if (!node) {

       alert("Nothing selected to edit");

       return false;

     }

     else{

     var oldname = node.locationName;

     var parid = node.parentId;

     this.controller = dojo.widget.manager.getWidgetById(controllerId);

     var rad = this.controller;

     var newname = prompt("Enter the location name",oldname);

     

     if(newname!=null){

     AddressHierarchy.editLocation(parid,oldname,newname, function(){

     	
		var dumm = parseInt(node.typeId);

     	var titl = newname+"( "+arr[dumm]+" )";

     	node.edit({title:titl , locationName:newname});

     })}

     }

   }

   else{

   	alert("Cannot Edit");

   }

 }};

 

 var DemoTreeManager = {//function which adds the context menu to the tree

   djWdgt: null,

   myTreeWidget: null,

   addTreeContextMenu: function(){

   	 

     var ctxMenu = this.djWdgt.createWidget("TreeContextMenu",{});

     ctxMenu.addChild(this.djWdgt.createWidget(

       "TreeMenuItem",{caption:"Add Location Component",

         widgetId:"ctxAdd"}));

     ctxMenu.addChild(this.djWdgt.createWidget(

       "TreeMenuItem",{caption:"Edit Location Component",

         widgetId:"ctxEdit"}));

     ctxMenu.addChild(this.djWdgt.createWidget(

       "TreeMenuItem",{caption:"Delete Location Component",

         widgetId:"ctxDelete"}));

     document.body.appendChild(ctxMenu.domNode);

     /* Bind the context menu to the tree */

     ctxMenu.listenTree(this.myTreeWidget);

   },

 

 addController: function(){

     this.djWdgt.createWidget(

       "TreeBasicController",

       {widgetId:"myTreeController",DNDController:"create"}

     );

   },

   bindEvents: function(){

     /* Bind the functions in the TreeActions object to the

        context menu entries */

     dojo.event.topic.subscribe("ctxAdd/engage",

       function (menuItem) { 

    	 this.comnode=menuItem.getTreeNode();
    	 mynode = menuItem.getTreeNode();
    	 typeid = mynode.typeId;
    	 
    	 var i;
    	 var selectbox = document.getElementById("addaddresstype"); 
    	 for(i=selectbox.options.length-1;i>=0;i--)
    	 {
    	 selectbox.remove(i);
    	 }
    	 document.getElementById("ind").value="";
    	 AddressHierarchy.getAddressHierarchyTypeList(typeid,function(data){
            var selectbox = document.getElementById("addaddresstype");
			var optn = document.createElement("OPTION");
			for (i=0; i < data.length;++i){
 				var optn = document.createElement("OPTION");
  				optn.text = data[i];
  				optn.value = data[i];
  				if(i==0)
  					document.getElementById("ind").value = data[i];
  				selectbox.options.add(optn);
  			}

 			fireEvent(menuItem.getTreeNode(),document.getElementById("linkit"),'click');
  			
        	});
    	   
		   
    	   
}

     );

     dojo.event.topic.subscribe("ctxDelete/engage",

       function (menuItem) { TreeActions.removeNode(menuItem.getTreeNode(),

         "myTreeController"); }

     );

     dojo.event.topic.subscribe("ctxEdit/engage",

       function (menuItem) { TreeActions.editNode(menuItem.getTreeNode(),

         "myTreeController"); }

     );

   },

   init: function(){

     /* Initialize this object */

     this.djWdgt = dojo.widget;

     this.myTreeWidget = this.djWdgt.manager.getWidgetById("myNewTreeWidget");

     this.addTreeContextMenu();

     this.addController();

     this.bindEvents();

   }

 };

 

 

 function loadData(){//function gets the json data from the servlet

    $.getJSON("${pageContext.request.contextPath}/moduleServlet/addresshierarchy/addressTree",//getting json data from servlet

        function(data){

          TreeBuilder.buildTree(data);

        });

  }

 

 

 dojo.addOnLoad( function(){//functions to load at the page start
	 AddressHierarchy.getAddressHierarchyTypeList(0,function(dataa){
         var list=["Start"];
         arr=list.concat(dataa);
	 });

   dojo.require("dojo.lang.*");

   dojo.require("dojo.widget.Tree");

   dojo.require("dojo.io.*");

   dojo.require("dojo.widget.TreeContextMenu");

   loadData();

   pullOptions();

   

   });

</script>



<body>
<input type="submit" value="Reload Tree" onclick="window.location.reload()" />
<br>
<br>
  <div  id="myWidgetContainer">

   <span id="treePlaceHolder"

      style="background-color:#F00; color:#FFF;">

     Loading...

   </span>

 </div>

<a href="#TB_inline?height=500&width=500&inlineId=addFormField" title="Add Locations" class="thickbox" id="linkit"></a>
<div class="cusform" id="addFormField">
<table>
<tr>
<th>Address Type</th>
<td>
<select name="addaddresstype" id="addaddresstype" onchange='document.getElementById("ind").value = this.value;'>
<option>Select one</option>
</select>
<label id="desc"></label></td>
</tr>
<tr>
<th>Enter the Location Names</th>
<td>
<textarea name="addText" id="addText" rows="25" cols="40" onblur='document.getElementById("txt").value = this.value;'></textarea>
</td>
</tr>
<tr>
<td>
<input type="submit" value="Add" onclick='added()'/>
</td>
</tr>
</table>
</div>
<input type="text" id="nodeval" class="hid"/>
<input type="text" id="ind" class="hid" />
<input type="text" id="txt" class="hid" />

</body>
