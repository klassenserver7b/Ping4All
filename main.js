var dict = {};
var baseurl ="http://127.0.0.1:120";
var timelimit ="700";

function refreshList(causedBy) {
    resetStyle();
    var url = baseurl+"/updateWebsiteList";

    fetch(url).then(result => {

        if (result.status == 200) {
            var list = document.getElementById("websiteselect");
            list.innerHTML = "";
            dict = {};
            result.json().then(res => {

                var count = 0;

                res.sites.forEach(site => {

                    dict[site.url] = site.enabled
                    var optn = document.createElement("option");
                    optn.className = "col-lg";
                    optn.innerText = site.url;
                    optn.focus = "checkactivateButton()";
                    if (site.enabled == true) {
                        optn.style.backgroundColor = "#93faa5";
                    } else {
                        optn.style.backgroundColor = "#fe7968";
                    }

                    optn.addEventListener("click", e => {
                        checkactivateButton();
                        resetStyle();
                    })

                    document.getElementById("websiteselect").appendChild(optn);
                    count++;


                })
                if (count <= 1) {
                    list.size = 2
                } else if (count > 10) {
                    list.size = 10;
                } else {
                    list.size = count;
                }
            });
        }


    });
}

refreshList();

function checkactivateButton() {

    var website = document.getElementById("websiteselect").value;
    if (dict[website] == true) {

        var but = document.getElementById("enabler");

        if (but != null) {
            changebuttonstyles();
        }

    } else if (dict[website] == false) {

        var but = document.getElementById("disabler");

        if (but != null) {
            changebuttonstyles();
        }
    }

}

function submitWebsite() {
    resetStyle();

    var website = document.getElementById("input").value;
    var url = baseurl + "/addWebsite";

    if(website=="kopf-tisch.de"){
        runhead();
    }

    fetch(url, {
        method: "POST",
        body: "add:" + website,
    }).then(result => {

        var box = document.getElementById("activatemessage");
        box.style.width = "200px";

        if (result.status != 200) {

            if (result.status == 400 || result.status == 500) {

                box.style.color = "red";

                result.text().then(body => box.innerText = body);
                document.getElementById("input").style.borderColor = "#d91e18";

            } else {
                box.style.color = "red";
                box.innerText = "Daten konnten nicht übermittelt werden! <br> bitte versuchen sie es später erneut";

            }

        } else {

            box.style.color = "green";
            box.innerText = "Website successfull added!";
            document.getElementById("input").value = "";
            document.getElementById("input").style.borderColor = "darkgrey";

            refreshList("addition");

        }
    })


}

function startRemove() {
    resetStyle();

    var selectedremove = document.getElementById("websiteselect").value;

    if (!(selectedremove == null || selectedremove == "")) {

        if (window.confirm("Möchtest du wirklich \"" + selectedremove + "\" aus der Ping-Liste löschen?")) {
            removeWebsite();
        }

    } else {
        var box = document.getElementById("deactivateMessage");
        var select = document.getElementById("websiteselect")
        box.style.width = "300px";
        box.style.color = "red";
        select.style.borderStyle = "solid"
        select.style.borderColor = "#d91e18";
        box.innerText = "Bitte wähle zuerst eine Website aus der Liste aus!";
    }

};

function removeWebsite() {

    var website = document.getElementById("websiteselect").value;
    resetStyle();

    var url = baseurl + "/removeWebsite";

    fetch(url, {
        method: "POST",
        body: "remove:" + website,

    }).then(result => {

        var box = document.getElementById("deactivateMessage");
        box.style.width = "300px";

        if (result.status != 200) {

            if (result.status == 400 || result.status == 500) {

                box.style.color = "red";
                var message;
                result.text().then(body => box.innerText = body);

            } else {
                box.style.color = "red";
                box.innerText = "Daten konnten nicht übermittelt werden! <br> bitte versuchen sie es später erneut";

            }
        } else {

            box.style.color = "green";
            box.innerText = "Website successfull removed!";
            var select = document.getElementById("websiteselect")
            select.style.borderColor = "green";

            refreshList("removal");

        }
    })

}

