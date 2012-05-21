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
        },
        patch: function() {
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
        },
        patch: function() {
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
        }
    },
    Audio: {
        patch: function() {
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
                });
                // replace the audio tag with out div
                audioclips[i].parentNode.replaceChild(newAudio, audioclips[i]);
            }
        }  
    },
    monkeypunch: function() {
        var scripts = document.getElementsByTagName('script'),
            len = scripts.length,
            src, 
            patch = "none";
        
        while (len--) {
            src = scripts[len].src;
            console.log("src = " + src);
            if (src && src.indexOf("corinthian") > -1) {
                console.log("I got a match");
                patch = scripts[len].getAttribute("corinthian-patch");
                break;
            }
        }
        
        if (patch === "none") {
            return;
        }
        else if (patch === "all") {
            corinthian.FileDialog.patch();
            corinthian.Video.patch();
            corinthian.Audio.patch();
        }
        else {
            if (patch.indexOf("audio") > -1) {
                corinthian.Audio.patch();
            }
            if (patch.indexOf("file") > -1) {
                corinthian.FileDialog.patch();
            }
            if (patch.indexOf("video") > -1) {
                corinthian.Video.patch();
            }
        }        
    }
};

document.addEventListener("deviceready", corinthian.monkeypunch, true);
