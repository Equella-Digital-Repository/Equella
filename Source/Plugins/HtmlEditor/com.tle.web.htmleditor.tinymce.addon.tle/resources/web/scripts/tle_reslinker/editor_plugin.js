(function(){tinymce.create("tinymce.plugins.TleResourceLinkerPlugin",{init:function(a,b){a.addCommand("mceTleResourceLinker",function(){a.windowManager.bookmark=a.selection.getBookmark("simple");a.windowManager.open({file:baseActionUrl+"select_link",width:810,height:600,inline:1,scroll:true},{plugin_url:b,some_custom_arg:"custom arg"})});a.addButton("tle_reslinker",{title:"Select a resource from EQUELLA",cmd:"mceTleResourceLinker",image:b+"/images/equellabutton.gif"})},createControl:function(b,a){return null},getInfo:function(){return{longname:"Tle Resource Linker plugin",author:"TLEI",authorurl:"http://thelearningedge.com.au",infourl:"http://thelearningedge.com.au",version:"5.0"}}});tinymce.PluginManager.add("tle_reslinker",tinymce.plugins.TleResourceLinkerPlugin)})();