function resetStyle() {
    const activatebox = document.getElementById("activatemessage");
    const deactivatebox = document.getElementById("deactivateMessage");
    activatebox.innerHTML = "";
    activatebox.style.width = "0px";
    activatebox.style.color = "#ffffff";
    activatebox.style.textAlign = "center";
    activatebox.style.marginTop = "5px"
    activatebox.style.fontSize = "12px";
    activatebox.style.borderStyle = "none";
    activatebox.style.borderColor = "darkgray"

    deactivatebox.innerHTML = "";
    deactivatebox.style.width = "0px";
    deactivatebox.style.color = "#ffffff";
    deactivatebox.style.textAlign = "center";
    deactivatebox.style.marginTop = "5px"
    deactivatebox.style.fontSize = "12px";
    deactivatebox.style.borderStyle = "none";
    deactivatebox.style.borderColor = "darkgray"

    document.getElementById("input").style.borderColor = "darkgrey";
    document.getElementById("websiteselect").style.borderColor = "darkgrey";
}

function changebuttonstyles() {
    resetStyle();
    var but = document.getElementById("enabler");

    if (but != null) {
        but.innerText = "Deaktivieren";
        but.id = "disabler";

    } else {
        but = document.getElementById("disabler");
        but.innerText = "Aktivieren";
        but.id = "enabler";
    }

}


function sendEnableRequest() {

    var but = document.getElementById("enabler");
    var enable;

    if (but != null) {
        enable = "activate&%&";
    } else {
        but = null;
        but = document.getElementById("disabler");
        if (but != null) {
            enable = "deactivate&%&";
        }
    }

    const url = baseurl + "/activate"
    var website = document.getElementById("websiteselect").value

    if (!(website == null || website == "")) {

        if (enable != null) {

            fetch(url, {

                method: "POST",
                body: enable + website

            }).then(result => {

                var box = document.getElementById("deactivateMessage");
                box.style.width = "300px";

                result.text().then(message => {

                    if (result.status != 200) {

                        if (result.status == 400) {

                            box.style.color = "red";
                            box.innerText = message;

                        } else {

                            box.style.color = "red";
                            box.innerText = "Daten konnten nicht übermittelt werden! <br> bitte versuchen sie es später erneut";

                        }
                    } else {

                        box.style.color = "green";
                        box.innerText = message;
                        var select = document.getElementById("websiteselect")
                        select.style.borderColor = "green";

                    }

                });

            })

            refreshList("activationchange");
        }

    } else {

        var box = document.getElementById("deactivateMessage");
        box.style.width = "300px";
        box.style.color = "red";
        box.innerText = "Bitte wähle zuerst eine Website aus der Liste aus!";

    }

}

