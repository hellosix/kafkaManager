
$(document).ready(function(){
    get("/consumer-list", function(res){
        var list = res.result;
        for(var i = 0; i < list.length; i++){
            var table_str = "<tr>";
            table_str += "<td> <span class='glyphicon glyphicon-record stat-success'></span> </td>";
            table_str += "<td>" +  list[i].zookeeper + "</td>";
            table_str += "<td>" +  list[i].groupId + "</td>";
            table_str += "<td>" +  list[i].topic + "</td>";
            table_str += "<td>" +  list[i].msgNum + "</td>";
            table_str += "<td>" +  list[i].consumerNum + "</td>";
            table_str += '<td> <div class="btn-group btn-group-xs" role="group" aria-label="Extra-small button group">' +
                         '<button type="button" class="btn btn-success">start</button>' +
                         '<button type="button" class="btn btn-warning">stop</button>' +
                         '<button type="button" class="btn btn-danger">delete</button>' +
                         '</div> ' +
                         '</td>';
            table_str += "</tr>";
            $("#consumer-detail-list").append( table_str );
        }
    });
});
