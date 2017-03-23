

function populateNavBar() {
    function getLinksAndNames() {
        return new Promise(function (fulfill, reject) {
            $.ajax({
                url: '/api/settings',
                type: 'GET',
            }).done(function (settings) {
                var regex = /external.links:(.+)/;
                if (regex.test(settings)) {                    
                    var values = regex.exec(settings)[1].trim().split(";");
                    var links = {};
                    if (values.length == 0){
                        reject()
                    }
                    values.forEach(function (value) {
                        links[value.split("=")[0]] = value.split("=")[1];
                    });
                    fulfill(links);
                } else {
                    reject();
                }
            });

        });

    }

    getLinksAndNames()
        .then(function (links) {
            if ("undefined" === typeof links) {
                return;
            }            
            var nav = $('#navbar');
            for (name in links) {
                var option = $("<li>").attr("id", name).append($("<a>").text(name).attr("href", "generic.html?name=" + name + "&link=" + links[name]));
                nav.append(option);
            }
        })
        .catch(e => console.log("No external links were defined"));

}