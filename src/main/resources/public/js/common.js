$(document).ready(function(){

});

function post(url, data, callback, errorcall) {
    $.ajax({
        type: "POST",
        url: url,
        async: true,
        contentType: "application/json; charset=utf-8",
        data: JSON.stringify(data),
        dataType: "json",
        success: callback,
        error:errorcall,
        beforeSend:function(XMLHttpRequest){
            show_loading();
        },
        complete:function(XMLHttpRequest){
            hide_loading();
        }
    });
}


function get(url, callback) {
    $.ajax({
        type: "GET",
        url: url,
        async: true,
        success: callback,
        beforeSend:function(XMLHttpRequest){
            show_loading();
        },
        complete:function(XMLHttpRequest){
            hide_loading();
        }
    });
}

function show_loading() {
    var con = '<div class="loading" id="page-loading">' +
        '<div class="gif"></div>' +
        '</div>';
    $(document.body).append(con);
}

function alarm(msg){
    $("#alert-model-content").text( msg );
    $("#alert-model").modal('show');
}

function hide_loading() {
    $("#page-loading").remove();
}


$(".jsurl").click(function(){
    var url = $(this).data("url");
    var zk = getUrlParam('zk');
    window.location.href= url + "?zk=" + zk;
});


function getUrlParam(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); //构造一个含有目标参数的正则表达式对象
    var r = window.location.search.substr(1).match(reg);  //匹配目标参数
    if (r != null) return unescape(r[2]).trim();
    return; //返回参数值
}


String.prototype.format = function(args) {
    var result = this;
    if (arguments.length > 0) {
        if (arguments.length == 1 && typeof (args) == "object") {
            for (var key in args) {
                if(args[key]!=undefined){
                    var reg = new RegExp("({" + key + "})", "g");
                    result = result.replace(reg, args[key]);
                }
            }
        }
        else {
            for (var i = 0; i < arguments.length; i++) {
                if (arguments[i] != undefined) {
                    var reg = new RegExp("({[" + i + "]})", "g");
                    result = result.replace(reg, arguments[i]);
                }
            }
        }
    }
    return result;
}