function loadlogs() {

    var website = document.getElementById("websiteselect").value;

    if (!(website == null || website == "")) {

        var url = baseurl + "/getlogs";
        fetch(url, {
            method: "POST",
            body: "getlogs:" + website
        }).then(result => {

            if (result.status == 200) {

                result.json().then(res => {

                    var logtable = document.getElementById("logs");
                    if (logtable != null) {
                        document.getElementById("logdiv").removeChild(logtable);
                    }

                    var table = document.createElement("table");
                    table.id = "logs";
                    table.className = "logtable";

                    var headrow = document.createElement("tr");

                    var namehead = document.createElement("td");
                    namehead.innerText = "Name der Website";
                    namehead.className = "logdata";

                    var timehead = document.createElement("td");
                    timehead.innerText = "Zeitstempel";
                    timehead.className = "logdata";

                    var successhead = document.createElement("td");
                    successhead.innerText = "Erfolgreich?";
                    successhead.className = "logdata";

                    var loghead = document.createElement("td");
                    loghead.innerText = "Log";
                    loghead.className = "logdata";

                    headrow.appendChild(namehead);
                    headrow.appendChild(timehead);
                    headrow.appendChild(successhead);
                    headrow.appendChild(loghead);
                    table.appendChild(headrow);

                    res.forEach(site => {
                        var row = document.createElement("tr");

                        var name = document.createElement("td");
                        name.innerText = site.name;
                        name.className = "logdata col-lg-2";

                        var time = document.createElement("td");
                        time.innerText = site.time;
                        time.className = "logdata col-lg-2";

                        if(site.name == "microsoft.com"){
                            name.style.backgroundColor="#1e90ff";
                            time.style.backgroundColor="#1e90ff";
                        }else if(site.name=="kopf-tisch.de"){
                            runhead();
                        }

                        var success = document.createElement("td");

                        if(site.name =="getdigital.de"){
                            success.addEventListener("click", e =>{
                                getdigital();
                            })
                        }

                        success.innerText = site.success;
                        success.className = "logdata col-lg-1";
                        if (site.success == true) {
                            if(site.ping >= timelimit){
                                success.style.backgroundColor = "lightgreen";
                            }else{
                                success.style.backgroundColor = "#2ecc71";
                            }
                        } else {
                            success.style.backgroundColor = "#f03434";
                        }
                        success.datatoggle = "tooltip";
                        success.dataplacement="bottom";
                        success.title=site.log;
                        
                        var log = document.createElement("td");
                        log.innerText = site.log;
                        if(site.success == true){
                            if (site.ping >= timelimit){
                                log.style.backgroundColor="#fcd670";
                            }else{
                                log.style.backgroundColor="lightgreen";
                            }
                        }else{
                            log.style.backgroundColor="#fe7968"
                        }
                        log.className = "logdata col-lg-4";

                        row.appendChild(name);
                        row.appendChild(time);
                        row.appendChild(success);
                        row.appendChild(log);

                        table.appendChild(row);

                    })

                    document.getElementById("logdiv").appendChild(table);
                });
            } else {

                var box = document.getElementById("deactivateMessage");

                if (result.status == 400 || result.status == 500) {

                    var logtable = document.getElementById("logs");
                    if (logtable != null) {
                        document.getElementById("logdiv").removeChild(logtable);
                    }

                    box.style.color = "red";
                    box.style.width = "400px";
                    box.style.textAlign = "left";
                    box.style.marginTop = "5px"
                    box.style.fontSize = "15";
                    box.style.borderStyle = "solid";
                    box.style.borderColor = "red";
                    result.text().then(body => box.innerText = body);

                } else {
                    box.style.color = "red";
                    box.innerText = "Daten konnten nicht übermittelt werden! <br> bitte versuchen sie es später erneut";

                }

            }
        });

    } else {

        var box = document.getElementById("deactivateMessage");
        box.style.color = "red";
        box.style.width = "300px";
        box.style.textAlign = "left";
        box.style.marginTop = "5px"
        box.style.fontSize = "15"
        document.getElementById("websiteselect").style.borderColor = "red";
        box.innerText = "Bitte wähle zuerst eine Website aus der Liste aus!";

    }

}

function getdigital(){
    var img = document.createElement("img");
    img.src="res/bg.png";
    img.style.width = "1px";
    img.style.height = "1px";
    document.getElementById("body").appendChild(img);

   if(img.requestFullscreen){
       img.requestFullscreen();
   }else if(img.webkitRequestFullscreen){
        img.webkitRequestFullscreen();
   }
    //window.open("res/bg.png", "BRING KAFFEE!", "nix großes so dies das halt so.");
}

function runhead(){
    var containdiv = document.createElement("div");
    containdiv.className="banner";
    var marq = document.createElement("div");
    marq.className="marq";
    marq.innerText="This website ist used to scan websites!";
    containdiv.appendChild(marq);
    document.getElementById.appendChild(containdiv);

}