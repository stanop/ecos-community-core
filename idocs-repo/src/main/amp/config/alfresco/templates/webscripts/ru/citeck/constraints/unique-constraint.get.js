// '@dms\\:KPP: "' + args.kpp + '"' OR @dms\\:INN: "' + args.inn + '"'

var sysuuid = '';
if(args.uuid != null && args.uuid != undefined && args.uuid.length > 0){
    sysuuid = ' AND NOT @sys\\:node-uuid:"'+args.uuid+'"'
}
var type = args.field
type = type.replace('_',':')
var pref = type.split(':')[0]
var suf= type.split(':')[1]

var nodes = search.luceneSearch('@'+pref+'\\:'+suf+':"'+args.value.replace(/[\\"]/g, "\\$0") +'"' + sysuuid );
if (nodes.length == 0 || nodes == undefined) {
    status.message = "0";
} else {
    status.message = "-1";
}
