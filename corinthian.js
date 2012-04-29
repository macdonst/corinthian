var corinthian = {
    version: "0.1",
    
    FileDialog: {
        pickFile: function(successCallback, errorCallback, options) {
            var win = typeof successCallback !== 'function' ? null : function(f) {
                window.resolveLocalFileSystemURI(f, function(fileEntry) {
                    successCallback(fileEntry);                    
                }, fail);
            };
            cordova.exec(win, errorCallback, "FileDialog", "pickFile", [options]);
        },    
        pickFolder: function(successCallback, errorCallback, options) {
            var win = typeof successCallback !== 'function' ? null : function(d) {
                window.resolveLocalFileSystemURI(d, function(dirEntry) {
                    successCallback(dirEntry);                    
                }, fail);
            };
            cordova.exec(win, errorCallback, "FileDialog", "pickFolder", [options]);
        }    
    },
    punchInputFileType: function() {
        var inputs = document.getElementsByTagName("input");
        
        for (var i=0; i < inputs.length; i++) {
           console.log("input " + i);
           if (inputs[i].getAttribute('type') == 'file'){
               var me = inputs[i];
               inputs[i].addEventListener("click", function() {
                   corinthian.FileDialog.pickFile(function(fileEntry) {
                       me.value = fileEntry.fullPath;
                   });
               });
           }
        }
    },
    ContactPicker: {
        choose: function(successCallback, errorCallback) {
            var win = typeof successCallback !== 'function' ? null : function(contact) {
                successCallback(navigator.contacts.create(contact));
            };
            cordova.exec(win, errorCallback, "ContactPicker", "choose", []);
        }
    }
};

document.addEventListener("deviceready", corinthian.punchInputFileType, true);
