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
    ContactPicker: {
        choose: function(successCallback, errorCallback) {
            var win = typeof successCallback !== 'function' ? null : function(contact) {
                successCallback(navigator.contacts.create(contact));
            };
            cordova.exec(win, errorCallback, "ContactPicker", "choose", []);
        }
    },
    Video: {
        play: function(url) {
            cordova.exec(null, null, "VideoPlayer", "playVideo", [url]);
        }
    },
    monkeypunch: function() {
        // patch file chooser
        var inputs = document.getElementsByTagName("input");        
        for (var i=0; i < inputs.length; i++) {
           if (inputs[i].getAttribute('type') == 'file'){
               var me = inputs[i];
               inputs[i].addEventListener("click", function() {
                   corinthian.FileDialog.pickFile(function(fileEntry) {
                       me.value = fileEntry.fullPath;
                   });
               });
           }
        }
        // patch videos
        var videos = document.getElementsByTagName("video");        
        for (var i=0; i < videos.length; i++) {
           var me = videos[i];
           videos[i].addEventListener("click", function() {
               corinthian.Video.play(me.src);
           });
        }
    }
};

document.addEventListener("deviceready", corinthian.monkeypunch, true);
