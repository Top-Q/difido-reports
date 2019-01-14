/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function populateChildren(source, destination) {
    $(source).each(function() {
        switch (this.type) {
            case "scenario" :
                var children = new Array();
                destination.push({'text': this.name, 'icon': suiteIcon(this.status), 'children': children, 'type': this.status , 'a_attr':{'class' : 's_' + this.status +"_text"}});
                populateChildren(this.children, children);
                break;
            case "test":
                if (isTestShownInHtml(this)){
                    destination.push({'text': this.index + ". " + this.name, 'icon': testIcon(this.status), 'rel': this.status, 'type': this.status, 'a_attr': {'href': "tests/test_" + this.uid+"/test.html",'class':'s_' + this.status + "_text"}});
                }                
                break;
        }
    });
}

function testIcon(status) {
    switch (status) {
        case "success":
            return "images/testok.gif";
        case "failure":
            return "images/testfail.gif";
        case "error":
            return "images/testerr.gif";
        case "warning":
            return "images/testwarning.gif";
        case "inProgress":
            return "images/testrun.gif";
    }
}



function suiteIcon(status) {
    switch (status) {
        case "success":
            return "images/tsuiteok.gif";
        case "failure":
            return "images/tsuitefail.gif";
        case "error":
            return "images/tsuiteerror.gif";
        case "warning":
            return "images/tsuiteWarning.gif";
    }
}

function treeController(element) {
    var json = execution;
    var tree = {'text': 'Execution', 'icon': 'images/play_icon.png', 'children': []};
    $(json.machines).each(function(machineIndex) {
        tree.children.push({'text': this.name, icon: 'images/machine.png', 'children': [], 'state': {'opened': true, 'selected': true}});
        $(this.children).each(function(scenarioIndex) {
            var children = new Array();
            tree.children[machineIndex].children[scenarioIndex] = {'text': this.name, icon: suiteIcon(this.status), 'children': children};
            populateChildren(this.children, children);
        });


    });
    core = {'core': {'data': [tree]}};
    core.plugins = ['search', 'state', 'types'];
    core.types = {'valid_children': 
                ['success'],
        'types':
                {'success':
                            {'icon':
                                        {'image': './images/jsystem_ico.gif'}
                            }
                }
    };
    $(element).jstree(core);
//    return result;
}
//
//'data': [
//                                    {   'text': 'JSystem',
//                                        'icon': 'images/jsystem_ico.gif',
//                                        'children': [
//                                            {
//                                                'text': '127.0.0.1',
//                                                'icon': 'images/machine.png',
//                                                'state': {
//                                                    'opened': true,
//                                                    'selected': true
//                                                },
//                                                'children': [
//                                                    {'text': 'scenario',
//                                                        'icon': 'images/tsuitefail.gif',
//                                                        'children': [
//                                                            {
//                                                                'url': "testOne345353.html",
//                                                                'text': '1. TestOne',
//                                                                'icon': 'images/testok.gif',
//                                                                'a_attr': {'href': "testOne345353.html"}
//                                                            },
//                                                            {
//                                                                'url': "testOne345353.html",
//                                                                'text': '2. TestTwo',
//                                                                'icon': 'images/testwarning.gif',
//                                                                'a_attr': {'href': "testOne345353.html"}
//                                                            },
//                                                            {
//                                                                'url': "testOne345353.html",
//                                                                'text': '3. TestThree',
//                                                                'icon': 'images/testerr.gif',
//                                                                'a_attr': {'href': "testOne345353.html"}
//                                                            },
//                                                            {
//                                                                'text': "scenario",
//                                                                'icon': 'images/tsuitefail.gif',
//                                                                'children': [{
//                                                                        'url': "testOne345353.html",
//                                                                        'text': '3. TestThree',
//                                                                        'icon': 'images/testerr.gif',
//                                                                        'a_attr': {'href': "testOne345353.html"}
//                                                                    }
//                                                                ]
//
//                                                            }
//                                                        ]},
//                                                    {'text': '4. testTwo'
//                                                        , 'icon': 'images/testok.gif'},
//                                                    {'text': '5. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '6. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '7. testTwo', 'icon': 'images/testwarning.gif'},
//                                                    {'text': '8. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '9. testTwo', 'icon': 'images/testfail.gif'},
//                                                    {'text': '10. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '11. testTwo', 'icon': 'images/testrun.gif'},
//                                                    {'text': '4. testTwo'
//                                                        , 'icon': 'images/testok.gif'},
//                                                    {'text': '5. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '6. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '7. testTwo', 'icon': 'images/testwarning.gif'},
//                                                    {'text': '8. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '9. testTwo', 'icon': 'images/testfail.gif'},
//                                                    {'text': '10. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '11. testTwo', 'icon': 'images/testrun.gif'},
//                                                    {'text': '4. testTwo'
//                                                        , 'icon': 'images/testok.gif'},
//                                                    {'text': '5. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '6. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '7. testTwo', 'icon': 'images/testwarning.gif'},
//                                                    {'text': '8. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '9. testTwo', 'icon': 'images/testfail.gif'},
//                                                    {'text': '10. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '11. testTwo', 'icon': 'images/testrun.gif'},
//                                                    {'text': '4. testTwo'
//                                                        , 'icon': 'images/testok.gif'},
//                                                    {'text': '5. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '6. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '7. testTwo', 'icon': 'images/testwarning.gif'},
//                                                    {'text': '8. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '9. testTwo', 'icon': 'images/testfail.gif'},
//                                                    {'text': '10. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '11. testTwo', 'icon': 'images/testrun.gif'},
//                                                    {'text': '4. testTwo'
//                                                        , 'icon': 'images/testok.gif'},
//                                                    {'text': '5. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '6. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '7. testTwo', 'icon': 'images/testwarning.gif'},
//                                                    {'text': '8. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '9. testTwo', 'icon': 'images/testfail.gif'},
//                                                    {'text': '10. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '11. testTwo', 'icon': 'images/testrun.gif'},
//                                                    {'text': '4. testTwo'
//                                                        , 'icon': 'images/testok.gif'},
//                                                    {'text': '5. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '6. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '7. testTwo', 'icon': 'images/testwarning.gif'},
//                                                    {'text': '8. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '9. testTwo', 'icon': 'images/testfail.gif'},
//                                                    {'text': '10. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '11. testTwo', 'icon': 'images/testrun.gif'},
//                                                    {'text': '4. testTwo'
//                                                        , 'icon': 'images/testok.gif'},
//                                                    {'text': '5. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '6. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '7. testTwo', 'icon': 'images/testwarning.gif'},
//                                                    {'text': '8. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '9. testTwo', 'icon': 'images/testfail.gif'},
//                                                    {'text': '10. testTwo', 'icon': 'images/testok.gif'},
//                                                    {'text': '11. testTwo', 'icon': 'images/testrun.gif'},
//                                                ]
//                                            }
//
//
//
//
//                                        ]}
//                                ]



