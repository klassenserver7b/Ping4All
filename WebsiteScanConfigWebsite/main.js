var dict = {};

function refreshList(causedBy) {
    var url = "http://127.0.0.1:80/updateWebsiteList";

    fetch(url, {
        method: "POST",
        body: causedBy
    }).then(result => {

        if (result.status == 200) {
            var list = document.getElementById("websiteselect");
            list.innerHTML = "";
            dict = {};
            result.json().then(res => {

                var count = 0;

                res.sites.forEach(site => {

                    dict[site.url] = site.enabled
                    var optn = document.createElement("option");
                    optn.className = "ddoption";
                    optn.innerHTML = site.url;
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
    var url = "http://127.0.0.1:80/addWebsite";

    fetch(url, {
        method: "POST",
        body: "add:" + website,
    }).then(result => {

        var box = document.getElementById("activatemessage");
        box.style.width = "200px";

        if (result.status != 200) {

            if (result.status == 400 || result.status == 500) {

                box.style.color = "red";

                result.text().then(body => box.innerHTML = body);
                document.getElementById("input").style.borderColor = "#d91e18";

            } else {
                box.style.color = "red";
                box.innerHTML = "Daten konnten nicht übermittelt werden! <br> bitte versuchen sie es später erneut";

            }

        } else {

            box.style.color = "green";
            box.innerHTML = "Website successfull added!";
            document.getElementById("input").value = "";
            document.getElementById("input").style.borderColor = "darkgrey";

            refreshList("addition");

        }
    })


}

function startRemove() {
    resetStyle();

    var selectedremove = document.getElementById("websiteselect").value;

    if (selectedremove != null) {

        if (window.confirm("Möchtest du wirklich \"" + selectedremove + "\" aus der Ping-Liste löschen?")) {
            removeWebsite();
        }

    } else {
        var box = document.getElementById("deactivateMessage");
        var select = document.getElementById("websiteselect")
        box.style.width = "200px";
        box.style.color = "red";
        select.style.borderStyle = "solid"
        select.style.borderColor = "#d91e18";
        box.innerHTML = "Bitte wähle erst eine Website aus der Ping-Liste aus!";
    }

};

function removeWebsite() {

    var website = document.getElementById("websiteselect").value;
    resetStyle();

    var url = "http://127.0.0.1:80/removeWebsite";

    fetch(url, {
        method: "POST",
        body: "remove:" + website,

    }).then(result => {

        var box = document.getElementById("deactivateMessage");
        box.style.width = "200px";

        if (result.status != 200) {

            if (result.status == 400 || result.status == 500) {

                box.style.color = "red";
                var message;
                result.text().then(body => box.innerHTML = body);

            } else {
                box.style.color = "red";
                box.innerHTML = "Daten konnten nicht übermittelt werden! <br> bitte versuchen sie es später erneut";

            }
        } else {

            box.style.color = "green";
            box.innerHTML = "Website successfull removed!";
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
        but.innerHTML = "Deaktivieren";
        but.id = "disabler";

    } else {
        but = document.getElementById("disabler");
        but.innerHTML = "Aktivieren";
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

    const url = "http://127.0.0.1:80/activate"
    var website = document.getElementById("websiteselect").value

    if (!(website == null || website == "")) {

        if (enable != null) {

            fetch(url, {

                method: "POST",
                body: enable + website

            }).then(result => {

                var box = document.getElementById("deactivateMessage");
                box.style.width = "200px";

                result.text().then(message => {

                    if (result.status != 200) {

                        if (result.status == 400) {

                            box.style.color = "red";
                            box.innerHTML = message;

                        } else {

                            box.style.color = "red";
                            box.innerHTML = "Daten konnten nicht übermittelt werden! <br> bitte versuchen sie es später erneut";

                        }
                    } else {

                        box.style.color = "green";
                        box.innerHTML = message;
                        var select = document.getElementById("websiteselect")
                        select.style.borderColor = "green";

                    }

                });

            })

            refreshList("activationchange");
        }

    } else {

        var box = document.getElementById("deactivateMessage");
        box.style.width = "200px";
        box.style.color = "red";
        box.innerHTML = "Bitte wähle zuerst eine Website aus der Liste aus!";

    }

}

function loadlogs() {

    var website = document.getElementById("websiteselect").value;

    if (!(website == null || website == "")) {

        var url = "http://127.0.0.1:80/getlogs";
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

                    var head = document.createElement("tr");

                    var namehead = document.createElement("td");
                    namehead.innerHTML = "Name der Website";
                    namehead.className = "logdata";

                    var timehead = document.createElement("td");
                    timehead.innerHTML = "Zeitstempel";
                    timehead.className = "logdata";

                    var successhead = document.createElement("td");
                    successhead.innerHTML = "Erfolgreich?";
                    successhead.className = "logdata";

                    var loghead = document.createElement("td");
                    loghead.innerHTML = "Log";
                    loghead.className = "logdata";

                    head.appendChild(namehead);
                    head.appendChild(timehead);
                    head.appendChild(successhead);
                    head.appendChild(loghead);
                    table.appendChild(head);

                    res.forEach(site => {
                        var row = document.createElement("tr");

                        var name = document.createElement("td");
                        name.innerHTML = site.name;
                        name.className = "logdata";

                        var time = document.createElement("td");
                        time.innerHTML = site.time;
                        time.className = "logdata";

                        var success = document.createElement("td");
                        success.innerHTML = site.success;
                        success.className = "logdata";
                        if (site.success == true) {
                            success.style.backgroundColor = "green";
                        } else {
                            success.style.backgroundColor = "red";
                        }

                        var log = document.createElement("td");
                        log.innerHTML = site.log;
                        log.className = "logdata";

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
                    result.text().then(body => box.innerHTML = body);

                } else {
                    box.style.color = "red";
                    box.innerHTML = "Daten konnten nicht übermittelt werden! <br> bitte versuchen sie es später erneut";

                }

            }
        });

    } else {

        var box = document.getElementById("deactivateMessage");
        box.style.color = "red";
        box.style.width = "400px";
        box.style.textAlign = "left";
        box.style.marginTop = "5px"
        box.style.fontSize = "15"
        document.getElementById("websiteselect").style.borderColor = "red";
        box.innerHTML = "Bitte wähle zuerst eine Website aus der Liste aus!";

    }

}