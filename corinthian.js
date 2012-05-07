var corinthian = {
    version: "0.1",
    mediaObjs: {},
    
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
            var me;
            if (videos[i].src) {
                me = videos[i].src;
            } else {
                me = videos[i].firstElementChild.src; 
            } 
            videos[i].addEventListener("click", function() {
                corinthian.Video.play(me);
            });
        }
        // patch audio
        var audioclips = document.getElementsByTagName("audio");
        for (var i=0; i < audioclips.length; i++) {
            // Create new Media object.
            var audioSrc = audioclips[i].firstElementChild.src;
            if (audioSrc.indexOf("file:///android_asset") == 0) {
                audioSrc = audioSrc.substring(7);
            }
            corinthian.mediaObjs[audioSrc] = new Media(audioSrc);
            // Create the HTML
            var newAudio = document.createElement('div');
            var newImg = document.createElement('img');
            newImg.setAttribute('src', 'images/play.png');
            newAudio.appendChild(newImg);
            // Set the onclick listener
            newAudio.addEventListener("click", function() {
                // figure out what image is displayed
                if (newImg.src.indexOf("images/play.png", newImg.src.length - "images/play.png".length) !== -1) {
                    newImg.src = "images/pause.png"; 
                    corinthian.mediaObjs[audioSrc].play();               
                } else {
                    newImg.src = "images/play.png";
                    corinthian.mediaObjs[audioSrc].pause();                
                }
            })
            document.body.appendChild(newAudio);
        }

    }
};

document.addEventListener("deviceready", corinthian.monkeypunch, true);
