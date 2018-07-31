/**
 * Created by lzz on 2018/1/16.
 */

$("#start-consumer").click(function () {
    var data = {};
    data.zk_address = $("input[name='zk-address']").val();
    data.topic = $("input[name='topic']").val();
    data.consumer_partition = $("input[name='consumer-partition']").val();
    data.runtime = $("input[name='runtime']").val();
    if( !data.runtime ){
        data.runtime = 10;
    }
    connect( JSON.stringify(data) );
});

$("#stop-consumer").click(function () {
    disconnect();
});

$("#topic-list").click(function () {
    $('#topic-list-model').modal('show');
});

$("#create-topic").click(function () {
    var data = {};
    data.zk_address = $("input[name='zk-address']").val();
    data.topic = $("input[name='topic']").val();
    data.partition = $("input[name='partition']").val();
    data.replication = $("input[name='replication']").val();
    post("/create-topic", data, function (res) {
        console.log(res);
    })
});

$("#append-msg").click(function () {
    var data = {};
    data.topic = $("input[name='topic']").val();
    data.msg = $("[name='producer-msg']").val();
    post("/producer-msg", data, function (res) {
        console.log(res);
    })
});

function post(url, data, callback) {
    $.ajax({
        type: "POST",
        url: url,
        contentType: "application/json; charset=utf-8",
        data: JSON.stringify(data),
        dataType: "json",
        success: callback
    });
}


document.onkeydown=function(e){
    var data = {};
    data.topic = $("input[name='topic']").val();
    if(e.keyCode == 13){
        var message = document.getElementById("producer-text").value;
        var msg = message.split("\n");
        data.msg = msg[ msg.length - 2 ];
        post("/producer-msg", data, function (res) {
            console.log(res);
        })
    }
}